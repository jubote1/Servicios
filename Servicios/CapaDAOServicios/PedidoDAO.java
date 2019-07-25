package CapaDAOServicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import com.mysql.jdbc.ResultSetMetaData;
import CapaDAOServicios.TiendaDAO;
import ConexionServicios.ConexionBaseDatos;
import Modelo.Pedido;
import Modelo.Tienda;

public class PedidoDAO {
	
	/**
	 * Método que permite la consulta de pedidos de acuerdo a los parámetros enviados para la consulta, esta consulta es exclusiva para los 
	 * productos que son registrados dentro del sistema contact center.
	 * @param fechainicial Fecha inicial de los pedidos a consultar.
	 * @param fechafinal Fecha final de los pedidos a consultar.
	 * @param tienda nombre de la tienda que se desea filtrar para la consulta de los pedidos.
	 * @param numeropedido En caso de desearlo se puede filtrar por un número de pedido en específico.
	 * @return Se retorna un ArrayList con objetos de tipo pedido con la información de los pedidos consultados.
	 */
	public static ArrayList<Pedido> ConsultaIntegradaPedidos(int idTienda)
	{
		ArrayList <Pedido> consultaPedidos = new ArrayList();
		String consulta = "";
		String fechaPed = "";
		Date fechaTemporal = new Date();
		DateFormat formatoFinal = new SimpleDateFormat("yyyy-MM-dd");
		fechaPed = formatoFinal.format(fechaTemporal);
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente,"
				+ " c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, "
				+ "a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, "
				+ "e.nombre formapago, e.idforma_pago, a.tiempopedido from pedido a, tienda b, cliente c, "
				+ "estado_pedido d, forma_pago e, pedido_forma_pago f "
				+ "where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido "
				+ "and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido =  '" + fechaPed +"' "
						+ " and a.idtienda =" + idTienda ;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContact();
		System.out.println(" consulta contact " + consulta);
		try
		{
			Statement stm = con1.createStatement();
			ResultSet rs = stm.executeQuery(consulta);
			int idpedido;
			String nombreTienda;
			double totalBruto;
			double impuesto;
			double totalNeto;
			String nombreCliente;
			String estadoPedido;
			String fechaPedido;
			int idcliente;
			int enviadopixel;
			int numposheader;
			String url;
			String stringpixel;
			String fechainsercion;
			String usuariopedido;
			String telefono;
			String direccion;
			String formapago;
			int idformapago;
			double tiempopedido;
			while(rs.next())
			{
				idpedido = rs.getInt("idpedido");
				nombreTienda = rs.getString("nombre");
				totalBruto = rs.getDouble("total_bruto");
				impuesto = rs.getDouble("impuesto");
				totalNeto = rs.getDouble("total_neto");
				nombreCliente = rs.getString("nombrecliente");
				estadoPedido = rs.getString("descripcion");
				fechaPedido = rs.getString("fechapedido");
				idcliente = rs.getInt("idcliente");
				enviadopixel = rs.getInt("enviadopixel");
				numposheader = rs.getInt("numposheader");
				stringpixel = rs.getString("stringpixel");
				fechainsercion = rs.getString("fechainsercion");
				usuariopedido = rs.getString("usuariopedido");
				direccion = rs.getString("direccion");
				telefono = rs.getString("telefono");
				url = rs.getString("url");
				formapago = rs.getString("formapago");
				idformapago = rs.getInt("idforma_pago");
				tiempopedido = rs.getDouble("tiempopedido");
				Tienda tiendapedido = new Tienda(idTienda, nombreTienda, "", url, 0);
				Pedido cadaPedido = new Pedido(idpedido,  nombreTienda,totalBruto, impuesto, totalNeto,
						estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, tiendapedido, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido);
				cadaPedido.setIdtienda(idTienda);
				consultaPedidos.add(cadaPedido);
			}
			rs.close();
			stm.close();
			con1.close();

		}catch(Exception e){
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
			
		}
		return(consultaPedidos);
	}
	
