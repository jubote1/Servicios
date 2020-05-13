package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.cj.jdbc.result.ResultSetMetaData;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.EmpleadoEvento;

public class ReporteHorariosDAO {
	
	
	
	public static ArrayList obtenerReporteHorarios(String fechaInicial, String fechaFinal)
	{
		ArrayList informeHorarios = new ArrayList();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT EMPLEADO, FECHA, DIA , INGRESO, SALIDA, IFNULL(TIMESTAMPDIFF(minute, INGRESO, SALIDA),0) AS MINUTOS, idtienda FROM (SELECT DISTINCT(b.nombre_largo) AS EMPLEADO, a.fecha AS FECHA, (ELT(WEEKDAY(a.fecha) + 1, 'Lunes', 'Martes', 'Miercoles', 'Jueves', 'Viernes', 'Sabado', 'Domingo')) AS DIA ," + 
					"IFNULL((SELECT fecha_hora_log c FROM empleado_evento c WHERE c.id = a.id AND c.tipo_evento = 'INGRESO' AND c.fecha = a.fecha),0) AS INGRESO, " + 
					"IFNULL((SELECT fecha_hora_log c FROM empleado_evento c WHERE c.id = a.id AND c.tipo_evento = 'SALIDA' AND c.fecha = a.fecha),0) AS SALIDA , a.idtienda " + 
					"FROM empleado_evento a, empleado b  WHERE a.id = b.id AND a.fecha >= '" + fechaInicial +"' AND a.fecha <= '" + fechaFinal + "' ORDER BY b.nombre_largo, a.fecha ) AS reporte";
			System.out.println(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			ResultSetMetaData rsMd = (ResultSetMetaData) rs.getMetaData();
			int numeroColumnas = rsMd.getColumnCount();
			while(rs.next()){
				String [] fila = new String[numeroColumnas];
				for(int y = 0; y < numeroColumnas; y++)
				{
					fila[y] = rs.getString(y+1);
				}
				informeHorarios.add(fila);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center " + e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(informeHorarios);
	}
	
	
	public static ArrayList obtenerReporteNoUsoHuellero(String fechaInicial, String fechaFinal)
	{
		ArrayList informeHorarios = new ArrayList();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT b.nombre_largo, a.fecha, (ELT(WEEKDAY(a.fecha) + 1, 'Lunes', 'Martes', 'Miercoles', 'Jueves', 'Viernes', 'Sabado', 'Domingo')) AS DIA, a.tipo_evento, a.idtienda  " + 
					"FROM empleado_evento a, empleado b  WHERE a.id = b.id AND a.fecha >= '" + fechaInicial +"' AND a.fecha <= '" + fechaFinal + "' and a.uso_biometria = 'N'";
			System.out.println(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			ResultSetMetaData rsMd = (ResultSetMetaData) rs.getMetaData();
			int numeroColumnas = rsMd.getColumnCount();
			while(rs.next()){
				String [] fila = new String[numeroColumnas];
				for(int y = 0; y < numeroColumnas; y++)
				{
					fila[y] = rs.getString(y+1);
				}
				informeHorarios.add(fila);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center " + e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(informeHorarios);
	}
	
	
	/**
	 * Método creado en la reestructuración de la forma de mostrar la información en donde se vuelve complejo retornarlo en un solo query
	 * y se hace necesario la devolución de un arrayList con la información para procesarla y mostarla de la manera correcta.
	 * @param idTienda
	 * @param fecha
	 * @param bdGeneral
	 * @param auditoria
	 * @return
	 */
	public static ArrayList<EmpleadoEvento> obtenerEntradasSalidasEmpleadosEventos(String fechaInicial, String fechaFinal )
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		String select  = "select b.nombre_largo, (ELT(WEEKDAY(a.fecha) + 1, 'Lunes', 'Martes', 'Miercoles', 'Jueves', 'Viernes', 'Sabado', 'Domingo')) AS DIA , a.* from empleado_evento a , empleado b where a.fecha >= '" + fechaInicial + "' and a.fecha <= '" + fechaFinal + "' and a.id = b.id order by a.id,a.fecha_hora_log asc";
		System.out.println(select);
		Statement stm;
		ArrayList<EmpleadoEvento> eventosEmpleado = new ArrayList();
		try
		{
			stm = con1.createStatement();
			ResultSet rs = stm.executeQuery(select);
			int id, idTienda;
			String tipoEvento, fechaHoraLog, usoBiometria;
			String nombreEmpleado, fecha, dia;
			EmpleadoEvento empEvento;
			while(rs.next())
			{
				id = rs.getInt("id");
				dia = rs.getString("dia");
				idTienda = rs.getInt("idtienda");
				tipoEvento = rs.getString("tipo_evento");
				fechaHoraLog = rs.getString("fecha_hora_log");
				usoBiometria = rs.getString("uso_biometria");
				nombreEmpleado = rs.getString("nombre_largo");
				fecha = rs.getString("fecha");
				empEvento = new EmpleadoEvento(id, tipoEvento, fecha, fechaHoraLog, idTienda, usoBiometria);
				empEvento.setNombreEmpleado(nombreEmpleado);
				empEvento.setDia(dia);
				eventosEmpleado.add(empEvento);
			}
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
