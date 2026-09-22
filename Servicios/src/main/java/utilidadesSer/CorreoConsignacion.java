package utilidadesSer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Lo propio de los correos de consignacion (Wompi y Bold): la tarjeta con el
 * total a consignar, el porcentaje de comision y el comparativo contra los
 * cobros de Bold.
 *
 * Todo lo demas -encabezado, tablas, avisos, pie- se fue a CorreoHtml, que es
 * el estilo de la casa y ahora lo usa tambien el reporte de horarios. Los
 * metodos que quedan aca delegando NO se borraron a proposito: los llaman
 * ReporteConsignacionBold, su reproceso y ReporteConsignacionWompi, y
 * cambiarles el nombre a los tres para ganar nada seria buscarse un problema.
 *
 * El HTML que sale es identico al de antes. Esto es una mudanza, no un rediseno.
 */
public class CorreoConsignacion {

	private static final String GRIS = CorreoHtml.GRIS;
	private static final String AZUL = CorreoHtml.AZUL;
	private static final String AMARILLO = CorreoHtml.AMARILLO;

	public static String pesos(final double valor) {
		return CorreoHtml.pesos(valor);
	}

	/** Un porcentaje con coma decimal: 1.5 -> "1,5%", 2 -> "2%". */
	public static String porcentaje(final double valor) {
		final DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.US);
		simbolos.setDecimalSeparator(',');
		return new DecimalFormat("0.##", simbolos).format(valor) + "%";
	}

	public static String entero(final long valor) {
		return CorreoHtml.entero(valor);
	}

	public static String h(final String texto) {
		return CorreoHtml.h(texto);
	}

	public static String abrir(final String titulo, final String subtitulo) {
		return CorreoHtml.abrir(titulo, subtitulo);
	}

	public static String cerrar(final String pie) {
		return CorreoHtml.cerrar(pie);
	}

	public static String abrirTabla(final String titulo, final String... encabezados) {
		return CorreoHtml.abrirTabla(titulo, encabezados);
	}

	public static String fila(final String... celdas) {
		return CorreoHtml.fila(celdas);
	}

	public static String filaTotal(final String... celdas) {
		return CorreoHtml.filaTotal(celdas);
	}

	public static String cerrarTabla() {
		return CorreoHtml.cerrarTabla();
	}

	public static String aviso(final String texto) {
		return CorreoHtml.aviso(texto);
	}

	/** La cifra grande del correo: lo que se va a consignar. */
	public static String tarjetaTotal(final String etiqueta, final double valor, final String detalle) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
		sb.append("<td style=\"background:#fff8e1;border-left:6px solid ").append(AMARILLO)
				.append(";padding:16px 18px;\">");
		sb.append("<div style=\"font-size:12px;color:").append(GRIS)
				.append(";font-weight:bold;letter-spacing:.4px;\">").append(h(etiqueta.toUpperCase())).append("</div>");
		sb.append("<div style=\"font-size:32px;font-weight:bold;color:").append(AZUL).append(";margin-top:2px;\">")
				.append(pesos(valor)).append("</div>");
		if (detalle != null && detalle.length() > 0) {
			sb.append("<div style=\"font-size:12px;color:").append(GRIS).append(";margin-top:4px;\">")
					.append(h(detalle)).append("</div>");
		}
		sb.append("</td></tr></table><div style=\"height:20px;line-height:20px;\">&nbsp;</div>");
		return sb.toString();
	}

	/**
	 * Una fila del comparativo, con la ULTIMA celda pintada segun la diferencia.
	 *
	 * Verde cuando Bold cobro MAS de lo que quedo registrado en los pedidos, y
	 * rojo cuando fue al reves. El criterio es el de caja, no el contable: en
	 * rojo la tienda registro plata que Bold no consigno -falta plata-, y en
	 * verde entro plata que nadie registro, que hay que averiguar pero no es
	 * una perdida.
	 *
	 * Los colores van en el fondo de la celda y NO como color de letra sola:
	 * varios clientes de correo, y las impresiones en blanco y negro, se comen
	 * el color del texto. Por eso ademas del color va el signo, para que la
	 * fila se entienda aunque el color no se vea.
	 *
	 * @param diferencia null cuando no se pudo consultar la tienda
	 */
	public static String filaComparativo(final String tienda, final String pagosPos,
			final String valorPos, final String cobrosBold, final String valorBold,
			final Double diferencia) {
		final String fondo;
		final String letra;
		final String texto;
		if (diferencia == null) {
			fondo = "#f4f5f7";
			letra = GRIS;
			texto = "sin datos";
		} else if (diferencia.doubleValue() > 0) {
			fondo = "#e4f1eb";
			letra = "#16704f";
			texto = "+" + pesos(diferencia.doubleValue());
		} else if (diferencia.doubleValue() < 0) {
			fondo = "#fadbdb";
			letra = "#c21c1f";
			texto = "-" + pesos(Math.abs(diferencia.doubleValue()));
		} else {
			fondo = "#ffffff";
			letra = GRIS;
			texto = "cuadra";
		}
		final StringBuilder sb = new StringBuilder("<tr>");
		final String[] primeras = { tienda, pagosPos, valorPos, cobrosBold, valorBold };
		for (int i = 0; i < primeras.length; i++) {
			sb.append("<td style=\"padding:8px 12px;border-bottom:1px solid #e7e9ee;text-align:")
					.append(i == 0 ? "left" : "right").append(";\">").append(h(primeras[i])).append("</td>");
		}
		sb.append("<td style=\"padding:8px 12px;border-bottom:1px solid #e7e9ee;text-align:right;")
				.append("background:").append(fondo).append(";color:").append(letra)
				.append(";font-weight:bold;\">").append(h(texto)).append("</td>");
		return sb.append("</tr>").toString();
	}

	/** La leyenda del comparativo. Sin ella el color no dice que significa. */
	public static String leyendaComparativo() {
		return "<div style=\"font-size:12px;color:" + GRIS + ";margin:-12px 0 20px;\">"
				+ "<span style=\"background:#e4f1eb;color:#16704f;font-weight:bold;padding:2px 8px;\">verde</span>"
				+ " Bold cobr&#243; m&#225;s de lo que se registr&#243; en los pedidos &#183; "
				+ "<span style=\"background:#fadbdb;color:#c21c1f;font-weight:bold;padding:2px 8px;\">rojo</span>"
				+ " se registr&#243; m&#225;s de lo que Bold cobr&#243;, revise esa tienda."
				+ "</div>";
	}
}
