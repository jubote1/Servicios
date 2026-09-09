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
	
	
	//M�todo creado para retornar el valor de variable desde sistema tienda
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
	
	//M�todo creado para retornar el valor de variable desde sistema tienda
		public static String retornarValorAlfanumericoTiendaLocal(String variable)
		{
			String valor = "";
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaLocal();
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
	
	//M�todo creado para retornar el valor de variable desde sistema tienda
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
			//Ejecutamos la inserci�n de log
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
	
	/**
	 * Método que se encarga de la actualización de un parametro de configuracion remotamente
	 * @param variable
	 * @return
	 */
	public static boolean EditarParametroTiendaRemota(String hostbd,String variable, String valorTexto)
	{
		boolean actualiza = false;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(hostbd);
		try
		{
			Statement stm = con1.createStatement();
			String update = "update parametros set valortexto = '" + valorTexto + "'  where valorparametro = '" + variable+"'" ; 
			int cant = stm.executeUpdate(update);
			//Ejecutamos la inserci�n de log
			String insercionLog = "insert into parametros_log (usuario, nuevovalor,variable) values ('"  + "AUTOMATICO" + "' , '" + valorTexto +"' ,'" + variable + "')" ;
			stm.executeUpdate(insercionLog);
			if(cant > 0)
			{
				actualiza = true;
			}else
			{
				System.out.println("Cantidad actualizados = 0 " + cant);
			}
			stm.close();
			con1.close();
		}catch (Exception e)
		{
			System.out.println("ERROR y por esto actualiza vale false " + e.toString());
			actualiza = false;
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				
			}
		}
		return(actualiza);
	}
	
	
	/**
	 * Un parametro numerico CON DECIMALES, de general.parametros.valornumericod.
	 *
	 * retornarValorNumerico no sirve para esto: hace Integer.parseInt sobre
	 * valornumerico, asi que un 2.8 se vuelve 0 y el calculo queda mudo sin dar
	 * error. Los porcentajes con decimales van en la columna valornumericod.
	 *
	 * Recibe un valor por defecto y lo devuelve cuando el parametro no existe o
	 * no se puede leer, en vez de un cero silencioso: un porcentaje en cero pasa
	 * desapercibido en un reporte, y un cobro que se deja de descontar se
	 * convierte en plata.
	 */
	public static double retornarValorNumericoDouble(String variable, double porDefecto)
	{
		double valor = porDefecto;
		boolean encontrado = false;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneral();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select valornumericod from parametros where valorparametro = '" + variable + "'";
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				String leido = rs.getString("valornumericod");
				if(leido != null && !leido.trim().equals(""))
				{
					try
					{
						valor = Double.parseDouble(leido.trim());
						encontrado = true;
					}catch(Exception e)
					{
						System.out.println("El parametro " + variable + " no es un numero: '" + leido + "'");
					}
				}
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e)
		{
			System.out.println("retornarValorNumericoDouble " + variable + ": " + e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		if(!encontrado)
		{
			System.out.println("El parametro " + variable + " no esta configurado, se usa " + porDefecto);
		}
		return(valor);
	}
}
