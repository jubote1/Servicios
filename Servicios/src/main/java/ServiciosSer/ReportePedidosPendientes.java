package ServiciosSer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.apache.http.NameValuePair;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import ControladorSer.PedidoCtrl;
import ModeloSer.Correo;
import ModeloSer.Pedido;
import ModeloSer.Tienda;
import utilidadesSer.ControladorEnvioCorreo;

public class ReportePedidosPendientes {
	
			
		
	public static void main( String[] args )  
	{
		//Requerimos primero que todo obtener el rango de fechas con el fin de tener dicho rango para las consultas
		//Definimos el formato como manejaremos las fechas
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		//Traemos la fechaActual en Blanco
		String fechaActual = "";
		//Traemos la fecha actual en un date
		Date datFechaActual = new Date();
		//Comenzamos a traer la fecha actual como un String
		fechaActual = dateFormat.format(datFechaActual);
		//Con la fecha actual vamos a realizar la consulta de los pedidos
		//En este punto ya tenemos las dos fechas de interés por el momento nos interesará retornar las ofertas dadas
		// y las ofertas redimidas en estos rango de tiempo
		ArrayList<Pedido> pedidosPendientes = PedidoDAO.ConsultarPedidosPendientes(fechaActual);
		//Intentamos realizar el envío de los pedidos pendientes
		//Parametro de la URL Server
		String urlServerContact = "";
		//Se crea la variable que se encargará de la respuesta
		String respuesta = "";
		boolean indicadorCorreo = false;
		//ESPACIO PARA EXTRAER LAS OFERTAS NUEVAS
		respuesta = respuesta + "<table border='2'> <tr> INFORMATIVO PEDIDOS PENDIENTES QUE SE INTENTARON ENVIAR " + " </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Id Pedido</strong></td>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>Nombre Cliente</strong></td>"
				+  "<td><strong>Fecha Pedido</strong></td>"
				+  "<td><strong>Usuario</strong></td>"
				+  "<td><strong>Intento Reenvio</strong></td>"
				+  "</tr>";
		for(int j = 0; j < pedidosPendientes.size(); j++)
		{
			Pedido pedido = pedidosPendientes.get(j);
			//Controlamos que solo se ejecute una vez el retorno de la URL del contact center
			if(!indicadorCorreo)
			{
				urlServerContact = ParametrosDAO.retornarValorAlfanumericoLocal("URLCONTACTCENTER");
			}
			//La idea es que en este punto se va a intentar reenviar el pedido y se notificará el resultado en el correo
			PedidoCtrl pedCtrl = new PedidoCtrl();
			boolean respReenvio = pedCtrl.reenviarPedidoJava(pedido, urlServerContact);
			String strRespReenvio = "";
			if(respReenvio)
			{
				strRespReenvio = "Se reenvió y OK";
			}else
			{
				strRespReenvio = "Se reenvió y NOK";
			}
			respuesta = respuesta + "<tr><td>" +  pedido.getIdpedido() + "</td><td>" +  pedido.getNombretienda() + "</td><td>" + pedido.getNombrecliente() + "</td><td>" + pedido.getFechainsercion() + "</td><td>" + pedido.getUsuariopedido() + "</td><td>" + strRespReenvio + "</td></tr>";
			indicadorCorreo = true;
		}	
		respuesta = respuesta + "</table> <br/>";
		
		if(indicadorCorreo)
		{
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPPEDIDOPENDIENTE");
			Date fecha = new Date();
			Correo correo = new Correo();
			correo.setAsunto("INFORMATIVO PEDIDOS PENDIENTES ENVIADOS " + fecha.toString());
			correo.setContrasena("Pizzaamericana2017");
			correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
			correo.setMensaje("Existían pedidos pendientes, los cuales se intentaron reenviar con los siguientes detalles y resultados: \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
		
		
		respuesta = "";
		//Obtenemos el valor de control para saber cuantas veces se alerta un pedido
		int maxAlertasPendientes  = ParametrosDAO.retornarValorNumericoLocal("CANTVECESPENDIENTE");
		pedidosPendientes = PedidoDAO.ConsultarPedidosPendientes(fechaActual);
		//Parametro de la URL Server
		indicadorCorreo = false;
		//ESPACIO PARA EXTRAER LAS OFERTAS NUEVAS
		respuesta = respuesta + "<table border='2'> <tr> URGENTE EXISTEN PEDIDOS PENDIENTES " + " </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Id Pedido</strong></td>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>Nombre Cliente</strong></td>"
				+  "<td><strong>Fecha Pedido</strong></td>"
				+  "<td><strong>Usuario</strong></td>"
				+  "</tr>";
		for(int i = 0; i < pedidosPendientes.size(); i++)
		{
			Pedido pedido = pedidosPendientes.get(i);
			if(PedidoDAO.seDebeReportar(pedido.getIdpedido(), "F", maxAlertasPendientes))
			{
				respuesta = respuesta + "<tr><td>" +  pedido.getIdpedido() + "</td><td>" +  pedido.getNombretienda() + "</td><td>" + pedido.getNombrecliente() + "</td><td>" + pedido.getFechainsercion() + "</td><td>" + pedido.getUsuariopedido() + "</td></tr>";
				indicadorCorreo = true;
			}
		}
		
		respuesta = respuesta + "</table> <br/>";
		
		if(indicadorCorreo)
		{
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPPEDIDOPENDIENTE");
			Date fecha = new Date();
			Correo correo = new Correo();
			correo.setAsunto("URGENTE PEDIDOS PENDIENTES CONTACT CENTER " + fecha.toString());
			correo.setContrasena("Pizzaamericana2017");
			correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
			correo.setMensaje("Urgente existen pedidos pendientes por ser enviado a las tiendas: \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
		
		
		//Incluiremos la lógica para los PC pedidos en curso.
		//Obtenemos los pedidos que están en curso que llevan más de 7 minutos
		ArrayList<Pedido> pedidosEnCurso = PedidoDAO.ConsultarPedidosEnCurso(fechaActual);
		//Se crea la variable que se encargará de la respuesta
		respuesta = "";
		indicadorCorreo = false;
		
		
		respuesta = respuesta + "<table border='2'> <tr><td colspan = '5'> CUIDADO PEDIDOS EN CURSO QUE LLEVAN MÁS DE 7 MINUTOS Y NO SE HAN FINALIZADO " + "</td> </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Id Pedido</strong></td>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>Nombre Cliente</strong></td>"
				+  "<td><strong>Fecha/Hora inició Pedido</strong></td>"
				+  "<td><strong>Usuario</strong></td>"
				+  "</tr>";
		for(int i = 0; i < pedidosEnCurso.size(); i++)
		{
			Pedido pedido = pedidosEnCurso.get(i);
			if(PedidoDAO.seDebeReportar(pedido.getIdpedido(), "C", maxAlertasPendientes))
			{
				respuesta = respuesta + "<tr><td>" +  pedido.getIdpedido() + "</td><td>" +  pedido.getNombretienda() + "</td><td>" + pedido.getNombrecliente() + "</td><td>" + pedido.getFechainsercion() + "</td><td>" + pedido.getUsuariopedido() + "</td></tr>";
				indicadorCorreo = true;
			}	
		}
		
		respuesta = respuesta + "</table> <br/>";
		
		if(indicadorCorreo)
		{
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPPEDIDOPENDIENTE");
			Date fecha = new Date();
			Correo correo = new Correo();
			correo.setAsunto("ATENCIÓN PEDIDOS QUE SE ESTÁN TOMANDO HACE MÁS DE 7 MINUTOS " + fecha.toString());
			correo.setContrasena("Pizzaamericana2017");
			correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
			correo.setMensaje("CUIDADO! Existen pedidos que se están tomando hace más de 7 minutos y no se han enviado a la tienda: \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
		
		
	}
	
		
}

