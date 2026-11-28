package ServiciosSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import CapaDAOSer.GeneralDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import utilidadesSer.ControladorEnvioCorreo;

/**
 * Vigilante de encuestas llenadas en masa.
 *
 * EL PROBLEMA. El QR del tiquete lleva el numero de pedido a la vista en la
 * direccion, asi que cambiandolo en el navegador se contesta la encuesta de un
 * pedido ajeno, y cada encuesta da un giro de ruleta. Medido el 27-nov-2026:
 * 16 telefonos, 315 pedidos entre mayo y noviembre, con casos de 75 encuestas
 * en una noche y 27 en 23 minutos -una cada 51 segundos-.
 *
 * QUE HACE ESTE PROCESO. Busca los grupos que se pasan del umbral y, por cada
 * uno, RETIENE EL PREMIO -no la encuesta- y avisa por correo.
 *
 * POR QUE RETENER EL PREMIO Y NO LA ENCUESTA. El motivo del fraude es el
 * premio. Rechazar la encuesta castigaria tambien al cliente honesto que cae en
 * la regla por casualidad, y encima nos dejaria sin la informacion.
 * Reteniendo el premio, equivocarse sale barato: alguien mira y lo suelta. La
 * marca es idofertacliente = -2, que la pantalla de dispersion no puede tocar
 * porque su UPDATE exige "WHERE idofertacliente = 0".
 *
 * DOS REGLAS:
 *   TELEFONO  - mismo telefono, misma tienda, mismo dia, N o mas encuestas.
 *               Es la que se valido contra los datos reales.
 *   NAVEGADOR - misma IP y mismo navegador, misma tienda, mismo dia. Es la mas
 *               dificil de esquivar -cambiar de telefono en el formulario es
 *               gratis, cambiar de equipo no- pero solo encuentra algo desde
 *               que encuesta_origen tenga datos, o sea desde que se despliegue
 *               el .war que la llena. Mientras tanto no devuelve nada, y eso no
 *               es una falla.
 *
 * NO AVISA DOS VECES DE LO MISMO: cada grupo se guarda en
 * alerta_encuesta_masiva con llave unica (regla, llave, tienda, dia).
 *
 * Parametros (general.parametros): VIGILANTEENCUESTA_UMBRAL (5),
 * VIGILANTEENCUESTA_DIAS (2) y VIGILANTEENCUESTA_RETIENE ('S'; en 'N' solo
 * avisa). Destinatarios en parametros_correo, ALERTAFRAUDEENCUESTA.
 *
 * Uso:  java ServiciosSer.ProcesoVigilanteEncuestas [dias]
 */
public class ProcesoVigilanteEncuestas {

	private static final String REGLA_TELEFONO = "TELEFONO";
	private static final String REGLA_NAVEGADOR = "NAVEGADOR";

	/** Un grupo sospechoso. */
	private static class Grupo {
		String regla;
		String llave;
		String nombre = "";
		int idTienda;
		String tienda = "";
		String dia;
		int encuestas;
		int minutos;
		int premiosRetenidos;
	}

