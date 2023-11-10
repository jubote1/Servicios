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

public class ReportePedidosPendientesRAPPI {
	
			
		
	public static void main( String[] args )  
	{
		//Requerimos primero que todo obtener el rango de fechas con el fin de tener dicho rango para las consultas
		//Definimos el formato como manejaremos las fechas
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		//Traemos la fechaActual en Blanco
		String fechaActual = "";
		String respuesta = "";
		boolean indicadorCorreo = false;
		//Traemos la fecha actual en un date
		Date datFechaActual = new Date();
		int maxAlertasPendientes  = ParametrosDAO.retornarValorNumericoLocal("CANTVECESPENDIENTE");
		//Comenzamos a traer la fecha actual como un String
		fechaActual = dateFormat.format(datFechaActual);
		//Con la fecha actual vamos a realizar la consulta de los pedidos
		//En este punto ya tenemos las dos fechas de interés por el momento nos interesará retornar las ofertas dadas
		// y las ofertas redimidas en estos rango de tiempo
		ArrayList<Pedido> pedidosPendientes = PedidoDAO.ConsultarPedidosPendientesRAPPI(fechaActual);
		//Intentamos realizar el envío de los pedidos pendientes
		respuesta = respuesta + "<table border='2'> <tr> INFORMATIVO PEDIDOS PENDIENTES RAPPI " + " </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Id Pedido</strong></td>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>Nombre Cliente</strong></td>"
				+  "<td><strong>Fecha Pedido</strong></td>"
				+  "<td><strong>Usuario</strong></td>"
				+  "</tr>";
		for(int j = 0; j < pedidosPendientes.size(); j++)
		{
			Pedido pedido = pedidosPendientes.get(j);
			if(PedidoDAO.seDebeReportar(pedido.getIdpedido(), "F", maxAlertasPendientes))
			{
				respuesta = respuesta + "<tr><td>" +  pedido.getIdpedido() + "</td><td>" +  pedido.getNombretienda() + "</td><td>" + pedido.getNombrecliente() + "</td><td>" + pedido.getFechainsercion() + "</td><td>" + pedido.getUsuariopedido() + "</td></tr>";
				indicadorCorreo = true;
			}
		}	
		respuesta = respuesta + "</table> <br/>";
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		if(indicadorCorreo)
		{
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPPEDIDOPENDIENTE");
			Date fecha = new Date();
			Correo correo = new Correo();
			correo.setAsunto("RAPPI PEDIDOS PENDIENTES POR GESTIÓN " + fecha.toString());
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("Pedidos pendientes de rappi por realizar gestión: \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			//Documentamos esta parte dado que igual se va a reportar a continuación el pedido pendiente
			contro.enviarCorreoHTML();
		}
	}
	
		
}

