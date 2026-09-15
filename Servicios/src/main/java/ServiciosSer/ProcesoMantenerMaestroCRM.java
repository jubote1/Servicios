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
				tiendasLeidas++;
			}

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
