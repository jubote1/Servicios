package Servicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import CapaDAOServicios.GeneralDAO;
import CapaDAOServicios.Impresion;
import CapaDAOServicios.ImprimirAdmDAO;
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
import Modelo.ImprimirAdm;
import utilidades.ControladorEnvioCorreo;

public class ServicioImpresion {
	
	private static ServicioImpresion serviceInstance  = new ServicioImpresion();
		
		
	public static void main( String[] args )
	        
	{
		
		ArrayList<ImprimirAdm> impresiones = ImprimirAdmDAO.pendientesImpresion();
		System.out.println(impresiones.size());
		//Realizamos un recorrido de las tiendas para de cada uno se recuperan los pedidos.
		for(int i = 0; i < impresiones.size(); i++)
		{
			ImprimirAdm impresionTemp = impresiones.get(i);
			String paraImpr = impresionTemp.getImprimir();
			//Luego de realizada la impresi�n
			Impresion.main(paraImpr);
			
			try
			{
				//Tendremos un retardo de 2 segundos para iniciar de nuevo a revisar la tabla
				Thread.sleep(500);
			}catch(Exception e)
			{
				
			}
			//Luego eliminar el registro en la pantalla
			ImprimirAdmDAO.borrarImpresion(impresionTemp.getIdImpresion(), false);
		}
		

	}
	
	public static void windowsService(String args[]) {
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
	      //Recuperamos los par�metros con los cuales se ejecutaron
	      
	      int segundosEje;
	      //En este punto deberemos de recuperar el n�mero de minutos en el cual se ejecutar� el proceso
		  //Traemos de una variable de configuraci�n el valor de la marcacion domicilios.com
		  while(!stopped) {
	          String[] args ={"start"};
	    	  main(args);
	    	  try
				{
	    		  Thread.sleep(2000);
	    		}catch(Exception e)
				{
					System.out.println("Problemas en la pausa en la impresi�n");
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

