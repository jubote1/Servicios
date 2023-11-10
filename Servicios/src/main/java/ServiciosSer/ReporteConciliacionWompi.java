package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.GastoSemanalDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.GastoSemanal;
import capaModeloCC.Tienda;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteConciliacionWompi {
	
	public static void main( String[] args )
	{
		//TRABAJO CON LAS FECHAS///////
		//Recuperamos la fecha actual del sistema con la fecha apertura
		String fechaActual = "";
		//Variables donde manejaremos la fecha anerior con el fin realizar los cálculos de ventas
		Date datFechaAnterior;
		String fechaAnterior = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//Formato para mostrar las cantidades
		DecimalFormat formatea = new DecimalFormat("###,###");
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
		//En base en lo anterior tenemos la fechaActual y fechaAnterior para ejecutar los procesos
		double totalPedidos = capaDAOCC.PedidoDAO.consultarTotalPedidosVirtualRealizados(fechaAnterior, fechaActual);
		
		//Sacamos información resumida de total de pedidos en general y por tienda en la semana y por tienda y por día en la semana
		String respuesta = "";
		respuesta = respuesta + "<table border='2'> <tr> <td colspan='2'> TOTAL PEDIDOS PAGO VIRTUAL SEMANA - " + fechaAnterior + "  -  " + fechaActual +  "</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>TOTAL SEMANA</strong></td>"
				+  "<td><strong>"+ formatea.format(totalPedidos) +"</strong></td>"
				+  "</tr>";
		respuesta = respuesta + "</table> <br/>";
		
		//Mostraremos la tabla de venta en total por tienda
		ArrayList totalSemanaTienda = capaDAOCC.PedidoDAO.consultarPedidosVirtualTiendaSemana(fechaAnterior, fechaActual);
		respuesta = respuesta + "<table border='2'> <tr> <td colspan='2'> TOTAL PEDIDOS PAGO VIRTUAL SEMANA POR TIENDA - " + fechaAnterior + "  -  " + fechaActual +  "</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>TIENDA</strong></td>"
				+  "<td><strong>TOTAL SEMANA TIENDA</strong></td>"
				+  "</tr>";
		for(int i = 0; i < totalSemanaTienda.size(); i++)
		{
			String[] fila = (String[]) totalSemanaTienda.get(i);
			respuesta = respuesta + "<tr><td>" + fila[1] + "</td><td>" + formatea.format(Double.parseDouble(fila[0])) + "</td></tr>";
		}
		respuesta = respuesta + "</table> <br/>";
		
		//Mostraremos la tabla de venta en total por tienda y día
		
		ArrayList totalDiaSemanaTienda = capaDAOCC.PedidoDAO.consultarPedidosVirtualTiendaDiaSemana(fechaAnterior, fechaActual);
		respuesta = respuesta + "<table border='2'> <tr> <td colspan='3'> TOTAL PEDIDOS PAGO VIRTUAL SEMANA POR DIA/TIENDA - " + fechaAnterior + "  -  " + fechaActual +  "</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>TIENDA</strong></td>"
				+  "<td><strong>FECHA</strong></td>"
				+  "<td><strong>TOTAL DIA</strong></td>"
				+  "</tr>";
		for(int i = 0; i < totalDiaSemanaTienda.size(); i++)
		{
			String[] fila = (String[]) totalDiaSemanaTienda.get(i);
			respuesta = respuesta + "<tr><td>" + fila[1] +  "</td><td>" + fila[2] + "</td><td>" + formatea.format(Double.parseDouble(fila[0])) + "</td></tr>";
		}
		respuesta = respuesta + "</table> <br/>";
		
		//Vamos a sacar ahora el reporte por cada tienda con el detalle de los pagos
		//Comenzamos por obtener todas las tiendas y realizar un recorrido de cada una
		ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendas();
		//Variables para la labor de cada tienda
		Tienda tiendaTemp;
		ArrayList <capaModeloCC.Pedido> pedVirtualTienda;
		capaModeloCC.Pedido pedTemp = new capaModeloCC.Pedido();
		double comisionTotal;
		double comision;
		double ivaComisionTotal;
		double ivaComision;
		double retencionFuente;
		double retencionFuenteTotal;
		double retencionIca;
		double retencionIcaTotal;
		double impuestos;
		double impuestosTotal;
		//Recuperamos todos los valores parametrizables para el proceso
		double comisionWompi = ParametrosDAO.retornarValorNumericoLocalDouble("COMISIONWOMPI");
		double comisionWompiBanc = ParametrosDAO.retornarValorNumericoLocalDouble("COMISIONWOMPIBANC");
		double adicionComisionWompi = ParametrosDAO.retornarValorNumericoLocalDouble("ADICIONCOMISIONWOMPI");
		double ivaComisionWompi = ParametrosDAO.retornarValorNumericoLocalDouble("IVACOMISIONWOMPI");
		double retencionFuenteWompi = ParametrosDAO.retornarValorNumericoLocalDouble("RETENCIONFUENTEWOMPI");
		double retencionICAWompi = ParametrosDAO.retornarValorNumericoLocalDouble("RETENCIONICAWOMPI");
		double impuestosWompi = 0;
		for(int j = 0; j < tiendas.size(); j++)
		{
			tiendaTemp = tiendas.get(j);
			respuesta = respuesta + "<table border='2'> <tr> <td colspan='7'> PEDIDOS POR PUNTO DE VENTA " + tiendaTemp.getNombreTienda()+ "</td></tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Id Pedido Contact</strong></td>"
					+  "<td><strong>Factura Tienda</strong></td>"
					+  "<td><strong>Fecha Pedido</strong></td>"
					+  "<td><strong>Total Pedido</strong></td>"
					+  "<td><strong>Tipo Pago</strong></td>"
					+  "<td><strong>Comision</strong></td>"
					+  "<td><strong>Impuestos</strong></td>"
					+  "</tr>";
			comisionTotal = 0;
			ivaComisionTotal = 0;
			retencionFuenteTotal = 0;
			retencionIcaTotal = 0;
			impuestosTotal = 0;
			pedVirtualTienda = capaDAOCC.PedidoDAO.consultarPedidosVirtualRealizadosTienda(fechaAnterior, fechaActual, tiendaTemp.getIdTienda());
			//Recorremos los pedidos de la tienda realizando las liquidaciones y acumulaciones correspondientes
			for(int k = 0; k < pedVirtualTienda.size(); k++)
			{
				pedTemp = pedVirtualTienda.get(k);
				//Se hace necesario realizar una diferenciación con el pago de Bancolombia que cobra menos
				if(pedTemp.getTipoPago().equals(new String("BANCOLOMBIA_TRANSFER")) || pedTemp.getTipoPago().equals(new String("BANCOLOMBIA_QR")))
				{
					comision = (pedTemp.getTotal_neto()*((comisionWompiBanc)/100)) + adicionComisionWompi;
					ivaComision = (comision*(ivaComisionWompi/100));
				}else
				{
					comision = (pedTemp.getTotal_neto()*(comisionWompi/100)) + adicionComisionWompi;
					ivaComision = (comision*(ivaComisionWompi/100));
				}
				if(pedTemp.getTipoPago().equals(new String("CARD")))
				{
					retencionFuente = pedTemp.getTotal_neto()*(retencionFuenteWompi/100);
					retencionIca = pedTemp.getTotal_neto()*(retencionICAWompi/100);
				}else
				{
					retencionFuente = 0;
					retencionIca = 0;
				}
				impuestos = ivaComision + retencionFuente + retencionIca;
				respuesta = respuesta + "<tr><td>" + pedTemp.getIdpedido() +  "</td><td>" + pedTemp.getNumposheader() + "</td><td>" + pedTemp.getFechapedido() + "</td><td>" + formatea.format(pedTemp.getTotal_neto()) + "</td><td>" + pedTemp.getTipoPago() + "</td><td>" + formatea.format(comision) + "</td><td>" + formatea.format(impuestos) + "</td></tr>";
				comisionTotal = comisionTotal + comision;
				ivaComisionTotal = ivaComisionTotal + ivaComision;
				retencionFuenteTotal = retencionFuenteTotal + retencionFuente;
				retencionIcaTotal = retencionIcaTotal + retencionIca;
			}
			respuesta = respuesta + "</table> <br/>";
			
			//Sacamos el totalizados para la tienda
			respuesta = respuesta + "<table border='2'> <tr> <td colspan='2'> TOTALES PARA TIENDA " + tiendaTemp.getNombreTienda() + " - " + fechaAnterior + "-" + fechaActual +  "</td></tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>TOTAL COMISION</strong></td>"
					+  "<td><strong>"+ formatea.format(comisionTotal) +"</strong></td>"
					+  "</tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>TOTAL IVA COMISION</strong></td>"
					+  "<td><strong>"+ formatea.format(ivaComisionTotal) +"</strong></td>"
					+  "</tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>TOTAL RETENCION EN LA FUENTE</strong></td>"
					+  "<td><strong>"+ formatea.format(retencionFuenteTotal) +"</strong></td>"
					+  "</tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>TOTAL RETENCION ICA</strong></td>"
					+  "<td><strong>"+ formatea.format(retencionIcaTotal) +"</strong></td>"
					+  "</tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>TOTAL A DEDUCIR</strong></td>"
					+  "<td><strong>"+ formatea.format(comisionTotal + ivaComisionTotal + retencionFuenteTotal + retencionIcaTotal) +"</strong></td>"
					+  "</tr>";
			respuesta = respuesta + "</table> <br/>";
			//En este punto tenemos el total de la tienda y lo insertaremos en la tabla correspondiente
			GastoSemanal gastoSemanalTemp = new GastoSemanal(0,tiendaTemp.getIdTienda(),16,fechaActual,comisionTotal + ivaComisionTotal + retencionFuenteTotal + retencionIcaTotal,comisionTotal + ivaComisionTotal + retencionFuenteTotal + retencionIcaTotal);
			GastoSemanalDAO.insertarGastoSemanal(gastoSemanalTemp);
		}
		
		//Extraemos una información para sacar resumen de los pagos por tarjeta
		ArrayList<ModeloSer.Tienda> tiendasLocal = TiendaDAO.obtenerTiendasLocal();
		respuesta = respuesta + "<table border='2'> <tr> <td colspan='2'> TOTAL POR TIENDA EN FORMA DE PAGO TARJETA EN SEMANA QUE CIERRA</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Nombre Tienda</strong></td>"
				+  "<td><strong>Valor Dinero</strong></td>"
				+  "</tr>";
		double ventaTotalTarjeta = 0;
		for(ModeloSer.Tienda tien : tiendasLocal)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				//Realizamos la acumulación despues de cada iteración
				ventaTotalTarjeta = PedidoDAO.obtenerTotalesPedidosSemanaTarjeta(fechaAnterior, fechaActual, tien.getHostBD());
				respuesta = respuesta + "<tr><td>" + tien.getNombreTienda()+  "</td><td>" + formatea.format(ventaTotalTarjeta) + "</td></tr>";
			}
		}
		respuesta = respuesta + "</table><br/>";
		
		//Incluiremos la información del QR Bancolombia
		//valor de comisión QR Bancolombia
		double comisionQR = ParametrosDAO.retornarValorNumericoLocalDouble("QRBANCOLOMBIA");
		respuesta = respuesta + "<table border='2'> <tr> <td colspan='2'> TOTAL POR TIENDA EN FORMA DE PAGO QR BANCOLOMBIA</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Nombre Tienda</strong></td>"
				+  "<td><strong>Valor Dinero</strong></td>"
				+  "<td><strong>Valor Comisión</strong></td>"
				+  "</tr>";
		double ventaTotalQR = 0;
		double totalComisionQR = 0;
		for(ModeloSer.Tienda tien : tiendasLocal)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				//Realizamos la acumulación despues de cada iteración
				ventaTotalQR = TiendaDAO.obtenerTotalFormaPagoEntreFechas(fechaAnterior, fechaActual, tien.getHostBD(), false);
				totalComisionQR = (ventaTotalQR) * (comisionQR/100);
				respuesta = respuesta + "<tr><td>" + tien.getNombreTienda()+  "</td><td>" + formatea.format(ventaTotalQR) +  "</td><td>" + formatea.format(totalComisionQR) + "</td></tr>";
			}
		}
		respuesta = respuesta + "</table><br/>";
		
		//Incluiremos la información de pago de TARJETAS REGALO PIZZA AMERICANA
		respuesta = respuesta + "<table border='2'> <tr> <td colspan='2'> TOTAL POR TIENDA EN FORMA DE PAGO TARJETA PIZZA AMERICANA </td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Nombre Tienda</strong></td>"
				+  "<td><strong>Valor Dinero</strong></td>"
				+  "</tr>";
		for(ModeloSer.Tienda tien : tiendasLocal)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				//Realizamos la acumulación despues de cada iteración
				ventaTotalQR = TiendaDAO.obtenerTotalFormaPagoEntreFechasTarjetaPA(fechaAnterior, fechaActual, tien.getHostBD(), false);
				respuesta = respuesta + "<tr><td>" + tien.getNombreTienda()+  "</td><td>" + formatea.format(ventaTotalQR) +  "</td></tr>";
			}
		}
		respuesta = respuesta + "</table><br/>";
		
		
		//Al final el envío del correo
		//Procedemos al envío del correo
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("CONCILIACIÓN SEMANAL PAGOS VIRTUALES - PAGOS CON TARJETA DESDE " + fechaAnterior + " HASTA "  + fechaActual);
		correo.setContrasena(infoCorreo.getClaveCorreo());
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTECONCILIACIONWOMPI");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuación el detalle y resumen de los pedidos con forma de pago virtual entre las fechas " + fechaAnterior + " - " + fechaActual +  ": \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}

}
