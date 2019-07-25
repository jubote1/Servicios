package Servicios;

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

import Modelo.Correo;
import Modelo.RazonSocial;
import utilidades.ControladorEnvioCorreo;

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

import CapaDAOServicios.GeneralDAO;
import CapaDAOServicios.ParametrosDAO;
import CapaDAOServicios.PedidoDAO;
import CapaDAOServicios.RazonSocialDAO;

public class ReporteSemanalONLINE {
	
	
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
			//fechaActual = "2019-07-25";
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
		
		//Necesitamos validar si es un martes o un jueves
		int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
		//Es día martes
		if(diaActual == 3)
		{
			calendarioActual.add(Calendar.DAY_OF_YEAR, -5);
			datFechaAnterior = calendarioActual.getTime();
			fechaActual = dateFormat.format(datFechaAnterior);
			calendarioActual.add(Calendar.DAY_OF_YEAR, -4);
			datFechaAnterior = calendarioActual.getTime();
			fechaAnterior = dateFormat.format(datFechaAnterior);
		//Es día jueves
		}else if(diaActual == 5)
		{
			calendarioActual.add(Calendar.DAY_OF_YEAR, -5);
			datFechaAnterior = calendarioActual.getTime();
			fechaActual = dateFormat.format(datFechaAnterior);
			calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
			datFechaAnterior = calendarioActual.getTime();
			fechaAnterior = dateFormat.format(datFechaAnterior);
		}else
		{
			return;
		}
		
		//En este punto ya tenemos calculado las fechas del reporte
		
		//En este punto ya tenemos FechaActual y fechaAnterior, con estas dos iremos a obtener los pedidos.
		RazonSocial razTemp;
		//Recuperamos el idProducto asociado a domicilios.com
		for(int i = 0; i < razonesSociales.size(); i++)
		{
			razTemp = razonesSociales.get(i);
			//Con la razón social y con la fecha podemos ir a realizar la consulta de los pedidos de domicilios.com
			ArrayList pedidosDomCOM = PedidoDAO.obtenerPedidosDomiciliosCOM(razTemp.getIdRazon(), fechaAnterior, fechaActual);
			//Procedemos a procesar la información y a enviar el correo con el reporte
			String respuesta = "";
			respuesta = respuesta + "<table border='2'> <tr> RESUMEN PEDIDOS ON LINE DE LA SEMANA VENCIDA " + razTemp.getNombreRazon() +  " </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Cantidad de Pedidos con Pago ONLINE</strong></td>"
					+  "<td><strong>Valor Total de PAGO ONLINE</strong></td>"
					+  "</tr>";
			String[] resTemp;
			double totalPagoONLINE = 0;
			int cantidadPedidoONLINE = 0;
			//Variable donde iremos almacenando cada uno de los descuentos
			double descuento = 0;
			double totalPedido = 0;
			//Variable donde almacenamos la forma de pago
			String formaPago = "";
			int idFormaPago = 0;
			for(int y = 0; y < pedidosDomCOM.size();y++)
			{
				resTemp = (String[]) pedidosDomCOM.get(y);
				try 
				{
					descuento = Double.parseDouble(resTemp[7]);
				}catch(Exception e)
				{
					descuento = 0;
				}
				totalPedido = Double.parseDouble(resTemp[1]) - descuento;
				if(idFormaPago == 1)
				{
					formaPago = "EFE";
				}else if(idFormaPago == 2)
				{
					formaPago = "TAR";
				}else if(idFormaPago == 3)
				{
					formaPago = "ONLINE";
					totalPagoONLINE = totalPagoONLINE + totalPedido;
					cantidadPedidoONLINE++;
				}
			}
			respuesta = respuesta + "<tr><td>" + formatea.format(cantidadPedidoONLINE) + "</td><td>" + formatea.format(totalPagoONLINE) + "</td></tr>";
			respuesta = respuesta + "</table> <br/>";
			//Procedemos al envío del correo
			Correo correo = new Correo();
			correo.setAsunto("DOMICILIOS.COM REPORTE SEMANAL PAGOS ON LINE " + razTemp.getNombreRazon() + " " + razTemp.getIdentificacion() + " " + fechaActual);
			correo.setContrasena("Pizzaamericana2017");
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTESEMONLINEDOMICILIOS");
			correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
			correo.setMensaje("A continuación el reporte SEMANAL de PAGOS ONLINE para domicilios.com separados por razones sociales entre las fechas " + fechaAnterior + " - " + fechaActual +  ": \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
			
		}
		
	}
	
	

public static void main(String[] args)
{
	ReporteSemanalONLINE reporteDomicios = new ReporteSemanalONLINE();
	reporteDomicios.generarReporteDomiciliosCOM();
	//ConexionBaseDatos con = new ConexionBaseDatos();
	//Connection con1 = con.obtenerConexionBDTienda("");
	
}

}




