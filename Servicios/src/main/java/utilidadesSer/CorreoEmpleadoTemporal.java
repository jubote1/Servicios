package utilidadesSer;

import java.util.ArrayList;

/**
 * El HTML de los reportes de personal temporal: el semanal y el quincenal, y
 * sus reprocesos (4 reportes, un solo cuerpo de correo).
 *
 * Usa los ladrillos de CorreoHtml -es el estilo de la casa- y le agrega lo
 * propio de este reporte: la tarjeta con el total del periodo, el resumen por
 * tienda y el aviso cuando una hora de salida no se pudo interpretar. Ese caso
 * ya se avisaba aparte por correo a ERRORREPLICAINV (eso no cambia); aqui
 * ademas queda visible, en rojo, dentro del reporte.
 *
 * Antes de esto cada uno de los 4 reportes armaba su propio HTML a mano,
 * repetido casi letra por letra. Quedo un solo lugar para el calculo de
 * presentacion (formato de horas, de pesos, la fila de "Domingo/Festivo") y
 * cada reporte solo entrega los datos ya calculados (horas, valor, si es
 * domingo/festivo/sabado).
 */
public final class CorreoEmpleadoTemporal {

	private CorreoEmpleadoTemporal() {
		super();
	}

	// =======================================================================
	// Lo que recibe
	// =======================================================================

	/** Un dia de un empleado temporal, ya calculado. */
	public static class FilaEmpleado {
		public String nombre = "";
		public String fecha = "";
		public String horaIngreso = "";
		public String horaSalida = "";
		public double horas;
		public double valor;
		public String observacion = "";
		/** -1 cuando el reporte no trae numero de pedidos (el quincenal no los trae). */
		public int pedidos = -1;
		public double promedio;
		/** "Domingo", "Festivo" o "Sabado"; vacio en un dia normal. */
		public String etiquetaDia = "";
		public boolean errorConversion;
	}

	/** Una empresa temporal dentro de una tienda, con sus tarifas y sus dias. */
	public static class FilaEmpresa {
		public String nombreEmpresa = "";
		public double valorHoraNormal;
		public double valorHoraDominical;
		public double valorHoraSabado;
		public ArrayList<FilaEmpleado> dias = new ArrayList<FilaEmpleado>();
		public double total;
	}

