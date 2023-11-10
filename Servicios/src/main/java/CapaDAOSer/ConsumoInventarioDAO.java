package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import ConexionSer.ConexionBaseDatos;

public class ConsumoInventarioDAO {
	
	/**
	 * Método que se encarga de la inserción del consumo inventario de manera masiva.
	 * @param fecha
	 * @param idTienda
	 * @param idInsumo
	 * @param cantidad
	 * @return
	 */
	public static boolean insertarConsumoInventario(String fecha, int idTienda, int idInsumo, double cantidad)
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventarioLocal();
		boolean resultado = false;
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into consumo_inventario (fecha_consumo, idtienda, idinsumo, cantidad) values ('" + fecha +"', " + idTienda + " , " + idInsumo + " , " + cantidad + ")" ;
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
	
	
	public static boolean existeConsumoInventario(String fecha, int idTienda)
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventarioLocal();
		boolean resultado = false;
		try
		{
			Statement stm = con1.createStatement();
			String select = "select * from consumo_inventario where fecha_consumo = '" + fecha +"' and idtienda = " + idTienda ;
			ResultSet rs = stm.executeQuery(select);
			resultado = false;
			while(rs.next())
			{
				resultado = true;
			}
			rs.close();
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
