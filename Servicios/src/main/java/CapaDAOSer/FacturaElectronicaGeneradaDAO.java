package CapaDAOSer;

import capaConexionPOS.ConexionBaseDatos;
import capaModeloPOS.ColaFacturaElectronica;
import capaModeloPOS.FacturarElectronicaGenerada;
import capaModeloPOS.LogFacturacionElectronica;
import capaModeloPOS.Tienda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.log4j.Logger;

public class FacturaElectronicaGeneradaDAO {
	
  
		public static  boolean  actualizarLogFacturaElectronicaReproceso(int idLog, String hostBD) {
			
		    boolean respuesta = true;
		    ConexionBaseDatos con = new ConexionBaseDatos();
		    Connection con1 = con.obtenerConexionBDTiendaRemota(hostBD);
		    String sql = "update log_facturacion_electronica a  set a.mensaje_excepcion = 'FACTURA QUE RESPONDIO TIMEOUT PERO SI FUE GENERADA'  where a.idlog = ? ";
			try {
			
			   try (PreparedStatement pstmt = con1.prepareStatement(sql)) {
			       pstmt.setInt(1, idLog);
			       pstmt.executeUpdate();
				   }
				} catch (SQLException e) {
		           System.out.println(e.toString());
				   respuesta = false;
			
				} finally {
				   // Asegúrate de cerrar la conexión en un bloque finally
				   if (con1 != null) {
				       try {
				    	   con1.close();
				       } catch (SQLException e) {
				    	   respuesta = false;
				    	   System.out.println(e);
				
				       }
				   }
				} 
			    return respuesta;
			  }
}

