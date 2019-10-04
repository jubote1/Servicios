package CapaDAOServicios;
import Modelo.EmpleadoBiometria;
import Modelo.TipoEmpleado;
import Modelo.Usuario;

import ConexionServicios.ConexionBaseDatos;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.sql.ResultSet;


import com.mysql.jdbc.ResultSetMetaData;
/**
 * Clase que se encarga de implementar toda la interacción con la base de datos para la entidad Usuario.
 * @author JuanDavid
 *
 */
public class UsuarioDAO {

//LA PRIMERA FASE TIENE COMO OBJETIVO RECOPILAR LA INFO DE LA BASE DE DATOS GENERAL
/*
 * Método que se encargará de retornar todos los empleados de la base de datos general
 */
	public static ArrayList<Usuario> obtenerEmpleadosGeneral()
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		ArrayList<Usuario> empleados = new ArrayList();
		
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from empleado ";
			ResultSet rs = stm.executeQuery(consulta);
			int id;
			String nombre, nombreLargo, administrador, tipoInicio;
			int tipoEmpleado;
			
			
			while(rs.next()){
				id = rs.getInt("id");
				nombre = rs.getString("nombre");
				nombreLargo = rs.getString("nombre_largo");
				administrador = rs.getString("administrador");
				tipoInicio = rs.getString("tipoinicio");
				tipoEmpleado = rs.getInt("idtipoempleado");
				Usuario usuarioTemp = new Usuario(id, nombre, "", nombreLargo, tipoEmpleado,
			tipoInicio, administrador);
				empleados.add(usuarioTemp);
				
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(empleados);
		
	}
	
	/**
	 * Método qeu se encargará de retornar la toma de biometría de todos los empleados
	 * @return
	 */
	public static ArrayList<EmpleadoBiometria> obtenerEmpleadosBiometriaGeneral()
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		ArrayList<EmpleadoBiometria> empleadosBio = new ArrayList();
		
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from empleado_biometria";
			ResultSet rs = stm.executeQuery(consulta);
			int id;
			byte datosHuella[];
			while(rs.next()){
				id = rs.getInt("id");
				datosHuella = rs.getBytes("biometria");
				EmpleadoBiometria empBio = new EmpleadoBiometria(id, datosHuella);
				empleadosBio.add(empBio);
				
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(empleadosBio);
		
	}
	
	
	
// De aqui en adelante tendremos los temas para trabajar de manera LOCAL LA INSERCIÓN Y BORRADO DE LAS TABLAS
	/**
	 * Método para clarear las tablas locales que serán sujeto de inserción
	 * @param hostBD
	 * @return
	 */
	public static boolean eliminarInfoEmpleadoLocal(String hostBD)
	{
		boolean respuesta = true;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralTienda(hostBD);
		try
		{
			Statement stm = con1.createStatement();
			String deleteEmpleado = "delete from empleado";
			String deleteEmpBio = "delete from empleado_biometria";
			stm.executeUpdate(deleteEmpleado);
			stm.executeUpdate(deleteEmpBio);
			respuesta = true;
			stm.close();
			con1.close();
		}
		catch (Exception e){
			try
			{
				con1.close();
				respuesta = false;
			}catch(Exception e1)
			{
				respuesta = false;
			}
			
		}
		return(respuesta);
	}
	
		
	/**
	 * Método qeu se encarga de insertar un empleado en el sistema
	 * @param empleado Se recibe un objeto de tipo usuario con la información del empleado que termina siendo un autor del sistema
	 * @return Se retorna un entero con id asignado por el sistema en la inserción.
	 */
	public static int insertarEmpleadoLocal(Usuario empleado, String hostBD)
	{
		int idEmpleadoIns = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralTienda(hostBD);
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into empleado (id,nombre, password,  nombre_largo,administrador, idtipoempleado, tipoinicio) values (" + empleado.getIdUsuario() + " ,'" + empleado.getNombreUsuario() + "' , '" + empleado.getContrasena() + "' , '" + empleado.getNombreLargo() + "' , '" + empleado.getAdministrador() + "', " + empleado.getidTipoEmpleado() + " , '" + empleado.getTipoInicio() + "')"; 
			stm.executeUpdate(insert);
			ResultSet rs = stm.getGeneratedKeys();
			if (rs.next()){
				idEmpleadoIns=rs.getInt(1);
				
	        }
			stm.close();
			con1.close();
		}
		catch (Exception e){
			
			if(e instanceof NullPointerException)
			{
				idEmpleadoIns = -1;
			}

			try
			{
				System.out.println(e.toString());
				con1.close();
			}catch(Exception e1)
			{
			}
			return(0);
		}
		return(idEmpleadoIns);
	}
	
	
	public static boolean insertarEmpleadoBiometriaLocal(EmpleadoBiometria empBio, String hostBD)
	{
		int idEmpleadoIns = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralTienda(hostBD);
		String insert = "insert into empleado_biometria (id,biometria) values (?,?)";
		PreparedStatement pstm;
		try
		{
			pstm = con1.prepareStatement(insert);
			pstm.setInt(1, empBio.getId());
			pstm.setBytes(2, empBio.getDatoshuella()); 
			pstm.executeUpdate();
			pstm.close();
			con1.close();
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
	
	
	
}
