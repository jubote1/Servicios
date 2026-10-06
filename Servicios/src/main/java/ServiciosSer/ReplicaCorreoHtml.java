package ServiciosSer;

import java.util.List;

import CapaDAOSer.ReplicaDatamartDAO.Definicion;
import ModeloSer.ReplicaCelda;
import ModeloSer.ReplicaTienda;

/**
 * El correo de la replica al datamart: una matriz tienda x tabla, en verde lo que replico y en rojo
 * lo que no, para que de un vistazo se vea QUE tienda y QUE tabla fallaron.
 *
 * Todo el texto sale en ASCII y los acentos como entidades HTML: el correo anterior llegaba con
 * "informaci?n" porque dependia de como se compilara el archivo.
 */
public final class ReplicaCorreoHtml {

	private static final String VERDE = "#1B8A4B";
	private static final String ROJO = "#C21C1F";
	private static final String AZUL = "#102F6F";

	private ReplicaCorreoHtml() {
	}

	public static String construir(List<ReplicaTienda> tiendas, List<Definicion> tablas, String fechaDatos,
			int diasAtras, long milisegundos) {
		int conProblemas = 0;
		int conexionesCaidas = 0;
		for (ReplicaTienda t : tiendas) {
			if (t.tieneProblemas()) {
				conProblemas++;
			}
			if (!t.isConecto()) {
				conexionesCaidas++;
			}
		}
		boolean todoBien = conProblemas == 0;

		StringBuilder h = new StringBuilder();
		h.append("<div style=\"font-family:Arial,Helvetica,sans-serif;color:#1F2433;max-width:980px;\">");

		//Encabezado: el resumen de todo en una linea.
		h.append("<div style=\"background:").append(todoBien ? VERDE : ROJO)
				.append(";color:#fff;padding:16px 20px;border-radius:8px;\">");
		h.append("<div style=\"font-size:20px;font-weight:bold;\">");
		if (todoBien) {
			h.append("R&eacute;plica al datamart: TODO CORRECTO");
		} else {
			h.append("R&eacute;plica al datamart: ").append(conProblemas).append(" de ").append(tiendas.size())
					.append(" tienda(s) con problemas");
		}
		h.append("</div><div style=\"font-size:13px;margin-top:4px;opacity:.9;\">Datos del ").append(esc(fechaDatos))
				.append(" &middot; se revisaron los &uacute;ltimos ").append(diasAtras).append(" d&iacute;a(s) &middot; ")
				.append(milisegundos / 1000).append(" s");
		if (conexionesCaidas > 0) {
			h.append(" &middot; ").append(conexionesCaidas).append(" tienda(s) sin conexi&oacute;n");
		}
		h.append("</div></div>");

		//La matriz.
		h.append("<table cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:separate;border-spacing:3px;"
				+ "margin-top:14px;width:100%;font-size:12px;\">");
		h.append("<tr><td style=\"padding:6px 8px;font-weight:bold;color:").append(AZUL).append(";\">Tienda</td>");
		for (Definicion d : tablas) {
			h.append("<td style=\"padding:6px 4px;font-weight:bold;color:").append(AZUL)
					.append(";text-align:center;\">").append(titulo(d.tabla)).append("</td>");
		}
		h.append("<td style=\"padding:6px 8px;font-weight:bold;color:").append(AZUL)
				.append(";text-align:center;\">Estado</td></tr>");

		for (ReplicaTienda t : tiendas) {
			boolean mal = t.tieneProblemas();
			h.append("<tr>");
			h.append("<td style=\"padding:8px;border-radius:6px;font-weight:bold;background:")
					.append(mal ? "#FBD9D9" : "#DFF3E6").append(";color:").append(mal ? ROJO : "#14633A").append(";\">")
					.append(esc(t.getNombre()));
			if (!t.isConecto()) {
				h.append("<div style=\"font-size:10px;font-weight:normal;\">SIN CONEXI&Oacute;N</div>");
			}
			h.append("</td>");
			for (Definicion d : tablas) {
				celda(h, t.getCeldas().get(d.tabla));
			}
			h.append("<td style=\"padding:8px;border-radius:6px;text-align:center;font-weight:bold;color:#fff;background:")
					.append(mal ? ROJO : VERDE).append(";\">").append(mal ? "REVISAR" : "COMPLETA").append("</td>");
			h.append("</tr>");
		}

		//Totales: cuantas filas se escribieron hoy en cada tabla.
		h.append("<tr><td style=\"padding:6px 8px;color:#6E7487;\">Filas escritas</td>");
		for (Definicion d : tablas) {
			int total = 0;
			for (ReplicaTienda t : tiendas) {
				ReplicaCelda c = t.getCeldas().get(d.tabla);
				if (c != null && ReplicaCelda.OK.equals(c.getEstado())) {
					total += c.getFilas();
				}
			}
			h.append("<td style=\"padding:6px 4px;text-align:center;color:#6E7487;\">").append(total).append("</td>");
		}
		h.append("<td></td></tr></table>");

		//Leyenda.
		h.append("<div style=\"margin-top:10px;font-size:11px;color:#6E7487;\">");
		chip(h, "#DFF3E6", "#14633A", "OK (filas)");
		chip(h, "#EEF2F8", "#6E7487", "Ya estaba: no se toc&oacute;");
		chip(h, "#FFF1CC", "#8A5A00", "0 filas: revisar");
		chip(h, "#F1F3F6", "#6E7487", "No aplica / falta migraci&oacute;n");
		chip(h, "#FBD9D9", ROJO, "ERROR");
		h.append("</div>");

		//Lo que fallo, con el mensaje.
		StringBuilder fallas = new StringBuilder();
		StringBuilder avisos = new StringBuilder();
		for (ReplicaTienda t : tiendas) {
			if (!t.isConecto()) {
				fallas.append("<li><b>").append(esc(t.getNombre())).append("</b>: ").append(esc(t.getErrorConexion()))
						.append(" Se recupera sola en la pr&oacute;xima corrida mientras est&eacute; dentro de los &uacute;ltimos ")
						.append(diasAtras).append(" d&iacute;as.</li>");
				continue;
			}
			for (ReplicaCelda c : t.getCeldas().values()) {
				if (c.esError()) {
					fallas.append("<li><b>").append(esc(t.getNombre())).append(" &middot; ").append(esc(c.getTabla()))
							.append("</b>: ").append(esc(c.getDetalle()));
					if (c.getUltimoOk().length() > 0) {
						fallas.append(" (&uacute;ltimo d&iacute;a bueno: ").append(esc(c.getUltimoOk())).append(")");
					}
					fallas.append("</li>");
				} else if (ReplicaCelda.NO_APLICA.equals(c.getEstado())
						&& c.getDetalle().toLowerCase().contains("migracion")) {
					avisos.append("<li><b>").append(esc(t.getNombre())).append(" &middot; ").append(esc(c.getTabla()))
							.append("</b>: ").append(esc(c.getDetalle())).append("</li>");
				} else if (ReplicaCelda.OK.equals(c.getEstado()) && c.getDetalle().length() > 0) {
					avisos.append("<li><b>").append(esc(t.getNombre())).append(" &middot; ").append(esc(c.getTabla()))
							.append("</b>: ").append(esc(c.getDetalle())).append("</li>");
				}
			}
		}
		if (fallas.length() > 0) {
			h.append("<div style=\"margin-top:16px;padding:12px 16px;background:#FFF5F5;border:1px solid #F0B4B4;"
					+ "border-radius:8px;\"><div style=\"font-weight:bold;color:").append(ROJO)
					.append(";margin-bottom:6px;\">Qu&eacute; fall&oacute;</div><ul style=\"margin:0;padding-left:18px;"
							+ "font-size:12px;line-height:1.5;\">")
					.append(fallas).append("</ul></div>");
		}
		if (avisos.length() > 0) {
			h.append("<div style=\"margin-top:12px;padding:12px 16px;background:#F7F8FB;border:1px solid #DDE1EA;"
					+ "border-radius:8px;\"><div style=\"font-weight:bold;color:#6E7487;margin-bottom:6px;\">Avisos</div>"
					+ "<ul style=\"margin:0;padding-left:18px;font-size:12px;line-height:1.5;\">")
					.append(avisos).append("</ul></div>");
		}

		h.append("<div style=\"margin-top:14px;font-size:11px;color:#8A90A2;\">Este correo cuenta lo que se ESCRIBI&Oacute; "
				+ "en el datamart, no lo que se ley&oacute; de la tienda. Si una tienda estuvo apagada, el proceso la pone al "
				+ "d&iacute;a solo en la siguiente corrida.</div>");
		h.append("</div>");
		return h.toString();
	}

