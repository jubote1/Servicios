package ServiciosSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import CapaDAOSer.TiendaDAO;
import ModeloSer.Tienda;
import capaDAOPOS.ClienteMaestroDAO;

/**
 * Mantiene al dia el maestro de personas del CRM.
 *
 * El maestro se armo el 2026-09-15 con 449.731 personas. Eso fue UNA FOTO: sin
 * este proceso, cada cliente nuevo que entre al central o a una tienda se queda
 * por fuera. Medido ese dia: 530 clientes nuevos por semana en el central y 178
 * en Manrique sola, o sea del orden de 2.000 por semana entre todos. En un ano
 * son mas de cien mil personas que el maestro no conoceria.
 *
 * Hace lo mismo que los scripts 2026_09_15_03 y _06, pero sobre lo que llego
 * desde la ultima corrida. Es idempotente: correrlo dos veces seguidas no
 * cambia nada la segunda.
 *
 * NADA SE BORRA NI SE FUSIONA, y el maestro sigue siendo append-only: el
 * idpersona va a vivir en las bases de tienda, asi que un id reasignado dejaria
 * a una tienda apuntando a otra persona.
 *
 * Corre de noche: lee las once tiendas completas y eso mueve 665 mil filas.
 */
public class ProcesoMantenerMaestroCRM {

	/** Cuantas filas se mandan por lote al cargar la tabla de paso. */
	private static final int LOTE = 500;

	public static void main(final String[] args) {
		final long arranque = System.currentTimeMillis();
		int tiendasLeidas = 0;
		int tiendasConFalla = 0;

		final Connection con = new conexionCC.ConexionBaseDatos().obtenerConexionBDPrincipal();
		if (con == null) {
			System.out.println("No se pudo conectar al central. No se hace nada.");
			return;
		}

		try {
			//1. El central primero. Es barato y deja el maestro coherente aunque
			//   despues falle alguna tienda.
			System.out.println("--- central ---");
			final int nuevasCentral = actualizarCentral(con);
			System.out.println("personas nuevas desde el central: " + nuevasCentral);

			//2. Cada tienda. Una que falle no detiene a las demas.
			final ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
			for (int i = 0; i < tiendas.size(); i++) {
				final Tienda tienda = tiendas.get(i);
				if (tienda.getHostBD() == null || tienda.getHostBD().trim().length() == 0) {
					continue;
				}
				System.out.println("--- " + tienda.getNombreTienda() + " ---");
				final ArrayList<ClienteMaestroDAO.ClienteTienda> clientes =
						ClienteMaestroDAO.obtenerClientesParaMaestro(tienda.getHostBD());
				if (clientes.isEmpty()) {
					System.out.println("  no respondio o no tiene clientes con celular. Se salta.");
					tiendasConFalla++;
					continue;
				}
				cargarEnTablaDePaso(con, tienda.getIdTienda(), clientes);
				final int nuevas = actualizarDesdeTienda(con, tienda.getIdTienda());
				System.out.println("  " + clientes.size() + " clientes leidos, " + nuevas + " personas nuevas");

				//El vinculo se devuelve a la tienda. Va DESPUES de resolver, para
				//que baje ya con las personas nuevas de esta misma corrida.
				final int propagados = propagarIdPersona(con, tienda.getIdTienda(), tienda.getHostBD());
				if (propagados >= 0) {
					System.out.println("  idpersona propagado a " + propagados + " clientes");
				}

				//Y de una vez se trae lo que el central no tiene: los pedidos de
				//mostrador y para llevar, agregados por persona. Va despues de
				//propagar porque agrupa por cliente.idpersona, que es lo que
				//acaba de bajar.
				final int conPedidos = acumularPedidosTienda(con, tienda.getIdTienda(), tienda.getHostBD());
				if (conPedidos >= 0) {
					System.out.println("  " + conPedidos + " personas con pedidos de mostrador");
				}
				tiendasLeidas++;
			}

			//3. Y con las once tiendas ya cargadas, el resumen por persona que
			//   lee el CRM. Va al final a proposito: se arma completo o no se
			//   cambia, no queda a medias.
			recalcularResumen(con);

			System.out.println("--- resumen ---");
			imprimirResumen(con);
			System.out.println("tiendas leidas: " + tiendasLeidas + ", con falla: " + tiendasConFalla);
			System.out.println("duracion: " + ((System.currentTimeMillis() - arranque) / 1000) + " s");
		} catch (final Exception e) {
			System.out.println("Fallo el mantenimiento del maestro: " + e.toString());
		} finally {
			try {
				con.close();
			} catch (final Exception e) {
			}
		}
	}

