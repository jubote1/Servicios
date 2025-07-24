package ServiciosSer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
import capaControladorPOS.FacturaElectronicaCtrl;
import capaControladorPOS.PedidoCtrl;
import capaDAOPOS.ColaFacturaElectronicaDAO;
import capaModeloPOS.ColaFacturaElectronica;
import interfazGraficaPOS.Impresion;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioMonitoreoFE {
	
	private static ServicioMonitoreoFE serviceInstance  = new ServicioMonitoreoFE();
		
		
	public static void main( String[] args )      
	{
		
		try {
			ArrayList<ColaFacturaElectronica> facturas = ColaFacturaElectronicaDAO.pendientesGeneracion(false);
			String tokenApi = "";
			//La idea es en este punto validar si existen un número mayor a 4 facturas para generar
			if(facturas.size() >= 4)
			{
				//Hacemos el reinicio del servicio
				Runtime runtime = Runtime.getRuntime();
				try
				{
					Process process = runtime.exec("powershell.exe  C:\\Servicio\\reinicioservicio.ps1");
					
					process.getOutputStream().close();
					String line;
					BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
					while ((line = stdout.readLine()) != null){
						System.out.println(line);
					}
		 
					TimeUnit.SECONDS.sleep(2);
				}
				catch(IOException ex){
					System.err.println("Error");
					System.exit(-1);
				}
			}
			//Despues de cada ejecución tiene un descanso de 10 minutos
			try
			{
				Thread.sleep(600000);
			}catch(Exception e)
			{
				
			}
		}catch(Exception e)
		{
			//Se presenta excepción y no debe haber ejecución de los servicios
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

