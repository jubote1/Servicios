package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import ConexionSer.ConexionBaseDatos;



public class ParametrosDAO {
	
	public static int retornarValorNumerico(String variable)
	{
		int valor = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneral();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select valornumerico from parametros where valorparametro = '"+ variable +"'";
			System.out.println(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				
				try
				{
					valor = Integer.parseInt(rs.getString("valornumerico"));
				}catch(Exception e)
				{
				
					valor = 0;
				}
				
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
		return(valor);
	}
	
	public static int retornarValorNumericoLocal(String variable)
	{
		int valor = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select valornumerico from parametros where valorparametro = '"+ variable +"'";
			System.out.println(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				
				try
				{
					valor = Integer.parseInt(rs.getString("valornumerico"));
				}catch(Exception e)
				{
				
					valor = 0;
				}
				
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
		return(valor);
	}
	
	public static double retornarValorNumericoLocalDouble(String variable)
	{
		double valor = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select valornumericod from parametros where valorparametro = '"+ variable +"'";
			System.out.println(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				
				try
				{
					valor = Double.parseDouble(rs.getString("valornumericod"));
				}catch(Exception e)
				{
				
					valor = 0;
				}
				
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
		return(valor);
	}
	
	public static String retornarValorAlfanumerico(String variable)
	{
		String valor = "";
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneral();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select valortexto from parametros where valorparametro = '"+ variable +"'";
			System.out.println(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				
				try
				{
					valor = rs.getString("valortexto");
				}catch(Exception e)
				{
				
					valor = "";
				}
				
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e)
		{
			System.out.println("OJO ERROR" + e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				
			}
		}
		return(valor);
	}
	
	
	public static String retornarValorAlfanumericoLocal(String variable)
	{
		String valor = "";
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select valortexto from parametros where valorparametro = '"+ variable +"'";
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				
				try
				{
					valor = rs.getString("valortexto");
				}catch(Exception e)
				{
				
					valor = "";
				}
				
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e)
		{
			if(e instanceof NullPointerException)
			{
				valor = "ERROR";
			}
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				
			}
		}
		return(valor);
	}
	
	
	//Método creado para retornar el valor de variable desde sistema tienda
	public static String retornarValorAlfanumericoTienda(String hostBD, String variable)
	{
		String valor = "";
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(hostBD);
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select valortexto from parametros where valorparametro = '"+ variable +"'";
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				
				try
				{
					valor = rs.getString("valortexto");
				}catch(Exception e)
				{
				
					valor = "";
				}
				
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
		return(valor);
	}
	
	//Método creado para retornar el valor de variable desde sistema tienda
		public static int retornarValorNumericoTienda(String hostBD, String variable)
		{
			int valor = 0;
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(hostBD);
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select valornumerico from parametros where valorparametro = '"+ variable +"'";
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					
					try
					{
						valor = rs.getInt("valornumerico");
					}catch(Exception e)
					{
					
						valor = 0;
					}
					
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
			return(valor);
		}
	
	public static boolean EditarParametroTienda(String hostBD, String parametro, String valorAlfanumerico, int valorNumerico)
	{
		boolean respuesta;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(hostBD);
		try
		{
			Statement stm = con1.createStatement();
			String update = "update parametros set valortexto = '" + valorAlfanumerico + "' , valornumerico = " + valorNumerico + " where valorparametro = '" + parametro+"'" ; 
			stm.executeUpdate(update);
			//Ejecutamos la inserción de log
			String insercionLog = "insert into parametros_log (usuario, nuevovalor,variable) values ('"  + "AUTOMATICO" + "' , '" + valorNumerico+" " + valorAlfanumerico +"' ,'" + parametro + "')" ;
			stm.executeUpdate(insercionLog);
			stm.close();
			con1.close();
		}
		catch (Exception e){
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
			return(false);
		}
		return(true);
	}
	
}
