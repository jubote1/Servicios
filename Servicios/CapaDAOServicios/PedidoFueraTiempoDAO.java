package CapaDAOServicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionServicios.ConexionBaseDatos;
import Modelo.MarcacionAnulacionPedido;
import Modelo.MarcacionCambioPedido;
import Modelo.PedidoFueraTiempo;

public class PedidoFueraTiempoDAO {
	
	public static void insertarPedidoFueraTiempo(PedidoFueraTiempo pedFueraTiempo)
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conContact = conexion.obtenerConexionBDContact();
		String insert = "insert into pedido_fuera_tiempo (idpedido, idtienda, transact, tiempo_dado, tiempo_actual, por_desviacion, domiciliario, estado_pedido, observacion) values ( " + pedFueraTiempo.getIdPedido() + " , " + pedFueraTiempo.getIdTienda() + " , " + pedFueraTiempo.getTransact() + " , " + pedFueraTiempo.getTiempoDado() + " , " + pedFueraTiempo.getTiempoActual()  + " , " + pedFueraTiempo.getPorcDesviacion() + " , '" + pedFueraTiempo.getDomiciliario() + "' , '" + pedFueraTiempo.getEstadoPedido() + "' , '" + pedFueraTiempo.getObservacion() + "')";
		try
		{
			Statement stm= conContact .createStatement();
			stm.executeUpdate(insert);
			stm.close();
			conContact.close();
		}catch(Exception e)
		{
			System.out.println("Error en la inserción del pedido por fuera de tiempo " + e.toString() );
		}
		
		
	}
	
	public static boolean existePedido(int idPedido)
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conContact = conexion.obtenerConexionBDContact();
		String consulta = "select * from pedido_fuera_tiempo  where idpedido = " + idPedido;
		boolean respuesta = false;
		try
		{
			Statement stm= conContact .createStatement();
			ResultSet rs = stm.executeQuery(consulta);
			
			if (rs.next()){
				respuesta = true;
				
	        }
			rs.close();
			stm.close();
			conContact.close();
		}catch(Exception e)
		{
			System.out.println("Error validandola inserción de pedido fuera de tiempo " + e.toString() );
		}
		return(respuesta);
	}
	
	
	public static void ActualizarPedidoFueraTiempo(int idPedido, double porDesviacion, String domiciliario, String estadoPedido, double tiempoActualPedido, String observacion)
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conContact = conexion.obtenerConexionBDContact();
		String update = "update pedido_fuera_tiempo set por_desviacion ="+  porDesviacion + ", domiciliario ='" + domiciliario + "' , estado_pedido = '" + estadoPedido + "', tiempo_actual =" + tiempoActualPedido+ " , observacion = '"+observacion + "'  where idpedido = " + idPedido;
		try
		{
			Statement stm= conContact .createStatement();
			stm.executeUpdate(update);
			stm.close();
			conContact.close();
		}catch(Exception e)
		{
			System.out.println("Error en la actulización de Pedido fuera tiempo " + e.toString() );
		}
		
		
	}
	
	
	public static ArrayList<PedidoFueraTiempo> obtenerPedidoFueraTiempo(int idTienda, int numMinutos)
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conContact = conexion.obtenerConexionBDContact();
		String consulta = "select * from pedido_fuera_tiempo a where a.idtienda =" + idTienda + " and  TIMESTAMPDIFF(minute,fecha_actualizacion,NOW()) < " + numMinutos ;
		ArrayList<PedidoFueraTiempo> pedidosFueraTiempo = new ArrayList();
		PedidoFueraTiempo pedFueraTemp;
		System.out.println(consulta);
		try
		{
			Statement stm= conContact .createStatement();
			ResultSet rs = stm.executeQuery(consulta);
			int idPedido;
			long transact;
			double tiempoDado;
			double tiempoActual;
			double porcDesviacion;
			String domiciliario;
			String estadoPedido;
			String observacion;
			while(rs.next())
			{
				idPedido = rs.getInt("idpedido");
				transact = rs.getLong("transact");
				tiempoDado = rs.getDouble("tiempo_dado");
				tiempoActual = rs.getDouble("tiempo_actual");
				porcDesviacion = rs.getDouble("por_desviacion");
				domiciliario = rs.getString("domiciliario");
				estadoPedido = rs.getString("estado_pedido");
				observacion = rs.getString("observacion");
				pedFueraTemp = new PedidoFueraTiempo(idPedido, idTienda, transact, tiempoDado, tiempoActual, porcDesviacion, domiciliario, estadoPedido, observacion);
				pedidosFueraTiempo.add(pedFueraTemp);
			}
			stm.close();
			conContact.close();
		}catch(Exception e)
		{
			System.out.println("Error en la consulta de los pedidos fuera de tiempo por tienda " + e.toString() );
		}
		return(pedidosFueraTiempo);
		
	}
	
	
}
