package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.ClienteZapier;
import ModeloSer.OfertaCliente;
import ModeloSer.ResumenOferta;


/**
 * Clase que implementa todos los métodos de acceso a la base de datos para la administración de la entidad Excepcion de Precio.
 * @author JuanDavid
 *
 */
public class OfertaClienteDAO {

	/**
	 * De donde salio la oferta, deducido de como quedo el registro.
	 *
	 * oferta_cliente no tiene una columna de origen. Pero cada camino que la
	 * llena deja una marca distinta y confiable, mas confiable que leer la
	 * "observacion" en texto libre:
	 *   - Envio de Publicidad (CampanaDAO/EnviadorPublicidadDirecta) siempre
	 *     pone idenvio.
	 *   - La ruleta de premios (RuletaCtrl) siempre pone usuario_ingreso = 'RULETA'.
	 *   - El bono de recompra (ProcesoBonoRecompra) siempre llama a cerrar()
	 *     con usuario = 'bono-nocturno'.
	 *   - Lo que no encaja en ninguna de las tres es una emision manual desde
	 *     la pantalla de Ofertas (CRUDOfertaCliente).
	 */
	private static final String SQL_ORIGEN =
			"case"
			+ " when a.idenvio is not null then 'Campaña CRM'"
			+ " when a.usuario_ingreso = 'RULETA' then 'Ruleta de premios'"
			+ " when a.usuario_ingreso = 'bono-nocturno' then 'Bono de Recompra'"
			+ " else 'Manual / otro'"
			+ " end";

