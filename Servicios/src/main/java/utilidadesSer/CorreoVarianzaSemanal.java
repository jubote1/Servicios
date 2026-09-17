package utilidadesSer;

import java.util.ArrayList;

/**
 * El correo semanal de varianza de inventario.
 *
 * Lo que responde, en este orden: cuanta plata se perdio en la semana, en que
 * tienda, y en cual de los insumos caros. Esa es la lectura que sirve un
 * domingo; el resto es respaldo.
 *
 * Por que los caros van al detalle y las carnes acumuladas: medido sobre 30
 * dias, los caros son el 64% del faltante y el queso solo es la mitad de todo.
 * Poner los 120 insumos al detalle esconderia eso en tres paginas de ceros.
 *
 * Se muestra tambien una linea de "resto de insumos" que nadie pidio, y es a
 * proposito: sin ella el total de la tienda no cuadra con lo que esta arriba, y
 * un numero que no cuadra le quita credibilidad a todo el correo.
 *
 * Sobre el HTML: estilos en linea y maquetacion con tablas. Outlook descarta el
 * bloque style, asi que una hoja de estilos se ve bien en Gmail y se desarma en
 * Outlook, que es donde lo van a abrir.
 */
public final class CorreoVarianzaSemanal {

	private static final String AZUL = "#102F6F";
	private static final String ROJO = "#C21C1F";
	private static final String VERDE = "#16704F";
	private static final String AMBAR = "#8A6400";
	private static final String TINTA = "#1F2430";
	private static final String TINTA_2 = "#6C7482";
	private static final String LINEA = "#E2E6EC";
	private static final String FONDO = "#F4F6F8";
	private static final String FUENTE = "Segoe UI, Roboto, Helvetica, Arial, sans-serif";

	private CorreoVarianzaSemanal() {
		super();
	}

	// =======================================================================
	// Lo que recibe
	// =======================================================================

	/** Una tienda con su detalle de caros y sus acumulados. */
	public static class FilaTienda {
		public String tienda = "";
		/** Un caro: nombre, unidad, cantidad, neto. */
		public ArrayList<Object[]> caros = new ArrayList<Object[]>();
		public int carosEnCero = 0;
		public double netoCaros;
		public double netoCarnes;
		public double netoOtros;
		public double faltante;
		public double sobrante;

		public double neto() {
			return (netoCaros + netoCarnes + netoOtros);
		}
	}

	/** El asunto. Sin tildes ni enes a proposito: el asunto no viaja en UTF-8. */
	public static String asunto(final String desde, final String hasta, final double neto) {
		final String signo = neto < 0 ? "perdida" : "a favor";
		return ("Varianza de inventario " + desde + " a " + hasta + " - " + signo + " "
				+ pesos(Math.abs(neto)));
	}

	// =======================================================================
	// El cuerpo
	// =======================================================================

	public static String cuerpo(final ArrayList<FilaTienda> filas, final String desde,
			final String hasta) {
		double faltante = 0;
		double sobrante = 0;
		double caros = 0;
		double carnes = 0;
		double otros = 0;
		for (int i = 0; i < filas.size(); i++) {
			faltante = faltante + filas.get(i).faltante;
			sobrante = sobrante + filas.get(i).sobrante;
			caros = caros + filas.get(i).netoCaros;
			carnes = carnes + filas.get(i).netoCarnes;
			otros = otros + filas.get(i).netoOtros;
		}
		final double neto = caros + carnes + otros;

		final StringBuilder h = new StringBuilder();
		h.append("<div style=\"background-color:").append(FONDO)
			.append(";padding:18px 0;font-family:").append(FUENTE).append(";\">");
		h.append("<div style=\"max-width:860px;margin:0 auto;\">");

		encabezado(h, desde, hasta);
		tarjetas(h, faltante, sobrante, neto);
		resumenPorTienda(h, filas, caros, carnes, otros, neto);
		detallePorTienda(h, filas);
		pie(h);

		h.append("</div></div>");
		return (h.toString());
	}

	private static void encabezado(final StringBuilder h, final String desde, final String hasta) {
		h.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ")
			.append("style=\"background-color:").append(AZUL)
			.append(";border-radius:6px 6px 0 0;\"><tr><td style=\"padding:16px 20px;\">");
		h.append("<div style=\"color:#FFFFFF;font-size:19px;font-weight:bold;\">")
			.append("Varianza de inventario de la semana</div>");
		h.append("<div style=\"color:#C9D2E8;font-size:13px;margin-top:3px;\">Del ")
			.append(desde).append(" al ").append(hasta).append("</div>");
		h.append("</td></tr></table>");
	}

	/** Las tres cifras de arriba: lo que falto, lo que sobro y el neto. */
	private static void tarjetas(final StringBuilder h, final double faltante,
			final double sobrante, final double neto) {
		h.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ")
			.append("style=\"background-color:#FFFFFF;border-left:1px solid ").append(LINEA)
			.append(";border-right:1px solid ").append(LINEA).append(";\"><tr>");
		tarjeta(h, "Faltante", pesos(Math.abs(faltante)), ROJO, "plata que se perdio");
		tarjeta(h, "Sobrante", pesos(Math.abs(sobrante)), VERDE, "revisar conteo o receta");
		tarjeta(h, neto < 0 ? "Perdida neta" : "Neto a favor", pesos(Math.abs(neto)),
				neto < 0 ? ROJO : VERDE, neto < 0 ? "faltante menos sobrante" : "sobro mas de lo que falto");
		h.append("</tr></table>");
	}

