package conexion;
import java.sql.*;
/**
 * Clase que se encarga de la implementación de la conexión a las bases de datos del sistema contact center y la
 * base de datos de cada tienda.
 * @author JuanDavid
 *
 */
public class ConexionBaseDatos {
	
	
	
	public static void main(String args[]){
		
		ConexionBaseDatos cn = new ConexionBaseDatos();
		cn.obtenerConexionBDTienda("PixelSqlbase");
	}

	
	
	public Connection obtenerConexionBDGeneral(){
		try {
		    Class.forName("com.mysql.jdbc.Driver").newInstance();
		    

		} catch (Exception e) {

		    System.out.println(e.toString());

		}
		
		Connection con = null;
		//...

		try {

//			con = DriverManager.getConnection(
//		            "jdbc:mysql://localhost/general?"
//		            + "user=root&password=4m32017");
			
			con = DriverManager.getConnection(
		            "jdbc:mysql://192.168.0.25/general?"
		            + "user=root&password=4m32017");

		    // Otros y operaciones sobre la base de datos...

		} catch (SQLException ex) {

		    // Mantener el control sobre el tipo de error
		    System.out.println("SQLException: " + ex.getMessage());

		}
		return(con);
	}
	/**
	 * Método que implementa la conexión a la base de datos del sistema principal de contact center
	 * @return
	 */
	public Connection obtenerConexionBDPrincipal(){
		try {
			/**
			 * Se realiza el registro del drive de Mysql
			 */
		    Class.forName("com.mysql.jdbc.Driver").newInstance();
		    

		} catch (Exception e) {

		    System.out.println(e.toString());

		}
		
		Connection con = null;
		//...

		try {

			
			/**
			 * Se realiza la creación de la conexión a la base de datos
			 */
			//con = DriverManager.getConnection(
		    //        "jdbc:mysql://192.168.0.25/inventarioamericana?"
		    //        + "user=root&password=4m32017");
			
			con = DriverManager.getConnection(
		            "jdbc:mysql://192.168.0.25/inventarioamericana?"
		            + "user=root&password=4m32017");

		    // Otros y operaciones sobre la base de datos...

		} catch (SQLException ex) {

		    // Mantener el control sobre el tipo de error
		    System.out.println("SQLException: " + ex.getMessage());

		}
		return(con);
	}
	
	public Connection obtenerConexionBDPedidos(){
		try {
			/**
			 * Se realiza el registro del drive de Mysql
			 */
		    Class.forName("com.mysql.jdbc.Driver").newInstance();
		    

		} catch (Exception e) {

		    System.out.println(e.toString());

		}
		
		Connection con = null;
		//...

		try {

			
//			con = DriverManager.getConnection(
//		            "jdbc:mysql://localhost/pizzaamericana?"
//		            + "user=root&password=4m32017");
			
			con = DriverManager.getConnection(
		            "jdbc:mysql://192.168.0.25/pizzaamericana?"
		            + "user=root&password=4m32017");

		    // Otros y operaciones sobre la base de datos...

		} catch (SQLException ex) {

		    // Mantener el control sobre el tipo de error
		    System.out.println("SQLException: " + ex.getMessage());

		}
		return(con);
	}
	
	
	/**
	 * Método que se encarga de implementar la conexion a la base de datos de cada teinda
	 * @param dsn Recibe como parámetro el valor del Datasource Name
	 * @return Se retorna un objeto de la clase conexión.
	 */
	public Connection obtenerConexionBDTienda(String dsn){
		
		Connection con = null;
		try {

			 //Class.forName("sybase.jdbc.sqlanywhere.IDriver");
			 //con = DriverManager.getConnection("jdbc:sqlanywhere:dsn="+dsn+";uid=admin;pwd=xxx");//SystemPos
			
			/**
			 * Cambiamos para la versión 12 del driver en teoria no es necesario registrar el driver lo comentamos
			 */
			DriverManager.registerDriver( (Driver)
					 Class.forName( "sybase.jdbc.sqlanywhere.IDriver" ).newInstance() );
			
			/**
			 * Se crea el ojbeto conexión para sqlanyhwere
			 */
			//con = DriverManager.getConnection("jdbc:sqlanywhere:dsn="+dsn+";uid=admin;pwd=xxx");//SystemPos
			con = DriverManager.getConnection("jdbc:sqlanywhere:uid=admin;pwd=xxx;eng=PixelSqlbase;database=PixelSqlbase;links=tcpip(host=192.168.1.80;port=2638)");//SystemPos
			
		} catch (Exception ex) {

		    // Mantener el control sobre el tipo de error
		    System.out.println("SQLException: " + ex.getMessage());

		}
		return(con); 
	}
	
}
