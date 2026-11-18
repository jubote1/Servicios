package utilidadesSer;

import java.util.ArrayList;

import ModeloSer.CasoSospechosoFidelizacion;
import ModeloSer.FidelizacionRedencion;

/**
 * Generador HTML ejecutivo para el Reporte Semanal del Plan de Fidelización de Pizza Americana.
 * 
 * Incorpora:
 * - Diseño corporativo institucional (#102F6F, #FDC806) con tarjetas KPI resumen.
 * - Módulo de Auditoría Antifraude y Casos Sospechosos de Mal Manejo de Puntos:
 *   * Detección de cuentas vinculadas a empleados de nómina/tienda.
 *   * Auto-acumulación en caja propia (el cajero acumula a su propio correo).
 *   * Más de 1 acumulación en un mismo día.
 *   * Más de 3 acumulaciones en la semana.
 *   * Redenciones de puntos asociadas en el período.
 * - Tabla detallada de redenciones de la semana con tienda y alertas de personal.
 * - Formato estricto de números, moneda y puntos sin decimales basura.
 * - HTML con estilos inline optimizados para Gmail, Outlook y dispositivos móviles.
 */
public final class CorreoPlanFidelizacion {

	private CorreoPlanFidelizacion() {
	}

	public static String armarReporte(
			final String fechaAnterior,
			final String fechaActual,
			final int totalPersonasPlan,
			final int totalPersonaSemana,
			final double totalPuntosAcumulados,
			final double totalPuntosRedimidos,
			final ArrayList<CasoSospechosoFidelizacion> casosSospechosos,
			final ArrayList<FidelizacionRedencion> redenciones) {

		final StringBuilder sb = new StringBuilder();

		// Apertura institucional (ancho 800px para auditoría de columnas)
		sb.append(CorreoHtml.abrir("PLAN DE FIDELIZACIÓN", "Informe Semanal Consolidado & Auditoría de Seguridad", 800));

		// 1. Banner de Período Evaluado
		sb.append("<div style=\"background:#f8fafc;border:1px solid #e2e8f0;border-left:4px solid ").append(CorreoHtml.AZUL)
				.append(";border-radius:6px;padding:12px 16px;margin-bottom:20px;\">");
		sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"font-size:13px;color:#334155;\">");
		sb.append("<tr>");
		sb.append("<td>");
		sb.append("<span style=\"font-weight:bold;color:").append(CorreoHtml.AZUL).append(";\">📅 Semana Evaluada: </span>");
		sb.append("<strong>").append(CorreoHtml.h(fechaAnterior)).append("</strong> al <strong>")
				.append(CorreoHtml.h(fechaActual)).append("</strong>");
		sb.append("</td>");
		sb.append("<td align=\"right\" style=\"font-size:12px;color:#64748b;\">");
		sb.append("Reporte semanal automático • Dirección General & Mercadeo");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</div>");

