package Servicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOServicios.GeneralDAO;
import CapaDAOServicios.ParametrosDAO;
import CapaDAOServicios.PedidoDAO;
import Modelo.Correo;
import Modelo.Pedido;
import utilidades.ControladorEnvioCorreo;

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
		System.out.println( " antes del problema ");
		ArrayList<Pedido> pedidosPendientes = PedidoDAO.ConsultarPedidosPendientes(fechaActual);
		System.out.println(fechaActual + " cantidad de pedidos con problemas  ");
		
		//Obtenemos el valor de control para saber cuantas veces se alerta un pedido
		int maxAlertasPendientes  = ParametrosDAO.retornarValorNumericoLocal("CANTVECESPENDIENTE");

		//Se crea la variable que se encargará de la respuesta
		String respuesta = "";
		boolean indicadorCorreo = false;
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
			if(PedidoDAO.seDebeReportar(pedido.getIdpedido(), maxAlertasPendientes))
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
	}
		
	
}

