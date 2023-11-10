package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import capaControladorINV.InventarioCtrl;
import capaDAOINV.DesechoTiendaDAO;
import capaModeloCC.Tienda;
import capaModeloINV.DesechoTienda;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteDesechosTiendaReproceso {
	
	public static void main( String[] args )
	{

		// 1. Recuperamos la fecha actual del sistema con la fecha apertura
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
			//fechaActual = dateFormat.format(calendarioActual.getTime());
			//fechaActual = "2021-02-01";
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
		
		
		// 2. Recuperamos las tiendas de manera tendremos el listado para consultar la información de cada una.
		//Comenzamos por obtener todas las tiendas y realizar un recorrido de cada una
		ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendas();
		//Variables para la labor de cada tienda
		Tienda tiendaTemp;
		
		//3. Comenzamos el procesamiento para generar el HTML que se enviará el correo recorriendo cada tienda y generando una tabla 
		// con la información de los desechos
		String respuesta = "";
		Double valorDesechoTienda;
		for(int j = 0; j < tiendas.size(); j++)
		{
			tiendaTemp = tiendas.get(j);
			respuesta = respuesta + "<table border='2'> <tr> <td colspan='10'> REPORTE DESECHOS " + tiendaTemp.getNombreTienda()+ "</td></tr>";
			respuesta = respuesta + "<tr>"
					+  "<th width='50' ><strong>Id Desecho</strong></td>"
					+  "<th width='70'><strong>Numero Desecho</strong></td>"
					+  "<th width='80'><strong>Fecha</strong></td>"
					+  "<th width='200'><strong>Descripción</strong></td>"
					+  "<th width='200'><strong>Motivo</strong></td>"
					+  "<th width='130'><strong>Desecho</strong></td>"
					+  "<th width='50'><strong>Gramos</strong></td>"
					+  "<th width='50'><strong>Cantidad</strong></td>"
					+  "<th width='80'><strong>Costo</strong></td>"
					+  "<th width='80'><strong>Usuario</strong></td>"
					+  "</tr>";
			InventarioCtrl invCtrl = new InventarioCtrl();
			ArrayList<DesechoTienda> desechos = invCtrl.obtenerDesechosTiendaFechas(fechaAnterior, fechaActual, tiendaTemp.getIdTienda());
			valorDesechoTienda = 0.0;
			DesechoTienda desTiendaTemp;
			for(int k = 0; k < desechos.size(); k++)
			{
				desTiendaTemp = desechos.get(k);
				if(desTiendaTemp.getGramos() > 0 && desTiendaTemp.getCantidad() == 0)
				{
					valorDesechoTienda = valorDesechoTienda + (desTiendaTemp.getGramos()* desTiendaTemp.getCosto());
					respuesta = respuesta + "<tr><td width='50'>" + desTiendaTemp.getIdDesechoTienda() +  "</td><td width='70'>" + desTiendaTemp.getNumeroDesecho()+ "</td><td width='80'>" + desTiendaTemp.getFecha() + "</td><td width='200'>" + desTiendaTemp.getDescripcion() + "</td><td width='200'>" + desTiendaTemp.getMotivo() + "</td><td width='130'>" + desTiendaTemp.getDescripcionDesecho() + "</td><td width='50'>" + formatea.format(desTiendaTemp.getGramos()) + "</td><td width='50'>" + " " + "</td><td width='80'>" + formatea.format((desTiendaTemp.getGramos()* desTiendaTemp.getCosto())) + "</td><td width='80'>" + desTiendaTemp.getUsuario() + "</td></tr>";
					
				}else
				{
					valorDesechoTienda = valorDesechoTienda + (desTiendaTemp.getCosto()*desTiendaTemp.getCantidad());
					respuesta = respuesta + "<tr><td width='50'>" + desTiendaTemp.getIdDesechoTienda() +  "</td><td width='70'>" + desTiendaTemp.getNumeroDesecho()+ "</td><td width='50'>" + desTiendaTemp.getFecha() + "</td><td width='200'>" + desTiendaTemp.getDescripcion() + "</td><td width='200'>" + desTiendaTemp.getMotivo() + "</td><td width='130'>" + desTiendaTemp.getDescripcionDesecho() + "</td><td width='50'>" + " " + "</td><td width='50'>" + desTiendaTemp.getCantidad() + "</td><td width='80'>" + formatea.format((desTiendaTemp.getCosto()*desTiendaTemp.getCantidad())) + "</td><td width='80'>" + desTiendaTemp.getUsuario() + "</td></tr>";
					
				}
			}
			respuesta = respuesta + "<tr><td width='600' COLSPAN='5'>TOTAL TIENDA </td><td width='390' COLSPAN='5'>" + formatea.format(valorDesechoTienda) + " </td></tr></table> <br/>";
		}
				
		//Al final el envío del correo
		//Procedemos al envío del correo
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("DESECHOS SEMANALES DESDE " + fechaAnterior + " HASTA "  + fechaActual);
		correo.setContrasena(infoCorreo.getClaveCorreo());
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEDESECHOS");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuación el detalle y resumen de los desechos por tienda entre las fechas " + fechaAnterior + " - " + fechaActual +  ": \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();

		
	}

}
