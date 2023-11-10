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
import capaControladorPOS.PedidoCtrl;
import capaDAOCC.RazonSocialDAO;
import capaModeloCC.RazonSocial;
import capaModeloPOS.TicketPromedio;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteResumenVentasMensualReproceso {
	
	/**
	 * Partimos de la premisa que el proceso corre el 30 de cada mes
	 */
	public void generarReporte()
	{
		DecimalFormat formatea = new DecimalFormat("###,###");
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
		//Obtenemos el mes actual y año actual
		int mesActual = calendarioActual.get(Calendar.MONTH)+1;
		int anoActual = calendarioActual.get(Calendar.YEAR);
		int diaActual = calendarioActual.get(Calendar.DAY_OF_MONTH);
		int diaMaximoMesActual = calendarioActual.getActualMaximum(calendarioActual.DAY_OF_MONTH);
		System.out.println(" diaMaximoMesActual " + diaMaximoMesActual + " diaActual " + diaActual);
		if(diaMaximoMesActual == diaActual)
		{
			fechaAnterior = Integer.toString(anoActual) + "-" + Integer.toString(mesActual) + "-01";
			ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
			//Construimos la respuesta para desplegar en el correo
			String respuestaCantFacturas = "";
			respuestaCantFacturas = respuestaCantFacturas + "<table border='2'> <tr> CANTIDAD DE FACTURAS POR TIENDA </tr>";
			respuestaCantFacturas = respuestaCantFacturas + "<tr>"
					+  "<td><strong>NOMBRE TIENDA</strong></td>"
					+  "<td><strong>CANTIDAD FACTURAS</strong></td>"
					+  "</tr>";
			int cantidadFacturas  = 0;
			for(Tienda tien : tiendas)
			{
				if(!tien.getHostBD().equals(new String("")))
				{
					cantidadFacturas = PedidoDAO.obtenerCantFacturas(fechaAnterior, fechaActual, tien.getHostBD());
					respuestaCantFacturas = respuestaCantFacturas + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + cantidadFacturas + "</td></tr>";
				}
			}
			//Posteriormente realizamos el envío del correo
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("RESUMEN CANTIDAD PEDIDOS PIZZA AMERICANA DEL "  + fechaAnterior + " al " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTECANTPEDIDOS");
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuación el reporte Mensual de cantidad de pedidos " + fechaAnterior + " - " + fechaActual +  ": \n" + respuestaCantFacturas);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
			
			String respuesta = "";
			respuesta = respuesta + "<table border='2'> <tr> VENTA TOTALES POR TIENDA </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>NOMBRE TIENDA</strong></td>"
					+  "<td><strong>VENTA TOTAL MES</strong></td>"
					+  "</tr>";
			double ventaTotalTiendas = 0;
			double ventaTienda;
			for(Tienda tien : tiendas)
			{
				if(!tien.getHostBD().equals(new String("")))
				{
					ventaTienda = PedidoDAO.obtenerTotalesPedidosSemana(fechaAnterior, fechaActual, tien.getHostBD());
					respuesta = respuesta + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(ventaTienda) + "</td></tr>";
					//Realizamos la acumulación despues de cada iteración
					ventaTotalTiendas  = ventaTotalTiendas + ventaTienda;
				}
			}
			respuesta = respuesta + "<tr><td> TOTAL TIENDAS </td><td>" + formatea.format(ventaTotalTiendas) + "</td></tr>";
			respuesta = respuesta + "</table> <br/>";
			
			
			//Agregamos los totales de PAGO VIRTUAL WOMPI
			//Mostraremos la tabla de venta en total por tienda
			double ventaTotalWompi = 0;
			ArrayList totalSemanaTienda = capaDAOCC.PedidoDAO.consultarPedidosVirtualTiendaSemana(fechaAnterior, fechaActual);
			respuesta = respuesta + "<table border='2'> <tr> <td colspan='2'> TOTAL PEDIDOS PAGO VIRTUAL WOMPI MES POR TIENDA - " + fechaAnterior + "  -  " + fechaActual +  "</td></tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>TIENDA</strong></td>"
					+  "<td><strong>TOTAL MES TIENDA</strong></td>"
					+  "</tr>";
			for(int i = 0; i < totalSemanaTienda.size(); i++)
			{
				String[] fila = (String[]) totalSemanaTienda.get(i);
				respuesta = respuesta + "<tr><td>" + fila[1] + "</td><td>" + formatea.format(Double.parseDouble(fila[0])) + "</td></tr>";
				ventaTotalWompi = ventaTotalWompi + Double.parseDouble(fila[0]);
			}
			respuesta = respuesta + "<tr><td>TOTAL PAGO WOMPI MES</td><td>" + formatea.format(ventaTotalWompi) + "</td></tr>";
			respuesta = respuesta + "</table> <br/>";
			
			//Obtenemos el total de pago con datáfono
			respuesta = respuesta + "<table border='2'> <tr> <td colspan='2'> TOTAL POR TIENDA EN FORMA DE PAGO TARJETA MES </td></tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Nombre Tienda</strong></td>"
					+  "<td><strong>Valor Dinero</strong></td>"
					+  "</tr>";
			double ventaTotalTarjeta = 0;
			double ventaTotalTarjetaMes = 0;
			for(ModeloSer.Tienda tien : tiendas)
			{
				if(!tien.getHostBD().equals(new String("")))
				{
					//Realizamos la acumulación despues de cada iteración
					ventaTotalTarjeta = PedidoDAO.obtenerTotalesPedidosSemanaTarjeta(fechaAnterior, fechaActual, tien.getHostBD());
					ventaTotalTarjetaMes = ventaTotalTarjetaMes + ventaTotalTarjeta; 
					respuesta = respuesta + "<tr><td>" + tien.getNombreTienda()+  "</td><td>" + formatea.format(ventaTotalTarjeta) + "</td></tr>";
				}
			}
			respuesta = respuesta + "<tr><td>TOTAL VENTA CON DATÁFONO</td><td>" + formatea.format(ventaTotalTarjetaMes) + "</td></tr>";
			respuesta = respuesta + "</table><br/>";
			
			//TOTAL DE VENTAS DE PAYU
			//Mostraremos la tabla de venta en total por tienda
			totalSemanaTienda = capaDAOCC.PedidoDAO.consultarPedidosEpaycoTiendaSemana(fechaAnterior, fechaActual);
			respuesta = respuesta + "<table border='2'> <tr> <td colspan='2'> TOTAL PEDIDOS  PAYU SEMANA POR TIENDA MES - " + fechaAnterior + "  -  " + fechaActual +  "</td></tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>TIENDA</strong></td>"
					+  "<td><strong>TOTAL SEMANA TIENDA</strong></td>"
					+  "</tr>";
			double ventaTotalPayu = 0;
			for(int i = 0; i < totalSemanaTienda.size(); i++)
			{
				String[] fila = (String[]) totalSemanaTienda.get(i);
				respuesta = respuesta + "<tr><td>" + fila[1] + "</td><td>" + formatea.format(Double.parseDouble(fila[0])) + "</td></tr>";
			 	ventaTotalPayu = ventaTotalPayu + Double.parseDouble(fila[0]);
			}
			respuesta = respuesta + "<tr><td>VENTA TOTAL MES PAYU</td><td>" + formatea.format(ventaTotalPayu) + "</td></tr>";
			respuesta = respuesta + "</table> <br/>";
			
			
			//Incluimos la información mensual de RAPPI
			//Obtenemos un total por tienda de los pedidos
			ArrayList pedidosRappiTienda = capaDAOCC.PedidoDAO.obtenerPedidosPlataformasTienda(1, fechaAnterior, fechaActual,2);
			//Agregamos en este apartado el total de pedidos por tienda para poder extraer la comisión por tienda
			respuesta = respuesta + "<table border='2'> <tr> RAPPI TOTAL POR TIENDA " + fechaAnterior + "  -  " + fechaActual +  " </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Tienda</strong></td>"
					+  "<td><strong>Total Pedidos</strong></td>"
					+"</tr>";
			String[] resTotalTienda;
			double totalRappi = 0;
			for(int j = 0; j < pedidosRappiTienda.size(); j++)
			{
				resTotalTienda = (String[]) pedidosRappiTienda.get(j);
				totalRappi = totalRappi + Double.parseDouble(resTotalTienda[1]);
				respuesta = respuesta + "<tr><td>" + resTotalTienda[0] + "</td><td>" + formatea.format(Double.parseDouble(resTotalTienda[1])) + "</td></tr>";
			}
			respuesta = respuesta + "<tr><td>VENTA TOTAL MES RAPPI</td><td>" + formatea.format(totalRappi) + "</td></tr>";
			respuesta = respuesta + "</table> <br/>";
			
			
			
			//Incluimos la información mensual de DDI
			//Obtenemos un total por tienda de los pedidos
			ArrayList pedidosDIDITienda = capaDAOCC.PedidoDAO.obtenerPedidosPlataformasTienda(1, fechaAnterior, fechaActual,1);
			//Agregamos en este apartado el total de pedidos por tienda para poder extraer la comisión por tienda
			respuesta = respuesta + "<table border='2'> <tr> DIDI TOTAL POR TIENDA " + fechaAnterior + "  -  " + fechaActual +  " </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Tienda</strong></td>"
					+  "<td><strong>Total Pedidos</strong></td>"
					+"</tr>";
			String[] resTotalDIDITienda;
			double totalDIDI = 0;
			for(int j = 0; j < pedidosDIDITienda.size(); j++)
			{
				resTotalDIDITienda = (String[]) pedidosDIDITienda.get(j);
				totalDIDI = totalDIDI + Double.parseDouble(resTotalDIDITienda[1]);
				respuesta = respuesta + "<tr><td>" + resTotalDIDITienda[0] + "</td><td>" + formatea.format(Double.parseDouble(resTotalDIDITienda[1])) + "</td></tr>";
			}
			respuesta = respuesta + "<tr><td>VENTA TOTAL MES DIDI</td><td>" + formatea.format(totalDIDI) + "</td></tr>";
			respuesta = respuesta + "</table> <br/>";
			
			
			
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
			
			
			//Vamos a agregar la lógica para la generación de los tickets promedios
			PedidoCtrl pedCtrl = new PedidoCtrl(false);
			for(Tienda tien : tiendas)
			{
				if(!tien.getHostBD().equals(new String("")))
				{
					
					TicketPromedio ticketPromedio = pedCtrl.calcularTicketPromedio(fechaAnterior, fechaActual, tien.getHostBD());
					TicketPromedioMes ticketMes = new TicketPromedioMes(tien.getIdTienda(), mesActual, anoActual, ticketPromedio.getValor(), ticketPromedio.getCantidadPedidos());
					TicketPromedioMesDAO.insertarTicketPromedioMes(ticketMes, false);
				}
			}
			
			
			correo = new Correo();
			infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("RESUMEN VENTAS MENSUAL PIZZA AMERICANA DEL "  + fechaAnterior + " al " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correos = GeneralDAO.obtenerCorreosParametro("REPORTERESUMENVENTAS");
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuación el reporte Mensual de ventas entre las fechas " + fechaAnterior + " - " + fechaActual +  ": \n" + respuesta);
			contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
		
	}
	

public static void main(String[] args)
{
	ReporteResumenVentasMensualReproceso reporteDomicios = new ReporteResumenVentasMensualReproceso();
	reporteDomicios.generarReporte();
	//ConexionBaseDatos con = new ConexionBaseDatos();
	//Connection con1 = con.obtenerConexionBDTienda("");
	
}

}




