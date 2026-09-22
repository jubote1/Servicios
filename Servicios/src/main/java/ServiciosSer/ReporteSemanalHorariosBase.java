package ServiciosSer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;




import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.HorarioPlanificadoDAO;
import CapaDAOSer.HorarioResumenDAO;
import CapaDAOSer.HorarioTrabajadoDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.PedidoFueraTiempoDAO;
import CapaDAOSer.PedidoPOSPMDAO;
import CapaDAOSer.PedidoPixelDAO;
import CapaDAOSer.ReporteContactCenterDAO;
import CapaDAOSer.ReporteHorariosDAO;
import CapaDAOSer.TiempoPedidoDAO;
import CapaDAOSer.TiendaDAO;
import ConexionSer.ConexionBaseDatos;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.DiaFestivo;
import ModeloSer.EmpleadoEvento;
import ModeloSer.HorarioResumen;
import ModeloSer.HorarioTrabajado;
import ModeloSer.Pedido;
import ModeloSer.PedidoFueraTiempo;
import ModeloSer.PedidoPixel;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import utilidadesSer.ControladorEnvioCorreo;
import utilidadesSer.CorreoHtml;

/**
 * El reporte semanal de cumplimiento de horarios. UNA sola implementacion.
 *
 * Hasta el 2026-09-22 esto vivia copiado en cinco archivos de 886 y 844 lineas
 * que se diferenciaban en cinco a siete lineas reales: el nombre de la clase,
 * la lista de correo, los asuntos, de donde sale la fecha y cual metodo del DAO
 * trae la gente. Un arreglo habia que hacerlo cinco veces, y ya se veia la
 * deriva -comentarios distintos entre copias, un espacio suelto-.
 *
 * Las cinco clases siguen existiendo con su nombre exacto porque el servidor
 * las llama asi desde el crontab: ahora son un main() de tres lineas que arma
 * su Config y llama aqui.
 */
public class ReporteSemanalHorariosBase {

	/** Lo unico que cambia entre los cinco reportes. */
	public static class Config {
		/** Para los mensajes de consola. */
		public String nombre = "";
		/** Parametro de general.parametros_correo con los destinatarios. */
		public String parametroCorreo = "REPORTEHORAS";
		/** El asunto, sin las fechas: se le pegan al final. */
		public String asuntoPrincipal = "";
		/**
		 * El asunto del segundo correo, el de quien no marco huellero.
		 * En null no se manda: los reportes de externos nunca lo tuvieron,
		 * porque un domiciliario externo no marca huellero.
		 */
		public String asuntoNoUso = null;
		/** true en los reprocesos: la fecha final sale de FECHAREPROCESO. */
		public boolean fechaDesdeReproceso = false;
		/** INTERNOS, INTERNOS_MIOS o EXTERNOS. */
		public String poblacion = INTERNOS;
		/**
		 * Horas de la jornada en una semana CON festivo, que es lo que se le
		 * resta al acumulado para saber cuantas fueron extras.
		 *
		 * Van en la Config y no como constante porque internos y externos tienen
		 * contratos distintos: 37 y 35. Antes cada archivo tenia las dos cifras
		 * mezcladas -una rama con la de su familia y la otra con la de la otra-,
		 * asi que el ULTIMO empleado de cada reporte se calculaba distinto a
		 * todos los demas. Solo se notaba en semanas con festivo y en una sola
		 * persona, por eso duro tanto sin verse.
		 */
		public double topeFestivoAlto = 37;
		/** Lo mismo para salarios por debajo del millon: siempre la mitad. */
		public double topeFestivoBajo = 18.5;
	}

	public static final String INTERNOS = "INTERNOS";
	public static final String INTERNOS_MIOS = "INTERNOS_MIOS";
	public static final String EXTERNOS = "EXTERNOS";

	/** La gente que entra en el reporte, segun la poblacion configurada. */
	private static ArrayList<EmpleadoEvento> obtenerEntradasSalidas(final Config cfg,
			final String desde, final String hasta) {
		if (INTERNOS_MIOS.equals(cfg.poblacion)) {
			return ReporteHorariosDAO.obtenerEntradasSalidasEmpleadosInternosMiosEventos(desde, hasta);
		}
		if (EXTERNOS.equals(cfg.poblacion)) {
			return ReporteHorariosDAO.obtenerEntradasSalidasEmpleadosExternosEventos(desde, hasta);
		}
		return ReporteHorariosDAO.obtenerEntradasSalidasEmpleadosInternosEventos(desde, hasta);
	}

	public static void generar(final Config cfg)
	        
	{
		//Ruta donde generaremos los archivos para envio
		String rutaArchivoGenerado="";
		String rutaArchivoBD = ParametrosDAO.retornarValorAlfanumericoLocal("RUTAARCHIVOTIEMPO");
		String rutaImagenReporte = rutaArchivoBD + "LogoPizzaAmericana.png";
		//Creamos el archivo para el despliegue de la informaci�n
		//Creamos el libro en Excel y la hoja en cuesti�n, definimos los encabezados.
		Workbook workbook = new XSSFWorkbook();

		/*
		 * Los estilos se crean UNA vez y se reusan.
		 *
		 * No es manía: un CellStyle en POI es un objeto del libro, no del texto,
		 * y crear uno por celda revienta el archivo. El formato xlsx admite unos
		 * 64.000 estilos, y este reporte pinta entre 90 y 110 bloques con siete
		 * columnas cada uno. Creando estilos dentro del bucle se llega al tope y
		 * Excel abre el archivo diciendo que esta danado.
		 */
		final Font fuenteNombre = workbook.createFont();
		fuenteNombre.setBold(true);
		fuenteNombre.setFontHeightInPoints((short) 12);
		fuenteNombre.setColor(IndexedColors.DARK_BLUE.getIndex());
		final CellStyle estiloNombre = workbook.createCellStyle();
		estiloNombre.setFont(fuenteNombre);

		final Font fuenteEncabezado = workbook.createFont();
		fuenteEncabezado.setBold(true);
		fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());
		final CellStyle estiloEncabezado = workbook.createCellStyle();
		estiloEncabezado.setFont(fuenteEncabezado);
		estiloEncabezado.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
		estiloEncabezado.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
		estiloEncabezado.setAlignment(HorizontalAlignment.CENTER);
		estiloEncabezado.setBorderBottom(BorderStyle.THIN);

