package Servicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ConexionServicios.ConexionBaseDatos;


public class ServicioInventariosBKCHILI {
	
	private static ServicioInventarios serviceInstance  = new ServicioInventarios();

	
	
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
	
	//Método para obtener la homologación de los insumo con las diferentes tiendas
	public static int obtenerIdInsumoIntero(int idtienda, int idinsumotienda)
	{
		int idInsumoInterno=0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventario();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select idinsumo from insumo_homologacion_tienda where idtienda = "+ idtienda + " and insumotienda =  " + idinsumotienda;
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				idInsumoInterno = rs.getInt("idinsumo");
				break;
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println("ERROR OBTENIENDO HOMOLOGACION " + e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(idInsumoInterno);
	}
	
	public static void main( String[] args )
	        
	{
	    //obtenenemos la tienda donde estamos corriendo
		int idtienda = ObtenerTienda();
		
        
			ConexionBaseDatos conexion = new ConexionBaseDatos();
			Connection conTiendaPixel = conexion.obtenerConexionBDTienda("");
			String consulta = "SELECT a.INVENNUM,a.DESCRIPT,c.UNITS,a.UNITDES " + 
								"FROM dba.inventory a,dba.REPORTCAT b,dba.StockLevels c " + 
								"WHERE b.REPORTNO = a.REPORTNO " +
								"AND a.INVENNUM = c.INVENNUM " +
								"AND a.ISACTIVE = 1 " + 
								"ORDER BY b.DESCRIPT ASC ,a.DESCRIPT ASC ";
			Statement stmTiendaPixel;
			ResultSet rsTiendaPixel;
			Connection conInventario = conexion.obtenerConexionBDInventario();
			Statement stmInventario;
			try
			{
				stmTiendaPixel = conTiendaPixel.createStatement();
				rsTiendaPixel = stmTiendaPixel.executeQuery(consulta);
				stmInventario = conInventario.createStatement();
				int banderaInventario = 1;
				int idinsumotienda;
				int idinsumointerno;
				double cantidad;
				Date fechaTemporal = new Date();
				DateFormat formatoFinal = new SimpleDateFormat("yyyy-MM-dd");
				String fecha="";
				try
				{
					fecha = formatoFinal.format(fechaTemporal);
					
				}catch(Exception e){
					System.out.println("Problema transformando la fecha actual " + e.toString());
				}
				while(rsTiendaPixel.next())
				{
					if(banderaInventario == 1)
					{
						String delete = "delete from insumo_tienda_tmp where idtienda = " + idtienda;
						stmInventario.executeUpdate(delete);
					}
					banderaInventario++;
					idinsumotienda = rsTiendaPixel.getInt("INVENNUM");
					idinsumointerno = obtenerIdInsumoIntero(idtienda, idinsumotienda);
					//Control para solo insertar los productos que tengan homologación
					if(idinsumointerno > 0)
					{
						cantidad = rsTiendaPixel.getDouble("UNITS");
						String insert = "insert into insumo_tienda_tmp (idinsumo, idtienda, cantidad, fecha) values ("+ idinsumointerno + " , " + idtienda + " , " + cantidad + " , '" + fecha + "')" ;
						stmInventario.executeUpdate(insert);
					}
				}
				// Al pasar este punto y no se ha salido es porque no se ha disparado excepción por lo tanto aqui podemos realizar el borrado de la tabla oficial y
				// y pasar la información del  temporal
				String deleteFinal = "delete from insumo_tienda where idtienda = " + idtienda;
				stmInventario.executeUpdate(deleteFinal);
				String insertFinal = "insert into insumo_tienda (select * from insumo_tienda_tmp where idtienda = " + idtienda + ")";
				stmInventario.executeUpdate(insertFinal);
				stmInventario.close();
				conInventario.close();
				rsTiendaPixel.close();
				stmTiendaPixel.close();
				conTiendaPixel.close();
				
			}catch(Exception e)
			{
				System.out.println("Error en la conexión al Sistema POS tienda " + e.toString());
				try
				{
					conInventario.close();
				}catch(Exception ex)
				{
					
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

