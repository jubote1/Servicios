package CapaDAOServicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionServicios.ConexionBaseDatos;
import Modelo.MarcacionAnulacionPedido;

public class MarcacionAnulacionPedidoDAO {
	
	public static int insertarMarcacionAnulacion(MarcacionAnulacionPedido marcAnulacion)
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conContact = conexion.obtenerConexionBDContact();
		String insert = "insert into marcacion_anulacion_pedido (idmarcacion, idpedido, numposheader,fechapedido, total_neto) values ( " + marcAnulacion.getIdMarcacion() + " , " + marcAnulacion.getIdPedido() + " , " + marcAnulacion.getNumPosHeader() + " , '" + marcAnulacion.getFechaPedido() + "' , " + marcAnulacion.getTotalNeto() + ")";
		int idMarcacionAnulacion = 0;
		try
		{
			Statement stm= conContact .createStatement();
			stm.executeUpdate(insert);
			ResultSet rs = stm.getGeneratedKeys();
			if (rs.next()){
				idMarcacionAnulacion=rs.getInt(1);
				
	        }
			rs.close();
			stm.close();
			conContact.close();
		}catch(Exception e)
		{
			System.out.println("Error en la consulta de la tienda " + e.toString() );
		}
		System.out.println("La id Marcacion Anulacion insertada es  " + idMarcacionAnulacion);
		return(idMarcacionAnulacion);
	}
	
	
	public static ArrayList<MarcacionAnulacionPedido> consultarMarcacionAnulacion(String fechaInferior, String fechaSuperior, int idRazon)
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conContact = conexion.obtenerConexionBDContact();
		String consulta = "select * from marcacion_anulacion_pedido a, pedido b, razon_x_tienda c where a.fechapedido >= '" + fechaInferior + "' and a.fechapedido <= '" + fechaSuperior + "' and a.idpedido = b.idpedido and b.idtienda = c.idtienda and c.idrazon =" + idRazon;
		MarcacionAnulacionPedido marcAnulacion;
		ArrayList<MarcacionAnulacionPedido> marcacionesAnulacion = new ArrayList();
		try
		{
			Statement stm= conContact .createStatement();
			ResultSet rs = stm.executeQuery(consulta);
			int idMarcacionAnulacion;
			int idMarcacion;
			int idPedido;
			int numPosHeader;
			String fechaPedido;
			double totalNeto;
			if (rs.next()){
				idMarcacionAnulacion = rs.getInt("idmarcacion_anulacion");
				idMarcacion = rs.getInt("idmarcacion");;
				idPedido = rs.getInt("idPedido");
				numPosHeader = rs.getInt("numposheader");
				fechaPedido = rs.getString("fechapedido");
				totalNeto = rs.getDouble("total_neto");
				marcAnulacion = new MarcacionAnulacionPedido(idMarcacionAnulacion, idMarcacion, idPedido, numPosHeader, fechaPedido, totalNeto);
				marcacionesAnulacion.add(marcAnulacion);
	        }
			rs.close();
			stm.close();
			conContact.close();
		}catch(Exception e)
		{
			System.out.println("Error en la consulta de la tienda " + e.toString() );
		}
		
		return(marcacionesAnulacion);
	}
	
	
	public static void eliminarMarcacionAnulacion(String fechaInferior, String fechaSuperior, int idRazon)
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conContact = conexion.obtenerConexionBDContact();
		String consulta = "select * from marcacion_anulacion_pedido a, pedido b, razon_x_tienda c where a.fechapedido >= '" + fechaInferior + "' and a.fechapedido <= '" + fechaSuperior + "' and a.idpedido = b.idpedido and b.idtienda = c.idtienda and c.idrazon =" + idRazon;
		MarcacionAnulacionPedido marcAnulacion;
		ArrayList<MarcacionAnulacionPedido> marcacionesAnulacion = new ArrayList();
		try
		{
			Statement stm= conContact .createStatement();
			ResultSet rs = stm.executeQuery(consulta);
			int idMarcacionAnulacion;
			int idMarcacion;
			int idPedido;
			int numPosHeader;
			String fechaPedido;
			double totalNeto;
			if (rs.next()){
				idMarcacionAnulacion = rs.getInt("idmarcacion_anulacion");
				idMarcacion = rs.getInt("idmarcacion");;
				idPedido = rs.getInt("idPedido");
				numPosHeader = rs.getInt("numposheader");
				fechaPedido = rs.getString("fechapedido");
				totalNeto = rs.getDouble("total_neto");
				marcAnulacion = new MarcacionAnulacionPedido(idMarcacionAnulacion, idMarcacion, idPedido, numPosHeader, fechaPedido, totalNeto);
				marcacionesAnulacion.add(marcAnulacion);
	        }
			rs.close();
			stm.close();
			conContact.close();
		}catch(Exception e)
		{
			System.out.println("Error en la consulta de la tienda " + e.toString() );
		}
		
	}


}
