package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.VentaIntegralTiendaDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.Tienda;
import capaDAOCC.VentaIntegralCategoriaDAO;
import capaDAOCC.VentaIntegralResumenDAO;
import capaModeloCC.VentaIntegralCategoria;
import capaModeloCC.VentaIntegralResumenSemana;
import utilidadesSer.ControladorEnvioCorreo;

/**
 * Cierre semanal de Venta Integral: reemplaza las ~14 consultas SQL (7 por
 * tienda, 7 de contact center) que se corrian a mano cada semana. El
 * catalogo de categorias -que productos/especialidades cuentan en cada una y
 * como se miden- sale de capaDAOCC.VentaIntegralCategoriaDAO, no de codigo
 * fijo: cambiar la temporada es cambiar la parametrizacion en el central, no
 * recompilar este programa.
 *
 * Mismo esqueleto que ReporteSemanalRappi: generarReporteVentaIntegral() solo
 * calcula el periodo de hoy, generar(inicio, fin) tiene toda la logica y es
 * lo que reutiliza el reproceso.
 */
public class ServicioSemanalVentaIntegral {

	private static final String AZUL = "#20287F";
	private static final String GRIS_FONDO = "#F4F4F6";
	private static final String GRIS_BORDE = "#D9D9E0";
	private static final String VERDE = "#3C763D";
	private static final String AMARILLO = "#8A6D3B";
	private static final String ROJO = "#A94442";

	/** Parametro que trae la lista de destinatarios del correo (tabla parametros_correo). */
	private static final String PARAM_CORREOS = "REPORTEVENTAINTEGRAL";

	/** Parametro con la fecha de corte a reprocesar, en aaaa-mm-dd. */
	private static final String PARAM_FECHA_REPROCESO = "FECHAREPROCESO";

	public void generarReporteVentaIntegral() {
		this.generarParaCorte(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
	}

	public void generarParaCorte(final String fechaCorte) {
		final String[] rango = this.periodo(fechaCorte);
		if (rango == null) {
			return;
		}
		System.out.println("ServicioSemanalVentaIntegral: periodo " + rango[0] + " a " + rango[1]);
		this.generar(rango[0], rango[1]);
	}

	public void reprocesar() {
		final String corte = ParametrosDAO.retornarValorAlfanumerico(PARAM_FECHA_REPROCESO);
		if (corte == null || corte.trim().length() < 10) {
			System.out.println("ServicioSemanalVentaIntegral: el parametro " + PARAM_FECHA_REPROCESO
					+ " no tiene una fecha utilizable: '" + corte + "'");
			return;
		}
		this.generarParaCorte(corte.trim());
	}

	/**
	 * El periodo lunes-domingo que cierra con una fecha de corte. Igual que
	 * ReporteSemanalRappi: se acepta corte domingo (la semana que acaba de
	 * terminar) o lunes (por si el Task Scheduler quedo programado de madrugada
	 * del dia siguiente), para no atar el codigo a una hora exacta de corrida.
	 */
	private String[] periodo(final String fechaCorte) {
		if (fechaCorte == null || fechaCorte.trim().length() < 10) {
			System.out.println("ServicioSemanalVentaIntegral: la fecha de corte no es utilizable: '" + fechaCorte + "'");
			return (null);
		}
		final Calendar calendario = Calendar.getInstance();
		try {
			calendario.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(fechaCorte.trim()));
		} catch (Exception e) {
			System.out.println("ServicioSemanalVentaIntegral: no se pudo interpretar la fecha '" + fechaCorte + "': " + e);
			return (null);
		}
		final int dia = calendario.get(Calendar.DAY_OF_WEEK);
		if (dia != Calendar.SUNDAY && dia != Calendar.MONDAY) {
			System.out.println("ServicioSemanalVentaIntegral: el corte " + fechaCorte
					+ " no cae domingo ni lunes, no se procesa.");
			return (null);
		}
		final SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		final String fin = formatoFecha.format(calendario.getTime());
		calendario.add(Calendar.DAY_OF_YEAR, (dia == Calendar.SUNDAY ? -6 : -7));
		return (new String[] { formatoFecha.format(calendario.getTime()), fin });
	}

