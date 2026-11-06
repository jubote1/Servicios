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
	int diasVigencia = ParametrosDAO.retornarValorNumerico("DIASVIGENCIAPUNTOS");
	//La MISMA ventana que usa el proceso de todas las noches
	//(ServicioReplicaProgramaFidelizacion): DIASAVISOVENCIMIENTO1, hoy 60. Si el
	//reproceso usara otra, un dia repetido mostraria un numero distinto al que
	//ya habia salido, y nadie sabria a cual creerle.
	int diasAvisoVencer = ParametrosDAO.retornarValorNumerico("DIASAVISOVENCIMIENTO1");
	if(diasAvisoVencer <= 0)
	{
		diasAvisoVencer = 60;
	}
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
	/*
	 * Los rangos de puntos y la plantilla de Brevo que le toca a cada uno.
	 *
	 * Antes esto estaba QUEMADO: siete bloques identicos mas abajo, cada uno con
	 * su templateId y sus cortes escritos a mano, y los mismos siete repetidos en
	 * ServicioReplicaProgramaFidelizacionRep. Cambiar una plantilla obligaba a
	 * tocar catorce sitios y desplegar.
	 *
	 * Se lee UNA vez por corrida, no por tienda ni por destinatario.
	 */
	ArrayList<CapaDAOSer.PlantillaCorreoPuntosDAO.Rango> rangosCorreo =
			CapaDAOSer.PlantillaCorreoPuntosDAO.obtenerRangos();
	ArrayList<String> problemasRangos =
			CapaDAOSer.PlantillaCorreoPuntosDAO.revisarCobertura(rangosCorreo);
	for(int iP = 0; iP < problemasRangos.size(); iP++)
	{
		//Se grita en el log. Una configuracion con huecos se nota como clientes
		//que no reciben correo, que es justo lo que nadie ve: los cortes viejos
		//dejaban por fuera a quien tuviera 99, 149, 199, 249 o 299 puntos exactos
		//-106 clientes al 2026-11-04- y nadie se entero en meses.
		System.out.println("AVISO general.plantilla_correo_puntos: " + problemasRangos.get(iP));
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
				        // Lista de destinatarios con nombres y puntos
				        //Los puntos que se le vencen dentro de la ventana. Se consulta una
				        //vez por destinatario y no en los siete bloques de plantilla de
				        //abajo, que repiten el mismo dato.
				        double puntosPorVencer = FidelizacionTransaccionDAO
				        		.obtenerResumenPuntos(pedidoTemp.getCorreo(), diasAvisoVencer).puntosPorVencer;
				        destinatarios.add(new JSONObject().put("email", pedidoTemp.getCorreo()).put("nombre", pedidoTemp.getNombreCliente()).put("puntos", puntosSumar).put("puntostotal", puntosAcumulados).put("puntosavencer", puntosPorVencer));
					}
				}
			}
			/*
			 * EL CORREO DE PUNTOS, UNA PLANTILLA POR RANGO
			 *
			 * Aqui habia SIETE bloques copiados, uno por plantilla, con el templateId
			 * y los cortes quemados. Ahora es un solo ciclo sobre lo que diga
			 * general.plantilla_correo_puntos.
			 *
			 * Los rangos son INCLUSIVOS en los dos extremos. Los viejos estaban
			 * escritos como ">= 50 y < 99", ">= 100 y < 149" y asi, de modo que 99,
			 * 149, 199, 249 y 299 no caian en ninguno y esos clientes no recibian
			 * correo: 106 estaban parados justo ahi al 2026-11-04.
			 */
			for(int iR = 0; iR < rangosCorreo.size(); iR++)
			{
				CapaDAOSer.PlantillaCorreoPuntosDAO.Rango rango = rangosCorreo.get(iR);

				JSONObject paramsDefault = new JSONObject();
				paramsDefault.put("nombre", "maria isabel");
				paramsDefault.put("puntos", 0);
				paramsDefault.put("puntostotal", 0);
				paramsDefault.put("puntosavencer", 0);

				JSONObject jsonRequest = new JSONObject();
				jsonRequest.put("subject", rango.asunto.length() > 0 ? rango.asunto : subjectDefault);
				jsonRequest.put("sender", new JSONObject().put("email", senderEmail).put("name", senderName));
				jsonRequest.put("templateId", rango.idPlantillaBrevo);
				jsonRequest.put("params", paramsDefault);

				JSONArray messageVersions = new JSONArray();
				for (JSONObject destinatario : destinatarios)
				{
					if(!rango.cubre(destinatario.getInt("puntostotal")))
					{
						continue;
					}
					JSONObject params = new JSONObject();
					params.put("nombre", destinatario.getString("nombre"));
					params.put("puntos", destinatario.getInt("puntos"));
					params.put("puntostotal", destinatario.getInt("puntostotal"));
					params.put("puntosavencer", destinatario.getInt("puntosavencer"));
					JSONObject messageVersion = new JSONObject();
					messageVersion.put("to", new JSONArray().put(
						new JSONObject().put("email", destinatario.getString("email"))
							.put("name", destinatario.getString("nombre"))
					));
					messageVersion.put("params", params);
					messageVersion.put("subject", "Hola " + destinatario.getString("nombre")
							+ ", gracias por tu compra");
					messageVersions.put(messageVersion);
				}
				if(messageVersions.length() == 0)
				{
					//Tambien se anota el rango vacio: la bitacora tiene que mostrar
					//la corrida completa.
					CapaDAOSer.PlantillaCorreoPuntosDAO.registrar("REPROCESO", rango, 0,
							CapaDAOSer.PlantillaCorreoPuntosDAO.SIN_DESTINATARIOS, null);
					continue;
				}
				jsonRequest.put("messageVersions", messageVersions);
				System.out.println("Correo de puntos: plantilla " + rango.idPlantillaBrevo
						+ " (" + rango.descripcion + ") a " + messageVersions.length()
						+ " destinatario(s) de la tienda " + tien.getIdTienda() + ".");
				RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonRequest.toString());
				Request request = new Request.Builder()
						.url("https://api.brevo.com/v3/smtp/email")
						.post(body)
						.addHeader("Content-Type", "application/json")
						.addHeader("Accept", "application/json")
						.addHeader("api-key", apiKey)
						.build();
				try (Response response = client.newCall(request).execute()) {
					String cuerpo = response.body() == null ? "" : response.body().string();
					System.out.println("Response Code: " + response.code());
					System.out.println("Response Body: " + cuerpo);
					//El reproceso se marca distinto del diario: cuando algo sale
					//raro, lo primero que uno quiere saber es cual de los dos fue.
					boolean bien = response.code() >= 200 && response.code() < 300;
					CapaDAOSer.PlantillaCorreoPuntosDAO.registrar("REPROCESO", rango,
							messageVersions.length(),
							bien ? CapaDAOSer.PlantillaCorreoPuntosDAO.ENVIADO
								 : CapaDAOSer.PlantillaCorreoPuntosDAO.ERROR,
							bien ? null : "HTTP " + response.code() + " " + cuerpo);
				} catch (IOException e) {
					e.printStackTrace();
					CapaDAOSer.PlantillaCorreoPuntosDAO.registrar("REPROCESO", rango,
							messageVersions.length(),
							CapaDAOSer.PlantillaCorreoPuntosDAO.ERROR, e.toString());
				}
			}
			/*
			 * Y a quien no le toco ninguna plantilla se le DICE. Antes se perdia en
			 * silencio, que es como el hueco de los 99 puntos duro meses sin que
			 * nadie lo notara.
			 */
			for (JSONObject destinatario : destinatarios)
			{
				if(CapaDAOSer.PlantillaCorreoPuntosDAO.laQuePlantillaLeToca(rangosCorreo,
						destinatario.getInt("puntostotal")) == null)
				{
					System.out.println("Correo de puntos: SIN PLANTILLA para "
							+ destinatario.getString("email") + " con "
							+ destinatario.getInt("puntostotal")
							+ " puntos. Revise general.plantilla_correo_puntos.");
				}
			}
	    //FINALIZACION    
		}
	}
	//Al final realizamos la depuración de las no deseos de estar en el plan de fidelizacion
	ClienteNoFidelizacionDAO.depurarExistenciaClienteNoFidelizacion();
	
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




