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
import ModeloSer.CorreoElectronico;
import ModeloSer.Pedido;
import ModeloSer.Tienda;
import utilidadesSer.ControladorEnvioCorreo;

public class ReportePedidosPendientesVirtual {
	
			
		
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
		//Intentamos realizar el envío de los pedidos pendientes
		//Parametro de la URL Server
		String urlServerContact = "";
		//Se crea la variable que se encargará de la respuesta
		String respuesta = "";
		boolean indicadorCorreo = false;
		respuesta = "";
		//Obtenemos el valor de control para saber cuantas veces se alerta un pedido
		int maxAlertasPendientes  = ParametrosDAO.retornarValorNumericoLocal("CANTVECESPENDIENTE");
		ArrayList<Pedido> pedidosPendientes = PedidoDAO.ConsultarPedidosPendientesVirtual(fechaActual);
		//Parametro de la URL Server
		indicadorCorreo = false;
		//ESPACIO PARA EXTRAER LAS OFERTAS NUEVAS
		respuesta = respuesta + "<table border='2'> <tr> URGENTE PEDIDOS PENDIENTES TIENDA VIRTUAL " + " </tr>";
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
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("URGENTE PEDIDOS PENDIENTES TIENDA VIRTUAL " + fecha.toString());
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("Urgente existen pedidos pendientes por ser enviado a las tiendas: \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
			
	}
	
		
}

