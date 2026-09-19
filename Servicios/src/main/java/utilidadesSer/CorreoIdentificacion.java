package utilidadesSer;

import java.util.ArrayList;

/**
 * El correo que publica cuanto se identifica al cliente en el mostrador.
 *
 * Es la medicion del paso 3 de "Un Solo Cliente", y existe por una razon muy
 * concreta: ese paso es el unico que depende de la gente y no del codigo, y sin
 * la medicion publicada se desinfla en dos semanas.
 *
 * Por eso el correo esta armado para que se pueda comparar: las tiendas
 * ordenadas de mejor a peor, y dentro de cada una los cajeros. No es un
 * informe para archivar, es una tabla de posiciones.
 */
public final class CorreoIdentificacion {

	private static final String AZUL = "#102F6F";
	private static final String AMARILLO = "#FDC806";
	private static final String VERDE = "#16704F";
	private static final String ROJO = "#C21C1F";
	private static final String AMBAR = "#8A6400";
	private static final String TINTA = "#1F2430";
	private static final String TINTA_2 = "#6C7482";
	private static final String LINEA = "#E2E6EC";
	private static final String FUENTE = "Segoe UI, Roboto, Helvetica, Arial, sans-serif";

	/** De aqui para arriba se considera bien. */
	private static final int META = 70;

	/** De aqui para abajo es urgente. */
	private static final int MALO = 40;

	private CorreoIdentificacion() {
		super();
	}

	/** Una tienda con su cifra y el detalle por cajero. */
	public static class FilaTienda {
		public String tienda = "";
		public int pedidos = 0;
		public int identificados = 0;

		/**
		 * De los no identificados, a cuantos SI se les pregunto y el cliente no
		 * dio el dato. Es lo que separa dos problemas que hasta ahora se veian
		 * iguales: el cliente que se niega -hay que darle una razon- y el pedido
		 * en el que nadie pregunto -es de operacion-.
		 *
		 * Queda en cero donde todavia no se prendio PEDIRCLIENTEPV, que es lo
		 * correcto: ahi nadie esta preguntando.
		 */
		public int seNegaron = 0;

		public ArrayList<String[]> cajeros = new ArrayList<String[]>();

		public int porcentaje() {
			return (this.pedidos == 0 ? 0 : (this.identificados * 100) / this.pedidos);
		}

		/** Ni identificado ni preguntado. */
		public int sinPreguntar() {
			final int resto = this.pedidos - this.identificados - this.seNegaron;
			return (resto > 0 ? resto : 0);
		}
	}

	public static String asunto(final String desde, final String hasta) {
		return ("IDENTIFICACION DE CLIENTE EN MOSTRADOR " + desde + " a " + hasta);
	}