	private static void tarjeta(final StringBuilder h, final String rotulo, final String cifra,
			final String color, final String pie) {
		h.append("<td width=\"33%\" style=\"padding:14px 18px;vertical-align:top;\">");
		h.append("<div style=\"font-size:11px;color:").append(TINTA_2)
			.append(";text-transform:uppercase;letter-spacing:.04em;\">").append(rotulo).append("</div>");
		h.append("<div style=\"font-size:23px;font-weight:bold;color:").append(color)
			.append(";padding-top:2px;\">").append(cifra).append("</div>");
		h.append("<div style=\"font-size:11.5px;color:").append(TINTA_2).append(";\">")
			.append(pie).append("</div>");
		h.append("</td>");
	}

	/** Tabla de tiendas, de la que mas pierde a la que menos. */
	private static void resumenPorTienda(final StringBuilder h, final ArrayList<FilaTienda> filas,
			final double caros, final double carnes, final double otros, final double neto) {
		abrirPanel(h, "Por tienda", "De la que mas pierde a la que menos.");

		h.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ")
			.append("style=\"font-size:13px;color:").append(TINTA).append(";\">");
		h.append("<tr>");
		celdaTitulo(h, "Tienda", "left");
		celdaTitulo(h, "Caros", "right");
		celdaTitulo(h, "Carnes", "right");
		celdaTitulo(h, "Resto", "right");
		celdaTitulo(h, "Total", "right");
		h.append("</tr>");

		for (int i = 0; i < filas.size(); i++) {
			final FilaTienda f = filas.get(i);
			h.append("<tr>");
			celda(h, f.tienda, "left", TINTA, false);
			celda(h, pesos(f.netoCaros), "right", color(f.netoCaros), false);
			celda(h, pesos(f.netoCarnes), "right", color(f.netoCarnes), false);
			celda(h, pesos(f.netoOtros), "right", color(f.netoOtros), false);
			celda(h, pesos(f.neto()), "right", color(f.neto()), true);
			h.append("</tr>");
		}

		h.append("<tr style=\"background-color:#F7F9FC;\">");
		celda(h, "TOTAL", "left", TINTA, true);
		celda(h, pesos(caros), "right", color(caros), true);
		celda(h, pesos(carnes), "right", color(carnes), true);
		celda(h, pesos(otros), "right", color(otros), true);
		celda(h, pesos(neto), "right", color(neto), true);
		h.append("</tr>");
		h.append("</table>");
		cerrarPanel(h);
	}

	/** Por cada tienda, los caros uno por uno y despues los acumulados. */
	private static void detallePorTienda(final StringBuilder h, final ArrayList<FilaTienda> filas) {
		for (int i = 0; i < filas.size(); i++) {
			final FilaTienda f = filas.get(i);
			abrirPanel(h, f.tienda, "Detalle de los insumos caros. Carnes y resto van sumados.");

			h.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ")
				.append("style=\"font-size:13px;color:").append(TINTA).append(";\">");
			h.append("<tr>");
			celdaTitulo(h, "Insumo", "left");
			celdaTitulo(h, "Cantidad", "right");
			celdaTitulo(h, "Unidad", "left");
			celdaTitulo(h, "Valor", "right");
			h.append("</tr>");

			for (int j = 0; j < f.caros.size(); j++) {
				final Object[] c = f.caros.get(j);
				final double valor = ((Double) c[3]).doubleValue();
				h.append("<tr>");
				celda(h, texto(c[0]), "left", TINTA, false);
				celda(h, numero(((Double) c[2]).doubleValue()), "right", TINTA_2, false);
				celda(h, texto(c[1]), "left", TINTA_2, false);
				celda(h, pesos(valor), "right", color(valor), false);
				h.append("</tr>");
			}
			if (f.caros.isEmpty()) {
				h.append("<tr><td colspan=\"4\" style=\"padding:8px 6px;color:").append(TINTA_2)
					.append(";font-size:12.5px;\">Ningun insumo caro se movio esta semana.</td></tr>");
			}
			if (f.carosEnCero > 0) {
				h.append("<tr><td colspan=\"4\" style=\"padding:4px 6px;color:").append(TINTA_2)
					.append(";font-size:11.5px;\">(").append(f.carosEnCero)
					.append(" insumo(s) caro(s) quedaron en cero y no se listan)</td></tr>");
			}

			h.append("<tr style=\"background-color:#F7F9FC;\">");
			celda(h, "Subtotal caros", "left", TINTA, true);
			celda(h, "", "right", TINTA, false);
			celda(h, "", "left", TINTA, false);
			celda(h, pesos(f.netoCaros), "right", color(f.netoCaros), true);
			h.append("</tr>");

			h.append("<tr>");
			celda(h, "Carnes (acumulado)", "left", TINTA, true);
			celda(h, "", "right", TINTA, false);
			celda(h, "", "left", TINTA, false);
			celda(h, pesos(f.netoCarnes), "right", color(f.netoCarnes), true);
			h.append("</tr>");

			h.append("<tr>");
			celda(h, "Resto de insumos", "left", TINTA, true);
			celda(h, "", "right", TINTA, false);
			celda(h, "", "left", TINTA, false);
			celda(h, pesos(f.netoOtros), "right", color(f.netoOtros), true);
			h.append("</tr>");

			h.append("<tr style=\"background-color:#EFF2F6;\">");
			celda(h, "TOTAL " + f.tienda.toUpperCase(), "left", TINTA, true);
			celda(h, "", "right", TINTA, false);
			celda(h, "", "left", TINTA, false);
			celda(h, pesos(f.neto()), "right", color(f.neto()), true);
			h.append("</tr>");
			h.append("</table>");
			cerrarPanel(h);
		}
	}