	/**
	 * Genera el cierre para el rango de fechas indicado: consulta cada tienda y
	 * el contact center, guarda el resumen y envia el correo. Es el punto de
	 * entrada que usa el reproceso.
	 */
	public void generar(final String semanaInicio, final String semanaFin) {
		final ArrayList<VentaIntegralCategoria> categorias = VentaIntegralCategoriaDAO.listarCategoriasActivas();
		if (categorias.isEmpty()) {
			System.out.println("ServicioSemanalVentaIntegral: no hay categorias activas, no hay nada que calcular.");
			return;
		}
		final ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocalSinBodega();

		// idtienda -> idcategoria -> cantidad. Se llena primero con el lado tienda
		// (conexion remota, una por una) y despues se completa con el lado contact
		// center (una sola consulta al central por categoria, agrupada por tienda).
		final Map<Integer, Map<Integer, Double>> cantidadTiendaPorTienda = new HashMap<>();
		final Map<Integer, Map<Integer, Double>> cantidadCCPorTienda = new HashMap<>();
		final Map<Integer, String> nombrePorTienda = new HashMap<>();

		for (Tienda tienda : tiendas) {
			nombrePorTienda.put(tienda.getIdTienda(), tienda.getNombreTienda());
			final Map<Integer, Double> cantidadesCategoria = new HashMap<>();
			cantidadTiendaPorTienda.put(tienda.getIdTienda(), cantidadesCategoria);
			if (tienda.getHostBD() == null || tienda.getHostBD().trim().isEmpty()) {
				continue;
			}
			for (VentaIntegralCategoria categoria : categorias) {
				try {
					double total = VentaIntegralTiendaDAO.consultarTotalTienda(tienda.getHostBD(), categoria,
							semanaInicio, semanaFin);
					cantidadesCategoria.put(categoria.getIdCategoria(), total);
				} catch (Exception e) {
					// Una tienda inalcanzable no puede tumbar el cierre de las demas.
					System.out.println("ServicioSemanalVentaIntegral: fallo tienda " + tienda.getNombreTienda()
							+ " categoria " + categoria.getNombre() + ": " + e);
				}
			}
		}

		for (VentaIntegralCategoria categoria : categorias) {
			final HashMap<Integer, Double> totalesCC = VentaIntegralResumenDAO
					.consultarTotalesContactCenter(categoria, semanaInicio, semanaFin);
			for (Map.Entry<Integer, Double> entrada : totalesCC.entrySet()) {
				cantidadCCPorTienda.computeIfAbsent(entrada.getKey(), k -> new HashMap<>()).put(
						categoria.getIdCategoria(), entrada.getValue());
			}
		}

		// idtienda -> idcategoria -> cantidad_total, para el correo (CV en memoria,
		// sin volver a consultar la tabla de resumen).
		final Map<Integer, Map<Integer, Double>> totalCombinadoPorTienda = new HashMap<>();
		for (Tienda tienda : tiendas) {
			final Map<Integer, Double> cantidadesTienda = cantidadTiendaPorTienda.getOrDefault(tienda.getIdTienda(),
					new HashMap<>());
			final Map<Integer, Double> cantidadesCC = cantidadCCPorTienda.getOrDefault(tienda.getIdTienda(),
					new HashMap<>());
			final Map<Integer, Double> combinado = new HashMap<>();
			totalCombinadoPorTienda.put(tienda.getIdTienda(), combinado);
			for (VentaIntegralCategoria categoria : categorias) {
				double cantTienda = cantidadesTienda.getOrDefault(categoria.getIdCategoria(), 0.0);
				double cantCC = cantidadesCC.getOrDefault(categoria.getIdCategoria(), 0.0);
				combinado.put(categoria.getIdCategoria(), cantTienda + cantCC);
				VentaIntegralResumenDAO.insertarOActualizarResumen(new VentaIntegralResumenSemana(
						tienda.getIdTienda(), categoria.getIdCategoria(), semanaInicio, semanaFin, cantTienda,
						cantCC));
			}
		}

		this.enviarCorreo(semanaInicio, semanaFin, categorias, tiendas, nombrePorTienda, totalCombinadoPorTienda);
	}

	// =======================================================================
	// Correo: resumen de la semana, con el mismo indicador de dispersion (CV)
	// que muestra la pantalla de Monitoreo.
	// =======================================================================

