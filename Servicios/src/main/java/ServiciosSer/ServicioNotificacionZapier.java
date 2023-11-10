package ServiciosSer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import CapaDAOSer.ClienteDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.OfertaClienteDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaCodigoPromocionalDAO;
import ModeloSer.ClienteZapier;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.TiendaCodigoPromocional;
import capaControladorCC.PromocionesCtrl;
import utilidadesSer.ControladorEnvioCorreo;
import org.json.simple.JSONObject;

public class ServicioNotificacionZapier {
	
	
	
	
public static void main(String[] args)
{
	ServicioNotificacionZapier reporteConsumosUsuarios = new ServicioNotificacionZapier();
	//reporteConsumosUsuarios.generarNotificacionesZapier();
	reporteConsumosUsuarios.generarNotificacionZapier("+573148807773", "Juan David Botero" ,  "USDS45454");
	//id cliente del CRM, construir un webhook que escuche la data
}

public void generarNotificacionZapier(String telefono, String nombre,  String codigo)
{
	JSONObject datos = new JSONObject();
	datos.put("telefono", telefono);
	datos.put("nombre", nombre);
	datos.put("codigo", codigo);
	String jsonString = datos.toJSONString();
	//Realizamos la invocación mediante el uso de HTTPCLIENT
	HttpClient client = HttpClientBuilder.create().build();
	String rutaURLNotif = "https://hooks.zapier.com/hooks/catch/3150747/bf2084f/";
	HttpPost request = new HttpPost(rutaURLNotif);
	try
	{
		//Fijamos el header con el token
		//NO HAY SEGURIDAD TODAVÍA
		//request.setHeader("Authorization", "Bearer " + "prv_prod_Qdb2HcV6AkbkvCKr9UWbhFs6L73IFCkT");
		request.setHeader("Accept", "application/json");
		request.setHeader("Content-type", "application/json");
		//Fijamos los parámetros
		//pass the json string request in the entity
	    HttpEntity entity = new ByteArrayEntity(jsonString.getBytes("UTF-8"));
	    request.setEntity(entity);
		//request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
		StringBuffer retorno = new StringBuffer();
		HttpResponse responseFinPed = client.execute(request);
		BufferedReader rd = new BufferedReader
			    (new InputStreamReader(
			    		responseFinPed.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			    retorno.append(line);
			}
		//Traemos el valor del JSON con toda la info del pedido
		String datosJSON = retorno.toString();
		System.out.println(datosJSON);
	}catch (Exception e2) {
        e2.printStackTrace();
        System.out.println(e2.toString());
    }
}

public void generarNotificacionesZapier()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCIÓN PROCESO DE PROMOCIONES");
	//Capturamos el parámetro del proceso que se va a ejecutar
	int idProcesoOferta = 0;
	try
	{
		//OJO
		//fechaActual = dateFormat.format(calendarioActual.getTime());
		//fechaActual = "2020-07-26";
		idProcesoOferta = ParametrosDAO.retornarValorNumerico("IDPROCESOOFERTA");
	}catch(Exception exc)
	{
		idProcesoOferta = 1;
		System.out.println(exc.toString());
	}
	//Generamos String de tiendas exitosas y tiendas no exitosas para mandar correo.
	String respuesta = "";
	
	//Generamos la fecha en la que corre el proceso
	Date fechaActual = new Date();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//Formateamos la fecha Actual para consulta
	//Necesitamos la fecha Actual con el fin de poder tener una referencia para el vencimiento de las ofertas
	String strFechaActual = dateFormat.format(fechaActual);
	

	
	//Debemos recuperar la información de las tiendas codigos promocionales
	ArrayList<ClienteZapier> clientesZapier = OfertaClienteDAO.obtenerClientesNotificacionZapier(idProcesoOferta, strFechaActual);
	for(ClienteZapier clienteTemp : clientesZapier)
	{
		generarNotificacionZapier(clienteTemp.getTelefono(), clienteTemp.getNombre(),  clienteTemp.getCodigo());
	}
	respuesta = respuesta + " Se han enviado " + clientesZapier.size() + " notificaciones.";

	//Realizamos el envío del correo electrónico con los archivos
	Correo correo = new Correo();
	correo.setAsunto(" RENOTIFICACIÓN ZAPIER " + fechaActual.toString());
	CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
	correo.setContrasena(infoCorreo.getClaveCorreo());
	//Tendremos que definir los destinatarios de este correo
	ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPGENERACIONPROMOCIONES");
	correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
	String mensaje = "A continuación se información del proceso de NOTIFICACION ZAPIER " + respuesta;
	correo.setMensaje(mensaje);
	ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
	contro.enviarCorreoHTML();
}


}




