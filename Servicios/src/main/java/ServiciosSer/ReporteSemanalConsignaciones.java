package ServiciosSer;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import CapaDAOSer.ConsignacionSemanalDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.TiendaDAO;
import ModeloSer.ConsignacionSemanal;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.Tienda;
import utilidadesSer.ControladorEnvioCorreo;
import utilidadesSer.CorreoConsignacion;

/**
 * Cierre semanal de consignaciones: un solo Excel, con una hoja por tienda, en vez de
 * las ~12 correos sueltos que hoy manda cada tienda por su lado al terminar su cierre
 * semanal (PedidoCtrl.enviarCorreoConsignacionSemanal, boton "Finalizar Dia" del POS).
 * Esa ruta sigue existiendo -no se toco-, esta es una consolidacion aparte, pensada
 * para revisar y archivar las 12 tiendas de una sola vez, en un solo momento fijo
 * (madrugada del lunes), en vez de en 12 momentos distintos segun cuando cada tienda
 * cierre su semana.
 *
 * Mismo esqueleto que ReporteSemanalVentaIntegral: generarReporteConsignaciones() solo
 * calcula el periodo de hoy, generar(inicio, fin) tiene toda la logica y es lo que
 * reutiliza el reproceso.
 *
 * El archivo se guarda en la carpeta de general.parametros.RUTAARCHIVOTIEMPO -la misma
 * que ya usa el reporte de horarios para dejar sus Excel antes de mandarlos-: no hace
 * falta un parametro de carpeta aparte solo para este reporte.
 */
public class ReporteSemanalConsignaciones {

	/** Parametro con la lista de destinatarios (general.parametros_correo). */
	private static final String PARAM_CORREOS = "REPORTESEMCONSIGNACIONESEXCEL";

	/** Parametro con la fecha de corte a reprocesar, en aaaa-mm-dd (general.parametros). */
	private static final String PARAM_FECHA_REPROCESO = "FECHAREPROCESO";

	/** Mismo parametro de carpeta que usa ReporteSemanalHorariosBase. */
	private static final String PARAM_RUTA_ARCHIVO = "RUTAARCHIVOTIEMPO";

	public void generarReporteConsignaciones() {
		this.generarParaCorte(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
	}

	public void reprocesar() {
		final String corte = ParametrosDAO.retornarValorAlfanumerico(PARAM_FECHA_REPROCESO);
		if (corte == null || corte.trim().length() < 10) {
			System.out.println("ReporteSemanalConsignaciones: el parametro " + PARAM_FECHA_REPROCESO
					+ " no tiene una fecha utilizable: '" + corte + "'");
			return;
		}
		this.generarParaCorte(corte.trim());
	}

	public void generarParaCorte(final String fechaCorte) {
		final String[] rango = this.periodo(fechaCorte);
		if (rango == null) {
			return;
		}
		System.out.println("ReporteSemanalConsignaciones: periodo " + rango[0] + " a " + rango[1]);
		this.generar(rango[0], rango[1]);
	}

	/**
	 * El periodo lunes-domingo que cierra con una fecha de corte. Igual que
	 * ServicioSemanalVentaIntegral: se acepta corte domingo (la semana que acaba de
	 * terminar) o lunes (por si el Task Scheduler quedo programado de madrugada del
	 * dia siguiente), para no atar el codigo a una hora exacta de corrida.
	 */
	private String[] periodo(final String fechaCorte) {
		if (fechaCorte == null || fechaCorte.trim().length() < 10) {
			System.out.println("ReporteSemanalConsignaciones: la fecha de corte no es utilizable: '" + fechaCorte + "'");
			return (null);
		}
		final Calendar calendario = Calendar.getInstance();
		try {
			calendario.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(fechaCorte.trim()));
		} catch (final Exception e) {
			System.out.println("ReporteSemanalConsignaciones: no se pudo interpretar la fecha '" + fechaCorte + "': " + e);
			return (null);
		}
		final int dia = calendario.get(Calendar.DAY_OF_WEEK);
		if (dia != Calendar.SUNDAY && dia != Calendar.MONDAY) {
			System.out.println("ReporteSemanalConsignaciones: el corte " + fechaCorte
					+ " no cae domingo ni lunes, no se procesa.");
			return (null);
		}
		final SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		final String fin = formatoFecha.format(calendario.getTime());
		calendario.add(Calendar.DAY_OF_YEAR, (dia == Calendar.SUNDAY ? -6 : -7));
		return (new String[] { formatoFecha.format(calendario.getTime()), fin });
	}

