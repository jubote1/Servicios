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
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import capaDAOCC.RazonSocialDAO;
import capaModeloCC.RazonSocial;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteMensualDomiciliosCOM {
	
	
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
			//fechaActual = "2019-01-20";
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
		//Obtenemos el mes actual y año actual
		int mesActual = calendarioActual.get(Calendar.MONTH);
		int anoActual = calendarioActual.get(Calendar.YEAR);
		System.out.println("OJO ANO ACTUAL " + anoActual);
		//Teniendo el mes actual le restaremos un mes
		int mesAnterior = 0;
		int anoAnterior = 0;
		int diaAnterior = 0;
		double porcentajeComision = 0;
		double porcentajeIva = 0;
		if(mesActual == 1)
		{
			mesAnterior = 12;
			anoAnterior = anoActual - 1;
		}else
		{
			mesAnterior = mesActual - 1;
			anoAnterior = anoActual;
		}

		//Deberemos de obtener el valor del día, este se tomará de un parámetro.
		diaAnterior = ParametrosDAO.retornarValorNumericoLocal("DIAMESCORTEDOMICILIOS");
		porcentajeComision = ParametrosDAO.retornarValorNumericoLocal("PORCENTAJECOMDOMICILIOS");
		porcentajeIva = ParametrosDAO.retornarValorNumericoLocal("PORCENTAJEIVACOMISION");
		
		//Con los datos  objetnidos fijamos la fecha del calendario y de ahí obtenemos la fecha anterior
		calendarioActual.set(anoAnterior, mesAnterior, diaAnterior);
		
		//Llevamos a un string la fecha anterior para el cálculo de la venta
		datFechaAnterior = calendarioActual.getTime();
		fechaAnterior = dateFormat.format(datFechaAnterior);
		
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
			respuesta = respuesta + "<table border='2'> <tr> RESUMEN MENSUAL DOMICILIOS.COM RAZON SOCIAL " + razTemp.getNombreRazon() +  " </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Total Pedidos del MES</strong></td>"
					+  "<td><strong>Comisión Aproximada del MES</strong></td>"
					+  "<td><strong>Iva Aproximado del MES</strong></td>"
					+  "<td><strong>Total Pago a DOMICILIOS del MES</strong></td>"
					+  "<td><strong>Total Pagos ONLINE del MES</strong></td>"
					+  "<td><strong>Total Descuentos del MES</strong></td>"
					+  "</tr>";
			String[] resTemp;
			double totalPedidosMensual = 0;
			//Variable donde llevaremos la cuenta de los pedidos ONLINE
			double totalPedidosOnLine = 0;
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
					totalPedidosOnLine = totalPedidosOnLine + totalPedido;
				}
				
				totalPedidosMensual = totalPedidosMensual + totalPedido;
				//Validamos si el descuento es mayor a cero para acumularlo
				if(descuento > 0 )
				{
					totalDescuentos = totalDescuentos + descuento;
				}
			}
			double totalComision = (totalPedidosMensual*(porcentajeComision/100));
			double totalIva = (totalComision*(porcentajeIva/100));
			respuesta = respuesta + "<tr><td>" + formatea.format(totalPedidosMensual) + "</td><td>" + formatea.format(totalComision) + "</td><td>" + formatea.format(totalIva) + "</td><td>" + formatea.format((totalComision + totalIva)) + "</td><td>" + formatea.format(totalPedidosOnLine) + "</td><td>" + formatea.format(totalDescuentos) + "</td></tr>";
			respuesta = respuesta + "</table> <br/>";
			//Procedemos al envío del correo
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("DOMICILIOS.COM REPORTE MENSUAL DE COMISIÓN " + razTemp.getNombreRazon() + " " + razTemp.getIdentificacion() + " " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEDOMICILIOSCOMMENSUAL");
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuación el reporte Mensual de pedidos tomados para domicilios.com separados por razones sociales entre las fechas " + fechaAnterior + " - " + fechaActual +  ": \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
			
		}
		
	}
	
	

public static void main(String[] args)
{
	ReporteMensualDomiciliosCOM reporteDomicios = new ReporteMensualDomiciliosCOM();
	reporteDomicios.generarReporteDomiciliosCOM();
	//ConexionBaseDatos con = new ConexionBaseDatos();
	//Connection con1 = con.obtenerConexionBDTienda("");
	
}

}