	private static void pie(final StringBuilder h) {
		h.append("<div style=\"font-size:11.5px;color:").append(TINTA_2)
			.append(";padding:10px 4px 0;\">");
		h.append("Negativo es faltante, o sea plata que se perdio. Positivo es sobrante, que casi ")
			.append("nunca es ganancia: suele ser un conteo malo o una receta mal parametrizada.<br>");
		h.append("Los valores salen del costo que tiene cada insumo en el maestro de inventarios. ")
			.append("Un insumo sin costo cargado no suma nada aca.<br>");
		h.append("Poblado y Medayoung no aparecen porque no replican su inventario al central.");
		h.append("</div>");
	}

	// =======================================================================
	// Ladrillos
	// =======================================================================

	private static void abrirPanel(final StringBuilder h, final String titulo, final String sub) {
		h.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ")
			.append("style=\"background-color:#FFFFFF;border:1px solid ").append(LINEA)
			.append(";border-radius:6px;margin-top:12px;\"><tr><td style=\"padding:14px 18px;\">");
		h.append("<div style=\"font-size:15.5px;font-weight:bold;color:").append(AZUL)
			.append(";\">").append(escapar(titulo)).append("</div>");
		h.append("<div style=\"font-size:12px;color:").append(TINTA_2)
			.append(";padding:2px 0 10px;\">").append(sub).append("</div>");
	}

	private static void cerrarPanel(final StringBuilder h) {
		h.append("</td></tr></table>");
	}

	private static void celdaTitulo(final StringBuilder h, final String texto, final String alinea) {
		h.append("<td style=\"padding:5px 6px;border-bottom:2px solid ").append(LINEA)
			.append(";text-align:").append(alinea).append(";font-size:11px;color:").append(TINTA_2)
			.append(";text-transform:uppercase;letter-spacing:.04em;\">").append(texto).append("</td>");
	}

	private static void celda(final StringBuilder h, final String texto, final String alinea,
			final String color, final boolean negrita) {
		h.append("<td style=\"padding:5px 6px;border-bottom:1px solid ").append(LINEA)
			.append(";text-align:").append(alinea).append(";color:").append(color).append(";");
		if (negrita) {
			h.append("font-weight:bold;");
		}
		h.append("\">").append(escapar(texto)).append("</td>");
	}

	private static String color(final double valor) {
		if (valor < 0) {
			return (ROJO);
		}
		if (valor > 0) {
			return (VERDE);
		}
		return (AMBAR);
	}

	private static String texto(final Object valor) {
		return (valor == null ? "" : valor.toString());
	}

	/** Pesos redondeados con punto de miles. Sin centavos: nadie los lee. */
	public static String pesos(final double valor) {
		final long n = Math.round(valor);
		return ((n < 0 ? "-$ " : "$ ") + numeroEntero(Math.abs(n)));
	}

	/** Cantidades con un decimal: la varianza en gramos trae medios gramos. */
	private static String numero(final double valor) {
		final double redondeado = Math.round(valor * 10.0) / 10.0;
		final long entero = (long) Math.abs(redondeado);
		final long decimal = Math.round((Math.abs(redondeado) - entero) * 10.0);
		final String signo = redondeado < 0 ? "-" : "";
		if (decimal == 0) {
			return (signo + numeroEntero(entero));
		}
		return (signo + numeroEntero(entero) + "," + decimal);
	}

	private static String numeroEntero(final long valor) {
		final String crudo = Long.toString(Math.abs(valor));
		final StringBuilder salida = new StringBuilder();
		int cuenta = 0;
		for (int i = crudo.length() - 1; i >= 0; i--) {
			salida.insert(0, crudo.charAt(i));
			cuenta++;
			if (cuenta % 3 == 0 && i > 0) {
				salida.insert(0, '.');
			}
		}
		return ((valor < 0 ? "-" : "") + salida.toString());
	}

	/** Un nombre con & o < romperia el HTML del correo. */
	private static String escapar(final String texto) {
		if (texto == null) {
			return ("");
		}
		return (texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
	}
}
