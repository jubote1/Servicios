package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.ModificadorInventario;

public class IngresoInventarioTmpDAO {
	
	public static boolean insertarIngresoInventarioTmp( String fecha, int idDespacho, String tienda, String observacion)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(tienda);
		boolean resultado = false;
		try
		{
			//Realizamos la inserción del IdInventario
			Statement stm = con1.createStatement();
			String insert = "insert into ingreso_inventario_tmp (iddespacho,fecha_sistema, observacion) values (" + idDespacho + ", '" + fecha + "' , '" + observacion +  "')"; 
			stm.executeUpdate(insert);
			resultado = true;
			stm.close();
			con1.close();
		}
		catch (Exception e){
			resultado = false;
			try
			{
				con1.close();
			}catch(Exception e1)	
			{
			}
			
		}
		return(resultado);
	}
	
	public static boolean borrarIngresoInventarioTmp( int idDespacho, String tienda)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(tienda);
		boolean resultado = false;
		try
		{
			//Realizamos la inserción del IdInventario
			Statement stm = con1.createStatement();
			String delete = "delete from ingreso_inventario_tmp where iddespacho = " + idDespacho; 
			stm.executeUpdate(delete);
			resultado = true;
			stm.close();
			con1.close();
		}
		catch (Exception e){
			resultado = false;
			try
			{
				con1.close();
			}catch(Exception e1)	
			{
			}
			
		}
		return(resultado);
	}
	
	
	public static boolean existeIngresoInventarioTmp( int idDespacho, String tienda)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(tienda);
		boolean resultado = false;
		try
		{
			//Realizamos la inserción del IdInventario
			Statement stm = con1.createStatement();
			String consulta = "select * from ingreso_inventario_tmp where iddespacho = " + idDespacho; 
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next())
			{
				resultado = true;
				break;
			}
			stm.close();
			con1.close();
		}
		catch (Exception e){
			resultado = false;
			try
			{
				con1.close();
			}catch(Exception e1)	
			{
			}
			
		}
		return(resultado);
	}

}
