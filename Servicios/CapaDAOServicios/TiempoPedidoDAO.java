package CapaDAOServicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionServicios.ConexionBaseDatos;
import Modelo.TiempoPedido;


public class TiempoPedidoDAO {
	
	public static ArrayList<TiempoPedido> retornarTiemposPedidos()
	{
		ArrayList <TiempoPedido> tiemposTienda = new ArrayList();
		int tiempo = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContact();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select a.idtienda, a.tiempoentrega, b.nombre from tiempo_pedido_tienda a, tienda b where a.idtienda = b.idtienda";
			ResultSet rs = stm.executeQuery(consulta);
			int idtienda;
			int minutosPedido;
			String tienda;
			TiempoPedido tie;
			while(rs.next()){
				
				try{
					idtienda = Integer.parseInt(rs.getString("idtienda"));
					
				}catch(Exception e){
					System.out.println(e.toString());
					idtienda = 0;
				}
				try{
					minutosPedido = Integer.parseInt(rs.getString("tiempoentrega"));
					
				}catch(Exception e){
					System.out.println(e.toString());
					minutosPedido = 0;
				}
				tienda = rs.getString("nombre");
				tie = new TiempoPedido(idtienda, tienda, minutosPedido);
				tiemposTienda.add(tie);
				
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
		return(tiemposTienda);
	}
	
	public static ArrayList<TiempoPedido> retornarTiemposPedidosLocal()
	{
		ArrayList <TiempoPedido> tiemposTienda = new ArrayList();
		int tiempo = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select a.idtienda, a.tiempoentrega, b.nombre from tiempo_pedido_tienda a, tienda b where a.idtienda = b.idtienda";
			ResultSet rs = stm.executeQuery(consulta);
			int idtienda;
			int minutosPedido;
			String tienda;
			TiempoPedido tie;
			while(rs.next()){
				
				try{
					idtienda = Integer.parseInt(rs.getString("idtienda"));
					
				}catch(Exception e){
					System.out.println(e.toString());
					idtienda = 0;
				}
				try{
					minutosPedido = Integer.parseInt(rs.getString("tiempoentrega"));
					
				}catch(Exception e){
					System.out.println(e.toString());
					minutosPedido = 0;
				}
				tienda = rs.getString("nombre");
				tie = new TiempoPedido(idtienda, tienda, minutosPedido);
				tiemposTienda.add(tie);
				
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
		return(tiemposTienda);
	}
	
}
