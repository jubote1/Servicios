package utilidadesSer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import ModeloSer.FilaResumenOperacion;

/**
 * Arma el HTML del correo de resumen de operacion.
 *
 * Va con tablas y estilos en linea a proposito: Outlook no entiende CSS moderno y
 * un correo que se ve bien en el navegador llega descuadrado al escritorio.
 *
 * Las barritas de color son celdas de tabla con bgcolor, no imagenes. Un grafico
 * de imagen no sirve aca: Outlook bloquea las imagenes remotas por defecto y la
 * mitad de la gente veria un cuadro vacio.
 */
public class CorreoResumenOperacion {

	/** Largo del riel de la barrita, en pixeles. */
	private static final int RIEL = 52;
	/** Minutos que llenan el riel completo. */
	private static final int REF = 60;
	/** Hasta aca la barrita va verde. */
	private static final int TOPE_VERDE = 50;
	/** Hasta aca va amarilla; de ahi para arriba, roja. */
	private static final int TOPE_AMARILLO = 60;

	private static final String AZUL = "#102F6F";
	private static final String AZUL_BORDE = "#2A4785";
	private static final String AZUL_CLARO = "#BDCCEA";
	private static final String AMARILLO = "#FDC806";
	private static final String VERDE = "#1E9268";
	private static final String VERDE_TEXTO = "#16704F";
	private static final String AMBAR_TEXTO = "#8A6400";
	private static final String ROJO = "#E42528";
	private static final String ROJO_TEXTO = "#C21C1F";
	private static final String RIEL_GRIS = "#E3E6EC";
	private static final String FILA_ROJA = "#FDECEC";
	private static final String FILA_AMBAR = "#FFF8E4";
	private static final String FILA_ALTERNA = "#FAFBFC";
	private static final String TINTA = "#14181F";
	private static final String TINTA_2 = "#4E5765";
	private static final String TINTA_3 = "#9AA1AC";
	private static final String GRIS_SUAVE = "#F2F4F8";
	private static final String LINEA = "#EDEFF3";
	private static final String FUENTE = "Arial,Helvetica,sans-serif";

