package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.CierreDatafonoPOS;
import ModeloSer.VarianzaResumen;


public class CierreDatafonoPOSDAO {
  
	 public static void insertarCierreDatafonoPOS(CierreDatafonoPOS cierre) {
		    Logger logger = Logger.getLogger("log_file");
		    int idLog = 0;
		    ConexionBaseDatos con = new ConexionBaseDatos();
		    Connection con1 = con.obtenerConexionBDDatamartLocal();
		    try {
		      Statement stm = con1.createStatement();
		      String insert = "insert into cierre_datafono_pos (idtienda,fecha,valor_cierre) values (" + cierre.getIdTienda() + " , '" + cierre.getFecha() + "' , " + cierre.getValorCierre() + ")";
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
}
