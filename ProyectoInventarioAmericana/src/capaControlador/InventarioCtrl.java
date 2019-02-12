package capaControlador;

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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import capaDAO.InventarioDAO;
import capaDAO.ParametrosDAO;
import capaDAO.TiendaDAO;
import capaDAO.GeneralDAO;
import capaDAO.InsumoRequeridoTiendaDAO;
import capaModelo.*;
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

public class InventarioCtrl {
	
	/** ESTE MÉTDO ATIENDE LA CAPA DE PRESENTACIÓN
	 * Método que se encarga de definir la lógica de negocio para armar los inventarios de surtir una pizzeria en cuanto
	 * recupera el inventario actual de la tienda y lo cruza contra el deber ser y de esta manera precarga unos valores predifidos
	 * para ser mostrardos en la capa de presentación.
	 * @param idtienda valor con el idtienda de la cual se desea armar el inventario
	 * @param fecha para la cual se desea surtir con base en esta fecha es que se retorna el valor de los inventarios de la tienda, 
	 * adicionalmente se cálcula el día de la semana que se va a surtir, siendo el domingo el primer día de la semana.
	 * @return Se retornará un valor string en formato JSON con la base para ser desplegada en la capa de presentación.
	 */
	public String CalcularInventarioTienda(int idtienda, String fecha)
	{
		//obtenemos día de la semana para recuperar inventario requerido de la tienda
		// Creamos una instancia del calendario
		GregorianCalendar cal = new GregorianCalendar();
		int diasemana = 0;
		JSONArray listJSON = new JSONArray();
		try
		{
			Date fecha1 = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
			cal.setTime(fecha1);
			diasemana = cal.get(Calendar.DAY_OF_WEEK);
		}catch(Exception e)
		{
			System.out.println(e.toString());
		}
		ArrayList<InsumoTienda> insumosTienda = InventarioDAO.ObtenerInsumosTiendaAutomatico(idtienda, fecha);
		ArrayList<InsumoRequeridoTienda> insRequeridosTienda = InventarioDAO.ObtenerInsumosRequeridosTienda(idtienda, diasemana);
		for (InsumoRequeridoTienda insReqTienda : insRequeridosTienda)
		{
			JSONObject cadaJSON = new JSONObject();
			cadaJSON.put("idinsumo", insReqTienda.getIdinsumo());
			cadaJSON.put("requerido", insReqTienda.getCantidad());
			cadaJSON.put("unidadmedida", insReqTienda.getUnidadMedida());
			double cantidadLlevar = 0;
			int cantidadxcanasta;
			String manejacanastas;
			for (InsumoTienda insTienda : insumosTienda)
			{
				if(insReqTienda.getIdinsumo() == insTienda.getIdinsumo())
				{
					cadaJSON.put("nombreinsumo", insTienda.getNombreInsumo());
					cadaJSON.put("cantidadtienda", insTienda.getCantidad());
					if(insReqTienda.getManejacanasta().equals("S"))
					{
						cantidadLlevar =  insReqTienda.getCantidad() - insTienda.getCantidad();
						int canastas = (int)cantidadLlevar / insReqTienda.getCantidadxcanasta();
						int residuocanastas = (int)cantidadLlevar % insReqTienda.getCantidadxcanasta();
						if (residuocanastas > 0)
						{
							canastas++;
							cantidadLlevar = insReqTienda.getCantidadxcanasta()*canastas;
						}
						cadaJSON.put("cantidadcanastas", canastas);
					}
					else
					{
						cantidadLlevar =  insReqTienda.getCantidad() - insTienda.getCantidad();
						cadaJSON.put("cantidadcanastas", 0);
					}
									
					if (cantidadLlevar < 0)
					{
						cantidadLlevar = 0;
					}
					break;
				}
				
			}
			cadaJSON.put("cantidadllevar", cantidadLlevar);
			cadaJSON.put("nombrecontenedor", insReqTienda.getNombrecontenedor());
			cadaJSON.put("cantidadxcanasta", insReqTienda.getCantidadxcanasta());
			cadaJSON.put("manejacanastas", insReqTienda.getManejacanasta());
			listJSON.add(cadaJSON);
		}
		
		return(listJSON.toString());
	}
	
	
	public String ObtenerInsumosRequeridosTienda(int idtien, int diasemana)
	{
		JSONArray listJSON = new JSONArray();
		ArrayList<InsumoRequeridoTienda> insReq = InventarioDAO.ObtenerInsumosRequeridosTienda(idtien, diasemana);
		for (InsumoRequeridoTienda insReqTienda : insReq)
		{
			JSONObject cadaInsReqJSON = new JSONObject();
			cadaInsReqJSON.put("idinsumo", insReqTienda.getIdinsumo());
			cadaInsReqJSON.put("nombreinsumo", insReqTienda.getNombreInsumo());
			cadaInsReqJSON.put("nombrecontenedor", insReqTienda.getNombrecontenedor());
			cadaInsReqJSON.put("cantidadxcanasta", insReqTienda.getCantidadxcanasta());
			cadaInsReqJSON.put("cantidad", insReqTienda.getCantidad());
			cadaInsReqJSON.put("cantidadminima", insReqTienda.getCantidadMinima());
			listJSON.add(cadaInsReqJSON);
		}
		return(listJSON.toJSONString());
	}
	
