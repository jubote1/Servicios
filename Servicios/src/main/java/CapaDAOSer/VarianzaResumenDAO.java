package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.VarianzaResumen;


public class VarianzaResumenDAO {
  public static ArrayList<VarianzaResumen> consultarVarianzaResumen(String baseDatos, String fecha) {
    Logger logger = Logger.getLogger("log_file");
    int idLog = 0;
    ConexionBaseDatos con = new ConexionBaseDatos();
    Connection con1 = con.obtenerConexionBDTiendaRemota(baseDatos);
    ArrayList<VarianzaResumen> varResumen = new ArrayList();
    try {
      Statement stm = con1.createStatement();
      String select = "select * from varianza_resumen where fecha = '" + fecha + "'";
      ResultSet rs = stm.executeQuery(select);
      int idItem;
      double inicio, retiro,ingreso, consumo, valorReal, valorTeorico,varianza;
      VarianzaResumen varTemp;
      while(rs.next())
      {
    	  idItem = rs.getInt("iditem");
    	  inicio = rs.getDouble("inicio");
    	  retiro = rs.getDouble("retiro");
    	  ingreso = rs.getDouble("ingreso");
    	  consumo = rs.getDouble("consumo");
    	  valorReal = rs.getDouble("teorico_real");
    	  valorTeorico = rs.getDouble("teorico_ingresado");
    	  varianza = rs.getDouble("varianza");
    	  varTemp = new VarianzaResumen(idItem, fecha, inicio, retiro, ingreso, consumo, valorReal, valorTeorico, varianza);
    	  varResumen.add(varTemp);
      }
      rs.close();
      stm.close();
      con1.close();
    } catch (Exception e) {
      logger.error(e.toString());
      try {
        con1.close();
      } catch (Exception exception) {}
    } 
    return(varResumen);
  }
}
