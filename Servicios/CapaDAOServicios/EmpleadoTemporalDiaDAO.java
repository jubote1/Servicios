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
import Modelo.EmpleadoTemporalDia;

public class EmpleadoTemporalDiaDAO {
	
	
	/**
	 * Método que retorna un listado de los empleados temporales que se dieron ingreso en un determinado día en el sistema
	 * @param fecha
	 * @param auditoria
	 * @return
	 */
	public static ArrayList<EmpleadoTemporalDia> obtenerEmpleadoTemporalFecha(String fechaActual, String fechaAnterior, int idEmpresa, String conexionTienda)
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(conexionTienda);
		String select  = "select a.*, b.nombre_largo  from empleado_temporal_dia a, usuario b where a.id = b.id and a.fecha_sistema >= '" + fechaAnterior + "' and a.fecha_sistema <= '" + fechaActual + "' and a.idempresa = " + idEmpresa + " order by a.fecha_sistema";
		ArrayList<EmpleadoTemporalDia> empleadosTemp = new ArrayList();
		Statement stm;
		EmpleadoTemporalDia empRespuesta = new EmpleadoTemporalDia(0,"", "", "", "", "", 0);
		try
		{
			stm = con1.createStatement();
			ResultSet rs = stm.executeQuery(select);
			String evento = "";
			int id;
			String nombre, telefono, empresa, identificacion, usuario;
			String horaIngreso, horaSalida, fecha;
			while(rs.next())
			{
				id = rs.getInt("id");
				usuario = rs.getString("nombre_largo");
				nombre = rs.getString("nombre");
				identificacion = rs.getString("identificacion");
				telefono = rs.getString("telefono");
				empresa = rs.getString("empresa");
				horaIngreso = rs.getString("horaingreso");
				horaSalida = rs.getString("horasalida");
				fecha = rs.getString("fecha_sistema");
				empRespuesta = new EmpleadoTemporalDia(id, identificacion, usuario + " " + nombre, telefono, empresa,
					fecha, idEmpresa);
				empRespuesta.setHoraIngreso(horaIngreso);
				empRespuesta.setHoraSalida(horaSalida);
				empleadosTemp.add(empRespuesta);
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
		//Realizamos las validaciones de lo que retornamos
		
		return(empleadosTemp);
	}


}
