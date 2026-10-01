package ServiciosSer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.OfertaClienteDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.OfertaCliente;
import ModeloSer.ResumenOferta;
import utilidadesSer.ControladorEnvioCorreo;

/**
 * Reporte semanal de ofertas: cuantas se dieron, de que tipo, si vinieron de
 * una campana del CRM, de la ruleta de premios o del bono de recompra, y
 * cuantas de esas ya se redimieron.
 *
 * ANTES ESTO ERA UN VOLCADO
 *
 * La version vieja mandaba una fila por cada oferta_cliente, sin agrupar y
 * sin distinguir de donde salio cada una. Servia para auditar una oferta
 * puntual, pero no para contestar "como nos fue esta semana" de un vistazo:
 * para eso hay que sumar. Esta version arranca con los totales y el resumen
 * por origen y por oferta; el detalle fila por fila queda al final, para
 * quien de verdad lo necesite.
 */
public class ReporteOfertasSemanales {

	public static void main(String[] args) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		final String fechaActual = dateFormat.format(new Date());
		final Calendar calendarioActual = Calendar.getInstance();
		try {
			calendarioActual.setTime(dateFormat.parse(fechaActual));
		} catch (final Exception e) {
			System.out.println(e.toString());
		}
		//Mismo calculo de siempre: el lunes de la semana (o el lunes anterior,
		//si hoy es lunes -se prefiere mostrar la semana ya completa).
		final int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
		if (diaActual == 1) {
			calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
		} else if (diaActual == 2) {
			calendarioActual.add(Calendar.DAY_OF_YEAR, -7);
		} else {
			calendarioActual.add(Calendar.DAY_OF_YEAR, -(diaActual - 2));
		}
		final String fechaAnterior = dateFormat.format(calendarioActual.getTime());
		System.out.println("Semana del " + fechaAnterior + " al " + fechaActual);

		//Se trae el resumen de enviadas y el de redimidas por separado -son dos
		//preguntas distintas, "que salio" y "que se uso"- y se funden en un
		//mismo mapa por oferta+origen para no repetir la fila en el correo.
		final ArrayList<ResumenOferta> enviadas =
				OfertaClienteDAO.obtenerResumenEnviadasSemana(fechaAnterior, fechaActual);
		final ArrayList<ResumenOferta> redimidas =
				OfertaClienteDAO.obtenerResumenRedimidasSemana(fechaAnterior, fechaActual);

		final LinkedHashMap<String, ResumenOferta> porOferta = new LinkedHashMap<String, ResumenOferta>();
		for (int i = 0; i < enviadas.size(); i++) {
			final ResumenOferta r = enviadas.get(i);
			porOferta.put(r.getClave(), r);
		}
		for (int i = 0; i < redimidas.size(); i++) {
			final ResumenOferta r = redimidas.get(i);
			ResumenOferta acumulado = porOferta.get(r.getClave());
			if (acumulado == null) {
				acumulado = new ResumenOferta();
				acumulado.setNombreOferta(r.getNombreOferta());
				acumulado.setOrigen(r.getOrigen());
				porOferta.put(r.getClave(), acumulado);
			}
			acumulado.setRedimidas(r.getRedimidas());
			acumulado.setValorRedimido(r.getValorRedimido());
		}

		//El mismo resumen, ahora sumado solo por origen -la pregunta de "cuanto
		//nos esta costando cada canal", sin mirar oferta por oferta.
		final LinkedHashMap<String, ResumenOferta> porOrigen = new LinkedHashMap<String, ResumenOferta>();
		int totalEnviadas = 0;
		double totalValorEnviado = 0;
		int totalRedimidas = 0;
		double totalValorRedimido = 0;
		for (final ResumenOferta r : porOferta.values()) {
			totalEnviadas += r.getEnviadas();
			totalValorEnviado += r.getValorEnviado();
			totalRedimidas += r.getRedimidas();
			totalValorRedimido += r.getValorRedimido();

			ResumenOferta grupo = porOrigen.get(r.getOrigen());
			if (grupo == null) {
				grupo = new ResumenOferta();
				grupo.setNombreOferta(r.getOrigen());
				grupo.setOrigen(r.getOrigen());
				porOrigen.put(r.getOrigen(), grupo);
			}
			grupo.setEnviadas(grupo.getEnviadas() + r.getEnviadas());
			grupo.setValorEnviado(grupo.getValorEnviado() + r.getValorEnviado());
			grupo.setRedimidas(grupo.getRedimidas() + r.getRedimidas());
			grupo.setValorRedimido(grupo.getValorRedimido() + r.getValorRedimido());
		}

		final double porcentajeRedencion = totalEnviadas > 0
				? Math.round(totalRedimidas * 1000.0 / totalEnviadas) / 10.0 : 0;

		//El detalle, fila por fila, para quien necesite ver exactamente a
		//quien se le dio cada codigo -ya no es el reporte completo, es el
		//apendice.
		final ArrayList<OfertaCliente> detalle =
				OfertaClienteDAO.obtenerOfertasNuevasSemana(fechaActual, fechaAnterior);

