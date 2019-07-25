package CapaDAOServicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import com.mysql.jdbc.ResultSetMetaData;

import ConexionServicios.ConexionBaseDatos;
import Modelo.InsumoAlerta;


public class InsumoAlertaDAO {
	
	public static ArrayList<InsumoAlerta> retornarInsumosAlerta()
	{
		ArrayList<InsumoAlerta> insumosAlerta = new ArrayList();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventario();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from insumo_alerta";
			ResultSet rs = stm.executeQuery(consulta);
			int idInsumo = 0;
			double cantidad = 0;
			while(rs.next()){
				
				idInsumo = rs.getInt("idinsumo");
				cantidad = rs.getDouble("cantidad");
				InsumoAlerta insTemp = new InsumoAlerta(idInsumo, cantidad);
				insumosAlerta.add(insTemp);
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
		return(insumosAlerta);
	}
	
	
	public static boolean insumoAlertaReportado(int idInsumo, int idTienda, String fechaApertura)
	{
		boolean reportado = false;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventario();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from insumo_alerta_correo where idinsumo = " + idInsumo + " and idtienda = " + idTienda + " and fecha_sistema = '" + fechaApertura + "'";
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				
				reportado = true;
				break;
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
		return(reportado);
	}
	
	public static void insertarInsumoAlerta(int idInsumo, int idTienda, String fechaApertura)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventario();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into insumo_alerta_correo (idinsumo,fecha_sistema,idtienda) values (" + idInsumo + "  , '" + fechaApertura + "' ," + idTienda + ")";
			stm.executeUpdate(insert);
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
	}
	
	
	
}