	/**
	 * Le asigna persona a los clientes del central que no la tengan.
	 *
	 * @return cuantas personas nuevas se crearon
	 */
	private static int actualizarCentral(final Connection con) throws Exception {
		final Statement stm = con.createStatement();
		final int antes = contarPersonas(con);

		//Los comodines que hayan aparecido desde la ultima corrida.
		stm.executeUpdate("INSERT IGNORE INTO crm.celular_no_usable (celular_norm, motivo, filas)"
				+ " SELECT celular_norm, 'Diez digitos iguales', COUNT(*) FROM cliente"
				+ "  WHERE celular_norm IS NOT NULL AND celular_norm = REPEAT(LEFT(celular_norm,1),10)"
				+ "  GROUP BY celular_norm");

		//Las personas que falten.
		stm.executeUpdate("INSERT IGNORE INTO crm.persona (celular_norm, origen)"
				+ " SELECT DISTINCT c.celular_norm, 'CENTRAL' FROM cliente c"
				+ "  WHERE c.celular_norm IS NOT NULL AND c.idpersona IS NULL"
				+ "    AND NOT EXISTS (SELECT 1 FROM crm.celular_no_usable x"
				+ "                     WHERE x.celular_norm = c.celular_norm)");

		//Y se cuelgan. Se respeta el alias: si la persona resulto ser la misma que
		//otra -unidas por correo-, la fila cuelga de la principal.
		stm.executeUpdate("UPDATE cliente c JOIN crm.persona p ON p.celular_norm = c.celular_norm"
				+ "   SET c.idpersona = IFNULL(p.idpersona_principal, p.idpersona)"
				+ " WHERE c.idpersona IS NULL"
				+ "   AND NOT EXISTS (SELECT 1 FROM crm.celular_no_usable x"
				+ "                    WHERE x.celular_norm = c.celular_norm)");

		stm.close();
		return (contarPersonas(con) - antes);
	}

	/** Mete los clientes de la tienda en la tabla de paso, sin borrar lo que hay. */
	private static void cargarEnTablaDePaso(final Connection con, final int idTienda,
			final ArrayList<ClienteMaestroDAO.ClienteTienda> clientes) throws Exception {
		//Upsert y no DELETE+INSERT: asi un cliente que cambio de numero se
		//actualiza, y el idpersona que ya tenia no se pierde mientras tanto.
		final String sql = "INSERT INTO crm.stage_cliente_tienda"
				+ " (idtienda, idcliente, celular_norm, nombre, apellido, email, politica_datos)"
				+ " VALUES (?,?,?,?,?,?,?)"
				+ " ON DUPLICATE KEY UPDATE celular_norm=VALUES(celular_norm), nombre=VALUES(nombre),"
				+ " apellido=VALUES(apellido), email=VALUES(email), politica_datos=VALUES(politica_datos)";
		final PreparedStatement ps = con.prepareStatement(sql);
		int enLote = 0;
		for (int i = 0; i < clientes.size(); i++) {
			final ClienteMaestroDAO.ClienteTienda c = clientes.get(i);
			ps.setInt(1, idTienda);
			ps.setInt(2, c.idCliente);
			ps.setString(3, c.celularNorm);
			ps.setString(4, c.nombre);
			ps.setString(5, c.apellido);
			ps.setString(6, c.email);
			ps.setString(7, c.politicaDatos);
			ps.addBatch();
			enLote++;
			if (enLote >= LOTE) {
				ps.executeBatch();
				enLote = 0;
			}
		}
		if (enLote > 0) {
			ps.executeBatch();
		}
		ps.close();
	}

