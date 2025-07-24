package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.EmpresaTemporal;
import ModeloSer.TiendaCodigoPromocional;

public class FidelizacionTransaccionDAO {
	
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
			stmt.setString(1, fechaPosterior + " 23:59:59");
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

}
