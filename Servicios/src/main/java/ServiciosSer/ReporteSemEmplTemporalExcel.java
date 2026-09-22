package ServiciosSer;

import java.io.FileOutputStream;
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
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

import CapaDAOSer.EmpleadoTemporalDiaDAO;
import CapaDAOSer.EmpresaTemporalDAO;
import CapaDAOSer.GastoEmpleadoTemporalDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiempoPedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.DiaFestivo;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.EmpleadoTemporalDia;
import ModeloSer.EmpresaTemporal;
import ModeloSer.GastoEmpleadoTemporal;
import ModeloSer.Pedido;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import utilidadesSer.ControladorEnvioCorreo;
import utilidadesSer.CorreoHtml;

public class ReporteSemEmplTemporalExcel {
	
			
		
	public static void main( String[] args )
	        
	{
		//Ruta donde generaremos los archivos para envio
		String rutaArchivoGenerado="";
		String rutaArchivoBD = ParametrosDAO.retornarValorAlfanumericoLocal("RUTAARCHIVOEMPTEMP");
		//Creamos el libro en Excel y la hoja en cuesti�n, definimos los encabezados.
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("RESUMEN SEM-EMP TEM");
		sheet.setColumnWidth(0, 10000);
		sheet.setColumnWidth(1, 4500);
		sheet.setColumnWidth(2, 5500);
		sheet.setColumnWidth(3, 5500);
		sheet.setColumnWidth(4, 3000);
		sheet.setColumnWidth(5, 4500);
		sheet.setColumnWidth(6, 15000);
		sheet.setColumnWidth(7, 40000);
		String[] headers = new String[]{
	            "PERSONAL",
	            "FECHA",
	            "HORA INGRESO",
	            "HORA SALIDA",
	            "HOR TRABAJADAS",
	            "VALOR A PAGAR",
	            "OBSERVACION",
	            "NUM PEDIDOS"
			        };
		//Requerimos primero que todo obtener el rango de fechas con el fin de tener dicho rango para las consultas
		//Definimos el formato como manejaremos las fechas
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DecimalFormat formatea = new DecimalFormat("###,###.##");
		//Traemos la fechaActual en Blanco
		String fechaActual = "";
		//Traemos la fecha actual en un date
		Date datFechaActual = new Date();
		//Comenzamos a traer la fecha actual como un String
		fechaActual = dateFormat.format(datFechaActual);
		
		//Variables donde manejaremos la fecha anerior con el fin realizar el rango de los facturado por empleados temporales
		Date datFechaAnterior;
		String fechaAnterior = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		Calendar calendarioTrans = Calendar.getInstance();
		//Obtenemos la fecha Actual
		try
		{
			//OJO
			fechaActual = dateFormat.format(calendarioActual.getTime());
			//fechaActual = "2020-07-26";
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
		
		//AQUI INICIARA LA CREACION DEL ARCHIVO EN EXCEL PARA ESTE FIN
		try
		{
			   rutaArchivoGenerado = rutaArchivoBD  + "ReporteSemEmpTemporal" + "-" + fechaAnterior + "--" + fechaActual +".xls";
			   
			   FileOutputStream fileOut = new FileOutputStream(rutaArchivoGenerado);
			   
			   rutaArchivoGenerado = rutaArchivoGenerado + "%&" + "ReporteSemEmpTemporal" + "-" + fechaAnterior + "--" + fechaActual +".xls";
		
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
	            
	            //Creamos el estilo para la informacion del reporte
	            HSSFCellStyle styleInfRep = workbook.createCellStyle();
	            styleInfRep.setBorderBottom(BorderStyle.THIN);
	            styleInfRep.setBorderTop(BorderStyle.THIN);
	            styleInfRep.setBorderLeft(BorderStyle.THIN);
	            styleInfRep.setBorderRight(BorderStyle.THIN);
	            styleInfRep.setWrapText(true);

	            //Fila en rojo claro para cuando la hora de salida no se pudo interpretar.
	            HSSFCellStyle styleError = workbook.createCellStyle();
	            styleError.cloneStyleFrom(styleInfRep);
	            styleError.setFillForegroundColor(IndexedColors.ROSE.index);
	            styleError.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
	            int filaActual = 1;
		
				//El detalle dia a dia va en el Excel adjunto; en el correo solo queda el resumen por tienda.
				ArrayList<Object[]> resumenTiendas = new ArrayList<Object[]>();
				//Recuperaremos las tiendas y empezaremos a ir consultando una a una las tiendas para extraer la informaci�n
				ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
				//Recuperamos las empresas temporales de la base de datos general
				ArrayList<EmpresaTemporal> empresasTemp = EmpresaTemporalDAO.retornarEmpresasTemporales();
				//Se recuperan los d�as festivos
				ArrayList<DiaFestivo> festivos = GeneralDAO.obtenerDiasFestivos();
				double valorHoraNormal;
				double valorHoraDominical;
				double totalEmpresa = 0;
				double horasTrabajadas = 0;
				double valorHoraTrabajada = 0;
				boolean errorConversion = false;
				boolean esDomingo = false;
				double totalTienda;
				for(Tienda tien : tiendas)
				{
					totalTienda = 0;
					if(!tien.getHostBD().equals(new String("")))
					{
						for(EmpresaTemporal empTemp: empresasTemp)
						{
							totalEmpresa = 0;
							valorHoraNormal = empTemp.getValorHoraNormal();
							valorHoraDominical = empTemp.getValorHoraDominical();
							//Damos un salto adicional de separaci�n
							filaActual++;
							//Creamos Encabezado del reporte
							HSSFRow nombreEmpresa = sheet.createRow(filaActual);
							Cell cellFila = nombreEmpresa.createCell((short) 0);
							cellFila.setCellValue(tien.getNombreTienda() + " - " + empTemp.getNombreEmpresa() + "-" + empTemp.getValorHoraNormal() + "-" + empTemp.getValorHoraDominical());
							cellFila.setCellStyle(cellheader);
							filaActual++;
							//Continuamos con los encabezados
							HSSFRow encabezados = sheet.createRow(filaActual);
							for (int col = 0; col < headers.length; col++) {
								Cell cellFilaEncabezado = encabezados.createCell((short) col);
								cellFilaEncabezado.setCellValue(headers[col]);
								cellFilaEncabezado.setCellStyle(cellInfoReporte);
							}
							filaActual++;
							//Recuperamos los evento de empleados para la semana en cuesti�n
							ArrayList<capaModeloPOS.EmpleadoTemporalDia> empleadosTempDia = capaDAOPOS.EmpleadoTemporalDiaDAO.obtenerEmpleadoTemporalFecha(fechaActual, fechaAnterior, empTemp.getIdEmpresa(), tien.getHostBD());
							//Comenzamos a recorrer para ir presetnando la informaci�n
							for(capaModeloPOS.EmpleadoTemporalDia empleadoTemp : empleadosTempDia)
							{
								//Calculamos la cantidad de horas trabajadas
								//Intentamos realizar la conversi�n de las horas
								errorConversion = false;
								esDomingo = false;
								diaActual = 0;
								try
								{
									//Formateamos las fechas para posteriormente proceder a calcular el n�mero de horas trabajadas
									Date fechaIng = dateFormatHora.parse(empleadoTemp.getFechaSistema()+" "+empleadoTemp.getHoraIngreso());
									Date fechaSal = dateFormatHora.parse(empleadoTemp.getFechaSistema()+" "+empleadoTemp.getHoraSalida());
									//Pondremos un control por si hay error en la hora de salida del empleado temporal
									String hora = empleadoTemp.getHoraSalida().substring(0, 2);
									int intHora = 0;
									try
									{
										intHora = Integer.parseInt(hora);
									}catch(Exception e)
									{
										intHora  = 99;
									}
									//Si la hora es cero deberemos de sumar un d�a a la fechaSistema
									if(intHora == 0)
									{
										calendarioTrans.setTime(dateFormat.parse(empleadoTemp.getFechaSistema()));
										calendarioTrans.add(Calendar.DAY_OF_YEAR, 1);
										fechaSal = dateFormatHora.parse(dateFormat.format(calendarioTrans.getTime())+" "+empleadoTemp.getHoraSalida());
									}
									horasTrabajadas = ((fechaSal.getTime()-fechaIng.getTime())/1000);
									horasTrabajadas =(horasTrabajadas)/3600;
									//Fijar la fecha en el calendario para posteriormente saber si es domingo o no
									calendarioActual.setTime(fechaIng);
									diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
									//En caso de ser domingo debemos de prender un indicador que nos servir� para saber el valor de la hora
									if(diaActual ==  1)
									{
										esDomingo = true;
									}
									//Luego de la validaci�n de si es domingo hacemos la validaci�n de si es festivo
									boolean esFestivo = validarFestivo(festivos, empleadoTemp.getFechaSistema());
									if(esFestivo)
									{
										esDomingo = true;
									}
								}catch(Exception e)
								{
									errorConversion = true;
								}
								//En caso de ser domingo se hace c�lculo con la hora dominicial
								if(esDomingo)
								{
									valorHoraTrabajada = horasTrabajadas * valorHoraDominical;
								}else
								{
									valorHoraTrabajada = horasTrabajadas * valorHoraNormal;
								}
								//Se acumula el total de la empresa
								totalEmpresa  = totalEmpresa  + valorHoraTrabajada;
								//Incluimos el c�lculo de la cantidad de pedidos del domiciliario
								int cantidadPedidos = PedidoDAO.obtenerPedidosEntregados(empleadoTemp.getFechaSistema()+" "+empleadoTemp.getHoraIngreso(), empleadoTemp.getFechaSistema()+" "+empleadoTemp.getHoraSalida(), empleadoTemp.getId(), tien.getHostBD());
								//Si hay error de conversi�n de las fechas se muestra diferente.
								if(errorConversion)
								{
									//Realizamos pintado de la fila en el Excel de una fila de datos
									encabezados = sheet.createRow(filaActual);
									Cell cellFillaDatos = encabezados.createCell((short) 0);
									cellFillaDatos.setCellValue(empleadoTemp.getNombre());
									cellFillaDatos = encabezados.createCell((short) 1);
									cellFillaDatos.setCellValue(empleadoTemp.getFechaSistema());
									cellFillaDatos = encabezados.createCell((short) 2);
									cellFillaDatos.setCellValue(empleadoTemp.getHoraIngreso());
									cellFillaDatos = encabezados.createCell((short) 3);
									cellFillaDatos.setCellValue(empleadoTemp.getHoraSalida());
									cellFillaDatos = encabezados.createCell((short) 4);
									cellFillaDatos.setCellValue("ERROR CONVERSION");
									cellFillaDatos = encabezados.createCell((short) 5);
									cellFillaDatos.setCellValue("0");
									cellFillaDatos = encabezados.createCell((short) 6);
									cellFillaDatos.setCellValue(empleadoTemp.getObservacion());
									cellFillaDatos = encabezados.createCell((short) 7);
									cellFillaDatos.setCellValue(cantidadPedidos);
									for (int col = 0; col <= 7; col++) {
										encabezados.getCell(col).setCellStyle(styleError);
									}
									filaActual++;
								}else
								{
									//Realizamos pintado de la fila en el Excel de una fila de datos
									encabezados = sheet.createRow(filaActual);
									Cell cellFillaDatos = encabezados.createCell((short) 0);
									cellFillaDatos.setCellValue(empleadoTemp.getNombre());
									cellFillaDatos = encabezados.createCell((short) 1);
									cellFillaDatos.setCellValue(empleadoTemp.getFechaSistema());
									cellFillaDatos = encabezados.createCell((short) 2);
									cellFillaDatos.setCellValue(empleadoTemp.getHoraIngreso());
									cellFillaDatos = encabezados.createCell((short) 3);
									cellFillaDatos.setCellValue(empleadoTemp.getHoraSalida());
									cellFillaDatos = encabezados.createCell((short) 4);
									cellFillaDatos.setCellValue(formatea.format(horasTrabajadas));
									cellFillaDatos = encabezados.createCell((short) 5);
									cellFillaDatos.setCellValue(formatea.format(valorHoraTrabajada));
									cellFillaDatos = encabezados.createCell((short) 6);
									cellFillaDatos.setCellValue(empleadoTemp.getObservacion());
									cellFillaDatos = encabezados.createCell((short) 7);
									cellFillaDatos.setCellValue(cantidadPedidos);
									for (int col = 0; col <= 7; col++) {
										encabezados.getCell(col).setCellStyle(styleInfRep);
									}
									filaActual++;
								}
							}
							//Acumulamos el total para la tienda
							totalTienda = totalTienda + totalEmpresa;
						}
						resumenTiendas.add(new Object[] { tien.getNombreTienda(), Double.valueOf(totalTienda) });
					}
				}

					//En esta parte termina la generaci�n del correo
					workbook.write(fileOut);
					fileOut.close();

					//Buscamos la manera de enviar el correo
					String[] rutasArchivos = new String[1];
					rutasArchivos[0] = rutaArchivoGenerado;

					double totalGeneral = 0;
					for (int i = 0; i < resumenTiendas.size(); i++) {
						totalGeneral = totalGeneral + ((Double) resumenTiendas.get(i)[1]).doubleValue();
					}
					StringBuilder cuerpo = new StringBuilder();
					cuerpo.append(CorreoHtml.abrir("Reporte semanal de personal temporal",
							"Del " + fechaAnterior + " al " + fechaActual));
					cuerpo.append(CorreoHtml.abrirTabla("Resumen por tienda", "Tienda", "Total"));
					for (int i = 0; i < resumenTiendas.size(); i++) {
						cuerpo.append(CorreoHtml.fila((String) resumenTiendas.get(i)[0],
								CorreoHtml.pesos(((Double) resumenTiendas.get(i)[1]).doubleValue())));
					}
					cuerpo.append(CorreoHtml.filaTotal("TOTAL GENERAL", CorreoHtml.pesos(totalGeneral)));
					cuerpo.append(CorreoHtml.cerrarTabla());
					cuerpo.append(CorreoHtml.cerrar("El detalle dia a dia de cada persona va en el Excel adjunto."));

					//Recuperar la lista de distribuci�n para este correo
					ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPSEMEMPLTEMPORALEXCEL");
					Correo correo = new Correo();
					CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
					correo.setAsunto("Reporte semanal de personal temporal " + fechaAnterior + " al " + fechaActual
							+ " - " + CorreoHtml.pesos(totalGeneral));
					correo.setContrasena(infoCorreo.getClaveCorreo());
					correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
					//Anexamos el archivo generado
					correo.setRutasArchivos(rutasArchivos);
					correo.setMensaje(cuerpo.toString());
					ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
					contro.enviarCorreoHTMLAnexo();
		}catch(Exception e)
		{
			System.out.println("problemas en la generacion del archivo " + e.toString() + e.getMessage() + e.getStackTrace().toString() );
		}
		
	}
	
	public static boolean validarFestivo(ArrayList<DiaFestivo> festivos, String fechaActual )
	{
		DiaFestivo festivoTemp = new DiaFestivo(0,"");
		boolean respuesta = false;
		for(int i = 0; i < festivos.size(); i++)
		{
			festivoTemp = festivos.get(i);
			if(festivoTemp.getFechaFestiva().equals(fechaActual))
			{
				respuesta = true;
				break;
			}
		}
		return(respuesta);
	}
		
	
}

