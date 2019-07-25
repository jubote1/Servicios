package Servicios;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import utilidades.ControladorEnvioCorreo;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFRegionUtil;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import CapaDAOServicios.GeneralDAO;
import CapaDAOServicios.ItemInventarioDAO;
import CapaDAOServicios.ParametrosDAO;
import CapaDAOServicios.PedidoDAO;
import CapaDAOServicios.TiendaDAO;
import Modelo.Correo;
import Modelo.Insumo;
import Modelo.Tienda;

public class ReporteCierreInventarioCtrl {
	
	
	
	
public static void main(String[] args)
{
	ReporteCierreInventarioCtrl reporteTiendas = new ReporteCierreInventarioCtrl();
	reporteTiendas.generarReporteSemanalCierreInventarioTiendas();
	
}

public void generarReporteSemanalCierreInventarioTiendas()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCIÓN");
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	//Arreglo donde dejaremos la ruta de cada uno de los archivos generados.
	String[] rutasArchivos = new String[tiendas.size()];
	//Obtenemos la fecha actual, con base en la cual realizaremos el recorrido
	// En el String fecha guardaremos el contenido de la fecha
	//Para la ejecución automática asumimos que es un lunes
	String fechaActual = "";
	//Variables donde manejaremos la fecha anerior con el fin realizar los cálculos del cierre de inventarios
	Date datFechaAnterior;
	String fechaAnterior = "";
	//Creamos el objeto calendario
	Calendar calendarioActual = Calendar.getInstance();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//Obtenemos la fecha Actual en el formato necesario de base de datos
	try
	{
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
	int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
	//validamos que sea un día lunes
	if(diaActual == 2)
	{
		calendarioActual.add(Calendar.DAY_OF_YEAR, -7);
	}
	//Llevamos a un string la fecha anterior para el cálculo de la venta
	datFechaAnterior = calendarioActual.getTime();
	fechaAnterior = dateFormat.format(datFechaAnterior);
	
	
	//Queremos que la fecha actual sea puesta en el domingo
	try
	{
		//Al objeto calendario le fijamos la fecha actual del sistema
		calendarioActual.setTime(dateFormat.parse(fechaActual));
		calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
		datFechaAnterior = calendarioActual.getTime();
		fechaActual = dateFormat.format(datFechaAnterior);
	}catch(Exception e)
	{
		System.out.println(e.toString());
	}
	//-- En este punto finalizamos la fijación de las tiendas
	
	
	//Realizamos un ciclo para recorrer cada una de las tiendas	
	int fila = 0;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			try
			{
				
					rutasArchivos[fila] = CalcularCierreSemanalTiendaFormatoExcel(tien, fechaActual, fechaAnterior);
			}
			catch(Exception e)
			{
				System.out.println(e.toString() + " " + e.fillInStackTrace() + " " + e.getMessage());
			}
			fila++;
		}
	}
	
	//Realizamos el envío del correo electrónico con los archivos
	Correo correo = new Correo();
	correo.setAsunto("CIERRE SEMANAL DE INVENTARIO" + fechaActual + " " + fechaAnterior);
	correo.setContrasena("Pizzaamericana2017");
	//Tendremos que definir los destinatarios de este correo
	ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPCIERREINVENTARIO");
	correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
	correo.setMensaje("A continuación todos los CIERRES de inventarios de las tiendas de pizza americana");
	correo.setRutasArchivos(rutasArchivos);
	ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
	contro.enviarCorreo();
}


/**
 * Método grueso para la generación del formato de cierre de inventarios semanales
 * @param idtienda
 * @param fecha
 * @return
 */
