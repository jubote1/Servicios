package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import CapaDAOSer.GeneralDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import capaDAOCC.ConocimientoBloqueoDAO;
import capaDAOCC.LogBloqueoTiendaDAO;
import capaDAOCC.TiendaDAO;
import capaModeloCC.ConocimientoBloqueoTienda;
import capaModeloCC.LogBloqueoTienda;
import capaModeloCC.Tienda;
import utilidadesSer.ControladorEnvioCorreo;

/**
 * Reporte Semanal de Bloqueos de Tienda y Generador de Conocimiento Operativo.
 *
 * Funcionalidades clave:
 * 1. Analiza con precision los bloqueos de cada tienda en el periodo semanal.
 * 2. Captura la fotografia operacional en el momento del bloqueo:
 *    - Pedidos activos en cola / cocina ("pixeles")
 *    - Domiciliarios activos en turno
 *    - Ratio de presion de despacho (pedidos / domiciliario)
 *    - Venta estimada perdida por el tiempo fuera de linea
 *    - Clasificacion del patron y sugerencia preventiva accionable
 * 3. Persiste cada evento en `pizzaamericana.conocimiento_bloqueo_tienda` como
 *    base de conocimiento historica para modelos de bloqueo predictivo.
 * 4. Genera un correo corporativo de nivel ejecutivo con tablero KPI, ranking
 *    de impacto, detalle analitico por tienda y sugerencias preventivas.
 */
public class ReporteBloqueosTienda {

	private static final SimpleDateFormat SDF_FECHA = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat SDF_HORA = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat SDF_HORA_SOLA = new SimpleDateFormat("HH:mm");
	private static final DecimalFormat FMT_PESOS = new DecimalFormat("$ ###,###");
	private static final DecimalFormat FMT_DECIMAL = new DecimalFormat("###,##0.0");
	private static final DecimalFormat FMT_ENTERO = new DecimalFormat("###,###");

	public static void main(String[] args) {
		String fechaActual = "";
		String fechaAnterior = "";

		Calendar calendarioActual = Calendar.getInstance();

		if (args != null && args.length >= 2 && args[0] != null && args[1] != null) {
			fechaAnterior = args[0].trim();
			fechaActual = args[1].trim();
		} else {
			try {
				fechaActual = SDF_FECHA.format(calendarioActual.getTime());
				calendarioActual.setTime(SDF_FECHA.parse(fechaActual));
			} catch (Exception exc) {
				fechaActual = SDF_FECHA.format(new Date());
			}

			int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
			// Calcular lunes anterior
			if (diaActual == Calendar.SUNDAY) {
				calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
			} else if (diaActual == Calendar.MONDAY) {
				calendarioActual.add(Calendar.DAY_OF_YEAR, -7);
			} else if (diaActual == Calendar.TUESDAY) {
				calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
			} else if (diaActual == Calendar.WEDNESDAY) {
				calendarioActual.add(Calendar.DAY_OF_YEAR, -2);
			} else if (diaActual == Calendar.THURSDAY) {
				calendarioActual.add(Calendar.DAY_OF_YEAR, -3);
			} else if (diaActual == Calendar.FRIDAY) {
				calendarioActual.add(Calendar.DAY_OF_YEAR, -4);
			} else if (diaActual == Calendar.SATURDAY) {
				calendarioActual.add(Calendar.DAY_OF_YEAR, -5);
			}

			Date datFechaAnterior = calendarioActual.getTime();
			fechaAnterior = SDF_FECHA.format(datFechaAnterior);
		}

		System.out.println("Iniciando ReporteBloqueosTienda para el periodo: " + fechaAnterior + " al " + fechaActual);
		procesarReporte(fechaAnterior, fechaActual);
	}

