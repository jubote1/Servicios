package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.VarianzaResumen;


public class VarianzaResumenHistoricoDAO {
  
	 public static void insertarVarianzaResumenHistorico(VarianzaResumen varianza, int idTienda) {
		    Logger logger = Logger.getLogger("log_file");
		    int idLog = 0;
		    ConexionBaseDatos con = new ConexionBaseDatos();
		    Connection con1 = con.obtenerConexionBDDatamartLocal();
		    try {
		      Statement stm = con1.createStatement();
		      String insert = "insert into varianza_resumen_historico (iditem,fecha,inicio,retiro,ingreso,consumo,teorico_real,teorico_ingresado,varianza,idtienda) values (" + varianza.getIdItem() + " , '" + varianza.getFecha() + "' , " + varianza.getInicio() + " , " + varianza.getRetiro() + " , " + varianza.getIngreso() + " , " + varianza.getConsumo() + " , " + varianza.getTeoricoReal() + " , " + varianza.getTeoricoIngresado() + " , " + varianza.getVarianza() + " , " + idTienda + ")";
		      stm.executeUpdate(insert);
		      stm.close();
		      con1.close();
		    } catch (Exception e) {
		      logger.error(e.toString());
		      try {
		        con1.close();
		      } catch (Exception exception) {}
		    } 
		  }
	 
	 public static boolean existeVarianza(String fecha, int idTienda)
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDDatamartLocal();
		boolean resultado = false;
		try
		{
			Statement stm = con1.createStatement();
			String select = "select * from varianza_resumen_historico where fecha = '" + fecha +"' and idtienda = " + idTienda ;
			ResultSet rs = stm.executeQuery(select);
			resultado = false;
			while(rs.next())
			{
				resultado = true;
			}
			rs.close();
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
