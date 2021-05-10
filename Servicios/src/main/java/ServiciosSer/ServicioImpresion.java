package ServiciosSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ImprimirAdmDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.PedidoFueraTiempoDAO;
import CapaDAOSer.PedidoPOSPMDAO;
import CapaDAOSer.PedidoPixelDAO;
import CapaDAOSer.TiempoPedidoDAO;
import CapaDAOSer.TiendaDAO;
import ConexionSer.ConexionBaseDatos;
import ModeloSer.Correo;
import ModeloSer.ImprimirAdm;
import ModeloSer.Pedido;
import ModeloSer.PedidoFueraTiempo;
import ModeloSer.PedidoPixel;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import interfazGraficaPOS.Impresion;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioImpresion {
	
	private static ServicioImpresion serviceInstance  = new ServicioImpresion();
		
		
	public static void main( String[] args )
	        
	{
		
		ArrayList<ImprimirAdm> impresiones = ImprimirAdmDAO.pendientesImpresion();
		
		//Realizamos un recorrido de las tiendas para de cada uno se recuperan los pedidos.
		for(int i = 0; i < impresiones.size(); i++)
		{
			ImprimirAdm impresionTemp = impresiones.get(i);
			String paraImpr = impresionTemp.getImprimir();
			//Luego de realizada la impresión
			Impresion.main(paraImpr, impresionTemp.getImpresora());
			
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
	      try
			{
	  		  Thread.sleep(30000);
			}catch(Exception e)
			{
				System.out.println("Durmiendo el proceso para dar un poco de gabela que inicie");
			}	
	      
	      int segundosEje;
	      //En este punto deberemos de recuperar el número de minutos en el cual se ejecutará el proceso
		  //Traemos de una variable de configuración el valor de la marcacion domicilios.com
		  while(!stopped) {
	          String[] args ={"start"};
	    	  main(args);
	    	  try
				{
	    		  Thread.sleep(2000);
	    		}catch(Exception e)
				{
					System.out.println("Problemas en la pausa en la impresión");
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