	public static String cuerpo(final ArrayList<FilaTienda> filas, final String desde, final String hasta) {
		int totalPedidos = 0;
		int totalIdent = 0;
		for (int i = 0; i < filas.size(); i++) {
			totalPedidos = totalPedidos + filas.get(i).pedidos;
			totalIdent = totalIdent + filas.get(i).identificados;
		}
		final int totalPct = (totalPedidos == 0 ? 0 : (totalIdent * 100) / totalPedidos);

		final StringBuilder m = new StringBuilder();
		m.append("<div style=\"margin:0;padding:18px 0;background-color:#F4F6F8;font-family:")
				.append(FUENTE).append(";\">");
		m.append("<table cellpadding='0' cellspacing='0' border='0' align='center' width='100%'")
				.append(" style='max-width:720px;border-collapse:collapse;background-color:#FFFFFF;'>");

		m.append("<tr><td style=\"background-color:").append(AZUL).append(";padding:16px 22px;\">")
				.append("<div style=\"font-size:18px;font-weight:bold;color:#FFFFFF;\">")
				.append("PIZZA <span style=\"color:").append(AMARILLO).append(";\">AMERICANA</span></div>")
				.append("</td></tr>");
		m.append("<tr><td style=\"background-color:").append(AMARILLO)
				.append(";font-size:0;line-height:0;height:5px;\">&nbsp;</td></tr>");

		m.append("<tr><td style=\"padding:20px 22px 4px;\">")
				.append("<div style=\"font-size:20px;font-weight:bold;color:").append(TINTA).append(";\">")
				.append("Identificacion del cliente en el mostrador</div>")
				.append("<div style=\"font-size:13.5px;color:").append(TINTA_2).append(";padding-top:5px;\">")
				.append(escapar(desde)).append(" a ").append(escapar(hasta))
				.append("</div></td></tr>");

		m.append("<tr><td style=\"padding:12px 22px 4px;\">")
				.append("<div style=\"font-size:34px;font-weight:bold;color:").append(color(totalPct)).append(";\">")
				.append(totalPct).append("%</div>")
				.append("<div style=\"font-size:13px;color:").append(TINTA_2).append(";\">")
				.append(totalIdent).append(" de ").append(totalPedidos)
				.append(" pedidos de mostrador quedaron con un cliente identificable.</div>")
				.append("</td></tr>");

		m.append("<tr><td style=\"padding:14px 22px 18px;\">");
		m.append("<table cellpadding='6' cellspacing='0' border='0'")
				.append(" style=\"border-collapse:collapse;width:100%;font-size:13px;\">");
		//Las dos ultimas columnas son el aporte del paso 3: parten el "no
		//identificado" en el que se nego y el que nadie pregunto.
		m.append("<tr>").append(th("Tienda")).append(th("Pedidos")).append(th("Identificados"))
				.append(th("%")).append(th("Se nego")).append(th("No se pregunto")).append("</tr>");
		for (int i = 0; i < filas.size(); i++) {
			final FilaTienda f = filas.get(i);
			m.append("<tr>")
					.append(td("<b>" + escapar(f.tienda) + "</b>", TINTA, "left"))
					.append(td(Integer.toString(f.pedidos), TINTA, "right"))
					.append(td(Integer.toString(f.identificados), TINTA, "right"))
					.append(td("<b>" + f.porcentaje() + "%</b>", color(f.porcentaje()), "right"))
					.append(td(Integer.toString(f.seNegaron), TINTA_2, "right"))
					.append(td(Integer.toString(f.sinPreguntar()), TINTA_2, "right"))
					.append("</tr>");
			for (int j = 0; j < f.cajeros.size(); j++) {
				final String[] c = f.cajeros.get(j);
				m.append("<tr>")
						.append(td("&nbsp;&nbsp;&nbsp;" + escapar(c[0]), TINTA_2, "left"))
						.append(td(c[1], TINTA_2, "right"))
						.append(td(c[2], TINTA_2, "right"))
						.append(td(c[3] + "%", color(entero(c[3])), "right"))
						.append(td(c.length > 4 ? c[4] : "0", TINTA_2, "right"))
						.append(td(c.length > 5 ? c[5] : "0", TINTA_2, "right"))
						.append("</tr>");
			}
		}
		m.append("</table></td></tr>");

		m.append("<tr><td style=\"padding:0 22px 18px;font-size:13px;color:").append(TINTA)
				.append(";line-height:1.55;\">")
				.append("Se cuenta identificado cuando el pedido quedo con un cliente que tiene celular. ")
				.append("Para comparar: en <b>Para Llevar</b> se identifica alrededor del 90% en todas las tiendas, ")
				.append("asi que el sistema si da: es cuestion de preguntarlo.")
				.append("<br><br>")
				.append("<b>Se nego</b> son los pedidos en que la caja SI pregunto y el cliente no dio el dato. ")
				.append("<b>No se pregunto</b> es el resto. Son dos problemas distintos: el primero se ataca ")
				.append("dandole una razon al cliente, el segundo con la operacion de la caja. ")
				.append("En las tiendas donde todavia no se prendio el parametro PEDIRCLIENTEPV, ")
				.append("\"Se nego\" sale en cero porque alli nadie esta preguntando.")
				.append("</td></tr>");

		m.append("<tr><td style=\"padding:14px 22px 20px;border-top:1px solid ").append(LINEA)
				.append(";font-size:11.5px;color:#8A9199;line-height:1.5;\">")
				.append("Correo automatico. No responda a esta direccion.")
				.append("</td></tr>");

		m.append("</table></div>");
		return (m.toString());
	}

	private static String color(final int pct) {
		if (pct >= META) {
			return (VERDE);
		}
		if (pct < MALO) {
			return (ROJO);
		}
		return (AMBAR);
	}

	private static int entero(final String valor) {
		try {
			return (Integer.parseInt(valor.trim()));
		} catch (final Exception e) {
			return (0);
		}
	}

	private static String th(final String texto) {
		return ("<td style=\"background-color:#F1F4F9;color:" + AZUL + ";font-size:11.5px;font-weight:bold;"
				+ "text-transform:uppercase;letter-spacing:.04em;border-bottom:2px solid " + LINEA
				+ ";text-align:left;\">" + texto + "</td>");
	}

	private static String td(final String texto, final String color, final String alineacion) {
		return ("<td style=\"border-bottom:1px solid " + LINEA + ";color:" + color + ";text-align:" + alineacion
				+ ";\">" + texto + "</td>");
	}

	private static String escapar(final String valor) {
		if (valor == null) {
			return ("");
		}
		return (valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
	}
}
