package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.ModificadorInventario;

public class IngresoInventarioDetalleTmpDAO {
	
	public static boolean insertarIngresoInventarioDetTmp( int idDespacho, ModificadorInventario ingreso, String tienda)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(tienda);
		boolean resultado = false;
		try
		{
			//Realizamos la inserción del IdInventario
			Statement stm = con1.createStatement();
			String insert = "insert into ingreso_inventario_detalle_tmp (iddespacho,iditem,cantidad) values (" + idDespacho + ", " + ingreso.getIdItem() + ", " + ingreso.getCantidad() + ")"; 
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
	
	public static boolean borrarIngresoInventarioDetallesTmp( int idDespacho, String tienda)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(tienda);
		boolean resultado = false;
		try
		{
			//Realizamos la inserción del IdInventario
			Statement stm = con1.createStatement();
			String delete = "delete from ingreso_inventario_detalle_tmp where iddespacho = " + idDespacho; 
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

}