	/**
	 * Cuelga de su persona los clientes de una tienda que no la tengan.
	 *
	 * Mismo orden que el paso 2, y por la misma razon: primero se pega a quien ya
	 * existe -por celular, despues por correo y nombre- y solo al final se crea.
	 * Al reves, quien cambio de numero entraria como persona nueva y quedaria
	 * duplicado de alguien que ya estaba.
	 *
	 * @return cuantas personas nuevas se crearon
	 */
	private static int actualizarDesdeTienda(final Connection con, final int idTienda) throws Exception {
		final Statement stm = con.createStatement();
		final int antes = contarPersonas(con);
		final String primerNombreStage =
				"UPPER(SUBSTRING_INDEX(TRIM(REGEXP_REPLACE(IFNULL(s.nombre,''),'[^A-Za-z ]','')),' ',1))";
		final String primerNombrePersona =
				"UPPER(SUBSTRING_INDEX(TRIM(REGEXP_REPLACE(IFNULL(p2.nombre,''),'[^A-Za-z ]','')),' ',1))";

		//Comodines propios de esta tienda.
		stm.executeUpdate("INSERT IGNORE INTO crm.celular_no_usable (celular_norm, motivo, filas)"
				+ " SELECT celular_norm, 'Diez digitos iguales', COUNT(*)"
				+ "   FROM crm.stage_cliente_tienda WHERE idtienda = " + idTienda
				+ "    AND celular_norm IS NOT NULL AND celular_norm = REPEAT(LEFT(celular_norm,1),10)"
				+ "  GROUP BY celular_norm");

		//1. Por celular.
		stm.executeUpdate("UPDATE crm.stage_cliente_tienda s"
				+ "   JOIN crm.persona p ON p.celular_norm = s.celular_norm"
				+ "    SET s.idpersona = IFNULL(p.idpersona_principal, p.idpersona), s.como_se_pego = 'celular'"
				+ "  WHERE s.idtienda = " + idTienda + " AND s.idpersona IS NULL"
				+ "    AND NOT EXISTS (SELECT 1 FROM crm.celular_no_usable x"
				+ "                     WHERE x.celular_norm = s.celular_norm)");

		//2. Por correo y primer nombre, para quien cambio de numero.
		stm.executeUpdate("UPDATE crm.stage_cliente_tienda s"
				+ "   JOIN cliente c ON c.email_norm = s.email_norm"
				+ "   JOIN crm.persona p2 ON p2.idpersona = c.idpersona"
				+ "    SET s.idpersona = IFNULL(p2.idpersona_principal, p2.idpersona),"
				+ "        s.como_se_pego = 'correo y nombre'"
				+ "  WHERE s.idtienda = " + idTienda + " AND s.idpersona IS NULL"
				+ "    AND s.email_norm IS NOT NULL"
				+ "    AND NOT EXISTS (SELECT 1 FROM crm.email_no_usable x WHERE x.email = s.email_norm)"
				+ "    AND NOT EXISTS (SELECT 1 FROM crm.celular_no_usable y"
				+ "                     WHERE y.celular_norm = s.celular_norm)"
				+ "    AND " + primerNombreStage + " <> ''"
				+ "    AND " + primerNombreStage + " = " + primerNombrePersona);

		//3. Lo que quede, persona nueva.
		stm.executeUpdate("INSERT IGNORE INTO crm.persona"
				+ " (celular_norm, nombre, apellido, email, politica_datos, origen, idtienda_origen)"
				+ " SELECT s.celular_norm,"
				+ "        SUBSTRING_INDEX(GROUP_CONCAT(NULLIF(TRIM(s.nombre),'')   ORDER BY s.idcliente DESC),',',1),"
				+ "        SUBSTRING_INDEX(GROUP_CONCAT(NULLIF(TRIM(s.apellido),'') ORDER BY s.idcliente DESC),',',1),"
				+ "        SUBSTRING_INDEX(GROUP_CONCAT(NULLIF(TRIM(s.email),'')    ORDER BY s.idcliente DESC),',',1),"
				+ "        IF(MAX(s.politica_datos = 'S') = 1, 'S', 'N'), 'TIENDA', " + idTienda
				+ "   FROM crm.stage_cliente_tienda s"
				+ "  WHERE s.idtienda = " + idTienda + " AND s.idpersona IS NULL AND s.celular_norm IS NOT NULL"
				+ "    AND NOT EXISTS (SELECT 1 FROM crm.celular_no_usable x"
				+ "                     WHERE x.celular_norm = s.celular_norm)"
				+ "  GROUP BY s.celular_norm");

		//4. Y se cuelgan de la que se acaba de crear. Se excluyen comodines: sin
		//   esto, uno que ya tuviera persona se pegaba igual -se me paso la primera
		//   vez, corregido en 2026_09_15_07-.
		stm.executeUpdate("UPDATE crm.stage_cliente_tienda s"
				+ "   JOIN crm.persona p ON p.celular_norm = s.celular_norm"
				+ "    SET s.idpersona = IFNULL(p.idpersona_principal, p.idpersona),"
				+ "        s.como_se_pego = 'persona nueva'"
				+ "  WHERE s.idtienda = " + idTienda + " AND s.idpersona IS NULL"
				+ "    AND NOT EXISTS (SELECT 1 FROM crm.celular_no_usable x"
				+ "                     WHERE x.celular_norm = s.celular_norm)");

		stm.close();
		return (contarPersonas(con) - antes);
	}

