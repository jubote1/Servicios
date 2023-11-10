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


/**
 * Clase que implementa todos los métodos de acceso a la base de datos para la administración de la entidad Excepcion de Precio.
 * @author JuanDavid
 *
 */
public class OfertaClienteDAO {
	

	public static ArrayList<OfertaCliente> obtenerOfertasNuevasSemana(String fechaSuperior, String fechaInferior)
	{
		ArrayList<OfertaCliente> ofertas = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select a.*, b.nombre_oferta from oferta_cliente a, oferta b where a.idoferta = b.idoferta and a.ingreso_oferta >=  '" + fechaInferior + "'  and a.ingreso_oferta <= '" + fechaSuperior + "'";
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
