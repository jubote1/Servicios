package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import capaModeloCC.Tienda;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteConsignacionWompi {
	
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
			//fechaActual = "2019-05-13";
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
			calendarioActual.add(Calendar.DAY_OF_YEAR, -4);
		}//Lunes
		else if(diaActual == 2)
		{
			calendarioActual.add(Calendar.DAY_OF_YEAR, -5);
		}//Martes
		else if(diaActual == 3)
		{
			//Si es martes se resta uno solo
			calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
		}//Miercoles
		else if(diaActual == 4)
		{
			//Si es miercoles se resta dos
			calendarioActual.add(Calendar.DAY_OF_YEAR, -7);
		}//Jueves
		else if(diaActual == 5)
		{
			//Si es jueves se resta tres
			calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
		}
		else if(diaActual == 6)
		{
			//Si es viernes se resta cuatro
			calendarioActual.add(Calendar.DAY_OF_YEAR, -2);
		}
		else if(diaActual == 7)
		{
			//Si es sabado se resta cinco
			calendarioActual.add(Calendar.DAY_OF_YEAR, -3);
		}
		//Llevamos a un string la fecha anterior para el cálculo de la venta
		datFechaAnterior = calendarioActual.getTime();
		fechaAnterior = dateFormat.format(datFechaAnterior);
		
		String respuesta = "";
		respuesta = respuesta + "<table border='2'> <tr> <td colspan='6'> RESUMEN GENERAL PARA CONSIGNACIÓN DE WOMPI ENTRE " + fechaAnterior + "  " + fechaActual + " </td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Total Valor a Consignar</strong></td>"
				+  "<td><strong>Total Pedidos</strong></td>"
				+  "<td><strong>Total Comisión</strong></td>"
				+  "<td><strong>Iva Comision</strong></td>"
				+  "<td><strong>Retencion en la Fuente</strong></td>"
				+  "<td><strong>ReteICA</strong></td>"
				+  "</tr>";
		
		
		//Vamos a sacar ahora el reporte por cada tienda con el detalle de los pagos
		//Comenzamos por obtener todas las tiendas y realizar un recorrido de cada una
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
		double adicionComisionWompi = ParametrosDAO.retornarValorNumericoLocalDouble("ADICIONCOMISIONWOMPI");
		double ivaComisionWompi = ParametrosDAO.retornarValorNumericoLocalDouble("IVACOMISIONWOMPI");
		double retencionFuenteWompi = ParametrosDAO.retornarValorNumericoLocalDouble("RETENCIONFUENTEWOMPI");
		double retencionICAWompi = ParametrosDAO.retornarValorNumericoLocalDouble("RETENCIONICAWOMPI");
		double impuestosWompi = 0;
		double valorConsignacion = 0;
		double valorPedidos = 0;
		comisionTotal = 0;
		ivaComisionTotal = 0;
		retencionFuenteTotal = 0;
		retencionIcaTotal = 0;
		impuestosTotal = 0;
		pedVirtualTienda = capaDAOCC.PedidoDAO.consultarPedidosVirtualRealizadosTienda(fechaAnterior, fechaActual, 0);
		//Recorremos los pedidos de la tienda realizando las liquidaciones y acumulaciones correspondientes
		for(int k = 0; k < pedVirtualTienda.size(); k++)
		{
			pedTemp = pedVirtualTienda.get(k);
			//Se hace necesario realizar una diferenciación con el pago de Bancolombia que cobra menos
			if(pedTemp.getTipoPago().equals(new String("BANCOLOMBIA_TRANSFER")))
			{
				comision = (pedTemp.getTotal_neto()*((1.5)/100)) + 500;
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
			comisionTotal = comisionTotal + comision;
			ivaComisionTotal = ivaComisionTotal + ivaComision;
			retencionFuenteTotal = retencionFuenteTotal + retencionFuente;
			retencionIcaTotal = retencionIcaTotal + retencionIca;
			valorConsignacion = valorConsignacion + (pedTemp.getTotal_neto()-comision-ivaComision-retencionFuente-retencionIca);
			valorPedidos = (valorPedidos + pedTemp.getTotal_neto());
		}
		respuesta = respuesta + "<tr>"
				+  "<td>" + formatea.format(valorConsignacion)+ "</td>"
				+  "<td>" + formatea.format(valorPedidos)+ "</td>"
				+  "<td>" + formatea.format(comisionTotal)+ "</td>"
				+  "<td>" + formatea.format(ivaComisionTotal)+ "</td>"
				+  "<td>" + formatea.format(retencionFuenteTotal)+ "</td>"
				+  "<td>" + formatea.format(retencionIcaTotal)+ "</td>"
				+  "</tr>";
		respuesta = respuesta + "</table> <br/>";
		
		//Al final el envío del correo
		//Procedemos al envío del correo
		Correo correo = new Correo();
		correo.setAsunto("CONSIGNACION SEMANAL PAGOS VIRTUALES DESDE " + fechaAnterior + " HASTA "  + fechaActual);
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTECONSIGNACIONWOMPI");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuación el detalle DE LA CONSIGNACIÓN QUE REALIZARÁ WOMPI por los pedidos con forma de pago virtual entre las fechas " + fechaAnterior + " - " + fechaActual +  ": \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}

}
