package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import org.apache.log4j.Logger;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.EmpleadoEncuesta;

public class EmpleadoEncuestaDAO {
	
	public static int insertarEmpleadoEncuesta(EmpleadoEncuesta empEncuesta)
	{
		int idEmpleadoEncuesta = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneral();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into empleado_encuesta (id, idencuesta) values (" + empEncuesta.getId() + " , " + empEncuesta.getIdEncuesta() + ")"; 
			stm.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = stm.getGeneratedKeys();
			if (rs.next()){
				idEmpleadoEncuesta=rs.getInt(1);
	        }
			stm.close();
			con1.close();
		}
		catch (Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
			return(0);
		}
		return(idEmpleadoEncuesta);
	}
	
	public static ArrayList<EmpleadoEncuesta> obtenerEmpleadoEncuesta(String hostBD)
	{
		ConexionSer.ConexionBaseDatos con = new ConexionSer.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralTienda(hostBD);
		String consulta = "select * from empleado_encuesta ";
		Statement stm;
		ArrayList<EmpleadoEncuesta> empleadoEncuestas = new ArrayList();
		try
		{
			stm = con1.createStatement();
			ResultSet rs = stm.executeQuery(consulta);
			int id, idEncuesta, idEmpleadoEncuesta;
			String fechaIngreso;
						
			while(rs.next()){
				idEmpleadoEncuesta = rs.getInt("idempleadoencuesta");
				id = rs.getInt("id");
				idEncuesta= rs.getInt("idencuesta");
				fechaIngreso = rs.getString("fecha_ingreso");
				EmpleadoEncuesta empEncuesta = new EmpleadoEncuesta(idEmpleadoEncuesta, id, idEncuesta, fechaIngreso);
				empleadoEncuestas.add(empEncuesta);
				
			}
			rs.close();
			stm.close();
			con1.close();
		}
		catch (Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
				
			}catch(Exception e1)
			{
				
			}
			
		}
		return(empleadoEncuestas);
		
	}
	
	public static void borrarEmpleadoEncuesta(int idEmpleadoEncuesta, String hostBD)
	{
		Logger logger = Logger.getLogger("log_file");
		int idEmpleadoEncuestaDetalle = 0;
		ConexionSer.ConexionBaseDatos con = new ConexionSer.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralTienda(hostBD);
		try
		{
			Statement stm = con1.createStatement();
			String insert = "delete from empleado_encuesta where idempleadoencuesta = " + idEmpleadoEncuesta;
			stm.executeUpdate(insert);
			stm.close();
			con1.close();
		}
		catch (Exception e){
			logger.error(e.toString());
			System.out.println(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
	}
	

}
