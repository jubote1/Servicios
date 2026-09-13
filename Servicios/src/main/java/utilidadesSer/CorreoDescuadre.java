package utilidadesSer;

import java.util.ArrayList;

import capaDAOPOS.PedidoDAO.Descuadre;

/**
 * El correo que avisa de los descuadres de forma de pago de una tienda.
 *
 * El anterior decia, completo: "Ojo en la tienda: Niquia tiene descuadre en 1
 * descuadrados." Nada mas. En la tienda no habia como saber cual pedido mirar,
 * asi que el aviso llegaba todos los dias y no se podia atender ninguno.
 *
 * Aqui va la lista: que pedido, cuanto suma, cuanto se registro y cuanto falta.
 * Con eso el Administrador de tienda lo cuadra antes del cierre, que es el
 * momento en que todavia se puede.
 *
 * Todo con estilos EN LINEA y maquetado con tablas: Outlook descarta el
 * &lt;style&gt; del encabezado. Y sin una sola imagen, porque Gmail las bloquea
 * por defecto y el correo llegaria vacio.
 */
public final class CorreoDescuadre {

	private static final String AZUL = "#102F6F";
	private static final String AMARILLO = "#FDC806";
	private static final String ROJO_TEXTO = "#C21C1F";
	private static final String VERDE_TEXTO = "#16704F";
	private static final String TINTA = "#1F2430";
	private static final String TINTA_2 = "#6C7482";
	private static final String LINEA = "#E2E6EC";
	private static final String FUENTE = "Segoe UI, Roboto, Helvetica, Arial, sans-serif";

	private CorreoDescuadre() {
		super();
	}

	/** El asunto conserva el prefijo para no romper los filtros que ya existen. */
	public static String asunto(final String nombreTienda, final String fecha) {
		return ("OJO DESCUADRE FORMA PAGO " + nombreTienda + " " + fecha);
	}

