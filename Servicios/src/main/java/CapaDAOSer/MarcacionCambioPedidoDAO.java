package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import capaModeloCC.MarcacionCambioPedido;

public class MarcacionCambioPedidoDAO {
	
	public static int insertarMarcacionCambio(MarcacionCambioPedido marcCambio)
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conContact = conexion.obtenerConexionBDContact();
		String insert = "insert into marcacion_cambio_pedido (idmarcacion, idpedido, numposheader,fechapedido, total_neto_contact, total_neto_tienda) values ( " + marcCambio.getIdMarcacion() + " , " + marcCambio.getIdPedido() + " , " + marcCambio.getNumPosHeader() + " , '" + marcCambio.getFechaPedido() + "' , " + marcCambio.getTotalNetoContact() + " , " + marcCambio.getTotalNetoTienda() + ")";
		int idMarcacionCambio = 0;
		try
		{
			Statement stm= conContact .createStatement();
			stm.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = stm.getGeneratedKeys();
			if (rs.next()){
				idMarcacionCambio=rs.getInt(1);
				
	        }
			rs.close();
			stm.close();
			conContact.close();
		}catch(Exception e)
		{
			System.out.println("Error en la consulta de la tienda " + e.toString() );
		}
		System.out.println("La id Marcacion Cambio Pedido	 insertada es  " + idMarcacionCambio);
		return(idMarcacionCambio);
	}
	
	
	public static ArrayList<MarcacionCambioPedido> consultarMarcacionCambio(String fechaInferior, String fechaSuperior, int idRazon)
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conContact = conexion.obtenerConexionBDContact();
		String consulta = "select * from marcacion_cambio_pedido a, pedido b, razon_x_tienda c where a.fechapedido >= '" + fechaInferior + "' and a.fechapedido <= '" + fechaSuperior + "' and a.idpedido = b.idpedido and b.idtienda = c.idtienda and c.idrazon =" + idRazon;
		MarcacionCambioPedido marcCambio;
		ArrayList<MarcacionCambioPedido> marcacionesCambio = new ArrayList();
		try
		{
			Statement stm= conContact .createStatement();
			ResultSet rs = stm.executeQuery(consulta);
			int idMarcacionCambio;
			int idMarcacion;
			int idPedido;
			int numPosHeader;
			String fechaPedido;
			double totalNetoContact;
			double totalNetoTienda;
			if (rs.next()){
				idMarcacionCambio = rs.getInt("idmarcacion_cambio");
				idMarcacion = rs.getInt("idmarcacion");;
				idPedido = rs.getInt("idPedido");
				numPosHeader = rs.getInt("numposheader");
				fechaPedido = rs.getString("fechapedido");
				totalNetoContact = rs.getDouble("total_neto_contact");
				totalNetoTienda = rs.getDouble("total_neto_tienda");
				marcCambio = new MarcacionCambioPedido(idMarcacionCambio, idMarcacion, idPedido, numPosHeader, fechaPedido, totalNetoContact, totalNetoTienda);
				marcacionesCambio.add(marcCambio);
	        }
			rs.close();
			stm.close();
			conContact.close();
		}catch(Exception e)
		{
			System.out.println("Error en la consulta " + e.toString() );
		}
		
		return(marcacionesCambio);
	}

}
