package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.IngresoGaseosaHistorico;
import capaModeloPOS.Ingreso;

public class IngresoGaseosaHistoricoDAO {
	
	public static void insertarIngresoGaseosaHistorico(int idTienda, IngresoGaseosaHistorico ingreso)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDDatamartLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into ingreso_gaseosa_historico (idtienda,idingreso_inventario,iditem,nombre_item,cantidad,fecha_sistema) values(" + idTienda + " ," + ingreso.getIdIngresoInventario() + ", " + ingreso.getIdItem() + " , '"+ ingreso.getNombreItem() + "', " +  ingreso.getCantidad()+" , '" + ingreso.getFechaSistema()+ "')";
			stm.executeUpdate(insert);
			stm.close();
			con1.close();
		}catch (Exception e){
			e.toString();
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
	}
	
	public static ArrayList<IngresoGaseosaHistorico> obtenerIngresoGaseosaHistoricoTienda(String fechaAnterior, String fechaActual, String baseDatos)
	{
		ArrayList<IngresoGaseosaHistorico> ingresos  = new ArrayList();
		IngresoGaseosaHistorico ingTemp;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(baseDatos);
		try
		{
			Statement stm = con1.createStatement();
			String select = "SELECT b.idingreso_inventario, a.iditem, a.nombre_item, c.cantidad, b.fecha_sistema FROM item_inventario a , ingreso_inventario b, ingreso_inventario_detalle c WHERE " + 
					"	a.categoria = 'Bebidas' AND b.idingreso_inventario = c.idingreso_inventario AND a.iditem = c.iditem AND b.fecha_sistema >= '" + fechaAnterior +"' AND b.fecha_sistema  <= '" + fechaActual + "'";
			ResultSet rs = stm.executeQuery(select);
			int idIngresoInventario;
			int idItem;
			String nombreItem;
			double cantidad;
			String fechaSistema;
			while(rs.next())
			{
				idIngresoInventario = rs.getInt("idingreso_inventario");
				idItem = rs.getInt("iditem");
				nombreItem = rs.getString("nombre_item");
				cantidad = rs.getDouble("cantidad");
				fechaSistema = rs.getString("fecha_sistema");
				ingTemp = new IngresoGaseosaHistorico(idIngresoInventario, idItem, nombreItem, cantidad, fechaSistema);
				ingresos.add(ingTemp);
			}
			stm.close();
			con1.close();
		}catch (Exception e){
			e.toString();
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(ingresos);
	}

}
