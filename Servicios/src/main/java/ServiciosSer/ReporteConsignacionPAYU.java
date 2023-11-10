package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import capaModeloCC.Tienda;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteConsignacionPAYU {
	
	public static void main( String[] args )
	{
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
		//Tomamos estas dos variables que nos ayudarán a fijar la fecha anterior para las operaciones
		int mesActual = 0;
		int diaActual = 0;
		int anoActual = 0;
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
			//Fijamos los números para el mes actual y dia actual del mes
			mesActual = calendarioActual.get(Calendar.MONTH) + 1;
			diaActual = calendarioActual.get(Calendar.DAY_OF_MONTH);
			anoActual = calendarioActual.get(Calendar.YEAR);
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
		//Procedemos a tener la lógica para fijar la fecha anterior
		if(diaActual >=1 && diaActual<= 15)
		{
			diaActual= 16;
			if(mesActual == 1)
			{
				mesActual = 12;
				anoActual = anoActual -1;
			}else
			{
				mesActual = mesActual -1;
			}
		}
		else if(diaActual > 15 && diaActual <= 31)
		{
			diaActual = 1;
		}
		//Con lo anterior fijamos cual es la quincena a trabajar
		
		
		
		fechaAnterior = anoActual+"-"+mesActual+"-"+diaActual;

		//Antes de realizamos la actualización de los pedidos
		PedidoDAO.actualizarPedidosPayu(fechaAnterior);
		
		String respuesta = "";
		respuesta = respuesta + "<table border='2'> <tr> <td colspan='6'> RESUMEN GENERAL PARA CONSIGNACIÓN DE PAYU ENTRE " + fechaAnterior + "  " + fechaActual + " </td></tr>";
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
		double comisionEpayco = ParametrosDAO.retornarValorNumericoLocalDouble("COMISIONEPAYCO");
		double adicionComisionEpayco = ParametrosDAO.retornarValorNumericoLocalDouble("ADICIONCOMISIONEPAYCO");
		double valorMinimoEpayco = ParametrosDAO.retornarValorNumericoLocalDouble("VALORMINIMOEPAYCO");
		double valorMinComisionEpayco = ParametrosDAO.retornarValorNumericoLocalDouble("VALORMINCOMISIONEPAYCO");
		double ivaComisionWompi = ParametrosDAO.retornarValorNumericoLocalDouble("IVACOMISIONWOMPI");
		double retencionFuenteWompi = ParametrosDAO.retornarValorNumericoLocalDouble("RETENCIONFUENTEWOMPI");
		double retencionICAWompi = ParametrosDAO.retornarValorNumericoLocalDouble("RETENCIONICAWOMPI");
		double valorConsignacion = 0;
		double valorPedidos = 0;
		comisionTotal = 0;
		ivaComisionTotal = 0;
		retencionFuenteTotal = 0;
		retencionIcaTotal = 0;
		impuestosTotal = 0;
		pedVirtualTienda = capaDAOCC.PedidoDAO.consultarPedidosEpaycoRealizadosTienda(fechaAnterior, fechaActual, 0);
		//Recorremos los pedidos de la tienda realizando las liquidaciones y acumulaciones correspondientes
		for(int k = 0; k < pedVirtualTienda.size(); k++)
		{
			pedTemp = pedVirtualTienda.get(k);
			if(!(pedTemp.getTipoPago()== null))
			{
				if(pedTemp.getTipoPago().equals(new String("PSE")) && pedTemp.getTotal_neto() < valorMinimoEpayco)
				{
					comision = valorMinComisionEpayco;
				}
				else
				{
					comision = (pedTemp.getTotal_neto()*(comisionEpayco/100)) + adicionComisionEpayco;
				}
				ivaComision = (comision*(ivaComisionWompi/100));
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
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("CONSIGNACION SEMANAL PAYU DESDE " + fechaAnterior + " HASTA "  + fechaActual);
		correo.setContrasena(infoCorreo.getClaveCorreo());
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTECONSIGNACIONWOMPI");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuación el detalle DE LA CONSIGNACIÓN QUE REALIZARÁ PAYU por los pedidos con forma de pago virtual entre las fechas " + fechaAnterior + " - " + fechaActual +  ": \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}

}
