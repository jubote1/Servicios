package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.EmpresaTemporal;
import ModeloSer.FidelizacionRedencion;
import ModeloSer.TiendaCodigoPromocional;

public class ClienteFidelizacionDAO {
	
	/**
	 * Método que retorna el total de clientes que hay en el plan de fidelización
	 * @return
	 */
	public static int obtenerTotalClienteFidelizacion()
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		int totalClientes = 0;
		
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select count(*)  from cliente_fidelizacion ";
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				totalClientes = rs.getInt(1);
				break;
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
		return(totalClientes);
	}
	
	
	
	/**
	 * Método que retorna el total de clientes que se han vinculado al plan de fidelización en un rango de fechas.
	 * @param fechaAnterior
	 * @param fechaPosterior
	 * @return
	 */
	public static int obtenerTotalClienteAfiliadosTiempo(String fechaAnterior, String fechaPosterior)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		int totalClientes = 0;
		
		try
		{
			
			String consulta = "select count(*)  from cliente_fidelizacion where fecha_vinculacion >= ? and fecha_vinculacion <= ?";
			PreparedStatement stmt = con1.prepareStatement(consulta);
			stmt.setString(1, fechaAnterior + " 00:00:00");
			stmt.setString(2, fechaPosterior + " 23:59:59");
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				totalClientes = rs.getInt(1);
				break;
			}
			rs.close();
			stmt.close();
			con1.close();
		}catch (Exception e){
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(totalClientes);
	}
	
	
	/**
	 * Método que se encarga de retornar un acumulado de los puntos que fueron acumulados la semana inmediatamente anterior
	 * @param fechaAnterior
	 * @param fechaPosterior
	 * @return
	 */
	public static double obtenerTotalPuntosAcumuladosTiempo(String fechaAnterior, String fechaPosterior)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		double  totalPuntos = 0;
		
		try
		{
			
			String consulta = "select sum(puntos)  from fidelizacion_transaccion where fecha_transaccion >= ? and fecha_transaccion <= ?";
			PreparedStatement stmt = con1.prepareStatement(consulta);
			stmt.setString(1, fechaAnterior);
			stmt.setString(2, fechaPosterior);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				totalPuntos = rs.getDouble(1);
				break;
			}
			rs.close();
			stmt.close();
			con1.close();
		}catch (Exception e){
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(totalPuntos);
	}
	
	
	/**
	 * Método que nos retorna un total de los puntos redimidos en la semana anterior	
	 * @param fechaAnterior
	 * @param fechaPosterior
	 * @return
	 */
	public static double obtenerTotalPuntosRedimidosTiempo(String fechaAnterior, String fechaPosterior)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		double  totalPuntos = 0;
		
		try
		{
			
			String consulta = "select sum(puntos_redimidos)  from fidelizacion_redencion where fecha_redencion >= ? and fecha_redencion <= ?";
			PreparedStatement stmt = con1.prepareStatement(consulta);
			stmt.setString(1, fechaAnterior + " 00:00:00");
			stmt.setString(2, fechaPosterior + " 23:59:59");
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				totalPuntos = rs.getDouble(1);
				break;
			}
			rs.close();
			stmt.close();
			con1.close();
		}catch (Exception e){
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(totalPuntos);
	}
	
	/**
	 * Método que retorna las redenciones realizadas en un rango de fechas.
	 * @param fechaAnterior
	 * @param fechaPosterior
	 * @return
	 */
	public static ArrayList<FidelizacionRedencion> obtenerRedencionesFecha(String fechaAnterior, String fechaPosterior)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		ArrayList redenciones = new ArrayList();
		
		try
		{
			
			String consulta = "select * from fidelizacion_redencion where fecha_redencion >= ? and fecha_redencion <= ?";
			PreparedStatement stmt = con1.prepareStatement(consulta);
			stmt.setString(1, fechaAnterior + " 00:00:00");
			stmt.setString(2, fechaPosterior + " 23:59:59");
			ResultSet rs = stmt.executeQuery();
			String correo;
			String fechaRedencion;
			double puntosRedimidos;
			FidelizacionRedencion redencionTemp;
			while(rs.next()){
				correo = rs.getString("correo");
				fechaRedencion = rs.getString("fecha_redencion");
				puntosRedimidos = rs.getDouble("puntos_redimidos");
				redencionTemp = new FidelizacionRedencion(correo,fechaRedencion,puntosRedimidos);
				redenciones.add(redencionTemp);
			}
			rs.close();
			stmt.close();
			con1.close();
		}catch (Exception e){
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(redenciones);
	}

}