	/**
	 * Devuelve a la tienda el idpersona de cada uno de sus clientes.
	 *
	 * POR QUE EL VINCULO TIENE QUE VIVIR TAMBIEN EN LA TIENDA
	 *
	 * Hasta ahora vivia solo en el central, en crm.stage_cliente_tienda. Con la
	 * columna en la tienda, la tienda sabe a que persona corresponde cada
	 * cliente: un cambio hecho alla sube sabiendo a quien pertenece, y el
	 * vinculo sobrevive a un cambio de celular, que es lo que hoy rompe la
	 * identificacion.
	 *
	 * SOLO SE ESCRIBE LO QUE CAMBIA
	 *
	 * Se lee primero lo que la tienda YA tiene y se comparan en memoria. Son
	 * unas sesenta mil filas por tienda: cabe de sobra, y evita mandar 650 mil
	 * UPDATE cada noche para reescribir lo mismo. En la primera corrida bajan
	 * todos; despues solo los nuevos y los que cambiaron de persona.
	 *
	 * SI LA COLUMNA NO EXISTE, NO PASA NADA
	 *
	 * La tienda puede ir atrasada con la migracion 2026_09_19_02. En ese caso se
	 * avisa y se sigue: el maestro del central ya quedo bien, que es lo que no
	 * se puede perder. Reventar aqui dejaria a las tiendas siguientes sin
	 * procesar por una columna que falta en esta.
	 *
	 * @return cuantos clientes se actualizaron, o -1 si la tienda no esta lista
	 */
	private static int propagarIdPersona(final Connection con, final int idTienda, final String hostBD) {
		Connection cnTienda = null;
		try {
			cnTienda = new capaConexionPOS.ConexionBaseDatos().obtenerConexionBDTiendaRemota(hostBD);
			if (cnTienda == null) {
				System.out.println("  no se pudo conectar para propagar el idpersona.");
				return (-1);
			}
			if (!tieneColumnaIdPersona(cnTienda)) {
				System.out.println("  la tienda no tiene la columna idpersona todavia."
						+ " Corra sql/2026_09_19_02_idpersona_en_cliente.sql. Se sigue sin propagar.");
				return (-1);
			}

			//Lo que la tienda ya tiene.
			final java.util.HashMap<Integer, Long> enTienda = new java.util.HashMap<Integer, Long>();
			final Statement stLee = cnTienda.createStatement();
			final ResultSet rsLee = stLee.executeQuery("SELECT idcliente, idpersona FROM cliente");
			while (rsLee.next()) {
				final long idp = rsLee.getLong("idpersona");
				enTienda.put(Integer.valueOf(rsLee.getInt("idcliente")),
						rsLee.wasNull() ? null : Long.valueOf(idp));
			}
			rsLee.close();
			stLee.close();

			//Lo que el maestro dice que deberia tener.
			final PreparedStatement psMaestro = con.prepareStatement(
					"SELECT idcliente, idpersona FROM crm.stage_cliente_tienda"
					+ " WHERE idtienda = ? AND idpersona IS NOT NULL");
			psMaestro.setInt(1, idTienda);
			final ResultSet rsMaestro = psMaestro.executeQuery();

			final PreparedStatement psEscribe = cnTienda.prepareStatement(
					"UPDATE cliente SET idpersona = ? WHERE idcliente = ?");
			int cambios = 0;
			int enLote = 0;
			while (rsMaestro.next()) {
				final int idCliente = rsMaestro.getInt("idcliente");
				final long idPersona = rsMaestro.getLong("idpersona");
				final Long actual = enTienda.get(Integer.valueOf(idCliente));
				//Si ya esta igual no se toca: reescribir lo mismo solo gasta
				//tiempo y mueve la fecha de modificacion de la fila.
				if (actual != null && actual.longValue() == idPersona) {
					continue;
				}
				//Y si el cliente no existe en la tienda tampoco: seria una fila
				//borrada alla, y el UPDATE no haria nada.
				if (!enTienda.containsKey(Integer.valueOf(idCliente))) {
					continue;
				}
				psEscribe.setLong(1, idPersona);
				psEscribe.setInt(2, idCliente);
				psEscribe.addBatch();
				cambios++;
				enLote++;
				if (enLote >= LOTE) {
					psEscribe.executeBatch();
					enLote = 0;
				}
			}
			if (enLote > 0) {
				psEscribe.executeBatch();
			}
			psEscribe.close();
			rsMaestro.close();
			psMaestro.close();
			return (cambios);
		} catch (final Exception e) {
			System.out.println("  fallo la propagacion del idpersona: " + e.toString());
			return (-1);
		} finally {
			try {
				if (cnTienda != null) {
					cnTienda.close();
				}
			} catch (final Exception e) {
			}
		}
	}

