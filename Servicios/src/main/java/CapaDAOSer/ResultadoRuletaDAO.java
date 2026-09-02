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
			String select = armarConsulta(1, fecha);
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
				resTemp.setEstadoDispersion(estadoDispersion(rs.getInt("idofertacliente")));
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
				String select = armarConsulta(0, fecha);
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
	 * Arma la consulta del reporte diario. Las dos vistas -ganadores y no
	 * ganadores- son la misma consulta y solo cambian en el valor de premio, asi
	 * que antes estaba escrita dos veces y cualquier arreglo habia que hacerlo en
	 * dos lados o quedaban distintas.
	 *
	 * De donde salen los datos del cliente. Hay dos origenes de encuesta y se
	 * comportan al contrario de lo que parece:
	 *
	 *  - Encuesta de tienda: el cliente escribe sus datos en el formulario, asi que
	 *    el correo viene en resultado_ruleta, pero casi nunca cruza con un pedido
	 *    del central.
	 *  - Encuesta directa (canales no fisicos): el formulario llega con el numero de
	 *    pedido y el cliente no escribe correo, queda el texto 'ENCUESTA-DIRECTA'.
	 *    Estos si cruzan con el central y de ahi se recupera el correo real.
	 *
	 * Antes el reporte mostraba lo que hubiera en resultado_ruleta y nada mas, asi
	 * que el 44% de las filas salia sin nombre y con 'ENCUESTA-DIRECTA' en el
	 * correo. Con los dos origenes juntos se pasa de 139 a 219 ganadores con correo
	 * util, de 240.
	 *
	 * Dos cuidados para no multiplicar filas:
	 *
	 *  1. log_encuesta_servicio tiene 586 claves (idtienda, numero_pedido)
	 *     repetidas, asi que se consulta con subconsultas escalares y no con join.
	 *  2. (idtienda, numposheader) NO es unico en pedido: hay 23 combinaciones
	 *     repetidas, una hasta 67 veces. Por eso se elige un pedido con una
	 *     subconsulta acotada a los 15 dias anteriores a la jugada, y el join va
	 *     contra idpedido, que es la llave primaria.
	 *
	 * @param esPremio 1 para ganadores, 0 para no ganadores
	 * @param fecha    fecha del reporte, formato aaaa-mm-dd
	 * @return la consulta lista
	 */
	private static String armarConsulta(int esPremio, String fecha)
	{
		return("SELECT a.idpedido, c.nombre, b.titulo, a.idofertacliente,"
			+ " IFNULL(COALESCE(NULLIF(TRIM(a.telefono),''),"
			+ "   (SELECT MAX(l.telefono) FROM log_encuesta_servicio l"
			+ "     WHERE l.numero_pedido = a.idpedido AND l.idtienda = a.idtienda),"
			+ "   NULLIF(TRIM(cl.telefono_celular),''), NULLIF(TRIM(cl.telefono),'')),'') AS telefono,"
			+ " IFNULL(COALESCE(CASE WHEN a.correo LIKE '%@%' THEN TRIM(a.correo) END,"
			+ "   CASE WHEN cl.email LIKE '%@%' THEN TRIM(cl.email) END),'') AS correo,"
			+ " IFNULL(COALESCE(NULLIF(TRIM(a.nombre_cliente),''),"
			+ "   (SELECT MAX(l.nombre_cliente) FROM log_encuesta_servicio l"
			+ "     WHERE l.numero_pedido = a.idpedido AND l.idtienda = a.idtienda),"
			+ "   NULLIF(TRIM(CONCAT(IFNULL(cl.nombre,''),' ',IFNULL(cl.apellido,''))),'')),'') AS nombre_cliente"
			+ " FROM resultado_ruleta a"
			+ " JOIN opciones_ruleta b ON b.idopcion = a.idopcion"
			+ " JOIN tienda c ON c.idtienda = a.idtienda"
			+ " LEFT JOIN pedido p ON p.idpedido = ("
			+ "   SELECT p2.idpedido FROM pedido p2"
			+ "    WHERE p2.numposheader = a.idpedido AND p2.idtienda = a.idtienda"
			+ "      AND p2.fechapedido BETWEEN DATE_SUB(DATE(a.fecha), INTERVAL 15 DAY) AND DATE(a.fecha)"
			+ "    ORDER BY p2.fechapedido DESC, p2.idpedido DESC LIMIT 1)"
			+ " LEFT JOIN cliente cl ON cl.idcliente = p.idcliente"
			+ " WHERE DATE(a.fecha) = '" + fecha + "' AND b.premio = " + esPremio);
	}

	/**
	 * Traduce el idofertacliente de resultado_ruleta al estado de entrega del
	 * premio, para que el reporte diario diga cuales quedan por dispersar en la
	 * pantalla y nadie los deje olvidados.
	 *
	 * Son tres situaciones y no dos: cero es pendiente, positivo es dispersado por
	 * la pantalla, y negativo es la marca de los que se entregaron a mano antes de
	 * que la pantalla existiera. De esos ultimos no hay codigo ni fecha de aviso,
	 * asi que decir DISPERSADO haria creer que el dato esta.
	 *
	 * @param idOfertaCliente valor de resultado_ruleta.idofertacliente
	 * @return el estado en texto
	 */
	private static String estadoDispersion(int idOfertaCliente)
	{
		if(idOfertaCliente < 0)
		{
			return("ENTREGADO A MANO");
		}
		if(idOfertaCliente > 0)
		{
			return("DISPERSADO");
		}
		return("PENDIENTE");
	}
}
