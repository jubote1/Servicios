package capaDAO;
import capaModelo.Usuario;

import conexion.ConexionBaseDatos;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import org.apache.log4j.Logger;
/**
 * Clase que se encarga de implementar toda la interacci�n con la base de datos para la entidad Usuario.
 * @author JuanDavid
 *
 */
public class UsuarioDAO {

	/**
	 * M�todo que se encarga de validar la existencia y de un usuario y su contrase�a en la base de datos.
	 * @param usuario Se recibe como par�metro un objeto MOdelo Usuario, el cual trae la informaci�n base para la validaci�n,
	 * autenticaci�n del usuario.
	 * @return Se retorna un valor booleano que indica si el proceso de autenticaci�n es satifactorio o no.
	 */
	public static boolean validarUsuario(Usuario usuario)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select count(*) from usuario where nombre = '" + usuario.getNombreUsuario() + "' and password = '" + usuario.getContrasena()+"'";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				int cantidad=0;
				try{
					cantidad = Integer.parseInt(rs.getString(1));
					if (cantidad > 0){
						return(true);
					}
				}catch(Exception e){
					logger.error(e.toString());
					return(false);
				}
				rs.close();
				stm.close();
				con1.close();
			}
		}catch (Exception e){
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(false);
		
	}
	
	/**
	 * M�todo que se encarga de validar si un usuario existe o no en la base de datos
	 * @param usuario Recibe como par�metro un objeto Modelo Usuario con base en el cual se realiza la consulta.
	 * @return Se retorna un valor booleano con base en el cual se realiza la validaci�n del usuario en base de datos
	 * 
	 */
	public static String validarAutenticacion(Usuario usuario)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		String resultado = "";
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select administrador from usuario where nombre = '" + usuario.getNombreUsuario() + "'";
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				
				try{
					resultado = rs.getString(1);
					
				}catch(Exception e){
					
					
				}
				rs.close();
				stm.close();
				con1.close();
			}
		}catch (Exception e){
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
