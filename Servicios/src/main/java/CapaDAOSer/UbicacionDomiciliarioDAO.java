package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ConexionSer.ConexionBaseDatos;
import capaModeloCC.LogEventoWompi;

public class UbicacionDomiciliarioDAO {

	public static void insertarUbicacionDomiciliario(String claveDomiciliario, int idTienda, float latitud, float longitud)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into ubicacion_domiciliario (clave_dom,idtienda,latitud,longitud) values ('" + claveDomiciliario + "' ," + idTienda + " , " + latitud + " , " + longitud   +")"; 
			logger.info(insert);
			stm.executeUpdate(insert);
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
	}
	
	
	public static void depurarUbicacionDomiciliario()
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String delete = "DELETE FROM ubicacion_domiciliario WHERE fecha < DATE_ADD(NOW(),  INTERVAL -60 DAY)"; 
			logger.info(delete);
			stm.executeUpdate(delete);
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
	}

}
