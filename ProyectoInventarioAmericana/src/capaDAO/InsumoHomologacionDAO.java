package capaDAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import conexion.ConexionBaseDatos;

public class InsumoHomologacionDAO {

	public static int obtenerIdInsumoIntero(int idtienda, int idinsumotienda)
	{
		Logger logger = Logger.getLogger("log_file");
		int idInsumoInterno=0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select idinsumo from insumo_homologacion_tienda where idtienda = "+ idtienda + " and idinsumotienda =  " + idinsumotienda;
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				idInsumoInterno = rs.getInt("idinsumo");
				break;
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			logger.error(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(idInsumoInterno);
	}
	
}