	/** Los miles con punto, sin depender de la configuracion regional del servidor. */
	private static DecimalFormat formatoMiles() {
		final DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("es", "CO"));
		simbolos.setGroupingSeparator('.');
		return (new DecimalFormat("###,###", simbolos));
	}

	private static int nivel(final int minutos) {
		if (minutos <= CorreoResumenOperacion.TOPE_VERDE) {
			return (0);
		}
		if (minutos <= CorreoResumenOperacion.TOPE_AMARILLO) {
			return (1);
		}
		return (2);
	}

	private static String colorBarra(final int nivel) {
		if (nivel == 0) {
			return (CorreoResumenOperacion.VERDE);
		}
		if (nivel == 1) {
			return (CorreoResumenOperacion.AMARILLO);
		}
		return (CorreoResumenOperacion.ROJO);
	}

	private static String colorTexto(final int nivel) {
		if (nivel == 0) {
			return (CorreoResumenOperacion.VERDE_TEXTO);
		}
		if (nivel == 1) {
			return (CorreoResumenOperacion.AMBAR_TEXTO);
		}
		return (CorreoResumenOperacion.ROJO_TEXTO);
	}

	private static int anchoBarra(final int minutos) {
		int ancho = (int) Math.round((double) minutos / CorreoResumenOperacion.REF * CorreoResumenOperacion.RIEL);
		if (ancho > CorreoResumenOperacion.RIEL) {
			ancho = CorreoResumenOperacion.RIEL;
		}
		if (ancho < 3) {
			ancho = 3;
		}
		return (ancho);
	}

	/**
	 * Una celda con el numero de minutos y su barrita al lado.
	 *
	 * @param minutos -1 cuando no hay nada en esa etapa; se pinta un guion.
	 * @param fondo   color de fondo de la celda, o cadena vacia para heredarlo.
	 */
	private static String celdaTiempo(final int minutos, final String fondo) {
		final String estiloFondo = (fondo.length() == 0) ? "" : ("background-color:" + fondo + ";");
		if (minutos < 0) {
			return ("<td style='padding:7px 8px;border-bottom:1px solid " + CorreoResumenOperacion.LINEA + ";"
					+ estiloFondo + "color:" + CorreoResumenOperacion.TINTA_3 + ";font-size:11.5px;'>&mdash;</td>");
		}
		final int nivel = CorreoResumenOperacion.nivel(minutos);
		final int ancho = CorreoResumenOperacion.anchoBarra(minutos);
		final int resto = CorreoResumenOperacion.RIEL - ancho;
		final StringBuilder celda = new StringBuilder();
		celda.append("<td style='padding:7px 8px;border-bottom:1px solid ").append(CorreoResumenOperacion.LINEA)
				.append(";").append(estiloFondo).append("'>");
		celda.append("<table cellpadding='0' cellspacing='0' border='0'><tr>");
		celda.append("<td style='font-size:12px;font-weight:bold;color:").append(CorreoResumenOperacion.colorTexto(nivel))
				.append(";padding-right:6px;white-space:nowrap;'>").append(minutos).append("</td>");
		celda.append("<td><table cellpadding='0' cellspacing='0' border='0' width='").append(CorreoResumenOperacion.RIEL)
				.append("'><tr>");
		celda.append("<td width='").append(ancho).append("' height='7' bgcolor='")
				.append(CorreoResumenOperacion.colorBarra(nivel)).append("' style='font-size:0;line-height:0;'>&nbsp;</td>");
		if (resto > 0) {
			celda.append("<td width='").append(resto).append("' height='7' bgcolor='")
					.append(CorreoResumenOperacion.RIEL_GRIS).append("' style='font-size:0;line-height:0;'>&nbsp;</td>");
		}
		celda.append("</tr></table></td></tr></table></td>");
		return (celda.toString());
	}

	/** Una celda con una cantidad, en gris cuando es cero y en rojo cuando urge. */
	private static String celdaCantidad(final int valor, final boolean urgente) {
		String color = CorreoResumenOperacion.TINTA;
		String peso = "normal";
		if (valor == 0) {
			color = CorreoResumenOperacion.TINTA_3;
		} else if (urgente) {
			color = CorreoResumenOperacion.ROJO_TEXTO;
			peso = "bold";
		}
		return ("<td align='center' style='padding:7px 6px;border-bottom:1px solid " + CorreoResumenOperacion.LINEA
				+ ";color:" + color + ";font-weight:" + peso + ";'>" + valor + "</td>");
	}

	private static String celdaEncabezado(final String texto, final String extra) {
		return ("<td " + extra + " style='padding:7px 6px;color:#FFFFFF;font-size:10.5px;font-weight:bold;"
				+ "border-right:1px solid " + CorreoResumenOperacion.AZUL_BORDE + ";'>" + texto + "</td>");
	}

	/**
	 * El correo completo.
	 *
	 * @param filas       una por tienda, ya ordenadas como se quieren mostrar
	 * @param fechaHora   la fecha y hora de la corrida, para el encabezado
	 */
	public static String armar(final ArrayList<FilaResumenOperacion> filas, final String fechaHora) {
		final DecimalFormat miles = CorreoResumenOperacion.formatoMiles();
		final StringBuilder m = new StringBuilder();

		//Totales de la cadena, para la franja de arriba.
		int totCocina = 0;
		int totPorSalir = 0;
		int totUltHoraDom = 0;
		int totUltHoraOtros = 0;
		int totDomInt = 0;
		int totDomExt = 0;
		double totVenta = 0.0;
		String peorCocinaTienda = "";
		int peorCocinaMin = -1;
		String peorSalirTienda = "";
		int peorSalirMin = -1;
		int conProgramados = 0;
		for (int i = 0; i < filas.size(); i++) {
			final FilaResumenOperacion f = filas.get(i);
			totCocina += f.getPedidosCocina();
			totPorSalir += f.getPedidosPorSalir();
			totUltHoraDom += f.getUltimaHoraDomicilio();
			totUltHoraOtros += f.getUltimaHoraOtros();
			totDomInt += f.getDomiciliariosInternos();
			totDomExt += f.getDomiciliariosExternos();
			totVenta += f.getVentaDia();
			if (f.getMinutosCocina() > peorCocinaMin) {
				peorCocinaMin = f.getMinutosCocina();
				peorCocinaTienda = f.getNombreTienda();
			}
			if (f.getMinutosPorSalir() > peorSalirMin) {
				peorSalirMin = f.getMinutosPorSalir();
				peorSalirTienda = f.getNombreTienda();
			}
			if (f.getProgramados().size() > 0) {
				conProgramados++;
			}
		}

		m.append("<table cellpadding='0' cellspacing='0' border='0' width='100%' style='min-width:800px;")
				.append("border-collapse:collapse;font-family:").append(CorreoResumenOperacion.FUENTE)
				.append(";color:").append(CorreoResumenOperacion.TINTA).append(";'>");

		//Encabezado de marca.
		m.append("<tr><td style='background-color:").append(CorreoResumenOperacion.AZUL).append(";padding:14px 18px;'>");
		m.append("<table cellpadding='0' cellspacing='0' border='0' width='100%'><tr>");
		m.append("<td style='font-size:17px;font-weight:bold;color:#FFFFFF;font-family:")
				.append(CorreoResumenOperacion.FUENTE).append(";'>PIZZA AMERICANA</td>");
		m.append("<td align='right' style='font-size:12px;color:").append(CorreoResumenOperacion.AZUL_CLARO)
				.append(";font-family:").append(CorreoResumenOperacion.FUENTE)
				.append(";'>Resumen de operaci&oacute;n &nbsp;|&nbsp; ").append(fechaHora).append("</td>");
		m.append("</tr></table></td></tr>");
		m.append("<tr><td style='background-color:").append(CorreoResumenOperacion.AMARILLO)
				.append(";font-size:0;line-height:0;height:5px;'>&nbsp;</td></tr>");

		//Franja de totales.
		m.append("<tr><td style='background-color:").append(CorreoResumenOperacion.GRIS_SUAVE).append(";padding:0;'>");
		m.append("<table cellpadding='0' cellspacing='0' border='0' width='100%' style='border-collapse:collapse;'><tr>");
		m.append(CorreoResumenOperacion.tarjetaTotal(String.valueOf(totCocina), "EN COCINA",
				CorreoResumenOperacion.AZUL, true));
		m.append(CorreoResumenOperacion.tarjetaTotal(String.valueOf(totPorSalir), "PENDIENTES POR SALIR",
				CorreoResumenOperacion.ROJO, true));
		m.append(CorreoResumenOperacion.tarjetaTotal(miles.format(totVenta), "VENTA DEL D&Iacute;A",
				CorreoResumenOperacion.TINTA, true));
		m.append(CorreoResumenOperacion.tarjetaTotal(String.valueOf(totDomInt + totDomExt), "DOMICILIARIOS EN TIENDA",
				CorreoResumenOperacion.TINTA, false));
		m.append("</tr></table></td></tr>");

		//La tabla unica.
		m.append("<tr><td style='padding:20px 18px 6px;font-family:").append(CorreoResumenOperacion.FUENTE).append(";'>");
		m.append("<div style='font-size:14px;font-weight:bold;color:").append(CorreoResumenOperacion.AZUL)
				.append(";padding-bottom:3px;'>Las ").append(filas.size())
				.append(" tiendas, de la que tiene m&aacute;s trabajo a la que tiene menos</div>");
		m.append("<div style='font-size:11.5px;color:#6E7784;padding-bottom:11px;'>")
				.append("El orden lo da la cantidad de pedidos que tiene cada tienda en el momento. ")
				.append("Los minutos son del pedido m&aacute;s viejo que sigue en cada etapa, ")
				.append("contados desde que se tom&oacute; el pedido.</div>");

		m.append("<table cellpadding='0' cellspacing='0' border='0' width='100%' style='border-collapse:collapse;")
				.append("font-family:").append(CorreoResumenOperacion.FUENTE).append(";font-size:12.5px;'>");
		m.append("<tr style='background-color:").append(CorreoResumenOperacion.AZUL).append(";'>");
		m.append(CorreoResumenOperacion.celdaEncabezado("TIENDA", "rowspan='2'"));
		m.append(CorreoResumenOperacion.celdaEncabezado("TIEMPO<br>POR TIENDA<br><span style='font-weight:normal;color:"
				+ CorreoResumenOperacion.AZUL_CLARO + ";'>min</span>", "rowspan='2' align='center'"));
		m.append("<td colspan='2' align='center' style='padding:6px 9px;color:#FFFFFF;font-size:10.5px;")
				.append("font-weight:bold;border-right:1px solid ").append(CorreoResumenOperacion.AZUL_BORDE)
				.append(";border-bottom:1px solid ").append(CorreoResumenOperacion.AZUL_BORDE)
				.append(";'>EN COCINA</td>");
		m.append("<td colspan='2' align='center' style='padding:6px 9px;color:#FFFFFF;font-size:10.5px;")
				.append("font-weight:bold;border-right:1px solid ").append(CorreoResumenOperacion.AZUL_BORDE)
				.append(";border-bottom:1px solid ").append(CorreoResumenOperacion.AZUL_BORDE)
				.append(";'>PENDIENTES POR SALIR</td>");
		m.append(CorreoResumenOperacion.celdaEncabezado("&Uacute;LT. HORA<br><span style='font-weight:normal;color:"
				+ CorreoResumenOperacion.AZUL_CLARO + ";'>dom / otros</span>", "rowspan='2' align='center'"));
		m.append(CorreoResumenOperacion.celdaEncabezado("VENTA", "rowspan='2' align='right'"));
		m.append("<td rowspan='2' align='center' style='padding:7px 6px;color:#FFFFFF;font-size:10.5px;")
				.append("font-weight:bold;'>DOMIS<br><span style='font-weight:normal;color:")
				.append(CorreoResumenOperacion.AZUL_CLARO).append(";'>int / ext</span></td>");
		m.append("</tr>");
		m.append("<tr style='background-color:").append(CorreoResumenOperacion.AZUL).append(";'>");
		m.append(CorreoResumenOperacion.subEncabezado("cant.", true));
		m.append(CorreoResumenOperacion.subEncabezado("el m&aacute;s viejo (min)", false));
		m.append(CorreoResumenOperacion.subEncabezado("cant.", true));
		m.append(CorreoResumenOperacion.subEncabezado("el m&aacute;s viejo (min)", false));
		m.append("</tr>");

		for (int i = 0; i < filas.size(); i++) {
			final FilaResumenOperacion f = filas.get(i);
			String fondo = "";
			if (f.getPeorTiempo() > CorreoResumenOperacion.TOPE_AMARILLO) {
				fondo = " style='background-color:" + CorreoResumenOperacion.FILA_ROJA + ";'";
			} else if (f.getPeorTiempo() > CorreoResumenOperacion.TOPE_VERDE) {
				fondo = " style='background-color:" + CorreoResumenOperacion.FILA_AMBAR + ";'";
			} else if (i % 2 == 1) {
				fondo = " style='background-color:" + CorreoResumenOperacion.FILA_ALTERNA + ";'";
			}
			final boolean apagada = (f.getCarga() == 0);
			m.append("<tr").append(fondo).append(">");
			m.append("<td style='padding:7px 9px;border-bottom:1px solid ").append(CorreoResumenOperacion.LINEA)
					.append(";color:").append(apagada ? CorreoResumenOperacion.TINTA_2 : CorreoResumenOperacion.TINTA)
					.append(";font-weight:").append(apagada ? "normal" : "bold").append(";'>")
					.append(f.getNombreTienda());
			if (!f.getSeLeyo()) {
				m.append(" <span style='color:").append(CorreoResumenOperacion.ROJO_TEXTO)
						.append(";font-size:11px;font-weight:bold;'>sin datos</span>");
			}
			m.append("</td>");
			m.append(CorreoResumenOperacion.celdaTiempo(f.getTiempoTienda(), "#F7F8FB"));
			m.append(CorreoResumenOperacion.celdaCantidad(f.getPedidosCocina(), false));
			m.append(CorreoResumenOperacion.celdaTiempo(f.getMinutosCocina(), ""));
			m.append(CorreoResumenOperacion.celdaCantidad(f.getPedidosPorSalir(),
					f.getPedidosPorSalir() > 0 && f.getMinutosPorSalir() > CorreoResumenOperacion.TOPE_AMARILLO));
			m.append(CorreoResumenOperacion.celdaTiempo(f.getMinutosPorSalir(), ""));
			m.append("<td align='center' style='padding:7px 6px;border-bottom:1px solid ")
					.append(CorreoResumenOperacion.LINEA).append(";color:").append(CorreoResumenOperacion.TINTA_2)
					.append(";'>").append(f.getUltimaHoraDomicilio()).append(" / ").append(f.getUltimaHoraOtros())
					.append("</td>");
			m.append("<td align='right' style='padding:7px 9px;border-bottom:1px solid ")
					.append(CorreoResumenOperacion.LINEA).append(";'>").append(miles.format(f.getVentaDia()))
					.append("</td>");
			m.append("<td align='center' style='padding:7px 6px;border-bottom:1px solid ")
					.append(CorreoResumenOperacion.LINEA).append(";color:").append(CorreoResumenOperacion.TINTA_2)
					.append(";'>").append(f.getDomiciliariosInternos()).append(" / ")
					.append(f.getDomiciliariosExternos()).append("</td>");
			m.append("</tr>");
		}

		//Fila de totales. Promediar promesas no significa nada, asi que ahi va el peor.
		m.append("<tr style='background-color:").append(CorreoResumenOperacion.LINEA).append(";'>");
		m.append("<td style='padding:8px 9px;font-weight:bold;'>TOTAL</td>");
		m.append("<td align='center' style='padding:8px 6px;color:").append(CorreoResumenOperacion.TINTA_2)
				.append(";'>&mdash;</td>");
		m.append("<td align='center' style='padding:8px 6px;font-weight:bold;'>").append(totCocina).append("</td>");
		m.append(CorreoResumenOperacion.celdaPeor(peorCocinaTienda, peorCocinaMin));
		m.append("<td align='center' style='padding:8px 6px;font-weight:bold;color:")
				.append(CorreoResumenOperacion.ROJO_TEXTO).append(";'>").append(totPorSalir).append("</td>");
		m.append(CorreoResumenOperacion.celdaPeor(peorSalirTienda, peorSalirMin));
		m.append("<td align='center' style='padding:8px 6px;font-weight:bold;'>").append(totUltHoraDom).append(" / ")
				.append(totUltHoraOtros).append("</td>");
		m.append("<td align='right' style='padding:8px 9px;font-weight:bold;'>").append(miles.format(totVenta))
				.append("</td>");
		m.append("<td align='center' style='padding:8px 6px;font-weight:bold;'>").append(totDomInt).append(" / ")
				.append(totDomExt).append("</td>");
		m.append("</tr></table>");

		//Leyenda de colores.
		m.append("<table cellpadding='0' cellspacing='0' border='0' style='margin-top:12px;font-size:11.5px;")
				.append("color:#6E7784;font-family:").append(CorreoResumenOperacion.FUENTE).append(";'><tr>");
		m.append(CorreoResumenOperacion.muestraColor(CorreoResumenOperacion.VERDE, "0 a 50 min"));
		m.append(CorreoResumenOperacion.muestraColor(CorreoResumenOperacion.AMARILLO, "51 a 60 min"));
		m.append(CorreoResumenOperacion.muestraColor(CorreoResumenOperacion.ROJO,
				"61 min o m&aacute;s &mdash; fuera de tiempo"));
		m.append("</tr></table>");
		m.append("<div style='font-size:11.5px;color:#8A9199;padding-top:8px;'>")
				.append("La misma escala aplica a las tres barritas de cada fila.</div>");
		m.append("</td></tr>");

		//Programados, un bloque por tienda y solo las que tienen.
		if (conProgramados > 0) {
			m.append("<tr><td style='padding:22px 18px 4px;font-family:").append(CorreoResumenOperacion.FUENTE)
					.append(";'>");
			m.append("<div style='font-size:14px;font-weight:bold;color:").append(CorreoResumenOperacion.AZUL)
					.append(";padding-bottom:2px;'>Pedidos programados pendientes</div>");
			m.append("<div style='font-size:11.5px;color:#6E7784;padding-bottom:14px;'>")
					.append("Uno por tienda. Solo aparecen las tiendas que tienen.</div>");
			for (int i = 0; i < filas.size(); i++) {
				final FilaResumenOperacion f = filas.get(i);
				if (f.getProgramados().size() == 0) {
					continue;
				}
				m.append(CorreoResumenOperacion.bloqueProgramados(f, miles));
			}
			m.append("</td></tr>");
		}

		//Pie.
		m.append("<tr><td style='padding:14px 18px 18px;font-family:").append(CorreoResumenOperacion.FUENTE)
				.append(";'>");
		m.append("<div style='font-size:11.5px;color:#8A9199;padding-top:11px;border-top:1px solid ")
				.append(CorreoResumenOperacion.LINEA).append(";'>")
				.append("Los minutos se cuentan desde que se tom&oacute; el pedido. ")
				.append("La fila se pinta seg&uacute;n el peor de los dos tiempos de pedidos. ")
				.append("Los pedidos programados no cuentan como pendientes por salir: van aparte. ")
				.append("Este correo solo se env&iacute;a cuando hay pedidos pendientes en alguna tienda.</div>");
		m.append("</td></tr>");

		m.append("</table>");
		return (m.toString());
	}

	private static String tarjetaTotal(final String numero, final String rotulo, final String color,
			final boolean conBorde) {
		final StringBuilder t = new StringBuilder();
		t.append("<td width='25%' align='center' style='padding:14px 8px;");
		if (conBorde) {
			t.append("border-right:1px solid #E0E4EC;");
		}
		t.append("font-family:").append(CorreoResumenOperacion.FUENTE).append(";'>");
		t.append("<div style='font-size:26px;font-weight:bold;color:").append(color).append(";line-height:1;'>")
				.append(numero).append("</div>");
		t.append("<div style='font-size:11px;color:#5A6373;padding-top:5px;'>").append(rotulo).append("</div>");
		t.append("</td>");
		return (t.toString());
	}

	private static String subEncabezado(final String texto, final boolean centrado) {
		return ("<td " + (centrado ? "align='center'" : "") + " style='padding:5px "
				+ (centrado ? "6px" : "8px") + ";color:" + CorreoResumenOperacion.AZUL_CLARO + ";font-size:10px;"
				+ (centrado ? "" : ("border-right:1px solid " + CorreoResumenOperacion.AZUL_BORDE + ";")) + "'>"
				+ texto + "</td>");
	}

	private static String celdaPeor(final String tienda, final int minutos) {
		final String texto = (minutos < 0) ? "sin pendientes" : ("peor: " + tienda + ", " + minutos);
		return ("<td style='padding:8px 8px;color:" + CorreoResumenOperacion.TINTA_2 + ";font-size:11.5px;'>" + texto
				+ "</td>");
	}

	private static String muestraColor(final String color, final String texto) {
		return ("<td style='padding-right:7px;'><table cellpadding='0' cellspacing='0' border='0'><tr>"
				+ "<td width='24' height='7' bgcolor='" + color + "' style='font-size:0;line-height:0;'>&nbsp;</td>"
				+ "</tr></table></td><td style='padding-right:16px;'>" + texto + "</td>");
	}

	private static String bloqueProgramados(final FilaResumenOperacion f, final DecimalFormat miles) {
		final ArrayList<String[]> lista = f.getProgramados();
		final StringBuilder b = new StringBuilder();
		b.append("<table cellpadding='0' cellspacing='0' border='0' width='100%' style='border-collapse:collapse;")
				.append("font-family:").append(CorreoResumenOperacion.FUENTE)
				.append(";font-size:12.5px;margin-bottom:16px;border:1px solid #E0E4EC;'>");
		b.append("<tr><td colspan='4' style='background-color:").append(CorreoResumenOperacion.GRIS_SUAVE)
				.append(";padding:7px 10px;font-weight:bold;color:").append(CorreoResumenOperacion.AZUL)
				.append(";border-bottom:1px solid #E0E4EC;'>").append(f.getNombreTienda())
				.append(" &nbsp;<span style='font-weight:normal;color:#6E7784;font-size:11.5px;'>")
				.append(lista.size()).append(lista.size() == 1 ? " programado" : " programados")
				.append("</span></td></tr>");
		b.append("<tr>");
		b.append(CorreoResumenOperacion.tituloProgramado("PEDIDO", ""));
		b.append(CorreoResumenOperacion.tituloProgramado("FACTURA", ""));
		b.append(CorreoResumenOperacion.tituloProgramado("HORA", "align='center'"));
		b.append(CorreoResumenOperacion.tituloProgramado("VALOR", "align='right'"));
		b.append("</tr>");
		for (int i = 0; i < lista.size(); i++) {
			final String[] p = lista.get(i);
			final String fondo = (i % 2 == 1) ? (" style='background-color:" + CorreoResumenOperacion.FILA_ALTERNA + ";'")
					: "";
			b.append("<tr").append(fondo).append(">");
			b.append(CorreoResumenOperacion.celdaProgramado(p[0], ""));
			b.append(CorreoResumenOperacion.celdaProgramado(p[1], ""));
			b.append(CorreoResumenOperacion.celdaProgramado(p[3], "align='center' bold"));
			b.append(CorreoResumenOperacion.celdaProgramado(CorreoResumenOperacion.valorSeguro(p[2], miles),
					"align='right'"));
			b.append("</tr>");
		}
		b.append("</table>");
		return (b.toString());
	}

	/** El valor puede llegar vacio o con basura; que no tumbe el correo. */
	private static String valorSeguro(final String valor, final DecimalFormat miles) {
		try {
			return (miles.format(Double.parseDouble(valor)));
		} catch (final Exception e) {
			return ("&mdash;");
		}
	}

	private static String tituloProgramado(final String texto, final String extra) {
		return ("<td " + extra + " style='padding:5px 10px;font-size:10.5px;font-weight:bold;color:#6E7784;'>" + texto
				+ "</td>");
	}

	private static String celdaProgramado(final String texto, final String extra) {
		final boolean negrita = extra.contains("bold");
		final String alineacion = extra.replace(" bold", "");
		return ("<td " + alineacion + " style='padding:6px 10px;border-top:1px solid " + CorreoResumenOperacion.LINEA
				+ ";" + (negrita ? "font-weight:bold;" : "") + "'>" + texto + "</td>");
	}
}