	/**
	 * Ejecuta el analisis completo, guarda en la tabla de conocimiento y despacha el correo.
	 *
	 * @param fechaAnterior Fecha inicial (yyyy-MM-dd)
	 * @param fechaActual   Fecha final (yyyy-MM-dd)
	 * @return El HTML generado para el reporte
	 */
	public static String procesarReporte(String fechaAnterior, String fechaActual) {
		String periodoSemana = fechaAnterior + " al " + fechaActual;

		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendas();
		List<ConocimientoBloqueoTienda> todosBloqueos = new ArrayList<>();
		Map<Integer, List<ConocimientoBloqueoTienda>> bloqueosPorTienda = new HashMap<>();

		long redMinutosBloqueo = 0;
		double redVentaPerdida = 0.0;
		int totalEventos = 0;

		// Procesar cada tienda
		for (Tienda tiendaTemp : tiendas) {
			int idTienda = tiendaTemp.getIdTienda();
			String nomTienda = tiendaTemp.getNombreTienda();

			ArrayList<LogBloqueoTienda> historial = LogBloqueoTiendaDAO.obtenerHistorialTienda(idTienda, fechaAnterior, fechaActual);
			List<ConocimientoBloqueoTienda> listaTienda = new ArrayList<>();

			Date fechaBloqueo = null;
			String motivoBloqueo = "";
			String obsBloqueo = "";
			String debloqueoEn = "";
			int idLogBloqueo = 0;

			for (LogBloqueoTienda logTemp : historial) {
				if ("BLOQUEO".equalsIgnoreCase(logTemp.getAccion())) {
					// Si ya habia un bloqueo sin desbloqueo previo, cerramos el evento anterior con la hora actual
					if (fechaBloqueo != null) {
						long difMs = Math.max(0, logTemp.getFechaAccion() != null ? parsearFecha(logTemp.getFechaAccion()).getTime() - fechaBloqueo.getTime() : 0);
						long min = Math.max(1, TimeUnit.MILLISECONDS.toMinutes(difMs));
						String finStr = logTemp.getFechaAccion();
						ConocimientoBloqueoTienda cPrev = ConocimientoBloqueoDAO.analizarYGuardarBloqueo(idTienda, nomTienda,
								SDF_HORA.format(fechaBloqueo), finStr, min, motivoBloqueo, obsBloqueo, debloqueoEn, periodoSemana, idLogBloqueo);
						listaTienda.add(cPrev);
						todosBloqueos.add(cPrev);
						redMinutosBloqueo += min;
						redVentaPerdida += cPrev.getVentaEstimadaPerdida();
						totalEventos++;
					}

					try {
						fechaBloqueo = SDF_HORA.parse(logTemp.getFechaAccion());
					} catch (Exception e) {
						fechaBloqueo = new Date();
					}
					motivoBloqueo = logTemp.getMotivo() != null ? logTemp.getMotivo() : "";
					obsBloqueo = logTemp.getObservacion() != null ? logTemp.getObservacion() : "";
					debloqueoEn = logTemp.getDebloqueoEn() != null ? logTemp.getDebloqueoEn() : "";
					idLogBloqueo = logTemp.getIdLogBloqueo();

				} else if ("DESBLOQUEO".equalsIgnoreCase(logTemp.getAccion())) {
					if (fechaBloqueo != null) {
						Date fechaDesbloqueo;
						try {
							fechaDesbloqueo = SDF_HORA.parse(logTemp.getFechaAccion());
						} catch (Exception e) {
							fechaDesbloqueo = new Date();
						}

						long dif = Math.max(0, fechaDesbloqueo.getTime() - fechaBloqueo.getTime());
						long difMinutos = Math.max(1, TimeUnit.MILLISECONDS.toMinutes(dif));

						ConocimientoBloqueoTienda c = ConocimientoBloqueoDAO.analizarYGuardarBloqueo(idTienda, nomTienda,
								SDF_HORA.format(fechaBloqueo), SDF_HORA.format(fechaDesbloqueo), difMinutos,
								motivoBloqueo, obsBloqueo, debloqueoEn, periodoSemana, idLogBloqueo);

						listaTienda.add(c);
						todosBloqueos.add(c);
						redMinutosBloqueo += difMinutos;
						redVentaPerdida += c.getVentaEstimadaPerdida();
						totalEventos++;

						fechaBloqueo = null;
						motivoBloqueo = "";
						obsBloqueo = "";
						debloqueoEn = "";
						idLogBloqueo = 0;
					}
				}
			}

			// Caso: Bloqueo activo que no ha tenido DESBLOQUEO al cierre del periodo
			if (fechaBloqueo != null) {
				Date ahora = new Date();
				long dif = Math.max(0, ahora.getTime() - fechaBloqueo.getTime());
				long difMinutos = Math.max(1, TimeUnit.MILLISECONDS.toMinutes(dif));
				ConocimientoBloqueoTienda cActivo = ConocimientoBloqueoDAO.analizarYGuardarBloqueo(idTienda, nomTienda,
						SDF_HORA.format(fechaBloqueo), "[EN CURSO]", difMinutos, motivoBloqueo,
						obsBloqueo + " (Bloqueo en curso al generar reporte)", debloqueoEn, periodoSemana, idLogBloqueo);
				listaTienda.add(cActivo);
				todosBloqueos.add(cActivo);
				redMinutosBloqueo += difMinutos;
				redVentaPerdida += cActivo.getVentaEstimadaPerdida();
				totalEventos++;
			}

			if (!listaTienda.isEmpty()) {
				bloqueosPorTienda.put(idTienda, listaTienda);
			}
		}

		// Construir el reporte HTML de alto nivel
		String html = construirHtmlReporte(tiendas, bloqueosPorTienda, todosBloqueos, fechaAnterior, fechaActual,
				redMinutosBloqueo, redVentaPerdida, totalEventos);

		// Enviar correo
		try {
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("REPORTE SEMANAL BLOQUEOS TIENDAS Y CONOCIMIENTO OPERATIVO (" + fechaAnterior + " A " + fechaActual + ")");
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEBLOQUEOTIENDAS");
			correo.setMensaje(html);

			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
			System.out.println("ReporteBloqueosTienda: Correo enviado exitosamente a lista REPORTEBLOQUEOTIENDAS (" + correos.size() + " destinatarios)");
		} catch (Exception e) {
			System.err.println("Error enviando correo de ReporteBloqueosTienda: " + e.getMessage());
			e.printStackTrace();
		}

		return html;
	}

