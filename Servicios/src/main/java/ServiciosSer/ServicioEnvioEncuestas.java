package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.GeneralDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import capaDAOCC.LogEncuestaServicioDAO;
import capaDAOCC.ParametrosDAO;
import capaDAOPOS.PedidoDAO;
import capaModeloCC.LogEncuestaServicio;
import capaModeloCC.Tienda;
import capaModeloPOS.ClienteEnvioEncuesta;
import utilidadesSer.ControladorEnvioCorreo;

import com.fasterxml.jackson.databind.ObjectMapper;

import capaDAOCC.IntegracionCRMDAO;
import capaDAOCC.ParametrosDAO;
import capaModeloCC.IntegracionCRM;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 2026-02-26 Hacemos cambios para no realizar el envío con el parsing y a través de la herramienta BREVO.
 */
public class ServicioEnvioEncuestas {
	
	private static final String BREVO_URL =
            ParametrosDAO.retornarValorAlfanumerico("BREVO_ULR_WP");

    private static final String NUMEROWHATSAPPBREVO =
            ParametrosDAO.retornarValorAlfanumerico("NUMEROWHATSAPPBREVO");
    
    private static final int IDPLANTILLABREVO =
            ParametrosDAO.retornarValorNumerico("IDPLANTILLABREVOENCUESTA");

    private static final HttpClient CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

    private static final ObjectMapper MAPPER = new ObjectMapper();

