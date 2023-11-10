package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.GastoConfiguracion;

public class GastoConfiguracionDAO {
	
	public static ArrayList<GastoConfiguracion> obtenerGastorConfiguracionTienda()
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventarioLocal();
		ArrayList<GastoConfiguracion> gastoConfiguraciones = new ArrayList();
		try
		{
			Statement stm = con1.createStatement();
			String select = "SELECT * FROM gasto_configuracion" ;
			int idGastoConf;
			String nombreGasto;
			String consultaSQL;
			double porcentajeGasto;
			String origen;
			ResultSet rs = stm.executeQuery(select);
			GastoConfiguracion gastConf = new GastoConfiguracion();
			while(rs.next())
			{
				idGastoConf = rs.getInt("idgasto_conf");
				nombreGasto = rs.getString("nombre_gasto");
				consultaSQL = rs.getString("consulta_sql");
				porcentajeGasto = rs.getDouble("porcentaje_gasto");
				origen = rs.getString("origen");
				gastConf = new GastoConfiguracion(idGastoConf, nombreGasto, consultaSQL, porcentajeGasto, origen);
				gastoConfiguraciones.add(gastConf);
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
		}
		return(gastoConfiguraciones);
	}
	

}
