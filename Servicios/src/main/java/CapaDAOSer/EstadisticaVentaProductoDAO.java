package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.EstadisticaVentaProducto;

public class EstadisticaVentaProductoDAO {
	
	public static ArrayList<EstadisticaVentaProducto> retornarEstadisticasVentaProducto(String periodicidad)
	{
		ArrayList<EstadisticaVentaProducto> estadisticas = new ArrayList();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneral();
		EstadisticaVentaProducto estTemp;
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from estadistica_venta_producto where periodicidad = '"+ periodicidad +"'";
			ResultSet rs = stm.executeQuery(consulta);
			int idProducto  = 0;
			while(rs.next()){
				idProducto = rs.getInt("idproducto");
				estTemp = new EstadisticaVentaProducto(idProducto, periodicidad);
				estadisticas.add(estTemp);
				
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
		return(estadisticas);
	}

}
