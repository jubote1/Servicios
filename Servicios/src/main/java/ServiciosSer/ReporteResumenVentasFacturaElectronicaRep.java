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
import CapaDAOSer.TicketPromedioMesDAO;
import CapaDAOSer.TiendaDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.TicketPromedioMes;
import ModeloSer.Tienda;
import capaDAOCC.RazonSocialDAO;
import capaModeloCC.RazonSocial;
import capaControladorPOS.PedidoCtrl;
import capaModeloPOS.TicketPromedio;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteResumenVentasFacturaElectronicaRep {
	
	/**
	 * Partimos de la premisa que el proceso corre el 30 de cada mes
	 */
	public void generarReporte()
	{
		DecimalFormat formatea = new DecimalFormat("###,###");
		//Posteriormente realizamos el procesamiento para definir el rango de fechas del cual deseamos procesar el reporte
		//Recuperamos la fecha actual del sistema con la fecha apertura
		String fechaActual = "";
		//Variables donde manejaremos la fecha anerior con el fin realizar los c�lculos de ventas
		Date datFechaAnterior;
		String fechaAnterior = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		//Obtenemos la fecha Actual
		
		try
		{
			//OJO
			fechaActual = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
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
		//Obtenemos el mes actual y a�o actual
		int mesActual = calendarioActual.get(Calendar.MONTH)+1;
		int anoActual = calendarioActual.get(Calendar.YEAR);
		int diaActual = calendarioActual.get(Calendar.DAY_OF_MONTH);
		int diaMaximoMesActual = Calendar.getInstance().getActualMaximum(calendarioActual.DAY_OF_MONTH);
		fechaAnterior = Integer.toString(anoActual) + "-" + Integer.toString(mesActual) + "-01";
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();

		//Posteriormente realizamos el env�o del correo
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		ArrayList correos;
		ControladorEnvioCorreo contro;
		
		String respuesta = "";
		respuesta = respuesta + "<table border='2'> <tr> VENTA TOTALES POR TIENDA </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>NOMBRE TIENDA</strong></td>"
				+  "<td><strong>VENTA TOTAL MES</strong></td>"
				+  "<td><strong>VENTA TOTAL FE</strong></td>"
				+  "<td><strong>%</strong></td>"
				+  "</tr>";
		double ventaTotalTiendas = 0;
		double ventaTotalTiendasFE = 0;
		double ventaTienda;
		double ventaTiendaFE;
		double porVentaFE;
		for(Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				ventaTienda = PedidoDAO.obtenerTotalesPedidosSemana(fechaAnterior, fechaActual, tien.getHostBD());
				ventaTiendaFE = PedidoDAO.obtenerTotalesPedidosSemanaFE(fechaAnterior, fechaActual, tien.getHostBD());
				respuesta = respuesta + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(ventaTienda) +"</td><td>" + formatea.format(ventaTiendaFE)+  "</td><td>" + formatea.format((ventaTiendaFE/ventaTienda)*100) + "</td></tr>";
				//Realizamos la acumulaci�n despues de cada iteraci�n
				ventaTotalTiendas  = ventaTotalTiendas + ventaTienda;
				ventaTotalTiendasFE  = ventaTotalTiendasFE + ventaTiendaFE;
			}
		}
		respuesta = respuesta + "<tr><td> TOTAL TIENDAS </td><td>" + formatea.format(ventaTotalTiendas) + "</td></tr>";
		respuesta = respuesta + "<tr><td> TOTAL TIENDAS FE </td><td>" + formatea.format(ventaTotalTiendasFE) + "</td></tr>";
		respuesta = respuesta + "<tr><td>%</td><td>" + formatea.format((ventaTotalTiendasFE/ventaTotalTiendas)*100) + "</td></tr>";
		respuesta = respuesta + "</table> <br/>";
		
		
		//Agregamos los totales de PAGO VIRTUAL WOMPI
		//Mostraremos la tabla de venta en total por tienda
		double ventaTotalWompi = 0;
		ArrayList totalSemanaTienda = capaDAOCC.PedidoDAO.consultarPedidosVirtualTiendaSemana(fechaAnterior, fechaActual);
		for(int i = 0; i < totalSemanaTienda.size(); i++)
		{
			String[] fila = (String[]) totalSemanaTienda.get(i);
			ventaTotalWompi = ventaTotalWompi + Double.parseDouble(fila[0]);
		}
		
		//Obtenemos el total de pago con dat�fono
		double ventaTotalTarjeta = 0;
		double ventaTotalTarjetaMes = 0;
		for(ModeloSer.Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				//Realizamos la acumulaci�n despues de cada iteraci�n
				ventaTotalTarjeta = PedidoDAO.obtenerTotalesPedidosSemanaTarjeta(fechaAnterior, fechaActual, tien.getHostBD());
				ventaTotalTarjetaMes = ventaTotalTarjetaMes + ventaTotalTarjeta; 
			}
		}
		
		//TOTAL DE VENTAS DE PAYU
		//Mostraremos la tabla de venta en total por tienda
		totalSemanaTienda = capaDAOCC.PedidoDAO.consultarPedidosEpaycoTiendaSemana(fechaAnterior, fechaActual);
		double ventaTotalPayu = 0;
		for(int i = 0; i < totalSemanaTienda.size(); i++)
		{
			String[] fila = (String[]) totalSemanaTienda.get(i);
			ventaTotalPayu = ventaTotalPayu + Double.parseDouble(fila[0]);
		}
		
		
		//Incluimos la informaci�n mensual de RAPPI
		//Obtenemos un total por tienda de los pedidos
		ArrayList pedidosRappiTienda = capaDAOCC.PedidoDAO.obtenerPedidosPlataformasTienda(1, fechaAnterior, fechaActual,2);
		//Agregamos en este apartado el total de pedidos por tienda para poder extraer la comisi�n por tienda
		String[] resTotalTienda;
		double totalRappi = 0;
		for(int j = 0; j < pedidosRappiTienda.size(); j++)
		{
			resTotalTienda = (String[]) pedidosRappiTienda.get(j);
			totalRappi = totalRappi + Double.parseDouble(resTotalTienda[1]);
		}
	
		
		//Incluimos la informaci�n mensual de DDI
		//Obtenemos un total por tienda de los pedidos
		ArrayList pedidosDIDITienda = capaDAOCC.PedidoDAO.obtenerPedidosPlataformasTienda(1, fechaAnterior, fechaActual,1);
		//Agregamos en este apartado el total de pedidos por tienda para poder extraer la comisi�n por tienda
		String[] resTotalDIDITienda;
		double totalDIDI = 0;
		for(int j = 0; j < pedidosDIDITienda.size(); j++)
		{
			resTotalDIDITienda = (String[]) pedidosDIDITienda.get(j);
			totalDIDI = totalDIDI + Double.parseDouble(resTotalDIDITienda[1]);
		}

		//Calculamos un gran total
		double totalGeneral = totalRappi + ventaTotalPayu + ventaTotalTarjetaMes + ventaTotalWompi + totalDIDI;
		respuesta = respuesta + "<table border='2'> <tr> RESUMEN GENERAL " + fechaAnterior + "  -  " + fechaActual +  " </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>ITEM RESUMEN</strong></td>"
				+  "<td><strong>VALOR TOTAL</strong></td>"
				+"</tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>TOTAL VENTA MES</strong></td>"
				+  "<td><strong>"+ formatea.format(ventaTotalTiendas) +"</strong></td>"
				+"</tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>TOTAL VENTA EN LINEA</strong></td>"
				+  "<td><strong>"+ formatea.format(totalGeneral) +"</strong></td>"
				+"</tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>PORCENTAJE VENTA EN LINEA</strong></td>"
				+  "<td><strong>"+ formatea.format((totalGeneral/ventaTotalTiendas)*100) +"%</strong></td>"
				+"</tr>";
		
				
		correo = new Correo();
		infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("RESUMEN PARCIAL FACTURACION PIZZA AMERICANA DEL "  + fechaAnterior + " al " + fechaActual);
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correos = GeneralDAO.obtenerCorreosParametro("REPORTERESUMENVENTAS");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuacion el reporte de avance de facturacion " + fechaAnterior + " - " + fechaActual +  ": \n" + respuesta);
		contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
		
		
	}
	
	

public static void main(String[] args)
{
	ReporteResumenVentasFacturaElectronicaRep reporteDomicios = new ReporteResumenVentasFacturaElectronicaRep();
	reporteDomicios.generarReporte();
	//ConexionBaseDatos con = new ConexionBaseDatos();
	//Connection con1 = con.obtenerConexionBDTienda("");
	
}

}




