package ServiciosSer;
/*Este proceos deber� correr los lunes as� sea reproceso
 * y no se deber� de sobreescribir cuando se cambie el proceso titular.
 */
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

import CapaDAOSer.CierreInventarioSemanalDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ItemInventarioDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.VentaSemanalTiendaDAO;
import ModeloSer.CierreInventarioSemanal;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.Insumo;
import ModeloSer.Tienda;
import ModeloSer.VentaSemanalTienda;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteCierreInventarioReprocesoCtrl {
	

	String respuesta = "";
	String respuestaConDescuentos = "";
	DecimalFormat formatea = new DecimalFormat("###,###.##");
	
public static void main(String[] args)
{
	ReporteCierreInventarioReprocesoCtrl reporteTiendas = new ReporteCierreInventarioReprocesoCtrl();
	reporteTiendas.generarReporteSemanalCierreInventarioTiendas();
	
}

public void generarReporteSemanalCierreInventarioTiendas()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCI�N");
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	//Arreglo donde dejaremos la ruta de cada uno de los archivos generados.
	String[] rutasArchivos = new String[tiendas.size()];
	//Obtenemos la fecha actual, con base en la cual realizaremos el recorrido
	// En el String fecha guardaremos el contenido de la fecha
	//Para la ejecuci�n autom�tica asumimos que es un lunes
	String fechaActual = "";
	String fechaPosActual = "";
	//Variables donde manejaremos la fecha anerior con el fin realizar los c�lculos del cierre de inventarios
	Date datFechaAnterior;
	Date datFechaActual;
	String fechaAnterior = "";
	//Creamos el objeto calendario
	Calendar calendarioActual = Calendar.getInstance();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//Obtenemos la fecha Actual en el formato necesario de base de datos
	try
	{
		fechaPosActual =  ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
	}catch(Exception exc)
	{
		System.out.println(exc.toString());
	}
	int diaActual = 0;
	try
	{
		//Al objeto calendario le fijamos la fecha actual del sitema
		calendarioActual.setTime(dateFormat.parse(fechaPosActual));
		diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
		
	}catch(Exception e)
	{
		System.out.println(e.toString());
	}
	//Podemos validaci�n si el proceso no se corre un d�a lunes no deber�a de correr
	//validamos que sea un d�a lunes
	if(diaActual == 2)
	{
		calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
		datFechaActual = calendarioActual.getTime();
		fechaActual =  dateFormat.format(datFechaActual);
		calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
		//Llevamos a un string la fecha anterior para el c�lculo de la venta
		datFechaAnterior = calendarioActual.getTime();
		fechaAnterior = dateFormat.format(datFechaAnterior);
		
		//INCLUIMOS LA CONSTRUCCI�N DE LOS CONSUMOS POR TIENDA
		respuesta =  "<table WIDTH='700' border='2'> <tr> <td colspan='2'> REPORTE DE PORCENTAJE CONSUMO TIENDAS - " + fechaAnterior + "  -  " + fechaActual +  "</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<td WIDTH='100'><strong>TIENDA</strong></td>"
				+  "<td WIDTH='50'><strong>HORA>PORCENTAJE</strong></td>"
				+  "</tr>";
		respuestaConDescuentos =  "<table WIDTH='700' border='2'> <tr> <td colspan='5'> REPORTE DE PORCENTAJE CON DESCUENTOS CONSUMO TIENDAS - " + fechaAnterior + "  -  " + fechaActual +  "</td></tr>";
		respuestaConDescuentos = respuesta + "<tr>"
				+  "<td WIDTH='100'><strong>TIENDA</strong></td>"
				+  "<td WIDTH='50'><strong>PORCENTAJE SIN DESCUENTOS</strong></td>"
				+  "<td WIDTH='50'><strong>TOTAL VENTA CON DESCUENTOS</strong></td>"
				+  "<td WIDTH='50'><strong>TOTAL DESCUENTOS</strong></td>"
				+  "<td WIDTH='50'><strong>PORCENTAJE CON DESCUENTOS</strong></td>"
				+  "</tr>";
		//-- En este punto finalizamos la fijaci�n de las tiendas
		
		//Vamos a realizar una modificaci�n para calcular la venta total de la semana para tienda
		//Definimos la variable en donde vamos a almacenar dicho total
		double ventaTotalTiendas = 0;
		double ventaTienda = 0;
		for(Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				//Realizamos la acumulaci�n despues de cada iteraci�n
				ventaTienda = PedidoDAO.obtenerTotalesPedidosSemana(fechaAnterior, fechaActual, tien.getHostBD());
				ventaTotalTiendas  = ventaTotalTiendas + ventaTienda;
				VentaSemanalTienda ventSemanal = new VentaSemanalTienda(tien.getIdTienda(),fechaActual,ventaTienda, tien.getMeta());
				VentaSemanalTiendaDAO.insertarVentaSemanalTienda(ventSemanal);
			}
		}
		
		//Realizamos un ciclo para recorrer cada una de las tiendas	
		int fila = 0;
		for(Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				try
				{
					
						rutasArchivos[fila] = CalcularCierreSemanalTiendaFormatoExcel(tien, fechaActual, fechaAnterior, ventaTotalTiendas, fechaPosActual);
				}
				catch(Exception e)
				{
					System.out.println(e.toString() + " " + e.fillInStackTrace() + " " + e.getMessage());
				}
				fila++;
			}
		}
		
		//Realizamos el env�o del correo electr�nico con los archivos
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("CIERRE SEMANAL DE INVENTARIO" + fechaActual + " " + fechaAnterior);
		correo.setContrasena(infoCorreo.getClaveCorreo());
		//Tendremos que definir los destinatarios de este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPCIERREINVENTARIO");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuaci�n todos los CIERRES de inventarios de las tiendas de pizza americana");
		correo.setRutasArchivos(rutasArchivos);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreo();
		
		//cerramos la tabla de los totales por tienda
		respuesta = respuesta + "</table> <br/>";
		respuestaConDescuentos = respuestaConDescuentos + "</table> <br/>";
		correo.setAsunto("RESUMEN SEMANAL PORCENTAJE CONSUMO TIENDAS " + fechaActual + " " + fechaAnterior);
		correos = GeneralDAO.obtenerCorreosParametro("RESUMENPORCOMIDA");
		correo.setMensaje("A continuaci�n se anexan los resultados de porcentaje de comidas en la semana que finaliza. " + respuesta);
		correo.setRutasArchivos(null);
		contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
		
		//ENVIO DE CORREO CON DESCUENTO
		correo.setAsunto("RESUMEN SEMANAL PORCENTAJE CONSUMO TIENDAS CON DESCUENTO " + fechaActual + " " + fechaAnterior);
		correos = GeneralDAO.obtenerCorreosParametro("RESUMENPORCOMIDADESCUENTO");
		correo.setMensaje("A continuaci�n se anexan los resultados de porcentaje de comidas en la semana que finaliza. " + respuestaConDescuentos);
		correo.setRutasArchivos(null);
		contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}
	
}


