package utilidadesSer;

import java.util.ArrayList;

/**
 * Generador HTML ejecutivo para los reportes de personal temporal (quincenal,
 * semanal y reprocesos).
 *
 * Diseñado con el estilo institucional de Pizza Americana:
 * - Tarjetas KPI resumen (Total Liquidado, Horas Totales, Turnos, Tiendas Activas).
 * - Tabla ejecutiva consolidada por tienda con % de participación.
 * - Desglose detallado por tienda y empresa temporal con badges de tarifas.
 * - Respeto estricto de todas las columnas (Personal, Fecha, Ingreso, Salida, Horas, Valor, Obs).
 * - Badges visuales de Domingo, Festivo y Sábado.
 * - Subtotales por empresa y tienda con total de horas y total a pagar.
 * - Resaltado y alerta en caso de inconsistencias en horas de salida (00:00 o error).
 * - Diseño adaptable y estilos inline resistentes para Gmail, Outlook y móviles.
 */
public final class CorreoEmpleadoTemporal {

	private CorreoEmpleadoTemporal() {
		super();
	}

	// =======================================================================
	// Estructuras de Datos
	// =======================================================================

	/** Un día de un empleado temporal, ya calculado. */
	public static class FilaEmpleado {
		public String nombre = "";
		public String fecha = "";
		public String horaIngreso = "";
		public String horaSalida = "";
		public double horas;
		public double valor;
		public String observacion = "";
		/** -1 cuando el reporte no trae número de pedidos (el quincenal no los trae). */
		public int pedidos = -1;
		public double promedio;
		/** "Domingo", "Festivo" o "Sábado"; vacío en un día ordinario. */
		public String etiquetaDia = "";
		public boolean errorConversion;
	}

	/** Una empresa temporal dentro de una tienda, con sus tarifas y sus días. */
	public static class FilaEmpresa {
		public String nombreEmpresa = "";
		public double valorHoraNormal;
		public double valorHoraDominical;
		public double valorHoraSabado;
		public ArrayList<FilaEmpleado> dias = new ArrayList<FilaEmpleado>();
		public double total;

		public double totalHoras() {
			double h = 0;
			for (int i = 0; i < dias.size(); i++) {
				final FilaEmpleado d = dias.get(i);
				if (!d.errorConversion) {
					h += d.horas;
				}
			}
			return h;
		}

		public int totalTurnos() {
			return dias.size();
		}

		public int totalPedidos() {
			int p = 0;
			for (int i = 0; i < dias.size(); i++) {
				if (dias.get(i).pedidos > 0) {
					p += dias.get(i).pedidos;
				}
			}
			return p;
		}
	}

	/** Una tienda, con las empresas temporales que le trabajaron en el período. */
	public static class FilaTienda {
		public String nombreTienda = "";
		public ArrayList<FilaEmpresa> empresas = new ArrayList<FilaEmpresa>();

		public double total() {
			double t = 0;
			for (int i = 0; i < empresas.size(); i++) {
				t = t + empresas.get(i).total;
			}
			return t;
		}

		public double totalHoras() {
			double h = 0;
			for (int i = 0; i < empresas.size(); i++) {
				h = h + empresas.get(i).totalHoras();
			}
			return h;
		}

		public int totalTurnos() {
			int n = 0;
			for (int i = 0; i < empresas.size(); i++) {
				n = n + empresas.get(i).totalTurnos();
			}
			return n;
		}