	public static void main(final String[] args) {
		int dias = 0;
		if (args != null && args.length > 0) {
			try {
				dias = Integer.parseInt(args[0].trim());
			} catch (final Exception ignored) {
			}
		}
		Connection con = null;
		try {
			// La misma conexion que usan los demas procesos para el central.
			con = new conexionCC.ConexionBaseDatos().obtenerConexionBDPrincipal();
			if (dias <= 0) {
				dias = parametroNumerico(con, "VIGILANTEENCUESTA_DIAS", 2);
			}
			final int umbral = parametroNumerico(con, "VIGILANTEENCUESTA_UMBRAL", 5);
			final boolean retiene = !"N".equalsIgnoreCase(parametroTexto(con, "VIGILANTEENCUESTA_RETIENE", "S"));

			System.out.println("Vigilante de encuestas: ultimos " + dias + " dias, umbral " + umbral
					+ ", retiene premios: " + (retiene ? "SI" : "NO"));

			final List<Grupo> grupos = new ArrayList<Grupo>();
			grupos.addAll(buscarPorTelefono(con, dias, umbral));
			grupos.addAll(buscarPorNavegador(con, dias, umbral));

			final List<Grupo> nuevos = new ArrayList<Grupo>();
			for (final Grupo g : grupos) {
				if (yaAvisado(con, g)) {
					continue;
				}
				if (retiene) {
					g.premiosRetenidos = retenerPremios(con, g);
				}
				anotarAviso(con, g);
				nuevos.add(g);
			}

			if (nuevos.isEmpty()) {
				System.out.println("Vigilante de encuestas: nada nuevo que avisar.");
				return;
			}
			int premios = 0;
			for (final Grupo g : nuevos) {
				premios += g.premiosRetenidos;
				System.out.println("  " + g.regla + " " + g.llave + " | tienda " + g.tienda + " | " + g.dia
						+ " | " + g.encuestas + " encuestas en " + g.minutos + " min | premios retenidos: "
						+ g.premiosRetenidos);
			}
			enviarCorreo(nuevos, premios, umbral, dias);
		} catch (final Exception e) {
			System.err.println("Vigilante de encuestas: fallo - " + e);
			e.printStackTrace();
		} finally {
			cerrar(con);
		}
	}

	/**
	 * Mismo telefono, misma tienda, mismo dia. El telefono lo escribe quien
	 * responde, asi que es facil de esquivar cambiandolo; aun asi es la regla que
	 * encontro los 16 casos reales, porque quien llena 75 encuestas no se molesta
	 * en cambiar el nombre.
	 */
	private static List<Grupo> buscarPorTelefono(final Connection con, final int dias, final int umbral)
			throws Exception {
		final String sql = "select c.telefono, min(c.nombre_cliente), e.idtienda,"
				+ " ifnull(t.nombre,concat('tienda ',e.idtienda)), date(e.fecha_creacion),"
				+ " count(distinct e.idpedido),"
				+ " timestampdiff(minute, min(e.fecha_creacion), max(e.fecha_creacion))"
				+ " from pizzaamericana.encuesta_servicio e"
				+ " join pizzaamericana.cliente_servicio c"
				+ "   on c.idpedido = e.idpedido and c.idtienda = e.idtienda"
				+ " left join pizzaamericana.tienda t on t.idtienda = e.idtienda"
				+ " where e.fecha_creacion >= date_sub(curdate(), interval ? day)"
				+ "   and c.telefono <> 'CLI-TIENDA' and c.telefono is not null"
				+ " group by c.telefono, e.idtienda, date(e.fecha_creacion), t.nombre"
				+ " having count(distinct e.idpedido) >= ?";
		return leerGrupos(con, sql, dias, umbral, REGLA_TELEFONO);
	}

	/**
	 * Misma IP y mismo navegador, misma tienda, mismo dia. Cambiar el telefono en
	 * el formulario es gratis; cambiar de equipo no. Solo encuentra algo cuando
	 * encuesta_origen tiene datos.
	 */
	private static List<Grupo> buscarPorNavegador(final Connection con, final int dias, final int umbral)
			throws Exception {
		final String sql = "select concat(ifnull(o.ip,'?'),' / ',left(ifnull(o.navegador,'?'),60)), '',"
				+ " o.idtienda, ifnull(t.nombre,concat('tienda ',o.idtienda)), date(o.fecha),"
				+ " count(distinct o.idpedido),"
				+ " timestampdiff(minute, min(o.fecha), max(o.fecha))"
				+ " from pizzaamericana.encuesta_origen o"
				+ " left join pizzaamericana.tienda t on t.idtienda = o.idtienda"
				+ " where o.fecha >= date_sub(curdate(), interval ? day)"
				+ "   and o.accion = 'ENCUESTA' and o.ip is not null"
				+ " group by o.ip, o.navegador, o.idtienda, date(o.fecha), t.nombre"
				+ " having count(distinct o.idpedido) >= ?";
		return leerGrupos(con, sql, dias, umbral, REGLA_NAVEGADOR);
	}

