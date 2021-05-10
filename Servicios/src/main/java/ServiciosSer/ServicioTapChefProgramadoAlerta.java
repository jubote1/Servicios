package ServiciosSer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.InsumoAlertaDAO;
import CapaDAOSer.TiendaDAO;
import ConexionSer.ConexionBaseDatos;
import ModeloSer.Correo;
import ModeloSer.InsumoAlerta;
import capaControladorPOS.PedidoCtrl;
import capaModeloPOS.EstadoPosterior;
import utilidadesSer.ControladorEnvioCorreo;
import capaControladorPOS.PedidoCtrl;



public class ServicioTapChefProgramadoAlerta {
	
	private static ServicioTapChefProgramadoAlerta serviceInstance  = new ServicioTapChefProgramadoAlerta();

	
	
	public static int ObtenerTienda()
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conTiendaLocal = conexion.obtenerConexionBDLocal();
		String consulta = "select idtienda from tienda ";
		int idtienda = 0;
		try
		{
			Statement stmTiendaLocal= conTiendaLocal.createStatement();
			ResultSet rsTiendaLocal = stmTiendaLocal.executeQuery(consulta);
			while(rsTiendaLocal.next())
			{
				idtienda = rsTiendaLocal.getInt("idtienda");
			}
			rsTiendaLocal.close();
			stmTiendaLocal.close();
			conTiendaLocal.close();
		}catch(Exception e)
		{
			System.out.println("Error en la consulta de la tienda " + e.toString() );
		}
		System.out.println("la tienda recuperada es " + idtienda);
		return(idtienda);
	}
	
	
	public static void main( String[] args, String datos )
	        
	{
		
	}
	
	public static void windowsService(String args[]) {
	    System.out.println("Arrancando");  
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
	      
	    //Al arrancar el servicio verificaremos si la variable comanda electrónica esta o no habilitada
		String comandaElec = capaDAOPOS.ParametrosDAO.retornarValorAlfanumerico("COMANDAELECTRONICA", false);
		capaControladorPOS.PedidoCtrl pedCtrl = new PedidoCtrl(false);
	      while(!stopped) {
	    	  try
				{
	    		  //Thread.sleep(1800000);
	    		  Thread.sleep(120000);
	    		  Date fecha = new Date();
	    		  System.out.println("Fecha " + fecha);
	    		  pedCtrl.obtenerPedidosProgramadosParaCocina();
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