/**
 * M�todo grueso para la generaci�n del formato de cierre de inventarios semanales
 * @param idtienda
 * @param fecha
 * @return
 */
public String CalcularCierreSemanalTiendaFormatoExcel(Tienda tienda, String fechaActual, String fechaAnterior, double ventaTotalTiendas, String fechaPosActual)
{
	String rutaArchivoGenerado="";
	String rutaArchivoBD = ParametrosDAO.retornarValorAlfanumericoLocal("RUTACIERREINVENTARIO");
	String rutaImagenReporte = rutaArchivoBD + "LogoPizzaAmericana.png";
	//obtenemos d�a de la semana para recuperar inventario requerido de la tienda
	// Creamos una instancia del calendario
	GregorianCalendar cal = new GregorianCalendar();
	int diasemana = 0;
	String nombreTienda = tienda.getNombreTienda();
	//Creamos el libro en Excel y la hoja en cuesti�n, definimos los encabezados.
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
		   //Deberemos de recuperar la informaci�n para el informe
			
			//Esta parte de recuperaci�n de los insumos tienda se establece control dado que est� recuperando constantemente
		   	ArrayList cierreInventario = new ArrayList();
		   	ArrayList cierreInventarioGas = new ArrayList();
		   	//Llamamos m�todo qeu llenar� el ArrayList con el resumen de la informaci�n
		   	cierreInventario = CapaDAOSer.ItemInventarioDAO.obtenerCierreSemanalInsumosReproceso(fechaActual, fechaAnterior, "Insumos", tienda.getHostBD(), fechaPosActual);
		   	cierreInventarioGas = CapaDAOSer.ItemInventarioDAO.obtenerCierreSemanalInsumosReproceso(fechaActual, fechaAnterior, "Bebidas", tienda.getHostBD(), fechaPosActual);
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
            
            //Creamos el estilo para la segunda fila de informaci�n
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
            
            //Realizamos la adici�n de la imagen del logo de pizza americana
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
            
            //Aplicamos los bordes a la regi�n merge
            CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, 3);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            
            //Para la imagen
            sheet.addMergedRegion(CellRangeAddress.valueOf("$E$1:$G$1"));
            sheet.addMergedRegion(CellRangeAddress.valueOf("$E$2:$G$2"));
          //Aplicamos los bordes a la regi�n merge
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
            
            //Etiquetas de segunda linea de informaic�n 
            HSSFRow equitetasInfReporte = sheet.createRow(1);
            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$2:$B$2"));
            //Aplicamos los bordes a la regi�n merge
            cellRangeAddress = new CellRangeAddress(1, 1, 0, 1);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            sheet.addMergedRegion(CellRangeAddress.valueOf("$C$2:$D$2"));
          //Aplicamos los bordes a la regi�n merge
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
            double totalDescuentosReembolsables = PedidoDAO.obtenerTotalDescuentosReembolsables(fechaAnterior, fechaActual, tienda.getIdTienda());
            cellFila2 = equitetasInfReporte.createCell((short) 7);
            cellFila2.setCellValue(totalVentaSemana);
            //Adicionamos la etiqueta de TOTAL TIENDAS
            cellFila2 = equitetasInfReporte.createCell((short) 8);
            cellFila2.setCellValue("TOTAL TIENDAS");
            cellFila2.setCellStyle(cellInfoReporte);
            cellFila2 = equitetasInfReporte.createCell((short) 9);
            cellFila2.setCellValue(ventaTotalTiendas);
            double porcentajeVentaTienda = (totalVentaSemana/ventaTotalTiendas)*100;
            cellFila2 = equitetasInfReporte.createCell((short) 10);
            cellFila2.setCellValue(porcentajeVentaTienda);
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
	        double embalajeCosto = 0;
	        double costoTotal = 0;
	        double costoTotalComida = 0;
	        double costoTotalComidaSinUsar = 0;
	        //Variable donde almacenamos el valor de la comida que quedo en la tienda
	        double costoTotalSinUsar = 0;
	        int idItem;
	        int idInsumo;
	        Insumo insumoTemp;
	        CierreInventarioSemanal cierreInv = new CierreInventarioSemanal();
	        cierreInv.setIdTienda(tienda.getIdTienda());
	        cierreInv.setFecha(fechaActual);
	        //Nos traemos los insumos inventarios
	        ArrayList <Insumo> insumos = ItemInventarioDAO.obtenerInfoBasicaInsumos();
	        for (int i = 0; i < filasInforme; ++i) {
	            HSSFRow dataRow = sheet.createRow(i + 3);
	            
	            String[] d = (String[]) cierreInventario.get(i);
	            idItem = Integer.parseInt((String) d[0]);
	            cierreInv.setIdInsumo(idItem);
	            nombreInsumo = (String) d[1];
	            inventarioInicial = Double.parseDouble((String) d[3]);
	            cierreInv.setInventarioInicial(inventarioInicial);
	            enviadoTienda = Double.parseDouble((String) d[4]);
	            cierreInv.setEnviadoTienda(enviadoTienda);
	            retiroTienda = Double.parseDouble((String) d[5]);
	            cierreInv.setRetiro(retiroTienda);
	            inventarioFinal= Double.parseDouble((String) d[6]);
	            cierreInv.setInventarioFinal(inventarioFinal);
	            consumo = inventarioInicial + enviadoTienda - retiroTienda - inventarioFinal;
	            cierreInv.setConsumo(consumo);
	            //Buscamos el insumo para saber su costounidad
	            for(int y = 0; y < insumos.size(); y++ )
	            {
	            	insumoTemp = insumos.get(y);
	            	idInsumo = insumoTemp.getIdinsumo();
	            	if(idItem == idInsumo)
	            	{
	            		costoUnidad = insumoTemp.getCostoUnidad();
	            		embalajeCosto = insumoTemp.getEmbalajeCosto();
	            		cierreInv.setCostoUnitario(costoUnidad);
	            		if(insumoTemp.getUnidadMedida().equals(new String("unidad")))
	            		{
	            			costoTotal = costoUnidad * consumo;
	            			costoTotalSinUsar = costoUnidad * inventarioFinal;
	            		}else if(insumoTemp.getUnidadMedida().equals(new String("gramos")))
	            		{
	            			costoTotal = (consumo/embalajeCosto)* costoUnidad;
	            			costoTotalSinUsar = (inventarioFinal/embalajeCosto) * costoUnidad;
	            		}
	            		costoTotalComida = costoTotalComida + costoTotal;
	            		costoTotalComidaSinUsar = costoTotalComidaSinUsar + costoTotalSinUsar;
	            		cierreInv.setCostoUnitario(costoUnidad);
	            		cierreInv.setCostoTotal(costoTotal);
            			cierreInv.setCostoSinConsumir(costoTotalSinUsar);
	            		break;
	            	}
	            }
	            //Hacemos la inserci�n en la tabla
	            CierreInventarioSemanalDAO.insertarCierreInventarioSemanal(cierreInv);
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
            
            
            cierreInv = new CierreInventarioSemanal();
	        cierreInv.setIdTienda(tienda.getIdTienda());
	        cierreInv.setFecha(fechaActual);
            //Continuamos con la inclusi�n de la informaci�n de los consumos de gaseosa
	        for (int y = 0 ; y < cierreInventarioGas.size(); y++) {
	            HSSFRow dataRow = sheet.createRow(filasInforme+4);
	            
	            String[] d = (String[]) cierreInventarioGas.get(y);
	            idItem = Integer.parseInt((String) d[0]);
	            cierreInv.setIdInsumo(idItem);
	            nombreInsumo = (String) d[1];
	            inventarioInicial = Double.parseDouble((String) d[3]);
	            cierreInv.setInventarioInicial(inventarioInicial);
	            enviadoTienda = Double.parseDouble((String) d[4]);
	            cierreInv.setEnviadoTienda(enviadoTienda);
	            retiroTienda = Double.parseDouble((String) d[5]);
	            cierreInv.setRetiro(retiroTienda);
	            inventarioFinal= Double.parseDouble((String) d[6]);
	            cierreInv.setInventarioFinal(inventarioFinal);
	            consumo = inventarioInicial + enviadoTienda - retiroTienda - inventarioFinal;
	            cierreInv.setConsumo(consumo);
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
	            		cierreInv.setCostoUnitario(costoUnidad);
	            		cierreInv.setCostoTotal(costoTotal);
            			cierreInv.setCostoSinConsumir(costoTotalSinUsar);
	            		break;
	            	}
	            }
	            //Hacemos la inserci�n en la tabla
	            CierreInventarioSemanalDAO.insertarCierreInventarioSemanal(cierreInv);
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
            //En este punto tenemos el porcentaje de comida total con GASEOSA para la tienda
            respuesta = respuesta + "<tr><td>" + tienda.getNombreTienda() +  "</td><td>" + Double.toString(porcentajeComida) + "</td></tr>";
            //Generamos otro correo con informaci�n m�s detallada
            double porcentajeComidaConDes = (costoTotalComida/(totalVentaSemana+totalDescuentosReembolsables))*100;
            respuestaConDescuentos = respuestaConDescuentos + "<tr><td>" + tienda.getNombreTienda() +  "</td><td>" + Double.toString(porcentajeComida) + "</td><td>" + formatea.format(totalVentaSemana+totalDescuentosReembolsables) + "</td><td>" + formatea.format(totalDescuentosReembolsables) + "</td><td>" + Double.toString(porcentajeComidaConDes) + "</td></tr>";
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