public String CalcularCierreSemanalTiendaFormatoExcel(Tienda tienda, String fechaActual, String fechaAnterior)
{
	String rutaArchivoGenerado="";
	String rutaArchivoBD = ParametrosDAO.retornarValorAlfanumericoLocal("RUTACIERREINVENTARIO");
	String rutaImagenReporte = rutaArchivoBD + "LogoPizzaAmericana.png";
	//obtenemos día de la semana para recuperar inventario requerido de la tienda
	// Creamos una instancia del calendario
	GregorianCalendar cal = new GregorianCalendar();
	int diasemana = 0;
	String nombreTienda = tienda.getNombreTienda();
	//Creamos el libro en Excel y la hoja en cuestión, definimos los encabezados.
	HSSFWorkbook workbook = new HSSFWorkbook();
	HSSFSheet sheet = workbook.createSheet(tienda.getNombreTienda());
	sheet.setColumnWidth(0, 7500);
	sheet.setColumnWidth(1, 4500);
	sheet.setColumnWidth(2, 4500);
	sheet.setColumnWidth(3, 4500);
	sheet.setColumnWidth(4, 4500);
	sheet.setColumnWidth(5, 4500);
	sheet.setColumnWidth(6, 4500);
	sheet.setColumnWidth(7, 4500);
	sheet.setColumnWidth(8, 4500);
	sheet.setColumnWidth(9, 4500);
	String[] headers = new String[]{
            "PRODUCTO",
            "INVENTARIO \n INICIAL",
            "ENVIADO A TIENDA",
            "RETIRO OTRAS TIENDAS",
            "INVENTARIO \n FINAL",
            "CONSUMO",
            "COSTO UNITARIO",
            "COSTO TOTAL",
            "PORCENTAJE",
            "COMIDA FINAL"
        };
	
	try
	{
		   rutaArchivoGenerado = rutaArchivoBD + "CierreInventario"+ nombreTienda + "-" + fechaAnterior + "--" + fechaActual +".xls";
		   
		   FileOutputStream fileOut = new FileOutputStream(rutaArchivoGenerado);
		   rutaArchivoGenerado = rutaArchivoGenerado + "%&" + "CierreInventario"+ nombreTienda + "--" + fechaAnterior + "-" + fechaActual +".xls";
		   System.out.println(rutaArchivoGenerado);
		   //Deberemos de recuperar la información para el informe
			
			//Esta parte de recuperación de los insumos tienda se establece control dado que está recuperando constantemente
		   	ArrayList cierreInventario = new ArrayList();
		   	ArrayList cierreInventarioGas = new ArrayList();
		   	//Llamamos método qeu llenará el ArrayList con el resumen de la información
		   	cierreInventario = CapaDAOServicios.ItemInventarioDAO.obtenerCierreSemanalInsumos(fechaActual, fechaAnterior, "Insumos", tienda.getHostBD());
		   	cierreInventarioGas = CapaDAOServicios.ItemInventarioDAO.obtenerCierreSemanalInsumos(fechaActual, fechaAnterior, "Bebidas", tienda.getHostBD());
			//Contralaremos la fila en la que vamos con la variable fila
			int fila = 0;
			int filasInforme = 0;
			//Llevamos a filasInforme el valor de los items de inventario
			filasInforme = cierreInventario.size();
			//filasInforme = cierreInventario.size() + cierreInventarioGas.size();
			//Agregamos nombre del reporte
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
            	            
            //Creamos el estilo para la tercer fila de encabezados
            Font fontTerFila = workbook.createFont();
            //fontTerFila.setColor(IndexedColors.ORANGE.index);
            fontTerFila.setFontHeightInPoints((short) 10.00);
            fontTerFila.setBold(true);
            HSSFCellStyle styleEnc = workbook.createCellStyle();
            styleEnc.setBorderBottom(BorderStyle.THIN);
            styleEnc.setBorderTop(BorderStyle.THIN);
            styleEnc.setBorderLeft(BorderStyle.THIN);
            styleEnc.setBorderRight(BorderStyle.THIN);
            styleEnc.setWrapText(true);
            styleEnc.setFont(fontTerFila);
            styleEnc.setAlignment(HorizontalAlignment .CENTER);
            
            //Creamos el estilo para la informacion del reporte
            HSSFCellStyle styleInfRep = workbook.createCellStyle();
            styleInfRep.setBorderBottom(BorderStyle.THIN);
            styleInfRep.setBorderTop(BorderStyle.THIN);
            styleInfRep.setBorderLeft(BorderStyle.THIN);
            styleInfRep.setBorderRight(BorderStyle.THIN);
            styleInfRep.setWrapText(true);
                        
            //NOMBRE DEL REPORTE
            HSSFRow headerRow = sheet.createRow((short) 0);
            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$D$1"));
            Cell cellHeader = headerRow.createCell((short) 0);
            cellHeader.setCellValue(new HSSFRichTextString("CIERRE SEMANAL DE INVENTARIOS Y CONSUMOS \n" + nombreTienda ));
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
            
            //Aplicamos los bordes a la región merge
            CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, 3);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            
            //Para la imagen
            sheet.addMergedRegion(CellRangeAddress.valueOf("$E$1:$G$1"));
            sheet.addMergedRegion(CellRangeAddress.valueOf("$E$2:$G$2"));
          //Aplicamos los bordes a la región merge
            cellRangeAddress = new CellRangeAddress(0, 0, 4, 6);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            
            cellRangeAddress = new CellRangeAddress(1, 1, 4, 6);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            
            //Etiquetas de segunda linea de informaicón 
            HSSFRow equitetasInfReporte = sheet.createRow(1);
            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$2:$B$2"));
            //Aplicamos los bordes a la región merge
            cellRangeAddress = new CellRangeAddress(1, 1, 0, 1);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            sheet.addMergedRegion(CellRangeAddress.valueOf("$C$2:$D$2"));
          //Aplicamos los bordes a la región merge
            cellRangeAddress = new CellRangeAddress(1, 1, 2, 3);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            Cell cellFila2 = equitetasInfReporte.createCell((short) 0);
            cellFila2.setCellValue("TIENDA: " + nombreTienda);
            cellFila2.setCellStyle(cellInfoReporte);
            cellFila2 = equitetasInfReporte.createCell((short) 2);
            cellFila2.setCellValue("FECHA: " + fechaAnterior + " / " + fechaActual);
            cellFila2.setCellStyle(cellInfoReporte);
            cellFila2 = equitetasInfReporte.createCell((short) 4);
            cellFila2.setCellValue("TOTAL VENTA DE LA SEMANA");
            cellFila2.setCellStyle(cellInfoReporte);
            //Obtenemos el total de venta de la semana
            double totalVentaSemana = PedidoDAO.obtenerTotalesPedidosSemana(fechaAnterior, fechaActual, tienda.getHostBD());
            cellFila2 = equitetasInfReporte.createCell((short) 7);
            cellFila2.setCellValue(totalVentaSemana);
            
            
            //Etiquetas reporte
            HSSFRow equitetasRow = sheet.createRow(2);
	        for (int i = 0; i < headers.length; ++i) 
	        {
	            String header = headers[i];
	            HSSFCell cell = equitetasRow.createCell(i);
	            cell.setCellValue(header);
	            cell.setCellStyle(styleEnc);
	        }
	        
	        //
	        Cell datos;
	        String nombreInsumo;
	        double inventarioInicial;
	        double enviadoTienda;
	        double retiroTienda;
	        double inventarioFinal;
	        double consumo;
	        double costoUnidad = 0;
	        double costoTotal = 0;
	        double costoTotalComida = 0;
	        double costoTotalComidaSinUsar = 0;
	        //Variable donde almacenamos el valor de la comida que quedo en la tienda
	        double costoTotalSinUsar = 0;
	        int idItem;
	        int idInsumo;
	        Insumo insumoTemp;
	        //Nos traemos los insumos inventarios
	        ArrayList <Insumo> insumos = ItemInventarioDAO.obtenerInfoBasicaInsumos();
	        for (int i = 0; i < filasInforme; ++i) {
	            HSSFRow dataRow = sheet.createRow(i + 3);
	            
	            String[] d = (String[]) cierreInventario.get(i);
	            idItem = Integer.parseInt((String) d[0]);
	            nombreInsumo = (String) d[1];
	            inventarioInicial = Double.parseDouble((String) d[3]);
	            enviadoTienda = Double.parseDouble((String) d[4]);
	            retiroTienda = Double.parseDouble((String) d[5]);
	            inventarioFinal= Double.parseDouble((String) d[6]);
	            consumo = inventarioInicial + enviadoTienda - retiroTienda - inventarioFinal;
	            //Buscamos el insumo para saber su costounidad
	            for(int y = 0; y < insumos.size(); y++ )
	            {
	            	insumoTemp = insumos.get(y);
	            	idInsumo = insumoTemp.getIdinsumo();
	            	if(idItem == idInsumo)
	            	{
	            		costoUnidad = insumoTemp.getCostoUnidad();
	            		if(insumoTemp.getUnidadMedida().equals(new String("unidad")))
	            		{
	            			costoTotal = costoUnidad * consumo;
	            			costoTotalSinUsar = costoUnidad * inventarioFinal;
	            		}else if(insumoTemp.getUnidadMedida().equals(new String("gramos")))
	            		{
	            			costoTotal = (consumo/1000)* costoUnidad;
	            			costoTotalSinUsar = (inventarioFinal/1000) * costoUnidad;
	            		}
	            		costoTotalComida = costoTotalComida + costoTotal;
	            		costoTotalComidaSinUsar = costoTotalComidaSinUsar + costoTotalSinUsar;
	            		break;
	            	}
	            }
	            
	            //Buscamos el valor inicial
	            datos = dataRow.createCell(0);
	            datos.setCellValue(nombreInsumo);
	            datos.setCellStyle(styleInfRep);
	            datos = dataRow.createCell(1);
	            datos.setCellValue(inventarioInicial);
	            datos.setCellStyle(styleInfRep);
	            datos = dataRow.createCell(2);
	            datos.setCellValue(enviadoTienda);
	            datos.setCellStyle(styleInfRep);
	            datos = dataRow.createCell(3);
	            datos.setCellValue(retiroTienda);
	            datos.setCellStyle(styleInfRep);
	            datos = dataRow.createCell(4);
	            datos.setCellValue(inventarioFinal);
	            datos.setCellStyle(styleInfRep);
	            datos = dataRow.createCell(5);
	            datos.setCellStyle(styleInfRep);
	            datos.setCellValue(consumo);
	            datos = dataRow.createCell(6);
	            datos.setCellStyle(styleInfRep);
	            datos.setCellValue(costoUnidad);
	            datos = dataRow.createCell(7);
	            datos.setCellStyle(styleInfRep);
	            datos.setCellValue(costoTotal);
	            datos = dataRow.createCell(9);
	            datos.setCellStyle(styleInfRep);
	            datos.setCellValue(costoTotalSinUsar);
	            
	        }
	        //Agregamos el total de la comida sin gaseosa
	        HSSFRow dataInt = sheet.createRow(filasInforme + 3);
	        datos = dataInt.createCell(6);
            datos.setCellValue("COSTO SIN GASEOSA");
            datos.setCellStyle(styleEnc);
            datos = dataInt.createCell(7);
            datos.setCellValue(costoTotalComida);
            datos.setCellStyle(styleInfRep);
            datos = dataInt.createCell(8);
            double porcentajeComida = (costoTotalComida/totalVentaSemana)*100;
            datos.setCellValue(porcentajeComida);
            datos.setCellStyle(styleInfRep);
            
            
            
            //Continuamos con la inclusión de la información de los consumos de gaseosa
	        for (int y = 0 ; y < cierreInventarioGas.size(); y++) {
	            HSSFRow dataRow = sheet.createRow(filasInforme+4);
	            
	            String[] d = (String[]) cierreInventarioGas.get(y);
	            idItem = Integer.parseInt((String) d[0]);
	            nombreInsumo = (String) d[1];
	            inventarioInicial = Double.parseDouble((String) d[3]);
	            enviadoTienda = Double.parseDouble((String) d[4]);
	            retiroTienda = Double.parseDouble((String) d[5]);
	            inventarioFinal= Double.parseDouble((String) d[6]);
	            consumo = inventarioInicial + enviadoTienda - retiroTienda - inventarioFinal;
	            //Buscamos el insumo para saber su costounidad
	            for(int z = 0; z < insumos.size(); z++ )
	            {
	            	insumoTemp = insumos.get(z);
	            	idInsumo = insumoTemp.getIdinsumo();
	            	if(idItem == idInsumo)
	            	{
	            		costoUnidad = insumoTemp.getCostoUnidad();
	            		if(insumoTemp.getUnidadMedida().equals(new String("unidad")))
	            		{
	            			costoTotal = costoUnidad * consumo;
	            			costoTotalSinUsar = costoUnidad * inventarioFinal;
	            		}else if(insumoTemp.getUnidadMedida().equals(new String("gramos")))
	            		{
	            			costoTotal = (consumo/1000)* costoUnidad;
	            			costoTotalSinUsar = (inventarioFinal/1000) * costoUnidad;
	            		}
	            		costoTotalComida = costoTotalComida + costoTotal;
	            		costoTotalComidaSinUsar = costoTotalComidaSinUsar + costoTotalSinUsar;
	            		break;
	            	}
	            }
	            //Buscamos el valor inicial
	            datos = dataRow.createCell(0);
	            datos.setCellValue(nombreInsumo);
	            datos.setCellStyle(styleInfRep);
	            datos = dataRow.createCell(1);
	            datos.setCellValue(inventarioInicial);
	            datos.setCellStyle(styleInfRep);
	            datos = dataRow.createCell(2);
	            datos.setCellValue(enviadoTienda);
	            datos.setCellStyle(styleInfRep);
	            datos = dataRow.createCell(3);
	            datos.setCellValue(retiroTienda);
	            datos.setCellStyle(styleInfRep);
	            datos = dataRow.createCell(4);
	            datos.setCellValue(inventarioFinal);
	            datos.setCellStyle(styleInfRep);
	            datos = dataRow.createCell(5);
	            datos.setCellStyle(styleInfRep);
	            datos.setCellValue(consumo);
	            datos = dataRow.createCell(6);
	            datos.setCellStyle(styleInfRep);
	            datos.setCellValue(costoUnidad);
	            datos = dataRow.createCell(7);
	            datos.setCellStyle(styleInfRep);
	            datos.setCellValue(costoTotal);
	            datos = dataRow.createCell(9);
	            datos.setCellStyle(styleInfRep);
	            datos.setCellValue(costoTotalSinUsar);
	            filasInforme++;
	        }
	        
	      //Agregamos el total de la comida con gaseosa
	        HSSFRow dataInt2 = sheet.createRow(filasInforme + 3);
	        datos = dataInt2.createCell(6);
            datos.setCellValue("COSTO CON GASEOSA");
            datos.setCellStyle(styleEnc);
            datos = dataInt2.createCell(7);
            datos.setCellValue(costoTotalComida);
            datos.setCellStyle(styleInfRep);
            datos = dataInt2.createCell(8);
            porcentajeComida = (costoTotalComida/totalVentaSemana)*100;
            datos.setCellValue(porcentajeComida);
            datos.setCellStyle(styleInfRep);
            //Colocaremos los valores del total de comida tienda
            datos = dataInt2.createCell(9);
            datos.setCellValue("COSTO INSUMOS EN TIENDA");
            datos.setCellStyle(styleEnc);
            datos = dataInt2.createCell(10);
            datos.setCellValue(costoTotalComidaSinUsar);
            datos.setCellStyle(styleInfRep);
                 
	        
	    workbook.write(fileOut);
		fileOut.close();
	}catch(Exception e)
	{
		System.out.println("problemas en la generacion del archivo " + e.toString() + e.getMessage() + e.getStackTrace().toString() );
	}
	return(rutaArchivoGenerado);
    
}

}