	private static List<Grupo> leerGrupos(final Connection con, final String sql, final int dias,
			final int umbral, final String regla) throws Exception {
		final List<Grupo> lista = new ArrayList<Grupo>();
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, dias);
			ps.setInt(2, umbral);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					final Grupo g = new Grupo();
					g.regla = regla;
					g.llave = rs.getString(1);
					g.nombre = rs.getString(2) == null ? "" : rs.getString(2);
					g.idTienda = rs.getInt(3);
					g.tienda = rs.getString(4);
					g.dia = rs.getString(5);
					g.encuestas = rs.getInt(6);
					g.minutos = rs.getInt(7);
					lista.add(g);
				}
			}
		}
		return lista;
	}

	private static boolean yaAvisado(final Connection con, final Grupo g) throws Exception {
		final String sql = "select 1 from pizzaamericana.alerta_encuesta_masiva"
				+ " where regla=? and llave=? and idtienda=? and dia=?";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, g.regla);
			ps.setString(2, recortar(g.llave, 120));
			ps.setInt(3, g.idTienda);
			ps.setString(4, g.dia);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	private static void anotarAviso(final Connection con, final Grupo g) throws Exception {
		final String sql = "insert ignore into pizzaamericana.alerta_encuesta_masiva"
				+ " (regla, llave, idtienda, dia, encuestas, minutos, premios_retenidos)"
				+ " values (?,?,?,?,?,?,?)";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, g.regla);
			ps.setString(2, recortar(g.llave, 120));
			ps.setInt(3, g.idTienda);
			ps.setString(4, g.dia);
			ps.setInt(5, g.encuestas);
			ps.setInt(6, g.minutos);
			ps.setInt(7, g.premiosRetenidos);
			ps.executeUpdate();
		}
	}

	/**
	 * Marca con -2 los premios de ruleta pendientes de los pedidos de ese grupo.
	 * Solo toca los que estan en 0 -o sea pendientes- y solo los que de verdad son
	 * premio, nunca un giro perdedor: de los 315 pedidos del caso real, 294 no
	 * habian ganado nada y marcarlos no tendria sentido.
	 */
	private static int retenerPremios(final Connection con, final Grupo g) throws Exception {
		final String sub = REGLA_TELEFONO.equals(g.regla)
				? " and exists (select 1 from pizzaamericana.encuesta_servicio e"
						+ "   join pizzaamericana.cliente_servicio c"
						+ "     on c.idpedido = e.idpedido and c.idtienda = e.idtienda"
						+ "  where e.idtienda = r.idtienda and e.idpedido = r.idpedido"
						+ "    and c.telefono = ? and date(e.fecha_creacion) = ?)"
				: " and exists (select 1 from pizzaamericana.encuesta_origen o"
						+ "  where o.idtienda = r.idtienda and o.idpedido = r.idpedido"
						+ "    and concat(ifnull(o.ip,'?'),' / ',left(ifnull(o.navegador,'?'),60)) = ?"
						+ "    and date(o.fecha) = ?)";
		final String sql = "update pizzaamericana.resultado_ruleta r"
				+ " join pizzaamericana.ruleta_oferta ro on ro.idopcion = r.idopcion and ro.activo = 'S'"
				+ "  set r.idofertacliente = -2"
				+ " where r.idtienda = ? and r.idofertacliente = 0" + sub;
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, g.idTienda);
			ps.setString(2, g.llave);
			ps.setString(3, g.dia);
			return ps.executeUpdate();
		}
	}

	private static void enviarCorreo(final List<Grupo> grupos, final int premios, final int umbral,
			final int dias) {
		try {
			final StringBuilder h = new StringBuilder();
			h.append("<div style=\"font-family:Arial,Helvetica,sans-serif;color:#333\">");
			h.append("<h2 style=\"color:#E42528;margin-bottom:4px\">Posible fraude en encuestas</h2>");
			h.append("<p style=\"margin-top:0\">Encuestas llenadas en masa en los ultimos ").append(dias)
					.append(" dias. La regla es: <b>").append(umbral)
					.append(" o mas encuestas de la misma fuente, en una tienda, en un dia</b>.</p>");
			h.append("<p><b>Premios de ruleta retenidos: ").append(premios)
					.append("</b>. Se retiene el premio, no la encuesta: ")
					.append("la encuesta se conserva y el premio no se entrega hasta que alguien lo revise ")
					.append("en Administrador &gt; Dispersion Premios Ruleta, donde aparece como ")
					.append("<b>BLOQUEADO POR REVISAR</b>.</p>");
			h.append("<table cellpadding=\"6\" cellspacing=\"0\" border=\"1\" ")
					.append("style=\"border-collapse:collapse;font-size:13px\">");
			h.append("<tr style=\"background:#FDC806\"><th>Regla</th><th>Fuente</th><th>Nombre</th>")
					.append("<th>Punto de venta</th><th>Dia</th><th>Encuestas</th><th>En minutos</th>")
					.append("<th>Premios retenidos</th></tr>");
			for (final Grupo g : grupos) {
				h.append("<tr><td>").append(g.regla).append("</td><td>").append(escapar(g.llave))
						.append("</td><td>").append(escapar(g.nombre)).append("</td><td>")
						.append(escapar(g.tienda)).append("</td><td>").append(g.dia).append("</td>")
						.append("<td align=\"center\"><b>").append(g.encuestas).append("</b></td>")
						.append("<td align=\"center\">").append(g.minutos).append("</td>")
						.append("<td align=\"center\">").append(g.premiosRetenidos).append("</td></tr>");
			}
			h.append("</table>");
			h.append("<p style=\"font-size:12px;color:#666\">Esto es un aviso, no una condena: ")
					.append("puede haber casos legitimos -una familia, un pedido corregido varias veces-. ")
					.append("Por eso se retiene el premio y no la encuesta, y soltarlo es volver a poner el ")
					.append("registro en pendiente.</p>");
			h.append("</div>");

			final Correo correo = new Correo();
			final CorreoElectronico info = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES",
					"CLAVECORREOREPORTE");
			correo.setAsunto("POSIBLE FRAUDE EN ENCUESTAS - " + grupos.size() + " caso(s), " + premios
					+ " premio(s) retenido(s)");
			correo.setContrasena(info.getClaveCorreo());
			correo.setUsuarioCorreo(info.getCuentaCorreo());
			correo.setMensaje(h.toString());
			final ArrayList correos = GeneralDAO.obtenerCorreosParametro("ALERTAFRAUDEENCUESTA");
			if (correos == null || correos.isEmpty()) {
				System.out.println("Vigilante de encuestas: no hay destinatarios en ALERTAFRAUDEENCUESTA.");
				return;
			}
			new ControladorEnvioCorreo(correo, correos).enviarCorreoHTML();
			System.out.println("Vigilante de encuestas: aviso enviado a " + correos.size() + " destinatario(s).");
		} catch (final Exception e) {
			// El correo es lo ultimo: si falla, los premios ya quedaron retenidos y
			// el aviso ya quedo anotado. Se reporta y no se deshace nada.
			System.err.println("Vigilante de encuestas: no se pudo enviar el correo - " + e);
		}
	}

	private static int parametroNumerico(final Connection con, final String nombre, final int porDefecto) {
		try (Statement st = con.createStatement();
				ResultSet rs = st.executeQuery("select valornumerico from general.parametros"
						+ " where valorparametro = '" + nombre + "'")) {
			if (rs.next()) {
				final int v = rs.getInt(1);
				if (v > 0) {
					return v;
				}
			}
		} catch (final Exception ignored) {
		}
		return porDefecto;
	}

	private static String parametroTexto(final Connection con, final String nombre, final String porDefecto) {
		try (Statement st = con.createStatement();
				ResultSet rs = st.executeQuery("select valortexto from general.parametros"
						+ " where valorparametro = '" + nombre + "'")) {
			if (rs.next()) {
				final String v = rs.getString(1);
				if (v != null && !v.trim().isEmpty()) {
					return v.trim();
				}
			}
		} catch (final Exception ignored) {
		}
		return porDefecto;
	}

	private static String recortar(final String v, final int largo) {
		if (v == null) {
			return "";
		}
		return v.length() <= largo ? v : v.substring(0, largo);
	}

	private static String escapar(final String v) {
		if (v == null) {
			return "";
		}
		return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static void cerrar(final Connection con) {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (final Exception ignored) {
		}
	}
}
