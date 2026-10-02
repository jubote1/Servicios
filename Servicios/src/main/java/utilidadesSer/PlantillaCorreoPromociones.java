package utilidadesSer;

import java.text.DecimalFormat;
import java.util.ArrayList;

import CapaDAOSer.PromocionReporteDAO;

/**
 * El correo diario de promociones.
 *
 * QUE CAMBIA RESPECTO AL VIEJO
 *
 * El viejo manda trece tablas, una por promocion, cada una con las once
 * tiendas y una columna de cantidad. Son mas de cien numeros sueltos sin nada
 * contra que compararlos, y la mayoria en cero. Ademas cada tabla aparece solo
 * si hubo venta, asi que el correo cambia de forma todos los dias y no se puede
 * leer de un vistazo.
 *
 * Este manda UNA tabla con una fila por promocion, y al lado de lo de hoy pone
 * el promedio del mismo dia de las semanas anteriores. Un numero sin referencia
 * no dice nada: "3" no es bueno ni malo hasta que uno sabe que lo normal son 8.
 *
 * Y va la plata, no solo la cantidad: tres pizzas de sesenta mil no son lo
 * mismo que tres de veinte mil.
 *
 * LO QUE NO SE INVENTA
 *
 * Mientras no haya historia suficiente, la columna de referencia dice "sin
 * referencia" y no un cero ni un guion que se pueda confundir con "vendio
 * cero". La tabla datamart.promocion_dia arranca el dia que se despliega esto,
 * asi que las primeras cuatro semanas la comparacion va llenandose sola.
 */
public final class PlantillaCorreoPromociones {

	private static final String AZUL = "#102F6F";
	private static final String ROJO = "#C21C1F";
	private static final String VERDE = "#16704F";
	private static final String GRIS = "#6C7482";

	/** Cuanto se tiene que mover algo para que valga la pena pintarlo. */
	private static final double UMBRAL_VARIACION = 15;

	private PlantillaCorreoPromociones() {
	}

	public static String asunto(final String fecha, final double unidades) {
		return ("Promociones del " + fecha + " - " + entero(unidades) + " vendidas");
	}

	/**
	 * El cuerpo.
	 *
	 * @param fecha         el dia reportado, aaaa-mm-dd
	 * @param nombreDia     lunes, martes...
	 * @param resumenes     una fila por promocion, ya ordenadas
	 * @param porTienda     el detalle por tienda de las que se movieron
	 * @param sinResponder  tiendas que no contestaron
	 */
	public static String cuerpo(final String fecha, final String nombreDia,
			final ArrayList<PromocionReporteDAO.Resumen> resumenes,
			final String porTienda, final ArrayList<String> sinResponder) {

		double totalUnidades = 0;
		double totalValor = 0;
		for (int i = 0; i < resumenes.size(); i++) {
			totalUnidades += resumenes.get(i).unidades;
			totalValor += resumenes.get(i).valor;
		}

		final StringBuilder h = new StringBuilder();
		h.append("<div style=\"font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#222;\">");

		h.append("<p style=\"font-size:17px;font-weight:bold;color:").append(AZUL)
		 .append(";margin:0 0 2px;\">Promociones del ").append(nombreDia).append(" ").append(fecha)
		 .append("</p>");
		h.append("<p style=\"color:").append(GRIS).append(";margin:0 0 16px;font-size:13px;\">")
		 .append(entero(totalUnidades)).append(" promociones vendidas por ")
		 .append(pesos(totalValor))
		 .append(". La referencia es el promedio del mismo d&iacute;a de las semanas anteriores.</p>");

		//Lo que no se pudo preguntar va ARRIBA, no al pie: si falta una tienda,
		//todo lo de abajo esta incompleto y hay que saberlo antes de leerlo.
		if (sinResponder != null && !sinResponder.isEmpty()) {
			h.append("<div style=\"background:#FADBDB;border-left:4px solid ").append(ROJO)
			 .append(";padding:10px 14px;margin-bottom:14px;\">");
			h.append("<strong>Faltan tiendas.</strong> No respondieron ").append(unirNombres(sinResponder));
			h.append(", as&iacute; que lo de abajo est&aacute; incompleto y no se guard&oacute; su d&iacute;a. ");
			h.append("Se puede reprocesar cuando vuelvan.</div>");
		}

		h.append(tabla(resumenes));

		if (porTienda != null && porTienda.length() > 0) {
			h.append(porTienda);
		}

		h.append("<p style=\"font-size:11.5px;color:").append(GRIS).append(";margin-top:18px;\">");
		h.append("Se cuentan unidades vendidas en las tiendas, por todos los canales -mostrador, ");
		h.append("contact center, CRM, app, tienda virtual, DiDi y Rappi-, descontando lo anulado. ");
		h.append("Las promociones se administran desde el cat&aacute;logo: agregar una no necesita ");
		h.append("tocar el programa.</p>");

		h.append("</div>");
		return (h.toString());
	}