	public static ArrayList<OfertaCliente> obtenerOfertasNuevasSemana(String fechaSuperior, String fechaInferior)
	{
		ArrayList<OfertaCliente> ofertas = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select a.*, b.nombre_oferta, " + SQL_ORIGEN + " as origen"
					+ " from oferta_cliente a, oferta b where a.idoferta = b.idoferta"
					+ " and a.ingreso_oferta >=  '" + fechaInferior + "'  and a.ingreso_oferta <= '" + fechaSuperior + "'"
					+ " order by a.ingreso_oferta desc";
			System.out.println(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int idOfertaCliente;
			int idOferta;
			int idCliente = 0;
			String utilizada;
			String ingresoOferta;
			String usoOferta;
			String nombreOferta = "";
			String observacion = "";
			int PQRS = 0;
			OfertaCliente ofertaTemp = new OfertaCliente(0,0,0,"", 0,"","", "");
			while(rs.next()){
				idOfertaCliente = rs.getInt("idofertacliente");
				idOferta = rs.getInt("idoferta");
				utilizada = rs.getString("utilizada");
				idCliente = rs.getInt("idcliente");
				ingresoOferta = rs.getString("ingreso_oferta");
				usoOferta = rs.getString("uso_oferta");
				nombreOferta = rs.getString("nombre_oferta");
				observacion = rs.getString("observacion");
				PQRS = rs.getInt("PQRS");
				ofertaTemp = new OfertaCliente(idOfertaCliente, idOferta, idCliente, utilizada, PQRS,ingresoOferta, usoOferta, observacion);
				ofertaTemp.setNombreOferta(nombreOferta);
				ofertaTemp.setOrigen(rs.getString("origen"));
				ofertaTemp.setValor(rs.getDouble("saldo"));
				ofertas.add(ofertaTemp);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(ofertas);

	}

	/** Cuanto se envio esta semana, agrupado por oferta y origen. */
	public static ArrayList<ResumenOferta> obtenerResumenEnviadasSemana(String fechaInferior, String fechaSuperior)
	{
		return (resumen("a.ingreso_oferta", fechaInferior, fechaSuperior, false));
	}

	/** Cuanto se redimio esta semana, agrupado por oferta y origen. */
	public static ArrayList<ResumenOferta> obtenerResumenRedimidasSemana(String fechaInferior, String fechaSuperior)
	{
		return (resumen("a.uso_oferta", fechaInferior, fechaSuperior, true));
	}

	private static ArrayList<ResumenOferta> resumen(String columnaFecha, String fechaInferior,
			String fechaSuperior, boolean soloUtilizadas)
	{
		final ArrayList<ResumenOferta> lista = new ArrayList<>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			final Statement stm = con1.createStatement();
			final String consulta = "select b.nombre_oferta, " + SQL_ORIGEN + " as origen,"
					+ " count(*) as cuantas, sum(a.saldo) as valor"
					+ " from oferta_cliente a, oferta b"
					+ " where a.idoferta = b.idoferta"
					+ (soloUtilizadas ? " and a.utilizada = 'S'" : "")
					+ " and " + columnaFecha + " >= '" + fechaInferior + "'"
					+ " and " + columnaFecha + " <= '" + fechaSuperior + "'"
					+ " group by b.nombre_oferta, origen";
			System.out.println(consulta);
			final ResultSet rs = stm.executeQuery(consulta);
			while (rs.next()) {
				final ResumenOferta r = new ResumenOferta();
				r.setNombreOferta(rs.getString("nombre_oferta"));
				r.setOrigen(rs.getString("origen"));
				if (soloUtilizadas) {
					r.setRedimidas(rs.getInt("cuantas"));
					r.setValorRedimido(rs.getDouble("valor"));
				} else {
					r.setEnviadas(rs.getInt("cuantas"));
					r.setValorEnviado(rs.getDouble("valor"));
				}
				lista.add(r);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return (lista);
	}
	
	/**
	 * Método que retornará un ArrayList con objetos de tipo oferta Cliente, con todas las ofertas redimidas dentro del  rango de fechas 
	 * enviadas como parámetro.
	 * @param fechaSuperior
	 * @param fechaInferior
	 * @return
	 */
	public static ArrayList<OfertaCliente> obtenerOfertasRedimidasSemana(String fechaSuperior, String fechaInferior)
	{
		ArrayList<OfertaCliente> ofertas = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select a.*, b.nombre_oferta from oferta_cliente a, oferta b where a.idoferta = b.idoferta and a.uso_oferta >=  '" + fechaInferior + "'  and a.uso_oferta <= '" + fechaSuperior + "'";
			System.out.println(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int idOfertaCliente;
			int idOferta;
			int idCliente = 0;
			String utilizada;
			String ingresoOferta;
			String usoOferta;
			String nombreOferta = "";
			String observacion = "";
			int PQRS = 0;
			OfertaCliente ofertaTemp = new OfertaCliente(0,0,0,"", 0,"","", "");
			while(rs.next()){
				idOfertaCliente = rs.getInt("idofertacliente");
				idOferta = rs.getInt("idoferta");
				utilizada = rs.getString("utilizada");
				idCliente = rs.getInt("idcliente");
				ingresoOferta = rs.getString("ingreso_oferta");
				usoOferta = rs.getString("uso_oferta");
				nombreOferta = rs.getString("nombre_oferta");
				observacion = rs.getString("observacion");
				PQRS = rs.getInt("PQRS");
				ofertaTemp = new OfertaCliente(idOfertaCliente, idOferta, idCliente, utilizada, PQRS,ingresoOferta, usoOferta, observacion);
				ofertaTemp.setNombreOferta(nombreOferta);
				ofertas.add(ofertaTemp);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(ofertas);
		
	}
	
	public static ArrayList consultarCodigosPromocionalesEnviados(String fecha)
	{
		ArrayList<String[]> codigosEnviados = new ArrayList();
		String consulta = "";
		consulta = "SELECT c.nombre,COUNT(*)  FROM oferta_cliente a, cliente b, tienda c WHERE a.idcliente = b.idcliente and b.idtienda = c.idtienda and a.ingreso_oferta >= '" + fecha +" 00:00:00' AND " + 
				"a.ingreso_oferta <= '" + fecha + " 23:59:00' group by c.nombre";
		ConexionBaseDatos con = new ConexionBaseDatos();
		//Llamamos metodo de conexión asumiendo que corremos en el servidor de aplicaciones de manera local
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			ResultSet rs = stm.executeQuery(consulta);

			while(rs.next())
			{
				String[] filaTemp = new String[2];
				filaTemp[0] = rs.getString(1);
				filaTemp[1] = Integer.toString(rs.getInt(2));
				codigosEnviados.add(filaTemp);
			}
			rs.close();
			stm.close();
			con1.close();

		}catch(Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
			
		}
		return(codigosEnviados);
	}
	
	public static ArrayList<ClienteZapier> obtenerClientesNotificacionZapier(int idOferta, String fechaActual)
	{
		Logger logger = Logger.getLogger("log_file");
		ArrayList<ClienteZapier> clientesZapier = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT a.codigo_promocion AS codigo, b.nombre & ' ' & b.apellido AS nombre, b.telefono AS telefono FROM oferta_cliente a, cliente b WHERE a.idcliente = b.idcliente and idoferta = " + idOferta + " AND fecha_caducidad > '" + fechaActual +"' AND utilizada = 'N' AND TIMESTAMPDIFF(DAY, '" + fechaActual + "', fecha_caducidad) <= 3;";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			String telefono;
			String nombre;
			String codigo;
			int PQRS = 0;
			ClienteZapier clienteTemp = new ClienteZapier("","", "");
			while(rs.next()){
				telefono = rs.getString("telefono");
				nombre = rs.getString("nombre");
				codigo = rs.getString("codigo");
				clienteTemp = new ClienteZapier(telefono, nombre, codigo);
				clientesZapier.add(clienteTemp);
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
		return(clientesZapier);
		
	}

}
