package Servicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import CapaDAOServicios.MarcacionAnulacionPedidoDAO;
import CapaDAOServicios.MarcacionCambioPedidoDAO;
import CapaDAOServicios.ParametrosDAO;
import CapaDAOServicios.PedidoDAO;
import CapaDAOServicios.PedidoPOSPMDAO;
import CapaDAOServicios.PedidoPixelDAO;
import CapaDAOServicios.TiendaDAO;
import ConexionServicios.ConexionBaseDatos;
import Modelo.MarcacionAnulacionPedido;
import Modelo.MarcacionCambioPedido;
import Modelo.Pedido;
import Modelo.PedidoPixel;

public class ServicioPedidosDomiciliosCOM {
	
	private static ServicioPedidosDomiciliosCOM serviceInstance  = new ServicioPedidosDomiciliosCOM();

	
	
	
		
	public static void main( String[] args )
	        
	{
	    //obtenenemos la tienda donde estamos corriendo
		int idTienda = TiendaDAO.ObtenerTienda();
		String tienda = TiendaDAO.obtenerNombreTienda();
		//Traemos de una variable de configuración el valor de la marcacion domicilios.com
		int idMarDomiciliosCOM = ParametrosDAO.retornarValorNumerico("MARCADORDOMICILIOSCOM");
	    //Obtenemos los pedidos de domicilios.com para la tienda  en cuestión en la semana en cuestión
		ArrayList<Pedido> pedidosDOMCOM = PedidoDAO.ConsultaDomiciosCOMSemana(idTienda, idMarDomiciliosCOM);
		//Teniendo los pedidos de la semana y la tienda la idea es comenzar a recorrerlos uno a uno y 
		//tener dos arreglos uno con posibles anulacioes y otro con posibles cambios de precio,
		//cuando los detectamos llenamos un par de tablas de control en el contact center
		
		//De la tienda en cuestion es necesario saber que POS está manejando para saber como lanzar la consulta
		int pos = TiendaDAO.ObtenerTipoPOSTienda(idTienda);
		System.out.println("POS RECUPERADO " + pos);
		for(Pedido pedTemp : pedidosDOMCOM)
		{
			double valorPedContact = pedTemp.getTotal_neto();
			int numPosHeader = pedTemp.getNumposheader();
			//Vamos a recuperar el valor del pedido con el fin de hacer la comparación
			double valorTotalTienda = 0;
			if(pos == 2)
			{
				valorTotalTienda = PedidoPixelDAO.obtenerTotalNetoPixel(numPosHeader);
			}else if(pos == 1)
			{
				valorTotalTienda = PedidoPOSPMDAO.obtenerTotalNetoPOSPM(numPosHeader);
			}
			if(valorTotalTienda == 0)
			{
				//Realiza la inserción en la tabla de marcacion_anulacion_pedido
				MarcacionAnulacionPedido marAnulacion = new MarcacionAnulacionPedido(0, idMarDomiciliosCOM,pedTemp.getIdpedido(), numPosHeader, pedTemp.getFechapedido(),valorPedContact);
				MarcacionAnulacionPedidoDAO.insertarMarcacionAnulacion(marAnulacion);
			}else if(valorTotalTienda != valorPedContact)
			{
				//Realiza la inserción en la tabla marcacion_cambio_pedido
				MarcacionCambioPedido marCambio = new MarcacionCambioPedido(0, idMarDomiciliosCOM,pedTemp.getIdpedido(), numPosHeader, pedTemp.getFechapedido(),valorPedContact, valorTotalTienda );
				MarcacionCambioPedidoDAO.insertarMarcacionCambio(marCambio);
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
			
	      while(!stopped) {
	          String[] args ={"start"};
	    	  main(args);
	    	  try
				{
					//Thread.sleep(1800000);
	    		  Thread.sleep(1200000);
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

