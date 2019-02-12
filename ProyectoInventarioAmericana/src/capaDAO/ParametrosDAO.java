package capaDAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import conexion.ConexionBaseDatos;

public class ParametrosDAO {
	
	public static int obtenerParametroNumero(String variable)
	{
		Logger logger = Logger.getLogger("log_file");
		int valorNumerico=0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select valornumerico from parametros where valorparametro = '"+ variable + "'";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				valorNumerico = rs.getInt("valornumerico");
				break;
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			logger.error(e.toString() + " con variable " + variable);
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(valorNumerico);
	}

	public static String obtenerParametroTexto(String variable)
	{
		Logger logger = Logger.getLogger("log_file");
		String valorTexto="";
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select valortexto from parametros where valorparametro = '"+ variable + "'";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				valorTexto = rs.getString("valortexto");
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
		return(valorTexto);
	}
	
}
