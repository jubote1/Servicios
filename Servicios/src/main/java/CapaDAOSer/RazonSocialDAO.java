package CapaDAOSer;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.RazonSocial;


/**
 * Clase que se encarga de implementar todo lo relacionado con la base de datos de la entidad tienda.
 * @author JuanDavid
 *
 */
public class RazonSocialDAO {
	
/**
 * Método que se encarga de retornar todas las entidades Tiendas definidas en la base de datos
 * @return Se retorna un ArrayList con todas las entidades Tiendas definidas en la base de datos.
 */
	public static ArrayList<RazonSocial> obtenerTiendas()
	{
		ArrayList<RazonSocial> razonesSociales = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from razon_social";
			ResultSet rs = stm.executeQuery(consulta);
			int idRazon;
			String nombreRazon, identificacion;
			RazonSocial razSocial;
			while(rs.next()){
				idRazon = rs.getInt("idrazon");
				nombreRazon = rs.getString("nombre_razon");
				identificacion = rs.getString("identificacion");
				razSocial = new RazonSocial(idRazon,nombreRazon,identificacion);
				razonesSociales.add(razSocial);
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
		return(razonesSociales);
		
	}
	
	}
