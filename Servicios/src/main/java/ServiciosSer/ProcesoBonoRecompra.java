package ServiciosSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import CapaDAOSer.TiendaDAO;
import ModeloSer.Tienda;
import capaDAOCC.BonoRecompraDAO;
import capaDAOCC.OfertaClienteDAO;
import capaDAOCC.ParametrosDAO;
import capaModeloCC.Correo;
import capaModeloCC.CorreoElectronico;
import utilidadesCC.ControladorEnvioCorreo;
import utilidadesCC.CorreoOferta;

/**
 * El bono de recompra: "compre del 1 al 15 y le devolvemos el 10% en un bono".
 *
 * POR QUE ESTO VIVE AQUI Y NO EN EL CENTRAL
 *
 * El central solo tiene los pedidos que el mismo tomo -medido el 2026-09-28,
 * tipos 1 y 4-. Todo el mostrador esta en la base de cada tienda. Si esto se
 * calculara en el central, el bono se lo ganaria unicamente quien pide a
 * domicilio y quien compra en el punto de venta nunca, por mas que cumpla.
 *
 * Este proceso entra a las once tiendas igual que ProcesoMantenerMaestroCRM, y
 * por eso ve el 100% de las compras.
 *
 * ORDEN: TIENE QUE CORRER DESPUES DEL MAESTRO DEL CRM
 *
 * Agrupa por cliente.idpersona, que es justo lo que el maestro le baja a las
 * tiendas. Corriendo antes, los clientes nuevos de hoy no tendrian persona y
 * sus compras no contarian para nadie.
 *
 * QUE NO CUENTA
 *
 *   - Los pedidos anulados.
 *   - Los que se pagaron con un codigo promocional. Esto NO es opcional: sin
 *     ello, el bono que alguien redime generaria otro bono, y ese otro, para
 *     siempre.
 *   - Los vendidos con promocion, si la campana lo pide.
 *   - Los productos que la campana no incluya.
 */
public class ProcesoBonoRecompra {

	/** Entre correo y correo. Mandar de golpe quema la reputacion del dominio. */
	private static final int SEGUNDOS_POR_DEFECTO = 20;

	/** Cuantos avisos por noche. Los que falten salen la noche siguiente. */
	private static final int MAXIMO_POR_DEFECTO = 200;

	public static void main(final String[] args) {
		final long arranque = System.currentTimeMillis();

		final ArrayList<BonoRecompraDAO.Campana> campanas = BonoRecompraDAO.paraCalcular();
		if (campanas.isEmpty()) {
			System.out.println("No hay campanas de bono activas. No se hace nada.");
			return;
		}
		System.out.println("Campanas activas: " + campanas.size());

		final Connection con = new conexionCC.ConexionBaseDatos().obtenerConexionBDPrincipal();
		if (con == null) {
			System.out.println("No se pudo conectar al central. No se hace nada.");
			return;
		}

		try {
			final ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();

			for (int c = 0; c < campanas.size(); c++) {
				final BonoRecompraDAO.Campana campana = campanas.get(c);
				System.out.println("=== " + campana.nombre + " (" + campana.compraDesde + " a "
						+ campana.compraHasta + ") ===");

				int nuevos = 0;
				int tiendasLeidas = 0;
				for (int i = 0; i < tiendas.size(); i++) {
					final Tienda tienda = tiendas.get(i);
					if (tienda.getHostBD() == null || tienda.getHostBD().trim().length() == 0) {
						continue;
					}
					final int n = leerTienda(con, campana, tienda);
					if (n >= 0) {
						nuevos += n;
						tiendasLeidas++;
						System.out.println("  " + tienda.getNombreTienda() + ": " + n + " pedidos nuevos");
					} else {
						System.out.println("  " + tienda.getNombreTienda() + ": no respondio, se salta");
					}
				}
				System.out.println("  tiendas leidas: " + tiendasLeidas + ", pedidos nuevos: " + nuevos);

				//Una tienda que no respondio deja compras sin contar. Emitir con
				//la foto incompleta le daria a esa gente un bono mas chico del
				//que se gano, y un bono ya emitido no se corrige.
				if (tiendasLeidas < tiendas.size() - contarSinHost(tiendas)) {
					System.out.println("  NO se cierra: falto alguna tienda y el bono quedaria corto.");
					continue;
				}

				final BonoRecompraDAO.Resultado r = BonoRecompraDAO.cerrar(campana, "bono-nocturno");
				System.out.println("  califican: " + r.califican + ", emitidos: " + r.emitidos
						+ ", fallidos: " + r.fallidos + ", valor: " + Math.round(r.valor));
				if (r.aviso.length() > 0) {
					System.out.println("  " + r.aviso);
				}

				if (campana.avisar && r.emitidos > 0) {
					final int avisados = avisar(con, campana);
					System.out.println("  avisados por correo: " + avisados);
				}
			}
			System.out.println("duracion: " + ((System.currentTimeMillis() - arranque) / 1000) + " s");
		} catch (final Exception e) {
			System.out.println("Fallo el proceso de bonos: " + e.toString());
		} finally {
			try {
				con.close();
			} catch (final Exception e) {
			}
		}
	}