	public String insertarActualizarInsumReqTienda(InsumoRequeridoTienda ins)
	{
		InsumoRequeridoTiendaDAO.insertarActualizarInsumReqTienda(ins);
		JSONArray listJSON = new JSONArray();
		JSONObject insReqJSON = new JSONObject();
		insReqJSON.put("respuesta", "exitoso");
		listJSON.add(insReqJSON);
		return(listJSON.toJSONString());
	}
	
	/**
	 * Método que se encarga de calgular los inventarios a llevar a una tienda y generar el excel correspondiente
	 * @param idtienda Se recibe el idtienda de la cual se calculara el inventario a llevar
	 * @param fecha Se recibe parámetro fecha de la cual se cálcular el inventario
	 * @return
	 */
	public String CalcularInventarioTiendaFormatoExcel(int idtienda, String fecha)
	{
		String rutaArchivoGenerado="";
		String rutaArchivoBD = ParametrosDAO.obtenerParametroTexto("RUTAINV");
		String rutaImagenReporte = rutaArchivoBD + "LogoPizzaAmericana.png";
		//obtenemos día de la semana para recuperar inventario requerido de la tienda
		// Creamos una instancia del calendario
		GregorianCalendar cal = new GregorianCalendar();
		int diasemana = 0;
		JSONArray listJSON = new JSONArray();
		try
		{
			Date fecha1 = new SimpleDateFormat("yyyy-MM-dd").parse(fecha);
			cal.setTime(fecha1);
			diasemana = cal.get(Calendar.DAY_OF_WEEK);
		}catch(Exception e)
		{
			System.out.println(e.toString() + e.getMessage() + e.getStackTrace());
		}
		String nombreTienda = TiendaDAO.obtenerNombreTienda(idtienda);
		//Creamos el libro en Excel y la hoja en cuestión, definimos los encabezados.
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("inventarioSurtir");
		sheet.setColumnWidth(0, 7500);
		sheet.setColumnWidth(1, 3500);
		sheet.setColumnWidth(2, 4500);
		sheet.setColumnWidth(3, 3000);
		sheet.setColumnWidth(4, 5500);
		sheet.setColumnWidth(5, 5500);
		sheet.setColumnWidth(6, 5500);
		String[] headers = new String[]{
	            "Producto",
	            "Cantidad \n Tienda",
	            "Cantidad \n Sugerida a Llevar",
	            "Empaque"
	        };
		
		try
		{
			   rutaArchivoGenerado = rutaArchivoBD + "InventarioSurtir"+ nombreTienda +".xls";
			   
			   FileOutputStream fileOut = new FileOutputStream(rutaArchivoGenerado);
			   rutaArchivoGenerado = rutaArchivoGenerado + "%&" + "InventarioSurtir"+ nombreTienda +".xls";
				
				//Esta parte de recuperación de los insumos tienda se establece control dado que está recuperando constantemente
			   boolean bandRecuperoInsumos = false;
			   int contadorReintentos = 1;
			   ArrayList<InsumoTienda> insumosTienda = new ArrayList();
			   
			   /**
			    * Este ciclo while tiene como objetivo realizar reintentos en caso de que no se encuentre información en la tabla
			    * esto se puede dar en el momento en que corra al mismo tiempo el reporte de inventarios y el servicio que trae los 
			    * inventario de una tienda
			    */
			   while(!bandRecuperoInsumos)
			   {
				   insumosTienda = InventarioDAO.ObtenerInsumosTiendaAutomatico(idtienda, fecha);
				   if ((insumosTienda.size() > 0)|| contadorReintentos > 3)
				   {
					   bandRecuperoInsumos = true;
				   }
				   else
				   {
					   Thread.sleep(10000);
					   contadorReintentos++;
				   }
			   }
				//ArrayList<InsumoTienda> insumosTienda = InventarioDAO.ObtenerInsumosTiendaAutomatico(idtienda, fecha);
				ArrayList<InsumoRequeridoTienda> insRequeridosTienda = InventarioDAO.ObtenerInsumosRequeridosTienda(idtienda, diasemana);
				//Contralaremos la fila en la que vamos con la variable fila
				int fila = 0;
				int filasInforme = 0;
				int insumorequeridos = insRequeridosTienda.size();
				//Definimos un arreglo donde iremos dejando los datos
				Object[][] data = new Object[insumorequeridos][5];
				for (InsumoRequeridoTienda insReqTienda : insRequeridosTienda)
				{
					//instanciamos el arreglo donde llevaremos la información para el excel
					//data = new Object[insRequeridosTienda.size()][5];
					JSONObject cadaJSON = new JSONObject();
					cadaJSON.put("idinsumo", insReqTienda.getIdinsumo());
					cadaJSON.put("requerido", insReqTienda.getCantidad());
					cadaJSON.put("unidadmedida", insReqTienda.getUnidadMedida());
					double cantidadLlevar = 0;
					int cantidadxcanasta;
					String manejacanastas;
					for (InsumoTienda insTienda : insumosTienda)
					{
						if(insReqTienda.getIdinsumo() == insTienda.getIdinsumo())
						{
							filasInforme++;
							data[fila][0]= new String( insTienda.getNombreInsumo());
							cadaJSON.put("nombreinsumo", insTienda.getNombreInsumo());
							//EN este punto llenamos lo que tiene la tienda
							data[fila][1]= insTienda.getCantidad();
							cadaJSON.put("cantidadtienda", insTienda.getCantidad());
							if(insReqTienda.getManejacanasta().equals("S"))
							{
								//En este punto deberemos de controlar como se maneja el insumo si este controla o no
								// por cantidad y no por minimo de almacenamiento
								if(insTienda.getControlCantidad() == 1)
								{
									//Si se hace el control por cantidad Mínima entonces se valida si lo que tiene 
									//la tienda es menor o igual a lo que se debe tener como cantidad mínima
									if(insTienda.getCantidad() <= insReqTienda.getCantidadMinima())
									{
										//Si esto se cumple se debe llevar el valor de insumo requerido con el valor de cantidad
										cantidadLlevar = insReqTienda.getCantidad();
									}
									else
									{
										// sino se tiene menos del mínimo entonces no se debe llevar nada
										cantidadLlevar = 0;
									}
								}
								else{
									cantidadLlevar =  insReqTienda.getCantidad() - insTienda.getCantidad();
								}
								int canastas = (int)cantidadLlevar / insReqTienda.getCantidadxcanasta();
								int residuocanastas = (int)cantidadLlevar % insReqTienda.getCantidadxcanasta();
								if (residuocanastas > 0)
								{
									canastas++;
									cantidadLlevar = insReqTienda.getCantidadxcanasta()*canastas;
								}
								if (cantidadLlevar > 0)
								{
									cadaJSON.put("cantidadcanastas", canastas);
									data[fila][3]= canastas +  insReqTienda.getNombrecontenedor();
								}
								else
								{
									cadaJSON.put("cantidadcanastas", "");
									data[fila][3]= "";
								}
								
							}
							else
							{
								//Validamos si el insumo tienda hace el control cantidad Minima
								if(insTienda.getControlCantidad() == 1)
								{
									//Si se hace el control por cantidad Mínima entonces se valida si lo que tiene 
									//la tienda es menor o igual a lo que se debe tener como cantidad mínima
									if(insTienda.getCantidad() <= insReqTienda.getCantidadMinima())
									{
										//Si esto se cumple se debe llevar el valor de insumo requerido con el valor de cantidad
										cantidadLlevar = insReqTienda.getCantidad();
									}
									else
									{
										// sino se tiene menos del mínimo entonces no se debe llevar nada
										cantidadLlevar = 0;
									}
								}
								else{
									cantidadLlevar =  insReqTienda.getCantidad() - insTienda.getCantidad();
								}
								cadaJSON.put("cantidadcanastas", 0);
								data[fila][3]= 0;
							}
											
							if (cantidadLlevar < 0)
							{
								cantidadLlevar = 0;
							}
							break;
						}
						
						
					}
					cadaJSON.put("cantidadllevar", cantidadLlevar);
					data[fila][2]= cantidadLlevar;
					cadaJSON.put("nombrecontenedor", insReqTienda.getNombrecontenedor());
					cadaJSON.put("cantidadxcanasta", insReqTienda.getCantidadxcanasta());
					cadaJSON.put("manejacanastas", insReqTienda.getManejacanasta());
					listJSON.add(cadaJSON);
					//Aumentamos en uno el valor de la variable fila
					fila++;
				}
				
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
	            cellHeader.setCellValue(new HSSFRichTextString("LISTADO DE INSUMOS POR PUNTO DE VENTA \n" + nombreTienda ));
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
	          //Aplicamos los bordes a la región merge
	            cellRangeAddress = new CellRangeAddress(0, 0, 4, 6);
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
	            cellFila2.setCellValue("FECHA: " + fecha);
	            cellFila2.setCellStyle(cellInfoReporte);
	            cellFila2 = equitetasInfReporte.createCell((short) 4);
	            cellFila2.setCellValue("RESPONSABLE DE \n SEPARAR INSUMOS");
	            cellFila2.setCellStyle(cellInfoReporte);
	            cellFila2 = equitetasInfReporte.createCell((short) 5);
	            cellFila2.setCellValue("REVISION Y \n VERIFICACIÓN LIDER \n CALIDAD Y LOG");
	            cellFila2.setCellStyle(cellInfoReporte);
	            cellFila2 = equitetasInfReporte.createCell((short) 6);
	            cellFila2.setCellValue("VERIFICADO POR \n ADMON EN PUNTO DE \n VENTA");
	            cellFila2.setCellStyle(cellInfoReporte);
				//Etiquetas reporte
	            HSSFRow equitetasRow = sheet.createRow(2);
		        for (int i = 0; i < headers.length; ++i) {
		            String header = headers[i];
		            HSSFCell cell = equitetasRow.createCell(i);
		            cell.setCellValue(header);
		            cell.setCellStyle(styleEnc);
		            if(i == headers.length -1)
		            {
		            	cell = equitetasRow.createCell(4);
		            	cell.setCellStyle(styleEnc);
		            	cell = equitetasRow.createCell(5);
		            	cell.setCellStyle(styleEnc);
		            	cell = equitetasRow.createCell(6);
		            	cell.setCellStyle(styleEnc);
		            }
		        }
		        
		        //
		        Cell datos;
		        for (int i = 0; i < filasInforme; ++i) {
		            HSSFRow dataRow = sheet.createRow(i + 3);
		            
		            Object[] d = data[i];
		            
		            String nombreInsumo = (String) d[0];
		            
		            //Aqui se puede dar un problema cuando se este agregando el inventario
		            double cantidadTienda = (double) d[1];
		            
		            double cantidadLlevar = (double) d[2];
		            
		            String empaque = "";
		            try
		            {
		            	empaque = (String) d[3];
		            }catch(Exception e)
		            {
		            	empaque = "";
		            }
		            
		            datos = dataRow.createCell(0);
		            datos.setCellValue(nombreInsumo);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(1);
		            datos.setCellValue(cantidadTienda);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(2);
		            datos.setCellValue(cantidadLlevar);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(3);
		            datos.setCellValue(empaque);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(4);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(5);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(6);
		            datos.setCellStyle(styleInfRep);
		        }
		        //Agregamos el estilo para el pie de página
		        //Creamos el estilo para la segunda fila de información
	            Font fontFinal = workbook.createFont();
	            fontFinal.setFontHeightInPoints((short) 8.00);
	            fontFinal.setBold(true);
	            HSSFCellStyle cellInfoFinal = workbook.createCellStyle();
	            cellInfoFinal.setBorderBottom(BorderStyle.THIN);
	            cellInfoFinal.setBorderTop(BorderStyle.THIN);
	            cellInfoFinal.setBorderLeft(BorderStyle.THIN);
	            cellInfoFinal.setBorderRight(BorderStyle.THIN);
	            cellInfoFinal.setWrapText(true);
	            cellInfoFinal.setFont(fontFinal);
	            cellInfoFinal.setAlignment(HorizontalAlignment .CENTER);
	            
	            HSSFCellStyle cellInfoOBS = workbook.createCellStyle();
	            cellInfoOBS.setBorderBottom(BorderStyle.THIN);
	            cellInfoOBS.setBorderTop(BorderStyle.THIN);
	            cellInfoOBS.setBorderLeft(BorderStyle.THIN);
	            cellInfoOBS.setBorderRight(BorderStyle.THIN);
	            cellInfoOBS.setWrapText(true);
	            cellInfoOBS.setFont(fontFinal);
	            cellInfoOBS.setAlignment(HorizontalAlignment.LEFT);
	            	            
		        
		        //Agreamos los items del final del formato
		        int filaFinal = filasInforme + 3;
		        HSSFRow ultDatos = sheet.createRow(filaFinal);
		        datos = ultDatos.createCell(0);
		        datos.setCellValue("HORA DE SALIDA PLANTA:\n");
		        datos.setCellStyle(cellInfoFinal);
		        datos = ultDatos.createCell(1);
		        datos.setCellStyle(cellInfoFinal);
		        datos = ultDatos.createCell(2);
		        datos.setCellValue("HORA DE LLEGADA A PLANTA :\n");
		        datos.setCellStyle(cellInfoFinal);
		        datos = ultDatos.createCell(4);
		        datos.setCellValue("FIRMA\n \n");
		        datos.setCellStyle(cellInfoFinal);
		        datos = ultDatos.createCell(5);
		        datos.setCellValue("FIRMA\n \n");
		        datos.setCellStyle(cellInfoFinal);
		        datos = ultDatos.createCell(6);
		        datos.setCellValue("FIRMA\n \n");
		        datos.setCellStyle(cellInfoFinal);
		        //Hacemos merge a varias filas
		        sheet.addMergedRegion(CellRangeAddress.valueOf("$C$"+(filaFinal+1)+":$D$" + (filaFinal+1)));
		        cellRangeAddress = new CellRangeAddress(filaFinal, filaFinal, 2, 3);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
	            int filaFirmaIni = filaFinal+1;
	            int filaFimraFin  = filaFinal+2;
		        sheet.addMergedRegion(CellRangeAddress.valueOf("$E$"+(filaFirmaIni)+":$E$" + (filaFimraFin)));
		        cellRangeAddress = new CellRangeAddress((filaFirmaIni-1), (filaFimraFin-1), 4, 4);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
		        sheet.addMergedRegion(CellRangeAddress.valueOf("$F$"+(filaFirmaIni)+":$F$" + (filaFimraFin)));
		        cellRangeAddress = new CellRangeAddress((filaFirmaIni-1), (filaFimraFin-1), 5, 5);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
		        sheet.addMergedRegion(CellRangeAddress.valueOf("$G$"+(filaFirmaIni)+":$G$" + (filaFimraFin)));
		        cellRangeAddress = new CellRangeAddress((filaFirmaIni-1), (filaFimraFin-1), 6, 6);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
	            filaFinal++;
	            ultDatos = sheet.createRow(filaFinal);
		        datos = ultDatos.createCell(0);
		        datos.setCellValue("HORA DE LLEGADA PUNTO DE VENTA:\n");
		        datos.setCellStyle(cellInfoFinal);
		        datos = ultDatos.createCell(1);
		        datos.setCellStyle(cellInfoFinal);
		        datos = ultDatos.createCell(2);
		        datos.setCellValue("HORA DE SALIDA DE PUNTO DE VENTA :\n");
		        datos.setCellStyle(cellInfoFinal);
		      //Hacemos merge a varias filas
		        sheet.addMergedRegion(CellRangeAddress.valueOf("$C$"+(filaFinal+1)+":$D$" + (filaFinal+1)));
		        cellRangeAddress = new CellRangeAddress(filaFinal, filaFinal, 2, 3);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
	            filaFinal++;
	            ultDatos = sheet.createRow(filaFinal);
	            datos = ultDatos.createCell(0);
		        datos.setCellValue("OBSERVACIONES:\n");
		        datos.setCellStyle(cellInfoOBS);
	            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(filaFinal+1)+":$G$" + (filaFinal+4)));
		        cellRangeAddress = new CellRangeAddress(filaFinal, filaFinal+3, 0, 6);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
	            