	public static ArrayList<Pedido> ConsultaDomiciosCOMSemana(int idTienda, int idMarcador)
	{
		String fechaActual = "";
		//Variables donde manejaremos la fecha anerior con el fin realizar los cálculos de ventas
		Date datFechaAnterior;
		String fechaAnterior = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		//Obtenemos la fecha Actual
		try
		{
			//OJO
			fechaActual = dateFormat.format(calendarioActual.getTime());
			//fechaActual = "2019-01-20";
		}catch(Exception exc)
		{
			System.out.println(exc.toString());
		}
		try
		{
			//Al objeto calendario le fijamos la fecha actual del sitema
			calendarioActual.setTime(dateFormat.parse(fechaActual));
			
		}catch(Exception e)
		{
			System.out.println(e.toString());
		}
		//Retormanos el día de la semana actual segun la fecha del calendario
		//OJO
		//int diaActual = 1;
		int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
		
		//Domingo
		if(diaActual == 1)
		{
			calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
		}
		else if(diaActual == 2)
		{
			calendarioActual.add(Calendar.DAY_OF_YEAR, -7);
			//Si es lunes no se hace nada
		}
		else if(diaActual == 3)
		{
			//Si es martes se resta uno solo
			calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
		}
		else if(diaActual == 4)
		{
			//Si es miercoles se resta dos
			calendarioActual.add(Calendar.DAY_OF_YEAR, -2);
		}
		else if(diaActual == 5)
		{
			//Si es jueves se resta tres
			calendarioActual.add(Calendar.DAY_OF_YEAR, -3);
		}
		else if(diaActual == 6)
		{
			//Si es viernes se resta cuatro
			calendarioActual.add(Calendar.DAY_OF_YEAR, -4);
		}
		else if(diaActual == 7)
		{
			//Si es sabado se resta cinco
			calendarioActual.add(Calendar.DAY_OF_YEAR, -5);
		}
		//Llevamos a un string la fecha anterior para el cálculo de la venta
		datFechaAnterior = calendarioActual.getTime();
		fechaAnterior = dateFormat.format(datFechaAnterior);
		//Teniendo el rango de rechas ejecutamos la consulta que se encargará de retornar los pedidos con 
		//estas características de domicilios.com
		
		//Luego de construidas las fechas realizamos limpieza de las tablas de anulación y cambio para las fechas en cuestión
		
		ArrayList <Pedido> consultaPedidos = new ArrayList();
		String consulta = "";
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente,"
				+ " c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, "
				+ "a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, "
				+ "e.nombre formapago, e.idforma_pago, a.tiempopedido, g.observacion from pedido a, tienda b, cliente c, "
				+ "estado_pedido d, forma_pago e, pedido_forma_pago f, marcacion_pedido g "
				+ "where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido "
				+ "and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido >=  '" + fechaAnterior +"' and a.fechapedido <=  '"+ fechaActual +"'"
						+ " and a.idtienda =" + idTienda + " and a.idpedido = g.idpedido and g.idmarcacion = " + idMarcador;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContact();
		try
		{
			Statement stm = con1.createStatement();
			ResultSet rs = stm.executeQuery(consulta);
			int idpedido;
			String nombreTienda;
			double totalBruto;
			double impuesto;
			double totalNeto;
			String nombreCliente;
			String estadoPedido;
			String fechaPedido;
			int idcliente;
			int enviadopixel;
			int numposheader;
			String url;
			String stringpixel;
			String fechainsercion;
			String usuariopedido;
			String telefono;
			String direccion;
			String formapago;
			int idformapago;
			double tiempopedido;
			String obsMarcacion;
			while(rs.next())
			{
				idpedido = rs.getInt("idpedido");
				nombreTienda = rs.getString("nombre");
				totalBruto = rs.getDouble("total_bruto");
				impuesto = rs.getDouble("impuesto");
				totalNeto = rs.getDouble("total_neto");
				nombreCliente = rs.getString("nombrecliente");
				estadoPedido = rs.getString("descripcion");
				fechaPedido = rs.getString("fechapedido");
				idcliente = rs.getInt("idcliente");
				enviadopixel = rs.getInt("enviadopixel");
				numposheader = rs.getInt("numposheader");
				stringpixel = rs.getString("stringpixel");
				fechainsercion = rs.getString("fechainsercion");
				usuariopedido = rs.getString("usuariopedido");
				direccion = rs.getString("direccion");
				telefono = rs.getString("telefono");
				url = rs.getString("url");
				formapago = rs.getString("formapago");
				idformapago = rs.getInt("idforma_pago");
				tiempopedido = rs.getDouble("tiempopedido");
				obsMarcacion = rs.getString("observacion");
				Tienda tiendapedido = new Tienda(idTienda, nombreTienda, "", url, 0);
				Pedido cadaPedido = new Pedido(idpedido,  nombreTienda,totalBruto, impuesto, totalNeto,
						estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, tiendapedido, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido);
				cadaPedido.setObsMarcacion(obsMarcacion);
				consultaPedidos.add(cadaPedido);
			}
			rs.close();
			stm.close();
			con1.close();

		}catch(Exception e){
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
			
		}
		return(consultaPedidos);
	}
	