		// 2. Tarjetas KPI Métricas Clave (4 Cards)
		sb.append("<div style=\"margin-bottom:24px;\">");
		sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:separate;border-spacing:8px 0;\">");
		sb.append("<tr>");

		// KPI 1: Total Clientes en Programa
		sb.append("<td width=\"25%\" style=\"background:#ffffff;border:1px solid #e2e8f0;border-top:3px solid ")
				.append(CorreoHtml.AZUL).append(";border-radius:8px;padding:14px 12px;text-align:center;box-shadow:0 1px 3px rgba(0,0,0,0.05);\">");
		sb.append("<div style=\"font-size:11px;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:0.5px;\">Total en Programa</div>");
		sb.append("<div style=\"font-size:22px;font-weight:800;color:").append(CorreoHtml.AZUL).append(";margin:6px 0 2px 0;\">")
				.append(CorreoHtml.entero(totalPersonasPlan)).append("</div>");
		sb.append("<div style=\"font-size:11px;color:#94a3b8;\">Clientes registrados</div>");
		sb.append("</td>");

		// KPI 2: Clientes Vinculados Última Semana
		sb.append("<td width=\"25%\" style=\"background:#ffffff;border:1px solid #e2e8f0;border-top:3px solid #16a34a;border-radius:8px;padding:14px 12px;text-align:center;box-shadow:0 1px 3px rgba(0,0,0,0.05);\">");
		sb.append("<div style=\"font-size:11px;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:0.5px;\">Nuevas Afiliaciones</div>");
		sb.append("<div style=\"font-size:22px;font-weight:800;color:#16a34a;margin:6px 0 2px 0;\">+")
				.append(CorreoHtml.entero(totalPersonaSemana)).append("</div>");
		sb.append("<div style=\"font-size:11px;color:#94a3b8;\">Vinculados en la semana</div>");
		sb.append("</td>");

		// KPI 3: Puntos Acumulados Semana
		sb.append("<td width=\"25%\" style=\"background:#ffffff;border:1px solid #e2e8f0;border-top:3px solid ")
				.append(CorreoHtml.AZUL).append(";border-radius:8px;padding:14px 12px;text-align:center;box-shadow:0 1px 3px rgba(0,0,0,0.05);\">");
		sb.append("<div style=\"font-size:11px;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:0.5px;\">Puntos Acumulados</div>");
		sb.append("<div style=\"font-size:22px;font-weight:800;color:").append(CorreoHtml.AZUL).append(";margin:6px 0 2px 0;\">")
				.append(CorreoHtml.decimales(totalPuntosAcumulados)).append("</div>");
		sb.append("<div style=\"font-size:11px;color:#94a3b8;\">Puntos otorgados</div>");
		sb.append("</td>");

		// KPI 4: Puntos Redimidos Semana
		sb.append("<td width=\"25%\" style=\"background:#ffffff;border:1px solid #e2e8f0;border-top:3px solid #d97706;border-radius:8px;padding:14px 12px;text-align:center;box-shadow:0 1px 3px rgba(0,0,0,0.05);\">");
		sb.append("<div style=\"font-size:11px;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:0.5px;\">Puntos Redimidos</div>");
		sb.append("<div style=\"font-size:22px;font-weight:800;color:#d97706;margin:6px 0 2px 0;\">")
				.append(CorreoHtml.decimales(totalPuntosRedimidos)).append("</div>");
		sb.append("<div style=\"font-size:11px;color:#94a3b8;\">").append(redenciones != null ? redenciones.size() : 0).append(" canjes en semana</div>");
		sb.append("</td>");

		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</div>");

		// 3. Módulo de Auditoría: Casos Sospechosos de Mal Manejo de Puntos
		sb.append(armarSeccionCasosSospechosos(casosSospechosos));

		// 4. Módulo de Redenciones de la Semana
		sb.append(armarSeccionRedenciones(redenciones, totalPuntosRedimidos));

		// Cierre institucional
		sb.append(CorreoHtml.cerrar("Pizza Americana • Sistema Integral de Fidelización y Control POS • Informe Confidencial de Auditoría y Gestión"));

		return sb.toString();
	}

	/**
	 * Construye la sección visual de auditoría con los casos sospechosos detectados.
	 */
	private static String armarSeccionCasosSospechosos(final ArrayList<CasoSospechosoFidelizacion> casos) {
		final StringBuilder h = new StringBuilder();

		final int cantidadCasos = casos != null ? casos.size() : 0;

		h.append("<div style=\"margin-bottom:28px;background:#ffffff;border:1px solid #fecaca;border-radius:10px;box-shadow:0 2px 6px rgba(220,38,38,0.08);overflow:hidden;\">");

		// Encabezado del contenedor de auditoría
		h.append("<div style=\"background:#fff1f2;padding:14px 18px;border-bottom:1px solid #fecaca;\">");
		h.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">");
		h.append("<tr>");
		h.append("<td>");
		h.append("<div style=\"font-size:15px;font-weight:bold;color:#991b1b;display:inline-block;\">");
		h.append("🚨 AUDITORÍA DE SEGURIDAD: TOP ").append(Math.min(cantidadCasos, 5))
				.append(" CASOS SOSPECHOSOS DE MAL MANEJO DE PUNTOS");
		h.append("</div>");
		h.append("<div style=\"font-size:12px;color:#7f1d1d;margin-top:4px;\">");
		h.append("Criterios evaluados: cuentas asociadas a personal de nómina, auto-acumulación en caja propia, reincidencia diaria (&gt;1 pedido/día) y frecuencia semanal excesiva (&gt;3 pedidos/sem).");
		h.append("</div>");
		h.append("</td>");
		h.append("<td align=\"right\" width=\"120\">");
		h.append("<span style=\"background:#dc2626;color:#ffffff;font-size:11px;font-weight:bold;padding:5px 10px;border-radius:20px;\">");
		h.append(cantidadCasos).append(" Detectado").append(cantidadCasos != 1 ? "s" : "");
		h.append("</span>");
		h.append("</td>");
		h.append("</tr>");
		h.append("</table>");
		h.append("</div>");

		if (casos == null || casos.isEmpty()) {
			h.append("<div style=\"padding:20px;text-align:center;color:#16a34a;font-size:13px;font-weight:600;\">");
			h.append("✅ No se detectaron casos sospechosos ni patrones anómalos de acumulación durante esta semana.");
			h.append("</div>");
			h.append("</div>");
			return h.toString();
		}

		// Tabla de Casos Sospechosos
		h.append("<div style=\"padding:12px 14px;overflow-x:auto;\">");
		h.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"font-size:12px;border-collapse:collapse;\">");
		h.append("<thead>");
		h.append("<tr style=\"background:#f8fafc;border-bottom:2px solid #e2e8f0;color:#475569;font-size:11px;text-transform:uppercase;\">");
		h.append("<th style=\"padding:9px 10px;text-align:center;width:65px;\">Riesgo</th>");
		h.append("<th style=\"padding:9px 10px;text-align:left;min-width:180px;\">Cuenta / Empleado</th>");
		h.append("<th style=\"padding:9px 10px;text-align:left;min-width:180px;\">Patrones Detectados</th>");
		h.append("<th style=\"padding:9px 10px;text-align:right;width:110px;\">Actividad Sem.</th>");
		h.append("<th style=\"padding:9px 10px;text-align:left;min-width:150px;\">Tiendas &amp; Cajeros</th>");
		h.append("</tr>");
		h.append("</thead>");
		h.append("<tbody>");

		int index = 1;
		for (final CasoSospechosoFidelizacion c : casos) {
			final String fondoFila = (index % 2 == 0) ? "#ffffff" : "#fffbfb";
			h.append("<tr style=\"background:").append(fondoFila).append(";border-bottom:1px solid #f1f5f9;\">");

			// Columna 1: Nivel de Riesgo
			h.append("<td style=\"padding:10px 8px;text-align:center;vertical-align:top;\">");
			if ("CRÍTICO".equalsIgnoreCase(c.getNivelRiesgo())) {
				h.append("<span style=\"background:#fee2e2;color:#991b1b;border:1px solid #f87171;font-weight:bold;font-size:10px;padding:3px 7px;border-radius:12px;display:inline-block;\">");
				h.append("CRÍTICO</span>");
			} else if ("ALTO".equalsIgnoreCase(c.getNivelRiesgo())) {
				h.append("<span style=\"background:#fef3c7;color:#92400e;border:1px solid #f59e0b;font-weight:bold;font-size:10px;padding:3px 7px;border-radius:12px;display:inline-block;\">");
				h.append("ALTO</span>");
			} else {
				h.append("<span style=\"background:#f1f5f9;color:#475569;border:1px solid #cbd5e1;font-weight:bold;font-size:10px;padding:3px 7px;border-radius:12px;display:inline-block;\">");
				h.append("MEDIO</span>");
			}
			h.append("<div style=\"font-size:10px;color:#94a3b8;margin-top:4px;\">#").append(index).append("</div>");
			h.append("</td>");

			// Columna 2: Cuenta / Titular
			h.append("<td style=\"padding:10px 10px;vertical-align:top;\">");
			h.append("<div style=\"font-weight:bold;color:#0f172a;font-size:12.5px;\">").append(CorreoHtml.h(c.getCorreo())).append("</div>");
			if (c.isEsEmpleado()) {
				h.append("<div style=\"margin-top:4px;display:inline-block;background:#fef2f2;border:1px solid #fca5a5;border-radius:4px;padding:2px 6px;color:#991b1b;font-size:11px;font-weight:bold;\">");
				h.append("👤 Empleado: ").append(CorreoHtml.h(c.getEmpleadoNombre()));
				if (c.getEmpleadoUsuario() != null && !c.getEmpleadoUsuario().isEmpty()) {
					h.append(" <span style=\"color:#7f1d1d;font-weight:normal;\">(").append(CorreoHtml.h(c.getEmpleadoUsuario())).append(")</span>");
				}
				h.append("</div>");
			} else {
				h.append("<div style=\"margin-top:4px;color:#64748b;font-size:11px;\">Cliente Externo (no figura en nómina)</div>");
			}
			h.append("</td>");

			// Columna 3: Patrones / Badges de Infracción
			h.append("<td style=\"padding:10px 10px;vertical-align:top;\">");
			h.append("<div style=\"line-height:1.7;\">");
			if (c.isEsEmpleado()) {
				h.append("<span style=\"background:#fee2e2;color:#b91c1c;border-radius:4px;padding:2px 6px;font-size:10.5px;font-weight:bold;margin-right:4px;display:inline-block;\">")
						.append("⚠️ Empleado Nómina</span> ");
			}
			if (c.getAutoCajeroCount() > 0) {
				h.append("<span style=\"background:#7f1d1d;color:#ffffff;border-radius:4px;padding:2px 6px;font-size:10.5px;font-weight:bold;margin-right:4px;display:inline-block;\">")
						.append("🚨 Auto-Cajero: ").append(c.getAutoCajeroCount()).append(" ped.</span> ");
			}
			if (c.getMaxDia() > 1) {
				h.append("<span style=\"background:#ffedd5;color:#c2410c;border-radius:4px;padding:2px 6px;font-size:10.5px;font-weight:bold;margin-right:4px;display:inline-block;\">")
						.append("⚠️ ").append(c.getMaxDia()).append(" pedidos en 1 día</span> ");
			}
			if (c.getTotalAcumulaciones() > 3) {
				h.append("<span style=\"background:#fef3c7;color:#b45309;border-radius:4px;padding:2px 6px;font-size:10.5px;font-weight:bold;margin-right:4px;display:inline-block;\">")
						.append("⚠️ ").append(c.getTotalAcumulaciones()).append(" acum / sem</span> ");
			}
			if (c.getPuntosRedimidosSemana() > 0) {
				h.append("<span style=\"background:#f3e8ff;color:#7e22ce;border-radius:4px;padding:2px 6px;font-size:10.5px;font-weight:bold;margin-right:4px;display:inline-block;\">")
						.append("🎁 Redimió ").append(CorreoHtml.decimales(c.getPuntosRedimidosSemana())).append(" pts</span> ");
			}
			h.append("</div>");
			h.append("</td>");

			// Columna 4: Actividad Semana
			h.append("<td style=\"padding:10px 10px;text-align:right;vertical-align:top;\">");
			h.append("<div style=\"font-size:13px;font-weight:bold;color:").append(CorreoHtml.AZUL).append(";\">")
					.append(CorreoHtml.decimales(c.getTotalPuntos())).append(" pts</div>");
			h.append("<div style=\"font-size:11px;color:#475569;margin-top:2px;\">")
					.append(c.getTotalAcumulaciones()).append(" pedidos (").append(c.getDiasActivos()).append(" d)</div>");
			h.append("<div style=\"font-size:10.5px;color:#94a3b8;\">Venta: ").append(CorreoHtml.pesos(c.getTotalVenta())).append("</div>");
			h.append("</td>");

			// Columna 5: Tiendas & Cajeros
			h.append("<td style=\"padding:10px 10px;vertical-align:top;font-size:11px;color:#334155;\">");
			h.append("<div><strong>Tiendas: </strong>").append(CorreoHtml.h(c.getTiendas())).append("</div>");
			h.append("<div style=\"margin-top:3px;color:#64748b;\"><strong>Cajeros: </strong>")
					.append(CorreoHtml.h(c.getCajeros())).append("</div>");
			h.append("</td>");

			h.append("</tr>");
			index++;
		}

		h.append("</tbody>");
		h.append("</table>");
		h.append("</div>"); // Fin tabla

		// Aviso de acción recomendada
		h.append("<div style=\"background:#fafafa;padding:10px 16px;border-top:1px solid #f1f5f9;font-size:11.5px;color:#64748b;\">");
		h.append("💡 <strong>Recomendación Operativa:</strong> Verificar con auditoría de tienda los números de pedido involucrados y aplicar la política de fidelización que prohíbe a colaboradores acumular puntos de consumos de terceros o en sus propios turnos de caja.");
		h.append("</div>");

		h.append("</div>"); // Fin card auditoria

		return h.toString();
	}

	/**
	 * Construye la sección detallada de redenciones de la semana.
	 */
	private static String armarSeccionRedenciones(final ArrayList<FidelizacionRedencion> redenciones, final double totalPuntosRedimidos) {
		final StringBuilder h = new StringBuilder();

		final int totalRedenciones = redenciones != null ? redenciones.size() : 0;

		h.append("<div style=\"margin-bottom:20px;background:#ffffff;border:1px solid #e2e8f0;border-radius:10px;box-shadow:0 1px 4px rgba(0,0,0,0.05);overflow:hidden;\">");

		// Encabezado sección
		h.append("<div style=\"background:#f8fafc;padding:14px 18px;border-bottom:1px solid #e2e8f0;\">");
		h.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">");
		h.append("<tr>");
		h.append("<td>");
		h.append("<div style=\"font-size:15px;font-weight:bold;color:").append(CorreoHtml.AZUL).append(";\">")
				.append("🎁 REDENCIONES EFECTUADAS EN LA SEMANA</div>");
		h.append("<div style=\"font-size:12px;color:#64748b;margin-top:3px;\">")
				.append("Detalle de canjes de puntos procesados en tiendas y contact center.</div>");
		h.append("</td>");
		h.append("<td align=\"right\">");
		h.append("<span style=\"background:#e0f2fe;color:#0369a1;font-size:11px;font-weight:bold;padding:5px 10px;border-radius:20px;\">");
		h.append(totalRedenciones).append(" Canje").append(totalRedenciones != 1 ? "s" : "");
		h.append("</span>");
		h.append("</td>");
		h.append("</tr>");
		h.append("</table>");
		h.append("</div>");

		if (redenciones == null || redenciones.isEmpty()) {
			h.append("<div style=\"padding:20px;text-align:center;color:#64748b;font-size:13px;\">");
			h.append("No se registraron redenciones de puntos durante este período.");
			h.append("</div>");
			h.append("</div>");
			return h.toString();
		}

		// Tabla detallada
		h.append("<div style=\"padding:10px 14px;overflow-x:auto;\">");
		h.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"font-size:12px;border-collapse:collapse;\">");
		h.append("<thead>");
		h.append("<tr style=\"background:#f1f5f9;border-bottom:2px solid #cbd5e1;color:#334155;font-size:11px;text-transform:uppercase;\">");
		h.append("<th style=\"padding:8px 10px;text-align:left;min-width:180px;\">Cliente</th>");
		h.append("<th style=\"padding:8px 10px;text-align:left;width:120px;\">Tienda</th>");
		h.append("<th style=\"padding:8px 10px;text-align:center;width:130px;\">Fecha y Hora</th>");
		h.append("<th style=\"padding:8px 10px;text-align:right;width:110px;\">Puntos Redimidos</th>");
		h.append("<th style=\"padding:8px 10px;text-align:center;width:110px;\">Auditoría</th>");
		h.append("</tr>");
		h.append("</thead>");
		h.append("<tbody>");

		int i = 0;
		for (final FidelizacionRedencion r : redenciones) {
			final String bg = (i % 2 == 0) ? "#ffffff" : "#f8fafc";
			h.append("<tr style=\"background:").append(bg).append(";border-bottom:1px solid #f1f5f9;\">");

			// Cliente
			h.append("<td style=\"padding:8px 10px;text-align:left;\">");
			h.append("<div style=\"font-weight:600;color:#1e293b;\">").append(CorreoHtml.h(r.getCorreo())).append("</div>");
			if (r.isEsEmpleado()) {
				h.append("<div style=\"color:#b91c1c;font-size:10.5px;font-weight:bold;\">👤 ")
						.append(CorreoHtml.h(r.getEmpleadoNombre())).append("</div>");
			}
			h.append("</td>");

			// Tienda
			final String tiendaTxt = (r.getTienda() != null && !r.getTienda().isEmpty()) ? r.getTienda() : "-";
			h.append("<td style=\"padding:8px 10px;text-align:left;color:#475569;\">").append(CorreoHtml.h(tiendaTxt)).append("</td>");

			// Fecha
			h.append("<td style=\"padding:8px 10px;text-align:center;color:#64748b;font-size:11px;\">")
					.append(CorreoHtml.h(r.getFechaRedencion())).append("</td>");

			// Puntos Redimidos
			h.append("<td style=\"padding:8px 10px;text-align:right;font-weight:bold;color:#d97706;font-size:13px;\">")
					.append(CorreoHtml.decimales(r.getPuntosRedimidos())).append("</td>");

			// Alerta Auditoría
			h.append("<td style=\"padding:8px 10px;text-align:center;\">");
			if (r.isEsEmpleado()) {
				h.append("<span style=\"background:#fee2e2;color:#991b1b;font-size:10px;font-weight:bold;padding:2px 6px;border-radius:10px;\">⚠️ Empleado</span>");
			} else {
				h.append("<span style=\"color:#94a3b8;font-size:11px;\">OK</span>");
			}
			h.append("</td>");

			h.append("</tr>");
			i++;
		}

		// Fila de Total
		h.append("<tr style=\"background:#fff8e1;border-top:2px solid ").append(CorreoHtml.AZUL).append(";font-weight:bold;\">");
		h.append("<td style=\"padding:10px 10px;color:").append(CorreoHtml.AZUL).append(";\">TOTAL GENERAL REDENCIONES</td>");
		h.append("<td style=\"padding:10px 10px;color:#64748b;\">").append(totalRedenciones).append(" canjes</td>");
		h.append("<td style=\"padding:10px 10px;text-align:center;color:#94a3b8;\">-</td>");
		h.append("<td style=\"padding:10px 10px;text-align:right;color:#d97706;font-size:14px;\">")
				.append(CorreoHtml.decimales(totalPuntosRedimidos)).append(" pts</td>");
		h.append("<td style=\"padding:10px 10px;text-align:center;\"></td>");
		h.append("</tr>");

		h.append("</tbody>");
		h.append("</table>");
		h.append("</div>");

		h.append("</div>"); // Fin card redenciones

		return h.toString();
	}
}