		public String nombresEmpresas() {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < empresas.size(); i++) {
				if (empresas.get(i).totalTurnos() > 0) {
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(empresas.get(i).nombreEmpresa);
				}
			}
			return sb.length() == 0 ? "-" : sb.toString();
		}
	}

	// =======================================================================
	// Asunto del Correo
	// =======================================================================

	public static String asunto(final boolean quincenal, final String desde, final String hasta,
			final double total) {
		return (quincenal ? "REPORTE QUINCENAL PERSONAL TEMPORAL " : "REPORTE SEMANAL PERSONAL TEMPORAL ")
				+ desde + " AL " + hasta + " - " + CorreoHtml.pesos(total);
	}

	// =======================================================================
	// Cuerpo del Correo
	// =======================================================================

	public static String cuerpo(final ArrayList<FilaTienda> tiendas, final boolean quincenal,
			final boolean mostrarPedidos, final String desde, final String hasta) {

		double totalPeriodo = 0;
		double totalHorasPeriodo = 0;
		int totalTurnosPeriodo = 0;
		int totalErrores = 0;
		int tiendasConPersonal = 0;
		final ArrayList<String> tiendasSinPersonal = new ArrayList<String>();

		for (int i = 0; i < tiendas.size(); i++) {
			final FilaTienda t = tiendas.get(i);
			final int turnos = t.totalTurnos();
			if (turnos > 0) {
				tiendasConPersonal++;
				totalPeriodo += t.total();
				totalHorasPeriodo += t.totalHoras();
				totalTurnosPeriodo += turnos;
				for (int j = 0; j < t.empresas.size(); j++) {
					final FilaEmpresa e = t.empresas.get(j);
					for (int k = 0; k < e.dias.size(); k++) {
						if (e.dias.get(k).errorConversion) {
							totalErrores++;
						}
					}
				}
			} else {
				tiendasSinPersonal.add(t.nombreTienda);
			}
		}

		final StringBuilder h = new StringBuilder();

		// Contenedor principal con ancho óptimo (800px para albergar todas las columnas cómodamente)
		h.append(CorreoHtml.abrir(
				(quincenal ? "Reporte Quincenal" : "Reporte Semanal") + " de Personal Temporal",
				"Período liquidado: " + desde + " al " + hasta,
				800));

		// 1. Tarjetas KPI métricas
		h.append(tarjetasKpi(totalPeriodo, totalHorasPeriodo, totalTurnosPeriodo, tiendasConPersonal, tiendas.size()));

		// 2. Alerta de errores si los hay
		if (totalErrores > 0) {
			h.append(alertaErrores(totalErrores));
		}

		// 3. Resumen Ejecutivo Consolidado por Tienda
		h.append(resumenConsolidadoPorTienda(tiendas, totalPeriodo, totalHorasPeriodo, totalTurnosPeriodo, tiendasSinPersonal));

		// 4. Detalle Operativo por Tienda y Empresa (solo tiendas con actividad)
		h.append("<div style=\"margin:32px 0 12px;border-top:2px solid #e2e8f0;padding-top:20px;\">");
		h.append("<div style=\"font-size:17px;font-weight:bold;color:").append(CorreoHtml.AZUL)
				.append(";letter-spacing:-0.2px;\">📋 Detalle Operativo de Turnos por Tienda</div>");
		h.append("<div style=\"font-size:12px;color:").append(CorreoHtml.GRIS)
				.append(";margin-top:3px;\">Desglose completo de jornadas, horas liquidadas y observaciones por punto de venta.</div>");
		h.append("</div>");

		int tiendasRenderizadas = 0;
		for (int i = 0; i < tiendas.size(); i++) {
			final FilaTienda t = tiendas.get(i);
			if (t.totalTurnos() > 0) {
				h.append(detalleTiendaCard(t, quincenal, mostrarPedidos));
				tiendasRenderizadas++;
			}
		}

		if (tiendasRenderizadas == 0) {
			h.append("<div style=\"text-align:center;padding:30px;color:#64748b;font-style:italic;background:#f8fafc;border-radius:6px;\">")
					.append("No se registraron turnos de personal temporal en ninguna tienda para este período.</div>");
		}

		// 5. Pie de página institucional
		h.append(CorreoHtml.cerrar("Liquidación calculada según los registros de entrada y salida del personal temporal en cada tienda. "
				+ "Domingos y festivos liquidados a tarifa dominical"
				+ (quincenal ? "; sábados liquidados a tarifa de sábado." : ".")
				+ " Documento oficial para control y auditoría contable de Pizza Americana."));

		return h.toString();
	}

	// =======================================================================
	// Componentes Visuales Ejecutivos
	// =======================================================================

	private static String tarjetasKpi(final double totalPeriodo, final double totalHoras, final int totalTurnos,
			final int tiendasActivas, final int totalTiendas) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:separate;border-spacing:8px 0;margin:0 0 24px;\"><tr>");

		// Tarjeta 1: Total Periodo
		sb.append("<td width=\"36%\" style=\"background:#fffdf0;border:1.5px solid ").append(CorreoHtml.AMARILLO)
				.append(";border-radius:8px;padding:14px 16px;vertical-align:top;\">");
		sb.append("<div style=\"font-size:11px;color:#854d0e;font-weight:bold;letter-spacing:.5px;text-transform:uppercase;\">TOTAL DEL PERÍODO</div>");
		sb.append("<div style=\"font-size:24px;font-weight:bold;color:").append(CorreoHtml.AZUL)
				.append(";margin:4px 0 2px;\">").append(CorreoHtml.pesos(totalPeriodo)).append("</div>");
		sb.append("<div style=\"font-size:11px;color:").append(CorreoHtml.GRIS).append(";\">Total liquidado a pagar</div>");
		sb.append("</td>");

		// Tarjeta 2: Horas Totales
		sb.append("<td width=\"22%\" style=\"background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:14px 16px;vertical-align:top;\">");
		sb.append("<div style=\"font-size:11px;color:#475569;font-weight:bold;letter-spacing:.5px;text-transform:uppercase;\">HORAS TOTALES</div>");
		sb.append("<div style=\"font-size:20px;font-weight:bold;color:").append(CorreoHtml.AZUL)
				.append(";margin:4px 0 2px;\">").append(CorreoHtml.decimales(totalHoras)).append(" <span style=\"font-size:12px;font-weight:normal;\">hrs</span></div>");
		sb.append("<div style=\"font-size:11px;color:").append(CorreoHtml.GRIS).append(";\">Tiempo laborado</div>");
		sb.append("</td>");

		// Tarjeta 3: Turnos Registrados
		sb.append("<td width=\"21%\" style=\"background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:14px 16px;vertical-align:top;\">");
		sb.append("<div style=\"font-size:11px;color:#475569;font-weight:bold;letter-spacing:.5px;text-transform:uppercase;\">TURNOS</div>");
		sb.append("<div style=\"font-size:20px;font-weight:bold;color:").append(CorreoHtml.AZUL)
				.append(";margin:4px 0 2px;\">").append(totalTurnos).append("</div>");
		sb.append("<div style=\"font-size:11px;color:").append(CorreoHtml.GRIS).append(";\">Jornadas registradas</div>");
		sb.append("</td>");

		// Tarjeta 4: Tiendas con personal
		sb.append("<td width=\"21%\" style=\"background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:14px 16px;vertical-align:top;\">");
		sb.append("<div style=\"font-size:11px;color:#475569;font-weight:bold;letter-spacing:.5px;text-transform:uppercase;\">TIENDAS ACTIVAS</div>");
		sb.append("<div style=\"font-size:20px;font-weight:bold;color:").append(CorreoHtml.AZUL)
				.append(";margin:4px 0 2px;\">").append(tiendasActivas).append("/").append(totalTiendas).append("</div>");
		sb.append("<div style=\"font-size:11px;color:").append(CorreoHtml.GRIS).append(";\">Puntos con servicio</div>");
		sb.append("</td>");

		sb.append("</tr></table>");
		return sb.toString();
	}

	private static String alertaErrores(final int errores) {
		return "<div style=\"border-left:5px solid " + CorreoHtml.ROJO + ";background:#fef2f2;color:#991b1b;padding:12px 16px;"
				+ "font-size:13px;border-radius:0 6px 6px 0;margin-bottom:20px;line-height:1.4;\">"
				+ "⚠️ <b>Atención (" + errores + " registro(s) por verificar):</b> Existen turnos con la hora de salida en <code>00:00:00</code> o formato no interpretable. "
				+ "Se muestran resaltados en <span style=\"color:#b91c1c;font-weight:bold;\">rojo</span> en las tablas de detalle para su corrección en el punto de venta correspondiente.</div>";
	}

	private static String resumenConsolidadoPorTienda(final ArrayList<FilaTienda> tiendas, final double totalPeriodo,
			final double totalHorasPeriodo, final int totalTurnosPeriodo, final ArrayList<String> tiendasSinPersonal) {
		final StringBuilder h = new StringBuilder();

		h.append("<div style=\"font-size:16px;font-weight:bold;color:").append(CorreoHtml.AZUL)
				.append(";margin:0 0 8px;letter-spacing:-0.2px;\">📊 Resumen Consolidado por Tienda</div>");

		h.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse;font-size:13px;border:1px solid #e2e8f0;border-radius:6px;overflow:hidden;\">");
		h.append("<tr style=\"background:#102F6F;color:#ffffff;\">");
		h.append("<th style=\"padding:10px 12px;text-align:left;font-weight:bold;font-size:12px;text-transform:uppercase;\">Tienda</th>");
		h.append("<th style=\"padding:10px 12px;text-align:left;font-weight:bold;font-size:12px;text-transform:uppercase;\">Empresa(s)</th>");
		h.append("<th style=\"padding:10px 12px;text-align:center;font-weight:bold;font-size:12px;text-transform:uppercase;\">Turnos</th>");
		h.append("<th style=\"padding:10px 12px;text-align:right;font-weight:bold;font-size:12px;text-transform:uppercase;\">Horas Totales</th>");
		h.append("<th style=\"padding:10px 12px;text-align:right;font-weight:bold;font-size:12px;text-transform:uppercase;\">Total a Pagar</th>");
		h.append("<th style=\"padding:10px 12px;text-align:right;font-weight:bold;font-size:12px;text-transform:uppercase;\">% Part.</th>");
		h.append("</tr>");

		int filaIdx = 0;
		for (int i = 0; i < tiendas.size(); i++) {
			final FilaTienda t = tiendas.get(i);
			if (t.totalTurnos() > 0) {
				final String bg = (filaIdx % 2 == 0) ? "#ffffff" : "#f8fafc";
				final double pct = (totalPeriodo > 0) ? (t.total() / totalPeriodo) * 100.0 : 0.0;
				h.append("<tr style=\"background:").append(bg).append(";border-bottom:1px solid #e2e8f0;\">");
				h.append("<td style=\"padding:9px 12px;text-align:left;font-weight:600;color:").append(CorreoHtml.AZUL).append(";\">")
						.append("📍 ").append(CorreoHtml.h(t.nombreTienda)).append("</td>");
				h.append("<td style=\"padding:9px 12px;text-align:left;color:#475569;\">")
						.append(CorreoHtml.h(t.nombresEmpresas())).append("</td>");
				h.append("<td style=\"padding:9px 12px;text-align:center;font-weight:600;color:#1e293b;\">")
						.append(t.totalTurnos()).append("</td>");
				h.append("<td style=\"padding:9px 12px;text-align:right;color:#334155;font-weight:500;\">")
						.append(CorreoHtml.decimales(t.totalHoras())).append(" hrs</td>");
				h.append("<td style=\"padding:9px 12px;text-align:right;font-weight:bold;color:").append(CorreoHtml.AZUL).append(";\">")
						.append(CorreoHtml.pesos(t.total())).append("</td>");
				h.append("<td style=\"padding:9px 12px;text-align:right;color:#64748b;font-size:12px;\">")
						.append(String.format(java.util.Locale.US, "%.1f%%", pct)).append("</td>");
				h.append("</tr>");
				filaIdx++;
			}
		}

		// Fila de Total General del Resumen
		h.append("<tr style=\"background:#fff8e1;border-top:2px solid ").append(CorreoHtml.AZUL).append(";font-weight:bold;\">");
		h.append("<td style=\"padding:11px 12px;text-align:left;color:").append(CorreoHtml.AZUL).append(";font-size:13px;\">TOTAL GENERAL</td>");
		h.append("<td style=\"padding:11px 12px;text-align:left;color:").append(CorreoHtml.GRIS).append(";\">Consolidado</td>");
		h.append("<td style=\"padding:11px 12px;text-align:center;color:").append(CorreoHtml.AZUL).append(";\">").append(totalTurnosPeriodo).append("</td>");
		h.append("<td style=\"padding:11px 12px;text-align:right;color:").append(CorreoHtml.AZUL).append(";\">").append(CorreoHtml.decimales(totalHorasPeriodo)).append(" hrs</td>");
		h.append("<td style=\"padding:11px 12px;text-align:right;color:").append(CorreoHtml.AZUL).append(";font-size:15px;\">").append(CorreoHtml.pesos(totalPeriodo)).append("</td>");
		h.append("<td style=\"padding:11px 12px;text-align:right;color:").append(CorreoHtml.AZUL).append(";\">100.0%</td>");
		h.append("</tr>");
		h.append("</table>");

		if (!tiendasSinPersonal.isEmpty()) {
			h.append("<div style=\"font-size:12px;color:#64748b;margin-top:8px;font-style:italic;\">")
					.append("ℹ️ Tiendas sin personal temporal registrado en este período: ");
			for (int i = 0; i < tiendasSinPersonal.size(); i++) {
				if (i > 0) {
					h.append(", ");
				}
				h.append(CorreoHtml.h(tiendasSinPersonal.get(i)));
			}
			h.append(".</div>");
		}

		return h.toString();
	}

	private static String detalleTiendaCard(final FilaTienda tienda, final boolean quincenal,
			final boolean mostrarPedidos) {
		final StringBuilder h = new StringBuilder();

		for (int i = 0; i < tienda.empresas.size(); i++) {
			final FilaEmpresa e = tienda.empresas.get(i);
			if (e.dias.isEmpty()) {
				continue; // Omitir empresas sin turnos en esta tienda
			}

			h.append("<div style=\"margin:24px 0 16px;border:1px solid #cbd5e1;border-radius:8px;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,0.04);\">");

			// Encabezado de la Tarjeta (Tienda + Empresa + Tarifas en píldoras)
			h.append("<div style=\"background:#102F6F;padding:12px 16px;color:#ffffff;\">");
			h.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
			h.append("<td style=\"vertical-align:middle;\">");
			h.append("<div style=\"font-size:15px;font-weight:bold;color:#ffffff;\">📍 ").append(CorreoHtml.h(tienda.nombreTienda))
					.append(" &nbsp;<span style=\"color:#FDC806;\">|</span>&nbsp; 🏢 Empresa: ").append(CorreoHtml.h(e.nombreEmpresa)).append("</div>");
			h.append("</td>");
			h.append("<td style=\"text-align:right;vertical-align:middle;\">");
			h.append("<span style=\"background:rgba(255,255,255,0.12);padding:3px 8px;border-radius:4px;font-size:11px;color:#f1f5f9;margin-left:4px;\">")
					.append("Ord: <b>").append(CorreoHtml.pesos(e.valorHoraNormal)).append("</b></span>");
			if (quincenal) {
				h.append("<span style=\"background:rgba(255,255,255,0.12);padding:3px 8px;border-radius:4px;font-size:11px;color:#f1f5f9;margin-left:4px;\">")
						.append("Sáb: <b>").append(CorreoHtml.pesos(e.valorHoraSabado)).append("</b></span>");
			}
			h.append("<span style=\"background:rgba(255,255,255,0.12);padding:3px 8px;border-radius:4px;font-size:11px;color:#f1f5f9;margin-left:4px;\">")
					.append("Dom/Fest: <b>").append(CorreoHtml.pesos(e.valorHoraDominical)).append("</b></span>");
			h.append("</td>");
			h.append("</tr></table>");
			h.append("</div>");

			// Tabla Detallada de Turnos
			h.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse;font-size:12.5px;\">");
			h.append("<tr style=\"background:#eef2f6;color:#1e293b;border-bottom:1px solid #cbd5e1;\">");
			h.append("<th style=\"padding:8px 12px;text-align:left;font-weight:600;font-size:11.5px;text-transform:uppercase;\">Personal</th>");
			h.append("<th style=\"padding:8px 10px;text-align:center;font-weight:600;font-size:11.5px;text-transform:uppercase;\">Fecha</th>");
			h.append("<th style=\"padding:8px 10px;text-align:center;font-weight:600;font-size:11.5px;text-transform:uppercase;\">Ingreso</th>");
			h.append("<th style=\"padding:8px 10px;text-align:center;font-weight:600;font-size:11.5px;text-transform:uppercase;\">Salida</th>");
			h.append("<th style=\"padding:8px 12px;text-align:right;font-weight:600;font-size:11.5px;text-transform:uppercase;\">Horas</th>");
			h.append("<th style=\"padding:8px 12px;text-align:right;font-weight:600;font-size:11.5px;text-transform:uppercase;\">Valor a Pagar</th>");
			if (mostrarPedidos) {
				h.append("<th style=\"padding:8px 10px;text-align:center;font-weight:600;font-size:11.5px;text-transform:uppercase;\">Pedidos</th>");
				h.append("<th style=\"padding:8px 10px;text-align:right;font-weight:600;font-size:11.5px;text-transform:uppercase;\">Prom</th>");
			}
			h.append("<th style=\"padding:8px 12px;text-align:left;font-weight:600;font-size:11.5px;text-transform:uppercase;\">Observación</th>");
			h.append("</tr>");

			for (int j = 0; j < e.dias.size(); j++) {
				final FilaEmpleado d = e.dias.get(j);
				final String rowBg = d.errorConversion ? "#fef2f2" : ((j % 2 == 0) ? "#ffffff" : "#f8fafc");

				h.append("<tr style=\"background:").append(rowBg).append(";border-bottom:1px solid #e2e8f0;\">");

				// Columna Personal
				h.append("<td style=\"padding:8px 12px;text-align:left;font-weight:500;color:#1e293b;\">")
						.append(CorreoHtml.h(d.nombre)).append("</td>");

				// Columna Fecha con Badge si aplica
				h.append("<td style=\"padding:8px 10px;text-align:center;white-space:nowrap;color:#334155;\">")
						.append(d.fecha);
				if ("Domingo".equalsIgnoreCase(d.etiquetaDia)) {
					h.append(" <span style=\"background:#fef3c7;color:#92400e;padding:1px 5px;border-radius:3px;font-size:10px;font-weight:600;\">DOM</span>");
				} else if ("Festivo".equalsIgnoreCase(d.etiquetaDia)) {
					h.append(" <span style=\"background:#fee2e2;color:#991b1b;padding:1px 5px;border-radius:3px;font-size:10px;font-weight:600;\">FEST</span>");
				} else if ("Sabado".equalsIgnoreCase(d.etiquetaDia) || "Sábado".equalsIgnoreCase(d.etiquetaDia)) {
					h.append(" <span style=\"background:#e0f2fe;color:#0369a1;padding:1px 5px;border-radius:3px;font-size:10px;font-weight:600;\">SÁB</span>");
				}
				h.append("</td>");

				// Horas Ingreso y Salida
				h.append("<td style=\"padding:8px 10px;text-align:center;color:#475569;\">").append(d.horaIngreso).append("</td>");
				h.append("<td style=\"padding:8px 10px;text-align:center;color:#475569;\">").append(d.horaSalida).append("</td>");

				// Horas Trabajadas
				h.append("<td style=\"padding:8px 12px;text-align:right;font-weight:600;color:").append(d.errorConversion ? "#b91c1c" : "#0f172a").append(";\">")
						.append(d.errorConversion ? "Error conv." : CorreoHtml.decimales(d.horas) + " hrs").append("</td>");

				// Valor a Pagar
				h.append("<td style=\"padding:8px 12px;text-align:right;font-weight:bold;color:").append(d.errorConversion ? "#b91c1c" : CorreoHtml.AZUL).append(";\">")
						.append(d.errorConversion ? "$ 0" : CorreoHtml.pesos(d.valor)).append("</td>");

				// Pedidos y promedio (semanal)
				if (mostrarPedidos) {
					h.append("<td style=\"padding:8px 10px;text-align:center;color:#334155;\">")
							.append(d.pedidos < 0 ? "-" : String.valueOf(d.pedidos)).append("</td>");
					h.append("<td style=\"padding:8px 10px;text-align:right;color:#334155;\">")
							.append(d.errorConversion || d.horas == 0 ? "-" : CorreoHtml.decimales(d.promedio)).append("</td>");
				}

				// Observación
				final String obs = (d.observacion == null || d.observacion.trim().isEmpty()) ? "-" : d.observacion.trim();
				h.append("<td style=\"padding:8px 12px;text-align:left;color:#64748b;font-size:11.5px;\">")
						.append(CorreoHtml.h(obs)).append("</td>");

				h.append("</tr>");
			}

			// Fila Total de la Empresa en esta Tienda
			h.append("<tr style=\"background:#fff8e1;border-top:2px solid ").append(CorreoHtml.AZUL).append(";font-weight:bold;\">");
			h.append("<td style=\"padding:10px 12px;text-align:left;color:").append(CorreoHtml.AZUL).append(";\">")
					.append("TOTAL ").append(CorreoHtml.h(e.nombreEmpresa.toUpperCase())).append("</td>");
			h.append("<td style=\"padding:10px 10px;text-align:center;color:#64748b;\">").append(e.totalTurnos()).append(" turnos</td>");
			h.append("<td style=\"padding:10px 10px;text-align:center;color:#94a3b8;\">-</td>");
			h.append("<td style=\"padding:10px 10px;text-align:center;color:#94a3b8;\">-</td>");
			h.append("<td style=\"padding:10px 12px;text-align:right;color:").append(CorreoHtml.AZUL).append(";\">")
					.append(CorreoHtml.decimales(e.totalHoras())).append(" hrs</td>");
			h.append("<td style=\"padding:10px 12px;text-align:right;color:").append(CorreoHtml.AZUL).append(";font-size:14px;\">")
					.append(CorreoHtml.pesos(e.total)).append("</td>");
			if (mostrarPedidos) {
				h.append("<td style=\"padding:10px 10px;text-align:center;color:").append(CorreoHtml.AZUL).append(";\">")
						.append(e.totalPedidos()).append("</td>");
				h.append("<td style=\"padding:10px 10px;text-align:right;color:#94a3b8;\">-</td>");
			}
			h.append("<td style=\"padding:10px 12px;text-align:left;\"></td>");
			h.append("</tr>");

			h.append("</table>");
			h.append("</div>"); // Fin Card
		}

		return h.toString();
	}
}