	private static void celda(StringBuilder h, ReplicaCelda c) {
		String fondo = "#F1F3F6";
		String color = "#6E7487";
		String texto = "n/a";
		String pie = "";
		if (c != null) {
			String estado = c.getEstado();
			if (ReplicaCelda.OK.equals(estado)) {
				fondo = "#DFF3E6";
				color = "#14633A";
				texto = "OK <span style=\"font-weight:normal;\">(" + c.getFilas() + ")</span>";
				if (c.getDiasRecuperados() > 0) {
					pie = "+" + c.getDiasRecuperados() + " d&iacute;a(s) recuperado(s)";
				}
			} else if (ReplicaCelda.YA_ESTABA.equals(estado)) {
				fondo = "#EEF2F8";
				color = "#6E7487";
				texto = "ya estaba";
			} else if (ReplicaCelda.CERO.equals(estado)) {
				fondo = "#FFF1CC";
				color = "#8A5A00";
				texto = "0 filas";
			} else if (ReplicaCelda.ERROR.equals(estado)) {
				fondo = "#FBD9D9";
				color = ROJO;
				texto = "ERROR";
				if (c.getUltimoOk().length() > 0) {
					pie = "&uacute;ltimo OK " + esc(c.getUltimoOk());
				}
			} else {
				texto = c.getDetalle().toLowerCase().contains("migracion") ? "falta migraci&oacute;n" : "n/a";
			}
		}
		h.append("<td style=\"padding:8px 4px;border-radius:6px;text-align:center;font-weight:bold;background:")
				.append(fondo).append(";color:").append(color).append(";\">").append(texto);
		if (pie.length() > 0) {
			h.append("<div style=\"font-size:10px;font-weight:normal;\">").append(pie).append("</div>");
		}
		h.append("</td>");
	}

