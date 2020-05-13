package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.DiaFestivo;


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
	
	
	public static ArrayList obtenerDiasFestivos()
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		ArrayList<DiaFestivo> festivo = new ArrayList();
		DiaFestivo festivoTemp = new DiaFestivo(0,"");
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from dia_festivo";
			ResultSet rs = stm.executeQuery(consulta);
			int id = 0;
			String fechaFestiva = "";
			while(rs.next()){
				id = rs.getInt("id");
				fechaFestiva = rs.getString("fecha_festiva");
				festivoTemp = new DiaFestivo(id, fechaFestiva);
				festivo.add(festivoTemp);
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
		return(festivo);
		
	}

}
