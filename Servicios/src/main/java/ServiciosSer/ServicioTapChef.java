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


public class ServicioTapChef {
	
	private static ServicioTapChef serviceInstance  = new ServicioTapChef();

	
	
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
		//obtenenemos la tienda donde estamos corriendo
		int idTienda = ObtenerTienda();
		//Necesitamos extraer el pedido del cual se está cambiando el estado
		//Por ejemplo <LOBBY order="49099">
		//Trabajaremos con StringTokenizer
		StringTokenizer tokens=new StringTokenizer(datos, "\"");
		String idPedidoStr = "";
		while(tokens.hasMoreTokens()){
            idPedidoStr=tokens.nextToken();
            idPedidoStr = tokens.nextToken();
            break;
        }
		int idPedido = Integer.parseInt(idPedidoStr);
		System.out.println(idPedidoStr);
		//Vamos a obtener el estado actual del pedido
		int idEstadoActual = capaDAOPOS.PedidoDAO.obtenerIdEstadoPedido(idPedido, false);
		//Obtenemos los posibles estados posteriores
		ArrayList<EstadoPosterior> estPosteriores = capaDAOPOS.EstadoPosteriorDAO.obtenerEstadosPos(idEstadoActual, false);
		//Verificamos si solo hay un estado posterior
		int idEstadoPosterior = 0;
		if(estPosteriores.size() == 1)
		{
			//Realizamos el cambio de estado del pedido
			EstadoPosterior estTemp = estPosteriores.get(0);
			capaControladorPOS.PedidoCtrl pedCtrl = new PedidoCtrl(false);
			pedCtrl.ActualizarEstadoPedidoTabChef(idPedido, idEstadoActual, estTemp.getIdEstadoPosterior(), idTienda, "COCINA");
		}
		//Posteriormente deberemos de avanzar el estado del pedido.
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
	      try
			{
	  		  Thread.sleep(60000);
			}catch(Exception e)
			{
				System.out.println("Durmiendo el proceso para dar un poco de gabela que inicie");
			}	  
	      
	    //Al arrancar el servicio verificaremos si la variable comanda electrónica esta o no habilitada
		String comandaElec = capaDAOPOS.ParametrosDAO.retornarValorAlfanumerico("COMANDAELECTRONICA", false);
		//validaremos que si está prendida la variable vamos a aperturar a escuchar por los puertos
		if(comandaElec.equals(new String("S")))
		{
			//LLenamos las variables de los estados
		    final long estCocina;
		  	//variable para estado en ruta domicilios
		  	final long estEnHorno;
		      while(!stopped) {
		    	  try
					{
		    		  	ServerSocket serverSocket = new ServerSocket(9100);
		    		  	Socket skCliente = serverSocket.accept(); // Crea objeto
			  			DataInputStream dis = new DataInputStream(skCliente.getInputStream());
			  			DataOutputStream dos = new DataOutputStream(skCliente.getOutputStream());
			  			BufferedReader br = new BufferedReader(new InputStreamReader(skCliente.getInputStream(), "UTF-8"));
			  			String line = null;
			  			StringBuilder sb = new StringBuilder();
			  			while ((line = br.readLine()) != null) {
			  				sb.append(line);
			  			}
			  			String datos = sb.toString();
			  			//Tomados los datos vamos a realizar la ejecución del main para cambiar de estado el pedido
			  			String[] args ={"start"};
				    	main(args, datos);
				    	skCliente.close();
					}catch(Exception e)
					{
						Date fecha = new Date();
						//System.out.println("Problemas intentando leer socket " + fecha.toString());
					}
		          
		    	  
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