	private static int contarSinHost(final ArrayList<Tienda> tiendas) {
		int n = 0;
		for (int i = 0; i < tiendas.size(); i++) {
			final String h = tiendas.get(i).getHostBD();
			if (h == null || h.trim().length() == 0) {
				n++;
			}
		}
		return (n);
	}

	// =======================================================================
	// Leer las compras de una tienda
	// =======================================================================

	/**
	 * Trae los pedidos de esta tienda que cuentan para esta campana.
	 *
	 * Se suma por PEDIDO y no por linea: el libro guarda un renglon por pedido,
	 * que es lo que permite contestar "por que a este cliente le dieron $12.400"
	 * sin volver a entrar a la tienda.
	 *
	 * @return cuantos pedidos nuevos entraron, o -1 si la tienda no respondio
	 */
	private static int leerTienda(final Connection con, final BonoRecompraDAO.Campana campana,
			final Tienda tienda) {
		Connection cnTienda = null;
		try {
			cnTienda = new capaConexionPOS.ConexionBaseDatos()
					.obtenerConexionBDTiendaRemota(tienda.getHostBD());
			if (cnTienda == null) {
				return (-1);
			}
			if (!tieneColumnaIdPersona(cnTienda)) {
				//Tienda a la que todavia no le corrieron el script del CRM. Sin
				//idpersona no hay a quien darle el bono.
				return (-1);
			}

			final StringBuilder sql = new StringBuilder();
			sql.append("SELECT p.idpedidotienda AS idpedido, c.idpersona AS idpersona,")
				.append("       DATE(p.fechapedido) AS fecha, SUM(d.valortotal) AS base")
				.append("  FROM pedido p")
				.append("  JOIN cliente c ON c.idcliente = p.idcliente")
				.append("  JOIN detalle_pedido d ON d.idpedidotienda = p.idpedidotienda")
				.append(" WHERE p.idmotivoanulacion IS NULL")
				.append("   AND c.idpersona IS NOT NULL")
				.append("   AND p.fechapedido >= ?")
				.append("   AND p.fechapedido < DATE_ADD(?, INTERVAL 1 DAY)");

			//Los productos que cuentan. Vacio = todo lo que se vendio.
			final String productos = soloNumeros(campana.productos);
			if (productos.length() > 0) {
				sql.append("   AND d.idproducto IN (").append(productos).append(")");
			}

			//Un pedido pagado con codigo promocional NUNCA cuenta. Si contara,
			//el bono que alguien redime generaria otro bono sin fin.
			sql.append("   AND NOT EXISTS (SELECT 1 FROM pedido_descuento pd")
				.append("                   WHERE pd.idpedido = p.idpedidotienda")
				.append("                     AND pd.codigo_estado = 'C')");

			if (campana.excluirPromociones) {
				sql.append("   AND NOT EXISTS (SELECT 1 FROM pedido_promocion pp")
					.append("                   WHERE pp.idpedidotienda = p.idpedidotienda)");
			}

			sql.append(" GROUP BY p.idpedidotienda, c.idpersona, DATE(p.fechapedido)")
				.append(" HAVING SUM(d.valortotal) > 0");

			final PreparedStatement psLee = cnTienda.prepareStatement(sql.toString());
			psLee.setString(1, campana.compraDesde);
			psLee.setString(2, campana.compraHasta);
			final ResultSet rs = psLee.executeQuery();

			final ArrayList<long[]> claves = new ArrayList<long[]>();
			final ArrayList<Double> bases = new ArrayList<Double>();
			final ArrayList<java.sql.Date> fechas = new ArrayList<java.sql.Date>();
			while (rs.next()) {
				claves.add(new long[] { rs.getLong("idpedido"), rs.getLong("idpersona") });
				bases.add(Double.valueOf(rs.getDouble("base")));
				fechas.add(rs.getDate("fecha"));
			}
			rs.close();
			psLee.close();

			if (claves.isEmpty()) {
				return (0);
			}
			return (BonoRecompraDAO.registrarPedidos(con, campana.idBono, tienda.getIdTienda(),
					claves, bases, fechas));
		} catch (final Exception e) {
			System.out.println("  fallo leyendo " + tienda.getNombreTienda() + ": " + e.toString());
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

	/**
	 * La lista de productos, dejando solo numeros y comas.
	 *
	 * Va concatenada a la consulta porque IN (?) no existe, asi que lo que se
	 * concatena tiene que ser imposible de usar para otra cosa. Cualquier cosa
	 * que no sea digito o coma se cae aqui.
	 */
	private static String soloNumeros(final String lista) {
		if (lista == null) {
			return ("");
		}
		final StringBuilder limpio = new StringBuilder();
		final String[] partes = lista.split(",");
		for (int i = 0; i < partes.length; i++) {
			final String p = partes[i].trim();
			if (p.length() > 0 && p.matches("[0-9]+")) {
				if (limpio.length() > 0) {
					limpio.append(",");
				}
				limpio.append(p);
			}
		}
		return (limpio.toString());
	}

	private static boolean tieneColumnaIdPersona(final Connection cnTienda) {
		try {
			final Statement st = cnTienda.createStatement();
			final ResultSet rs = st.executeQuery("SHOW COLUMNS FROM cliente LIKE 'idpersona'");
			final boolean hay = rs.next();
			rs.close();
			st.close();
			return (hay);
		} catch (final Exception e) {
			return (false);
		}
	}

	// =======================================================================
	// Avisarle a la gente
	// =======================================================================

	/**
	 * Le manda a cada uno el correo de su bono.
	 *
	 * Espaciado y con tope por noche: un bono se lo pueden ganar miles y soltar
	 * miles de correos de golpe desde la cuenta de la empresa quema la
	 * reputacion del dominio. Los que no alcancen quedan en EMITIDO y salen la
	 * noche siguiente; el codigo ya esta emitido, asi que nadie pierde nada.
	 *
	 * @return cuantos avisos salieron
	 */
	private static int avisar(final Connection con, final BonoRecompraDAO.Campana campana) {
		final int segundos = parametro("BONOSEGUNDOSCORREO", SEGUNDOS_POR_DEFECTO);
		final int maximo = parametro("BONOMAXCORREOSNOCHE", MAXIMO_POR_DEFECTO);
		int salieron = 0;
		try {
			final PreparedStatement ps = con.prepareStatement(
					"SELECT e.idemision, e.idofertacliente, IFNULL(r.email,'') AS correo"
					+ "  FROM pizzaamericana.bono_emitido e"
					+ "  LEFT JOIN crm.persona_resumen r ON r.idpersona = e.idpersona"
					+ " WHERE e.idbono = ? AND e.estado = 'EMITIDO'"
					+ "   AND e.idofertacliente IS NOT NULL"
					+ " ORDER BY e.valor DESC LIMIT ?");
			ps.setInt(1, campana.idBono);
			ps.setInt(2, maximo);
			final ResultSet rs = ps.executeQuery();

			final ArrayList<int[]> pendientes = new ArrayList<int[]>();
			final ArrayList<String> correos = new ArrayList<String>();
			while (rs.next()) {
				pendientes.add(new int[] { rs.getInt("idemision"), rs.getInt("idofertacliente") });
				correos.add(rs.getString("correo"));
			}
			rs.close();
			ps.close();

			final CorreoElectronico cuenta = ControladorEnvioCorreo.recuperarCorreo(
					"CUENTACORREOREPORTES", "CLAVECORREOREPORTE");

			for (int i = 0; i < pendientes.size(); i++) {
				final int idEmision = pendientes.get(i)[0];
				final int idOfertaCliente = pendientes.get(i)[1];
				final String destino = correos.get(i) == null ? "" : correos.get(i).trim();

				if (!ControladorEnvioCorreo.esDireccionValida(destino)) {
					marcar(con, idEmision, "FALLIDO", "Sin correo valido: " + destino, destino);
					continue;
				}

				final OfertaClienteDAO.DatosCorreoOferta datos =
						OfertaClienteDAO.obtenerDatosCorreoOferta(idOfertaCliente);
				if (!datos.seLeyo) {
					marcar(con, idEmision, "FALLIDO", "No se pudieron leer los datos de la oferta", destino);
					continue;
				}

				final Correo correo = new Correo();
				correo.setAsunto(CorreoOferta.armarAsunto(datos));
				correo.setUsuarioCorreo(cuenta.getCuentaCorreo());
				correo.setContrasena(cuenta.getClaveCorreo());
				correo.setMensaje(CorreoOferta.armarCuerpo(datos));
				final ArrayList<String> destinos = new ArrayList<String>();
				destinos.add(destino);

				final ControladorEnvioCorreo envio = new ControladorEnvioCorreo(correo, destinos);
				final ControladorEnvioCorreo.ResultadoEnvio res = envio.enviarCorreoClasificado();

				if (res == ControladorEnvioCorreo.ResultadoEnvio.ENVIADO) {
					OfertaClienteDAO.marcarCorreoEnviado(idOfertaCliente);
					marcar(con, idEmision, "AVISADO", "", destino);
					salieron++;
				} else {
					//El codigo NO se anula: el cliente se lo gano. Queda en
					//EMITIDO para reintentar manana, o para dictarselo por
					//telefono si vuelve a fallar.
					marcar(con, idEmision, "EMITIDO", "No salio el correo: " + res, destino);
				}

				if (i + 1 < pendientes.size()) {
					Thread.sleep(segundos * 1000L);
				}
			}
		} catch (final Exception e) {
			System.out.println("  fallo avisando: " + e.toString());
		}
		return (salieron);
	}

	private static void marcar(final Connection con, final int idEmision, final String estado,
			final String detalle, final String destino) {
		try (PreparedStatement ps = con.prepareStatement(
				"UPDATE pizzaamericana.bono_emitido SET estado = ?, detalle = ?, destino = ?,"
				+ " avisado_en = CASE WHEN ? = 'AVISADO' THEN NOW() ELSE avisado_en END"
				+ " WHERE idemision = ?")) {
			ps.setString(1, estado);
			ps.setString(2, detalle.length() > 300 ? detalle.substring(0, 300) : detalle);
			ps.setString(3, destino.length() > 120 ? destino.substring(0, 120) : destino);
			ps.setString(4, estado);
			ps.setInt(5, idEmision);
			ps.executeUpdate();
		} catch (final Exception e) {
			System.out.println("  no se pudo marcar la emision " + idEmision + ": " + e.toString());
		}
	}

	private static int parametro(final String nombre, final int porDefecto) {
		try {
			final int v = ParametrosDAO.retornarValorNumerico(nombre);
			return (v > 0 ? v : porDefecto);
		} catch (final Exception e) {
			return (porDefecto);
		}
	}
}
