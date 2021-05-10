package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.cj.jdbc.result.ResultSetMetaData;

import ConexionSer.ConexionBaseDatos;

public class ReporteContactCenterDAO {
	
	public static int obtenerCantidadPedidos(String fechaAnterior, String fechaActual)
	{
		int cantidadPedidos = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT COUNT(*) FROM pedido a where  a.fechapedido >= '" + fechaAnterior + "' and a.fechapedido <= '" + fechaActual + "' and a.enviadopixel = 1 and a.origen = 'C'";
			ResultSet rs = stm.executeQuery(consulta);
			System.out.println(consulta);
			while(rs.next()){
				cantidadPedidos = rs.getInt(1);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			cantidadPedidos = 0;
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(cantidadPedidos);
	}
	
	public static int obtenerCantidadPedidosContact(String fechaAnterior, String fechaActual)
	{
		int cantidadPedidos = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT COUNT(*) FROM pedido a where  a.fechapedido >= '" + fechaAnterior + "' and a.fechapedido < '" + fechaActual + "' and a.enviadopixel = 1 and a.origen = 'C'";
			ResultSet rs = stm.executeQuery(consulta);
			System.out.println(consulta);
			while(rs.next()){
				cantidadPedidos = rs.getInt(1);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			cantidadPedidos = 0;
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(cantidadPedidos);
	}
	
	public static int obtenerCantidadProductoContact(String fechaAnterior, String fechaActual, int idProducto)
	{
		int cantidadPedidos = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT COUNT(*) FROM pedido a, detalle_pedido b where a.idpedido = b.idpedido and a.fechapedido >= '" + fechaAnterior + "' and a.fechapedido < '" + fechaActual + "' and a.enviadopixel = 1 and a.origen = 'C' and b.idproducto = " + idProducto;
			ResultSet rs = stm.executeQuery(consulta);
			System.out.println(consulta);
			while(rs.next()){
				cantidadPedidos = rs.getInt(1);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			cantidadPedidos = 0;
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(cantidadPedidos);
	}
	
	public static int obtenerCantidadPedidosVirtual(String fechaAnterior, String fechaActual)
	{
		int cantidadPedidos = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT COUNT(*) FROM pedido a where  a.fechapedido >= '" + fechaAnterior + "' and a.fechapedido < '" + fechaActual + "' and a.enviadopixel = 1 and a.origen IN ('T','TK')";
			ResultSet rs = stm.executeQuery(consulta);
			System.out.println(consulta);
			while(rs.next()){
				cantidadPedidos = rs.getInt(1);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			cantidadPedidos = 0;
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(cantidadPedidos);
	}
	
	public static ArrayList obtenerPedidosVirtualPorUsuario(String fechaAnterior, String fechaActual)
	{
		ArrayList pedidosUsuario = new ArrayList();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT COUNT(*), usuarioreenvio FROM pedido a where  a.fechapedido >= '" + fechaAnterior + "' and a.fechapedido < '" + fechaActual + "' and a.enviadopixel = 1 and a.origen = 'T' AND numposheader > 0 GROUP BY usuarioreenvio";
			ResultSet rs = stm.executeQuery(consulta);
			System.out.println(consulta);
			ResultSetMetaData rsMd = (ResultSetMetaData) rs.getMetaData();
			int numeroColumnas = rsMd.getColumnCount();
			while(rs.next()){
				String [] fila = new String[numeroColumnas];
				for(int y = 0; y < numeroColumnas; y++)
				{
					fila[y] = rs.getString(y+1);
				}
				pedidosUsuario.add(fila);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(pedidosUsuario);
	}
	
	
	public static int obtenerPedidosVirtualTotalDia(String fecha)
	{
		int cantidadPedidos = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT COUNT(*) FROM pedido a where  a.fechapedido = '" + fecha + "' and a.enviadopixel = 1 and a.origen = 'T' AND numposheader > 0";
			ResultSet rs = stm.executeQuery(consulta);
			System.out.println(consulta);
			while(rs.next()){
				cantidadPedidos = rs.getInt(1);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(cantidadPedidos);
	}
	
	public static int obtenerPedidosVirtualNuevaTotalDia(String fecha)
	{
		int cantidadPedidos = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT COUNT(*) FROM pedido a where  a.fechapedido = '" + fecha + "' and a.enviadopixel = 1 and a.origen = 'TK' AND numposheader > 0";
			ResultSet rs = stm.executeQuery(consulta);
			System.out.println(consulta);
			while(rs.next()){
				cantidadPedidos = rs.getInt(1);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(cantidadPedidos);
	}
	
	/**
	 * Método que se encarga de traer los pedidos totales de tienda virtual para una fecha y una tienda determinada
	 * @param fecha
	 * @param idTienda
	 * @return
	 */
	public static int obtenerPedidosVirtualTotalDiaTienda(String fecha, int idTienda)
	{
		int cantidadPedidos = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT COUNT(*) FROM pedido a where  a.fechapedido = '" + fecha + "' and a.enviadopixel = 1 and a.origen = 'TK' AND numposheader > 0 and a.idtienda = " + idTienda;
			ResultSet rs = stm.executeQuery(consulta);
			System.out.println(consulta);
			while(rs.next()){
				cantidadPedidos = rs.getInt(1);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(cantidadPedidos);
	}
	
	
	/**
	 * Método que se encarga de obtener el total de pedidos por canales no físicos para una tienda y una fechad determinado
	 * @param fecha
	 * @param idTienda
	 * @return
	 */
	public static int obtenerPedidosNoFisicosTotalDiaTienda(String fecha, int idTienda)
	{
		int cantidadPedidos = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT COUNT(*) FROM pedido a where  a.fechapedido = '" + fecha + "' and a.enviadopixel = 1 AND numposheader > 0 and a.idtienda = " + idTienda;
			ResultSet rs = stm.executeQuery(consulta);
			System.out.println(consulta);
			while(rs.next()){
				cantidadPedidos = rs.getInt(1);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(cantidadPedidos);
	}
	
	public static double obtenerPromedioVirtualTotalDia(String fecha)
	{
		double promedio = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT AVG(TIMESTAMPDIFF(MINUTE, a.fechainsercion, a.fechaenviotienda)) FROM pedido a where  a.fechapedido = '" + fecha + "' and a.enviadopixel = 1 and a.origen = 'T' AND numposheader > 0";
			ResultSet rs = stm.executeQuery(consulta);
			System.out.println(consulta);
			while(rs.next()){
				promedio = rs.getInt(1);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(promedio);
	}
	
	
	
	public static ArrayList obtenerPedidosUsuario(String fechaAnterior, String fechaActual)
	{
		ArrayList pedidosUsuario = new ArrayList();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT b.nombre_largo, count(*), b.teletrabajador FROM pedido a, usuario b where a.usuariopedido = b.nombre and a.fechapedido >= '" + fechaAnterior + "' and a.fechapedido <= '" + fechaActual + "' group by b.nombre_largo, b.teletrabajador";
			ResultSet rs = stm.executeQuery(consulta);
			System.out.println(consulta);
			ResultSetMetaData rsMd = (ResultSetMetaData) rs.getMetaData();
			int numeroColumnas = rsMd.getColumnCount();
			while(rs.next()){
				String [] fila = new String[numeroColumnas];
				for(int y = 0; y < numeroColumnas; y++)
				{
					fila[y] = rs.getString(y+1);
				}
				pedidosUsuario.add(fila);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(pedidosUsuario);
	}
	
	
	public static ArrayList obtenerPedidosDia(String fechaAnterior, String fechaActual)
	{
		ArrayList pedidosUsuario = new ArrayList();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT a.fechapedido, count(*) FROM pedido a where a.fechapedido >= '" + fechaAnterior + "' and a.fechapedido <= '" + fechaActual + "' group by a.fechapedido order by a.fechapedido";
			ResultSet rs = stm.executeQuery(consulta);
			System.out.println(consulta);
			ResultSetMetaData rsMd = (ResultSetMetaData) rs.getMetaData();
			int numeroColumnas = rsMd.getColumnCount();
			while(rs.next()){
				String [] fila = new String[numeroColumnas];
				for(int y = 0; y < numeroColumnas; y++)
				{
					fila[y] = rs.getString(y+1);
				}
				pedidosUsuario.add(fila);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center" + e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(pedidosUsuario);
	}
	
	
	public static ArrayList obtenerPedidosDiaHora(String fechaAnterior, String fechaActual)
	{
		ArrayList pedidosUsuario = new ArrayList();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT a.fechapedido, substr(a.fechainsercion, 12, 2), count(*) FROM pedido a where a.fechapedido >= '" + fechaAnterior + "' and a.fechapedido <= '" + fechaActual + "' group by a.fechapedido, substr(a.fechainsercion, 12, 2) order by a.fechapedido";
			System.out.println(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			ResultSetMetaData rsMd = (ResultSetMetaData) rs.getMetaData();
			int numeroColumnas = rsMd.getColumnCount();
			while(rs.next()){
				String [] fila = new String[numeroColumnas];
				for(int y = 0; y < numeroColumnas; y++)
				{
					fila[y] = rs.getString(y+1);
				}
				pedidosUsuario.add(fila);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center " + e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(pedidosUsuario);
	}
	
	/**
	 * Esta consulta nos trae la cantidad de pedidos por mes de los últimos meses(18 aproximadamente)
	 * @return
	 */
	public static ArrayList obtenerCantidadPedidosMes()
	{
		ArrayList pedidosUsuario = new ArrayList();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT substr(a.fechainsercion, 1, 7), count(*) FROM pedido a group by substr(a.fechainsercion, 1, 7) order by substr(a.fechainsercion, 1, 7) desc";
			System.out.println(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			ResultSetMetaData rsMd = (ResultSetMetaData) rs.getMetaData();
			int numeroColumnas = rsMd.getColumnCount();
			while(rs.next()){
				String [] fila = new String[numeroColumnas];
				for(int y = 0; y < numeroColumnas; y++)
				{
					fila[y] = rs.getString(y+1);
				}
				pedidosUsuario.add(fila);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println("falle lanzando la consulta estadísticas pedidos contact center " + e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(pedidosUsuario);
	}

}
