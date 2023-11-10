package CapaDAOSer;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.mysql.cj.jdbc.result.ResultSetMetaData;

import ConexionSer.ConexionBaseDatos;
import capaModeloPOS.DatafonoCierre;

/**
 * Clase que se encarga de implementar todo lo relacionado con la base de datos de la entidad impuestos por producto
 * @author JuanDavid
 *
 */
public class DatafonoCierreDAO {
	


	public static boolean insertarDatafonoCierre(DatafonoCierre datIns, int idTienda, String fecha, boolean auditoria)
	{
		boolean respuesta = false;
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDDatamartLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into datafono_cierre (nombre, terminal, valor_calculado, valor_ingresado, observacion, idtienda, fecha) values ('" + datIns.getNombreDatafono() + "' , '" + datIns.getTerminal() + "' , " + datIns.getValorCalculado() +" , " + datIns.getValorIngresado() + " , '" + datIns.getObservacion() + "' , " + idTienda + " , '" + fecha + "')"; 
			if(auditoria)
			{
				logger.info(insert);
			}
			stm.executeUpdate(insert);
			respuesta = true;
			stm.close();
			con1.close();
		}
		catch (Exception e){
			respuesta = false;
			logger.error(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(respuesta);
	}
	
	
}