	/**
	 * Genera el Excel de la semana (inicio a fin, ambos incluidos) y lo manda por
	 * correo. Es el punto de entrada que usa el reproceso.
	 */
	public void generar(final String semanaInicio, final String semanaFin) {
		// Se incluye Bodega a proposito -es dinero, no un ranking de desempeno-: una
		// tienda operando que faltara en un reporte de consignaciones seria justo el
		// tipo de hueco que este reporte deberia estar mostrando, no escondiendo.
		final ArrayList<Tienda> tiendas = new ArrayList<Tienda>();
		for (final Tienda tienda : TiendaDAO.obtenerTiendasLocal()) {
			if (tienda.getHostBD() != null && tienda.getHostBD().trim().length() > 0) {
				tiendas.add(tienda);
			}
		}
		if (tiendas.isEmpty()) {
			System.out.println("ReporteSemanalConsignaciones: no hay tiendas con base local configurada.");
			return;
		}

		final Workbook workbook = new XSSFWorkbook();
		final Estilos estilos = new Estilos(workbook);

		final ArrayList<String> sinRespuesta = new ArrayList<String>();
		final ArrayList<Object[]> resumenPorTienda = new ArrayList<Object[]>(); // {nombre, cantidad, total}
		double totalGeneral = 0;
		int cantidadGeneral = 0;
		final Set<String> nombresHojaUsados = new HashSet<String>();

		for (final Tienda tienda : tiendas) {
			final ArrayList<ConsignacionSemanal> consignaciones = ConsignacionSemanalDAO
					.obtenerConsignacionesRango(semanaInicio, semanaFin, tienda.getHostBD());
			if (consignaciones == null) {
				sinRespuesta.add(tienda.getNombreTienda());
				resumenPorTienda.add(new Object[] { tienda.getNombreTienda(), null, null });
				continue;
			}
			double totalTienda = 0;
			for (final ConsignacionSemanal fila : consignaciones) {
				totalTienda += fila.getValorConsignacion();
			}
			totalGeneral += totalTienda;
			cantidadGeneral += consignaciones.size();
			resumenPorTienda.add(new Object[] { tienda.getNombreTienda(), Integer.valueOf(consignaciones.size()),
					Double.valueOf(totalTienda) });
			this.pintarHojaTienda(workbook, estilos, this.nombreHoja(tienda.getNombreTienda(), nombresHojaUsados),
					consignaciones, totalTienda);
		}

		this.pintarResumen(workbook, estilos, semanaInicio, semanaFin, resumenPorTienda, cantidadGeneral,
				totalGeneral);

		final String rutaArchivo = this.guardarArchivo(workbook, semanaInicio, semanaFin);
		if (rutaArchivo == null) {
			return;
		}

		this.enviarCorreo(semanaInicio, semanaFin, tiendas.size() - sinRespuesta.size(), cantidadGeneral,
				totalGeneral, sinRespuesta, rutaArchivo);
	}

	// =======================================================================
	// Excel
	// =======================================================================

	private static class Estilos {
		final CellStyle encabezado;
		final CellStyle textoNormal;
		final CellStyle valor;
		final CellStyle total;
		final CellStyle totalValor;
		final CellStyle tituloHoja;

		Estilos(final Workbook workbook) {
			final Font fuenteEncabezado = workbook.createFont();
			fuenteEncabezado.setBold(true);
			fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());
			this.encabezado = workbook.createCellStyle();
			this.encabezado.setFont(fuenteEncabezado);
			this.encabezado.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
			this.encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			this.encabezado.setAlignment(HorizontalAlignment.CENTER);
			this.encabezado.setBorderBottom(BorderStyle.THIN);

			this.textoNormal = workbook.createCellStyle();
			this.textoNormal.setBorderBottom(BorderStyle.HAIR);

