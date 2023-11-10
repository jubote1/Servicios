package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import ModeloSer.EmpleadoEncuestaDetalle;

public class EmpleadoEncuestaDetalleDAO {
	
	public static int insertarEmpleadoEncuestaDetalle(EmpleadoEncuestaDetalle empEncuestaDetalle)
	{
		Logger logger = Logger.getLogger("log_file");
		int idEmpleadoEncuestaDetalle = 0;
		ConexionSer.ConexionBaseDatos con = new ConexionSer.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneral();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into empleado_encuesta_detalle (idempleadoencuesta, idencuestadetalle, respuesta_si, respuesta_no, observacion) values (" + empEncuestaDetalle.getIdEmpleadoEncuesta() + " , " + empEncuestaDetalle.getIdEncuestaDetalle() + " , '" + empEncuestaDetalle.getRespuestaSi() + "' , '" + empEncuestaDetalle.getRespuestaNo() + "' , '" + empEncuestaDetalle.getObservacion() + "')"; 
			stm.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = stm.getGeneratedKeys();
			if (rs.next()){
				idEmpleadoEncuestaDetalle=rs.getInt(1);
	        }
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
			return(0);
		}
		return(idEmpleadoEncuestaDetalle);
	}
	
	
	public static ArrayList<EmpleadoEncuestaDetalle> obtenerEmpleadoEncuesta(String hostBD, int idEmpleadoEncuesta)
	{
		ConexionSer.ConexionBaseDatos con = new ConexionSer.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralTienda(hostBD);
		String consulta = "select * from empleado_encuesta_detalle where idempleadoencuesta = " + idEmpleadoEncuesta;
		Statement stm;
		ArrayList<EmpleadoEncuestaDetalle> empleadoEncuestaDetalles = new ArrayList();
		try
		{
			stm = con1.createStatement();
			ResultSet rs = stm.executeQuery(consulta);
			int  idEncuestaDetalle;
			String respuestaSi, respuestaNo, observacion;
						
			while(rs.next()){
				idEncuestaDetalle = rs.getInt("idencuestadetalle");
				respuestaNo = rs.getString("respuesta_no");
				respuestaSi = rs.getString("respuesta_si");
				observacion  = rs.getString("observacion");
				EmpleadoEncuestaDetalle empEncuestaDetalle = new EmpleadoEncuestaDetalle(0, idEmpleadoEncuesta, idEncuestaDetalle, respuestaSi, respuestaNo, observacion);
				empleadoEncuestaDetalles.add(empEncuestaDetalle);
				
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
		return(empleadoEncuestaDetalles);
		
	}
	
	public static void borrarEmpleadoEncuestaDetalle(int idEmpleadoEncuesta, String hostBD)
	{
		Logger logger = Logger.getLogger("log_file");
		int idEmpleadoEncuestaDetalle = 0;
		ConexionSer.ConexionBaseDatos con = new ConexionSer.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralTienda(hostBD);
		try
		{
			Statement stm = con1.createStatement();
			String insert = "delete from empleado_encuesta_detalle where idempleadoencuesta = " + idEmpleadoEncuesta;
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
