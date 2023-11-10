package CapaDAOSer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.InsumoDespachoTienda;
import ModeloSer.InsumoDespachoTiendaDetalle;


/**
 * Clase que se encarga de implementar todos aquellos métodos que tienen una interacción directa con la base de datos
 * @author JuanDavid
 *
 */
public class InsumoDespachoTiendaDetalleDAO {
	
	
/**
 * Método para retornar el detalle de un despacho de tienda.
 * @param idDespacho
 * @return Un ArrayList con el detalle del despacho seleccionado.
 */
	public static ArrayList<InsumoDespachoTiendaDetalle> obtenerDetalleDespachoTienda(int idDespacho)
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventarioLocal();
		ArrayList<InsumoDespachoTiendaDetalle> detalleDespacho = new ArrayList();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select max(iddespacho_detalle) AS iddespacho_detalle,idinsumo, sum(cantidad) AS cantidad from  insumo_despacho_tienda_detalle where iddespacho = " + idDespacho + " GROUP BY idinsumo";
			ResultSet rs = stm.executeQuery(consulta);
			//Variables para capturar cada despacho
			int idDespachoDetalle;
			int idInsumo;
			double cantidad;
			InsumoDespachoTiendaDetalle insDetalleTemp = new InsumoDespachoTiendaDetalle(0,0,0, 0);
			while(rs.next()){
				idDespachoDetalle = rs.getInt("iddespacho_detalle");
				idInsumo = rs.getInt("idinsumo");
				cantidad = rs.getDouble("cantidad");
				insDetalleTemp = new InsumoDespachoTiendaDetalle(idDespacho, idDespachoDetalle, idInsumo, cantidad);
				detalleDespacho.add(insDetalleTemp);	
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
		return(detalleDespacho);
	}
	
	/**
	 * Método que se encarga de realizar la inserción de un detalle de insumo en el sistema de inventarios
	 * @param iddespacho
	 * @param idinsumo
	 * @param cantidad
	 * @param contenedor
	 * @return
	 */
	public static int InsertarDetalleInsumoDespachoTienda(int iddespacho,int idinsumo,double cantidad, String contenedor)
	{
		int idDespachoDetalle = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventarioLocal();
				
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into insumo_despacho_tienda_detalle (iddespacho,idinsumo, cantidad, contenedor) values (" + iddespacho + ", " + idinsumo  + ", " + cantidad +" , '" + contenedor +"' )"; 
			stm.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = stm.getGeneratedKeys();
			if (rs.next()){
				idDespachoDetalle=rs.getInt(1);
				
	        }
	        rs.close();
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
			return(0);
		}
		return(idDespachoDetalle);
	}
	
	
	
}
