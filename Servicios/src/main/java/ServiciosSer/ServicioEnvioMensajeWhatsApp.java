package ServiciosSer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiempoPedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ControladorSer.PedidoCtrl;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.Pedido;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import capaDAOCC.ClienteDAO;
import capaDAOCC.PedidoPagoVirtualConsolidadoDAO;
import capaModeloCC.Cliente;
import capaModeloCC.PedidoPagoVirtualConsolidado;
import utilidadesSer.ControladorEnvioCorreo;


public class ServicioEnvioMensajeWhatsApp {
	
			
		
	public static void main( String[] args )  
	{
		//Requerimos primero que todo obtener el rango de fechas con el fin de tener dicho rango para las consultas
		//Definimos el formato como manejaremos las fechas
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DecimalFormat formatea = new DecimalFormat("###,###");
		//Recuperamos la variable de NOTIFICARNOPAGOS
		boolean notificarNoPagos = false;
		boolean notificarPagos = false;
		try
		{
			notificarNoPagos = Boolean.parseBoolean(ParametrosDAO.retornarValorAlfanumericoLocal("NOTIFICARNOPAGOS"));
		}catch(Exception e)
		{
			notificarNoPagos = false;
		}
		try
		{
			notificarPagos = Boolean.parseBoolean(ParametrosDAO.retornarValorAlfanumericoLocal("NOTIFICARPAGOSWOMPI"));
		}catch(Exception e)
		{
			notificarPagos = false;
		}
		//Traemos la fechaActual en Blanco
		String fechaActual = "";
		//Traemos la fecha actual en un date
		Date datFechaActual = new Date();
		//Comenzamos a traer la fecha actual como un String
		fechaActual = dateFormat.format(datFechaActual);
		//Con la fecha actual vamos a realizar la consulta de los pedidos
		
		//Extraemos los minutos para saber cuando debemos enviar minutos	
		Calendar fechaCalendario = Calendar.getInstance();
		int minutos = fechaCalendario.get(Calendar.MINUTE);
		
		//Parametro de la URL Server
		String urlServerContact = "";
		//Se crea la variable que se encargará de la respuesta
		String respuesta = "";
		boolean indicadorCorreo = false;
		//A continuación hacemos referencia a obtener los pedidos virtuales que están pendientes y ya fueron realizados para mandar a la tienda
		ArrayList<Pedido> pedidosVirtualesRealizados = PedidoDAO.ConsultarPedidosVirtualRealizados(fechaActual);
		
		//Vamos armando un correo con los pagos realizados por cada ejecución
		respuesta = respuesta + "<table border='2'> <tr> PAGOS VIRTUALES YA REALIZADOS Y SU RESULTADO " + " </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Id Pedido</strong></td>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>Nombre Cliente</strong></td>"
				+  "<td><strong>Fecha Pedido</strong></td>"
				+  "<td><strong>Usuario</strong></td>"
				+  "<td><strong>IdLink</strong></td>"
				+  "<td><strong>Estado Envío</strong></td>"
				+  "</tr>";
		String tiendaKuno = "";
		//Luego de obtenidos los pedidos que se podrían enviar, procedemos a realizar el envío uno a uno
		for(int i = 0; i < pedidosVirtualesRealizados.size(); i++)
		{
			//Si es la primera 	vez se hace la consulta de la URL
			if(!indicadorCorreo)
			{
				urlServerContact = ParametrosDAO.retornarValorAlfanumericoLocal("URLCONTACTCENTER");
			}
			Pedido pedido = pedidosVirtualesRealizados.get(i);
			//La idea es que en este punto se va a intentar reenviar el pedido y se notificará el resultado en el correo
			PedidoCtrl pedCtrl = new PedidoCtrl();
			if(pedido.getOrigen().equals(new String("TK")))
			{
				tiendaKuno = "S";
				//Se hace una diferenciación de los pedidos en tienda virtual, aqui haremos una validación de que la hora
				//de ingreso del pedido vs la hora actual tenga más de 10 minutos para enviarlo.
				Date datefechaInsercion = new Date();
				try
				{
					datefechaInsercion = dateFormatHora.parse(pedido.getFechainsercion());
				}catch(Exception e)
				{	
				}
				
				//Hacemos la diferencia de las fechas en minutos
				//Calcularemos el tiempo Pedido
				int difTiempo = Math.abs((int) (datFechaActual.getTime() - datefechaInsercion.getTime() ));
				Math.abs(minutos = (int)TimeUnit.MILLISECONDS.toMinutes(difTiempo ));
				double dMinutos = (double) minutos;
				if(dMinutos < 10)
				{
					continue;
				}
			}else
			{
				tiendaKuno = "N";
			}
			boolean respReenvio = pedCtrl.reenviarPedidoJava(pedido, urlServerContact,tiendaKuno);
			String strRespReenvio = "";
			if(respReenvio)
			{
				strRespReenvio = "SE ENVÍO A TIENDA";
				//En este punto contamos que si hubo envío a la tienda, es aqui donde vamos a incluir la notificación
				//ESTA PARTE LA DEJAREMOS SUSPENDIDA, PORQUE NOS PARECE UN POCO INVASIVA EN CUANTO A INFORMACIÓN PARA EL CLIENTE
			}else
			{
				strRespReenvio = "ERROR AL ENVIAR";
			}
			respuesta = respuesta + "<tr><td>" +  pedido.getIdpedido() + "</td><td>" +  pedido.getNombretienda() + "</td><td>" + pedido.getNombrecliente() + "</td><td>" + pedido.getFechainsercion() + "</td><td>" + pedido.getUsuariopedido() + "</td><td>" + pedido.getIdLink() + "</td><td>" + strRespReenvio + "</td></tr>";
			indicadorCorreo = true;
		}
		
		respuesta = respuesta + "</table> <br/>";
		
		//Se debe realizar el envío del correo electrónico reportando como estuvo el envío de los pedidos pagados
		//Adicionamos condición de notificar Pagos
		if(indicadorCorreo && notificarPagos)
		{
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEPAGOSVIRTUAL");
			Date fecha = new Date();
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("PAGOS VIRTUALES - ENVIO A TIENDA " + fecha.toString());
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuación los pagos virtuales realizados y su estado de envío a tienda: \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
		
		indicadorCorreo = false;
		respuesta = "";
		respuesta = respuesta + "<table border='2'> <tr> PEDIDOS - PAGO VIRTUAL SIN REALIZAR " + " </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Id Pedido</strong></td>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>Nombre Cliente</strong></td>"
				+  "<td><strong>Fecha Pedido</strong></td>"
				+  "<td><strong>IdLink</strong></td>"
				+  "<td><strong>Usuario</strong></td>"
				+  "</tr>";
		//Posteriormente realizamos el reporte de los pagos virtuales que llevan más de 15 minutos y no se han pagado.
		//Vamos a agregar un control para ejecutar todo este bloque en los minutos 6
		if(minutos%6 == 0)
		{
			ArrayList<Pedido> pedidosVirtualesSinFin = PedidoDAO.ConsultarPagosVirtualSinPagar(fechaActual, 20);
			for(int j = 0; j < pedidosVirtualesSinFin.size(); j++)
			{
				Pedido pedidoSinPagar = pedidosVirtualesSinFin.get(j);
				boolean reportarCliente = PedidoDAO.seDebeReportarPagoVirtual(pedidoSinPagar.getIdpedido());
				if(reportarCliente)
				{
					//Realizaremos la lógica para enviarle un correo y mensaje al cliente indicando que lleva 20 minutos y no se 
					//ha realizado el pago
					capaControladorCC.PedidoCtrl pedCtrl = new capaControladorCC.PedidoCtrl();
					pedCtrl.realizarRenotificacionWompi(pedidoSinPagar.getIdLink(), pedidoSinPagar.getIdcliente(), "https://checkout.wompi.co/l/" +pedidoSinPagar.getIdLink(), pedidoSinPagar.getIdpedido());
				}
				respuesta = respuesta + "<tr><td>" +  pedidoSinPagar.getIdpedido() + "</td><td>" +  pedidoSinPagar.getNombretienda() + "</td><td>" + pedidoSinPagar.getNombrecliente() + "</td><td>" + pedidoSinPagar.getFechainsercion() + "</td><td>" + pedidoSinPagar.getIdLink() + "</td><td>" + pedidoSinPagar.getUsuariopedido() + "</td></tr>";
				indicadorCorreo = true;
			}
			
			respuesta = respuesta + "</table> <br/>";
			
			if(indicadorCorreo && notificarNoPagos)
			{
				//Recuperar la lista de distribución para este correo
				ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEVIRTUALSINPAGO");
				Date fecha = new Date();
				Correo correo = new Correo();
				CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
				correo.setAsunto("URGENTE PAGOS VIRTUALES SIN REALIZAR " + fecha.toString());
				correo.setContrasena(infoCorreo.getClaveCorreo());
				correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
				correo.setMensaje("Urgente existen pedidos con forma de pago virtual, cuyo pago no se ha realizado Y POR LO TANTO"
						+ " NO SE HA ENVIADO EL PEDIDO A LA TIENDA: \n" + respuesta);
				ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
				contro.enviarCorreoHTML();
			}
			
			//La idea en esta Franja también es ejecutar el envío del mensaje de WhatsApp si es el caso
			ArrayList<Pedido> pedidosVirtualesNotWha = PedidoDAO.ConsultarPagosVirtualSinPagarRango(fechaActual, 10,20);
			for(int j = 0; j < pedidosVirtualesNotWha.size(); j++)
			{
				Pedido pedidoSinPagar = pedidosVirtualesNotWha.get(j);
				boolean reportarCliente = PedidoDAO.seDebeReportarPagoWhatsApp(pedidoSinPagar.getIdpedido());
				if(reportarCliente)
				{
					notificarWhatsApp(pedidoSinPagar.getNombrecliente(), pedidoSinPagar.getIdpedido(), pedidoSinPagar.getIdcliente(), "https://checkout.wompi.co/l/" +pedidoSinPagar.getIdLink());
				}
			}
			
		}
		//Realizamos proceso para cancelar pedidos que tienen más de 50 minutos y enviar notificación al cliente de esta situación
		ArrayList<Pedido> pedidosVirtualesCancelar = PedidoDAO.ConsultarPagosVirtualSinPagar(fechaActual, 50);
		//Se crea la variable que se encargará de la respuesta
		respuesta = "";
		indicadorCorreo = false;
		//Vamos armando un correo con los pagos realizados por cada ejecución
		respuesta = respuesta + "<table border='2'> <tr> PAGOS VIRTUALES CANCELADOS POR NO PAGO " + " </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Id Pedido</strong></td>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>Nombre Cliente</strong></td>"
				+  "<td><strong>Fecha Pedido</strong></td>"
				+  "<td><strong>Usuario</strong></td>"
				+  "<td><strong>IdLink</strong></td>"
				+  "<td><strong>ESTADO</strong></td>"
				+  "</tr>";
		
		//Luego de obtenidos los pedidos que se podrían enviar, procedemos a realizar el envío uno a uno
		for(int i = 0; i < pedidosVirtualesCancelar.size(); i++)
		{
			//Si es la primera 	vez se hace la consulta de la URL
			if(urlServerContact.equals(new String("")))
			{
				urlServerContact = ParametrosDAO.retornarValorAlfanumericoLocal("URLCONTACTCENTER");
			}
			Pedido pedido = pedidosVirtualesCancelar.get(i);
			//La idea es que en este punto se va a intentar reenviar el pedido y se notificará el resultado en el correo
			capaControladorCC.PedidoCtrl pedCtrl = new capaControladorCC.PedidoCtrl();
			pedCtrl.realizarCancelacionWompi(pedido.getIdcliente(), pedido.getIdpedido());
			pedCtrl.cancelarPedido(pedido.getIdpedido());
			respuesta = respuesta + "<tr><td>" +  pedido.getIdpedido() + "</td><td>" +  pedido.getNombretienda() + "</td><td>" + pedido.getNombrecliente() + "</td><td>" + pedido.getFechainsercion() + "</td><td>" + pedido.getUsuariopedido() + "</td><td>" + pedido.getIdLink() + "</td><td>" + "CANCELADO" + "</td></tr>";
			indicadorCorreo = true;
		}
		
		respuesta = respuesta + "</table> <br/>";
		
		//Se debe realizar el envío del correo electrónico reportando como estuvo el envío de los pedidos pagados
		if(indicadorCorreo)
		{
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEVIRTUALSINPAGO");
			Date fecha = new Date();
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("PEDIDOS PAGO VIRTUAL CANCELADOS " + fecha.toString());
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuación los pedidos de pago virtuales cancelados por no pago en el tiempo estipulado: \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
		
		
		//Realizamos otro control al proceso para controlar si hay pedido que tengan forma de pago virtual y tengan el idlink vacío.
		ArrayList<Pedido> pedidosVirtualesSinLink = PedidoDAO.ConsultarPagosVirtualSinLink(fechaActual);
		//Se crea la variable que se encargará de la respuesta
		respuesta = "";
		indicadorCorreo = false;
		//Vamos armando un correo con los pagos realizados por cada ejecución
		respuesta = respuesta + "<table border='2'> <tr> CUIDADO PAGOS VIRTUALES SIN LINK DE PAGOS " + " </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Id Pedido</strong></td>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>Nombre Cliente</strong></td>"
				+  "<td><strong>Fecha Pedido</strong></td>"
				+  "<td><strong>Usuario</strong></td>"
				+  "<td><strong>IdLink</strong></td>"
				+  "</tr>";
		
		
		for(int i = 0; i < pedidosVirtualesSinLink.size(); i++)
		{
			Pedido pedido = pedidosVirtualesSinLink.get(i);
			respuesta = respuesta + "<tr><td>" +  pedido.getIdpedido() + "</td><td>" +  pedido.getNombretienda() + "</td><td>" + pedido.getNombrecliente() + "</td><td>" + pedido.getFechainsercion() + "</td><td>" + pedido.getUsuariopedido() + "</td><td>" + pedido.getIdLink() + "</td></tr>";
			indicadorCorreo = true;
		}
		
		respuesta = respuesta + "</table> <br/>";
		
		//Se debe realizar el envío del correo electrónico reportando como estuvo el envío de los pedidos pagados
		if(indicadorCorreo)
		{
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEVIRTUALSINPAGO");
			Date fecha = new Date();
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("OJO PEDIDOS PAGO VIRTUAL SIN LINK GENERADO  " + fecha.toString());
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuación los pedidos de pago virtuales que no tienen link se debería recrear el link y verificar que si le llegue al cliente: \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
		
		//Realizamos control de los pagos provenientes de tiendas que se han realizado y que se tienen que notificar a las tiendas
		ArrayList<PedidoPagoVirtualConsolidado> pagosNoti = PedidoPagoVirtualConsolidadoDAO.obtenerPagosNotificarTienda();
		PedidoCtrl pedCtrlNoti = new PedidoCtrl();
		capaDAOCC.PedidoPagoVirtualConsolidadoDAO pedCC = new capaDAOCC.PedidoPagoVirtualConsolidadoDAO();
		boolean respNoti;
		for(int i = 0; i < pagosNoti.size(); i++)
		{
			PedidoPagoVirtualConsolidado pedidoTemp = pagosNoti.get(i);
			//Debemos de consumir un servicio que estará expuesto en la tienda
			respNoti = pedCtrlNoti.notificarPedidoTienda(pedidoTemp.getIdLink(), pedidoTemp.getTipoPago(), pedidoTemp.getIdTienda());
			if(respNoti)
			{
				pedCC.actualizarNotificacionPago(pedidoTemp.getIdLink());
			}
		}
				
	}
		
	
	public static void notificarWhatsApp(String nombre, int idPedido, int idCliente, String linkPago)
	{
		String telefonoCelular = "";
		Cliente clienteNotif = ClienteDAO.obtenerClienteporID(idCliente);
		//Revisamos la lógica para obtener el telefono
		if(clienteNotif.getTelefonoCelular()!= null)
		{
			if(!clienteNotif.getTelefonoCelular().equals(new String("")))
			{
				if(clienteNotif.getTelefonoCelular().length() == 10)
				{
					if(clienteNotif.getTelefonoCelular().substring(0,1).equals(new String("3")))
					{
						telefonoCelular = clienteNotif.getTelefonoCelular();
					}
				}
			}
		}
		
		if(telefonoCelular.equals(new String("")))
		{
			if(clienteNotif.getTelefono()!= null)
			{
				if(!clienteNotif.getTelefono().equals(new String("")))
				{
					if(clienteNotif.getTelefono().length() == 10)
					{
						if(clienteNotif.getTelefono().substring(0,1).equals(new String("3")))
						{
							telefonoCelular = clienteNotif.getTelefono();
						}
					}
				}
			}
		}
		
		//Validaremos que el telefono celular si se hubiese podido tomar
		if(!telefonoCelular.equals(new String("")))
		{
			String jsonString = '{' +
		            "\"templateId\": \"link_pago\","+
		            "\"customerNumber\": \"whatsapp:+" + telefonoCelular +"\"," + 
		            "\"inputs\": [\""+ nombre+" - "+ idPedido +"\" , \""+ linkPago +"\"]" +
		          "}";
					//Realizamos la invocación mediante el uso de HTTPCLIENT
					HttpClient client = HttpClientBuilder.create().build();
					String rutaURLNotif = "https://us-east1-bottapizzaamericana.cloudfunctions.net/fnBottaWhatsAppNotification";
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
	}

}

