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
import ModeloSer.NovedadDidi;
import capaControladorINV.InventarioCtrl;
import capaDAOINV.DesechoTiendaDAO;
import capaModeloCC.Tienda;
import capaModeloINV.DesechoTienda;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteControlDidiDesechos {
	
	public static void main( String[] args )
	{

		// 1. Recuperamos la fecha actual del sistema con la fecha apertura
		String fechaActual = "";
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
			//fechaActual = "2025-10-30";
		}catch(Exception exc)
		{
			System.out.println(exc.toString());
		}
		
		// 2. Recuperamos las tiendas de manera tendremos el listado para consultar la informaci�n de cada una.
		//Comenzamos por obtener todas las tiendas y realizar un recorrido de cada una
		ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendas();
		//Variables para la labor de cada tienda
		Tienda tiendaTemp;
		
		//3. Comenzamos el procesamiento para generar el HTML que se enviar� el correo recorriendo cada tienda y generando una tabla 
		// con la informaci�n de los desechos
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
					+  "<th width='200'><strong>Descripci�n</strong></td>"
					+  "<th width='200'><strong>Motivo</strong></td>"
					+  "<th width='130'><strong>Desecho</strong></td>"
					+  "<th width='50'><strong>Gramos</strong></td>"
					+  "<th width='50'><strong>Cantidad</strong></td>"
					+  "<th width='80'><strong>Costo</strong></td>"
					+  "<th width='80'><strong>Usuario</strong></td>"
					+  "</tr>";
			InventarioCtrl invCtrl = new InventarioCtrl();
			ArrayList<DesechoTienda> desechos = invCtrl.obtenerDesechosTiendaFechas(fechaActual, fechaActual, tiendaTemp.getIdTienda());
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
		//Posteriormente le vamos a dar tratamiento 
		for(int j = 0; j < tiendas.size(); j++)
		{
			tiendaTemp = tiendas.get(j);
			ArrayList<NovedadDidi> novedades = PedidoDAO.obtenerNovedadesDidi(fechaActual, tiendaTemp.getHosbd());
			respuesta = respuesta + "<table border='2'> <tr> <td colspan='5'> NOVEDADES DIDI " + tiendaTemp.getNombreTienda()+ "</td></tr>";
			respuesta = respuesta + "<tr>"
					+  "<th width='50' ><strong>Idpedidotienda</strong></td>"
					+  "<th width='150'><strong>Novedad</strong></td>"
					+  "<th width='80'><strong>Valor Anulacion</strong></td>"
					+  "<th width='80'><strong>Valor Descuento</strong></td>"
					+  "<th width='80'><strong>Total Pedido</strong></td>"
					+  "</tr>";
			NovedadDidi novedadTemp;
			for(int k = 0; k < novedades.size(); k++)
			{
				novedadTemp = novedades.get(k);
				respuesta = respuesta + "<tr><td>" + novedadTemp.getIdPedidoTienda() +  "</td><td>" + novedadTemp.getNovedad() + "</td><td>" + novedadTemp.getValorAnulacion() + "</td><td>" + novedadTemp.getValorDescuento() + "</td><td>" + novedadTemp.getValorTotal() + "</td></tr>";
			}
			respuesta = respuesta + "</table> <br/>";
		}
		
		//Al final el env�o del correo
		//Procedemos al env�o del correo
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("REVISIÓN DIARIA DESECHOS Y NOVEDADES DIDI DESDE " + fechaActual);
		correo.setContrasena(infoCorreo.getClaveCorreo());
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEDESECHOSDIDI");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuaci�n el detalle y resumen de los desechos y novedades con DIDI "  + fechaActual +  ": \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();

		
	}

}