		final StringBuilder html = new StringBuilder();
		html.append("<p>Semana del <b>").append(fechaAnterior).append("</b> al <b>")
			.append(fechaActual).append("</b>.</p>");

		//LOS TOTALES PRIMERO
		html.append("<table border='1' cellpadding='6' cellspacing='0'>")
			.append("<tr><td>Ofertas enviadas</td><td><b>").append(totalEnviadas).append("</b></td></tr>")
			.append("<tr><td>Ofertas redimidas</td><td><b>").append(totalRedimidas).append("</b></td></tr>")
			.append("<tr><td>% de redencion</td><td><b>").append(porcentajeRedencion).append("%</b></td></tr>")
			.append("<tr><td>Costo de la semana (redimido)</td><td><b>$")
			.append(pesos(totalValorRedimido)).append("</b></td></tr>")
			.append("<tr><td>Valor comprometido (enviado, no todo se redime)</td><td>$")
			.append(pesos(totalValorEnviado)).append("</td></tr>")
			.append("</table><br/>");

		//RESUMEN POR ORIGEN: campana CRM, ruleta, bono de recompra, manual.
		html.append("<h3>Por origen</h3>")
			.append("<table border='1' cellpadding='5' cellspacing='0'>")
			.append("<tr><td><b>Origen</b></td><td><b>Enviadas</b></td><td><b>Redimidas</b></td>")
			.append("<td><b>% redencion</b></td><td><b>Valor redimido</b></td></tr>");
		for (final ResumenOferta g : porOrigen.values()) {
			final double pct = g.getEnviadas() > 0 ? Math.round(g.getRedimidas() * 1000.0 / g.getEnviadas()) / 10.0 : 0;
			html.append("<tr><td>").append(g.getOrigen()).append("</td><td>").append(g.getEnviadas())
				.append("</td><td>").append(g.getRedimidas()).append("</td><td>").append(pct)
				.append("%</td><td>$").append(pesos(g.getValorRedimido())).append("</td></tr>");
		}
		html.append("</table><br/>");

		//RESUMEN POR OFERTA, DENTRO DE CADA ORIGEN.
		html.append("<h3>Por oferta</h3>")
			.append("<table border='1' cellpadding='5' cellspacing='0'>")
			.append("<tr><td><b>Oferta</b></td><td><b>Origen</b></td><td><b>Enviadas</b></td>")
			.append("<td><b>Redimidas</b></td><td><b>% redencion</b></td>")
			.append("<td><b>Valor enviado</b></td><td><b>Valor redimido</b></td></tr>");
		for (final ResumenOferta r : porOferta.values()) {
			final double pct = r.getEnviadas() > 0 ? Math.round(r.getRedimidas() * 1000.0 / r.getEnviadas()) / 10.0 : 0;
			html.append("<tr><td>").append(r.getNombreOferta()).append("</td><td>").append(r.getOrigen())
				.append("</td><td>").append(r.getEnviadas()).append("</td><td>").append(r.getRedimidas())
				.append("</td><td>").append(pct).append("%</td><td>$").append(pesos(r.getValorEnviado()))
				.append("</td><td>$").append(pesos(r.getValorRedimido())).append("</td></tr>");
		}
		html.append("</table><br/>");

		//EL DETALLE, AL FINAL: cada oferta enviada esta semana, una por una.
		html.append("<h3>Detalle de lo enviado esta semana (").append(detalle.size()).append(")</h3>")
			.append("<table border='1' cellpadding='4' cellspacing='0'>")
			.append("<tr><td><b>Oferta</b></td><td><b>Origen</b></td><td><b>Valor</b></td>")
			.append("<td><b>Enviada</b></td><td><b>Redimida</b></td><td><b>Usada</b></td></tr>");
		for (int i = 0; i < detalle.size(); i++) {
			final OfertaCliente d = detalle.get(i);
			html.append("<tr><td>").append(texto(d.getNombreOferta())).append("</td><td>")
				.append(texto(d.getOrigen())).append("</td><td>$").append(pesos(d.getValor()))
				.append("</td><td>").append(texto(d.getIngresoOferta())).append("</td><td>")
				.append(texto(d.getUsoOferta())).append("</td><td>")
				.append("S".equals(d.getUtilizada()) ? "Si" : "No").append("</td></tr>");
		}
		html.append("</table>");

		final ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPUSOOFERTAS");
		final Correo correo = new Correo();
		final CorreoElectronico infoCorreo =
				ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("Reporte semanal de ofertas: " + fechaAnterior + " a " + fechaActual
				+ " -- " + totalEnviadas + " enviadas, " + totalRedimidas + " redimidas ("
				+ porcentajeRedencion + "%), $" + pesos(totalValorRedimido) + " de costo");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje(html.toString());
		final ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}

	private static String texto(final String v) {
		return (v == null ? "" : v);
	}

	/** $1.234.567, sin decimales -un bono no se da en centavos. */
	private static String pesos(final double valor) {
		return (String.format("%,.0f", valor).replace(",", "."));
	}
}
