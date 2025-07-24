package ServiciosSer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import CapaDAOSer.ConsumoInventarioDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ItemInventarioDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.ConsumoInventario;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.Insumo;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import utilidadesSer.ControladorEnvioCorreo;

/**
 * Clase que se encarga de  monitorear los sitios
 */
public class ServicioMonitoreoIntegraciones {
	
	
	
	
public static void main(String[] args)
{
	ServicioMonitoreoIntegraciones monitoreoRed = new ServicioMonitoreoIntegraciones();
	monitoreoRed.monitorearIntegraciones();
	
}

public void monitorearIntegraciones()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCI�N");
	//Generamos la fecha en la que corre el proceso
	Date fechaActual = new Date();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//Formateamos la fecha Actual para consulta
	String strFechaActual = dateFormat.format(fechaActual);
	//Generamos String de tiendas exitosas y tiendas no exitosas para mandar correo.
	String respuesta = "";
	String urlRappi = "https://merchants.rappi.com/es-co";
	String urlDidi = "https://openapi.didi-food.com/v1/order/order/confirm";
	String urlKommo = "https://www.kommo.com/es/";
	String urlUltra = "https://ultramsg.com/es/";
	try {
		(new URL(urlRappi)).openStream().close();
	}catch(Exception e)
	{
		respuesta = respuesta + " Problema comunicacion URL RAPPI.";
	}
	try {
		(new URL(urlDidi)).openStream().close();
	}catch(Exception e)
	{
		respuesta = respuesta + " Problema comunicacion URL DIDI.";
	}
	try {
		(new URL(urlKommo)).openStream().close();
	}catch(Exception e)
	{
		respuesta = respuesta + " Problema comunicacion URL KOMMO.";
	}
//	try {
//		(new URL(urlUltra)).openStream().close();
//	}catch(Exception e)
//	{
//		respuesta = respuesta + " Problema comunicacion URL ULTRA.";
//	}
	
	//Realizamos el env�o del correo electr�nico con los archivos
	if(respuesta.length() > 0)
	{
		Correo correo = new Correo();
		correo.setAsunto("URGENTE SITIOS NO RESPONDEN DESDE SEDE ADMINIS " + fechaActual.toString());
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		//Tendremos que definir los destinatarios de este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("ERRORINTERNET");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		String mensaje = "Se presentan inconvenientes en la navegación en el centro administrativo en  " + respuesta;
		correo.setMensaje(mensaje);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}
	
}

}