	private static void chip(StringBuilder h, String fondo, String color, String texto) {
		h.append("<span style=\"display:inline-block;margin:0 6px 4px 0;padding:3px 9px;border-radius:10px;background:")
				.append(fondo).append(";color:").append(color).append(";\">").append(texto).append("</span>");
	}

	/** El nombre corto de cada tabla para el encabezado de la matriz. */
	private static String titulo(String tabla) {
		if ("pedido".equals(tabla)) {
			return "Pedidos";
		} else if ("detalle_pedido".equals(tabla)) {
			return "Detalle";
		} else if ("despacho_real".equals(tabla)) {
			return "Despachos";
		} else if ("despacho_real_det".equals(tabla)) {
			return "Despacho<br>detalle";
		} else if ("pedido_sugerencia".equals(tabla)) {
			return "Enrutam.";
		} else if ("pedido_sugerencia_det".equals(tabla)) {
			return "Enrutam.<br>detalle";
		} else if ("pedido_sugerencia_log".equals(tabla)) {
			return "Enrutam.<br>log";
		}
		return esc(tabla);
	}

	/** Escapa HTML y deja todo lo que no es ASCII como entidad numerica. */
	static String esc(String s) {
		if (s == null) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '<') {
				b.append("&lt;");
			} else if (c == '>') {
				b.append("&gt;");
			} else if (c == '&') {
				b.append("&amp;");
			} else if (c > 126) {
				b.append("&#").append((int) c).append(';');
			} else {
				b.append(c);
			}
		}
		return b.toString();
	}
}
