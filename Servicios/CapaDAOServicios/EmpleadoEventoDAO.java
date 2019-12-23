package CapaDAOServicios;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.mysql.jdbc.ResultSetMetaData;

import ConexionServicios.ConexionBaseDatos;
import Modelo.EmpleadoEvento;
import Modelo.Usuario;


public class EmpleadoEventoDAO {
	
	
	/**
	 * Método de base para la insercion de los eventos en la base de datos centralizada
	 * @param empEvento
	 * @return
	 */
	public static boolean insertarEventoRegistroEmpleado(EmpleadoEvento empEvento)
	{
		ConexionServicios.ConexionBaseDatos con = new ConexionServicios.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		String insert = "insert into empleado_evento (id,tipo_evento,fecha,idtienda,uso_biometria) values (" + empEvento.getId()+ " , '" + empEvento.getTipoEvento() + "' , '" + empEvento.getFecha() + "' , " + empEvento.getIdTienda() + " , '" + empEvento.getUsoBiometria() + "')";
		Statement stm;
		try
		{
			stm = con1.createStatement();
			stm.executeUpdate(insert);
			return(true);
		}
		catch (Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
				return(false);
			}catch(Exception e1)
			{
				return(false);
			}
			
		}
		
	}
	
	/**
	 * Método para realizar la inserción del evento empleado pero en dirección local
	 * @param empEvento
	 * @return
	 */
	public static boolean insertarEventoRegistroEmpleadoLocal(EmpleadoEvento empEvento, String hostBD)
	{
		ConexionServicios.ConexionBaseDatos con = new ConexionServicios.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralTienda(hostBD);
		String insert = "insert into empleado_evento (id,tipo_evento,fecha,idtienda,uso_biometria,migrado) values (" + empEvento.getId()+ " , '" + empEvento.getTipoEvento() + "' , '" + empEvento.getFecha() + "' , " + empEvento.getIdTienda() + " , '" + empEvento.getUsoBiometria() +  "' , 'S')";
		Statement stm;
		try
		{
			stm = con1.createStatement();
			stm.executeUpdate(insert);
			return(true);
		}
		catch (Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
				return(false);
			}catch(Exception e1)
			{
				return(false);
			}
			
		}
		
	}
	
	
	/**
	 * Método para realizar la actualización de la inserción en la bodega
	 * @param empEvento
	 * @param hostBD
	 * @return
	 */
	public static boolean actualizarEventoRegistroEmpleadoGeneral(EmpleadoEvento empEvento)
	{
		ConexionServicios.ConexionBaseDatos con = new ConexionServicios.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		String update = "update empleado_evento set fecha_hora_log = '"+ empEvento.getFechaHoraLog() +"' where id =" + empEvento.getId()+ " and tipo_evento =  '" + empEvento.getTipoEvento() + "' and fecha = '" + empEvento.getFecha() + "'";
		Statement stm;
		try
		{
			stm = con1.createStatement();
			stm.executeUpdate(update);
			return(true);
		}
		catch (Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
				return(false);
			}catch(Exception e1)
			{
				return(false);
			}
			
		}
		
	}
	
	public static boolean actualizarEventoRegistroEmpleadoLocal(EmpleadoEvento empEvento, String hostBD)
	{
		ConexionServicios.ConexionBaseDatos con = new ConexionServicios.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralTienda(hostBD);
		String update = "update empleado_evento set fecha_hora_log = '"+ empEvento.getFechaHoraLog() +"' where id =" + empEvento.getId()+ " and tipo_evento =  '" + empEvento.getTipoEvento() + "' and fecha = '" + empEvento.getFecha() + "'";
		Statement stm;
		try
		{
			stm = con1.createStatement();
			stm.executeUpdate(update);
			return(true);
		}
		catch (Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
				return(false);
			}catch(Exception e1)
			{
				return(false);
			}
			
		}
		
	}
	
	/*
	 * Método local que se encargará de borrar los eventos locales que ya tienen más de un día en la base de datos local
	 */
	public static boolean borrarEventoRegistroEmpleadoLocal(String hostBD)
	{
		//Capturamos la fecha actual
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendarioActual = Calendar.getInstance();
		//Capturaremos la hora y validaremos la resta del día
		int horaActual = calendarioActual.get(Calendar.HOUR_OF_DAY);
		if (horaActual < 3)
		{
			//Restamos en 2 dado que ya pasamos de día pero todavía estamos en trabajo seguramente
			calendarioActual.add(Calendar.DAY_OF_YEAR, -2);
		}else
		{
			//Restamos en 1 el día para borrar lo del día anterior
			calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
		}
		Date datFechaAnterior = calendarioActual.getTime();
		String fechaAnterior = dateFormat.format(datFechaAnterior);
		//En este punto ya tenemos la fecha que utilizaremos en el where
		
		ConexionServicios.ConexionBaseDatos con = new ConexionServicios.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralTienda(hostBD);
		String delete = "delete from empleado_evento where fecha <= '" + fechaAnterior + "' and migrado = 'S'";
		Statement stm;
		try
		{
			stm = con1.createStatement();
			stm.executeUpdate(delete);
			return(true);
		}
		catch (Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
				return(false);
			}catch(Exception e1)
			{
				return(false);
			}
			
		}
		
	}
	
	
	public static boolean marcarEventoRegistroEmpleadoLocal(int id, String tipoEvento, String fecha, String hostBD)
	{
		ConexionServicios.ConexionBaseDatos con = new ConexionServicios.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralTienda(hostBD);
		String update = "update empleado_evento set migrado = 'S' where fecha = '" + fecha + "' and tipo_evento = '"+ tipoEvento + "' and id = " + id;
		Statement stm;
		try
		{
			stm = con1.createStatement();
			stm.executeUpdate(update);
			return(true);
		}
		catch (Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
				return(false);
			}catch(Exception e1)
			{
				return(false);
			}
			
		}
		
	}
	
	
	public static ArrayList<EmpleadoEvento> obtenerEventosPendientesLocal(String hostBD)
	{
		ConexionServicios.ConexionBaseDatos con = new ConexionServicios.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralTienda(hostBD);
		String consulta = "select * from empleado_evento where migrado = 'N' ";
		Statement stm;
		ArrayList<EmpleadoEvento> eventosEmpleado = new ArrayList();
		try
		{
			stm = con1.createStatement();
			ResultSet rs = stm.executeQuery(consulta);
			int id, idTienda;
			String tipoEvento, fecha, fechaHoraLog, usoBiometria; 
						
			while(rs.next()){
				id = rs.getInt("id");
				idTienda = rs.getInt("idtienda");
				tipoEvento = rs.getString("tipo_evento");
				fecha = rs.getString("fecha");
				fechaHoraLog = rs.getString("fecha_hora_log");
				usoBiometria = rs.getString("uso_biometria");
				EmpleadoEvento empEvento = new EmpleadoEvento(id, tipoEvento, fecha, fechaHoraLog, idTienda, usoBiometria);
				eventosEmpleado.add(empEvento);
				
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
		return(eventosEmpleado);
		
	}
	
	
	public static boolean existeEventoEmpleadoLocal(int id, String fecha, String tipoEvento, String hostBD)
	{
		ConexionServicios.ConexionBaseDatos con = new ConexionServicios.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralTienda(hostBD);
		String consulta = "select * from empleado_evento where fecha = '" + fecha + "' and tipo_evento = '" + tipoEvento + "' and id =" + id;
		Statement stm;
		boolean respuesta = false;
		try
		{
			stm = con1.createStatement();
			ResultSet rs = stm.executeQuery(consulta);
				
			while(rs.next()){
				respuesta = true;
				
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
				respuesta = false;
			}
			
		}
		return(respuesta);
		
	}
	
	
	public static ArrayList<EmpleadoEvento> obtenerEventosGeneral()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendarioActual = Calendar.getInstance();
		Date datFechaActual = calendarioActual.getTime();
		String fechaActual = dateFormat.format(datFechaActual);
		//Ya con la fecha recuperaremos los eventos del día
		ConexionServicios.ConexionBaseDatos con = new ConexionServicios.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		String consulta = "select * from empleado_evento where fecha = '" + fechaActual + "'";
		Statement stm;
		ArrayList<EmpleadoEvento> eventosEmpleado = new ArrayList();
		try
		{
			stm = con1.createStatement();
			ResultSet rs = stm.executeQuery(consulta);
			int id, idTienda;
			String tipoEvento, fecha, fechaHoraLog, usoBiometria; 
						
			while(rs.next()){
				id = rs.getInt("id");
				idTienda = rs.getInt("idtienda");
				tipoEvento = rs.getString("tipo_evento");
				fecha = rs.getString("fecha");
				fechaHoraLog = rs.getString("fecha_hora_log");
				usoBiometria = rs.getString("uso_biometria");
				EmpleadoEvento empEvento = new EmpleadoEvento(id, tipoEvento, fecha, fechaHoraLog, idTienda, usoBiometria);
				eventosEmpleado.add(empEvento);
				
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
		return(eventosEmpleado);
		
	}

}
