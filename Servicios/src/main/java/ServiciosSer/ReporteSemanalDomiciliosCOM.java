package ServiciosSer;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import CapaDAOSer.GeneralDAO;
import conexionINV.ConexionBaseDatos;
import utilidadesSer.ControladorEnvioCorreo;
import ModeloSer.CorreoElectronico;
import capaDAOCC.MarcacionAnulacionPedidoDAO;
import capaDAOCC.MarcacionCambioPedidoDAO;
import capaDAOCC.PedidoDAO;
import capaDAOCC.RazonSocialDAO;
import capaModeloCC.MarcacionAnulacionPedido;
import capaModeloCC.MarcacionCambioPedido;
import capaModeloCC.RazonSocial;
import ModeloSer.Correo;

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

public class ReporteSemanalDomiciliosCOM {
	
	
	public void generarReporteDomiciliosCOM()
	{
		DecimalFormat formatea = new DecimalFormat("###,###");
		//Obtenemos las razones sociales que vamos a procesar
		ArrayList<RazonSocial> razonesSociales = RazonSocialDAO.obtenerTiendas();
		//Posteriormente realizamos el procesamiento para definir el rango de fechas del cual deseamos procesar el reporte
		//Recuperamos la fecha actual del sistema con la fecha apertura
		String fechaActual = "";
		//Variables donde manejaremos la fecha anerior con el fin realizar los cálculos de ventas
		Date datFechaAnterior;
		String fechaAnterior = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		//Obtenemos la fecha Actual
		try
		{
			//OJO
			fechaActual = dateFormat.format(calendarioActual.getTime());
			//fechaActual = "2021-02-01";
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
		
		//En este punto ya tenemos FechaActual y fechaAnterior, con estas dos iremos a obtener los pedidos para la presentación pero esto lo haremos en un ciclo for por razón social.
		RazonSocial razTemp;
		//Recuperamos el idProducto asociado a domicilios.com
		for(int i = 0; i < razonesSociales.size(); i++)
		{
			razTemp = razonesSociales.get(i);
			//Con la razón social y con la fecha podemos ir a realizar la consulta de los pedidos de domicilios.com
			ArrayList pedidosDomCOM = PedidoDAO.obtenerPedidosDomiciliosCOM(razTemp.getIdRazon(), fechaAnterior, fechaActual);
			//Obtenemos un total por tienda de los pedidos
			ArrayList pedidosDomCOMTienda = PedidoDAO.obtenerPedidosDomiciliosCOMTienda(razTemp.getIdRazon(), fechaAnterior, fechaActual);
			//Obtenemos totales de pago online por tienda
			ArrayList pedidosDomCOMONLINETienda = PedidoDAO.obtenerPedidosDomiciliosCOMONLINETienda(razTemp.getIdRazon(), fechaAnterior, fechaActual);
			//Obtenemos totales de pago online por tienda
			ArrayList descuentosDomCOMTienda = PedidoDAO.obtenerDescuentosDomiciliosCOMTienda(razTemp.getIdRazon(), fechaAnterior, fechaActual);
			//Procedemos a procesar la información y a enviar el correo con el reporte
			String respuesta = "";
			respuesta = respuesta + "<table border='2'> <tr> RESUMEN SEMANAL DOMICILIOS.COM RAZON SOCIAL " + razTemp.getNombreRazon() +  " </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Tienda del Pedido</strong></td>"
					+  "<td><strong>Valor Neto Pedido</strong></td>"
					+  "<td><strong>Fecha Pedido</strong></td>"
					+  "<td><strong># Pedido Contact</strong></td>"
					+  "<td><strong># Pedido Tienda</strong></td>"
					+  "<td><strong># Pedido Domicilios.com</strong></td>"
					+  "<td><strong>Descuento</strong></td>"
					+  "<td><strong>Mot/Desc</strong></td>"
					+  "<td><strong>Forma de Pago</strong></td>"
					+  "</tr>";
			String[] resTemp;
			double totalPedidosSemana = 0;
			//Variable donde llevaremos la cuenta de los pedidos ONLINE
			double totalPagoOnLine = 0;
			//Llevare el control del total de descuentos
			double totalDescuentos = 0;
			//Variable donde iremos almacenando cada uno de los descuentos
			double descuento = 0;
			double totalPedido = 0;
			//Variable donde almacenamos la forma de pago
			String formaPago = "";
			int idFormaPago = 0;
			for(int y = 0; y < pedidosDomCOM.size();y++)
			{
				resTemp = (String[]) pedidosDomCOM.get(y);
				idFormaPago = Integer.parseInt(resTemp[6]);
				try 
				{
					descuento = Double.parseDouble(resTemp[7]);
				}catch(Exception e)
				{
					descuento = 0;
				}
				//Ya realizamos los cambios pora que el total_neto tenga el descuento por lo tanto no es necesario
				//totalPedido = Double.parseDouble(resTemp[1]) - descuento;
				totalPedido = Double.parseDouble(resTemp[1]);
				if(idFormaPago == 1)
				{
					formaPago = "EFE";
				}else if(idFormaPago == 2)
				{
					formaPago = "TAR";
				}else if(idFormaPago == 3)
				{
					formaPago = "ONLINE";
					totalPagoOnLine = totalPagoOnLine + totalPedido;
				}
				respuesta = respuesta + "<tr><td>" + resTemp[0] + "</td><td>" + formatea.format(totalPedido) + "</td><td>" + resTemp[2] + "</td><td>" + resTemp[3] + "</td><td>" + resTemp[4] + "</td><td>" + resTemp[5] + "</td><td>" + formatea.format(descuento) + "</td><td>" + resTemp[8] + "</td><td>"  + formaPago + "</td></tr>";
				totalPedidosSemana = totalPedidosSemana + totalPedido;
				//Validamos si el descuento es mayor a cero para acumularlo
				if(descuento > 0 )
				{
					totalDescuentos = totalDescuentos + descuento;
				}
			}
			respuesta = respuesta + "</table> <br/>";
			respuesta = respuesta + "<b>TOTAL PEDIDOS SEMANA " + formatea.format(totalPedidosSemana) +"</b><br/>";
			respuesta = respuesta + "<b>TOTAL PEDIDOS PAGO-ONLINE " + formatea.format(totalPagoOnLine) +"</b><br/>";
			respuesta = respuesta + "<b>TOTAL DE DESCUENTOS EN LA SEMANA " + formatea.format(totalDescuentos) +"</b><br/>";
			
			//Agregamos en este apartado el total de pedidos por tienda para poder extraer la comisión por tienda
			respuesta = respuesta + "<table border='2'> <tr> TOTAL POR TIENDA " + razTemp.getNombreRazon() +  " </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Tiendao</strong></td>"
					+  "<td><strong>Total Pedidos</strong></td>"
					+"</tr>";
			String[] resTotalTienda;
			for(int j = 0; j < pedidosDomCOMTienda.size(); j++)
			{
				resTotalTienda = (String[]) pedidosDomCOMTienda.get(j);
				respuesta = respuesta + "<tr><td>" + resTotalTienda[0] + "</td><td>" + formatea.format(Double.parseDouble(resTotalTienda[1])) + "</td></tr>";
			}
			respuesta = respuesta + "</table> <br/>";
			
			
			//Agregaremos el TOTAL de pago ONLINE por tienda
			respuesta = respuesta + "<table border='2'> <tr> TOTAL PAGO ONLINE POR TIENDA " + razTemp.getNombreRazon() +  " </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Tienda</strong></td>"
					+  "<td><strong>Total Pedidos ONLINE</strong></td>"
					+  "<td><strong>FORMA DE PAGO</strong></td>"
					+"</tr>";
			for(int j = 0; j < pedidosDomCOMONLINETienda.size(); j++)
			{
				resTotalTienda = (String[]) pedidosDomCOMONLINETienda.get(j);
				respuesta = respuesta + "<tr><td>" + resTotalTienda[0] + "</td><td>" + formatea.format(Double.parseDouble(resTotalTienda[1])) + "</td><td>" + resTotalTienda[2] + "</td></tr>";
			}
			respuesta = respuesta + "</table> <br/>";
			
			
			//Agregaremos el TOTAL de descuentos por tienda
			respuesta = respuesta + "<table border='2'> <tr> TOTAL DESCUENTOS POR TIENDA " + razTemp.getNombreRazon() +  " </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Tienda</strong></td>"
					+  "<td><strong>Total Descuentos</strong></td>"
					+"</tr>";
			for(int j = 0; j < descuentosDomCOMTienda.size(); j++)
			{
				resTotalTienda = (String[]) descuentosDomCOMTienda.get(j);
				respuesta = respuesta + "<tr><td>" + resTotalTienda[0] + "</td><td>" + formatea.format(Double.parseDouble(resTotalTienda[1])) + "</td></tr>";
			}
			respuesta = respuesta + "</table> <br/>";
			
			//Continuamos con las anulaciones que deben realizarse por razón zocial y por rango de fechas
			ArrayList<MarcacionAnulacionPedido> marAnulaciones = MarcacionAnulacionPedidoDAO.consultarMarcacionAnulacion(fechaAnterior, fechaActual, razTemp.getIdRazon());
			respuesta = respuesta + "<table border='2'> <tr> RESUMEN SEMANAL POSIBLES ANULACIONES DOMICILIOS.COM RAZON SOCIAL " + razTemp.getNombreRazon() +  " </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>ID Pedido Contact</strong></td>"
					+  "<td><strong>Num PosHeader</strong></td>"
					+  "<td><strong>Fecha Pedido</strong></td>"
					+  "<td><strong>Total Neto</strong></td>"
					+  "</tr>";
			MarcacionAnulacionPedido marAnuTemp;
			for(int y = 0; y < marAnulaciones.size(); y++)
			{
				marAnuTemp = marAnulaciones.get(y);
				respuesta = respuesta + "<tr><td>" + marAnuTemp.getIdPedido() + "</td><td>" + marAnuTemp.getNumPosHeader() + "</td><td>" + marAnuTemp.getFechaPedido() + "</td><td>" + formatea.format(marAnuTemp.getTotalNeto()) + "</td></tr>";
				totalPedidosSemana = totalPedidosSemana - marAnuTemp.getTotalNeto();
			}
			respuesta = respuesta + "</table> <br/>";
			respuesta = respuesta + "<b>TOTAL PEDIDOS SEMANA MENOS LAS ANULACIONES " + formatea.format(totalPedidosSemana) +"</b>";
			
			
			//Continuamos con los cambiso de pedidos para alertar y qeu se revisen si es el caso
			ArrayList<MarcacionCambioPedido> marCambios = MarcacionCambioPedidoDAO.consultarMarcacionCambio(fechaAnterior, fechaActual, razTemp.getIdRazon());
			respuesta = respuesta + "<table border='2'> <tr> RESUMEN SEMANAL POSIBLES CAMBIO DE PEDIDO DOMICILIOS.COM RAZON SOCIAL " + razTemp.getNombreRazon() +  " </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>ID Pedido Contact</strong></td>"
					+  "<td><strong>Num PosHeader</strong></td>"
					+  "<td><strong>Fecha Pedido</strong></td>"
					+  "<td><strong>Total Neto Contact</strong></td>"
					+  "<td><strong>Total Neto Tienda</strong></td>"
					+  "</tr>";
			MarcacionCambioPedido marCambioTemp;
			for(int y = 0; y < marCambios.size(); y++)
			{
				marCambioTemp = marCambios.get(y);
				respuesta = respuesta + "<tr><td>" + marCambioTemp.getIdPedido() + "</td><td>" + marCambioTemp.getNumPosHeader() + "</td><td>" + marCambioTemp.getFechaPedido() + "</td><td>" + formatea.format(marCambioTemp.getTotalNetoContact()) + "</td><td>" + formatea.format(marCambioTemp.getTotalNetoTienda()) + "</td></tr>";
			}
			respuesta = respuesta + "</table> <br/>";
						
			//Procedemos al envío del correo
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("Reporte Semanal Domicilios.com de la Razón Social " + razTemp.getNombreRazon() + " " + razTemp.getIdentificacion());
			correo.setContrasena(infoCorreo.getClaveCorreo());
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEDOMICILIOSCOM");
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuación el reporte semanal de pedidos tomados para domicilios.com separados por razones sociales entre las fechas " + fechaAnterior + " - " + fechaActual +  ": \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
			
		}
		
	}
	
	

public static void main(String[] args)
{
	ReporteSemanalDomiciliosCOM reporteDomicios = new ReporteSemanalDomiciliosCOM();
	reporteDomicios.generarReporteDomiciliosCOM();
	//ConexionBaseDatos con = new ConexionBaseDatos();
	//Connection con1 = con.obtenerConexionBDTienda("");
	
}

}