	/**
	 * Método que se encargará de consultar los pedidos pendientes dada una fecha determinada, con el fin de alertar posteriormente en correo electrónico
	 * @param fechaPed
	 * @return
	 */
	public static ArrayList<Pedido> ConsultarPedidosPendientes(String fechaPed)
	{
		ArrayList <Pedido> consultaPedidos = new ArrayList();
		int idtienda = 0;
		String consulta = "";
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 2 and a.enviadopixel = 0 AND TIMESTAMPDIFF(MINUTE, a.fechainsercion, NOW()) > 5 and b.alertarpedidos = 1";
		ConexionBaseDatos con = new ConexionBaseDatos();
		//Llamamos metodo de conexión asumiendo que corremos en el servidor de aplicaciones de manera local
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			ResultSet rs = stm.executeQuery(consulta);
			int idpedido;
			String nombreTienda;
			double totalBruto;
			double impuesto;
			double totalNeto;
			String nombreCliente;
			String estadoPedido;
			String fechaPedido;
			int idcliente;
			int enviadopixel;
			int numposheader;
			String url;
			String stringpixel;
			String fechainsercion;
			String usuariopedido;
			String telefono;
			String direccion;
			String formapago;
			int idformapago;
			double tiempopedido;
			while(rs.next())
			{
				idpedido = rs.getInt("idpedido");
				nombreTienda = rs.getString("nombre");
				totalBruto = rs.getDouble("total_bruto");
				impuesto = rs.getDouble("impuesto");
				totalNeto = rs.getDouble("total_neto");
				nombreCliente = rs.getString("nombrecliente");
				estadoPedido = rs.getString("descripcion");
				fechaPedido = rs.getString("fechapedido");
				idcliente = rs.getInt("idcliente");
				enviadopixel = rs.getInt("enviadopixel");
				numposheader = rs.getInt("numposheader");
				stringpixel = rs.getString("stringpixel");
				fechainsercion = rs.getString("fechainsercion");
				usuariopedido = rs.getString("usuariopedido");
				direccion = rs.getString("direccion");
				telefono = rs.getString("telefono");
				url = rs.getString("url");
				formapago = rs.getString("formapago");
				idformapago = rs.getInt("idforma_pago");
				tiempopedido = rs.getDouble("tiempopedido");
				Pedido cadaPedido = new Pedido(idpedido,  nombreTienda,totalBruto, impuesto, totalNeto,
						estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, null, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido);
				consultaPedidos.add(cadaPedido);
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
		return(consultaPedidos);
	}
	
	public static boolean seDebeReportar(int idpedido ,int maxAlertas)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		boolean seDebeReportar = false;
		boolean existeRegistro = false;
		int vecesReportado = 0;
		try
		{
			Statement stm = con1.createStatement();
			Statement stm1 = con1.createStatement();
			// Actualizamos la tabla pedido con el numero pedido pixel y le ponemos estado al pedido = 1, indicando que ya fue enviado a la tienda.
			String select = "select veces_reportado from pedidos_pendiente where idpedido =" + idpedido;
			ResultSet rs = stm.executeQuery(select);
			while(rs.next())
			{
				vecesReportado = rs.getInt("veces_reportado");
				if(vecesReportado == maxAlertas)
				{
					seDebeReportar = false;
				}
				else
				{
					seDebeReportar = true;
					vecesReportado++;
					String update = "update pedidos_pendiente set veces_reportado = " + vecesReportado + " where idpedido = " + idpedido;
					System.out.println("update " + update);
					stm1.executeUpdate(update);
				}
				existeRegistro = true;
			}
			if(!existeRegistro)
			{
				seDebeReportar = true;
				vecesReportado = 1;
				String insert = "insert into pedidos_pendiente (idpedido, veces_reportado) values(" + idpedido + "," + vecesReportado + ")";
				System.out.println("insert " + insert);
				stm1.executeUpdate(insert);
			}
			stm.close();
			con1.close();
		}
		catch (Exception e){
			System.out.println(e.getMessage());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
			return(false);
		}
		return(seDebeReportar);
	}
	
	//Método que retona totales por tamaño de pizzas en un día determinado
	public static ArrayList obtenerTotalPizzasFechas(String fechaInicial, String fechaFinal, String url)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(url);
		ArrayList pedidos = new ArrayList();
		
