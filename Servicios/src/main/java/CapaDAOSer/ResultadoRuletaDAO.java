package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import org.apache.log4j.Logger;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.ResultadoRuleta;
import ModeloSer.TicketPromedio;


public class ResultadoRuletaDAO {
	

	public static int cantidadEncuestasServicioEnviadas(String fecha, boolean auditoria)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		int cantidad = 0;
		try
		{
			Statement stm = con1.createStatement();
			String select = "SELECT COUNT(*) FROM log_encuesta_servicio WHERE hora_envio >= '"+fecha+" 00:00:00' AND hora_envio <= '"+fecha+" 23:59:59'"; 
			if(auditoria)
			{
				logger.info(select);
			}
			ResultSet rs = stm.executeQuery(select);
			while(rs.next())
			{
				cantidad = rs.getInt(1);
			}
			stm.close();
			con1.close();
		}
		catch (Exception e){
			logger.error(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(cantidad);
	}


/**
 * Método que retorna un ArrayList con los ganadores de premios de una fecha determinada
 * @param fecha
 * @param auditoria
 * @return
 */
	public static ArrayList<ResultadoRuleta> obtenerResultadoDiarioRuletaGanadores(String fecha, boolean auditoria)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		ArrayList<ResultadoRuleta> resultados = new ArrayList();
		ResultadoRuleta resTemp;
		int idPedido;
		String tienda;
		String premio;
		String telefono;
		String correo;
		String nombreCliente;
		try
		{
			Statement stm = con1.createStatement();
			String select = "SELECT a.idpedido, c.nombre, b.titulo, a.telefono, a.correo, a.nombre_cliente  FROM resultado_ruleta a, opciones_ruleta b, tienda c  WHERE  DATE(a.fecha) = '" + fecha + "' and a.idopcion = b.idopcion AND premio = 1 AND a.idtienda = c.idtienda"; 
			if(auditoria)
			{
				logger.info(select);
			}
			ResultSet rs = stm.executeQuery(select);
			while(rs.next())
			{
				idPedido = rs.getInt("idpedido");
				tienda = rs.getString("nombre");
				premio = rs.getString("titulo");
				telefono = rs.getString("telefono");
				correo = rs.getString("correo");
				nombreCliente = rs.getString("nombre_cliente");
				resTemp = new ResultadoRuleta(idPedido, tienda, premio, telefono,correo, nombreCliente);
				resultados.add(resTemp);
			}
			stm.close();
			con1.close();
		}
		catch (Exception e){
			logger.error(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(resultados);
	}

	/**
	 * Método que retorna un ArrayList con los no ganadores de premios de una fecha determinada
	 * @param fecha
	 * @param auditoria
	 * @return
	 */
		public static ArrayList<ResultadoRuleta> obtenerResultadoDiarioRuletaNoGanadores(String fecha, boolean auditoria)
		{
			Logger logger = Logger.getLogger("log_file");
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			ArrayList<ResultadoRuleta> resultados = new ArrayList();
			ResultadoRuleta resTemp;
			int idPedido;
			String tienda;
			String premio;
			String telefono;
			String correo;
			String nombreCliente;
			try
			{
				Statement stm = con1.createStatement();
				String select = "SELECT a.idpedido, c.nombre, b.titulo, a.telefono, a.correo, a.nombre_cliente  FROM resultado_ruleta a, opciones_ruleta b, tienda c  WHERE  DATE(a.fecha) = '" + fecha + "' and a.idopcion = b.idopcion AND premio = 0 AND a.idtienda = c.idtienda"; 
				if(auditoria)
				{
					logger.info(select);
				}
				ResultSet rs = stm.executeQuery(select);
				while(rs.next())
				{
					idPedido = rs.getInt("idpedido");
					tienda = rs.getString("nombre");
					premio = rs.getString("titulo");
					telefono = rs.getString("telefono");
					correo = rs.getString("correo");
					nombreCliente = rs.getString("nombre_cliente");
					resTemp = new ResultadoRuleta(idPedido, tienda, premio, telefono,correo, nombreCliente);
					resultados.add(resTemp);
				}
				stm.close();
				con1.close();
			}
			catch (Exception e){
				logger.error(e.toString());
				try
				{
					con1.close();
				}catch(Exception e1)
				{
				}
			}
			return(resultados);
		}
	
}