	/** Si la tienda ya corrio la migracion que agrega la columna. */
	private static boolean tieneColumnaIdPersona(final Connection cnTienda) {
		try {
			final Statement stm = cnTienda.createStatement();
			final ResultSet rs = stm.executeQuery(
					"SELECT COUNT(*) AS hay FROM information_schema.COLUMNS"
					+ " WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cliente'"
					+ "   AND COLUMN_NAME = 'idpersona'");
			boolean hay = false;
			if (rs.next()) {
				hay = (rs.getInt("hay") > 0);
			}
			rs.close();
			stm.close();
			return (hay);
		} catch (final Exception e) {
			//Ante la duda, no se escribe.
			System.out.println("  no se pudo verificar la columna idpersona: " + e.toString());
			return (false);
		}
	}

	private static int contarPersonas(final Connection con) throws Exception {
		final Statement stm = con.createStatement();
		final ResultSet rs = stm.executeQuery("SELECT COUNT(*) FROM crm.persona");
		int total = 0;
		if (rs.next()) {
			total = rs.getInt(1);
		}
		rs.close();
		stm.close();
		return (total);
	}

	/**
	 * Sube a la tabla de paso los pedidos de mostrador y para llevar de una
	 * tienda, ya agregados por persona.
	 *
	 * EL CENTRAL NO TIENE ESTOS PEDIDOS. Medido el 2026-09-20 sobre siete dias:
	 * el central ve 4.455 de 7.486 pedidos de la cadena. El 41% -los tipos 2 y
	 * 3- solo existe en la base de la tienda. Un CRM armado unicamente con
	 * pizzaamericana.pedido diria que un cliente de mostrador no compra nunca,
	 * y esa es la clase de error que no se nota: da un numero, no da un error.
	 *
	 * Se manda AGREGADO y no pedido por pedido: una fila por persona en vez de
	 * decenas de miles de pedidos. Manrique, la mas grande, resuelve su
	 * agregado en 2,6 segundos y devuelve 18 mil filas.
	 *
	 * Los tipos 1 -domicilio- y 4 -virtual recoger- NO se traen: esos si suben
	 * al central, y traerlos seria contarlos dos veces. Los cuatro ids son
	 * identicos en las once tiendas, se verifico uno por uno.
	 *
	 * @return cuantas personas quedaron, o -1 si la tienda no respondio
	 */
	private static int acumularPedidosTienda(final Connection con, final int idTienda,
			final String hostBD) {
		Connection cnTienda = null;
		try {
			cnTienda = new capaConexionPOS.ConexionBaseDatos().obtenerConexionBDTiendaRemota(hostBD);
			if (cnTienda == null) {
				System.out.println("  no se pudo conectar para leer los pedidos de mostrador.");
				return (-1);
			}
			if (!tieneColumnaIdPersona(cnTienda)) {
				//Sin la columna no hay por donde agrupar. No es una falla: es
				//una tienda a la que todavia no le corrieron el script.
				return (-1);
			}

			final Statement stLee = cnTienda.createStatement();
			final ResultSet rs = stLee.executeQuery(
					"SELECT c.idpersona AS idpersona, COUNT(*) AS pedidos,"
					+ " IFNULL(SUM(p.total_neto),0) AS valor,"
					+ " MIN(p.fechapedido) AS primero, MAX(p.fechapedido) AS ultimo"
					+ " FROM pedido p JOIN cliente c ON c.idcliente = p.idcliente"
					+ " WHERE p.idtipopedido IN (2,3) AND p.idmotivoanulacion IS NULL"
					+ " AND c.idpersona IS NOT NULL"
					+ " GROUP BY c.idpersona");

			//Se reemplaza lo de ESTA tienda y no se acumula: lo que llega es la
			//foto completa. Borrar e insertar dentro de una transaccion evita
			//que el recalculo alcance a ver la tienda a medio cargar.
			con.setAutoCommit(false);
			final PreparedStatement psBorra = con.prepareStatement(
					"DELETE FROM crm.stage_pedido_tienda WHERE idtienda = ?");
			psBorra.setInt(1, idTienda);
			psBorra.executeUpdate();
			psBorra.close();

			final PreparedStatement psEscribe = con.prepareStatement(
					"INSERT INTO crm.stage_pedido_tienda"
					+ " (idtienda, idpersona, pedidos, valor, primer_pedido, ultimo_pedido)"
					+ " VALUES (?, ?, ?, ?, ?, ?)");
			int filas = 0;
			int enLote = 0;
			while (rs.next()) {
				psEscribe.setInt(1, idTienda);
				psEscribe.setLong(2, rs.getLong("idpersona"));
				psEscribe.setInt(3, rs.getInt("pedidos"));
				psEscribe.setDouble(4, rs.getDouble("valor"));
				psEscribe.setDate(5, rs.getDate("primero"));
				psEscribe.setDate(6, rs.getDate("ultimo"));
				psEscribe.addBatch();
				filas++;
				enLote++;
				if (enLote >= LOTE) {
					psEscribe.executeBatch();
					enLote = 0;
				}
			}
			if (enLote > 0) {
				psEscribe.executeBatch();
			}
			psEscribe.close();
			rs.close();
			stLee.close();
			con.commit();
			con.setAutoCommit(true);
			return (filas);
		} catch (final Exception e) {
			//El rollback importa: sin el, la tienda quedaria con las filas
			//borradas y sin las nuevas, o sea reportando cero pedidos de
			//mostrador hasta la noche siguiente.
			try {
				con.rollback();
				con.setAutoCommit(true);
			} catch (final Exception e2) {
			}
			System.out.println("  fallo la carga de pedidos de mostrador: " + e.toString());
			return (-1);
		} finally {
			try {
				cnTienda.close();
			} catch (final Exception e) {
			}
		}
	}