		final Font fuenteTotal = workbook.createFont();
		fuenteTotal.setBold(true);
		final CellStyle estiloTotal = workbook.createCellStyle();
		estiloTotal.setFont(fuenteTotal);
		estiloTotal.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
		estiloTotal.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
		estiloTotal.setBorderTop(BorderStyle.MEDIUM);
		Sheet sheet = workbook.createSheet("RESUMEN TIEMPOS");
		sheet.setColumnWidth(0, 7500);
		sheet.setColumnWidth(1, 4500);
		sheet.setColumnWidth(2, 4500);
		sheet.setColumnWidth(3, 4500);
		sheet.setColumnWidth(4, 4500);
		sheet.setColumnWidth(5, 4500);
		sheet.setColumnWidth(6, 5500);
		String[] headers = new String[]{
	            "NOMBRE EMPLEADO",
	            "FECHA",
	            "DIA",
	            "INGRESO",
	            "SALIDA",
	            "HORAS",
	            "TIENDA"
	        };
		
		
		
		//TRABAJO CON LAS FECHAS///////
		//Recuperamos la fecha actual del sistema con la fecha apertura
				String fechaActual = "";
				//Variables donde manejaremos la fecha anerior con el fin realizar los c�lculos de ventas
				Date datFechaAnterior;
				String fechaAnterior = "";
				//Creamos el objeto calendario
				Calendar calendarioActual = Calendar.getInstance();
				Calendar calendarioComodin = Calendar.getInstance();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				//Obtenemos la fecha Actual
				try
				{
					//Los reprocesos corren como si hoy fuera FECHAREPROCESO. Es la unica
					//diferencia entre un reporte y su reproceso.
					if (cfg.fechaDesdeReproceso) {
						fechaActual = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
					} else {
						fechaActual = dateFormat.format(calendarioActual.getTime());
					}
					//fechaActual = "2020-08-09";
				}catch(Exception exc)
				{
					System.out.println(exc.toString());
				}
				try
				{
					//Al objeto calendario le fijamos la fecha actual del sitema
					calendarioActual.setTime(dateFormat.parse(fechaActual));
					
				}catch(Exception e)
				{
					System.out.println(e.toString());
				}
				//Retormanos el d�a de la semana actual segun la fecha del calendario
				//OJO
				//int diaActual = 1;
				int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
				
				//Domingo
				if(diaActual == 1)
				{
					calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
				}
				else if(diaActual == 2)
				{
					calendarioActual.add(Calendar.DAY_OF_YEAR, -7);
				}
				else if(diaActual == 3)
				{
					//Si es martes se resta uno solo
					calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
				}
				else if(diaActual == 4)
				{
					//Si es miercoles se resta dos
					calendarioActual.add(Calendar.DAY_OF_YEAR, -2);
				}
				else if(diaActual == 5)
				{
					//Si es jueves se resta tres
					calendarioActual.add(Calendar.DAY_OF_YEAR, -3);
				}
				else if(diaActual == 6)
				{
					//Si es viernes se resta cuatro
					calendarioActual.add(Calendar.DAY_OF_YEAR, -4);
				}
				else if(diaActual == 7)
				{
					//Si es sabado se resta cinco
					calendarioActual.add(Calendar.DAY_OF_YEAR, -5);
				}
				//Llevamos a un string la fecha anterior para el c�lculo de la venta
				datFechaAnterior = calendarioActual.getTime();
				fechaAnterior = dateFormat.format(datFechaAnterior);
		///////////////////////////////
		DecimalFormat formatea = new DecimalFormat("###,###.##");
		//En respuesta guardaremos el html que guardar� todo lo que se desplegar� en el correo.
		String respuesta = "";
		//Recuperamos los d�as festivos
		ArrayList<DiaFestivo> festivos = GeneralDAO.obtenerDiasFestivos();
		
		//En este punto vamos a replicar la l�gica para procesar y generar el reporte
		
