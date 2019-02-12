package capaDAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import capaModelo.InsumoRequeridoTienda;
import conexion.ConexionBaseDatos;

public class InsumoRequeridoTiendaDAO {

	public static void insertarActualizarInsumReqTienda(InsumoRequeridoTienda ins)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		boolean existeInsReq = false;
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select a.* from insumo_requerido_tienda  a where a.idinsumo = " + ins.getIdinsumo() + " and a.idtienda = " + ins.getIdtienda() + " and a.diasemana = " + ins.getDiasemana() ;
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				existeInsReq = true;
				String update = "update insumo_requerido_tienda set cantidad = " + ins.getCantidad() + " , cantidad_minima = " + ins.getCantidadMinima() + " where idinsumo = " + ins.getIdinsumo() + " and idtienda = " + ins.getIdtienda() + " and diasemana =" + ins.getDiasemana() ;
				logger.info(update);
				stm.executeUpdate(update);
				break;
			}
			if(!existeInsReq)
			{
				String insert = "insert into insumo_requerido_tienda (idinsumo, idtienda, diasemana, cantidad, cantidad_minima) values (" + ins.getIdinsumo() + " , " + ins.getIdtienda() + " , " + ins.getDiasemana() + " , " + ins.getCantidad() + " , " + ins.getCantidadMinima() + ")" ;
				logger.info(insert);
				stm.executeUpdate(insert);
			}
			rs.close();
			stm.close();
			con1.close();
		}
		catch (Exception e){
			try{
				con1.close();
				logger.error(e.toString());
			}catch(Exception e1)
			{
				logger.error(e1.toString());
			}
			
		}
	}
	
}
