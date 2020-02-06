package Servicios;

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

import CapaDAOServicios.GeneralDAO;
import CapaDAOServicios.ParametrosDAO;
import CapaDAOServicios.PedidoDAO;
import CapaDAOServicios.PedidoFueraTiempoDAO;
import CapaDAOServicios.PedidoPOSPMDAO;
import CapaDAOServicios.PedidoPixelDAO;
import CapaDAOServicios.ReporteContactCenterDAO;
import CapaDAOServicios.ReporteHorariosDAO;
import CapaDAOServicios.TiempoPedidoDAO;
import CapaDAOServicios.TiendaDAO;
import ConexionServicios.ConexionBaseDatos;
import Modelo.Pedido;
import Modelo.PedidoFueraTiempo;
import Modelo.PedidoPixel;
import Modelo.TiempoPedido;
import Modelo.Tienda;
import Modelo.Correo;
import Modelo.EmpleadoEvento;
import utilidades.ControladorEnvioCorreo;

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
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				//Obtenemos la fecha Actual
				try
				{
					//OJO
					fechaActual = dateFormat.format(calendarioActual.getTime());
					//fechaActual = "2019-05-13";
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
				ArrayList<EmpleadoEvento>  repEntradasSalidas = ReporteHorariosDAO.obtenerEntradasSalidasEmpleadosEventos(fechaAnterior,fechaActual);
				//Variables necesarias para el recorrido
				EmpleadoEvento eventoTemp;
				//Arreglo donde iremos dejando cada fila
				String[] filaTemp = new String[7];
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
							filaTemp = new String[7];
							filaTemp[0] = eventoTemp.getNombreEmpleado();
							filaTemp[1] = eventoTemp.getFecha();
							filaTemp[2] = eventoTemp.getDia();
							filaTemp[3] = eventoTemp.getFechaHoraLog();
							filaTemp[6] = Integer.toString(eventoTemp.getIdTienda());
						}if(salida)
						{
							filaTemp = new String[7];
							filaTemp[0] = eventoTemp.getNombreEmpleado();
							filaTemp[1] = eventoTemp.getFecha();
							filaTemp[2] = eventoTemp.getDia();
							filaTemp[3] = eventoTemp.getFechaHoraLog();
							filaTemp[6] = Integer.toString(eventoTemp.getIdTienda());
						}
						ingreso = true;
						salida = false;
					}else if(eventoTemp.getTipoEvento().equals(new String("SALIDA")))
					{
						filaTemp[4] = eventoTemp.getFechaHoraLog();
						//Hacer la resta de tiempos para lo cual formateamos las fechas
						Date fechaFinal = new Date(), fechaInicial = new Date();
						double horas = 0;
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
				        if(!errorInicial && !errorFinal)
				        {
				        	  horas = ((fechaFinal.getTime()-fechaInicial.getTime())/1000);
				        	  horas =(horas)/3600;
				        }
				        //DecimalFormat df = new DecimalFormat("#.00");
				        filaTemp[5] = Double.toString(horas);
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
			String empleadoActual = "";
			double horas = 0;
			String strHoras = "";
			double acumuladoHoras = 0;
			String tienda = "";
			int idTienda;
			for(int i = 0; i < reporteHorarios.size(); i++)
			{
				String[] fila = (String[]) reporteHorarios.get(i);
				empleadoActual = fila[0];
				if(empleadoAnterior.equals(new String("")))
				{
					empleadoAnterior = fila[0];
					respuesta = respuesta + "<table WIDTH='400' border='2'> <TH COLSPAN='6'> " + empleadoActual  + "</TH> </tr>";
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
					respuesta = respuesta + "<tr> <td COLSPAN='6' width='400' nowrap><strong>TOTAL HORAS " + acumuladoHoras + "</strong></td> </tr>";
					respuesta = respuesta + "</table> <br/>";
					//Insertamos el pie
					HSSFRow pie = sheet.createRow(filaActual);
					Cell cellFilaPie = pie.createCell((short) 0);
					cellFilaPie.setCellValue("TOTAL HORAS " +  acumuladoHoras);
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
			}
			respuesta = respuesta + "<tr> <td COLSPAN='6' width='400' nowrap><strong>TOTAL HORAS " + formatea.format(acumuladoHoras) + "</strong></td> </tr>";
			respuesta = respuesta + "</table> <br/>";
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
			correo.setAsunto("GENERAL CUMPLIMIENTO DE HORARIOS SEMANAL DE " + fechaAnterior + " HASTA " + fechaActual);
			correo.setContrasena("Pizzaamericana2017");
			correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
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
				String[] fila = (String[])reporteNoUso.get(i);
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
			correo.setAsunto("GENERAL PERSONAS Y MOMENTOS DE NO USO DEL HUELLERO DACTILAR DE " + fechaAnterior + " HASTA " + fechaActual);
			correo.setMensaje("Resumen de momentos y empleados que no usaron el huellero: \n" + respuesta);
			contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}catch(Exception e)
		{
			System.out.println("problemas en la generacion del archivo " + e.toString() + e.getMessage() + e.getStackTrace().toString() );
		}
	}
	
	
	
	
}

