package capaDAO;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import capaModelo.Usuario;
import conexion.ConexionBaseDatos;
import capaModelo.RazonSocial;
import capaModelo.Tienda;
import org.apache.log4j.Logger;
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
		Logger logger = Logger.getLogger("log_file");
		ArrayList<RazonSocial> razonesSociales = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPedidos();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from razon_social";
			
			logger.info(consulta);
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
			logger.info(e.toString());
			System.out.println("falle consultando tiendas");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				logger.info(e1.toString());
				System.out.println("falle consultando tiendas");
			}
		}
		return(razonesSociales);
		
	}
	
	}