	/** Una tienda, con las empresas temporales que le trabajaron en el periodo. */
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
	}

	// =======================================================================
	// El asunto y el cuerpo
	// =======================================================================

	/** El asunto, con el total del periodo para verlo sin abrir el correo. */
	public static String asunto(final boolean quincenal, final String desde, final String hasta,
			final double total) {
		return (quincenal ? "Reporte quincenal de personal temporal " : "Reporte semanal de personal temporal ")
				+ desde + " al " + hasta + " - " + CorreoHtml.pesos(total);
	}

	public static String cuerpo(final ArrayList<FilaTienda> tiendas, final boolean quincenal,
			final boolean mostrarPedidos, final String desde, final String hasta) {
		double total = 0;
		int errores = 0;
		for (int i = 0; i < tiendas.size(); i++) {
			total = total + tiendas.get(i).total();
			for (int j = 0; j < tiendas.get(i).empresas.size(); j++) {
				final FilaEmpresa e = tiendas.get(i).empresas.get(j);
				for (int k = 0; k < e.dias.size(); k++) {
					if (e.dias.get(k).errorConversion) {
						errores++;
					}
				}
			}
		}

		final StringBuilder h = new StringBuilder();
		h.append(CorreoHtml.abrir((quincenal ? "Reporte quincenal" : "Reporte semanal") + " de personal temporal",
				"Del " + desde + " al " + hasta));

		h.append(tarjetaTotal(total, tiendas.size()));

		if (errores > 0) {
			h.append(CorreoHtml.aviso(errores + " registro(s) con la hora de salida sin interpretar: quedan en"
					+ " cero y estan marcados abajo en rojo. Ya se aviso aparte por correo para corregirlos."));
		}

		h.append(resumenPorTienda(tiendas));

		for (int i = 0; i < tiendas.size(); i++) {
			h.append(detalleTienda(tiendas.get(i), quincenal, mostrarPedidos));
		}

		h.append(CorreoHtml.cerrar("Domingos y festivos se pagan a la tarifa dominical de cada empresa."
				+ (quincenal ? " Los sabados, a su tarifa de sabado." : "")));
		return h.toString();
	}

	// =======================================================================
	// Ladrillos propios de este reporte
	// =======================================================================

	private static String tarjetaTotal(final double total, final int tiendas) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
		sb.append("<td style=\"background:#fff8e1;border-left:6px solid ").append(CorreoHtml.AMARILLO)
				.append(";padding:16px 18px;\">");
		sb.append("<div style=\"font-size:12px;color:").append(CorreoHtml.GRIS)
				.append(";font-weight:bold;letter-spacing:.4px;\">TOTAL DEL PERIODO</div>");
		sb.append("<div style=\"font-size:32px;font-weight:bold;color:").append(CorreoHtml.AZUL)
				.append(";margin-top:2px;\">").append(CorreoHtml.pesos(total)).append("</div>");
		sb.append("<div style=\"font-size:12px;color:").append(CorreoHtml.GRIS).append(";margin-top:4px;\">")
				.append(tiendas).append(" tienda(s) con personal temporal en el periodo</div>");
		sb.append("</td></tr></table><div style=\"height:20px;line-height:20px;\">&nbsp;</div>");
		return sb.toString();
	}

	private static String resumenPorTienda(final ArrayList<FilaTienda> tiendas) {
		final StringBuilder h = new StringBuilder();
		h.append(CorreoHtml.abrirTabla("Resumen por tienda", "Tienda", "Total"));
		double total = 0;
		for (int i = 0; i < tiendas.size(); i++) {
			final FilaTienda t = tiendas.get(i);
			h.append(CorreoHtml.fila(t.nombreTienda, CorreoHtml.pesos(t.total())));
			total = total + t.total();
		}
		h.append(CorreoHtml.filaTotal("TOTAL GENERAL", CorreoHtml.pesos(total)));
		h.append(CorreoHtml.cerrarTabla());
		return h.toString();
	}

	private static String detalleTienda(final FilaTienda tienda, final boolean quincenal,
			final boolean mostrarPedidos) {
		final StringBuilder h = new StringBuilder();
		for (int i = 0; i < tienda.empresas.size(); i++) {
			final FilaEmpresa e = tienda.empresas.get(i);
			final String tarifas = "hora normal " + CorreoHtml.pesos(e.valorHoraNormal) + " - dominical/festivo "
					+ CorreoHtml.pesos(e.valorHoraDominical)
					+ (quincenal ? " - sabado " + CorreoHtml.pesos(e.valorHoraSabado) : "");

			final ArrayList<String> encabezados = new ArrayList<String>();
			encabezados.add("Personal");
			encabezados.add("Fecha");
			encabezados.add("Ingreso");
			encabezados.add("Salida");
			encabezados.add("Horas");
			encabezados.add("Valor");
			if (mostrarPedidos) {
				encabezados.add("Pedidos");
				encabezados.add("Promedio");
			}
			encabezados.add("Observacion");
			h.append(CorreoHtml.abrirTabla(tienda.nombreTienda + " - " + e.nombreEmpresa + " (" + tarifas + ")",
					encabezados.toArray(new String[0])));

			for (int j = 0; j < e.dias.size(); j++) {
				final FilaEmpleado d = e.dias.get(j);
				final ArrayList<String> celdas = new ArrayList<String>();
				celdas.add(d.nombre);
				celdas.add(d.fecha + (d.etiquetaDia.isEmpty() ? "" : " (" + d.etiquetaDia + ")"));
				celdas.add(d.horaIngreso);
				celdas.add(d.horaSalida);
				celdas.add(d.errorConversion ? "sin calcular" : CorreoHtml.decimales(d.horas));
				celdas.add(d.errorConversion ? "$0" : CorreoHtml.pesos(d.valor));
				if (mostrarPedidos) {
					celdas.add(d.pedidos < 0 ? "" : String.valueOf(d.pedidos));
					celdas.add(d.errorConversion || d.horas == 0 ? "-" : CorreoHtml.decimales(d.promedio));
				}
				celdas.add(d.observacion == null ? "" : d.observacion);
				if (d.errorConversion) {
					h.append(CorreoHtml.filaAviso(celdas.toArray(new String[0])));
				} else {
					h.append(CorreoHtml.fila(celdas.toArray(new String[0])));
				}
			}

			final ArrayList<String> totalFila = new ArrayList<String>();
			totalFila.add("TOTAL " + e.nombreEmpresa.toUpperCase());
			totalFila.add("");
			totalFila.add("");
			totalFila.add("");
			totalFila.add("");
			totalFila.add(CorreoHtml.pesos(e.total));
			if (mostrarPedidos) {
				totalFila.add("");
				totalFila.add("");
			}
			totalFila.add("");
			h.append(CorreoHtml.filaTotal(totalFila.toArray(new String[0])));
			h.append(CorreoHtml.cerrarTabla());
		}
		return h.toString();
	}
}