	/**
	 * Manda recalcular el resumen por persona que lee el CRM.
	 *
	 * El calculo vive en el procedimiento crm.pr_recalcular_persona_resumen y
	 * no aqui, a proposito: asi afinar la formula de un segmento o corregir un
	 * conteo es correr un archivo .sql, y no volver a armar y desplegar el jar.
	 * En esta casa eso pesa -el jar del maestro estuvo cuatro dias sin
	 * actualizar y nadie lo noto-.
	 *
	 * Tarda algo mas de un minuto: recorre los 797 mil pedidos del central una
	 * sola vez y de ahi saca todo lo demas.
	 *
	 * Si falla no se tumba la corrida: el maestro ya quedo al dia, que es lo
	 * importante, y el resumen se puede recalcular a mano con un CALL.
	 */
	private static void recalcularResumen(final Connection con) {
		try {
			final long arranque = System.currentTimeMillis();
			final Statement stm = con.createStatement();
			stm.execute("CALL crm.pr_recalcular_persona_resumen()");
			stm.close();
			System.out.println("resumen del CRM recalculado en "
					+ ((System.currentTimeMillis() - arranque) / 1000) + " s");
		} catch (final Exception e) {
			System.out.println("fallo el recalculo del resumen: " + e.toString());
		}
	}

	private static void imprimirResumen(final Connection con) throws Exception {
		final Statement stm = con.createStatement();
		final ResultSet rs = stm.executeQuery(
				"SELECT (SELECT COUNT(*) FROM crm.persona WHERE idpersona_principal IS NULL AND activa='S'),"
				+ " (SELECT COUNT(*) FROM cliente WHERE idpersona IS NULL AND celular_norm IS NOT NULL),"
				+ " (SELECT COUNT(*) FROM crm.stage_cliente_tienda WHERE idpersona IS NULL)");
		if (rs.next()) {
			System.out.println("personas reales en el maestro : " + rs.getInt(1));
			System.out.println("central sin colgar            : " + rs.getInt(2));
			System.out.println("tienda sin colgar             : " + rs.getInt(3));
		}
		rs.close();
		stm.close();
	}
}
