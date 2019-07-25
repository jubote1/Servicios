package Servicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import CapaDAOServicios.ParametrosDAO;
import CapaDAOServicios.PedidoDAO;
import CapaDAOServicios.PedidoFueraTiempoDAO;
import CapaDAOServicios.PedidoPOSPMDAO;
import CapaDAOServicios.PedidoPixelDAO;
import CapaDAOServicios.TiendaDAO;
import ConexionServicios.ConexionBaseDatos;
import Modelo.Pedido;
import Modelo.PedidoFueraTiempo;
import Modelo.PedidoPixel;

public class ServicioControlPedidos {
	
	private static ServicioControlPedidos serviceInstance  = new ServicioControlPedidos();
	public static boolean controlTiempo = true;
	public static double minutosDesviacion = 5;
	public static double porcenDesviacionTiempo;
	public static double minutosSinRuta = 5;
	
	
		
	public static void main( String[] args )
	        
	{
	    //obtenenemos la tienda donde estamos corriendo
		int idTienda = TiendaDAO.ObtenerTienda();
		String tienda = TiendaDAO.obtenerNombreTienda();
		//Debemos de validar que tipo de POS tiene la tienda para verificar a donde se debe lanzar la consulta del POS
		//De la tienda en cuestion es necesario saber que POS está manejando para saber como lanzar la consulta
		int pos = TiendaDAO.ObtenerTipoPOSTienda(idTienda);
		ArrayList<PedidoPixel> pedidosPOS = new ArrayList();
		if(pos == 2)
		{
			//Obtenemos los pedidos del sistema POS en el cual se está ejecutando el servicio
			pedidosPOS = PedidoPixelDAO.obtenerPedidosPOS();
		}else if(pos == 1)
		{
			//Obtenemos los pedidos del sistema POS en el cual se está ejecutando el servicio
			pedidosPOS = PedidoPOSPMDAO.obtenerPedidosPOSPM();
						
		}
		//Obtenemos los pedidos del sistema contact center para la tienda en cuestión
		ArrayList<Pedido> pedidosContact = PedidoDAO.ConsultaIntegradaPedidos(idTienda);
		//Tendremos un objeto de la clase modelo  para almacenar los pedidos desfasados de tiempo
		
		PedidoPixel pedidoTiendaTemp = new PedidoPixel();
		Pedido pedidoContactTemp = new Pedido();
		//Variable donde almacenaremos el tiempo dado al pedido
		double tiempoPedido;
		double tiempoActualPedido;
		//Variable para almacenar la diferencia de tiempos entre el tiempo actual y el tiempo dado
		double diferenciaTiempos;
		//Variable que almacenará el porcentaje de desviación de tiempos
		double porcDesvTiempo;
		//Variable temporal para almacenar los pedidos fuera de tiempo
		PedidoFueraTiempo pedFueraTiempo = new PedidoFueraTiempo(0,0,0,0,0,0,"","","");
		
		//Realizamos el recorrido de todos los pedidos de tienda con el fin de verificar los tiempos
		for(int i = 0; i < pedidosPOS.size(); i++)
		{
			pedidoTiendaTemp = pedidosPOS.get(i);
			for(int j = 0; j < pedidosContact.size(); j++)
			{
				pedidoContactTemp = pedidosContact.get(j);
				if(pedidoContactTemp.getNumposheader() == pedidoTiendaTemp.getTransact())
				{
					//traemos el tiempo dado al pedido
					tiempoPedido = pedidoContactTemp.getTiempopedido();
					//Validamos si el tiempo actual del pedido es mayor al dado al pedido
					tiempoActualPedido = pedidoTiendaTemp.getTiempoPedido();
					//Esta variable booleana me ayudara a controlar si ya fue reportado el pedido para no reportarlo doble
					boolean reportado = false;
					if(tiempoActualPedido > tiempoPedido)
					{
						//En caso de cumplirse está confición validaremos la diferencia entre el tiempo dado y el tiempo actual
						diferenciaTiempos = tiempoActualPedido - tiempoPedido;
						//EN este punto hacemos la diferenciación de si el control es por tiempo o por porcentaje
						if(!controlTiempo)
						{
							porcDesvTiempo =  (diferenciaTiempos / tiempoPedido) * 100;
							//Validamos si la desviación es mayor al valor definido como parámetro
							if(porcDesvTiempo > porcenDesviacionTiempo)
							{
								//Debemos de validar si el pedido ya había sido ingresado, o si por el contrario sería una actualización
								boolean existe = PedidoFueraTiempoDAO.existePedido(pedidoContactTemp.getIdpedido());
								if(!existe)
								{
									pedFueraTiempo = new PedidoFueraTiempo(pedidoContactTemp.getIdpedido(),pedidoContactTemp.getIdtienda(), pedidoTiendaTemp.getTransact(), pedidoContactTemp.getTiempopedido(),pedidoTiendaTemp.getTiempoPedido(), porcDesvTiempo, pedidoTiendaTemp.getDomiciliario(), pedidoTiendaTemp.getEstadoPedido(), "PEDIDO EXCEDE ESTANDAR DE TIEMPO");
									PedidoFueraTiempoDAO.insertarPedidoFueraTiempo(pedFueraTiempo);
									//Aqui se deberá almacenar la información
								}else//Deberemos de actualizar la infromación
								{
									PedidoFueraTiempoDAO.ActualizarPedidoFueraTiempo(pedidoContactTemp.getIdpedido(), porcDesvTiempo,pedidoTiendaTemp.getDomiciliario(), pedidoTiendaTemp.getEstadoPedido(), tiempoActualPedido, "PEDIDO EXCEDE ESTANDAR DE TIEMPO");
								}
								reportado = true;
							}
						}else if(controlTiempo)
						{
							if(diferenciaTiempos > minutosDesviacion)
							{
								//Debemos de validar si el pedido ya había sido ingresado, o si por el contrario sería una actualización
								boolean existe = PedidoFueraTiempoDAO.existePedido(pedidoContactTemp.getIdpedido());
								if(!existe)
								{
									pedFueraTiempo = new PedidoFueraTiempo(pedidoContactTemp.getIdpedido(), pedidoContactTemp.getIdtienda(), pedidoTiendaTemp.getTransact(), pedidoContactTemp.getTiempopedido(),pedidoTiendaTemp.getTiempoPedido(), diferenciaTiempos, pedidoTiendaTemp.getDomiciliario(), pedidoTiendaTemp.getEstadoPedido(),"PEDIDO EXCEDE ESTANDAR DE TIEMPO");
									PedidoFueraTiempoDAO.insertarPedidoFueraTiempo(pedFueraTiempo);
									//Aqui se deberá almacenar la información
								}else//Deberemos de actualizar la infromación
								{
									PedidoFueraTiempoDAO.ActualizarPedidoFueraTiempo(pedidoContactTemp.getIdpedido(), diferenciaTiempos, pedidoTiendaTemp.getDomiciliario(), pedidoTiendaTemp.getEstadoPedido(),tiempoActualPedido, "PEDIDO EXCEDE ESTANDAR DE TIEMPO");
								}
								reportado = true;
							}
						}
						//Validamos si no ha sido reportado
						if(!reportado)
						{
							//Colocamos validación de la otra condición de los tiempos
							if(((diferenciaTiempos < 0)&&(Math.abs(diferenciaTiempos)< minutosSinRuta)&&(pedidoTiendaTemp.getEstadoPedido().equals(new String("Esperando"))))||((diferenciaTiempos > 0)&&(pedidoTiendaTemp.getEstadoPedido().equals(new String("Esperando")))))
							{
								//Debemos de validar si el pedido ya había sido ingresado, o si por el contrario sería una actualización
								boolean existe = PedidoFueraTiempoDAO.existePedido(pedidoContactTemp.getIdpedido());
								if(!existe)
								{
									pedFueraTiempo = new PedidoFueraTiempo(pedidoContactTemp.getIdpedido(), pedidoContactTemp.getIdtienda(), pedidoTiendaTemp.getTransact(), pedidoContactTemp.getTiempopedido(),pedidoTiendaTemp.getTiempoPedido(), diferenciaTiempos, pedidoTiendaTemp.getDomiciliario(), pedidoTiendaTemp.getEstadoPedido(),"PEDIDO MUY RETARDADO PARA SALIR DE TIENDA");
									PedidoFueraTiempoDAO.insertarPedidoFueraTiempo(pedFueraTiempo);
									//Aqui se deberá almacenar la información
								}else//Deberemos de actualizar la infromación
								{
									PedidoFueraTiempoDAO.ActualizarPedidoFueraTiempo(pedidoContactTemp.getIdpedido(), diferenciaTiempos, pedidoTiendaTemp.getDomiciliario(), pedidoTiendaTemp.getEstadoPedido(),tiempoActualPedido,"PEDIDO MUY RETARDADO PARA SALIR DE TIENDA");
								}
								
							}
						}
					}
					break;
				}
			}
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
	      int numMinutos;
	      int segundosEje;
	      //En este punto deberemos de recuperar el número de minutos en el cual se ejecutará el proceso
		  //Traemos de una variable de configuración el valor de la marcacion domicilios.com
		  numMinutos = ParametrosDAO.retornarValorNumerico("REPCONTTIEMPOSINSUMO");
		  segundosEje = numMinutos * 60 * 1000;
		  //Recuperamos el valor desviación de los tiempos para saber si el tiempo tiene desviación en el tiempo
		  //inicialmente dado al cliente.
		  porcenDesviacionTiempo = ParametrosDAO.retornarValorNumerico("DESVPORCENTAJETIEMPO");
		  minutosDesviacion =  ParametrosDAO.retornarValorNumerico("MINUTOSDESVIACION");
		  minutosSinRuta =  ParametrosDAO.retornarValorNumerico("MINUTOSSINRUTA");
		  String strControlTiempo = ParametrosDAO.retornarValorAlfanumerico("DESVTIEMPOPORCEN");
		  controlTiempo = false;
		  if(strControlTiempo.equals(new String("S")))
		  {
			  controlTiempo = true;
		  }else
		  {
			  controlTiempo = false;
		  }
	      while(!stopped) {
	          String[] args ={"start"};
	    	  main(args);
	    	  try
				{
	    		  Thread.sleep(segundosEje);
	    		  System.out.println("volviendo a dar vuelta");
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