	private void enviarCorreo(final String semanaInicio, final String semanaFin,
			final ArrayList<VentaIntegralCategoria> categorias, final ArrayList<Tienda> tiendas,
			final Map<Integer, String> nombrePorTienda, final Map<Integer, Map<Integer, Double>> totales) {

		// Promedio de la red por categoria, para el indice tienda/promedio.
		final Map<Integer, Double> promedioPorCategoria = new HashMap<>();
		for (VentaIntegralCategoria categoria : categorias) {
			double suma = 0;
			int conteo = 0;
			for (Tienda tienda : tiendas) {
				Double valor = totales.get(tienda.getIdTienda()).get(categoria.getIdCategoria());
				if (valor != null) {
					suma += valor;
					conteo++;
				}
			}
			promedioPorCategoria.put(categoria.getIdCategoria(), conteo > 0 ? (suma / conteo) : 0.0);
		}

		final DecimalFormat numero = new DecimalFormat("###,##0.##");
		final StringBuilder html = new StringBuilder();
		html.append("<div style=\"font-family:Verdana,Arial,sans-serif;font-size:12px;color:#222\">");
		html.append("<div style=\"background:").append(AZUL).append(";color:#FFFFFF;padding:12px 16px;")
				.append("font-size:15px;font-weight:bold\">CIERRE SEMANAL VENTA INTEGRAL</div>");
		html.append("<p style=\"margin:8px 0 16px 0;color:#555\">Periodo del <b>").append(semanaInicio)
				.append("</b> al <b>").append(semanaFin)
				.append("</b>. Solo venta fisica (tienda + contact center), no incluye app / tienda virtual.</p>");

		html.append("<table cellspacing=\"0\" cellpadding=\"6\" style=\"border-collapse:collapse;"
				+ "border:1px solid " + GRIS_BORDE + "\">");
		html.append("<tr><td style=\"background:").append(AZUL).append(";color:#FFFFFF;font-weight:bold\">Tienda</td>");
		for (VentaIntegralCategoria categoria : categorias) {
			html.append("<td style=\"background:").append(AZUL).append(";color:#FFFFFF;font-weight:bold;")
					.append("text-align:right\">").append(categoria.getNombre()).append("</td>");
		}
		html.append("<td style=\"background:").append(AZUL).append(";color:#FFFFFF;font-weight:bold;")
				.append("text-align:right\">CV</td></tr>");

		for (Tienda tienda : tiendas) {
			final Map<Integer, Double> cantidadesTienda = totales.get(tienda.getIdTienda());
			final ArrayList<Double> indices = new ArrayList<>();
			html.append("<tr><td style=\"border-top:1px solid ").append(GRIS_BORDE).append("\">")
					.append(tienda.getNombreTienda()).append("</td>");
			for (VentaIntegralCategoria categoria : categorias) {
				double cantidad = cantidadesTienda.getOrDefault(categoria.getIdCategoria(), 0.0);
				double promedioRed = promedioPorCategoria.getOrDefault(categoria.getIdCategoria(), 0.0);
				if (promedioRed > 0) {
					indices.add(cantidad / promedioRed);
				}
				html.append("<td style=\"border-top:1px solid ").append(GRIS_BORDE).append(";text-align:right\">")
						.append(numero.format(cantidad)).append("</td>");
			}
			final double cv = this.coeficienteVariacion(indices);
			final String color = cv > 0.35 ? ROJO : (cv > 0.15 ? AMARILLO : VERDE);
			html.append("<td style=\"border-top:1px solid ").append(GRIS_BORDE).append(";text-align:right;")
					.append("color:").append(color).append(";font-weight:bold\">")
					.append(indices.isEmpty() ? "&mdash;" : new DecimalFormat("0.000").format(cv)).append("</td></tr>");
		}
		html.append("</table>");
		html.append("<p style=\"margin:10px 0 0 0;color:#777;font-size:11px\">CV = coeficiente de variacion de los "
				+ "indices (cantidad de la tienda / promedio de la red) entre las categorias de la temporada. Bajo "
				+ "(verde) es desempeno parejo entre categorias; alto (rojo) esta concentrado en pocas.</p>");
		html.append("</div>");

		final Correo correo = new Correo();
		final CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES",
				"CLAVECORREOREPORTE");
		correo.setAsunto("Cierre Semanal Venta Integral " + semanaInicio + " a " + semanaFin);
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		final ArrayList correos = GeneralDAO.obtenerCorreosParametro(PARAM_CORREOS);
		if (correos.isEmpty()) {
			System.out.println("ServicioSemanalVentaIntegral: no hay destinatarios en parametros_correo para "
					+ PARAM_CORREOS + ", no se envia correo. El resumen ya quedo guardado en la base.");
			return;
		}
		correo.setMensaje(html.toString());
		final ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}

	/** Desviacion estandar de los indices sobre su promedio. 0 si no hay indices o el promedio es 0. */
	private double coeficienteVariacion(final ArrayList<Double> indices) {
		if (indices.isEmpty()) {
			return (0);
		}
		double promedio = 0;
		for (Double indice : indices) {
			promedio += indice;
		}
		promedio = promedio / indices.size();
		if (promedio == 0) {
			return (0);
		}
		double varianza = 0;
		for (Double indice : indices) {
			varianza += Math.pow(indice - promedio, 2);
		}
		varianza = varianza / indices.size();
		return (Math.sqrt(varianza) / promedio);
	}

	public static void main(String[] args) {
		new ServicioSemanalVentaIntegral().generarReporteVentaIntegral();
	}

}
