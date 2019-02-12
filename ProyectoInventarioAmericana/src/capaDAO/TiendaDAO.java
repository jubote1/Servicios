package capaDAO;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import capaModelo.Usuario;
import conexion.ConexionBaseDatos;
import capaModelo.Tienda;
import org.apache.log4j.Logger;
/**
 * Clase que se encarga de implementar todo lo relacionado con la base de datos de la entidad tienda.
 * @author JuanDavid
 *
 */
public class TiendaDAO {
	
/**
 * Método que se encarga de retornar todas las entidades Tiendas definidas en la base de datos
 * @return Se retorna un ArrayList con todas las entidades Tiendas definidas en la base de datos.
 */
	public static ArrayList<Tienda> obtenerTiendas()
	{
		Logger logger = Logger.getLogger("log_file");
		ArrayList<Tienda> tiendas = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from tienda";
			
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				int idTienda = rs.getInt("idtienda");
				String nombre = rs.getString("nombre");
				Tienda tien = new Tienda(idTienda, nombre, "");
				tiendas.add(tien);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			logger.info(e.toString());
			System.out.println("falle consultando tiendas");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				logger.info(e1.toString());
				System.out.println("falle consultando tiendas");
			}
		}
		return(tiendas);
		
	}
	
	/**
	 * Método que se encarga de la consulta de un idtienda con base en nombre de la tienda recibido como parámetro.
	 * @param nombreTienda Se recibe como parámetro un valor String con el nombre de la tienda.
	 * @return Se retorna el idtienda asociado al nombre de la tienda recibido como parámetro.
	 */
	public static int obteneridTienda(String nombreTienda)
	{
		Logger logger = Logger.getLogger("log_file");
		int idTienda=0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select idtienda from tienda where nombre = '"+nombreTienda + "'";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				idTienda = rs.getInt("idtienda");
				break;
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			logger.error(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(idTienda);
	}
	
	
	
	
	/**
	 * Método que retorna un objeto de la clase tienda con la información de la URL y del dsn asociado a la tienda
	 * enviada como parámetro.
	 * @param idtienda Se recibe como parámetro el idtienda con base en el cual se realizará la consulta
	 * @return Se retorna variable String con el valor del URL Servicio de la tienda.
	 */
	public static Tienda obtenerUrlTienda(int idtienda)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		Tienda tienda = new Tienda();
		
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select url, dsn from tienda where idtienda = " + idtienda; 
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				tienda.setUrl(rs.getString("url"));
				tienda.setDsnTienda(rs.getString("dsn"));
				tienda.setIdTienda(idtienda);
				break;
			}
			stm.close();
			con1.close();
			
		}
		catch (Exception e){
			logger.error(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
			
		}
		return(tienda);
	}

	
	public static String obtenerNombreTienda(int idtienda)
	{
		Logger logger = Logger.getLogger("log_file");
		String nombreTienda="";
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select nombre from tienda where idtienda = '"+ idtienda + "'";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				nombreTienda = rs.getString("nombre");
				break;
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			logger.error(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(nombreTienda);
	}
	
}
