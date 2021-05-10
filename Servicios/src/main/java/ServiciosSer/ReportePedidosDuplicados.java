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

public class ReportePedidosDuplicados {
	
			
		
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
		ArrayList pedidosDuplicados = PedidoDAO.ConsultarPosiblesPedidosDuplicados(fechaActual);
		//Se crea la variable que se encargará de la respuesta
		String respuesta = "";
		boolean indicadorCorreo = false;
		//ESPACIO PARA EXTRAER LAS OFERTAS NUEVAS
		respuesta = respuesta + "<table border='2'> <tr><td colspan = '4'> INFORMATIVO CLIENTES CON POSIBLES PEDIDOS DUPLICADOS " + "</td> </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Cantidad Pedidos</strong></td>"
				+  "<td><strong>Teléfono Cliente</strong></td>"
				+  "<td><strong>Id Cliente</strong></td>"
				+  "<td><strong>Fecha Pedido</strong></td>"
				+  "</tr>";
		for(int j = 0; j < pedidosDuplicados.size(); j++)
		{
			String[] duplicado = (String[])pedidosDuplicados.get(j);
			respuesta = respuesta + "<tr><td>" +  duplicado[0] + "</td><td>" +  duplicado[1] + "</td><td>" + duplicado[2] + "</td><td>" + fechaActual + "</td></tr>";
			indicadorCorreo = true;
		}	
		respuesta = respuesta + "</table> <br/>";
		
		if(indicadorCorreo)
		{
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPPEDIDODUPLICADO");
			Date fecha = new Date();
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("POSIBLE PEDIDO DUPLICADO " + fecha.toString());
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("Mucho cuidado, para el día en cuestión hay varios pedidos para un mismo cliente, revisar si son duplicados: \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
			
	}
	
		
}

