package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import ConexionSer.ConexionBaseDatos;

public class ConsumoPorcionesDAO {
	
	/**
	 * Método que se encarga de la inserción del consumo de porciones en una fecha determinada
	 * @param fecha
	 * @param idTienda
	 * @param idInsumo
	 * @param cantidad
	 * @return
	 */
	public static boolean insertarConsumoPorciones(String fecha, int idTienda,double cantidad)
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventarioLocal();
		boolean resultado = false;
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into consumo_porciones(fecha, idtienda, cantidad) values ('" + fecha +"', " + idTienda + " , " + cantidad + ")" ;
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
	
	
	/**
	 * Método que recupera la cantidad de porciones vendidas en un día determinada en una tienda determinada
	 * @param fecha
	 * @param hostBD
	 * @return
	 */
	public static int recuperarCantidadPorciones(String fecha, String hostBD)
	{
		ConexionSer.ConexionBaseDatos con = new ConexionSer.ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(hostBD);
		int cantidadPorciones = 0;
		int idInsumo;
		double consumo;
		String consulta = "SELECT SUM(cantidad) FROM producto a ,detalle_pedido d , pedido p WHERE a.idproducto = d.idproducto AND"
				+ " d.idpedidotienda = p.idpedidotienda AND  a.impresion LIKE '%Porcion%' AND d.valorunitario > 0 "
				+ "AND  p.fechapedido = '" + fecha + "'";
		Statement stm;
		ResultSet rs;
		try
		{
			stm = con1.createStatement();
			rs = stm.executeQuery(consulta);
			while(rs.next())
			{
				cantidadPorciones = rs.getInt(1);
			}
			rs.close();
			stm.close();
			con1.close();
		}
		catch (Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
				
			}catch(Exception e1)
			{
				
			}
			
		}
		return(cantidadPorciones);
		
	}
	
	/**
	 * Método que nos retorna un valor booleano para indicar si para ese día ya hay consumo de porciones.
	 * @param fecha
	 * @param idTienda
	 * @return
	 */
	public static boolean existeConsumoPorciones(String fecha, int idTienda)
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventarioLocal();
		boolean resultado = false;
		try
		{
			Statement stm = con1.createStatement();
			String select = "select * from consumo_porciones where fecha = '" + fecha +"' and idtienda = " + idTienda ;
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
