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
import ModeloSer.DiaFestivo;
import ModeloSer.Tienda;
import capaControladorPOS.PedidoCtrl;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteConsignacionDavivienda {
	
	public static void main( String[] args )
	{
		//TRABAJO CON LAS FECHAS///////
		//Recuperamos la fecha actual del sistema con la fecha apertura
		String fechaActual = "";
		//Variables donde manejaremos la fecha anerior con el fin realizar los c�lculos de ventas
		Date datFechaAnterior;
		String fechaAnterior = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//Formato para mostrar las cantidades
		DecimalFormat formatea = new DecimalFormat("###,###");
		//Se recuperan los d�as festivos
		ArrayList<DiaFestivo> festivos = GeneralDAO.obtenerDiasFestivos();
		//Obtenemos la fecha Actual
		try
		{
			//OJO
			fechaActual = dateFormat.format(calendarioActual.getTime());
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
		String fechaControl = "";
		calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
		//Verificamos si dicho d�a es festivo
		fechaControl = dateFormat.format(calendarioActual.getTime());
		fechaAnterior = fechaControl;
		
		String respuesta = "";
		//Realizamos procedimiento para indicar las adquirencias
		ArrayList<Tienda> tiendasAdqDavivienda = TiendaDAO.obtenerTiendasAdqDavivienda();
		double ventaTarjetaTienda = 0;
		double totalTarjetaTiendas = 0;
		respuesta = respuesta + "<table border='2'> <tr> <td colspan='2'> VENTA POR TIENDA ADQUIRENCIA DAVIVIENDA</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Nombre Tienda</strong></td>"
				+  "<td><strong>Valor Venta - Comision</strong></td>"
				+  "</tr>";
		for(int i = 0; i < tiendasAdqDavivienda.size(); i++)
		{
			Tienda tiendaTemp = tiendasAdqDavivienda.get(i);
			if(!tiendaTemp.getHostBD().equals(new String("")))
			{
				ventaTarjetaTienda = PedidoDAO.obtenerTotalesPedidosSemanaTarjeta(fechaAnterior, fechaActual, tiendaTemp.getHostBD());
				ventaTarjetaTienda = ventaTarjetaTienda - (ventaTarjetaTienda*0.019) - (ventaTarjetaTienda*0.019* 0.19);
				totalTarjetaTiendas = totalTarjetaTiendas  + ventaTarjetaTienda;
				respuesta = respuesta + "<tr><td>" + tiendaTemp.getNombreTienda()+  "</td><td>" + formatea.format(ventaTarjetaTienda) + "</td></tr>";
			}
		}
		respuesta = respuesta + "<tr><td>TOTAL DATAFONOS DAVIVIENDA</td><td>" + formatea.format(totalTarjetaTiendas) + "</td></tr>";
		respuesta = respuesta + "</table><br/>";
		//Al final el env�o del correo
		//Procedemos al env�o del correo
		Correo correo = new Correo();
		correo.setAsunto("CONSIGNACION DIARIA DAVIVIENDA " + fechaAnterior + " HASTA "  + fechaActual);
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTECONSIGNACIONWOMPI");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuacion el detalle DE LA CONSIGNACION QUE REALIZARA DAVIVIENDA POR DATAFONOS " + fechaAnterior + " - " + fechaActual +  ": \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();

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
