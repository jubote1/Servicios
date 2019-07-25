package CapaDAOServicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionServicios.ConexionBaseDatos;


public class GeneralDAO {
	
	public static ArrayList obtenerCorreosParametro(String parametro)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		ArrayList correos = new ArrayList();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select correo from parametros_correo where valorparametro = '" +parametro+"'";
			ResultSet rs = stm.executeQuery(consulta);
			
			while(rs.next()){
				String correo = rs.getString("correo");
				correos.add(correo);
				
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println(e.toString() + e.getMessage() +  e.getStackTrace());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(correos);
		
	}
	
	public static ArrayList obtenerCorreosParametroTienda(String parametro)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDLocal();
		ArrayList correos = new ArrayList();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select correo from parametros_correo where valorparametro = '" +parametro+"'";
			ResultSet rs = stm.executeQuery(consulta);
			
			while(rs.next()){
				String correo = rs.getString("correo");
				correos.add(correo);
				
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println(e.toString() + e.getMessage() +  e.getStackTrace());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(correos);
		
	}

}
