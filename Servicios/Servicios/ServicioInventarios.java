package Servicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import CapaDAOServicios.GeneralDAO;
import CapaDAOServicios.InsumoAlertaDAO;
import CapaDAOServicios.TiendaDAO;
import ConexionServicios.ConexionBaseDatos;
import Modelo.Correo;
import Modelo.InsumoAlerta;
import utilidades.ControladorEnvioCorreo;


public class ServicioInventarios {
	
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
		String nombreTienda = TiendaDAO.obtenerNombreTienda();
        
			ConexionBaseDatos conexion = new ConexionBaseDatos();
			Connection conTiendaPixel = conexion.obtenerConexionBDLocal();
			String consulta = "SELECT a.iditem , a.nombre_item, a.cantidad, a.unidad_medida " + 
								"FROM item_inventario a  " + 
								"ORDER BY a.iditem ";
			Statement stmTiendaPixel;
			ResultSet rsTiendaPixel;
			Connection conInventario = conexion.obtenerConexionBDInventario();
			Statement stmInventario;
			ArrayList<InsumoAlerta> insumosAlertas = InsumoAlertaDAO.retornarInsumosAlerta();
			ArrayList<InsumoAlerta> insumosNuevosAlertar = new ArrayList();
			//Variables para enviar correo con los items que tienen menos de inventario
			String cuerpoCorreo = "";
			boolean enviarCorreo = false;
			cuerpoCorreo = cuerpoCorreo + "<table border='2'> <tr> ALERTAS POR INVENTARIOS "+ nombreTienda +"</tr>";
			cuerpoCorreo = cuerpoCorreo + "<tr>"
					+  "<td><strong>Item Inventario</strong></td>"
					+  "<td><strong>Cantidad Actual</strong></td>"
					+  "</tr>";
			String nombreInsumo = "";
			int banderaInventario = 1;
			int idinsumotienda;
			int idinsumointerno = 0;
			double cantidad;
			String fechaApertura  = TiendaDAO.obtenerFechaAperturaTienda();
			Date fechaTemporal = new Date();
			try
			{
				stmTiendaPixel = conTiendaPixel.createStatement();
				rsTiendaPixel = stmTiendaPixel.executeQuery(consulta);
				stmInventario = conInventario.createStatement();
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
					idinsumotienda = rsTiendaPixel.getInt("iditem");
					nombreInsumo = rsTiendaPixel.getString("nombre_item");
					idinsumointerno = obtenerIdInsumoIntero(idtienda, idinsumotienda);
					//Control para solo insertar los productos que tengan homologación
					if(idinsumointerno > 0)
					{
						cantidad = rsTiendaPixel.getDouble("cantidad");
						String insert = "insert into insumo_tienda_tmp (idinsumo, idtienda, cantidad, fecha) values ("+ idinsumointerno + " , " + idtienda + " , " + cantidad + " , '" + fecha + "')" ;
						stmInventario.executeUpdate(insert);
						//En este punto validamos si se tiene un insumo que tenga el valor menor al tope
						for(int  i = 0; i < insumosAlertas.size(); i++)
						{
							InsumoAlerta insTemp = insumosAlertas.get(i);
							if(insTemp.getIdInsumo() == idinsumointerno)
							{
								//Si la cantidad retirada de la tienda es menor al tope definido
								if(cantidad < insTemp.getCantidad())
								{
									// Se Acumula el insumo en la conformación de la tabla
									cuerpoCorreo = cuerpoCorreo + "<tr><td>"+nombreInsumo +"</td><td>" +  cantidad + "</td></tr>";
									//Se valida si el insumo no ha sido reportado
									boolean insumoReportado = InsumoAlertaDAO.insumoAlertaReportado(idinsumointerno, idtienda, fechaApertura);
									//Se prenderá el indicador para envío de correo y se adicionará en la tabla de reportados
									if(!insumoReportado)
									{
										InsumoAlertaDAO.insertarInsumoAlerta(idinsumointerno, idtienda, fechaApertura);
										enviarCorreo = true;
									}
									break;
								}
							}
						}
					}
				}
				// Al pasar este punto y no se ha salido es porque no se ha disparado excepción por lo tanto aqui podemos realizar el borrado de la tabla oficial y
				// y pasar la información del  temporal
				String deleteFinal = "delete from insumo_tienda where idtienda = " + idtienda;
				stmInventario.executeUpdate(deleteFinal);
				String insertFinal = "insert into insumo_tienda (select * from insumo_tienda_tmp where idtienda = " + idtienda + ")";
				stmInventario.executeUpdate(insertFinal);
				//En este punto haremos la validación de si hay que enviar el correo
				if(enviarCorreo)
				{
						Date fechaHoraExacta = new Date();
						cuerpoCorreo = cuerpoCorreo +  "</table> <br/>";
						Correo correo = new Correo();
						correo.setAsunto("ALERTA INSUMO INVENTARIO " + nombreTienda + " " + fechaHoraExacta.toString() );
						correo.setContrasena("Pizzaamericana2017");
						ArrayList correos = GeneralDAO.obtenerCorreosParametro("ALERTAINVENTARIO");
						correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
						correo.setMensaje("A continuación el reporte de insumos de inventario que están por debajo de los parámetros establecidos: \n" + cuerpoCorreo);
						ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
						contro.enviarCorreoHTML();
				}
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