			final DataFormatCompartido formato = new DataFormatCompartido(workbook);
			this.valor = workbook.createCellStyle();
			this.valor.setBorderBottom(BorderStyle.HAIR);
			this.valor.setAlignment(HorizontalAlignment.RIGHT);
			this.valor.setDataFormat(formato.pesos);

			final Font fuenteTotal = workbook.createFont();
			fuenteTotal.setBold(true);
			this.total = workbook.createCellStyle();
			this.total.setFont(fuenteTotal);
			this.total.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
			this.total.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			this.total.setBorderTop(BorderStyle.MEDIUM);

			this.totalValor = workbook.createCellStyle();
			this.totalValor.setFont(fuenteTotal);
			this.totalValor.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
			this.totalValor.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			this.totalValor.setBorderTop(BorderStyle.MEDIUM);
			this.totalValor.setAlignment(HorizontalAlignment.RIGHT);
			this.totalValor.setDataFormat(formato.pesos);

			final Font fuenteTitulo = workbook.createFont();
			fuenteTitulo.setBold(true);
			fuenteTitulo.setFontHeightInPoints((short) 13);
			fuenteTitulo.setColor(IndexedColors.DARK_BLUE.getIndex());
			this.tituloHoja = workbook.createCellStyle();
			this.tituloHoja.setFont(fuenteTitulo);
		}
	}

	/** El formato de pesos se pide una sola vez: DataFormat tambien es del libro, no de la celda. */
	private static class DataFormatCompartido {
		final short pesos;

		DataFormatCompartido(final Workbook workbook) {
			this.pesos = workbook.createDataFormat().getFormat("#,##0");
		}
	}

	private void pintarHojaTienda(final Workbook workbook, final Estilos estilos, final String nombreHoja,
			final ArrayList<ConsignacionSemanal> consignaciones, final double totalTienda) {
		final Sheet sheet = workbook.createSheet(nombreHoja);
		sheet.setColumnWidth(0, 3000);
		sheet.setColumnWidth(1, 3500);
		sheet.setColumnWidth(2, 10000);
		sheet.setColumnWidth(3, 4000);
		sheet.setColumnWidth(4, 3500);
		sheet.setColumnWidth(5, 3500);
		sheet.setColumnWidth(6, 4500);
		sheet.setColumnWidth(7, 4500);

		int fila = 0;
		final Row filaEncabezado = sheet.createRow(fila++);
		final String[] encabezados = { "Id Consignacion", "Fecha Sistema", "Descripcion", "Valor Consignado",
				"Hora Consignacion", "Fecha Real", "Usuario", "Usuario Testigo" };
		for (int c = 0; c < encabezados.length; c++) {
			final Cell celda = filaEncabezado.createCell(c);
			celda.setCellValue(encabezados[c]);
			celda.setCellStyle(estilos.encabezado);
		}

		for (final ConsignacionSemanal con : consignaciones) {
			final Row filaDatos = sheet.createRow(fila++);
			this.celdaTexto(filaDatos, 0, String.valueOf(con.getIdConsignacion()), estilos.textoNormal);
			this.celdaTexto(filaDatos, 1, con.getFechaSistema(), estilos.textoNormal);
			this.celdaTexto(filaDatos, 2, con.getDescripcion(), estilos.textoNormal);
			final Cell celdaValor = filaDatos.createCell(3);
			celdaValor.setCellValue(con.getValorConsignacion());
			celdaValor.setCellStyle(estilos.valor);
			this.celdaTexto(filaDatos, 4, con.getHoraConsignacion(), estilos.textoNormal);
			this.celdaTexto(filaDatos, 5, con.getFechaReal(), estilos.textoNormal);
			this.celdaTexto(filaDatos, 6, con.getUsuario(), estilos.textoNormal);
			this.celdaTexto(filaDatos, 7, con.getUsuarioTestigo(), estilos.textoNormal);
		}

		final Row filaTotal = sheet.createRow(fila);
		this.celdaTexto(filaTotal, 0, "TOTAL (" + consignaciones.size() + " consignaciones)", estilos.total);
		for (int c = 1; c <= 2; c++) {
			final Cell vacio = filaTotal.createCell(c);
			vacio.setCellStyle(estilos.total);
		}
		final Cell celdaTotalValor = filaTotal.createCell(3);
		celdaTotalValor.setCellValue(totalTienda);
		celdaTotalValor.setCellStyle(estilos.totalValor);
		for (int c = 4; c <= 7; c++) {
			final Cell vacio = filaTotal.createCell(c);
			vacio.setCellStyle(estilos.total);
		}
	}

	private void pintarResumen(final Workbook workbook, final Estilos estilos, final String semanaInicio,
			final String semanaFin, final ArrayList<Object[]> resumenPorTienda, final int cantidadGeneral,
			final double totalGeneral) {
		// Se crea de ultima pero se mueve al comienzo: es lo primero que alguien
		// deberia ver al abrir el archivo, antes de entrar hoja por hoja.
		final Sheet sheet = workbook.createSheet("RESUMEN");
		sheet.setColumnWidth(0, 7000);
		sheet.setColumnWidth(1, 4500);
		sheet.setColumnWidth(2, 5500);

		int fila = 0;
		final Row filaTitulo = sheet.createRow(fila++);
		filaTitulo.createCell(0)
				.setCellValue("Consignaciones de la semana del " + semanaInicio + " al " + semanaFin);
		filaTitulo.getCell(0).setCellStyle(estilos.tituloHoja);
		fila++;

		final Row filaEncabezado = sheet.createRow(fila++);
		final String[] encabezados = { "Tienda", "Consignaciones", "Valor Consignado" };
		for (int c = 0; c < encabezados.length; c++) {
			final Cell celda = filaEncabezado.createCell(c);
			celda.setCellValue(encabezados[c]);
			celda.setCellStyle(estilos.encabezado);
		}

		for (final Object[] resumen : resumenPorTienda) {
			final Row filaDatos = sheet.createRow(fila++);
			this.celdaTexto(filaDatos, 0, (String) resumen[0], estilos.textoNormal);
			if (resumen[1] == null) {
				this.celdaTexto(filaDatos, 1, "SIN RESPUESTA", estilos.textoNormal);
				this.celdaTexto(filaDatos, 2, "-", estilos.textoNormal);
				continue;
			}
			final Cell celdaCantidad = filaDatos.createCell(1);
			celdaCantidad.setCellValue(((Integer) resumen[1]).intValue());
			celdaCantidad.setCellStyle(estilos.textoNormal);
			final Cell celdaValor = filaDatos.createCell(2);
			celdaValor.setCellValue(((Double) resumen[2]).doubleValue());
			celdaValor.setCellStyle(estilos.valor);
		}

		final Row filaTotal = sheet.createRow(fila);
		this.celdaTexto(filaTotal, 0, "TOTAL GENERAL", estilos.total);
		final Cell celdaCantidadTotal = filaTotal.createCell(1);
		celdaCantidadTotal.setCellValue(cantidadGeneral);
		celdaCantidadTotal.setCellStyle(estilos.total);
		final Cell celdaValorTotal = filaTotal.createCell(2);
		celdaValorTotal.setCellValue(totalGeneral);
		celdaValorTotal.setCellStyle(estilos.totalValor);

		workbook.setSheetOrder("RESUMEN", 0);
		workbook.setActiveSheet(0);
	}

	private void celdaTexto(final Row fila, final int columna, final String valor, final CellStyle estilo) {
		final Cell celda = fila.createCell(columna);
		celda.setCellValue(valor == null ? "" : valor);
		celda.setCellStyle(estilo);
	}

	/**
	 * El nombre de una hoja de Excel no puede pasar de 31 caracteres ni repetirse,
	 * y no admite : \ / ? * [ ]. El nombre de una tienda por si solo nunca ha dado
	 * problema, pero mejor no confiar en que nunca lo dara.
	 */
	private String nombreHoja(final String nombreTienda, final Set<String> usados) {
		String limpio = (nombreTienda == null ? "Tienda" : nombreTienda).replaceAll("[\\\\/\\?\\*\\[\\]:]", " ")
				.trim();
		if (limpio.length() > 31) {
			limpio = limpio.substring(0, 31);
		}
		if (limpio.isEmpty()) {
			limpio = "Tienda";
		}
		String candidato = limpio;
		int sufijo = 2;
		while (usados.contains(candidato.toUpperCase())) {
			final String cola = " (" + sufijo + ")";
			candidato = limpio.substring(0, Math.min(limpio.length(), 31 - cola.length())) + cola;
			sufijo++;
		}
		usados.add(candidato.toUpperCase());
		return candidato;
	}

	// =======================================================================
	// Archivo y correo
	// =======================================================================

	/** @return la ruta del archivo guardado, o null si no se pudo guardar. */
	private String guardarArchivo(final Workbook workbook, final String semanaInicio, final String semanaFin) {
		final String carpeta = ParametrosDAO.retornarValorAlfanumericoLocal(PARAM_RUTA_ARCHIVO);
		if (carpeta == null || carpeta.trim().length() == 0) {
			System.out.println("ReporteSemanalConsignaciones: general.parametros." + PARAM_RUTA_ARCHIVO
					+ " no esta configurado en esta maquina; no hay donde guardar el Excel.");
			return null;
		}
		final String separador = carpeta.trim().endsWith(File.separator) ? "" : File.separator;
		final String ruta = carpeta.trim() + separador + "ReporteConsignaciones-" + semanaInicio + "--" + semanaFin
				+ ".xlsx";
		try {
			final FileOutputStream salida = new FileOutputStream(ruta);
			workbook.write(salida);
			salida.close();
			workbook.close();
			return ruta;
		} catch (final Exception e) {
			System.out.println("ReporteSemanalConsignaciones: no se pudo guardar el Excel en '" + ruta + "': " + e);
			return null;
		}
	}

	private void enviarCorreo(final String semanaInicio, final String semanaFin, final int tiendasConDatos,
			final int cantidadGeneral, final double totalGeneral, final ArrayList<String> sinRespuesta,
			final String rutaArchivo) {
		final ArrayList correos = GeneralDAO.obtenerCorreosParametro(PARAM_CORREOS);
		if (correos.isEmpty()) {
			System.out.println("ReporteSemanalConsignaciones: no hay destinatarios en parametros_correo para "
					+ PARAM_CORREOS + ". El Excel ya quedo guardado en " + rutaArchivo + ", pero no se envia correo.");
			return;
		}

		final DecimalFormat formatea = new DecimalFormat("###,###");
		final StringBuilder cuerpo = new StringBuilder();
		cuerpo.append(CorreoConsignacion.abrir("Consignaciones de la semana",
				"Semana del " + semanaInicio + " al " + semanaFin + " · " + tiendasConDatos + " tiendas"));
		if (!sinRespuesta.isEmpty()) {
			cuerpo.append(CorreoConsignacion.aviso("No respondieron estas tiendas y sus consignaciones NO estan en"
					+ " el archivo: " + this.join(sinRespuesta) + ". Verifique que el computador este encendido y"
					+ " reprocese la semana."));
		}
		cuerpo.append(CorreoConsignacion.tarjetaTotal("Total consignado en la semana", totalGeneral,
				cantidadGeneral + " consignaciones registradas entre las " + tiendasConDatos + " tiendas que"
						+ " respondieron"));
		cuerpo.append(CorreoConsignacion.cerrar("El detalle de cada tienda -una hoja por tienda, mas un resumen- va"
				+ " en el archivo Excel adjunto. Generado automaticamente por Servicios Pizza Americana el "
				+ new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime()) + "."));

		final Correo correo = new Correo();
		correo.setAsunto("CONSIGNACIONES SEMANALES " + semanaInicio + " a " + semanaFin);
		final CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES",
				"CLAVECORREOREPORTE");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje(cuerpo.toString());
		correo.setRutasArchivos(new String[] { rutaArchivo });
		new ControladorEnvioCorreo(correo, correos).enviarCorreoHTMLAnexo();
	}

	private String join(final ArrayList<String> nombres) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nombres.size(); i++) {
			sb.append(i == 0 ? "" : ", ").append(nombres.get(i));
		}
		return sb.toString();
	}

	public static void main(final String[] args) {
		new ReporteSemanalConsignaciones().generarReporteConsignaciones();
	}

}