	public static String cuerpo(final String nombreTienda, final String fecha,
			final ArrayList<Descuadre> descuadres) {
		final StringBuilder m = new StringBuilder();
		m.append("<div style=\"margin:0;padding:18px 0;background-color:#F4F6F8;font-family:")
				.append(FUENTE).append(";\">");
		m.append("<table cellpadding='0' cellspacing='0' border='0' align='center' width='100%'")
				.append(" style='max-width:680px;border-collapse:collapse;background-color:#FFFFFF;'>");

		m.append("<tr><td style=\"background-color:").append(AZUL).append(";padding:16px 22px;\">")
				.append("<div style=\"font-size:18px;font-weight:bold;color:#FFFFFF;\">")
				.append("PIZZA <span style=\"color:").append(AMARILLO).append(";\">AMERICANA</span></div>")
				.append("</td></tr>");
		m.append("<tr><td style=\"background-color:").append(AMARILLO)
				.append(";font-size:0;line-height:0;height:5px;\">&nbsp;</td></tr>");

		final int cuantos = (descuadres == null ? 0 : descuadres.size());
		m.append("<tr><td style=\"padding:20px 22px 4px;\">")
				.append("<div style=\"font-size:20px;font-weight:bold;color:").append(TINTA).append(";\">")
				.append(cuantos == 1 ? "Hay 1 pedido descuadrado" : "Hay " + cuantos + " pedidos descuadrados")
				.append("</div>")
				.append("<div style=\"font-size:13.5px;color:").append(TINTA_2).append(";padding-top:5px;\">")
				.append(escapar(nombreTienda)).append(" &nbsp;&middot;&nbsp; ").append(escapar(fecha))
				.append("</div></td></tr>");

		m.append("<tr><td style=\"padding:10px 22px 4px;font-size:13.5px;color:").append(TINTA)
				.append(";line-height:1.55;\">")
				.append("El total del pedido no coincide con lo que dicen sus formas de pago. ")
				.append("Se corrigen desde el POS <b>antes del cierre</b>: despues ya queda asi en la venta del dia.")
				.append("</td></tr>");

		m.append("<tr><td style=\"padding:8px 22px 18px;\">");
		m.append("<table cellpadding='6' cellspacing='0' border='0'")
				.append(" style=\"border-collapse:collapse;width:100%;font-size:13px;\">");
		m.append("<tr>")
				.append(celdaEncabezado("Pedido"))
				.append(celdaEncabezado("Total del pedido"))
				.append(celdaEncabezado("Registrado en pagos"))
				.append(celdaEncabezado("Diferencia"))
				.append(celdaEncabezado("Que pasa"))
				.append("</tr>");

		double faltante = 0.0;
		double sobrante = 0.0;
		for (int i = 0; (descuadres != null) && (i < descuadres.size()); i++) {
			final Descuadre d = descuadres.get(i);
			final double dif = d.diferencia();
			//Se llevan separados a proposito. Un neto mezclaria las dos cosas: con un
			//pedido al que le faltan 20.000 y otro al que le sobran 20.000 el neto da
			//cero, y no hay nada mas lejos de la verdad que decir que todo cuadra.
			if (dif > 0) {
				faltante = faltante + dif;
			} else {
				sobrante = sobrante - dif;
			}
			//Positiva: el pedido vale mas de lo que se registro, o sea que falta
			//plata por cobrar. Negativa: se registro de mas, que es lo que pasa
			//cuando un pago quedo dos veces.
			final String explicacion = (dif > 0 ? "Falta registrar el pago" : "Hay un pago de mas");
			final String color = (dif > 0 ? ROJO_TEXTO : VERDE_TEXTO);
			m.append("<tr>")
					.append(celda("<b>#" + d.idPedido + "</b>", TINTA, "left"))
					.append(celda(pesos(d.total), TINTA, "right"))
					.append(celda(pesos(d.pagado), TINTA, "right"))
					.append(celda("<b>" + pesos(Math.abs(dif)) + "</b>", color, "right"))
					.append(celda(explicacion, color, "left"))
					.append("</tr>");
		}
		m.append("</table></td></tr>");

		if (faltante > 0.0 || sobrante > 0.0) {
			m.append("<tr><td style=\"padding:0 22px 18px;font-size:13.5px;color:").append(TINTA).append(";\">");
			if (faltante > 0.0) {
				m.append("Falta registrar <b style=\"color:").append(ROJO_TEXTO).append(";\">")
						.append(pesos(faltante)).append("</b>.");
			}
			if (faltante > 0.0 && sobrante > 0.0) {
				m.append(" &nbsp; ");
			}
			if (sobrante > 0.0) {
				m.append("Hay <b style=\"color:").append(VERDE_TEXTO).append(";\">").append(pesos(sobrante))
						.append("</b> registrados de mas.");
			}
			m.append("</td></tr>");
		}

		m.append("<tr><td style=\"padding:14px 22px 20px;border-top:1px solid ").append(LINEA)
				.append(";font-size:11.5px;color:#8A9199;line-height:1.5;\">")
				.append("Correo automatico del resumen de operacion. No responda a esta direccion.")
				.append("</td></tr>");

		m.append("</table></div>");
		return (m.toString());
	}

	private static String celdaEncabezado(final String texto) {
		return ("<td style=\"background-color:#F1F4F9;color:" + AZUL + ";font-size:11.5px;font-weight:bold;"
				+ "text-transform:uppercase;letter-spacing:.04em;border-bottom:2px solid " + LINEA
				+ ";text-align:left;\">" + texto + "</td>");
	}

	private static String celda(final String texto, final String color, final String alineacion) {
		return ("<td style=\"border-bottom:1px solid " + LINEA + ";color:" + color + ";text-align:" + alineacion
				+ ";\">" + texto + "</td>");
	}

	/** 21650 -> "$ 21.650". Sin decimales: en la tienda no se manejan centavos. */
	private static String pesos(final double valor) {
		final long redondeado = Math.round(Math.abs(valor));
		final String digitos = Long.toString(redondeado);
		final StringBuilder conPuntos = new StringBuilder();
		for (int i = 0; i < digitos.length(); i++) {
			if (i > 0 && (digitos.length() - i) % 3 == 0) {
				conPuntos.append('.');
			}
			conPuntos.append(digitos.charAt(i));
		}
		return ((valor < 0 ? "-$ " : "$ ") + conPuntos);
	}

	private static String escapar(final String valor) {
		if (valor == null) {
			return ("");
		}
		return (valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
	}
}
