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
	//Ventana del aviso de vencimiento: la MISMA que usan los correos de puntos
	//por vencer (DIASAVISOVENCIMIENTO1, hoy 60), para que el numero de la
	//plantilla y el del aviso no se contradigan.
	//
	//Si el parametro falta o viene en cero se usan 60: con cero la consulta no
	//contaria nada y la plantilla diria siempre 0, que es peor que no mostrarlo.
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
					        	//Cuantos de sus puntos se le vencen dentro de la ventana. Se consulta
					        	//aqui, una vez por destinatario, y no en los siete bloques de plantilla
					        	//de abajo, que son el mismo dato repetido.
					        	//
					        	//El vencimiento de esta noche ya corrio mas arriba, asi que lo que quedo
					        	//marcado como vencido no se cuenta aqui.
					        	double puntosPorVencer = FidelizacionTransaccionDAO
					        			.obtenerResumenPuntos(pedidoTemp.getCorreo(), diasAvisoVencer).puntosPorVencer;
					        	destinatarios.add(new JSONObject().put("email", pedidoTemp.getCorreo()).put("nombre", pedidoTemp.getNombreCliente()).put("puntos", puntosSumar).put("puntostotal", puntosAcumulados).put("puntosavencer", puntosPorVencer));
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
					//Tambien se anota el rango que no le toco a nadie: en la bitacora
					//se tiene que ver la corrida COMPLETA, o no se sabe si un rango
					//falto porque no habia clientes o porque el codigo no lo conocia.
					CapaDAOSer.PlantillaCorreoPuntosDAO.registrar("DIARIO", rango, 0,
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
					//Queda escrito QUE plantilla salio y a cuantos. Es lo que el
					//2026-11-05 no se pudo responder sin ir a Brevo.
					boolean bien = response.code() >= 200 && response.code() < 300;
					CapaDAOSer.PlantillaCorreoPuntosDAO.registrar("DIARIO", rango,
							messageVersions.length(),
							bien ? CapaDAOSer.PlantillaCorreoPuntosDAO.ENVIADO
								 : CapaDAOSer.PlantillaCorreoPuntosDAO.ERROR,
							bien ? null : "HTTP " + response.code() + " " + cuerpo);
				} catch (IOException e) {
					e.printStackTrace();
					CapaDAOSer.PlantillaCorreoPuntosDAO.registrar("DIARIO", rango,
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




