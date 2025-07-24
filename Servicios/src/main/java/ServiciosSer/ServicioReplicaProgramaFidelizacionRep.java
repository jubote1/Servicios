package ServiciosSer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFRegionUtil;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.json.JSONArray;
import org.json.JSONObject;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ItemInventarioDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.Insumo;
import ModeloSer.PedidoPlanFidelizacion;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import capaDAOCC.ClienteFidelizacionDAO;
import capaDAOCC.FidelizacionTransaccionDAO;
import capaDAOCC.IntegracionCRMDAO;
import capaModeloCC.FidelizacionTransaccion;
import capaModeloCC.IntegracionCRM;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioReplicaProgramaFidelizacionRep {
	
	
	
	
public static void main(String[] args)
{
	ServicioReplicaProgramaFidelizacionRep reporteReplicaUsuarios = new ServicioReplicaProgramaFidelizacionRep();
	reporteReplicaUsuarios.generarReplicaProgramaFidelidad();
	
}

public void generarReplicaProgramaFidelidad()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCI�N");
	//Generamos la fecha en la que corre el proceso
	String fechaActual = "";
	Calendar calendarioActual = Calendar.getInstance();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//Obtenemos la fecha Actual
	
	try
	{
		fechaActual = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
	}catch(Exception exc)
	{
		System.out.println(exc.toString());
	}
	Double valorPuntos = ParametrosDAO.retornarValorNumericoLocalDouble("VALORPUNTO");
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocalSinBodega();
	ArrayList<PedidoPlanFidelizacion> pedidos;
	PedidoPlanFidelizacion pedidoTemp;
	double puntosSumar;
	//Realizamos recuperación de datos de integración de brevo
	IntegracionCRM brevo = IntegracionCRMDAO.obtenerInformacionIntegracion("BREVO");
	//Se realiza logica para envio de correo
	OkHttpClient client = new OkHttpClient();
    // Configuración global
    String apiKey = brevo.getAccessToken();
    String senderEmail = "mercadeo@pizzaamericana.com.co";
    String senderName = "Pizza Americana";
    String subjectDefault = "Gracias por tu compra";
    ArrayList<JSONObject> destinatarios = new ArrayList();
    int templateId = 2; // ID de la plantilla en Brevo
    double puntosAcumulados = 0;
	for(Tienda tien : tiendas)
	{
		destinatarios = new ArrayList();
		if(!tien.getHostBD().equals(new String("")))
		{
			//Debemos obtener los pedidos de una tienda determinada
			pedidos = PedidoDAO.obtenerPedidosValidarFidelizacion(fechaActual, tien.getHostBD());
			for(int i = 0; i < pedidos.size(); i++)
			{
				pedidoTemp = pedidos.get(i);
				//Validamos la existencia en el plan de fidelizacion
				boolean clienteFideliza = ClienteFidelizacionDAO.existeClienteFidelizacion(pedidoTemp.getCorreo());
				if(clienteFideliza)
				{
					puntosSumar = pedidoTemp.getValorNeto()/valorPuntos;
					boolean existeTransaccion = FidelizacionTransaccionDAO.existeFidelizacionTransaccion(pedidoTemp.getCorreo(), pedidoTemp.getIdTienda(), pedidoTemp.getIdPedidoTienda());
					if(!existeTransaccion && puntosSumar > 0)
					{
						puntosAcumulados = ClienteFidelizacionDAO.sumarPuntosClienteFidelizacion(pedidoTemp.getCorreo(), puntosSumar);
						FidelizacionTransaccion fidelizaTransac = new FidelizacionTransaccion(pedidoTemp.getCorreo(), pedidoTemp.getIdTienda(), pedidoTemp.getIdPedidoTienda(), pedidoTemp.getValorNeto(), puntosSumar);
						FidelizacionTransaccionDAO.insertarFidelizacionTransaccion(fidelizaTransac);
				        // Lista de destinatarios con nombres y puntos
				        destinatarios.add(new JSONObject().put("email", pedidoTemp.getCorreo()).put("nombre", pedidoTemp.getNombreCliente()).put("puntos", puntosSumar).put("puntostotal", puntosAcumulados));
					}
				}
			}
			//REALIZAMOS UNA SEPARACIÓN POR CANTIDAD DE PUNTOS 
			
			
			//INICIAMOS POR MENOS DE 50 PUNTOS
			//Realiza el envío de correo con Brevo por tienda
			// Construcción del JSON principal
			JSONObject paramsDefault = new JSONObject();
	        paramsDefault.put("nombre", "maria isabel");
	        paramsDefault.put("puntos", 0);
	        paramsDefault.put("puntostotal", 0);
	        JSONObject jsonRequest = new JSONObject();
	        jsonRequest.put("subject", subjectDefault);
	        jsonRequest.put("sender", new JSONObject().put("email", senderEmail).put("name", senderName));
	        jsonRequest.put("templateId", 2);
	        jsonRequest.put("params", paramsDefault);
	        // Construcción de `messageVersions` con parámetros personalizados
	        JSONArray messageVersions = new JSONArray();
	        for (JSONObject destinatario : destinatarios) {
	        	if(destinatario.getInt("puntostotal") < 50)
	        	{
	        		// Parámetros personalizados por destinatario
		            JSONObject params = new JSONObject();
		            params.put("nombre", destinatario.getString("nombre"));
		            params.put("puntos", destinatario.getInt("puntos"));
		            params.put("puntostotal", destinatario.getInt("puntostotal"));
		            JSONObject messageVersion = new JSONObject();
		            messageVersion.put("to", new JSONArray().put(
		                new JSONObject().put("email", destinatario.getString("email")).put("name", destinatario.getString("nombre"))
		            ));
		            messageVersion.put("params", params);
		            messageVersion.put("subject", "Hola " + destinatario.getString("nombre") + ", gracias por tu compra");
		           
		            messageVersions.put(messageVersion);
	        	} 
	        }
	        if(messageVersions.length() > 0)
	        {
	        	jsonRequest.put("messageVersions", messageVersions);
		        // Envío de la solicitud HTTP
		        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonRequest.toString());
		        Request request = new Request.Builder()
		                .url("https://api.brevo.com/v3/smtp/email")
		                .post(body)
		                .addHeader("Content-Type", "application/json")
		                .addHeader("Accept", "application/json")
		                .addHeader("api-key", apiKey)
		                .build();
		        // Ejecución de la solicitud
		        try (Response response = client.newCall(request).execute()) {
		            System.out.println("Response Code: " + response.code());
		            System.out.println("Response Body: " + response.body().string());
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
	        }
	        
	        //50 A 99 PUNTOS: #17
	        paramsDefault = new JSONObject();
	        paramsDefault.put("nombre", "maria isabel");
	        paramsDefault.put("puntos", 0);
	        paramsDefault.put("puntostotal", 0);
	        jsonRequest = new JSONObject();
	        jsonRequest.put("subject", subjectDefault);
	        jsonRequest.put("sender", new JSONObject().put("email", senderEmail).put("name", senderName));
	        jsonRequest.put("templateId", 17);
	        jsonRequest.put("params", paramsDefault);
	        messageVersions = new JSONArray();
	        for (JSONObject destinatario : destinatarios) {
	        	if(destinatario.getInt("puntostotal") >= 50 && destinatario.getInt("puntostotal") < 99 )
	        	{
	        		// Parámetros personalizados por destinatario
		            JSONObject params = new JSONObject();
		            params.put("nombre", destinatario.getString("nombre"));
		            params.put("puntos", destinatario.getInt("puntos"));
		            params.put("puntostotal", destinatario.getInt("puntostotal"));
		            JSONObject messageVersion = new JSONObject();
		            messageVersion.put("to", new JSONArray().put(
		                new JSONObject().put("email", destinatario.getString("email")).put("name", destinatario.getString("nombre"))
		            ));
		            messageVersion.put("params", params);
		            messageVersion.put("subject", "Hola " + destinatario.getString("nombre") + ", gracias por tu compra");
		           
		            messageVersions.put(messageVersion);
	        	} 
	        }
	        if(messageVersions.length() > 0)
	        {
	        	jsonRequest.put("messageVersions", messageVersions);
		        // Envío de la solicitud HTTP
		        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonRequest.toString());
		        Request request = new Request.Builder()
		                .url("https://api.brevo.com/v3/smtp/email")
		                .post(body)
		                .addHeader("Content-Type", "application/json")
		                .addHeader("Accept", "application/json")
		                .addHeader("api-key", apiKey)
		                .build();
		        // Ejecución de la solicitud
		        try (Response response = client.newCall(request).execute()) {
		            System.out.println("Response Code: " + response.code());
		            System.out.println("Response Body: " + response.body().string());
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
	        }
	        //100 A 149 PUNTOS #18
	        paramsDefault = new JSONObject();
	        paramsDefault.put("nombre", "maria isabel");
	        paramsDefault.put("puntos", 0);
	        paramsDefault.put("puntostotal", 0);
	        jsonRequest = new JSONObject();
	        jsonRequest.put("subject", subjectDefault);
	        jsonRequest.put("sender", new JSONObject().put("email", senderEmail).put("name", senderName));
	        jsonRequest.put("templateId", 18);
	        jsonRequest.put("params", paramsDefault);
	        messageVersions = new JSONArray();
	        for (JSONObject destinatario : destinatarios) {
	        	if(destinatario.getInt("puntostotal") >= 100 && destinatario.getInt("puntostotal") < 149 )
	        	{
	        		// Parámetros personalizados por destinatario
		            JSONObject params = new JSONObject();
		            params.put("nombre", destinatario.getString("nombre"));
		            params.put("puntos", destinatario.getInt("puntos"));
		            params.put("puntostotal", destinatario.getInt("puntostotal"));
		            JSONObject messageVersion = new JSONObject();
		            messageVersion.put("to", new JSONArray().put(
		                new JSONObject().put("email", destinatario.getString("email")).put("name", destinatario.getString("nombre"))
		            ));
		            messageVersion.put("params", params);
		            messageVersion.put("subject", "Hola " + destinatario.getString("nombre") + ", gracias por tu compra");
		           
		            messageVersions.put(messageVersion);
	        	} 
	        }
	        if(messageVersions.length() > 0)
	        {
	        	jsonRequest.put("messageVersions", messageVersions);
		        // Envío de la solicitud HTTP
		        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonRequest.toString());
		        Request request = new Request.Builder()
		                .url("https://api.brevo.com/v3/smtp/email")
		                .post(body)
		                .addHeader("Content-Type", "application/json")
		                .addHeader("Accept", "application/json")
		                .addHeader("api-key", apiKey)
		                .build();
		        // Ejecución de la solicitud
		        try (Response response = client.newCall(request).execute()) {
		            System.out.println("Response Code: " + response.code());
		            System.out.println("Response Body: " + response.body().string());
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
	        }
	        //150 A 199 PUNTOS: #19
	        paramsDefault = new JSONObject();
	        paramsDefault.put("nombre", "maria isabel");
	        paramsDefault.put("puntos", 0);
	        paramsDefault.put("puntostotal", 0);
	        jsonRequest = new JSONObject();
	        jsonRequest.put("subject", subjectDefault);
	        jsonRequest.put("sender", new JSONObject().put("email", senderEmail).put("name", senderName));
	        jsonRequest.put("templateId", 19);
	        jsonRequest.put("params", paramsDefault);
	        messageVersions = new JSONArray();
	        for (JSONObject destinatario : destinatarios) {
	        	if(destinatario.getInt("puntostotal") >= 150 && destinatario.getInt("puntostotal") < 199 )
	        	{
	        		// Parámetros personalizados por destinatario
		            JSONObject params = new JSONObject();
		            params.put("nombre", destinatario.getString("nombre"));
		            params.put("puntos", destinatario.getInt("puntos"));
		            params.put("puntostotal", destinatario.getInt("puntostotal"));
		            JSONObject messageVersion = new JSONObject();
		            messageVersion.put("to", new JSONArray().put(
		                new JSONObject().put("email", destinatario.getString("email")).put("name", destinatario.getString("nombre"))
		            ));
		            messageVersion.put("params", params);
		            messageVersion.put("subject", "Hola " + destinatario.getString("nombre") + ", gracias por tu compra");
		           
		            messageVersions.put(messageVersion);
	        	} 
	        }
	        if(messageVersions.length() > 0)
	        {
	        	jsonRequest.put("messageVersions", messageVersions);
		        // Envío de la solicitud HTTP
		        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonRequest.toString());
		        Request request = new Request.Builder()
		                .url("https://api.brevo.com/v3/smtp/email")
		                .post(body)
		                .addHeader("Content-Type", "application/json")
		                .addHeader("Accept", "application/json")
		                .addHeader("api-key", apiKey)
		                .build();
		        // Ejecución de la solicitud
		        try (Response response = client.newCall(request).execute()) {
		            System.out.println("Response Code: " + response.code());
		            System.out.println("Response Body: " + response.body().string());
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
	        }
	        //200 A 249 PUNTOS #20 
	        paramsDefault = new JSONObject();
	        paramsDefault.put("nombre", "maria isabel");
	        paramsDefault.put("puntos", 0);
	        paramsDefault.put("puntostotal", 0);
	        jsonRequest = new JSONObject();
	        jsonRequest.put("subject", subjectDefault);
	        jsonRequest.put("sender", new JSONObject().put("email", senderEmail).put("name", senderName));
	        jsonRequest.put("templateId", 20);
	        jsonRequest.put("params", paramsDefault);
	        messageVersions = new JSONArray();
	        for (JSONObject destinatario : destinatarios) {
	        	if(destinatario.getInt("puntostotal") >= 200 && destinatario.getInt("puntostotal") < 249 )
	        	{
	        		// Parámetros personalizados por destinatario
		            JSONObject params = new JSONObject();
		            params.put("nombre", destinatario.getString("nombre"));
		            params.put("puntos", destinatario.getInt("puntos"));
		            params.put("puntostotal", destinatario.getInt("puntostotal"));
		            JSONObject messageVersion = new JSONObject();
		            messageVersion.put("to", new JSONArray().put(
		                new JSONObject().put("email", destinatario.getString("email")).put("name", destinatario.getString("nombre"))
		            ));
		            messageVersion.put("params", params);
		            messageVersion.put("subject", "Hola " + destinatario.getString("nombre") + ", gracias por tu compra");
		           
		            messageVersions.put(messageVersion);
	        	} 
	        }
	        if(messageVersions.length() > 0)
	        {
	        	jsonRequest.put("messageVersions", messageVersions);
		        // Envío de la solicitud HTTP
		        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonRequest.toString());
		        Request request = new Request.Builder()
		                .url("https://api.brevo.com/v3/smtp/email")
		                .post(body)
		                .addHeader("Content-Type", "application/json")
		                .addHeader("Accept", "application/json")
		                .addHeader("api-key", apiKey)
		                .build();
		        // Ejecución de la solicitud
		        try (Response response = client.newCall(request).execute()) {
		            System.out.println("Response Code: " + response.code());
		            System.out.println("Response Body: " + response.body().string());
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
	        }
	        //250 A 299 PUNTOS 21
	        paramsDefault = new JSONObject();
	        paramsDefault.put("nombre", "maria isabel");
	        paramsDefault.put("puntos", 0);
	        paramsDefault.put("puntostotal", 0);
	        jsonRequest = new JSONObject();
	        jsonRequest.put("subject", subjectDefault);
	        jsonRequest.put("sender", new JSONObject().put("email", senderEmail).put("name", senderName));
	        jsonRequest.put("templateId", 21);
	        jsonRequest.put("params", paramsDefault);
	        messageVersions = new JSONArray();
	        for (JSONObject destinatario : destinatarios) {
	        	if(destinatario.getInt("puntostotal") >= 250 && destinatario.getInt("puntostotal") < 299 )
	        	{
	        		// Parámetros personalizados por destinatario
		            JSONObject params = new JSONObject();
		            params.put("nombre", destinatario.getString("nombre"));
		            params.put("puntos", destinatario.getInt("puntos"));
		            params.put("puntostotal", destinatario.getInt("puntostotal"));
		            JSONObject messageVersion = new JSONObject();
		            messageVersion.put("to", new JSONArray().put(
		                new JSONObject().put("email", destinatario.getString("email")).put("name", destinatario.getString("nombre"))
		            ));
		            messageVersion.put("params", params);
		            messageVersion.put("subject", "Hola " + destinatario.getString("nombre") + ", gracias por tu compra");
		           
		            messageVersions.put(messageVersion);
	        	} 
	        }
	        if(messageVersions.length() > 0)
	        {
	        	jsonRequest.put("messageVersions", messageVersions);
		        // Envío de la solicitud HTTP
		        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonRequest.toString());
		        Request request = new Request.Builder()
		                .url("https://api.brevo.com/v3/smtp/email")
		                .post(body)
		                .addHeader("Content-Type", "application/json")
		                .addHeader("Accept", "application/json")
		                .addHeader("api-key", apiKey)
		                .build();
		        // Ejecución de la solicitud
		        try (Response response = client.newCall(request).execute()) {
		            System.out.println("Response Code: " + response.code());
		            System.out.println("Response Body: " + response.body().string());
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
	        }
	        //300 PUNTOS EN ADELANTE: #22
	        paramsDefault = new JSONObject();
	        paramsDefault.put("nombre", "maria isabel");
	        paramsDefault.put("puntos", 0);
	        paramsDefault.put("puntostotal", 0);
	        jsonRequest = new JSONObject();
	        jsonRequest.put("subject", subjectDefault);
	        jsonRequest.put("sender", new JSONObject().put("email", senderEmail).put("name", senderName));
	        jsonRequest.put("templateId", 22);
	        jsonRequest.put("params", paramsDefault);
	        messageVersions = new JSONArray();
	        for (JSONObject destinatario : destinatarios) {
	        	if(destinatario.getInt("puntostotal") >= 300)
	        	{
	        		// Parámetros personalizados por destinatario
		            JSONObject params = new JSONObject();
		            params.put("nombre", destinatario.getString("nombre"));
		            params.put("puntos", destinatario.getInt("puntos"));
		            params.put("puntostotal", destinatario.getInt("puntostotal"));
		            JSONObject messageVersion = new JSONObject();
		            messageVersion.put("to", new JSONArray().put(
		                new JSONObject().put("email", destinatario.getString("email")).put("name", destinatario.getString("nombre"))
		            ));
		            messageVersion.put("params", params);
		            messageVersion.put("subject", "Hola " + destinatario.getString("nombre") + ", gracias por tu compra");
		           
		            messageVersions.put(messageVersion);
	        	} 
	        }
	        if(messageVersions.length() > 0)
	        {
	        	jsonRequest.put("messageVersions", messageVersions);
		        // Envío de la solicitud HTTP
		        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonRequest.toString());
		        Request request = new Request.Builder()
		                .url("https://api.brevo.com/v3/smtp/email")
		                .post(body)
		                .addHeader("Content-Type", "application/json")
		                .addHeader("Accept", "application/json")
		                .addHeader("api-key", apiKey)
		                .build();
		        // Ejecución de la solicitud
		        try (Response response = client.newCall(request).execute()) {
		            System.out.println("Response Code: " + response.code());
		            System.out.println("Response Body: " + response.body().string());
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
	        }
	    //FINALIZACION    
		}
	}
	
	if(false)
	{
		//Realizamos el env�o del correo electr�nico con los archivos
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("PROBLEMAS REPLICA DE USUARIOS EN TIENDAS " + fechaActual.toString());
		correo.setContrasena(infoCorreo.getClaveCorreo());
		//Tendremos que definir los destinatarios de este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPLICAUSUARIOS");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		String mensaje = "Las tiendas que no lograron la actualizaci�n fueron ";
		correo.setMensaje(mensaje);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}
	
}


}