	private static String tabla(final ArrayList<PromocionReporteDAO.Resumen> resumenes) {
		final StringBuilder h = new StringBuilder();
		h.append("<table style=\"border-collapse:collapse;font-size:13px;min-width:620px;\">");
		h.append("<tr>");
		h.append(th("Promoci&oacute;n", "left"));
		h.append(th("Hoy", "right"));
		h.append(th("Plata", "right"));
		h.append(th("Normal", "right"));
		h.append(th("", "left"));
		h.append("</tr>");

		boolean hayPlataforma = false;
		for (int i = 0; i < resumenes.size(); i++) {
			if (resumenes.get(i).plataforma) {
				hayPlataforma = true;
				break;
			}
		}

		boolean separadorPuesto = false;
		for (int i = 0; i < resumenes.size(); i++) {
			final PromocionReporteDAO.Resumen r = resumenes.get(i);

			//Las de plataforma van juntas y abajo, con un separador: la tienda no
			//decide esas ventas, y mezclarlas con las propias hace creer que el
			//punto de venta las puede mover.
			if (hayPlataforma && r.plataforma && !separadorPuesto) {
				separadorPuesto = true;
				h.append("<tr><td colspan=\"5\" style=\"padding:9px 8px 4px;font-size:11.5px;")
				 .append("text-transform:uppercase;letter-spacing:.04em;color:").append(GRIS)
				 .append(";border-bottom:1px solid #E2E6EC;\">Plataformas</td></tr>");
			}

			h.append("<tr>");
			h.append(td(escapar(r.nombre), "left", false));
			h.append(td(entero(r.unidades), "right", true));
			h.append(td(pesos(r.valor), "right", false));
			h.append(td(referencia(r), "right", false));
			h.append(td(variacion(r), "left", false));
			h.append("</tr>");
		}
		h.append("</table>");
		return (h.toString());
	}

	/** El promedio, o la verdad de que todavia no hay con que comparar. */
	private static String referencia(final PromocionReporteDAO.Resumen r) {
		if (r.promedioUnidades < 0) {
			return ("<span style=\"color:" + GRIS + ";font-style:italic;\">sin referencia</span>");
		}
		final String texto = entero(r.promedioUnidades);
		if (r.diasDeReferencia < 4) {
			//Se dice sobre cuantas semanas va el promedio. Un promedio de una
			//sola semana no merece la misma confianza que uno de cuatro.
			return (texto + "<span style=\"color:" + GRIS + ";font-size:11px;\"> ("
					+ r.diasDeReferencia + (r.diasDeReferencia == 1 ? " sem" : " sems") + ")</span>");
		}
		return (texto);
	}

	/**
	 * La flecha.
	 *
	 * Solo se pinta cuando el movimiento pasa del umbral. Si todo lleva flecha,
	 * ninguna flecha significa nada.
	 */
	private static String variacion(final PromocionReporteDAO.Resumen r) {
		if (r.promedioUnidades < 0) {
			return ("");
		}
		if (r.promedioUnidades == 0) {
			return (r.unidades > 0
					? "<span style=\"color:" + VERDE + ";font-weight:bold;\">arranc&oacute;</span>"
					: "");
		}
		final double porcentaje = ((r.unidades - r.promedioUnidades) / r.promedioUnidades) * 100;
		if (Math.abs(porcentaje) < UMBRAL_VARIACION) {
			return ("<span style=\"color:" + GRIS + ";\">igual que siempre</span>");
		}
		final String color = (porcentaje > 0) ? VERDE : ROJO;
		final String flecha = (porcentaje > 0) ? "&#9650;" : "&#9660;";
		return ("<span style=\"color:" + color + ";font-weight:bold;\">" + flecha + " "
				+ entero(Math.abs(porcentaje)) + "%</span>");
	}

	private static String th(final String texto, final String alineacion) {
		return ("<th style=\"background:" + AZUL + ";color:#fff;text-align:" + alineacion
				+ ";padding:6px 10px;font-size:11.5px;text-transform:uppercase;letter-spacing:.04em;"
				+ "white-space:nowrap;\">" + texto + "</th>");
	}

	private static String td(final String texto, final String alineacion, final boolean fuerte) {
		return ("<td style=\"border-bottom:1px solid #E2E6EC;text-align:" + alineacion
				+ ";padding:6px 10px;white-space:nowrap;"
				+ (fuerte ? "font-weight:bold;font-size:15px;" : "") + "\">" + texto + "</td>");
	}

	/** Medias unidades existen: una pizza mitad y mitad cuenta 0,5. */
	private static String entero(final double valor) {
		if (valor == Math.floor(valor) && !Double.isInfinite(valor)) {
			return (new DecimalFormat("###,###").format(valor));
		}
		return (new DecimalFormat("###,###.#").format(valor));
	}

	private static String pesos(final double valor) {
		return ("$ " + new DecimalFormat("###,###").format(Math.round(valor)));
	}

	private static String unirNombres(final ArrayList<String> nombres) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nombres.size(); i++) {
			if (i > 0) {
				sb.append(i == nombres.size() - 1 ? " y " : ", ");
			}
			sb.append(escapar(nombres.get(i)));
		}
		return (sb.toString());
	}

	private static String escapar(final String texto) {
		if (texto == null) {
			return ("");
		}
		return (texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
	}
}
