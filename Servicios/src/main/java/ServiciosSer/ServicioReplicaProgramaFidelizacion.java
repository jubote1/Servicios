package ServiciosSer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

import CapaDAOSer.ExcepcionFidelizacionDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ItemInventarioDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.ExcepcionFidelizacion;
import ModeloSer.Insumo;
import ModeloSer.PedidoPlanFidelizacion;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import capaDAOCC.ClienteFidelizacionDAO;
import capaDAOCC.ClienteNoFidelizacionDAO;
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

public class ServicioReplicaProgramaFidelizacion {

	/** Pausa entre correos de aviso. Dos segundos es el ritmo que aguanta Gmail. */
	private static final long MILIS_ENTRE_AVISOS = 2000L;
	
	
	
	
public static void main(String[] args)
{
	ServicioReplicaProgramaFidelizacion reporteReplicaUsuarios = new ServicioReplicaProgramaFidelizacion();
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
		//OJO
		fechaActual = dateFormat.format(calendarioActual.getTime());
	}catch(Exception exc)
	{
		System.out.println(exc.toString());
	}
	Double valorPuntos = ParametrosDAO.retornarValorNumericoLocalDouble("VALORPUNTO");
	int diasVigencia = ParametrosDAO.retornarValorNumerico("DIASVIGENCIAPUNTOS");
	if(diasVigencia == 0)
	{
		diasVigencia = 180;
	}
	//Revisamos si hay excepcion de fidelizacion en la fecha
	boolean existeExcepcion = false;
	ExcepcionFidelizacion excepcion = ExcepcionFidelizacionDAO.retornarExcepcionesFidelizacion(fechaActual);
	if(excepcion.getIdExcepcion() > 0)
	{
		existeExcepcion = true;
	}
	
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
    //Aplicamos lógica para el vencimiento de transacciones.
    ArrayList<FidelizacionTransaccion> vencimientos = FidelizacionTransaccionDAO.obtenerFidelizacionTransaccionesVencimiento();
    double puntosVencidos = 0;
    for(FidelizacionTransaccion venTemp: vencimientos)
    {
    	puntosVencidos = venTemp.getPuntos() - venTemp.getPuntosRedimidos();
    	FidelizacionTransaccionDAO.vencerFidelizacionTransaccion(venTemp.getCorreo(), venTemp.getIdTienda(), venTemp.getIdPedidoTienda(), puntosVencidos);
    	//Disminuimos los puntos vencidos utilizamos el método de redimir aunque estrictamente no es redención.
    	ClienteFidelizacionDAO.redimirPuntosClienteFidelizacion(venTemp.getCorreo(), puntosVencidos);
    }
	for(Tienda tien : tiendas)
	{
		destinatarios = new ArrayList();
		if(!tien.getHostBD().equals(new String("")))
		{
			//Debemos obtener los pedidos de una tienda determinada
			pedidos = PedidoDAO.obtenerPedidosValidarFidelizacion(fechaActual, tien.getHostBD());
			for(int i = 0; i < pedidos.size(); i++)
			{
				try
				{
					pedidoTemp = pedidos.get(i);
					//Validamos la existencia en el plan de fidelizacion
					boolean clienteFideliza = ClienteFidelizacionDAO.existeClienteFidelizacion(pedidoTemp.getCorreo());
					if(clienteFideliza)
					{
						puntosSumar = 0;
						if(existeExcepcion)
						{
							//Debemos hacer la validación que si estemos en el rango de la excepcion
							String strFechaPedido = pedidoTemp.getFechaInsercion();
							String strFechaInicioExp = fechaActual + " " + excepcion.getHoraInicio()+":00";
							String strFechaIFinExp = fechaActual + " " + excepcion.getHoraFin()+":00";
							DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
							LocalDateTime fechaPedido = LocalDateTime.parse(strFechaPedido, formatter);
							LocalDateTime fechaInicioExp = LocalDateTime.parse(strFechaInicioExp, formatter);
							LocalDateTime fechaIFinExp = LocalDateTime.parse(strFechaIFinExp, formatter);
							if (fechaPedido.isAfter(fechaInicioExp) && fechaPedido.isBefore(fechaIFinExp)) {
								puntosSumar = pedidoTemp.getValorNeto()/excepcion.getValorPunto();
					        } else {
					        	puntosSumar = pedidoTemp.getValorNeto()/valorPuntos;
					        }
						}else
						{
							puntosSumar = pedidoTemp.getValorNeto()/valorPuntos;
						}
						boolean existeTransaccion = FidelizacionTransaccionDAO.existeFidelizacionTransaccion(pedidoTemp.getCorreo(), pedidoTemp.getIdTienda(), pedidoTemp.getIdPedidoTienda());
						if(!existeTransaccion && puntosSumar > 0)
						{
							puntosAcumulados = ClienteFidelizacionDAO.sumarPuntosClienteFidelizacion(pedidoTemp.getCorreo(), puntosSumar);
							FidelizacionTransaccion fidelizaTransac = new FidelizacionTransaccion(pedidoTemp.getCorreo(), pedidoTemp.getIdTienda(), pedidoTemp.getIdPedidoTienda(), pedidoTemp.getValorNeto(), puntosSumar, pedidoTemp.getUsuarioPedido());
							FidelizacionTransaccionDAO.insertarFidelizacionTransaccion(fidelizaTransac, diasVigencia);
					        /*
					         * Los puntos se acumulan siempre, pero el correo solo se le manda a
					         * quien tenga una direccion que sirva. Mandarle a una direccion mal
					         * escrita gasta un envio de Brevo, que se paga, y no llega a nadie.
					         *
					         * Se usa el validador del central con el nombre completo del paquete
					         * a proposito. Servicios tiene su propia copia de ControladorEnvioCorreo
					         * en utilidadesSer, y si se duplicara la regla las dos podrian terminar
					         * diciendo cosas distintas del mismo correo. La regla vive en un solo
					         * lugar y desde ahi la usan el central, el POS y este proceso.
					         */
					        if(utilidadesCC.ControladorEnvioCorreo.esDireccionValida(pedidoTemp.getCorreo()))
					        {
					        	// Lista de destinatarios con nombres y puntos
					        	destinatarios.add(new JSONObject().put("email", pedidoTemp.getCorreo()).put("nombre", pedidoTemp.getNombreCliente()).put("puntos", puntosSumar).put("puntostotal", puntosAcumulados));
					        }
					        else
					        {
					        	System.out.println("Fidelizacion: se acumulan los puntos pero no se envia correo,"
					        			+ " la direccion no es valida: [" + pedidoTemp.getCorreo() + "]");
					        }
						}
					}
				}
				catch(Exception excPedido)
				{
					//Un pedido con datos malos no puede tumbar el proceso completo.
					//Antes, una fechainsercion nula o con otro formato hacia estallar
					//LocalDateTime.parse, la excepcion salia del main y las tiendas que
					//faltaban por recorrer no acumulaban puntos ese dia.
					System.out.println("Fidelizacion: se omite un pedido de la tienda "
							+ tien.getIdTienda() + " (" + (i + 1) + " de " + pedidos.size()
							+ "): " + excPedido);
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
	//Al final realizamos la depuración de las no deseos de estar en el plan de fidelizacion
	ClienteNoFidelizacionDAO.depurarExistenciaClienteNoFidelizacion();

	/*
	 * Aviso de puntos por vencerse. Va de ultimo y dentro de su propio try a
	 * proposito: lo importante de este proceso es acumular los puntos de todas
	 * las tiendas, y un problema mandando correos no puede impedir eso ni
	 * dejar tiendas sin procesar. Si falla, se pierde el aviso de una noche y
	 * al dia siguiente se recupera solo, porque los que no se alcanzaron a
	 * avisar siguen con la columna de control en nulo.
	 */
	try
	{
		enviarAvisosVencimiento();
	}
	catch(Exception excAviso)
	{
		System.out.println("Aviso de vencimiento de puntos: fallo el bloque completo, "
				+ "se reintenta manana. " + excAviso);
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



	/**
	 * Avisa por correo a los clientes que tienen puntos por vencerse.
	 *
	 * Son dos avisos en dos momentos: uno temprano, a los 60 dias, que le da al
	 * cliente tiempo de planear una compra, y un ultimo recordatorio a los 15
	 * dias para quien no redimio. Los dos se controlan con su propia columna en
	 * fidelizacion_transaccion, asi que a nadie se le manda el mismo aviso dos
	 * veces.
	 *
	 * Tres decisiones que vale la pena explicar:
	 *
	 * El aviso de 15 dias va primero. Si un cliente cae en las dos ventanas la
	 * misma noche, es mejor que reciba el urgente. Y se marcan las dos columnas
	 * para que no le llegue el otro correo al dia siguiente.
	 *
	 * Hay tope por noche. El dia que esto arranque hay cerca de seis mil
	 * clientes acumulados en la ventana de 60 dias; sin tope serian seis mil
	 * correos de una sola vez. Con el tope el atraso se drena en unas semanas y
	 * despues se estabiliza entre 60 y 210 por noche, que es el ritmo natural.
	 *
	 * Hay pausa entre correos. Es la leccion de la dispersion de premios de la
	 * ruleta: a Gmail no le molesta el volumen del dia, le molesta la rafaga.
	 *
	 * No usa Brevo. Sale por la cuenta de Gmail que ya tiene el sistema, que no
	 * cuesta por correo enviado.
	 */
	public static void enviarAvisosVencimiento()
	{
		final int diasAviso1 = numero("DIASAVISOVENCIMIENTO1", 60);
		final int diasAviso2 = numero("DIASAVISOVENCIMIENTO2", 15);
		final int maximo = numero("MAXAVISOSVENCIMIENTO", 250);

		//La cuenta sale de parametros, con el mismo respaldo que usa la ruleta.
		String cuenta = ParametrosDAO.retornarValorAlfanumerico("CUENTACORREOVENCIMIENTO");
		String clave = ParametrosDAO.retornarValorAlfanumerico("CLAVECORREOVENCIMIENTO");
		if(cuenta == null || cuenta.trim().length() == 0)
		{
			cuenta = ParametrosDAO.retornarValorAlfanumerico("CUENTACORREOWOMPI");
			clave = ParametrosDAO.retornarValorAlfanumerico("CLAVECORREOWOMPI");
		}
		if(cuenta == null || cuenta.trim().length() == 0)
		{
			System.out.println("Aviso de vencimiento: no hay cuenta de correo configurada, no se envia nada.");
			return;
		}

		//Primero el urgente, despues el temprano. El cupo de la noche se comparte.
		int enviados = enviarTanda(diasAviso2, FidelizacionTransaccionDAO.COLUMNA_AVISO_15, true,
				maximo, cuenta, clave);
		if(enviados < maximo)
		{
			enviados = enviados + enviarTanda(diasAviso1, FidelizacionTransaccionDAO.COLUMNA_AVISO_60, false,
					maximo - enviados, cuenta, clave);
		}
		System.out.println("Aviso de vencimiento de puntos: " + enviados + " correos enviados esta noche.");
	}

	/**
	 * Manda una tanda de avisos y deja constancia de cada uno.
	 *
	 * Se marca despues de enviar, no antes: si el correo no sale, el cliente
	 * queda pendiente y se le intenta manana. Es preferible avisar tarde que no
	 * avisar.
	 *
	 * @return cuantos correos salieron
	 */
	private static int enviarTanda(int dias, String columna, boolean esUltimo, int maximo,
			String cuenta, String clave)
	{
		if(maximo <= 0)
		{
			return(0);
		}
		ArrayList<FidelizacionTransaccionDAO.AvisoVencimiento> pendientes =
				FidelizacionTransaccionDAO.obtenerClientesParaAviso(dias, columna, maximo);

		int enviados = 0;
		for(FidelizacionTransaccionDAO.AvisoVencimiento aviso : pendientes)
		{
			try
			{
				/*
				 * Si la direccion no sirve no se gasta el envio, pero SI se marca como
				 * avisado. Si no se marcara, el barrido volveria a intentarlo cada
				 * noche con el mismo resultado y ocuparia el cupo de alguien a quien si
				 * se le puede escribir.
				 */
				if(!utilidadesCC.ControladorEnvioCorreo.esDireccionValida(aviso.correo))
				{
					System.out.println("Aviso de vencimiento: direccion invalida, se omite [" + aviso.correo + "]");
					FidelizacionTransaccionDAO.marcarAvisoEnviado(aviso.correo, dias, columna);
					continue;
				}

				//El ControladorEnvioCorreo del central espera su propio modelo de Correo.
				//Servicios tiene otro con el mismo nombre en ModeloSer, asi que hay que
				//nombrar el paquete completo para no tomar el equivocado.
				capaModeloCC.Correo correo = new capaModeloCC.Correo();
				correo.setUsuarioCorreo(cuenta);
				correo.setContrasena(clave);
				correo.setAsunto(utilidadesCC.PlantillaCorreoVencimientoPuntos.asunto(aviso.puntos, dias));
				correo.setMensaje(utilidadesCC.PlantillaCorreoVencimientoPuntos.cuerpo(aviso.nombre, aviso.puntos,
						aviso.fechaVence, dias, esUltimo));

				ArrayList destinos = new ArrayList();
				destinos.add(aviso.correo);

				utilidadesCC.ControladorEnvioCorreo envio =
						new utilidadesCC.ControladorEnvioCorreo(correo, destinos);
				utilidadesCC.ControladorEnvioCorreo.ResultadoEnvio resultado = envio.enviarConReintentos();

				if(resultado == utilidadesCC.ControladorEnvioCorreo.ResultadoEnvio.ENVIADO)
				{
					FidelizacionTransaccionDAO.marcarAvisoEnviado(aviso.correo, dias, columna);
					enviados++;
				}
				else if(resultado == utilidadesCC.ControladorEnvioCorreo.ResultadoEnvio.DIRECCION_INVALIDA)
				{
					//No sirve reintentarlo manana: se marca para liberar el cupo.
					FidelizacionTransaccionDAO.marcarAvisoEnviado(aviso.correo, dias, columna);
				}
				//Una falla pasajera no se marca: el correo quedo reintentandose solo y
				//si aun asi no sale, manana este cliente vuelve a aparecer en la lista.

				//Pausa entre correos. A Gmail no le molesta el volumen del dia, le
				//molesta la rafaga.
				Thread.sleep(MILIS_ENTRE_AVISOS);
			}
			catch(InterruptedException ie)
			{
				Thread.currentThread().interrupt();
				break;
			}
			catch(Exception e)
			{
				//Un cliente con datos raros no puede tumbar la tanda completa.
				System.out.println("Aviso de vencimiento: se omite " + aviso.correo + " por " + e);
			}
		}
		return(enviados);
	}

	/** Lee un parametro numerico, con valor por defecto si no esta configurado. */
	private static int numero(String parametro, int porDefecto)
	{
		try
		{
			int valor = ParametrosDAO.retornarValorNumerico(parametro);
			return(valor > 0 ? valor : porDefecto);
		}
		catch(Exception e)
		{
			return(porDefecto);
		}
	}
}




