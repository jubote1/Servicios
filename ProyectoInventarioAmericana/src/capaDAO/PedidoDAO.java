package capaDAO;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import capaModelo.Usuario;
import conexion.ConexionBaseDatos;
import capaModelo.ClienteFiel;
import capaModelo.Tienda;
import org.apache.log4j.Logger;
/**
 * Clase que se encarga de implementar todo lo relacionado con la base de datos de la entidad tienda.
 * @author JuanDavid
 *
 */
public class PedidoDAO {
	

	public static ArrayList<ClienteFiel> obtenerClientesFielesPedido(int diasPedido, int cantidadPedidos)
	{
		Logger logger = Logger.getLogger("log_file");
		ArrayList<ClienteFiel> clientesFieles = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPedidos();
		Date fechaTemporal = new Date();
		DateFormat formatoFinal = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fechaTemporal);
		calendar.add(Calendar.DAY_OF_YEAR, (-1* diasPedido));
		fechaTemporal = calendar.getTime();
		String fecha="";
		try
		{
			fecha = formatoFinal.format(fechaTemporal);
			
		}catch(Exception e){
			System.out.println("Problema transformando la fecha actual " + e.toString());
		}
		int idCliente;
		String nombreCliente;
		int numeroPedidos;
		String fechaMaxima;
		String fechaMinima;
		String telefono;
		String nombreTienda;
		int ofertas;
		int ofertasvigentes;
		ClienteFiel clienteInf = new ClienteFiel(0, "", 0, "", "", "", "",0,0);
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select b.idcliente, CONCAT(b.nombre ,'-',b.apellido,'-',b.nombrecompania) as nombre , b.telefono, c.nombre nombretienda, count(*) numeropedidos, max(a.fechapedido) fechamaxima, min(a.fechapedido) fechaminima,"
					+ "(select count(1) from oferta_cliente d where d.idcliente = b.idcliente ) as ofertas ," 
					+ "(select count(1) from oferta_cliente d where d.idcliente = b.idcliente and d.utilizada = 'N' ) as ofertasvigentes "
					+ " from pedido a, cliente b, tienda c where a.idcliente = b.idcliente and b.idtienda = c.idtienda and fechapedido >= '" + fecha + "' " + 
					" group by b.idcliente, b.nombre, b.telefono, c.nombre " +
					 " having count(*) > " + cantidadPedidos;
			
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				idCliente = rs.getInt("idcliente");
				nombreCliente = rs.getString("nombre");
				numeroPedidos = rs.getInt("numeropedidos");
				fechaMaxima = rs.getString("fechamaxima");
				fechaMinima = rs.getString("fechaminima");
				telefono = rs.getString("telefono");
				nombreTienda = rs.getString("nombretienda");
				ofertas = rs.getInt("ofertas");
				ofertasvigentes = rs.getInt("ofertasvigentes");
				clienteInf = new ClienteFiel(idCliente, nombreCliente, numeroPedidos, fechaMaxima, fechaMinima, telefono, nombreTienda,ofertas, ofertasvigentes);
				clientesFieles.add(clienteInf);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			logger.info(e.toString());
			System.out.println("falle consultando tiendas");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				logger.info(e1.toString());
				System.out.println("falle consultando tiendas");
			}
		}
		return(clientesFieles);
		
	}
	
	
	public static ArrayList<ClienteFiel> obtenerClientesNoFieles(int diasNoPedido, int diasNoPedidoInferior)
	{
		Logger logger = Logger.getLogger("log_file");
		ArrayList<ClienteFiel> clientesFieles = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPedidos();
		Date fechaTemporal = new Date();
		DateFormat formatoFinal = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar.setTime(fechaTemporal);
		calendar2.setTime(fechaTemporal);
		calendar.add(Calendar.DAY_OF_YEAR, (-1* diasNoPedido));
		calendar2.add(Calendar.DAY_OF_YEAR, (-1* diasNoPedidoInferior));
		fechaTemporal = calendar.getTime();
		String fecha="";
		String fechaInferior = "";
		try
		{
			fecha = formatoFinal.format(fechaTemporal);
			
		}catch(Exception e){
			System.out.println("Problema transformando la fecha final de la consulta " + e.toString());
		}
		fechaTemporal = calendar2.getTime();
		try
		{
			fechaInferior = formatoFinal.format(fechaTemporal);
			
		}catch(Exception e){
			System.out.println("Problema transformando la fecha Inferior de la consulta " + e.toString());
		}
		int idCliente;
		String nombreCliente;
		int numeroPedidos;
		String fechaMaxima;
		String fechaMinima;
		String telefono;
		String nombreTienda;
		int ofertas;
		int ofertasvigentes;
		ClienteFiel clienteInf = new ClienteFiel(0, "", 0, "", "", "", "",0,0);
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select b.idcliente, CONCAT(b.nombre ,'-',b.apellido,'-',b.nombrecompania) as nombre , b.telefono, c.nombre nombretienda, count(*) numeropedidos, max(a.fechapedido) fechamaxima, min(a.fechapedido) fechaminima, "
					+ "(select count(1) from oferta_cliente d where d.idcliente = b.idcliente ) as ofertas ," 
					+ "(select count(1) from oferta_cliente d where d.idcliente = b.idcliente and d.utilizada = 'N' ) as ofertasvigentes "
					+ " from pedido a, cliente b, tienda c where a.idcliente = b.idcliente and b.idtienda = c.idtienda " +
					" group by b.idcliente, b.nombre, b.telefono, c.nombre " +
					 " having max(fechapedido) < '" + fecha +"' and max(fechapedido) > '" + fechaInferior + "'";
			
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				idCliente = rs.getInt("idcliente");
				nombreCliente = rs.getString("nombre");
				numeroPedidos = rs.getInt("numeropedidos");
				fechaMaxima = rs.getString("fechamaxima");
				fechaMinima = rs.getString("fechaminima");
				telefono = rs.getString("telefono");
				nombreTienda = rs.getString("nombretienda");
				ofertas = rs.getInt("ofertas");
				ofertasvigentes = rs.getInt("ofertasvigentes");
				clienteInf = new ClienteFiel(idCliente, nombreCliente, numeroPedidos, fechaMaxima, fechaMinima, telefono, nombreTienda,ofertas, ofertasvigentes);
				clientesFieles.add(clienteInf);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			logger.info(e.toString());
			System.out.println("falle consultando tiendas");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				logger.info(e1.toString());
				System.out.println("falle consultando tiendas");
			}
		}
		return(clientesFieles);
		
	}
	
	
	public static ArrayList obtenerPedidosDomiciliosCOM(int idRazon,String fechaAnterior, String fechaActual)
	{
		Logger logger = Logger.getLogger("log_file");
		ArrayList pedidosDomCOM = new ArrayList();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPedidos();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select b.nombre, a.total_neto, a.fechapedido, a.idpedido, a.numposheader, f.observacion, g.idforma_pago from pedido a, tienda b, razon_x_tienda e, marcacion_pedido f, pedido_forma_pago g "
					+"where a.idtienda = b.idtienda and b.idtienda = e.idtienda and e.idrazon = " + idRazon +"  and a.idpedido = f.idpedido "
					+"and f.idmarcacion = 1 and a.fechapedido >= '" + fechaAnterior + "' and a.fechapedido <= '" + fechaActual + "' and a.idpedido = g.idpedido order by b.nombre, fechapedido";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			String[] resTemp = new String[7];
			while(rs.next()){
				resTemp = new String[7];
				resTemp[0] = rs.getString("nombre");
				resTemp[1] = rs.getString("total_neto");
				resTemp[2] = rs.getString("fechapedido");
				resTemp[3] = rs.getString("idpedido");
				resTemp[4] = rs.getString("numposheader");
				resTemp[5] = rs.getString("observacion");
				resTemp[6] = rs.getString("idforma_pago");
				pedidosDomCOM.add(resTemp);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			logger.info(e.toString());
			System.out.println("falle lanzando la consulta de domicilios.com");
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				logger.info(e1.toString());
				System.out.println("falle cerrando la conexion");
			}
		}
		return(pedidosDomCOM);
	}
	
	
}
