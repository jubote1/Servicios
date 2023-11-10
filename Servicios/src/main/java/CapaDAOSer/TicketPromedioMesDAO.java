package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import org.apache.log4j.Logger;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.TicketPromedio;
import ModeloSer.TicketPromedioMes;

/**
 * Clase que implementa todos los métodos de acceso a la base de datos para la administración de la entidad Excepcion de Precio.
 * @author JuanDavid
 *
 */
public class TicketPromedioMesDAO {
	



	public static void insertarTicketPromedioMes(TicketPromedioMes ticket, boolean auditoria)
	{
		Logger logger = Logger.getLogger("log_file");
		int idLog = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDDatamartLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into ticket_promedio_mes (idtienda, mes, ano, valor, cantidad) values (" + ticket.getIdTienda() + ", " + ticket.getMes() + " , " + ticket.getAno() + " , "+ ticket.getValor() + " , " + ticket.getCantidad() + ")"; 
			if(auditoria)
			{
				logger.info(insert);
			}
			stm.executeUpdate(insert);
			stm.close();
			con1.close();
		}
		catch (Exception e){
			System.out.println(e.toString());
			logger.error(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
	}

	
}
