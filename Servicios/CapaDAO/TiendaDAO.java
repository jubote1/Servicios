package CapaDAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import Conexion.ConexionBaseDatos;
import Modelo.Tienda;

public class TiendaDAO {
	

	public static int ObtenerTienda()
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conTiendaLocal = conexion.obtenerConexionBDLocal();
		String consulta = "select idtienda from tienda ";
		int idtienda = 0;
		try
		{
			Statement stmTiendaLocal= conTiendaLocal.createStatement();
			ResultSet rsTiendaLocal = stmTiendaLocal.executeQuery(consulta);
			while(rsTiendaLocal.next())
			{
				idtienda = rsTiendaLocal.getInt("idtienda");
			}
			rsTiendaLocal.close();
			stmTiendaLocal.close();
			conTiendaLocal.close();
		}catch(Exception e)
		{
			System.out.println("Error en la consulta de la tienda " + e.toString() );
		}
		System.out.println("la tienda recuperada es " + idtienda);
		return(idtienda);
	}
	
	//Esta informaci�n se extrae del contact center
	public static int ObtenerTipoPOSTienda(int idtienda)
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conTiendaLocal = conexion.obtenerConexionBDContact();
		String consulta = "select pos from tienda where idtienda = " + idtienda;
		int pos = 0;
		try
		{
			Statement stmTiendaLocal= conTiendaLocal.createStatement();
			ResultSet rsTiendaLocal = stmTiendaLocal.executeQuery(consulta);
			while(rsTiendaLocal.next())
			{
				pos = rsTiendaLocal.getInt("pos");
			}
			rsTiendaLocal.close();
			stmTiendaLocal.close();
			conTiendaLocal.close();
		}catch(Exception e)
		{
			System.out.println("Error en la consulta de la tienda " + e.toString() );
		}
		System.out.println("la tienda recuperada es " + idtienda);
		return(pos);
	}
	
	
	public static String obtenerNombreTienda()
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conTiendaLocal = conexion.obtenerConexionBDLocal();
		String consulta = "select nombretienda from tienda ";
		String tienda = "";
		try
		{
			Statement stmTiendaLocal= conTiendaLocal.createStatement();
			ResultSet rsTiendaLocal = stmTiendaLocal.executeQuery(consulta);
			while(rsTiendaLocal.next())
			{
				tienda = rsTiendaLocal.getString("nombretienda");
			}
			rsTiendaLocal.close();
			stmTiendaLocal.close();
			conTiendaLocal.close();
		}catch(Exception e)
		{
			System.out.println("Error en la consulta de la tienda " + e.toString() );
		}
		return(tienda);
	}
	
	
	
	//ESTOS M�TODOS APUNTAN A LA TIENDA EN EL SISTEMA CONTACT CENTER
	
	/**
	 * M�todo que se encarga de retornar todas las entidades Tiendas definidas en la base de datos
	 * @return Se retorna un ArrayList con todas las entidades Tiendas definidas en la base de datos.
	 */
		public static ArrayList<Tienda> obtenerTiendas()
		{
			ArrayList<Tienda> tiendas = new ArrayList<>();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContact();
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select * from tienda";
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					int idTienda = rs.getInt("idtienda");
					String nombre = rs.getString("nombre");
					Tienda tien = new Tienda(idTienda, nombre, "","",0);
					tiendas.add(tien);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("falle consultando tiendas");
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle consultando tiendas");
				}
			}
			return(tiendas);
			
		}

}
