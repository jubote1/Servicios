package utilidadesSer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Piezas HTML comunes de los correos de consignacion (Wompi y Bold), para que
 * se vean igual: encabezado de marca, tarjeta con el total a consignar, tablas
 * con la fila de total resaltada y aviso.
 *
 * Todo con estilos en linea porque los clientes de correo (Gmail incluido)
 * ignoran las hojas de estilo. Los textos dinamicos pasan por h(), que escapa
 * HTML y convierte lo que no es ASCII a entidades numericas: el correo se ve
 * bien sin depender de en que codificacion se compilo o se envio el mensaje.
 */
public class CorreoConsignacion {

	private static final String AZUL = "#102F6F";
	private static final String AMARILLO = "#FDC806";
	private static final String ROJO = "#E42528";
	private static final String GRIS = "#5b6270";

	/** Pesos colombianos con punto de miles, sin depender del idioma del servidor. */
	public static String pesos(final double valor) {
		final DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.US);
		simbolos.setGroupingSeparator('.');
		return "$" + new DecimalFormat("#,##0", simbolos).format(valor);
	}

	/** Un porcentaje con coma decimal: 1.5 -> "1,5%", 2 -> "2%". */
	public static String porcentaje(final double valor) {
		final DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.US);
		simbolos.setDecimalSeparator(',');
		return new DecimalFormat("0.##", simbolos).format(valor) + "%";
	}

	/** Un entero con punto de miles. */
	public static String entero(final long valor) {
		final DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.US);
		simbolos.setGroupingSeparator('.');
		return new DecimalFormat("#,##0", simbolos).format(valor);
	}

	/** Escapa HTML y pasa lo que no es ASCII a entidades numericas. */
	public static String h(final String texto) {
		if (texto == null) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < texto.length(); i++) {
			final char c = texto.charAt(i);
			if (c == '&') {
				sb.append("&amp;");
			} else if (c == '<') {
				sb.append("&lt;");
			} else if (c == '>') {
				sb.append("&gt;");
			} else if (c > 126) {
				sb.append("&#").append((int) c).append(';');
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String abrir(final String titulo, final String subtitulo) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div style=\"background:#f3f4f7;padding:20px 0;font-family:'Segoe UI',Arial,sans-serif;\">");
		sb.append("<table role=\"presentation\" width=\"640\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\""
				+ " style=\"width:640px;max-width:100%;background:#ffffff;border-radius:10px;\">");
		sb.append("<tr><td style=\"background:").append(AZUL).append(";padding:22px 26px;border-bottom:5px solid ")
				.append(AMARILLO).append(";border-radius:10px 10px 0 0;\">");
		sb.append("<div style=\"color:#ffffff;font-size:20px;font-weight:bold;\">").append(h(titulo))
				.append("</div>");
		sb.append("<div style=\"color:#c9d3ee;font-size:13px;margin-top:4px;\">").append(h(subtitulo))
				.append("</div>");
		sb.append("</td></tr>");
		sb.append("<tr><td style=\"padding:22px 26px;color:#1c1f24;font-size:14px;\">");
		return sb.toString();
	}

	public static String cerrar(final String pie) {
		return "</td></tr><tr><td style=\"background:#fafbfc;padding:14px 26px;color:" + GRIS
				+ ";font-size:12px;border-radius:0 0 10px 10px;\">" + h(pie) + "</td></tr></table></div>";
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

	public static String abrirTabla(final String titulo, final String... encabezados) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div style=\"font-size:15px;font-weight:bold;color:").append(AZUL)
				.append(";margin:0 0 8px;\">").append(h(titulo)).append("</div>");
		sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\""
				+ " style=\"border-collapse:collapse;font-size:13px;\"><tr>");
		for (int i = 0; i < encabezados.length; i++) {
			sb.append("<td style=\"background:#eef1f8;color:").append(AZUL)
					.append(";font-weight:bold;padding:9px 12px;text-align:").append(i == 0 ? "left" : "right")
					.append(";\">").append(h(encabezados[i])).append("</td>");
		}
		sb.append("</tr>");
		return sb.toString();
	}

	public static String fila(final String... celdas) {
		final StringBuilder sb = new StringBuilder("<tr>");
		for (int i = 0; i < celdas.length; i++) {
			sb.append("<td style=\"padding:8px 12px;border-bottom:1px solid #e7e9ee;text-align:")
					.append(i == 0 ? "left" : "right").append(";\">").append(h(celdas[i])).append("</td>");
		}
		return sb.append("</tr>").toString();
	}

	public static String filaTotal(final String... celdas) {
		final StringBuilder sb = new StringBuilder("<tr>");
		for (int i = 0; i < celdas.length; i++) {
			sb.append("<td style=\"padding:10px 12px;background:#fff8e1;border-top:2px solid ").append(AZUL)
					.append(";font-weight:bold;color:").append(AZUL).append(";text-align:")
					.append(i == 0 ? "left" : "right").append(";\">").append(h(celdas[i])).append("</td>");
		}
		return sb.append("</tr>").toString();
	}

	public static String cerrarTabla() {
		return "</table><div style=\"height:20px;line-height:20px;\">&nbsp;</div>";
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

	/** Recuadro rojo para lo que el lector no debe pasar por alto. */
	public static String aviso(final String texto) {
		return "<div style=\"border-left:5px solid " + ROJO + ";background:#fdeceb;color:#8a1c1f;padding:12px 14px;"
				+ "font-size:13px;margin-bottom:20px;\">" + h(texto) + "</div>";
	}

}
