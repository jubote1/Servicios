package capaDAO;

import conexion.ConexionBaseDatos;
import java.sql.Connection;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.sql.ResultSet;
import org.apache.log4j.Logger;

import capaModelo.InsumoDespachadoTienda;
import capaModelo.InsumoRequeridoTienda;
import capaModelo.InsumoTienda;

public class InventarioDAO {
	
	/**
	 * Método de la capa DAO para obtener dado una tienda y un día de semana, cual es el inventario requerido de acuerdo a las dinámicas de ventas.
	 * @param idtien Se recibe como parámetro el idtienda de la cual se requiere recuperar el inventario.
	 * @param diasemana Las tiendas son surtidas dependiendo el día de la semana y de esta misma manera se determina el inventario
	 * requerido en la tienda.
	 * @return Se retorna un ArrayList con objetos tipo InsumoRequeridoTienda, de acuerdon los parámetros enviados.
	 */
	public static ArrayList<InsumoRequeridoTienda> ObtenerInsumosRequeridosTienda(int idtien, int diasemana)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		ArrayList <InsumoRequeridoTienda> insumosRequeridos = new ArrayList();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select a.idinsumo, a.idtienda, a.cantidad, b.unidad_medida, b.manejacanastas, b.cantidadxcanasta, b.nombrecontenedor, a.cantidad_minima, b.nombre_insumo from insumo_requerido_tienda a, insumo b where a.idinsumo = b.idinsumo and a.idtienda = " + idtien + " and a.diasemana =" + diasemana;
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int idinsumo;
			double cantidad, cantidadMinima;
			String unidadMedida;
			String manejacanastas;
			int cantidadxcanasta;
			String nombrecontenedor;
			String nombreInsumo;
			while(rs.next()){
				idinsumo = Integer.parseInt(rs.getString("idinsumo"));
				cantidad = Double.parseDouble(rs.getString("cantidad"));
				unidadMedida = rs.getString("unidad_medida");
				manejacanastas = rs.getString("manejacanastas");
				cantidadxcanasta = rs.getInt("cantidadxcanasta");
				nombrecontenedor = rs.getString("nombrecontenedor");
				cantidadMinima = rs.getDouble("cantidad_minima");
				nombreInsumo = rs.getString("nombre_insumo");
				InsumoRequeridoTienda insReq = new InsumoRequeridoTienda(idinsumo, idtien, cantidad, diasemana, unidadMedida, manejacanastas, cantidadxcanasta, nombrecontenedor,cantidadMinima );
				insReq.setNombreInsumo(nombreInsumo);
				insumosRequeridos.add(insReq);
			}
			rs.close();
			stm.close();
			con1.close();
		}
		catch (Exception e){
			try{
				con1.close();
				logger.error(e.toString());
			}catch(Exception e1)
			{
				logger.error(e1.toString());
			}
			
		}
		return(insumosRequeridos);
	
	}
	/**
	 * Método que se encarga de retornar los valores de insumos con los que cerró la tienda el último día.
	 * @param idtien El idtienda que denota la tienda de la cual se requiere recuperar el inventario
	 * @param fecha de la cual se requiere recuperar los valores de insumo.
	 * @return Se retorna un ArrayList con objetos de tipo InsumoTienda con los valores de uno a uno los insumos que posee la tienda
	 * al último cierre.
	 */
	public static ArrayList<InsumoTienda> ObtenerInsumosTienda(int idtien, String fecha)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		ArrayList <InsumoTienda> insumosTienda = new ArrayList();
		//Tratamiento de la fecha para consulta
		Date fechaTemporal = new Date();
		DateFormat formatoFinal = new SimpleDateFormat("yyyy-MM-dd");
		String fechaFinal ="";
		try
		{
			fechaTemporal = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
			fechaFinal = formatoFinal.format(fechaTemporal);
			
		}catch(Exception e){
			logger.error(e.toString());
			fechaFinal = fecha;
		}
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select a.idinsumo, a.cantidad, b.nombre_insumo, b.control_cantidad from insumo_tienda a, insumo b where a.idinsumo = b.idinsumo and idtienda = " + idtien + " and fecha = '" + fechaFinal + "'";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int idinsumo;
			double cantidad;
			String nombreInsumo;
			int controlCantidad;
			while(rs.next()){
				idinsumo = Integer.parseInt(rs.getString("idinsumo"));
				cantidad = Double.parseDouble(rs.getString("cantidad"));
				nombreInsumo = rs.getString("nombre_insumo");
				controlCantidad = rs.getInt("control_cantidad");
				InsumoTienda insTie = new InsumoTienda(idinsumo, idtien, cantidad, fecha, nombreInsumo, controlCantidad);
				insumosTienda.add(insTie);
			}
			rs.close();
			stm.close();
			con1.close();
		}
		catch (Exception e){
			try{
				con1.close();
				logger.error(e.toString());
			}catch(Exception e1)
			{
				logger.error(e1.toString());
			}
			
		}
		return(insumosTienda);
	
	}
	
	/**
	 * Método en la capa DAO de la cual se extrae los insumos tienda para calcular los inventarios de la tienda, se tiene un comportamiento
	 * en donde si no hay para la fecha en cuestión, se busca si hay inventarios tomados despues de cierta hora del día anterior
	 * @param idtien idtienda con el que se consultará los insumos de la tienda
	 * @param fecha fecha para la cual se van a extraer los inventarios
	 * @return Se retorna un arraylist con objetos de tipo insumo tienda, con los inventarios que tiene la tienda.
	 */
	public static ArrayList<InsumoTienda> ObtenerInsumosTiendaAutomatico(int idtien, String fecha)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		ArrayList <InsumoTienda> insumosTienda = new ArrayList();
		
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select a.idinsumo, a.cantidad, b.nombre_insumo, b.control_cantidad from insumo_tienda a, insumo b where a.idinsumo = b.idinsumo and idtienda = " + idtien + " and fecha = '" + fecha + "'";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int idinsumo;
			double cantidad;
			String nombreInsumo;
			boolean banderaFechaAnterior = true;
			int controlCantidad;
			while(rs.next()){
				banderaFechaAnterior = false;
				idinsumo = Integer.parseInt(rs.getString("idinsumo"));
				cantidad = Double.parseDouble(rs.getString("cantidad"));
				nombreInsumo = rs.getString("nombre_insumo");
				controlCantidad = rs.getInt("control_cantidad");
				InsumoTienda insTie = new InsumoTienda(idinsumo, idtien, cantidad, fecha, nombreInsumo,controlCantidad);
				insumosTienda.add(insTie);
			}
			//Se valida si no hay datos para la fecha, validaremos si hay para la fecha anterior y entre las 12 y 10 de la nohce
			//Construimos la fecha del día anterior y validamos si se obtuvo el inventario el día anteiror entre las 10:30 pm y
			// 11:59 pm y adicionalmente se valida si la fecha de actualización es del mismo día, en cuyo caso se pueden tomar los datos
			if(banderaFechaAnterior)
			{
				Date fechaActual =  new Date();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(fechaActual);
				calendar.add(Calendar.DAY_OF_YEAR, -1);
				Date fechaAnterior = calendar.getTime();
				SimpleDateFormat formatoFinal = new SimpleDateFormat("yyyy-MM-dd");
				String fechaLimiteInferior = "";
				fechaLimiteInferior = formatoFinal.format(fechaAnterior) + " 22:30:00";
				consulta = "select a.idinsumo, a.cantidad, b.nombre_insumo, b.control_cantidad from insumo_tienda a, insumo b where a.idinsumo = b.idinsumo and idtienda = " + idtien + " and fechaInsercion >= '" + fechaLimiteInferior + "'";
				System.out.println("revisando consulta contingente " + consulta );
				logger.info(consulta);
				rs = stm.executeQuery(consulta);
				while(rs.next()){
					idinsumo = Integer.parseInt(rs.getString("idinsumo"));
					cantidad = Double.parseDouble(rs.getString("cantidad"));
					nombreInsumo = rs.getString("nombre_insumo");
					controlCantidad = rs.getInt("control_cantidad");
					InsumoTienda insTie = new InsumoTienda(idinsumo, idtien, cantidad, fecha, nombreInsumo,controlCantidad);
					insumosTienda.add(insTie);
				}
			}
			rs.close();
			stm.close();
			con1.close();
		}
		catch (Exception e){
			try{
				con1.close();
				logger.error(e.toString());
			}catch(Exception e1)
			{
				logger.error(e1.toString());
			}
			
		}
		return(insumosTienda);
	
	}
	
	public static ArrayList<InsumoTienda> ConsultarInsumosTienda(int idtien)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		ArrayList <InsumoTienda> insumosTienda = new ArrayList();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select a.idinsumo, a.cantidad, b.nombre_insumo, a.fechaInsercion, b.control_cantidad from insumo_tienda a, insumo b where a.idinsumo = b.idinsumo and idtienda = " + idtien;
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int idinsumo;
			double cantidad;
			String nombreInsumo;
			String fechaInsercion;
			int controlCantidad;
			while(rs.next()){
				idinsumo = Integer.parseInt(rs.getString("idinsumo"));
				cantidad = Double.parseDouble(rs.getString("cantidad"));
				nombreInsumo = rs.getString("nombre_insumo");
				fechaInsercion = rs.getString("fechaInsercion");
				controlCantidad = rs.getInt("control_cantidad");
				InsumoTienda insTie = new InsumoTienda(idinsumo, idtien, cantidad, fechaInsercion, nombreInsumo,controlCantidad);
				insumosTienda.add(insTie);
			}
			rs.close();
			stm.close();
			con1.close();
		}
		catch (Exception e){
			try{
				con1.close();
				logger.error(e.toString());
			}catch(Exception e1)
			{
				logger.error(e1.toString());
			}
			
		}
		return(insumosTienda);
	
	}
	
	public static int InsertarDetalleInsumoDespachoTienda(int iddespacho,int idinsumo,double cantidad, String contenedor)
	{
		Logger logger = Logger.getLogger("log_file");
		int idDespachoDetalle = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
				
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into insumo_despacho_tienda_detalle (iddespacho,idinsumo, cantidad, contenedor) values (" + iddespacho + ", " + idinsumo  + ", " + cantidad +" , '" + contenedor +"' )"; 
			logger.info(insert);
			stm.executeUpdate(insert);
			ResultSet rs = stm.getGeneratedKeys();
			if (rs.next()){
				idDespachoDetalle=rs.getInt(1);
				
	        }
	        rs.close();
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
			return(0);
		}
		return(idDespachoDetalle);
	}
	
	/**
	 * Método de la capa de acceso a datos que se encarga de la inserción de despacho de pedido, teniendo en cuenta que la tabla hace las veces
	 * de encabezado del despacho de insumos para la tienda.
	 * @param idtienda Se recibe el idtienda de la tienda asociada al inventario.
	 * @param fechasurtir Fecha que determina la fecha de llevado de los insumos a la tienda
	 * @return Retorna un enterio con el iddespacho que representa como el encabezado del detalle de los insumos que se llevará a la tienda
	 */
	public static int InsertarInsumoDespachoTienda(int idtienda, String fechasurtir)
	{
		Logger logger = Logger.getLogger("log_file");
		int idDespacho = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		Date fechaTemporal = new Date();
		DateFormat formatoFinal = new SimpleDateFormat("yyyy-MM-dd");
		String fechaSurtirFinal ="";
		try
		{
			fechaTemporal = new SimpleDateFormat("dd/MM/yyyy").parse(fechasurtir);
			fechaSurtirFinal = formatoFinal.format(fechaTemporal);
			
		}catch(Exception e){
			logger.error(e.toString());
			return(0);
		}
		
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into insumo_despacho_tienda (idtienda,fecha_despacho) values (" + idtienda + ", '" + fechaSurtirFinal  + "' )"; 
			logger.info(insert);
			stm.executeUpdate(insert);
			ResultSet rs = stm.getGeneratedKeys();
			if (rs.next()){
				idDespacho=rs.getInt(1);
				System.out.println(idDespacho);
	        }
	        rs.close();
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
			return(0);
		}
		return(idDespacho);
		
	}

	public static ArrayList<InsumoDespachadoTienda> ConsultarInventariosDespachados(int idtien, String fecha)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipal();
		ArrayList <InsumoDespachadoTienda> insumosDespachadosTienda = new ArrayList();
		//Tratamiento de la fecha para consulta
		Date fechaTemporal = new Date();
		DateFormat formatoFinal = new SimpleDateFormat("yyyy-MM-dd");
		String fechaFinal ="";
		try
		{
			fechaTemporal = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
			fechaFinal = formatoFinal.format(fechaTemporal);
			
		}catch(Exception e){
			logger.error(e.toString());
			
		}
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select b.idinsumo, b.nombre_insumo, c.cantidad, c.contenedor, b.unidad_medida from insumo_despacho_tienda a, insumo b, insumo_despacho_tienda_detalle c where c.idinsumo = b.idinsumo and a.iddespacho = c.iddespacho and a.idtienda = " + idtien + " and a.fecha_despacho = '" + fechaFinal + "'";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int idinsumo;
			String nombreInsumo;
			double cantidad;
			String contenedor;
			String unidadMedida;
			while(rs.next()){
				idinsumo = Integer.parseInt(rs.getString("idinsumo"));
				cantidad = Double.parseDouble(rs.getString("cantidad"));
				nombreInsumo = rs.getString("nombre_insumo");
				contenedor = rs.getString("contenedor");
				unidadMedida = rs.getString("unidad_medida");
				InsumoDespachadoTienda insDespTie = new InsumoDespachadoTienda(idinsumo, nombreInsumo, 0, cantidad, contenedor, unidadMedida);
				insumosDespachadosTienda.add(insDespTie);
			}
			rs.close();
			stm.close();
			con1.close();
		}
		catch (Exception e){
			try{
				con1.close();
				logger.error(e.toString());
			}catch(Exception e1)
			{
				logger.error(e1.toString());
			}
			
		}
		return(insumosDespachadosTienda);
	
	}
	
}