	public static void main( String[] args )
	{
		Map<String, Object> params;
		// 1. Recuperamos la fecha actual del sistema con la fecha apertura
		String fechaActual = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			fechaActual = dateFormat.format(calendarioActual.getTime());
		}catch(Exception exc)
		{
			System.out.println(exc.toString());
		}
		int hora = calendarioActual.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
		int minutos = calendarioActual.get(Calendar.MINUTE);
		//Realizamos la validación para no envío de la encuesta en horarios prohibidos la hora debe estar entre 10:00 am y 9:00 pm
		if(hora >= 10 && hora <= 21 )
		{
			if(hora >= 10 && hora < 12)
			{
				calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
				fechaActual = dateFormat.format(calendarioActual.getTime());
			}
			ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendas();
			//Variables para la labor de cada tienda
			Tienda tiendaTemp;
			ClienteEnvioEncuesta clienteTemp;
			boolean debeEnviarEncuesta = false;
			for(int j = 0; j < tiendas.size(); j++)
			{
				tiendaTemp = tiendas.get(j);
				ArrayList <ClienteEnvioEncuesta> clientesDomicilio = PedidoDAO.obtenerClientesEnvioEncuesta(tiendaTemp.getHosbd(), fechaActual, false);
				if(clientesDomicilio.size() > 0)
				{
					//Recuperamos el correo para envío del parsing
					ArrayList correos = GeneralDAO.obtenerCorreosParametro("PARSERENCUESTASERVICIO");
					for(int i = 0; i < clientesDomicilio.size(); i++)
					{
						clienteTemp = clientesDomicilio.get(i);
						//Tomado el cliente hacemos la validación de si se debe enviar o no
						debeEnviarEncuesta = LogEncuestaServicioDAO.validarEncuestaServicio(clienteTemp.getTelefonoCelular());
						if(debeEnviarEncuesta)
						{
							//Quitamos la parte del envío del correo y más bien será el llamado al método para el envío de la encuesta
							params = Map.of(
					                "nombre", clienteTemp.getNombreCliente(),
					                "idpedido", "?idpedido=" + clienteTemp.getIdPedidoTienda() +"&idtienda=" + tiendaTemp.getIdTienda() + "&tipo=domicilio"
					        );
							try {
								enviarMensaje(IDPLANTILLABREVO, clienteTemp.getTelefonoCelular(), params);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							//Agregamos log servicio encuesta
							LogEncuestaServicio encuestaServicio = new LogEncuestaServicio(0, clienteTemp.getIdPedidoTienda(), clienteTemp.getNombreCliente(),  clienteTemp.getTelefonoCelular(), tiendaTemp.getIdTienda(),
									"", "", "");
							LogEncuestaServicioDAO.insertarLogEncuestaServicio(encuestaServicio);
							try
							{
								Thread.sleep(3000);
							}catch(Exception e)
							{
								
							}
							//Una vez enviado el correo electrónico marcarmos le encuesta como enviada
							PedidoDAO.actualizarPedidoEncuestaEnviada(tiendaTemp.getHosbd(), clienteTemp.getIdPedidoTienda(), false);
						}
					}
				}
				//Una vez terminado los clientes a domicilio continuamos con los clientes de punto de venta
				//REALIZAMOS APAGADO PARA NO ENVIAR A CLIENTES PUNTO DE VENTA
//				ArrayList <ClienteEnvioEncuesta> clientesPV = PedidoDAO.obtenerClientesEnvioEncuestaPV(tiendaTemp.getHosbd(), fechaActual, false);
//				if(clientesPV.size() > 0)
//				{
//					ArrayList correos = GeneralDAO.obtenerCorreosParametro("PARSERENCUESTASERVICIOPV");
//					for(int i = 0; i < clientesPV.size(); i++)
//					{
//						clienteTemp = clientesPV.get(i);
//						//Tomado el cliente hacemos la validación de si se debe enviar o no
//						debeEnviarEncuesta = LogEncuestaServicioDAO.validarEncuestaServicio(clienteTemp.getTelefonoCelular());
//						if(debeEnviarEncuesta)
//						{
//							Correo correo = new Correo();
//							CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
//							correo.setContrasena(infoCorreo.getClaveCorreo());
//							correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
//							correo.setAsunto("ENCUESTA PEDIDO # " + clienteTemp.getIdPedidoTienda());
//							String mensajeCuerpoCorreo = "Numero Pedido:" + clienteTemp.getIdPedidoTienda() + " \n <br>"
//									+ "Nombre Cliente:" + clienteTemp.getNombreCliente() + " \n <br>"
//									+ "Numero Telefono:" + clienteTemp.getTelefonoCelular() + " \n <br>"
//									+ "email:" + clienteTemp.getEmail() + " \n <br>"
//									+ "idtienda:" + tiendaTemp.getIdTienda() + " \n <br>"
//									+ "Nombre:" + clienteTemp.getNombreCliente() + " \n <br>"
//									+ "Telefono:" + clienteTemp.getTelefonoCelular() + " \n <br>";
//							correo.setMensaje(mensajeCuerpoCorreo);ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
//							contro.enviarCorreoHTML();
//							//Agregamos log servicio encuesta
//							LogEncuestaServicio encuestaServicio = new LogEncuestaServicio(0, clienteTemp.getIdPedidoTienda(), clienteTemp.getNombreCliente(),  clienteTemp.getTelefonoCelular(), tiendaTemp.getIdTienda(),
//									"", "", "");
//							LogEncuestaServicioDAO.insertarLogEncuestaServicio(encuestaServicio);
//							try
//							{
//								Thread.sleep(3000);
//							}catch(Exception e)
//							{
//								
//							}
//							//Una vez enviado el correo electrónico marcarmos le encuesta como enviada
//							PedidoDAO.actualizarPedidoEncuestaEnviada(tiendaTemp.getHosbd(), clienteTemp.getIdPedidoTienda(), false);
//						}
//					}
//				}
			}
		}
		
				
	}
	
	public static String enviarMensaje(
            int idplantilla,
            String telefono,
            Map<String, Object> params
    ) throws IOException, InterruptedException {

        Map<String, Object> body = Map.of(
                "templateId", idplantilla,
                "senderNumber", NUMEROWHATSAPPBREVO,
                "params", params,
                "contactNumbers", List.of("+57" + telefono)
        );

        String jsonBody = MAPPER.writeValueAsString(body);

        IntegracionCRM brevo =
                IntegracionCRMDAO.obtenerInformacionIntegracion("BREVO");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BREVO_URL))
                .timeout(Duration.ofSeconds(30))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("api-key", brevo.getAccessToken())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response =
                CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 == 2) {
            return response.body();
        }

        throw new RuntimeException(
                "Error Brevo WhatsApp HTTP="
                        + response.statusCode()
                        + " -> " + response.body());
    }



}
