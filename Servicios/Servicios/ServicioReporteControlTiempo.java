package Servicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import CapaDAOServicios.GeneralDAO;
import CapaDAOServicios.ParametrosDAO;
import CapaDAOServicios.PedidoDAO;
import CapaDAOServicios.PedidoFueraTiempoDAO;
import CapaDAOServicios.PedidoPOSPMDAO;
import CapaDAOServicios.PedidoPixelDAO;
import CapaDAOServicios.TiempoPedidoDAO;
import CapaDAOServicios.TiendaDAO;
import ConexionServicios.ConexionBaseDatos;
import Modelo.Pedido;
import Modelo.PedidoFueraTiempo;
import Modelo.PedidoPixel;
import Modelo.TiempoPedido;
import Modelo.Tienda;
import Modelo.Correo;
import utilidades.ControladorEnvioCorreo;

public class ServicioReporteControlTiempo {
	
	private static ServicioReporteControlTiempo serviceInstance  = new ServicioReporteControlTiempo();
	public static int numMinutos = 20;			
		
	public static void main( String[] args )
	        
	{
		//En respuesta guardaremos el html que guardará todo lo que se desplegará en el correo.
		String respuesta = "";
		ArrayList<TiempoPedido> tiempos = TiempoPedidoDAO.retornarTiemposPedidos();
		respuesta = respuesta + "<table border='2'> <tr> RESUMEN ACTUAL DE TIEMPO DE LAS TIENDAS " + " </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>Tiempo Pedido Actual</strong></td>"
				+  "</tr>";
		for(int i = 0; i < tiempos.size(); i++)
		{
			TiempoPedido tiempoTemp = tiempos.get(i);
			respuesta = respuesta + "<tr><td>" +  tiempoTemp.getTienda() + "</td><td>" +  tiempoTemp.getMinutosPedido() + "</td></tr>";
		}
		
		respuesta = respuesta + "</table> <br/>";
		
		//Recuperar la lista de distribución para este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPCONTROLTIEMPOS");
		
		//Realizamos la recuperación de las tiendas para recorrerlas todos.
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendas();
		
		//Manejaremos un indicador qeu nos dirá si debemos de enviar el correo, dado que hay por lo menos un pedido
		boolean indEnvioCorreo = false;
		
		//Realizamos un recorrido de las tiendas para de cada uno se recuperan los pedidos.
		for(int i = 0; i < tiendas.size(); i++)
		{
			Tienda tiendaTemp = tiendas.get(i);
			//Recuperamos los pedidos fuera de tiempo para cada tienda
			ArrayList<PedidoFueraTiempo> pedFueraTiempo = PedidoFueraTiempoDAO.obtenerPedidoFueraTiempo(tiendaTemp.getIdTienda(), numMinutos);
			//Validamos que se tenga por lo menos un pedido por fuera del tiempo
			if(pedFueraTiempo.size() > 0)
			{
				respuesta = respuesta + "<table border='2'> <tr> PEDIDOS FUERA DE TIEMPO PARA  " + tiendaTemp.getNombreTienda() + " </tr>";
				respuesta = respuesta + "<tr>"
						+  "<td><strong>Id Pedido</strong></td>"
						+  "<td><strong>Id Pedido Tienda</strong></td>"
						+  "<td><strong>Tiempo Dado</strong></td>"
						+  "<td><strong>Tiempo Actual</strong></td>"
						+  "<td><strong>Tiempo Desviacion</strong></td>"
						+  "<td><strong>Domiciliario</strong></td>"
						+  "<td><strong>Estado Pedido</strong></td>"
						+  "<td><strong>Observacion</strong></td>"
						+  "</tr>";
				//Recorremos el arreglo para rellenar la información de la tabla con los pedidos fuera de tiempo
				for(int j = 0; j < pedFueraTiempo.size(); j++)
				{
					indEnvioCorreo = true;
					PedidoFueraTiempo pedTemp = pedFueraTiempo.get(j);
					respuesta = respuesta + "<tr><td>" +  pedTemp.getIdPedido() + "</td><td>" +  pedTemp.getTransact()  + "</td><td>" +  pedTemp.getTiempoDado() + "</td><td>" +pedTemp.getTiempoActual() + "</td><td>" + pedTemp.getPorcDesviacion() + "</td><td>" + pedTemp.getDomiciliario() + "</td><td>" + pedTemp.getEstadoPedido() + "</td><td>" + pedTemp.getObservacion()  + "</td></tr>";
				}
				respuesta = respuesta + "</table> <br/>";
			}
			
		}
		//Al final validamos si el indicador de envio de correo esta prendido
		if(indEnvioCorreo)
		{
			Date fecha = new Date();
			Correo correo = new Correo();
			correo.setAsunto("CONTROL DE TIEMPOS TIENDAS " + fecha.toString());
			correo.setContrasena("Pizzaamericana2017");
			correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
			correo.setMensaje("En este momento existen tiempo de pedidos por fuera de la promesa de servicio: \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}

	}
	
	public static void windowsService(String args[]) {
	    System.out.println("probando");  
		String cmd = "start";
	      if(args.length > 0) {
	         cmd = args[0];
	      }
		
	      if("start".equals(cmd)) {
	         serviceInstance.start();
	      }
	      else {
	         serviceInstance.stop();
	      }
	   }
	
	

	   /**
	    * Flag to know if this service
	    * instance has been stopped.
	    */
	   private boolean stopped = false;
	
	/**
	    * Start this service instance
	    */
	   public void start() {
		
	      stopped = false;
			
	      System.out.println("My Service Started "
	                         + new java.util.Date());
	      //Recuperamos los parámetros con los cuales se ejecutaron
	      
	      int segundosEje;
	      //En este punto deberemos de recuperar el número de minutos en el cual se ejecutará el proceso
		  //Traemos de una variable de configuración el valor de la marcacion domicilios.com
		  numMinutos = ParametrosDAO.retornarValorNumerico("REPCONTTIEMPOS");
		  segundosEje = numMinutos * 60 * 1000;
		  while(!stopped) {
	          String[] args ={"start"};
	    	  main(args);
	    	  try
				{
	    		  Thread.sleep(segundosEje);
	    		  System.out.println("volviendo a la ejecución despues de " + segundosEje + " milisegundos");
				}catch(Exception e)
				{
					System.out.println("Problemas en la pausa de 30 minutos");
				}
	    	  
	      }
			
	      System.out.println("My Service Finished "
	                          + new java.util.Date());
	   }
	   
	   public void stop() {
		      stopped = true;
		      synchronized(this) {
		         this.notify();
		      }
		   }
	
}