		try
		{
			Statement stm = con1.createStatement();
			//En esta consulta incluimos los pedidos anulados como se puede ver no tiene la condición idmotivoanulacion IS NULL
			String consulta = "SELECT c.tamano, COUNT(*) FROM pedido a, detalle_pedido b , producto c WHERE a.idpedidotienda = b.idpedidotienda AND b.idproducto = c.idproducto AND c.tamano IN ('MD', 'GD', 'XL', 'PZ') AND a.fechapedido >= '" + fechaInicial + "' and a.fechapedido <= '" + fechaFinal + "' GROUP BY tamano";
			System.out.println(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			ResultSetMetaData rsMd = (ResultSetMetaData) rs.getMetaData();
			int numeroColumnas = rsMd.getColumnCount();
			while(rs.next()){
				String [] fila = new String[numeroColumnas];
				for(int y = 0; y < numeroColumnas; y++)
				{
					fila[y] = rs.getString(y+1);
				}
				pedidos.add(fila);
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
		return(pedidos);
		
	}
	
	//Método que retona totales por especialidades de pizzas en un día determinado
		public static ArrayList obtenerTotalTipoFechas(String fechaInicial, String fechaFinal, String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			ArrayList pedidos = new ArrayList();
			
			try
			{
				Statement stm = con1.createStatement();
				//En esta consulta incluimos los pedidos anulados como se puede ver no tiene la condición idmotivoanulacion IS NULL
				String consulta = "SELECT c.descripcion, COUNT(*) FROM pedido a, detalle_pedido b , producto c WHERE a.idpedidotienda = b.idpedidotienda AND b.idproducto = c.idproducto AND b.iddetalle_pedido_master = 0 and c.tamano IN ('MD', 'GD', 'XL', 'PZ') AND a.fechapedido >= '" + fechaInicial + "' and a.fechapedido <= '" + fechaFinal +"' GROUP BY c.descripcion";
				System.out.println(consulta);
				ResultSet rs = stm.executeQuery(consulta);
				ResultSetMetaData rsMd = (ResultSetMetaData) rs.getMetaData();
				int numeroColumnas = rsMd.getColumnCount();
				while(rs.next()){
					String [] fila = new String[numeroColumnas];
					for(int y = 0; y < numeroColumnas; y++)
					{
						fila[y] = rs.getString(y+1);
					}
					pedidos.add(fila);
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
			return(pedidos);
			
		}
		
		public static double obtenerTotalesPedidosSemana(String fechaAnterior, String fechaPosterior , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double totalVenta = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select sum(total_neto) from pedido where fechapedido >= '" + fechaAnterior + "' and fechapedido <=  '" + fechaPosterior + "'  and idmotivoanulacion IS NULL"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					totalVenta = rs.getDouble(1);
					break;
				}
				rs.close();
				stm.close();
				con1.close();
			}
			catch (Exception e){
				System.out.println(e.toString());
				try
				{
					con1.close();
				}catch(Exception e1)
				{
				}
				return(0);
			}
			return(totalVenta);
		}
		
		public static ArrayList obtenerPedidosDomiciliosCOM(int idRazon,String fechaAnterior, String fechaActual)
		{
			ArrayList pedidosDomCOM = new ArrayList();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select b.nombre, a.total_neto, a.fechapedido, a.idpedido, a.numposheader, f.observacion, g.idforma_pago, f.descuento, f.motivo from pedido a, tienda b, razon_x_tienda e, marcacion_pedido f, pedido_forma_pago g "
						+"where a.idtienda = b.idtienda and b.idtienda = e.idtienda and e.idrazon = " + idRazon +"  and a.idpedido = f.idpedido "
						+"and f.idmarcacion = 1 and a.fechapedido >= '" + fechaAnterior + "' and a.fechapedido <= '" + fechaActual + "' and a.idpedido = g.idpedido order by b.nombre, fechapedido";
				ResultSet rs = stm.executeQuery(consulta);
				String[] resTemp = new String[9];
				while(rs.next()){
					resTemp = new String[9];
					resTemp[0] = rs.getString("nombre");
					resTemp[1] = rs.getString("total_neto");
					resTemp[2] = rs.getString("fechapedido");
					resTemp[3] = rs.getString("idpedido");
					resTemp[4] = rs.getString("numposheader");
					resTemp[5] = rs.getString("observacion");
					resTemp[6] = rs.getString("idforma_pago");
					resTemp[7] = rs.getString("descuento");
					resTemp[8] = rs.getString("motivo");
					pedidosDomCOM.add(resTemp);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("falle lanzando la consulta de domicilios.com");
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle cerrando la conexion");
				}
			}
			return(pedidosDomCOM);
		}

}
