package CapaDAOServicios;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionServicios.ConexionBaseDatos;
import Modelo.InsumoDespachoTienda;


/**
 * Clase que se encarga de implementar todos aquellos métodos que tienen una interacción directa con la base de datos
 * @author JuanDavid
 *
 */
public class InsumoDespachoTiendaDAO {
	
	

	/**
	 * Método que nos permitirá validar si existe o no inventario pendiente de ingresar para la tienda en cuestión.
	 * @param idTienda
	 * @param fecha
	 * @return
	 */
public static boolean existeInsumoDespachadoTienda(int idTienda, String fecha)
{
	
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDInventarioLocal();
	boolean resultado = false;
	try
	{
		Statement stm = con1.createStatement();
		String consulta = "select * from  insumo_despacho_tienda where idtienda = " + idTienda + " and fecha_despacho = '" + fecha + "' and estado ='DESPACHADO'";
		ResultSet rs = stm.executeQuery(consulta);
		while(rs.next()){
			resultado = true;
			break;
		}
		
		stm.close();
		con1.close();
	}
	catch (Exception e){
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
 * Método que se encarga de retornar los despachos de tienda pendientes
 * @param idTienda
 * @param fecha
 * @return Un ArrayList con todos los InsusmosDespachoTienda pendientes para la tienda y la fecha.
 */
public static ArrayList<InsumoDespachoTienda> obtenerInsumoDespachoTienda(int idTienda, String fecha)
{
	
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDInventarioLocal();
	ArrayList<InsumoDespachoTienda> insumoDespachos = new ArrayList();
	try
	{
		Statement stm = con1.createStatement();
		String consulta = "select * from  insumo_despacho_tienda where idtienda = " + idTienda + " and fecha_despacho = '" + fecha + "' and estado ='DESPACHADO'";
		ResultSet rs = stm.executeQuery(consulta);
		//Variables para capturar cada despacho
		int idDespacho;
		String fechaReal;
		String observacion = "";
		InsumoDespachoTienda insTemp = new InsumoDespachoTienda(0,0,"", "", "", "");
		while(rs.next()){
			idDespacho = rs.getInt("iddespacho");
			fechaReal = rs.getString("fecha_real");
			observacion = rs.getString("observacion");
			insTemp = new InsumoDespachoTienda(idDespacho, idTienda, fecha, fechaReal, "DESPACHADO", observacion);
			insumoDespachos.add(insTemp);
		}
		
		stm.close();
		con1.close();
	}
	catch (Exception e){
		try
		{
			con1.close();
		}catch(Exception e1)
		{
		}
	}
	return(insumoDespachos);
}


/**
 * Método que se encarga de la actualización del estado de un InsumoDespachoTienda.
 * @param idDespacho
 * @param estado
 * @return Un valor booleano indicando el resultado del proceso.
 */
public static boolean cambiarEstadoInsumoDespachadoTienda(int idDespacho, String estado)
{
	
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDInventarioLocal();
	boolean resultado = false;
	try
	{
		Statement stm = con1.createStatement();
		String consulta = "update  insumo_despacho_tienda set estado = '" + estado + "' where iddespacho = " + idDespacho;
		stm.executeUpdate(consulta);
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