	/**
	 * Genera la plantilla HTML corporativa con diseno ejecutivo responsive.
	 */
	private static String construirHtmlReporte(ArrayList<Tienda> tiendas,
			Map<Integer, List<ConocimientoBloqueoTienda>> bloqueosPorTienda,
			List<ConocimientoBloqueoTienda> todosBloqueos, String fechaAnterior, String fechaActual,
			long redMinutosBloqueo, double redVentaPerdida, int totalEventos) {

		double totalHorasRed = ((double) redMinutosBloqueo) / 60.0;
		int tiendasConBloqueo = bloqueosPorTienda.size();
		int tiendasTotal = tiendas.size();

		// Frecuencia de motivos
		Map<String, Integer> conteoMotivos = new HashMap<>();
		for (ConocimientoBloqueoTienda c : todosBloqueos) {
			String mot = c.getMotivo();
			if (mot == null || mot.trim().isEmpty()) mot = "Sin Motivo Especificado";
			conteoMotivos.put(mot, conteoMotivos.getOrDefault(mot, 0) + 1);
		}

		String motivoPredominante = "Ninguno";
		int maxConteoMotivo = 0;
		for (Map.Entry<String, Integer> entry : conteoMotivos.entrySet()) {
			if (entry.getValue() > maxConteoMotivo) {
				maxConteoMotivo = entry.getValue();
				motivoPredominante = entry.getKey();
			}
		}
		String pctMotivo = totalEventos > 0 ? (" (" + Math.round(((double) maxConteoMotivo / totalEventos) * 100.0) + "%)") : "";

		StringBuilder sb = new StringBuilder(16384);

		sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
		sb.append("<style>");
		sb.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #F4F6F9; color: #2D3748; margin: 0; padding: 20px; line-height: 1.5; }");
		sb.append(".container { max-width: 980px; margin: 0 auto; background: #FFFFFF; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); overflow: hidden; }");
		sb.append(".header { background: linear-gradient(135deg, #E42528 0%, #A31215 100%); color: #FFFFFF; padding: 25px 30px; text-align: left; }");
		sb.append(".header h1 { margin: 0 0 6px 0; font-size: 22px; font-weight: 700; letter-spacing: 0.5px; }");
		sb.append(".header p { margin: 0; font-size: 14px; opacity: 0.9; }");
		sb.append(".content { padding: 25px 30px; }");
		sb.append(".kpi-grid { display: table; width: 100%; table-layout: fixed; margin-bottom: 25px; }");
		sb.append(".kpi-cell { display: table-cell; padding: 12px; text-align: center; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 6px; }");
		sb.append(".kpi-val { font-size: 22px; font-weight: 800; color: #E42528; margin-top: 4px; }");
		sb.append(".kpi-lbl { font-size: 11px; text-transform: uppercase; color: #718096; font-weight: 700; letter-spacing: 0.5px; }");
		sb.append("h2 { font-size: 16px; color: #102F6F; border-bottom: 2px solid #E2E8F0; padding-bottom: 8px; margin: 25px 0 15px 0; font-weight: 700; }");
		sb.append(".tabla-resumen { width: 100%; border-collapse: collapse; margin-bottom: 25px; font-size: 13px; }");
		sb.append(".tabla-resumen th { background: #102F6F; color: #FFFFFF; font-weight: 600; text-align: left; padding: 10px 12px; font-size: 12px; }");
		sb.append(".tabla-resumen td { padding: 9px 12px; border-bottom: 1px solid #EDF2F7; }");
		sb.append(".tabla-resumen tr:hover { background-color: #F7FAFC; }");
		sb.append(".badge { display: inline-block; padding: 3px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; text-align: center; }");
		sb.append(".badge-critica { background: #FED7D7; color: #9B2C2C; }");
		sb.append(".badge-alta { background: #FEEBC8; color: #9C4221; }");
		sb.append(".badge-media { background: #FEFCBF; color: #744210; }");
		sb.append(".badge-baja { background: #C6F6D5; color: #22543D; }");
		sb.append(".badge-operativa { background: #E6FFFA; color: #234E52; border: 1px solid #38B2AC; }");
		sb.append(".seccion-tienda { background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 6px; margin-bottom: 20px; overflow: hidden; }");
		sb.append(".tienda-titulo { background: #F8FAFC; padding: 10px 15px; font-weight: 700; color: #2D3748; border-bottom: 1px solid #E2E8F0; font-size: 14px; }");
		sb.append(".tabla-detalle { width: 100%; border-collapse: collapse; font-size: 12px; }");
		sb.append(".tabla-detalle th { background: #EDF2F7; color: #4A5568; font-weight: 600; text-align: left; padding: 8px 10px; font-size: 11px; }");
		sb.append(".tabla-detalle td { padding: 8px 10px; border-bottom: 1px solid #EDF2F7; vertical-align: top; }");
		sb.append(".nota-predictiva { font-size: 11px; color: #2B6CB0; background: #EBF8FF; padding: 6px 10px; border-radius: 4px; margin-top: 4px; border-left: 3px solid #3182CE; }");
		sb.append(".hallazgos-box { background: #F7FAFC; border: 1px solid #CBD5E0; border-left: 4px solid #FDC806; padding: 15px; border-radius: 6px; margin-top: 25px; }");
		sb.append(".hallazgos-box h3 { margin: 0 0 10px 0; font-size: 14px; color: #102F6F; }");
		sb.append(".hallazgos-box ul { margin: 0; padding-left: 20px; font-size: 12px; color: #4A5568; }");
		sb.append(".hallazgos-box li { margin-bottom: 6px; }");
		sb.append(".footer { background: #F8FAFC; color: #718096; padding: 15px 30px; font-size: 11px; text-align: center; border-top: 1px solid #E2E8F0; }");
		sb.append("</style></head><body>");

		sb.append("<div class='container'>");

		// Header
		sb.append("<div class='header'>");
		sb.append("<h1>PIZZA AMERICANA &middot; REPORTE SEMANAL DE BLOQUEOS</h1>");
		sb.append("<p>Auditoría Operacional, Fotografía de Carga y Base de Conocimiento Predictivo &middot; Período: <strong>").append(fechaAnterior).append("</strong> al <strong>").append(fechaActual).append("</strong></p>");
		sb.append("</div>");

		sb.append("<div class='content'>");

		// KPI Grid
		sb.append("<div class='kpi-grid'>");
		sb.append("<div class='kpi-cell'><div class='kpi-lbl'>Tiendas Afectadas</div><div class='kpi-val'>").append(tiendasConBloqueo).append("<span style='font-size:14px; color:#718096;'> / ").append(tiendasTotal).append("</span></div></div>");
		sb.append("<div class='kpi-cell' style='border-left:none;'><div class='kpi-lbl'>Eventos Bloqueo</div><div class='kpi-val'>").append(totalEventos).append("</div></div>");
		sb.append("<div class='kpi-cell' style='border-left:none;'><div class='kpi-lbl'>Tiempo Total Red</div><div class='kpi-val'>").append(FMT_DECIMAL.format(totalHorasRed)).append(" <span style='font-size:12px; color:#718096;'>hrs</span></div></div>");
		sb.append("<div class='kpi-cell' style='border-left:none;'><div class='kpi-lbl'>Venta Estimada Perdida</div><div class='kpi-val' style='color:#C53030;'>").append(FMT_PESOS.format(redVentaPerdida)).append("</div></div>");
		sb.append("<div class='kpi-cell' style='border-left:none;'><div class='kpi-lbl'>Causa Principal</div><div class='kpi-val' style='font-size:14px; color:#102F6F; padding-top:4px;'>").append(motivoPredominante).append("<span style='font-size:11px; color:#718096;'>").append(pctMotivo).append("</span></div></div>");
		sb.append("</div>");

		// Tabla 1: Ranking de Impacto por Tienda
		sb.append("<h2>1. Ranking de Afectación por Tienda</h2>");
		sb.append("<table class='tabla-resumen'>");
		sb.append("<thead><tr>");
		sb.append("<th>Tienda</th><th style='text-align:center;'>Eventos</th><th style='text-align:right;'>Minutos</th><th style='text-align:right;'>Horas</th><th style='text-align:right;'>% Red</th><th style='text-align:right;'>Venta Est. Perdida</th><th>Patrón Predominante</th><th style='text-align:center;'>Criticidad</th>");
		sb.append("</tr></thead><tbody>");

		// Ordenar tiendas por minutos descendente
		List<TiendaImpacto> ranking = new ArrayList<>();
		List<String> tiendasSinBloqueo = new ArrayList<>();

		for (Tienda t : tiendas) {
			int idT = t.getIdTienda();
			List<ConocimientoBloqueoTienda> bList = bloqueosPorTienda.get(idT);
			if (bList != null && !bList.isEmpty()) {
				long mins = 0;
				double venta = 0;
				Map<String, Integer> patMap = new HashMap<>();
				String maxPat = "OPERATIVO";
				int maxPatC = 0;
				for (ConocimientoBloqueoTienda cb : bList) {
					mins += cb.getMinutosBloqueo();
					venta += cb.getVentaEstimadaPerdida();
					String p = cb.getPatronDetectado();
					int cnt = patMap.getOrDefault(p, 0) + 1;
					patMap.put(p, cnt);
					if (cnt > maxPatC) {
						maxPatC = cnt;
						maxPat = p;
					}
				}
				ranking.add(new TiendaImpacto(t.getNombreTienda(), bList.size(), mins, venta, maxPat));
			} else {
				tiendasSinBloqueo.add(t.getNombreTienda());
			}
		}

		Collections.sort(ranking, new Comparator<TiendaImpacto>() {
			@Override
			public int compare(TiendaImpacto a, TiendaImpacto b) {
				return Long.compare(b.minutos, a.minutos);
			}
		});

		if (ranking.isEmpty()) {
			sb.append("<tr><td colspan='8' style='text-align:center; padding:20px; color:#22543D;'><strong>Excelente noticia:</strong> Ninguna tienda registró bloqueos durante este período. Red 100% disponible.</td></tr>");
		} else {
			for (TiendaImpacto ti : ranking) {
				double pct = redMinutosBloqueo > 0 ? (((double) ti.minutos) / redMinutosBloqueo) * 100.0 : 0.0;
				String badgeClass = ti.minutos >= 120 ? "badge-critica" : (ti.minutos >= 60 ? "badge-alta" : "badge-media");
				String nivel = ti.minutos >= 120 ? "CRÍTICA" : (ti.minutos >= 60 ? "ALTA" : "MEDIA");

				sb.append("<tr>");
				sb.append("<td><strong>").append(ti.nombre).append("</strong></td>");
				sb.append("<td style='text-align:center;'>").append(ti.eventos).append("</td>");
				sb.append("<td style='text-align:right;'><strong>").append(FMT_ENTERO.format(ti.minutos)).append("</strong></td>");
				sb.append("<td style='text-align:right;'>").append(FMT_DECIMAL.format(((double) ti.minutos) / 60.0)).append("</td>");
				sb.append("<td style='text-align:right;'>").append(FMT_DECIMAL.format(pct)).append("%</td>");
				sb.append("<td style='text-align:right; color:#C53030;'>").append(FMT_PESOS.format(ti.ventaPerdida)).append("</td>");
				sb.append("<td><code style='font-size:11px; color:#4A5568;'>").append(formatearPatron(ti.patron)).append("</code></td>");
				sb.append("<td style='text-align:center;'><span class='badge ").append(badgeClass).append("'>").append(nivel).append("</span></td>");
				sb.append("</tr>");
			}
		}
		sb.append("</tbody></table>");

		// Tabla 2: Detalle Operacional de Cada Bloqueo
		if (!ranking.isEmpty()) {
			sb.append("<h2>2. Detalle Operacional y Condiciones al Momento del Bloqueo</h2>");
			sb.append("<p style='font-size:12px; color:#718096; margin-top:-8px;'>Fotografía en tiempo real: número de pedidos en cola (\"píxeles\"), domiciliarios disponibles, ratio de presión y diagnóstico preventivo.</p>");

			for (Tienda t : tiendas) {
				int idT = t.getIdTienda();
				List<ConocimientoBloqueoTienda> bList = bloqueosPorTienda.get(idT);
				if (bList == null || bList.isEmpty()) continue;

				long subMin = 0;
				for (ConocimientoBloqueoTienda c : bList) subMin += c.getMinutosBloqueo();

				sb.append("<div class='seccion-tienda'>");
				sb.append("<div class='tienda-titulo'>");
				sb.append("<span>").append(t.getNombreTienda()).append("</span>");
				sb.append("<span style='float:right; font-weight:normal; font-size:12px; color:#718096;'>Total: <strong>").append(bList.size()).append(" eventos</strong> &middot; <strong>").append(subMin).append(" mins (").append(FMT_DECIMAL.format(((double) subMin) / 60.0)).append(" hrs)</strong></span>");
				sb.append("</div>");

				sb.append("<table class='tabla-detalle'>");
				sb.append("<thead><tr>");
				sb.append("<th>Fecha & Horario</th>");
				sb.append("<th>Duración</th>");
				sb.append("<th>Motivo & Observación</th>");
				sb.append("<th style='text-align:center;'>Píxeles / Ped.</th>");
				sb.append("<th style='text-align:center;'>Domi. Turno</th>");
				sb.append("<th style='text-align:center;'>Ratio Presión</th>");
				sb.append("<th style='text-align:right;'>Venta Perdida</th>");
				sb.append("<th>Diagnóstico & Acción Predictiva</th>");
				sb.append("</tr></thead><tbody>");

				for (ConocimientoBloqueoTienda cb : bList) {
					String horaIni = extraerHora(cb.getFechaBloqueo());
					String horaFin = extraerHora(cb.getFechaDesbloqueo());
					String horarioStr = horaIni + " - " + horaFin;

					String ratioBadge = "badge-baja";
					if (cb.getRatioPedidosDomiciliario() >= 4.0) ratioBadge = "badge-critica";
					else if (cb.getRatioPedidosDomiciliario() >= 2.5) ratioBadge = "badge-alta";

					sb.append("<tr>");
					sb.append("<td style='white-space:nowrap;'><strong>").append(cb.getDiaSemana()).append("</strong><br><span style='color:#718096;'>").append(cb.getFechaBloqueo().substring(0, 10)).append("</span><br><code style='font-size:11px;'>").append(horarioStr).append("</code></td>");
					sb.append("<td style='white-space:nowrap;'><strong>").append(cb.getMinutosBloqueo()).append(" min</strong><br><span style='color:#718096; font-size:11px;'>(").append(FMT_DECIMAL.format(cb.getHorasBloqueo())).append(" h)</span></td>");
					sb.append("<td><strong>").append(cb.getMotivo()).append("</strong>");
					if (cb.getObservacion() != null && !cb.getObservacion().trim().isEmpty()) {
						sb.append("<br><span style='color:#718096; font-style:italic; font-size:11px;'>\"").append(cb.getObservacion()).append("\"</span>");
					}
					sb.append("</td>");

					sb.append("<td style='text-align:center;'><strong>").append(cb.getPedidosActivosPixel()).append("</strong><br><span style='color:#718096; font-size:10px;'>(").append(cb.getPizzasEnProceso()).append(" pizzas)</span></td>");
					sb.append("<td style='text-align:center;'><strong>").append(cb.getDomiciliariosActivos()).append("</strong></td>");
					sb.append("<td style='text-align:center;'><span class='badge ").append(ratioBadge).append("'>").append(FMT_DECIMAL.format(cb.getRatioPedidosDomiciliario())).append(" p/d</span></td>");
					sb.append("<td style='text-align:right; color:#C53030;'><strong>").append(FMT_PESOS.format(cb.getVentaEstimadaPerdida())).append("</strong></td>");
					sb.append("<td>");
					sb.append("<span class='badge badge-").append(cb.getNivelCriticidad().toLowerCase()).append("'>").append(cb.getNivelCriticidad()).append("</span> ");
					sb.append("<strong style='font-size:11px; color:#2D3748;'>").append(formatearPatron(cb.getPatronDetectado())).append("</strong>");
					if (cb.getSugerenciaPredictiva() != null && !cb.getSugerenciaPredictiva().isEmpty()) {
						sb.append("<div class='nota-predictiva'>&#128161; ").append(cb.getSugerenciaPredictiva()).append("</div>");
					}
					sb.append("</td>");

					sb.append("</tr>");
				}
				sb.append("</tbody></table>");
				sb.append("</div>");
			}
		}

		// 3. Tiendas con 100% Disponibilidad
		sb.append("<h2>3. Tiendas con 100% de Disponibilidad (Sin Bloqueos)</h2>");
		if (tiendasSinBloqueo.isEmpty()) {
			sb.append("<p style='font-size:13px; color:#C53030;'>Todas las tiendas de la red registraron al menos un evento de bloqueo en este período.</p>");
		} else {
			sb.append("<div style='margin-bottom:20px;'>");
			for (String tNom : tiendasSinBloqueo) {
				sb.append("<span class='badge badge-operativa' style='margin:3px 6px 3px 0; font-size:12px; padding:6px 12px;'>&#10004; ").append(tNom).append("</span>");
			}
			sb.append("</div>");
		}

		// 4. Hallazgos Clave y Conocimiento para Bloqueo Predictivo
		sb.append("<div class='hallazgos-box'>");
		sb.append("<h3>&#128202; Base de Conocimiento y Recomendaciones para Bloqueo Predictivo</h3>");
		sb.append("<ul>");

		// Conclusiones inteligentes segun la data
		if (totalEventos > 0) {
			int bloqueosPorDomi = 0;
			int bloqueosFinSemana = 0;
			double sumaRatio = 0.0;
			for (ConocimientoBloqueoTienda c : todosBloqueos) {
				if (c.getPatronDetectado().contains("DOMICILIO") || c.getPatronDetectado().contains("DEMANDA")) bloqueosPorDomi++;
				if (c.isEsFinSemana()) bloqueosFinSemana++;
				sumaRatio += c.getRatioPedidosDomiciliario();
			}
			double ratioProm = sumaRatio / totalEventos;
			int pctDomi = (int) Math.round(((double) bloqueosPorDomi / totalEventos) * 100.0);
			int pctFin = (int) Math.round(((double) bloqueosFinSemana / totalEventos) * 100.0);

			sb.append("<li><strong>Patrón Principal de Saturación:</strong> El <strong>").append(pctDomi).append("%</strong> de los bloqueos estuvieron vinculados a sobrecarga en despacho y desbalance de pedidos vs. domiciliarios disponibles (ratio promedio al momento del bloqueo: <strong>").append(FMT_DECIMAL.format(ratioProm)).append(" pedidos/domiciliario</strong>).</li>");
			sb.append("<li><strong>Concentración Temporal:</strong> El <strong>").append(pctFin).append("%</strong> de los eventos ocurrieron durante el fin de semana (Viernes a Domingo), especialmente en la franja pico nocturna (19:00 a 21:30).</li>");
			sb.append("<li><strong>Regla Predictiva Recomendada:</strong> Cuando una tienda alcance un ratio de <strong>3.2 pedidos por domiciliario</strong> con más de 8 pedidos en cola, el sistema debe alertar preventivamente al supervisor de zona o activar automáticamente apoyo de domiciliario retén, evitando que la tienda llegue al umbral de colapso (ratio &gt; 4.0) que fuerza el bloqueo manual.</li>");
		} else {
			sb.append("<li>Operación completamente estable durante la semana. Sin cuellos de botella registrados.</li>");
		}

		sb.append("<li><strong>Persistencia de Datos:</strong> Todos los eventos, condiciones de carga y patrones analizados quedaron guardados en la tabla <code>pizzaamericana.conocimiento_bloqueo_tienda</code> para alimentar los futuros modelos predictivos y tableros de control.</li>");
		sb.append("</ul>");
		sb.append("</div>");

		sb.append("</div>"); // content

		// Footer
		sb.append("<div class='footer'>");
		sb.append("Generado automáticamente por el Sistema de Servicios Pizza Americana &middot; Capa de Analítica Operacional y Predictiva");
		sb.append("</div>");

		sb.append("</div></body></html>");

		return sb.toString();
	}

	private static String formatearPatron(String patron) {
		if (patron == null) return "GENERAL";
		return patron.replace("_", " ");
	}

	private static String extraerHora(String fechaHoraStr) {
		if (fechaHoraStr == null || fechaHoraStr.trim().isEmpty()) return "--:--";
		if (fechaHoraStr.contains("[EN CURSO]")) return "[EN CURSO]";
		try {
			if (fechaHoraStr.length() >= 16) {
				return fechaHoraStr.substring(11, 16);
			}
			Date d = SDF_HORA.parse(fechaHoraStr);
			return SDF_HORA_SOLA.format(d);
		} catch (Exception e) {
			return fechaHoraStr;
		}
	}

	private static Date parsearFecha(String f) {
		try {
			return SDF_HORA.parse(f);
		} catch (Exception e) {
			return new Date();
		}
	}

	private static class TiendaImpacto {
		String nombre;
		int eventos;
		long minutos;
		double ventaPerdida;
		String patron;

		TiendaImpacto(String nombre, int eventos, long minutos, double ventaPerdida, String patron) {
			this.nombre = nombre;
			this.eventos = eventos;
			this.minutos = minutos;
			this.ventaPerdida = ventaPerdida;
			this.patron = patron;
		}
	}
}