		    workbook.write(fileOut);
			fileOut.close();
			
		}catch(Exception e)
		{
			System.out.println("problemas en la generacion del archivo " + e.toString() + e.getMessage() + e.getStackTrace().toString() );
		}
		return(rutaArchivoGenerado);
        
	}
	
	/**
	 * Método en la clase controladora que se encarga de realizar la inserción del encabezado del despacho de pedido y de hacer la 
	 * interface con la capa DAO.
	 * @param idtienda El id de la tienda a la cual se le guardará el envío de insumos.
	 * @param fechasurtir Fecha que relaciona el envió del pedido a la tienda
	 * @return El sistema retornará el iddespacho que hace las veces de encabezado de despacho de pedido.
	 */
	public String InsertarInsumoDespachoTienda(int idtienda, String fechasurtir)
	{
		JSONArray listJSON = new JSONArray();
		int iddespacho = InventarioDAO.InsertarInsumoDespachoTienda(idtienda, fechasurtir);
		JSONObject Respuesta = new JSONObject();
		Respuesta.put("iddespacho", iddespacho);
		listJSON.add(Respuesta);
		return(listJSON.toJSONString());
	}
	
	public String InsertarDetalleInsumoDespachoTienda(int iddespacho,int idinsumo, double cantidad, String contenedor)
	{
		JSONArray listJSON = new JSONArray();
		int iddespachodetalle = InventarioDAO.InsertarDetalleInsumoDespachoTienda(iddespacho,idinsumo,cantidad,contenedor);
		JSONObject Respuesta = new JSONObject();
		Respuesta.put("iddespachodetalle", iddespacho);
		listJSON.add(Respuesta);
		return(listJSON.toJSONString());
	}
	
	public String ConsultarInventariosDespachados(int idtienda, String fecha)
	{
		ArrayList<InsumoDespachadoTienda> insumosDespachadosTienda = InventarioDAO.ConsultarInventariosDespachados(idtienda, fecha);
		JSONArray listJSON = new JSONArray();
		for (InsumoDespachadoTienda insDespTienda : insumosDespachadosTienda)
		{
			JSONObject Respuesta = new JSONObject();
			Respuesta.put("idinsumo", insDespTienda.getIdinsumo());
			Respuesta.put("nombreinsumo", insDespTienda.getNombreInsumo());
			Respuesta.put("cantidadsurtir", insDespTienda.getCantidadSurtir());
			Respuesta.put("contenedor", insDespTienda.getContenedor());
			Respuesta.put("unidadmedida", insDespTienda.getUnidadMedida());
			listJSON.add(Respuesta);
		}
			return(listJSON.toJSONString());
	}
	
	public String ConsultarInventarioTienda(int idtienda)
	{
		ArrayList<InsumoTienda> insumosTienda = InventarioDAO.ConsultarInsumosTienda(idtienda);
		JSONArray listJSON = new JSONArray();
		for (InsumoTienda insTienda : insumosTienda)
		{
			JSONObject Respuesta = new JSONObject();
			Respuesta.put("idinsumo", insTienda.getIdinsumo());
			Respuesta.put("nombreinsumo", insTienda.getNombreInsumo());
			Respuesta.put("cantidad", insTienda.getCantidad());
			Respuesta.put("fechainsercion", insTienda.getFecha());
			listJSON.add(Respuesta);
		}
			return(listJSON.toJSONString());
	}

	/**
	 *Este método se encarga de recorrer una a un las tiendas y verificar si el día en cuestión se surte y si es el caso 
	 *enviar un correo con el calculo de los viajes en un archivo en formato excel.
	 */
	public void CalcularInventariosTiendas()
	{
		//Obtengo las tiendas parametrizadas en el sistema de inventarios
		
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendas();
		String[] rutasArchivos = new String[tiendas.size()];
		//Obtenemos la fecha actual, con base en la cual realizaremos el recorrido
		// En el String fecha guardaremos el contenido de la fecha
		Date fechaTemporal = new Date();
		DateFormat formatoFinal = new SimpleDateFormat("yyyy-MM-dd");
		String fecha="";
		try
		{
			fecha = formatoFinal.format(fechaTemporal);
			System.out.println("fecha transformada " + fecha );
			
		}catch(Exception e){
			System.out.println("Problema transformando la fecha actual " + e.toString());
		}
		int fila = 0;
		for(Tienda tien : tiendas)
		{
			int idtienda = tien.getIdTienda();
			try
			{
				
					rutasArchivos[fila] = CalcularInventarioTiendaFormatoExcel(idtienda, fecha);
				
				
				
			}
			catch(Exception e)
			{
				System.out.println(e.toString() + " " + e.fillInStackTrace() + " " + e.getMessage());
			}
			fila++;
			
		}
		
		//Realizamos el envío del correo electrónico con los archivos
		//Recorremos uno a
		
		Correo correo = new Correo();
		correo.setAsunto("INVENTARIOS A SURTIR TIENDAS PIZZA AMERICANA");
		correo.setContrasena("Pizzaamericana2017");
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTESURTIR");
		correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
		correo.setMensaje("A continuación todos los inventarios de las tiendas de pizza americana");
		correo.setRutasArchivos(rutasArchivos);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreo();
	}

public static void main(String[] args)
{
	InventarioCtrl inventario = new InventarioCtrl();
	inventario.CalcularInventariosTiendas();
	
}

}




