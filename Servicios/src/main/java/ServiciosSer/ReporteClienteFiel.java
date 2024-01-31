package ServiciosSer;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.PedidoDAO;
import ModeloSer.ClienteFiel;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import capaDAOCC.ParametrosDAO;
import utilidadesSer.ControladorEnvioCorreo;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

public class ReporteClienteFiel {
	
	
	//Lógica de Negocio para sacar los clientes fieles
	
		
	public void extraerClientesFieles()
	{
		//RECUPERACION DE PARÁMETROS PARA EJECUCIÓN DEL REPORTE
		String rutaArchivoGenerado="";
		String rutaArchivoBD = ParametrosDAO.retornarValorAlfanumerico("RUTACLIFIEL");
		String[] rutasArchivos = new String[1];
		int diasPedido = 0;
		int cantidadPedidos = 0;
		int diasNoPedido = 0;
		int diasNoPedidoInferior = 0;
		try
		{
			String strDiasPedidos = ParametrosDAO.retornarValorAlfanumerico("DIASPEDIDO");
			diasPedido = Integer.parseInt(strDiasPedidos);
		}catch(Exception e)
		{
			diasPedido = 0;
		}
		try
		{
			String strDiasNoPedidos = ParametrosDAO.retornarValorAlfanumerico("DIASNOPEDIDO");
			diasNoPedido = Integer.parseInt(strDiasNoPedidos);
		}catch(Exception e)
		{
			diasNoPedido = 0;
		}
		try
		{
			String strDiasNoPedidosInferior = ParametrosDAO.retornarValorAlfanumerico("DIASNOPEDIDOINFERIOR");
			diasNoPedidoInferior = Integer.parseInt(strDiasNoPedidosInferior);
		}catch(Exception e)
		{
			diasNoPedidoInferior = 0;
		}
		try
		{
			String strCantidadPedidos = ParametrosDAO.retornarValorAlfanumerico("CANTIDADPEDIDOS");
			cantidadPedidos = Integer.parseInt(strCantidadPedidos);
		}catch(Exception e)
		{
			cantidadPedidos = 0;
		}
		
		
		ArrayList<ClienteFiel> clientesNoFieles = PedidoDAO.obtenerClientesNoFieles(diasNoPedido, diasNoPedidoInferior);
		//RECUPERAMOS DE LA CAPA DAO LA INFORMACIÓN BASE DEL REPORTE
		
		ArrayList<ClienteFiel> clientesFieles = PedidoDAO.obtenerClientesFielesPedido(diasPedido, cantidadPedidos);
		//Se debe realizar el llenado 
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("ClienteFieles");
		sheet.setColumnWidth(0, 7500);
		sheet.setColumnWidth(1, 3500);
		sheet.setColumnWidth(2, 4500);
		sheet.setColumnWidth(3, 3000);
		sheet.setColumnWidth(4, 5500);
		sheet.setColumnWidth(5, 5500);
		sheet.setColumnWidth(6, 5500);
		HSSFSheet sheet2 = workbook.createSheet("ClienteNoFieles");
		sheet2.setColumnWidth(0, 7500);
		sheet2.setColumnWidth(1, 3500);
		sheet2.setColumnWidth(2, 4500);
		sheet2.setColumnWidth(3, 3000);
		sheet2.setColumnWidth(4, 5500);
		sheet2.setColumnWidth(5, 5500);
		sheet2.setColumnWidth(6, 5500);
		String[] headers = new String[]{
	            "IdCliente",
	            "Nombre Cliente",
	            "Telefono",
	            "Numero Pedidos",
	            "Nombre Tienda",
	            "Fecha de Pedido mas reciente",
	            "Fecha de Pedido mas Antiguo",
	            "Ofertas Totales",
	            "Ofertas Vigentes",
	            "correo electronico"
	        };
		
		
		//Creamos el estilo para el nombre del reporte
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
		
		//PROCESAMOS LA INFORMACIÓN PARA Y LA FORMATEAMOS EN EL EXCEL
		try
		{
			   rutaArchivoGenerado = rutaArchivoBD + "ClientesFieles" +".xls";
			   FileOutputStream fileOut = new FileOutputStream(rutaArchivoGenerado);
			   rutaArchivoGenerado = rutaArchivoGenerado + "%&" + "ClientesFieles"+".xls";
			   //NOMBRE DEL REPORTE
	            HSSFRow headerRow = sheet.createRow((short) 0);
	            HSSFRow headerRow2 = sheet2.createRow((short) 0);
	            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$G$1"));
	            sheet2.addMergedRegion(CellRangeAddress.valueOf("$A$1:$G$1"));
	            Cell cellHeader = headerRow.createCell((short) 0);
	            Cell cellHeader2 = headerRow2.createCell((short) 0);
	            cellHeader.setCellValue(new HSSFRichTextString(" INFORME CLIENTES FIELES "));
	            cellHeader2.setCellValue(new HSSFRichTextString(" INFORME CLIENTES NO FIELES "));
	            cellHeader.setCellStyle(cellheader);
	            cellHeader2.setCellStyle(cellheader);
	          //Etiquetas reporte
	            HSSFRow equitetasRow = sheet.createRow(1);
		        for (int j = 0; j < headers.length; ++j) {
		            String header = headers[j];
		            HSSFCell cell = equitetasRow.createCell(j);
		            cell.setCellValue(header);
		            cell.setCellStyle(styleEnc);
		        }
		        HSSFRow equitetasRow2 = sheet2.createRow(1);
		        for (int j = 0; j < headers.length; ++j) {
		            String header = headers[j];
		            HSSFCell cell = equitetasRow2.createCell(j);
		            cell.setCellValue(header);
		            cell.setCellStyle(styleEnc);
		        }
		        HSSFCell cellTemp;
		        for (int i = 0; i < clientesFieles.size(); ++i) {
		            HSSFRow dataRow = sheet.createRow(i + 2);
		            ClienteFiel cliTemp = clientesFieles.get(i);
		            cellTemp = dataRow.createCell(0);
		            cellTemp.setCellValue(cliTemp.getIdCliente());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(1);
		            cellTemp.setCellValue(cliTemp.getNombreCliente());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(2);
		            cellTemp.setCellValue(cliTemp.getTelefono());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(3);
		            cellTemp.setCellValue(cliTemp.getNumeroPedidos());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(4);
		            cellTemp.setCellValue(cliTemp.getNombreTienda());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(5);
		            cellTemp.setCellValue(cliTemp.getFechaMaxima());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(6);
		            cellTemp.setCellValue(cliTemp.getFechaMinima());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(7);
		            cellTemp.setCellValue(cliTemp.getOfertas());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(8);
		            cellTemp.setCellValue(cliTemp.getOfertasVigentes());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(9);
		            cellTemp.setCellValue(cliTemp.getCorreo());
		            cellTemp.setCellStyle(styleInfRep);
		        }
		        
		        for (int i = 0; i < clientesNoFieles.size(); ++i) {
		            HSSFRow dataRow = sheet2.createRow(i + 2);
		            ClienteFiel cliTemp = clientesNoFieles.get(i);
		            cellTemp = dataRow.createCell(0);
		            cellTemp.setCellValue(cliTemp.getIdCliente());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(1);
		            cellTemp.setCellValue(cliTemp.getNombreCliente());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(2);
		            cellTemp.setCellValue(cliTemp.getTelefono());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(3);
		            cellTemp.setCellValue(cliTemp.getNumeroPedidos());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(4);
		            cellTemp.setCellValue(cliTemp.getNombreTienda());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(5);
		            cellTemp.setCellValue(cliTemp.getFechaMaxima());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(6);
		            cellTemp.setCellValue(cliTemp.getFechaMinima());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(7);
		            cellTemp.setCellValue(cliTemp.getOfertas());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(8);
		            cellTemp.setCellValue(cliTemp.getOfertasVigentes());
		            cellTemp.setCellStyle(styleInfRep);
		            
		            cellTemp = dataRow.createCell(9);
		            cellTemp.setCellValue(cliTemp.getCorreo());
		            cellTemp.setCellStyle(styleInfRep);
		        }
		        workbook.write(fileOut);
				fileOut.close();
		}catch(Exception e)
		{
			System.out.println("problemas en la generacion del archivo " + e.toString() + e.getMessage() );
		}
		
		//RECUPERAMOS PARÁMETROS Y ENVIAMOS CORREO ELECTRÓNICO
		rutasArchivos[0] = rutaArchivoGenerado;
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		Correo correo = new Correo();
		correo.setAsunto("REPORTE CLIENTES FIELES");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTECLIFIEL");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuación el reporte de Clientes Fieles");
		correo.setRutasArchivos(rutasArchivos);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreo();
		
	}


public static void main(String[] args)
{
	ReporteClienteFiel inventario = new ReporteClienteFiel();
	inventario.extraerClientesFieles();
	
}

}




