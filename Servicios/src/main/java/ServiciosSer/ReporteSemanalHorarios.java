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

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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

public class ReporteSemanalHorarios {
	
			
	public static void main( String[] args )
	        
	{
		//Ruta donde generaremos los archivos para envio
		String rutaArchivoGenerado="";
		String rutaArchivoBD = ParametrosDAO.retornarValorAlfanumericoLocal("RUTAARCHIVOTIEMPO");
		String rutaImagenReporte = rutaArchivoBD + "LogoPizzaAmericana.png";
		//Creamos el archivo para el despliegue de la información
		//Creamos el libro en Excel y la hoja en cuestión, definimos los encabezados.
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("RESUMEN TIEMPOS");
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
				//Variables donde manejaremos la fecha anerior con el fin realizar los cálculos de ventas
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
					//OJO
					fechaActual = dateFormat.format(calendarioActual.getTime());
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
				//Retormanos el día de la semana actual segun la fecha del calendario
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
				//Llevamos a un string la fecha anterior para el cálculo de la venta
				datFechaAnterior = calendarioActual.getTime();
				fechaAnterior = dateFormat.format(datFechaAnterior);
		///////////////////////////////
		DecimalFormat formatea = new DecimalFormat("###,###.##");
		//En respuesta guardaremos el html que guardará todo lo que se desplegará en el correo.
		String respuesta = "";
		//Recuperamos los días festivos
		ArrayList<DiaFestivo> festivos = GeneralDAO.obtenerDiasFestivos();
		
		//En este punto vamos a replicar la lógica para procesar y generar el reporte
		
		//Luego de definidos las fechas crearemos el archivo que en su nombre contiene las fechas
		try
		{
			   rutaArchivoGenerado = rutaArchivoBD  + "ReporteHorasTrabajadas" + "-" + fechaAnterior + "--" + fechaActual +".xls";
			   
			   FileOutputStream fileOut = new FileOutputStream(rutaArchivoGenerado);
			   
			   rutaArchivoGenerado = rutaArchivoGenerado + "%&" + "ReporteHorasTrabajadas" + "-" + fechaAnterior + "--" + fechaActual +".xls";
			   
			   //Creamos los estilos para el encabezado del reporte y para el nombre de la persona que es el segundo nivel
			   //de rompimiento del reporte
			 //Creamos el estilo para el nombre del reporte
				Font whiteFont = workbook.createFont();
	            whiteFont.setColor(IndexedColors.BLUE.index);
	            whiteFont.setFontHeightInPoints((short) 14.00);
	            whiteFont.setBold(true);
	            HSSFCellStyle cellheader = workbook.createCellStyle();
	            cellheader.setWrapText(true);
	            cellheader.setFont(whiteFont);
	            cellheader.setAlignment(HorizontalAlignment .CENTER);
	            
	            //Creamos el estilo para la segunda fila de información
	            Font fontSegFila = workbook.createFont();
	            fontSegFila.setColor(IndexedColors.ORANGE.index);
	            fontSegFila.setFontHeightInPoints((short) 10.00);
	            fontSegFila.setBold(true);
	            HSSFCellStyle cellInfoReporte = workbook.createCellStyle();
	            cellInfoReporte.setBorderBottom(BorderStyle.THIN);
	            cellInfoReporte.setBorderTop(BorderStyle.THIN);
	            cellInfoReporte.setBorderLeft(BorderStyle.THIN);
	            cellInfoReporte.setBorderRight(BorderStyle.THIN);
	            cellInfoReporte.setWrapText(true);
	            cellInfoReporte.setFont(fontSegFila);
	            cellInfoReporte.setAlignment(HorizontalAlignment .CENTER);
	            
	            //Creamos el estilo para la informacion del reporte
	            HSSFCellStyle styleInfRep = workbook.createCellStyle();
	            styleInfRep.setBorderBottom(BorderStyle.THIN);
	            styleInfRep.setBorderTop(BorderStyle.THIN);
	            styleInfRep.setBorderLeft(BorderStyle.THIN);
	            styleInfRep.setBorderRight(BorderStyle.THIN);
	            styleInfRep.setWrapText(true);
	            
	            
	            //NOMBRE DEL REPORTE
	            HSSFRow headerRow = sheet.createRow((short) 0);
	            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$G$1"));
	            Cell cellHeader = headerRow.createCell((short) 0);
	            cellHeader.setCellValue(new HSSFRichTextString("REPORTE SEMANAL DE HORAS TRABAJADAS \n" + fechaAnterior + "--" + fechaActual ));
	            headerRow.setHeight((short)1000);
	            cellHeader.setCellStyle(cellheader);
	            
	            //Realizamos la adición de la imagen del logo de pizza americana
	            InputStream inputStream = new FileInputStream(rutaImagenReporte);
	            byte[] imageBytes = IOUtils.toByteArray(inputStream);
	            int pictureIdx = workbook.addPicture(imageBytes, workbook.PICTURE_TYPE_PNG);
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
				ArrayList<EmpleadoEvento>  repEntradasSalidas = ReporteHorariosDAO.obtenerEntradasSalidasEmpleadosInternosEventos(fechaAnterior,fechaActual);
				//Variables necesarias para el recorrido
				EmpleadoEvento eventoTemp;
				//Arreglo donde iremos dejando cada fila
				String[] filaTemp = new String[10];
				//Variables que nos permitiran saber si hubo error en la conversión de las fechas
				boolean errorInicial = false;
				boolean errorFinal = false;
				//Variables qeu nos permitiran saber en que punto vamos de la formación del registro
				boolean ingreso = false;
				//Salida empezará prendido dado que iniciamos con uno nuevo
				boolean salida = true;
				for(int i = 0; i < repEntradasSalidas.size(); i++)
				{
					//Retomamos el evento que vamos a procesar
					eventoTemp = repEntradasSalidas.get(i);
					//Hacemos la verificación de si el evento es de ingreso o de salida
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
						filaTemp[4] = eventoTemp.getFechaHoraLog();
						//Hacer la resta de tiempos para lo cual formateamos las fechas
						Date fechaFinal = new Date(), fechaInicial = new Date();
						double horas = 0;
						double recargoNocturno = 0;
						//Intentamos la conversión de las fechas
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
						//Sino se tuvo error en la conversión de las fehcas.
				        if(!errorInicial && !errorFinal)
				        {
				        	  //Antes de hacer un cálculo de las horas, revisaremos y homologaremos el valor de la hora final
				        	  //Con el fin de tomar acción sobre las personas que se dan salida muy tarde
				        	  int horaFinal = fechaFinal.getHours();
				        	  //Validamos si es lunes, martes, miercoles, jueves o domingo y si la hora Final es mayor a 23 en cuyo caso se fija en ese valor
				        	  if((filaTemp[2].equals(new String("Lunes")))||(filaTemp[2].equals(new String("Martes")))||(filaTemp[2].equals(new String("Miercoles")))||(filaTemp[2].equals(new String("Jueves")))||(filaTemp[2].equals(new String("Domingo"))))
				        	  {
				        		  //Si la hora final es mayor o igual a 23 o ya se fue para el otro día
				        		  if(horaFinal >= 23)
				        		  {
				        			  horaFinal = 23;
				        			  fechaFinal.setHours(23);
				        			  fechaFinal.setMinutes(0);
				        		  }else if(horaFinal >= 0 && horaFinal <= 4)
				        		  {
				        			  //Seguramente se pasó al día siguiente, por lo tanto con el objeto calendar
				        			  //restamos un día y fijamos la hora  a las 23:00 para los cálculos
				        			  horaFinal = 23;
				        			  calendarioComodin.setTime(fechaFinal);
				        			  calendarioComodin.add(Calendar.DAY_OF_YEAR, -1);
				        			  fechaFinal = calendarioComodin.getTime();
				        			  fechaFinal.setHours(23);
				        			  fechaFinal.setMinutes(0);
				        		  }
				        	  }else if((filaTemp[2].equals(new String("Viernes")))||(filaTemp[2].equals(new String("Sabado"))))
				        	  {
				        		//Si la hora final es mayor o igual a 23 o ya se fue para el otro día
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
				        	  // Si la hora inicial es mayor a las 9 de la noche, pues entonces el comienzo no es las 21
				        	  if(horaInicial >= 21)
				        	  {
				        		  recargoNocturno = ((fechaFinal.getTime()-fechaInicial.getTime())/1000);
				        	  }else
				        	  {
				        		  if((horaFinal >= 21) || (horaFinal >= 0 && horaFinal <= 4))
				        		  {
				        			//yyyy-MM-dd HH:mm:ss
					        		  Date fechaHoraRecargo = dateFormatHora.parse(dateFormat.format(fechaInicial)+ " 21:00:00");
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
			
			
			//Obtenemos la información consolidada por persona y día
			ArrayList reporteHorarios = respuestaReporte;
			ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
			
			//La primera parte de la lógica realiza el llenado del arreglo y la segunda realiza el pintado
			// es aqui donde se interviene el pintado
			//Se tendrá un variable que iniciará en 1 y que controlará el movimiento de las filas
			int filaActual = 1;
			
			//Comenzamos toda la lógica para recorrer el arreglo de empleados por fecha y pintar la inforación como lo requerimos
			//Variables que nos permitirán almacenar el empleado anterior y revisar si está cambiando con el fin de ir mostrando un camboi
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
			//Creación de variables para apoyar la liquidación de nómina
			//Para el manejo del cálculo de las horas de recargo nocturno
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
					respuesta = respuesta + "<table WIDTH='400' border='2'> <TH COLSPAN='7'> " + empleadoActual  + "</TH> </tr>";
					respuesta = respuesta + "<tr>"
							+  "<td width='120' nowrap><strong>NOMBRE</strong></td>"
							+  "<td width='50' nowrap><strong>FECHA</strong></td>"
							+  "<td width='50' nowrap><strong>DIA</strong></td>"
							+  "<td width='50' nowrap><strong>INGRESO</strong></td>"
							+  "<td width='50' nowrap><strong>SALIDA</strong></td>"
							+  "<td width='40' nowrap><strong>HORAS</strong></td>"
							+  "<td width='40' nowrap><strong>TIENDA</strong></td>"
							+  "</tr>";
							//Damos un salto adicional de separación 
							filaActual++;
							//Creamos Encabezado del reporte
							HSSFRow nombrePersona = sheet.createRow(filaActual);
							Cell cellFila = nombrePersona.createCell((short) 0);
							cellFila.setCellValue(empleadoActual);
							filaActual++;
							//Continuamos con los encabezados
							HSSFRow encabezados = sheet.createRow(filaActual);
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
							cellFilaEncabezado.setCellValue("TIENDA");
							filaActual++;
				}
				
				if(!empleadoAnterior.equals(empleadoActual))
				{
					respuesta = respuesta + "<tr> <td COLSPAN='7' width='400' nowrap><strong>TOTAL HORAS " + formatea.format(acumuladoHoras) + "</strong></td> </tr>";
					
					//Insertamos el pie
					HSSFRow pie = sheet.createRow(filaActual);
					Cell cellFilaPie = pie.createCell((short) 0);
					cellFilaPie.setCellValue("TOTAL HORAS " +  acumuladoHoras);
					//En este punto realizamos los cálculos
					if(tieneFestivo)
					{
						if(salarioEmpleadoAnterior >= 1000000)
						{
							horasExtResiduales = acumuladoHoras - horasFestivas - 39;	
						}else
						{
							horasExtResiduales = acumuladoHoras - horasFestivas - 19.5;	
						}
						 
					}else
					{
						if(salarioEmpleadoAnterior >= 1000000)
						{
							horasExtResiduales = acumuladoHoras - horasFestivas - 47;	
						}else
						{
							horasExtResiduales = acumuladoHoras - horasFestivas - 23.5;	
						}
							
					}
					//El tratamiento no es diferencial en esta parte
					horasExtrasDominicales = horasTrabDomingos - 8;
					//Realizamos una validación adicional en donde si las horas extras dominicales son mayores a las
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
					//Realizamos la inclusión de la información en la tabla HTML
					respuesta = respuesta + "<tr> <td COLSPAN='7' width='400' nowrap><strong>HORAS EXTRAS ORD " + formatea.format(horasExtrasOrdinarias) + "</strong></td> </tr>";
					respuesta = respuesta + "<tr> <td COLSPAN='7' width='400' nowrap><strong>HORAS EXTRAS DOMI " + formatea.format(horasExtrasDominicales) + "</strong></td> </tr>";
					respuesta = respuesta + "<tr> <td COLSPAN='7' width='400' nowrap><strong>HORAS FESTIVA " + formatea.format(horasFestivas) + "</strong></td> </tr>";
					respuesta = respuesta + "<tr> <td COLSPAN='7' width='400' nowrap><strong>HORAS RECARGO NOCTURNO " + formatea.format(recargoNocTotal) + "</strong></td> </tr>";
					respuesta = respuesta + "</table> <br/>";
					//Insertamos el resumen
					HorarioResumen horarioResumen = new HorarioResumen(0, idEmpleadoAnterior, acumuladoHoras, horasExtrasOrdinarias,horasExtrasDominicales, horasFestivas, recargoNocTotal,fechaAnterior,fechaActual );
					HorarioResumenDAO.insertarHorarioResumen(horarioResumen);
					
					filaActual = filaActual + 2;
					//Aqui tendremos un gran doble salto para pasar de empleado
					respuesta = respuesta + "<table WIDTH='400' border='2'> <TH COLSPAN='6'> " + empleadoActual  + "</TH> </tr>";
					//Creamos Encabezado del reporte
					HSSFRow nombrePersona = sheet.createRow(filaActual);
					Cell cellFila = nombrePersona.createCell((short) 0);
					cellFila.setCellValue(empleadoActual);
					filaActual++;
					respuesta = respuesta + "<tr>"
							+  "<td width='120' nowrap><strong>NOMBRE</strong></td>"
							+  "<td width='50' nowrap><strong>FECHA</strong></td>"
							+  "<td width='50' nowrap><strong>DIA</strong></td>"
							+  "<td width='50' nowrap><strong>INGRESO</strong></td>"
							+  "<td width='50' nowrap><strong>SALIDA</strong></td>"
							+  "<td width='40' nowrap><strong>HORAS</strong></td>"
							+  "<td width='40' nowrap><strong>TIENDA</strong></td>"
							+  "</tr>";
					//Continuamos con los encabezados
					HSSFRow encabezados = sheet.createRow(filaActual);
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
					cellFilaEncabezado.setCellValue("TIENDA");
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
				//Realizamos la conversión de las horas de recargo nocturna

				try
				{
					
					recargoNoc = Double.parseDouble(fila[7]);
				}catch(Exception e)
				{
					recargoNoc = 0;
				}
				recargoNocTotal = recargoNocTotal + recargoNoc;
				//Validaremos si el día es domingo
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
				respuesta = respuesta + "<tr><td width='120' nowrap>" + fila[0] + "</td><td width='50' nowrap> " + fila[1] + "</td><td width='50' nowrap> " + fila[2] + "</td><td width='50' nowrap> " + fila[3] + "</td><td width='50' nowrap> "+ fila[4] + "</td><td width='50' nowrap> " + strHoras + "</td><td width='50' nowrap> " + tienda +"</td></tr>";
				//Realizamos inserción de la tabla
				HorarioTrabajado horario = new HorarioTrabajado(0, Integer.parseInt(fila[8]),fila[1], fila[2], fila[3], fila[4],horas,idTienda );
				HorarioTrabajadoDAO.insertarHorarioTrabajado(horario);
				
				//Realizamos pintado de la fila en el Excel de una fila de datos
				HSSFRow encabezados = sheet.createRow(filaActual);
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
			respuesta = respuesta + "<tr> <td COLSPAN='6' width='400' nowrap><strong>TOTAL HORAS " + formatea.format(acumuladoHoras) + "</strong></td> </tr>";
			//En este punto realizamos los cálculos
			if(tieneFestivo)
			{
				if(salarioEmpleadoAnterior >= 1000000)
				{
					horasExtResiduales = acumuladoHoras - horasFestivas - 39;	
				}else
				{
					horasExtResiduales = acumuladoHoras - horasFestivas - 19.5;	
				}
				 
			}else
			{
				if(salarioEmpleadoAnterior >= 1000000)
				{
					horasExtResiduales = acumuladoHoras - horasFestivas - 47;	
				}else
				{
					horasExtResiduales = acumuladoHoras - horasFestivas - 23.5;	
				}
					
			}
			//El tratamiento no es diferencial en esta parte
			horasExtrasDominicales = horasTrabDomingos - 8;
			//Realizamos una validación adicional en donde si las horas extras dominicales son mayores a las
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
			//Realizamos la inclusión de la información en la tabla HTML
			respuesta = respuesta + "<tr> <td COLSPAN='7' width='400' nowrap><strong>HORAS EXTRAS ORD " + formatea.format(horasExtrasOrdinarias) + "</strong></td> </tr>";
			respuesta = respuesta + "<tr> <td COLSPAN='7' width='400' nowrap><strong>HORAS EXTRAS DOMI " + formatea.format(horasExtrasDominicales) + "</strong></td> </tr>";
			respuesta = respuesta + "<tr> <td COLSPAN='7' width='400' nowrap><strong>HORAS FESTIVA " + formatea.format(horasFestivas) + "</strong></td> </tr>";
			respuesta = respuesta + "<tr> <td COLSPAN='7' width='400' nowrap><strong>HORAS RECARGO NOCTURNO " + formatea.format(recargoNocTotal) + "</strong></td> </tr>";
			respuesta = respuesta + "</table> <br/>";
			//Insertamos el resumen del último
			HorarioResumen horarioResumen = new HorarioResumen(0, Integer.parseInt(fila[8]), acumuladoHoras, horasExtrasOrdinarias,horasExtrasDominicales, horasFestivas, recargoNocTotal,fechaAnterior,fechaActual );
			HorarioResumenDAO.insertarHorarioResumen(horarioResumen);
			//Insertamos el pie
			HSSFRow pie = sheet.createRow(filaActual);
			Cell cellFilaPie = pie.createCell((short) 0);
			cellFilaPie.setCellValue("TOTAL HORAS " +  formatea.format(acumuladoHoras));
			filaActual = filaActual + 2;
			
			//En esta parte termina la generación del correo
			workbook.write(fileOut);
			fileOut.close();
			
			//Buscamos la manera de enviar el correo 
			String[] rutasArchivos = new String[1];
			rutasArchivos[0] = rutaArchivoGenerado;
			
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEHORAS");
			Date fecha = new Date();
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("GENERAL CUMPLIMIENTO DE HORARIOS EMPLEADOS-INTERNOS SEMANAL DE " + fechaAnterior + " HASTA " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			//Anexamos el archivo generado
			correo.setRutasArchivos(rutasArchivos);
			correo.setMensaje("Resumen de los horarios cumplidos por Empleado: \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTMLAnexo();
			//Generamos otro correo con el fin de revisar las personas que no usaron biometria dentro de la semana que acaba de finalizar
			respuesta = "";
			respuesta = respuesta + "<table WIDTH='350' border='2'> <TH COLSPAN='5'> " + "NO REGISTRO DE HUELLA DACTILAR"  + "</TH> </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td width='150' nowrap><strong>NOMBRE</strong></td>"
					+  "<td width='50' nowrap><strong>FECHA</strong></td>"
					+  "<td width='50' nowrap><strong>DIA</strong></td>"
					+  "<td width='50' nowrap><strong>EVENTO</strong></td>"
					+  "<td width='50' nowrap><strong>TIENDA</strong></td>"
					+  "</tr>";
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
				respuesta = respuesta + "<tr><td width='150' nowrap>" + fila[0] + "</td><td width='50' nowrap> " + fila[1] + "</td><td width='50' nowrap> " + fila[2] + "</td><td width='50' nowrap> " + fila[3] + "</td><td width='50' nowrap> " + tienda +"</td></tr>";
			}
			correo.setAsunto("GENERAL PERSONAS EMPLEADOS Y MOMENTOS DE NO USO DEL HUELLERO DACTILAR DE " + fechaAnterior + " HASTA " + fechaActual);
			correo.setMensaje("Resumen de momentos y empleados que no usaron el huellero: \n" + respuesta);
			contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}catch(Exception e)
		{
			System.out.println("problemas en la generacion del archivo " + e.toString() + e.getMessage() + e.getStackTrace().toString() );
		}
	}
	
	
	
	
	
	
	
}

