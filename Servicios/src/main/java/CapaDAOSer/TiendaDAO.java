package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.Tienda;
import capaDAOPOS.FormaPagoDAO;
import capaModeloPOS.Parametro;

public class TiendaDAO {
	

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
	
	//Esta informaci�n se extrae del contact center
	public static int ObtenerTipoPOSTienda(int idtienda)
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conTiendaLocal = conexion.obtenerConexionBDContact();
		String consulta = "select pos from tienda where idtienda = " + idtienda;
		int pos = 0;
		try
		{
			Statement stmTiendaLocal= conTiendaLocal.createStatement();
			ResultSet rsTiendaLocal = stmTiendaLocal.executeQuery(consulta);
			while(rsTiendaLocal.next())
			{
				pos = rsTiendaLocal.getInt("pos");
			}
			rsTiendaLocal.close();
			stmTiendaLocal.close();
			conTiendaLocal.close();
		}catch(Exception e)
		{
			System.out.println("Error en la consulta de la tienda " + e.toString() );
		}
		System.out.println("la tienda recuperada es " + idtienda);
		return(pos);
	}
	
	
	public static String obtenerNombreTienda()
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conTiendaLocal = conexion.obtenerConexionBDLocal();
		String consulta = "select nombretienda from tienda ";
		String tienda = "";
		try
		{
			Statement stmTiendaLocal= conTiendaLocal.createStatement();
			ResultSet rsTiendaLocal = stmTiendaLocal.executeQuery(consulta);
			while(rsTiendaLocal.next())
			{
				tienda = rsTiendaLocal.getString("nombretienda");
			}
			rsTiendaLocal.close();
			stmTiendaLocal.close();
			conTiendaLocal.close();
		}catch(Exception e)
		{
			System.out.println("Error en la consulta de la tienda " + e.toString() );
		}
		return(tienda);
	}
	
	public static String obtenerFechaAperturaTienda()
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conTiendaLocal = conexion.obtenerConexionBDLocal();
		String consulta = "select fecha_apertura from tienda ";
		String fecha = "";
		try
		{
			Statement stmTiendaLocal= conTiendaLocal.createStatement();
			ResultSet rsTiendaLocal = stmTiendaLocal.executeQuery(consulta);
			while(rsTiendaLocal.next())
			{
				fecha = rsTiendaLocal.getString("fecha_apertura");
			}
			rsTiendaLocal.close();
			stmTiendaLocal.close();
			conTiendaLocal.close();
		}catch(Exception e)
		{
			System.out.println("Error en la consulta de la fecha tienda " + e.toString() );
		}
		return(fecha);
	}
	
	
	//ESTOS M�TODOS APUNTAN A LA TIENDA EN EL SISTEMA CONTACT CENTER
	
	/**
	 * M�todo que se encarga de retornar todas las entidades Tiendas definidas en la base de datos
	 * @return Se retorna un ArrayList con todas las entidades Tiendas definidas en la base de datos.
	 */
		public static ArrayList<Tienda> obtenerTiendas()
		{
			ArrayList<Tienda> tiendas = new ArrayList<>();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContact();
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select * from tienda";
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					int idTienda = rs.getInt("idtienda");
					String nombre = rs.getString("nombre");
					String hostBD = rs.getString("hosbd");
					Tienda tien = new Tienda(idTienda, nombre, "","",0);
					tien.setHostBD(hostBD);
					tiendas.add(tien);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("falle consultando tiendas");
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle consultando tiendas");
				}
			}
			return(tiendas);
			
		}
		
		public static Tienda obtenerTienda(int idTienda)
		{
			Tienda tienda = new Tienda();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select * from tienda where idtienda = " + idTienda;
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					String nombre = rs.getString("nombre");
					String hostBD = rs.getString("hosbd");
					String urlTienda = rs.getString("url");
					tienda = new Tienda(idTienda, nombre, "","",0);
					tienda.setHostBD(hostBD);
					tienda.setUrl(urlTienda);
					
					
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("falle consultando tiendas");
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle consultando tiendas");
				}
			}
			return(tienda);
			
		}
		
		public static ArrayList<Tienda> obtenerTiendasLocal()
		{
			ArrayList<Tienda> tiendas = new ArrayList<>();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select * from tienda where funcional = 'S'";
				ResultSet rs = stm.executeQuery(consulta);
				double meta;
				while(rs.next()){
					int idTienda = rs.getInt("idtienda");
					String nombre = rs.getString("nombre");
					String hostBD = rs.getString("hosbd");
					meta = rs.getDouble("meta");
					Tienda tien = new Tienda(idTienda, nombre, "","",0);
					tien.setHostBD(hostBD);
					tien.setMeta(meta);
					tiendas.add(tien);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("falle consultando tiendas");
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle consultando tiendas");
				}
			}
			return(tiendas);
			
		}
		
		public static ArrayList<Tienda> obtenerTiendasLocalSinBodega()
		{
			ArrayList<Tienda> tiendas = new ArrayList<>();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select * from tienda where nombre !='BODEGA'";
				ResultSet rs = stm.executeQuery(consulta);
				double meta;
				while(rs.next()){
					int idTienda = rs.getInt("idtienda");
					String nombre = rs.getString("nombre");
					String hostBD = rs.getString("hosbd");
					meta = rs.getDouble("meta");
					Tienda tien = new Tienda(idTienda, nombre, "","",0);
					tien.setHostBD(hostBD);
					tien.setMeta(meta);
					tiendas.add(tien);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("falle consultando tiendas");
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle consultando tiendas");
				}
			}
			return(tiendas);
			
		}
		
		//M�todo creado para retornar el valor de la fecha de sistema en una tienda remota
		public static String retornarFechaTiendaRemota(String hostBD)
		{
			String valorFecha = "";
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(hostBD);
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select fecha_apertura from tienda";
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					valorFecha = rs.getString("fecha_apertura");
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e)
			{
				
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					
				}
			}
			return(valorFecha);
		}

		public static double obtenerTotalFormaPago( String fecha, String hostBD, boolean auditoria)
		{
			//Revismamos unos par�metros
			int idFormaPago = ParametrosDAO.retornarValorNumericoTienda(hostBD, "IDQRBANCOLOMBIA");
			System.out.println("idformapago " + idFormaPago);
		
			Logger logger = Logger.getLogger("log_file");
			double total = 0;
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1;
			if(hostBD.equals(""))
			{
				con1 = con.obtenerConexionBDLocal();
			}else
			{
				con1 = con.obtenerConexionBDTiendaRemota(hostBD);
			}
			 
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT SUM(b.valordisminuido) FROM pedido a, pedido_forma_pago b WHERE a.idpedidotienda = b.idpedidotienda AND b.idforma_pago = " + idFormaPago +" AND a.fechapedido = '" + fecha + "'";
				
				if(auditoria)
				{
					logger.info(consulta);
					System.out.println(consulta);
				}
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				logger.error(e.toString());
				System.out.println(e.toString());
				try
				{
					con1.close();
				}catch(Exception e1)
				{
				}
			}
			return(total);
			
		}
		
		public static double obtenerTotalQRRango( String fechaAnterior, String fechaPosterior, String hostBD, boolean auditoria)
		{
			//Revismamos unos par�metros
			int idFormaPago = ParametrosDAO.retornarValorNumericoTienda(hostBD, "IDQRBANCOLOMBIA");
			System.out.println("idformapago " + idFormaPago);
		
			Logger logger = Logger.getLogger("log_file");
			double total = 0;
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1;
			if(hostBD.equals(""))
			{
				con1 = con.obtenerConexionBDLocal();
			}else
			{
				con1 = con.obtenerConexionBDTiendaRemota(hostBD);
			}
			 
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT SUM(b.valordisminuido) FROM pedido a, pedido_forma_pago b WHERE a.idpedidotienda = b.idpedidotienda AND b.idforma_pago = " + idFormaPago +" AND a.fechapedido >= '" + fechaAnterior + "' AND a.fechapedido <= '" + fechaPosterior +"'";
				
				if(auditoria)
				{
					logger.info(consulta);
					System.out.println(consulta);
				}
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				logger.error(e.toString());
				System.out.println(e.toString());
				try
				{
					con1.close();
				}catch(Exception e1)
				{
				}
			}
			return(total);
			
		}
		
		public static double obtenerTotalFormaPagoEntreFechas( String fechaInicial, String fechaFinal, String hostBD, boolean auditoria)
		{
			//Revismamos unos par�metros
			int idFormaPago = ParametrosDAO.retornarValorNumericoTienda(hostBD, "IDQRBANCOLOMBIA");
			System.out.println("idformapago " + idFormaPago);
		
			Logger logger = Logger.getLogger("log_file");
			double total = 0;
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1;
			if(hostBD.equals(""))
			{
				con1 = con.obtenerConexionBDLocal();
			}else
			{
				con1 = con.obtenerConexionBDTiendaRemota(hostBD);
			}
			 
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT SUM(b.valordisminuido) FROM pedido a, pedido_forma_pago b WHERE a.idpedidotienda = b.idpedidotienda AND b.idforma_pago = " + idFormaPago +" AND a.fechapedido >= '" + fechaInicial + "' and a.fechapedido <= '" + fechaFinal+"'";
				
				if(auditoria)
				{
					logger.info(consulta);
					System.out.println(consulta);
				}
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				logger.error(e.toString());
				System.out.println(e.toString());
				try
				{
					con1.close();
				}catch(Exception e1)
				{
				}
			}
			return(total);
			
		}
		
		public static double obtenerTotalFormaPagoEntreFechasTarjetaPA( String fechaInicial, String fechaFinal, String hostBD, boolean auditoria)
		{
			//Revismamos unos par�metros
			int idFormaPago = ParametrosDAO.retornarValorNumericoTienda(hostBD, "IDTARJETAPA");
			System.out.println("idformapago " + idFormaPago);
		
			Logger logger = Logger.getLogger("log_file");
			double total = 0;
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1;
			if(hostBD.equals(""))
			{
				con1 = con.obtenerConexionBDLocal();
			}else
			{
				con1 = con.obtenerConexionBDTiendaRemota(hostBD);
			}
			 
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT SUM(b.valordisminuido) FROM pedido a, pedido_forma_pago b WHERE a.idpedidotienda = b.idpedidotienda AND b.idforma_pago = " + idFormaPago +" AND a.fechapedido >= '" + fechaInicial + "' and a.fechapedido <= '" + fechaFinal+"'";
				
				if(auditoria)
				{
					logger.info(consulta);
					System.out.println(consulta);
				}
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				logger.error(e.toString());
				System.out.println(e.toString());
				try
				{
					con1.close();
				}catch(Exception e1)
				{
				}
			}
			return(total);
			
		}
		
		public static ArrayList obtenerPedidosFormaPago( String fecha, String hostBD, boolean auditoria)
		{
			//Revismamos unos par�metros
			ArrayList respuesta = new ArrayList();
			int idFormaPago = ParametrosDAO.retornarValorNumericoTienda(hostBD, "IDQRBANCOLOMBIA");
			System.out.println("idformapago " + idFormaPago);
		
			Logger logger = Logger.getLogger("log_file");
            String[] fila=  new String[5];
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1;
			if(hostBD.equals(""))
			{
				con1 = con.obtenerConexionBDLocal();
			}else
			{
				con1 = con.obtenerConexionBDTiendaRemota(hostBD);
			}
			 
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT a.idpedidotienda,b.valordisminuido, concat(c.nombre , ' ' , c.apellido), c.telefono, a.fechainsercion  FROM pedido a, pedido_forma_pago b, cliente c WHERE a.idpedidotienda = b.idpedidotienda AND a.idcliente = c.idcliente  AND b.idforma_pago = " + idFormaPago +" AND a.fechapedido = '" + fecha + "'";
				
				if(auditoria)
				{
					logger.info(consulta);
					System.out.println(consulta);
				}
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					fila = new String[5];
					fila[0] = rs.getString(1);
					fila[1] = rs.getString(2);
					fila[2] = rs.getString(3);
					fila[3] = rs.getString(4);
					fila[4] = rs.getString(5);
					respuesta.add(fila);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				logger.error(e.toString());
				System.out.println(e.toString());
				try
				{
					con1.close();
				}catch(Exception e1)
				{
				}
			}
			return(respuesta);
			
		}
		
		
		/**
		 * Método que trae las operaciones de un tipo de forma de pago determinado
		 * @param fecha
		 * @param idFormaPago
		 * @param hostBD
		 * @param auditoria
		 * @return
		 */
		public static ArrayList obtenerPedidosFormaPagoPar( String fecha, int idFormaPago, String hostBD, boolean auditoria)
		{
			//Revismamos unos par�metros
			ArrayList respuesta = new ArrayList();
			Logger logger = Logger.getLogger("log_file");
            String[] fila=  new String[6];
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1;
			if(hostBD.equals(""))
			{
				con1 = con.obtenerConexionBDLocal();
			}else
			{
				con1 = con.obtenerConexionBDTiendaRemota(hostBD);
			}
			 
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT a.idpedidotienda,b.valordisminuido, concat(c.nombre , ' ' , c.apellido), c.telefono, a.fechainsercion, b.datafono  FROM pedido a, pedido_forma_pago b, cliente c WHERE a.idpedidotienda = b.idpedidotienda AND a.idcliente = c.idcliente  AND b.idforma_pago = " + idFormaPago +" AND a.fechapedido = '" + fecha + "'";
				
				if(auditoria)
				{
					logger.info(consulta);
					System.out.println(consulta);
				}
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					fila = new String[6];
					fila[0] = rs.getString(1);
					fila[1] = rs.getString(2);
					fila[2] = rs.getString(3);
					fila[3] = rs.getString(4);
					fila[4] = rs.getString(5);
					fila[5] = rs.getString(6);
					respuesta.add(fila);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				logger.error(e.toString());
				System.out.println(e.toString());
				try
				{
					con1.close();
				}catch(Exception e1)
				{
				}
			}
			return(respuesta);
			
		}
		
		public static int obtenerCantidadVentas( int idProducto, String fechaAnterior, String fechaPosterior, String hostBD, boolean auditoria)
		{
			Logger logger = Logger.getLogger("log_file");
			int cantidad = 0;
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1;
			if(hostBD.equals(""))
			{
				con1 = con.obtenerConexionBDLocal();
			}else
			{
				con1 = con.obtenerConexionBDTiendaRemota(hostBD);
			}
			 
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT COUNT(*) FROM pedido a, detalle_pedido b WHERE a.idpedidotienda = b.idpedidotienda AND b.idproducto = " + idProducto + " AND a.fechapedido >= '" + fechaAnterior + "' AND a.fechapedido <= '" + fechaPosterior + "'";
				
				if(auditoria)
				{
					logger.info(consulta);
					System.out.println(consulta);
				}
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					cantidad = rs.getInt(1);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				logger.error(e.toString());
				System.out.println(e.toString());
				try
				{
					con1.close();
				}catch(Exception e1)
				{
				}
			}
			return(cantidad);
			
		}
		
		public static String obtenerNombreProducto( int idProducto, String hostBD, boolean auditoria)
		{
			Logger logger = Logger.getLogger("log_file");
			String nombre = "";
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1;
			if(hostBD.equals(""))
			{
				con1 = con.obtenerConexionBDLocal();
			}else
			{
				con1 = con.obtenerConexionBDTiendaRemota(hostBD);
			}
			 
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT descripcion FROM producto a WHERE a.idproducto = " + idProducto;
				
				if(auditoria)
				{
					logger.info(consulta);
					System.out.println(consulta);
				}
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					nombre = rs.getString(1);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				logger.error(e.toString());
				System.out.println(e.toString());
				try
				{
					con1.close();
				}catch(Exception e1)
				{
				}
			}
			return(nombre);
			
		}
		
		public static ArrayList<Tienda> obtenerTiendasAdqBancolombia()
		{
			ArrayList<Tienda> tiendas = new ArrayList<>();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select * from tienda where adquirencia = 'B'";
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					int idTienda = rs.getInt("idtienda");
					String nombre = rs.getString("nombre");
					String hostBD = rs.getString("hosbd");
					Tienda tien = new Tienda(idTienda, nombre, "","",0);
					tien.setHostBD(hostBD);
					tiendas.add(tien);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("falle consultando tiendas");
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle consultando tiendas");
				}
			}
			return(tiendas);
			
		}
		
		
		public static ArrayList<Tienda> obtenerTiendasAdqDavivienda()
		{
			ArrayList<Tienda> tiendas = new ArrayList<>();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select * from tienda where adquirencia = 'D'";
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					int idTienda = rs.getInt("idtienda");
					String nombre = rs.getString("nombre");
					String hostBD = rs.getString("hosbd");
					Tienda tien = new Tienda(idTienda, nombre, "","",0);
					tien.setHostBD(hostBD);
					tiendas.add(tien);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("falle consultando tiendas");
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle consultando tiendas");
				}
			}
			return(tiendas);
			
		}
		
		public static int obtenerCantidadConexionesBD()
		{
			ArrayList<Tienda> tiendas = new ArrayList<>();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			int cantidadConexiones = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT COUNT(*) FROM information_schema.PROCESSLIST;";
				ResultSet rs = stm.executeQuery(consulta);
				double meta;
				while(rs.next()){
					cantidadConexiones = rs.getInt(1);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("falle consultando cantidad conexiones");
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle consultando cantidad conexiones");
				}
			}
			return(cantidadConexiones);
			
		}

}