		//Luego de definidos las fechas crearemos el archivo que en su nombre contiene las fechas
		try
		{
			   rutaArchivoGenerado = rutaArchivoBD  + "ReporteHorasTrabajadas" + "-" + fechaAnterior + "--" + fechaActual + ".xlsx";
			   
			   FileOutputStream fileOut = new FileOutputStream(rutaArchivoGenerado);
			   
			   rutaArchivoGenerado = rutaArchivoGenerado + "%&" + "ReporteHorasTrabajadas" + "-" + fechaAnterior + "--" + fechaActual + ".xlsx";
			   
			   //Creamos los estilos para el encabezado del reporte y para el nombre de la persona que es el segundo nivel
			   //de rompimiento del reporte
			 //Creamos el estilo para el nombre del reporte
				Font whiteFont = workbook.createFont();
	            whiteFont.setColor(IndexedColors.BLUE.index);
	            whiteFont.setFontHeightInPoints((short) 14.00);
	            whiteFont.setBold(true);
	            CellStyle cellheader = workbook.createCellStyle();
	            cellheader.setWrapText(true);
	            cellheader.setFont(whiteFont);
	            cellheader.setAlignment(HorizontalAlignment .CENTER);
	            
	            //Creamos el estilo para la segunda fila de informaci�n
	            Font fontSegFila = workbook.createFont();
	            fontSegFila.setColor(IndexedColors.ORANGE.index);
	            fontSegFila.setFontHeightInPoints((short) 10.00);
	            fontSegFila.setBold(true);
	            CellStyle cellInfoReporte = workbook.createCellStyle();
	            cellInfoReporte.setBorderBottom(BorderStyle.THIN);
	            cellInfoReporte.setBorderTop(BorderStyle.THIN);
	            cellInfoReporte.setBorderLeft(BorderStyle.THIN);
	            cellInfoReporte.setBorderRight(BorderStyle.THIN);
	            cellInfoReporte.setWrapText(true);
	            cellInfoReporte.setFont(fontSegFila);
	            cellInfoReporte.setAlignment(HorizontalAlignment .CENTER);
	            
	            //Creamos el estilo para la informacion del reporte
	            CellStyle styleInfRep = workbook.createCellStyle();
	            styleInfRep.setBorderBottom(BorderStyle.THIN);
	            styleInfRep.setBorderTop(BorderStyle.THIN);
	            styleInfRep.setBorderLeft(BorderStyle.THIN);
	            styleInfRep.setBorderRight(BorderStyle.THIN);
	            styleInfRep.setWrapText(true);
	            
	            
	            //NOMBRE DEL REPORTE
	            Row headerRow = sheet.createRow((short) 0);
	            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$G$1"));
	            Cell cellHeader = headerRow.createCell((short) 0);
	            cellHeader.setCellValue(workbook.getCreationHelper().createRichTextString("REPORTE SEMANAL DE HORAS TRABAJADAS \n" + fechaAnterior + "--" + fechaActual ));
	            headerRow.setHeight((short)1000);
	            cellHeader.setCellStyle(cellheader);
	            
	            //Realizamos la adici�n de la imagen del logo de pizza americana
	            InputStream inputStream = new FileInputStream(rutaImagenReporte);
	            byte[] imageBytes = IOUtils.toByteArray(inputStream);
	            int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
	            //close the input stream
	            //Returns an object that handles instantiating concrete classes
	            CreationHelper helper = workbook.getCreationHelper();
	            //Creates the top-level drawing patriarch.
	            Drawing drawing = sheet.createDrawingPatriarch();
	            //Create an anchor that is attached to the worksheet
	            ClientAnchor anchor = helper.createClientAnchor();
	            //set top-left corner for the image
	            anchor.setDx1(0);
	            anchor.setDy1(0);
	            anchor.setDx2(1023);
	            anchor.setDy2(6000);
	            anchor.setCol1(5);
	            anchor.setRow1(0);
	            anchor.setCol2(5);
	            anchor.setRow2(1);
	            //Creates a picture
	            Picture pict = drawing.createPicture(anchor, pictureIdx);
	            //Reset the image to the original size
	            pict.resize();
			   
			   
			   //Instanciamos la respuesta ArrayList
				ArrayList<String[]> respuestaReporte = new ArrayList();
				//Recuperamos el arreglo con los eventos deberemos reprocesarlos para tener la vista qeu requerimos
				ArrayList<EmpleadoEvento>  repEntradasSalidas = obtenerEntradasSalidas(cfg, fechaAnterior, fechaActual);
				//Variables necesarias para el recorrido
				EmpleadoEvento eventoTemp;
				//Arreglo donde iremos dejando cada fila
				String[] filaTemp = new String[10];
				//Variables que nos permitiran saber si hubo error en la conversi�n de las fechas
				boolean errorInicial = false;
				boolean errorFinal = false;
				//Variables qeu nos permitiran saber en que punto vamos de la formaci�n del registro
				boolean ingreso = false;
				//Salida empezar� prendido dado que iniciamos con uno nuevo
				boolean salida = true;
				//El reporte abarca varios empleados y varios dias. Los indicadores ingreso y
				//salida hay que reiniciarlos en cada cambio de empleado o de fecha: una jornada
				//sin cerrar arrastraba su estado al empleado siguiente o al dia siguiente.
				int idEmpleadoCorte = -1;
				String fechaCorte = "";
				for(int i = 0; i < repEntradasSalidas.size(); i++)
				{
					//Retomamos el evento que vamos a procesar
					eventoTemp = repEntradasSalidas.get(i);
					if(eventoTemp.getId() != idEmpleadoCorte || !fechaCorte.equals(eventoTemp.getFecha()))
					{
						//Se cierra la jornada que quedo abierta antes de pasar al siguiente corte
						if(ingreso && !salida)
						{
							filaTemp[4] = "0";
							filaTemp[5] = "0";
							respuestaReporte.add(filaTemp);
						}
						idEmpleadoCorte = eventoTemp.getId();
						fechaCorte = eventoTemp.getFecha();
						filaTemp = new String[10];
						ingreso = false;
						salida = true;
						errorInicial = false;
						errorFinal = false;
					}
					//Hacemos la verificaci�n de si el evento es de ingreso o de salida
					if(eventoTemp.getTipoEvento().equals(new String("INGRESO")))
					{
						//Esto quiere decir que solo hay un ingreso por lo que llenamos el arreglo
						if(ingreso)
						{
							filaTemp[4] = "0";
							filaTemp[5] = "0";
							respuestaReporte.add(filaTemp);
							filaTemp = new String[10];
							filaTemp[0] = eventoTemp.getNombreEmpleado();
							filaTemp[1] = eventoTemp.getFecha();
							filaTemp[2] = eventoTemp.getDia();
							filaTemp[3] = eventoTemp.getFechaHoraLog();
							filaTemp[6] = Integer.toString(eventoTemp.getIdTienda());
							filaTemp[8] = Integer.toString(eventoTemp.getId());
							filaTemp[9] = Double.toString(eventoTemp.getSalario());
						}if(salida)
						{
							filaTemp = new String[10];
							filaTemp[0] = eventoTemp.getNombreEmpleado();
							filaTemp[1] = eventoTemp.getFecha();
							filaTemp[2] = eventoTemp.getDia();
							filaTemp[3] = eventoTemp.getFechaHoraLog();
							filaTemp[6] = Integer.toString(eventoTemp.getIdTienda());
							filaTemp[8] = Integer.toString(eventoTemp.getId());
							filaTemp[9] = Double.toString(eventoTemp.getSalario());
						}
						ingreso = true;
						salida = false;
					}else if(eventoTemp.getTipoEvento().equals(new String("SALIDA")))
					{
						//Salida sin ingreso abierto: se reporta con cero horas en vez de parsear un nulo
						//que el catch silenciaba, lo que dejaba filas con nombre y fecha vacios.
						if(!ingreso)
						{
							filaTemp[0] = eventoTemp.getNombreEmpleado();
							filaTemp[1] = eventoTemp.getFecha();
							filaTemp[2] = eventoTemp.getDia();
							filaTemp[3] = "";
							filaTemp[4] = eventoTemp.getFechaHoraLog();
							filaTemp[5] = "0";
							filaTemp[6] = Integer.toString(eventoTemp.getIdTienda());
							filaTemp[7] = "0";
							filaTemp[8] = Integer.toString(eventoTemp.getId());
							filaTemp[9] = Double.toString(eventoTemp.getSalario());
							respuestaReporte.add(filaTemp);
							filaTemp = new String[10];
							salida = true;
							continue;
						}
						filaTemp[4] = eventoTemp.getFechaHoraLog();
						//Hacer la resta de tiempos para lo cual formateamos las fechas
						Date fechaFinal = new Date(), fechaInicial = new Date();
						double horas = 0;
						double recargoNocturno = 0;
						//Intentamos la conversi�n de las fechas
						try
						{
							fechaInicial=dateFormatHora.parse(filaTemp[3]);
						}catch(Exception e)
						{
							errorInicial = true;
						}
						try
						{
							fechaFinal=dateFormatHora.parse(filaTemp[4]);
						}catch(Exception e)
						{
							errorFinal = true;
						}
						//Sino se tuvo error en la conversi�n de las fehcas.
				        if(!errorInicial && !errorFinal)
				        {
				        	  //Antes de hacer un c�lculo de las horas, revisaremos y homologaremos el valor de la hora final
				        	  //Con el fin de tomar acci�n sobre las personas que se dan salida muy tarde
				        	  int horaFinal = fechaFinal.getHours();
				        	  //Validamos si es lunes, martes, miercoles, jueves o domingo y si la hora Final es mayor a 23 en cuyo caso se fija en ese valor
				        	  if((filaTemp[2].equals(new String("Lunes")))||(filaTemp[2].equals(new String("Martes")))||(filaTemp[2].equals(new String("Miercoles")))||(filaTemp[2].equals(new String("Jueves")))||(filaTemp[2].equals(new String("Domingo"))))
				        	  {
				        		  //Si la hora final es mayor o igual a 23 o ya se fue para el otro d�a
				        		  if(horaFinal >= 23)
				        		  {
				        			  horaFinal = 23;
				        			  fechaFinal.setHours(23);
				        			  fechaFinal.setMinutes(0);
				        		  }else if(horaFinal >= 0 && horaFinal <= 4)
				        		  {
				        			  //Seguramente se pas� al d�a siguiente, por lo tanto con el objeto calendar
				        			  //restamos un d�a y fijamos la hora  a las 23:00 para los c�lculos
				        			  horaFinal = 23;
				        			  calendarioComodin.setTime(fechaFinal);
				        			  calendarioComodin.add(Calendar.DAY_OF_YEAR, -1);
				        			  fechaFinal = calendarioComodin.getTime();
				        			  fechaFinal.setHours(23);
				        			  fechaFinal.setMinutes(0);
				        		  }
				        	  }else if((filaTemp[2].equals(new String("Viernes")))||(filaTemp[2].equals(new String("Sabado"))))
				        	  {
				        		//Si la hora final es mayor o igual a 23 o ya se fue para el otro d�a
				        		  if((horaFinal >= 0 && horaFinal <= 4))
				        		  {
				        			  horaFinal = 0;
				        			  fechaFinal.setHours(0);
				        			  fechaFinal.setMinutes(0);
				        		  }
				        	  }
				        	  //Realizamos validaciones de la hora inicial y solo es para tiendas
				        	  int horaInicial = fechaInicial.getHours();
				        	  if(eventoTemp.getIdTienda() != 12)
				        	  {
				        		  if((filaTemp[2].equals(new String("Lunes")))||(filaTemp[2].equals(new String("Martes")))||(filaTemp[2].equals(new String("Miercoles")))||(filaTemp[2].equals(new String("Jueves"))))
					        	  {
					        		  if(horaInicial == 15)
					        		  {
					        			  horaInicial = 16;
					        			  fechaInicial.setHours(16);
					        			  fechaInicial.setMinutes(0);
					        		  }
					        	  }else if((filaTemp[2].equals(new String("Sabado")))||(filaTemp[2].equals(new String("Domingo"))))
					        	  {
					        		  if(horaInicial == 11)
					        		  {
					        			  horaInicial = 12;
					        			  fechaInicial.setHours(12);
					        			  fechaInicial.setMinutes(0);
					        		  }
					        	  }else if((filaTemp[2].equals(new String("Viernes"))))
					        	  {
					        		  if(horaInicial == 14)
					        		  {
					        			  horaInicial = 15;
					        			  fechaInicial.setHours(15);
					        			  fechaInicial.setMinutes(0);
					        		  }
					        	  }
				        	  }
				        	  horas = ((fechaFinal.getTime()-fechaInicial.getTime())/1000);
				        	  horas =(horas)/3600;
				        	  //Realizamos modificaciones para llenar el valor de recargo nocturno
				        	  recargoNocturno = 0;
				        	  // Si la hora inicial es mayor a las 7 de la noche, pues entonces el comienzo no es las 7pm
				        	  if(horaInicial >= 19)
				        	  {
				        		  recargoNocturno = ((fechaFinal.getTime()-fechaInicial.getTime())/1000);
				        	  }else
				        	  {
				        		  if((horaFinal >= 19) || (horaFinal >= 0 && horaFinal <= 4))
				        		  {
				        			//yyyy-MM-dd HH:mm:ss
					        		  Date fechaHoraRecargo = dateFormatHora.parse(dateFormat.format(fechaInicial)+ " 19:00:00");
					        		  recargoNocturno = ((fechaFinal.getTime() - fechaHoraRecargo.getTime())/1000);
					        		  recargoNocturno =(recargoNocturno)/3600;
				        		  }	  
				        	  }
				        }
				        //DecimalFormat df = new DecimalFormat("#.00");
				        filaTemp[5] = Double.toString(horas);
				        //La idea con el recargo nocturno es truncarlo
				        filaTemp[7] = Integer.toString((int)recargoNocturno);
						respuestaReporte.add(filaTemp);
						//volvemos a iniciarlizar las banderas de inicio y final
						errorInicial = false;
						errorFinal = false;
						//Prendemos la variable de salida
						salida = true;
						ingreso = false;
					}
				}
				//A la salida del for damos una revisa si no hay salida entonces se agrega al arreglo del resultado
				if(ingreso && !salida)
				{
					filaTemp[4] = "0";
					filaTemp[5] = "0";
					respuestaReporte.add(filaTemp);
				}
			
			
			//Obtenemos la informaci�n consolidada por persona y d�a
			ArrayList reporteHorarios = respuestaReporte;
			ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();

			//Lo PROGRAMADO de todas las personas del periodo, de una sola vez.
			//
			//Es la otra mitad del reporte: horario_trabajado dice lo que la
			//persona marco en el huellero, y horario_planificado lo que se le
			//habia programado. Un total de horas no se puede juzgar sin las dos:
			//40 horas son muchas o pocas segun cuantas se hayan programado.
			//
			//Se trae el mapa completo y no una consulta por empleado porque el
			//reporte recorre entre 90 y 110 personas.
			java.util.HashMap<Integer, HorarioPlanificadoDAO.Programado> programado =
					HorarioPlanificadoDAO.obtenerProgramadoPorEmpleado(fechaAnterior, fechaActual);
			double totalTrabajadoGeneral = 0;
			
			//La primera parte de la l�gica realiza el llenado del arreglo y la segunda realiza el pintado
			// es aqui donde se interviene el pintado
			//Se tendr� un variable que iniciar� en 1 y que controlar� el movimiento de las filas
			int filaActual = 1;
			
			//Comenzamos toda la l�gica para recorrer el arreglo de empleados por fecha y pintar la inforaci�n como lo requerimos
			//Variables que nos permitir�n almacenar el empleado anterior y revisar si est� cambiando con el fin de ir mostrando un camboi
			String empleadoAnterior = "";
			double salarioEmpleadoAnterior = 0;
			double salarioEmpleadoActual = 0;
			int idEmpleadoAnterior = 0;
			String empleadoActual = "";
			int idEmpleadoActual = 0;
			double horas = 0;
			String strHoras = "";
			double acumuladoHoras = 0;
			String tienda = "";
			int idTienda;
			//Creaci�n de variables para apoyar la liquidaci�n de n�mina
			//Para el manejo del c�lculo de las horas de recargo nocturno
			double recargoNocTotal = 0;
			double recargoNoc = 0;
			//Indicador para saber si la semana tiene festivo
			boolean tieneFestivo = false;
			double horasExtrasOrdinarias = 0;
			double horasExtrasDominicales = 0;
			double horasTrabDomingos = 0;
			double horasFestivas = 0;
			double horasExtResiduales = 0;
			String[] fila = new String[9];
			for(int i = 0; i < reporteHorarios.size(); i++)
			{
				fila = (String[]) reporteHorarios.get(i);
				empleadoActual = fila[0];
				idEmpleadoActual = Integer.parseInt(fila[8]);
				salarioEmpleadoActual = Double.parseDouble(fila[9]);
				if(empleadoAnterior.equals(new String("")))
				{
					empleadoAnterior = fila[0];
					salarioEmpleadoAnterior = Double.parseDouble(fila[9]);
					idEmpleadoAnterior = Integer.parseInt(fila[8]);
					//La columna NOMBRE se quita del correo: repetia el mismo
					//nombre en cada una de las siete filas del bloque, y ya esta
					//en el titulo. En el Excel SI se deja, porque alla sirve para
					//filtrar y ordenar.
					respuesta = respuesta + CorreoHtml.tituloPersona(empleadoActual);
					respuesta = respuesta + CorreoHtml.abrirTabla("", "Fecha", "Dia", "Ingreso", "Salida", "Horas", "Tienda");
							//Damos un salto adicional de separaci�n 
							filaActual++;
							//Creamos Encabezado del reporte
							Row nombrePersona = sheet.createRow(filaActual);
							Cell cellFila = nombrePersona.createCell((short) 0);
							cellFila.setCellValue(empleadoActual);							cellFila.setCellStyle(estiloNombre);
							filaActual++;
							//Continuamos con los encabezados
							Row encabezados = sheet.createRow(filaActual);
							Cell cellFilaEncabezado = encabezados.createCell((short) 0);
							cellFilaEncabezado.setCellValue("NOMBRE EMPLEADO");
							cellFilaEncabezado = encabezados.createCell((short) 1);
							cellFilaEncabezado.setCellValue("FECHA");
							cellFilaEncabezado = encabezados.createCell((short) 2);
							cellFilaEncabezado.setCellValue("DIA");
							cellFilaEncabezado = encabezados.createCell((short) 3);
							cellFilaEncabezado.setCellValue("INGRESO");
							cellFilaEncabezado = encabezados.createCell((short) 4);
							cellFilaEncabezado.setCellValue("SALIDA");
							cellFilaEncabezado = encabezados.createCell((short) 5);
							cellFilaEncabezado.setCellValue("HORAS");
							cellFilaEncabezado = encabezados.createCell((short) 6);
							cellFilaEncabezado.setCellValue("TIENDA");							//Los siete encabezados con el mismo estilo, en un solo sitio.							for (int cc = 0; cc <= 6; cc++) { encabezados.getCell(cc).setCellStyle(estiloEncabezado); }
							filaActual++;
				}
				
				if(!empleadoAnterior.equals(empleadoActual))
				{
					respuesta = respuesta + CorreoHtml.filaTotal("TOTAL", "", "", "", formatea.format(acumuladoHoras), "") + CorreoHtml.cerrarTabla();
					
					//Lo programado de ESTE empleado, al lado de lo que trabajo.
					//La diferencia se calcula trabajado menos programado, asi que
					//en positivo trabajo de mas y en negativo de menos, que es
					//como lo lee quien revisa la nomina.
					HorarioPlanificadoDAO.Programado progAnt =
							HorarioPlanificadoDAO.de(programado, idEmpleadoAnterior);
					totalTrabajadoGeneral = totalTrabajadoGeneral + acumuladoHoras;
					//La diferencia se pinta en rojo cuando se trabajo MENOS de lo programado,
					//que es lo que hay que mirar. Trabajar de mas ya se ve en las extras.
					respuesta = respuesta + "<div style=\"margin:-12px 0 8px;\">"
						+ CorreoHtml.dato("Trabajadas", formatea.format(acumuladoHoras), null)
						+ CorreoHtml.dato("Programadas", formatea.format(progAnt.horas), null)
						+ CorreoHtml.dato("Diferencia", formatea.format(acumuladoHoras - progAnt.horas),
								(acumuladoHoras - progAnt.horas) < 0 ? "#c21c1f" : "#16704f")
						+ CorreoHtml.dato("Turnos", String.valueOf(progAnt.turnos), null)
						+ CorreoHtml.dato("Descansos", String.valueOf(progAnt.diasSinTurno), null)
						+ "</div>";

					//Insertamos el pie
					Row pie = sheet.createRow(filaActual);
					Cell cellFilaPie = pie.createCell((short) 0);
					cellFilaPie.setCellValue("TOTAL HORAS " +  formatea.format(acumuladoHoras));					cellFilaPie.setCellStyle(estiloTotal);
					cellFilaPie = pie.createCell((short) 3);
					cellFilaPie.setCellValue("PROGRAMADAS " + formatea.format(progAnt.horas));					cellFilaPie.setCellStyle(estiloTotal);
					cellFilaPie = pie.createCell((short) 5);
					cellFilaPie.setCellValue("DIFERENCIA " + formatea.format(acumuladoHoras - progAnt.horas));					cellFilaPie.setCellStyle(estiloTotal);
					//En este punto realizamos los c�lculos
					if(tieneFestivo)
					{
						if(salarioEmpleadoAnterior >= 1000000)
						{
							horasExtResiduales = acumuladoHoras - horasFestivas - cfg.topeFestivoAlto;	
						}else
						{
							horasExtResiduales = acumuladoHoras - horasFestivas - cfg.topeFestivoBajo;	
						}
						 
					}else
					{
						if(salarioEmpleadoAnterior >= 1000000)
						{
							horasExtResiduales = acumuladoHoras - horasFestivas - 42;	
						}else
						{
							horasExtResiduales = acumuladoHoras - horasFestivas - 21;	
						}
							
					}
					//El tratamiento no es diferencial en esta parte
					horasExtrasDominicales = horasTrabDomingos - 8;
					//Realizamos una validaci�n adicional en donde si las horas extras dominicales son mayores a las
					//horas extras Residuales, entonces lo igualamos
					if(horasExtrasDominicales > horasExtResiduales)
					{
						horasExtrasDominicales = horasExtResiduales;
					}
					if(horasExtrasDominicales  < 0)
					{
						horasExtrasDominicales = 0;
					}
					//Recalculamos la horas extras residuales
					horasExtResiduales = horasExtResiduales - horasExtrasDominicales;
					//Verificamos que falten horas por revisar
					if(horasExtResiduales > 0)
					{
						horasExtrasOrdinarias = horasExtResiduales;
					}else
					{
						horasExtrasOrdinarias = 0;
					}
					//Realizamos la inclusi�n de la informaci�n en la tabla HTML
					respuesta = respuesta + "<div style=\"margin:-12px 0 8px;\">"
						+ CorreoHtml.dato("Extras ordinarias", formatea.format(horasExtrasOrdinarias), null)
						+ CorreoHtml.dato("Extras dominicales", formatea.format(horasExtrasDominicales), null)
						+ CorreoHtml.dato("Festivas", formatea.format(horasFestivas), null)
						+ CorreoHtml.dato("Recargo nocturno", formatea.format(recargoNocTotal), null)
						+ "</div>";
					//Insertamos el resumen
					HorarioResumen horarioResumen = new HorarioResumen(0, idEmpleadoAnterior, acumuladoHoras, horasExtrasOrdinarias,horasExtrasDominicales, horasFestivas, recargoNocTotal,fechaAnterior,fechaActual );
					HorarioResumenDAO.insertarHorarioResumen(horarioResumen);
					
					filaActual = filaActual + 2;
					//Aqui tendremos un gran doble salto para pasar de empleado
					respuesta = respuesta + CorreoHtml.tituloPersona(empleadoActual);
					//Creamos Encabezado del reporte
					Row nombrePersona = sheet.createRow(filaActual);
					Cell cellFila = nombrePersona.createCell((short) 0);
					cellFila.setCellValue(empleadoActual);					cellFila.setCellStyle(estiloNombre);
					filaActual++;
					respuesta = respuesta + CorreoHtml.abrirTabla("", "Fecha", "Dia", "Ingreso", "Salida", "Horas", "Tienda");
					//Continuamos con los encabezados
					Row encabezados = sheet.createRow(filaActual);
					Cell cellFilaEncabezado = encabezados.createCell((short) 0);
					cellFilaEncabezado.setCellValue("NOMBRE EMPLEADO");
					cellFilaEncabezado = encabezados.createCell((short) 1);
					cellFilaEncabezado.setCellValue("FECHA");
					cellFilaEncabezado = encabezados.createCell((short) 2);
					cellFilaEncabezado.setCellValue("DIA");
					cellFilaEncabezado = encabezados.createCell((short) 3);
					cellFilaEncabezado.setCellValue("INGRESO");
					cellFilaEncabezado = encabezados.createCell((short) 4);
					cellFilaEncabezado.setCellValue("SALIDA");
					cellFilaEncabezado = encabezados.createCell((short) 5);
					cellFilaEncabezado.setCellValue("HORAS");
					cellFilaEncabezado = encabezados.createCell((short) 6);
					cellFilaEncabezado.setCellValue("TIENDA");					//Los siete encabezados con el mismo estilo, en un solo sitio.					for (int cc = 0; cc <= 6; cc++) { encabezados.getCell(cc).setCellStyle(estiloEncabezado); }
					filaActual++;
					acumuladoHoras = 0;
					//En este punto realizamos el clareo de las variables
					recargoNoc = 0;
					recargoNocTotal = 0;
					tieneFestivo = false;
					horasExtrasOrdinarias = 0;
					horasExtrasDominicales = 0;
					horasTrabDomingos = 0;
					horasFestivas = 0;
					horasExtResiduales = 0;
				}
				
				//Debemos de cambiar de minutos a horas y debemos de consultar la tienda
				try
				{
					
					horas = Double.parseDouble(fila[5]);
					DecimalFormat df = new DecimalFormat("#.00");
					strHoras = df.format(horas);
				}catch(Exception e)
				{
					horas = 0;
				}
				acumuladoHoras = acumuladoHoras + horas;
				//Realizamos la conversi�n de las horas de recargo nocturna

				try
				{
					
					recargoNoc = Double.parseDouble(fila[7]);
				}catch(Exception e)
				{
					recargoNoc = 0;
				}
				recargoNocTotal = recargoNocTotal + recargoNoc;
				//Validaremos si el d�a es domingo
				if(fila[2].equals(new String("Domingo")))
				{
					horasTrabDomingos = horasTrabDomingos + horas;
				}
				//Validamos si la fecha es festivo
				for(int z = 0; z < festivos.size(); z++)
				{
					DiaFestivo festTemp = festivos.get(z);
					if(festTemp.getFechaFestiva().equals(fila[1]))
					{
						tieneFestivo = true;
						horasFestivas = horasFestivas + horas;
						break;
					}
				}
				
				
				
				//Revisamos el tema de la tienda
				try {
					idTienda = Integer.parseInt(fila[6]);
				}catch(Exception e)
				{
					idTienda = 0;
				}
				if(idTienda > 0)
				{
					for(int j = 0; j < tiendas.size(); j++)
					{
						Tienda tiendaTemp = tiendas.get(j);
						if (tiendaTemp.getIdTienda() == idTienda)
						{
							tienda = tiendaTemp.getNombreTienda();
							break;
						}
					}
				}else
				{
					tienda = "No Identificada";
				}
				//Realizamos el pintado de la fila
				respuesta = respuesta + CorreoHtml.fila(fila[1], fila[2], fila[3], fila[4], strHoras, tienda);
				//Realizamos inserci�n de la tabla
				HorarioTrabajado horario = new HorarioTrabajado(0, Integer.parseInt(fila[8]),fila[1], fila[2], fila[3], fila[4],horas,idTienda );
				HorarioTrabajadoDAO.insertarHorarioTrabajado(horario);
				
				//Realizamos pintado de la fila en el Excel de una fila de datos
				Row encabezados = sheet.createRow(filaActual);
				Cell cellFillaDatos = encabezados.createCell((short) 0);
				cellFillaDatos.setCellValue(fila[0]);
				cellFillaDatos = encabezados.createCell((short) 1);
				cellFillaDatos.setCellValue(fila[1]);
				cellFillaDatos = encabezados.createCell((short) 2);
				cellFillaDatos.setCellValue(fila[2]);
				cellFillaDatos = encabezados.createCell((short) 3);
				cellFillaDatos.setCellValue(fila[3]);
				cellFillaDatos = encabezados.createCell((short) 4);
				cellFillaDatos.setCellValue(fila[4]);
				cellFillaDatos = encabezados.createCell((short) 5);
				cellFillaDatos.setCellValue(strHoras);
				cellFillaDatos = encabezados.createCell((short) 6);
				cellFillaDatos.setCellValue(tienda);
				filaActual++;
				//Al final del procesamiento decimos que el empleadoAnterior es el actual
				empleadoAnterior = empleadoActual;
				salarioEmpleadoAnterior = salarioEmpleadoActual;
				idEmpleadoAnterior = idEmpleadoActual;
			}
			respuesta = respuesta + CorreoHtml.filaTotal("TOTAL", "", "", "", formatea.format(acumuladoHoras), "") + CorreoHtml.cerrarTabla();
			//En este punto realizamos los c�lculos
			if(tieneFestivo)
			{
				if(salarioEmpleadoAnterior >= 1000000)
				{
					horasExtResiduales = acumuladoHoras - horasFestivas - cfg.topeFestivoAlto;	
				}else
				{
					horasExtResiduales = acumuladoHoras - horasFestivas - cfg.topeFestivoBajo;	
				}
				 
			}else
			{
				if(salarioEmpleadoAnterior >= 1000000)
				{
					horasExtResiduales = acumuladoHoras - horasFestivas - 42;	
				}else
				{
					horasExtResiduales = acumuladoHoras - horasFestivas - 21;	
				}
					
			}
			//El tratamiento no es diferencial en esta parte
			horasExtrasDominicales = horasTrabDomingos - 8;
			//Realizamos una validaci�n adicional en donde si las horas extras dominicales son mayores a las
			//horas extras Residuales, entonces lo igualamos
			if(horasExtrasDominicales > horasExtResiduales)
			{
				horasExtrasDominicales = horasExtResiduales;
			}
			if(horasExtrasDominicales  < 0)
			{
				horasExtrasDominicales = 0;
			}
			//Recalculamos la horas extras residuales
			horasExtResiduales = horasExtResiduales - horasExtrasDominicales;
			//Verificamos que falten horas por revisar
			if(horasExtResiduales > 0)
			{
				horasExtrasOrdinarias = horasExtResiduales;
			}else
			{
				horasExtrasOrdinarias = 0;
			}
			//Realizamos la inclusi�n de la informaci�n en la tabla HTML
			respuesta = respuesta + "<div style=\"margin:-12px 0 8px;\">"
				+ CorreoHtml.dato("Extras ordinarias", formatea.format(horasExtrasOrdinarias), null)
				+ CorreoHtml.dato("Extras dominicales", formatea.format(horasExtrasDominicales), null)
				+ CorreoHtml.dato("Festivas", formatea.format(horasFestivas), null)
				+ CorreoHtml.dato("Recargo nocturno", formatea.format(recargoNocTotal), null)
				+ "</div>";
			//Insertamos el resumen del �ltimo
			HorarioResumen horarioResumen = new HorarioResumen(0, Integer.parseInt(fila[8]), acumuladoHoras, horasExtrasOrdinarias,horasExtrasDominicales, horasFestivas, recargoNocTotal,fechaAnterior,fechaActual );
			HorarioResumenDAO.insertarHorarioResumen(horarioResumen);
			//Insertamos el pie
			HorarioPlanificadoDAO.Programado progUlt =
					HorarioPlanificadoDAO.de(programado, idEmpleadoAnterior);
			totalTrabajadoGeneral = totalTrabajadoGeneral + acumuladoHoras;
			//La diferencia se pinta en rojo cuando se trabajo MENOS de lo programado,
			//que es lo que hay que mirar. Trabajar de mas ya se ve en las extras.
			respuesta = respuesta + "<div style=\"margin:-12px 0 8px;\">"
				+ CorreoHtml.dato("Trabajadas", formatea.format(acumuladoHoras), null)
				+ CorreoHtml.dato("Programadas", formatea.format(progUlt.horas), null)
				+ CorreoHtml.dato("Diferencia", formatea.format(acumuladoHoras - progUlt.horas),
						(acumuladoHoras - progUlt.horas) < 0 ? "#c21c1f" : "#16704f")
				+ CorreoHtml.dato("Turnos", String.valueOf(progUlt.turnos), null)
				+ CorreoHtml.dato("Descansos", String.valueOf(progUlt.diasSinTurno), null)
				+ "</div>";

			Row pie = sheet.createRow(filaActual);
			Cell cellFilaPie = pie.createCell((short) 0);
			cellFilaPie.setCellValue("TOTAL HORAS " +  formatea.format(acumuladoHoras));			cellFilaPie.setCellStyle(estiloTotal);
			cellFilaPie = pie.createCell((short) 3);
			cellFilaPie.setCellValue("PROGRAMADAS " + formatea.format(progUlt.horas));			cellFilaPie.setCellStyle(estiloTotal);
			cellFilaPie = pie.createCell((short) 5);
			cellFilaPie.setCellValue("DIFERENCIA " + formatea.format(acumuladoHoras - progUlt.horas));			cellFilaPie.setCellStyle(estiloTotal);
			filaActual = filaActual + 2;

			/*
			 * El total general, que hasta hoy no existia.
			 *
			 * El reporte reinicia acumuladoHoras en cada empleado, asi que quien
			 * queria saber cuantas horas trabajo la cadena en la semana tenia que
			 * sumar a mano los noventa y pico de bloques. Aqui queda de una, y al
			 * lado lo programado: esa comparacion es justamente lo que permite
			 * decir si se trabajo de mas o de menos frente a lo planeado.
			 */
			HorarioPlanificadoDAO.Programado progTotal = HorarioPlanificadoDAO.total(programado);
			respuesta = respuesta + CorreoHtml.abrirTabla("Total general de la semana", "Concepto", "Valor")
					+ CorreoHtml.fila("Horas trabajadas", formatea.format(totalTrabajadoGeneral))
					+ CorreoHtml.fila("Horas programadas", formatea.format(progTotal.horas))
					+ CorreoHtml.fila("Turnos programados", String.valueOf(progTotal.turnos))
					+ CorreoHtml.fila("Dias de descanso o vacaciones", String.valueOf(progTotal.diasSinTurno))
					+ CorreoHtml.filaTotal("Diferencia",
							formatea.format(totalTrabajadoGeneral - progTotal.horas))
					+ CorreoHtml.cerrarTabla();

			Row totalGeneral = sheet.createRow(filaActual);
			Cell celTot = totalGeneral.createCell((short) 0);
			celTot.setCellValue("TOTAL GENERAL DE LA SEMANA");			celTot.setCellStyle(estiloTotal);
			filaActual++;
			totalGeneral = sheet.createRow(filaActual);
			celTot = totalGeneral.createCell((short) 0);
			celTot.setCellValue("HORAS TRABAJADAS");			celTot.setCellStyle(estiloTotal);
			celTot = totalGeneral.createCell((short) 1);
			celTot.setCellValue(formatea.format(totalTrabajadoGeneral));			celTot.setCellStyle(estiloTotal);
			filaActual++;
			totalGeneral = sheet.createRow(filaActual);
			celTot = totalGeneral.createCell((short) 0);
			celTot.setCellValue("HORAS PROGRAMADAS");			celTot.setCellStyle(estiloTotal);
			celTot = totalGeneral.createCell((short) 1);
			celTot.setCellValue(formatea.format(progTotal.horas));			celTot.setCellStyle(estiloTotal);
			filaActual++;
			totalGeneral = sheet.createRow(filaActual);
			celTot = totalGeneral.createCell((short) 0);
			celTot.setCellValue("DIFERENCIA");			celTot.setCellStyle(estiloTotal);
			celTot = totalGeneral.createCell((short) 1);
			celTot.setCellValue(formatea.format(totalTrabajadoGeneral - progTotal.horas));			celTot.setCellStyle(estiloTotal);
			filaActual = filaActual + 2;
			
			//En esta parte termina la generaci�n del correo
			workbook.write(fileOut);
			fileOut.close();
			
			//Buscamos la manera de enviar el correo 
			String[] rutasArchivos = new String[1];
			rutasArchivos[0] = rutaArchivoGenerado;
			
			//Recuperar la lista de distribuci�n para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro(cfg.parametroCorreo);
			Date fecha = new Date();
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto(cfg.asuntoPrincipal + " DE " + fechaAnterior + " HASTA " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			//Anexamos el archivo generado
			correo.setRutasArchivos(rutasArchivos);
			//El encabezado y el pie se ponen aqui y no al armar respuesta, para
			//no tener que arrastrar las fechas hasta el comienzo del recorrido.
			correo.setMensaje(CorreoHtml.abrir("Cumplimiento de horarios",
					//Sin entidades HTML aqui: el subtitulo pasa por h(), que escapa el
					//ampersand y las dejaria a la vista como texto.
					cfg.nombre + " - semana del " + fechaAnterior + " al " + fechaActual)
					+ respuesta
					+ CorreoHtml.cerrar("El detalle completo va en el archivo adjunto."
							+ " Generado automaticamente por Servicios Pizza Americana."));
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTMLAnexo();

			//El segundo correo, el de quien no marco el huellero, solo lo mandan los
			//reportes de personal interno. Los de externos nunca lo tuvieron, y con
			//razon: un domiciliario externo no marca huellero, asi que ese listado
			//saldria con todo el mundo adentro y no serviria para nada.
			if (cfg.asuntoNoUso != null) {
			//Generamos otro correo con el fin de revisar las personas que no usaron biometria dentro de la semana que acaba de finalizar
			respuesta = "";
			respuesta = respuesta + CorreoHtml.abrir("No registro de huella dactilar",
					"Semana del " + fechaAnterior + " al " + fechaActual);
			respuesta = respuesta + CorreoHtml.abrirTabla("Momentos sin marcacion",
					"Nombre", "Fecha", "Dia", "Evento", "Tienda");
			ArrayList reporteNoUso = ReporteHorariosDAO.obtenerReporteNoUsoHuellero(fechaAnterior, fechaActual);
			for(int i = 0; i < reporteNoUso.size(); i++)
			{
				fila = (String[])reporteNoUso.get(i);
				try {
					idTienda = Integer.parseInt(fila[4]);
				}catch(Exception e)
				{
					idTienda = 0;
				}
				if(idTienda > 0)
				{
					for(int j = 0; j < tiendas.size(); j++)
					{
						Tienda tiendaTemp = tiendas.get(j);
						if (tiendaTemp.getIdTienda() == idTienda)
						{
							tienda = tiendaTemp.getNombreTienda();
							break;
						}
					}
				}else
				{
					tienda = "No Identificada";
				}
				respuesta = respuesta + CorreoHtml.fila(fila[0], fila[1], fila[2], fila[3], tienda);
			}
			correo.setAsunto(cfg.asuntoNoUso + " DE " + fechaAnterior + " HASTA " + fechaActual);
			correo.setMensaje(respuesta + CorreoHtml.cerrarTabla()
					+ CorreoHtml.cerrar("Estas son las marcaciones que el sistema esperaba y no encontro."
							+ " Generado automaticamente por Servicios Pizza Americana."));
			contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
			}
		}catch(Exception e)
		{
			System.out.println("problemas en la generacion del archivo " + e.toString() + e.getMessage() + e.getStackTrace().toString() );
		}
	}
	
	
	
	
	
	
	
}

