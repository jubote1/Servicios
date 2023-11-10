package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.mysql.cj.jdbc.result.ResultSetMetaData;

import CapaDAOSer.TiendaDAO;
import ConexionSer.ConexionBaseDatos;
import ModeloSer.ClienteCampana;
import ModeloSer.ClienteFiel;
import ModeloSer.Pedido;
import ModeloSer.Tienda;

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
						estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, tiendapedido, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido,0,0,"",0);
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
						estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, tiendapedido, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido,0,0,"",0);
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
		//consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido, f.valorformapago, a.descuento, c.memcode, a.idtienda, a.origen from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 2 and a.origen IN ('C','TK') and a.enviadopixel = 0 AND TIMESTAMPDIFF(MINUTE, a.fechainsercion, NOW()) > 5 and b.alertarpedidos = 1";
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido, f.valorformapago, a.descuento, c.memcode, a.idtienda, a.hora_programado, a.origen from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 2 and a.origen = 'C' and a.enviadopixel = 0 AND TIMESTAMPDIFF(MINUTE, a.fechainsercion, NOW()) > 5 and b.alertarpedidos = 1";
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
			double valorFormaPago;
			double descuento;
			String motivoDescuento;
			int memcode;
			int idTienda;
			String origen = "";
			String horaProgramado = "";
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
				valorFormaPago = rs.getDouble("valorformapago");
				descuento = rs.getDouble("descuento");
				motivoDescuento = "";
				memcode = rs.getInt("memcode");
				idTienda = rs.getInt("idtienda");
				origen = rs.getString("origen");
				horaProgramado = rs.getString("hora_programado");
				Pedido cadaPedido = new Pedido(idpedido,  nombreTienda,totalBruto, impuesto, totalNeto,
						estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, null, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido,valorFormaPago, descuento, motivoDescuento, memcode);
				cadaPedido.setIdtienda(idTienda);
				cadaPedido.setOrigen(origen);
				cadaPedido.setHoraProgramado(horaProgramado);
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
	
	public static ArrayList<Pedido> ConsultarPedidosPendientesRAPPI(String fechaPed)
	{
		ArrayList <Pedido> consultaPedidos = new ArrayList();
		int idtienda = 0;
		String consulta = "";
		//consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido, f.valorformapago, a.descuento, c.memcode, a.idtienda, a.origen from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 2 and a.origen IN ('C','TK') and a.enviadopixel = 0 AND TIMESTAMPDIFF(MINUTE, a.fechainsercion, NOW()) > 5 and b.alertarpedidos = 1";
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido, f.valorformapago, a.descuento, c.memcode, a.idtienda, a.hora_programado, a.origen from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 2 and a.origen = 'RAP' and a.enviadopixel = 0 AND TIMESTAMPDIFF(MINUTE, a.fechainsercion, NOW()) > 2";
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
			double valorFormaPago;
			double descuento;
			String motivoDescuento;
			int memcode;
			int idTienda;
			String origen = "";
			String horaProgramado = "";
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
				valorFormaPago = rs.getDouble("valorformapago");
				descuento = rs.getDouble("descuento");
				motivoDescuento = "";
				memcode = rs.getInt("memcode");
				idTienda = rs.getInt("idtienda");
				origen = rs.getString("origen");
				horaProgramado = rs.getString("hora_programado");
				Pedido cadaPedido = new Pedido(idpedido,  nombreTienda,totalBruto, impuesto, totalNeto,
						estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, null, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido,valorFormaPago, descuento, motivoDescuento, memcode);
				cadaPedido.setIdtienda(idTienda);
				cadaPedido.setOrigen(origen);
				cadaPedido.setHoraProgramado(horaProgramado);
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
	
	/**
	 * Método que se encargará de consultar los pedidos pendientes dada una fecha determinada y con origen de la tienda virtual, con el fin de alertar posteriormente en correo electrónico
	 * @param fechaPed
	 * @return
	 */
	public static ArrayList<Pedido> ConsultarPedidosPendientesVirtual(String fechaPed)
	{
		ArrayList <Pedido> consultaPedidos = new ArrayList();
		int idtienda = 0;
		String consulta = "";
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido, f.valorformapago, a.descuento, c.memcode, a.idtienda from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 2 and a.origen = 'T' and a.enviadopixel = 0 AND TIMESTAMPDIFF(MINUTE, a.fechainsercion, NOW()) > 5 and b.alertarpedidos = 1";
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
			double valorFormaPago;
			double descuento;
			String motivoDescuento;
			int memcode;
			int idTienda;
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
				valorFormaPago = rs.getDouble("valorformapago");
				descuento = rs.getDouble("descuento");
				motivoDescuento = "";
				memcode = rs.getInt("memcode");
				idTienda = rs.getInt("idtienda");
				Pedido cadaPedido = new Pedido(idpedido,  nombreTienda,totalBruto, impuesto, totalNeto,
						estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, null, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido,valorFormaPago, descuento, motivoDescuento, memcode);
				cadaPedido.setIdtienda(idTienda);
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
	
	
	/**
	 * Método que se encargará de traer los posibles pedidos duplicados que se alertarán de manera temprana con el fin de 
	 * @param fechaPed
	 * @return
	 */
	public static ArrayList ConsultarPosiblesPedidosDuplicados(String fechaPed)
	{
		ArrayList duplicadosClientes = new ArrayList();
		int idtienda = 0;
		String consulta = "";
		consulta = "SELECT COUNT(*), b.telefono, b.idcliente FROM pedido a, cliente b WHERE a.idcliente = b.idcliente AND a.idestadopedido = 2 AND fechapedido = '" + fechaPed + "' GROUP BY b.telefono, b.idcliente HAVING COUNT(*) >= 2;";
		ConexionBaseDatos con = new ConexionBaseDatos();
		//Llamamos metodo de conexión asumiendo que corremos en el servidor de aplicaciones de manera local
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			ResultSet rs = stm.executeQuery(consulta);
			ResultSetMetaData rsMd = (ResultSetMetaData) rs.getMetaData();
			int numeroColumnas = rsMd.getColumnCount();
			while(rs.next()){
				String [] fila = new String[numeroColumnas];
				for(int y = 0; y < numeroColumnas; y++)
				{
					fila[y] = rs.getString(y+1);
				}
				duplicadosClientes.add(fila);
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
		return(duplicadosClientes);
	}
	
	/**
	 * Método que devuelve los pagos virtuales que ya fueron realizados en la fecha y que están en el estado PENDIENTE PAGO VIRTUAL
	 * @param fechaPed
	 * @return
	 */
	public static ArrayList<Pedido> ConsultarPedidosVirtualRealizados(String fechaPed)
	{
		ArrayList <Pedido> consultaPedidos = new ArrayList();
		int idtienda = 0;
		String consulta = "";
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido, f.valorformapago, a.descuento, c.memcode, a.idtienda, a.idlink, a.hora_programado, a.origen from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 2 and a.enviadopixel = 2 and a.fechapagovirtual IS NOT NULL ";
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
			double valorFormaPago;
			double descuento;
			String motivoDescuento;
			int memcode;
			int idTienda;
			String idLink;
			String horaProgramado;
			String origen;
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
				valorFormaPago = rs.getDouble("valorformapago");
				descuento = rs.getDouble("descuento");
				motivoDescuento = "";
				memcode = rs.getInt("memcode");
				idTienda = rs.getInt("idtienda");
				idLink = rs.getString("idlink");
				horaProgramado = rs.getString("hora_programado");
				origen = rs.getString("origen");
				Pedido cadaPedido = new Pedido(idpedido,  nombreTienda,totalBruto, impuesto, totalNeto,
						estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, null, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido,valorFormaPago, descuento, motivoDescuento, memcode);
				cadaPedido.setIdtienda(idTienda);
				cadaPedido.setIdLink(idLink);
				cadaPedido.setHoraProgramado(horaProgramado);
				cadaPedido.setOrigen(origen);
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
	
	
	/**
	 * Método que se encarga de retornar los pedidos virtuales que llevan un tiempo de 15 minutos o más y no han sido realizados.
	 * @param fechaPed
	 * @return
	 */
	public static ArrayList<Pedido> ConsultarPagosVirtualSinPagar(String fechaPed, int minutos)
	{
		ArrayList <Pedido> consultaPedidos = new ArrayList();
		int idtienda = 0;
		String consulta = "";
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido, f.valorformapago, a.descuento, c.memcode, a.idtienda, a.idlink from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 2 and a.enviadopixel = 2 AND TIMESTAMPDIFF(MINUTE, a.fechainsercion, NOW()) >= " + minutos + " and a.fechapagovirtual IS NULL";
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
			double valorFormaPago;
			double descuento;
			String motivoDescuento;
			int memcode;
			int idTienda;
			String idLink;
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
				valorFormaPago = rs.getDouble("valorformapago");
				descuento = rs.getDouble("descuento");
				motivoDescuento = "";
				memcode = rs.getInt("memcode");
				idTienda = rs.getInt("idtienda");
				idLink = rs.getString("idlink");
				Pedido cadaPedido = new Pedido(idpedido,  nombreTienda,totalBruto, impuesto, totalNeto,
						estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, null, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido,valorFormaPago, descuento, motivoDescuento, memcode);
				cadaPedido.setIdtienda(idTienda);
				cadaPedido.setIdLink(idLink);
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
	
	public static ArrayList<Pedido> ConsultarPagosVirtualSinPagarEspecial(String fechaPed, int minutos)
	{
		ArrayList <Pedido> consultaPedidos = new ArrayList();
		int idtienda = 0;
		String consulta = "";
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido, f.valorformapago, a.descuento, c.memcode, a.idtienda, a.idlink from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 2 and a.enviadopixel = 2 AND TIMESTAMPDIFF(MINUTE, a.fechainsercion, NOW()) >= " + minutos + " and a.fechapagovirtual IS NULL and a.origen NOT IN ('APP','TK') and a.programado != 'S'";
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
			double valorFormaPago;
			double descuento;
			String motivoDescuento;
			int memcode;
			int idTienda;
			String idLink;
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
				valorFormaPago = rs.getDouble("valorformapago");
				descuento = rs.getDouble("descuento");
				motivoDescuento = "";
				memcode = rs.getInt("memcode");
				idTienda = rs.getInt("idtienda");
				idLink = rs.getString("idlink");
				Pedido cadaPedido = new Pedido(idpedido,  nombreTienda,totalBruto, impuesto, totalNeto,
						estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, null, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido,valorFormaPago, descuento, motivoDescuento, memcode);
				cadaPedido.setIdtienda(idTienda);
				cadaPedido.setIdLink(idLink);
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
	
	
	
	public static ArrayList<Pedido> ConsultarPagosVirtualSinPagarRango(String fechaPed, int minutos, int minutosSuperior)
	{
		ArrayList <Pedido> consultaPedidos = new ArrayList();
		int idtienda = 0;
		String consulta = "";
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido, f.valorformapago, a.descuento, c.memcode, a.idtienda, a.idlink from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 2 and a.enviadopixel = 2 AND TIMESTAMPDIFF(MINUTE, a.fechainsercion, NOW()) >= " + minutos + " AND TIMESTAMPDIFF(MINUTE, a.fechainsercion, NOW()) <=" + minutosSuperior + " and a.fechapagovirtual IS NULL";
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
			double valorFormaPago;
			double descuento;
			String motivoDescuento;
			int memcode;
			int idTienda;
			String idLink;
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
				valorFormaPago = rs.getDouble("valorformapago");
				descuento = rs.getDouble("descuento");
				motivoDescuento = "";
				memcode = rs.getInt("memcode");
				idTienda = rs.getInt("idtienda");
				idLink = rs.getString("idlink");
				Pedido cadaPedido = new Pedido(idpedido,  nombreTienda,totalBruto, impuesto, totalNeto,
						estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, null, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido,valorFormaPago, descuento, motivoDescuento, memcode);
				cadaPedido.setIdtienda(idTienda);
				cadaPedido.setIdLink(idLink);
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
	
	
	/**
	 * Método que se encarga de retornar los pedidos virtuales que llevan un tiempo de 15 minutos o más y no han sido realizados.
	 * @param fechaPed
	 * @return
	 */
	public static ArrayList<Pedido> ConsultarPagosVirtualSinLink(String fechaPed)
	{
		ArrayList <Pedido> consultaPedidos = new ArrayList();
		int idtienda = 0;
		String consulta = "";
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido, f.valorformapago, a.descuento, c.memcode, a.idtienda, a.idlink from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 2 and a.enviadopixel = 2 AND e.virtual = 'S' AND TIMESTAMPDIFF(MINUTE, a.fechainsercion, NOW()) > 7 and a.fechapagovirtual IS NULL and a.idlink = ''";
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
			double valorFormaPago;
			double descuento;
			String motivoDescuento;
			int memcode;
			int idTienda;
			String idLink;
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
				valorFormaPago = rs.getDouble("valorformapago");
				descuento = rs.getDouble("descuento");
				motivoDescuento = "";
				memcode = rs.getInt("memcode");
				idTienda = rs.getInt("idtienda");
				idLink = rs.getString("idlink");
				Pedido cadaPedido = new Pedido(idpedido,  nombreTienda,totalBruto, impuesto, totalNeto,
						estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, null, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido,valorFormaPago, descuento, motivoDescuento, memcode);
				cadaPedido.setIdtienda(idTienda);
				cadaPedido.setIdLink(idLink);
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
	
	
	public static ArrayList<Pedido> ConsultarPedidosEnCurso(String fechaPed)
	{
		ArrayList <Pedido> consultaPedidos = new ArrayList();
		int idtienda = 0;
		String consulta = "";
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, a.tiempopedido from pedido a, tienda b, cliente c, estado_pedido d where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 1 and a.enviadopixel = 0 AND TIMESTAMPDIFF(MINUTE, a.fechainsercion, NOW()) > 7 and b.alertarpedidos = 1";
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
				formapago = "";
				idformapago = 0;
				tiempopedido = rs.getDouble("tiempopedido");
				Pedido cadaPedido = new Pedido(idpedido,  nombreTienda,totalBruto, impuesto, totalNeto,
						estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, null, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido,0,0,"",0);
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
	
	/**
	 * Los tipos de pedidos recibidos serán C en curso y F finalizado.
	 * @param idpedido
	 * @param tipo
	 * @param maxAlertas
	 * @return
	 */
	public static boolean seDebeReportar(int idpedido, String tipo ,int maxAlertas)
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
			String select = "select veces_reportado from pedidos_pendiente where idpedido =" + idpedido + " and tipo = '" + tipo + "'";
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
					String update = "update pedidos_pendiente set veces_reportado = " + vecesReportado + " where idpedido = " + idpedido + " and tipo ='" + tipo + "'";
					System.out.println("update " + update);
					stm1.executeUpdate(update);
				}
				existeRegistro = true;
			}
			if(!existeRegistro)
			{
				seDebeReportar = true;
				vecesReportado = 1;
				String insert = "insert into pedidos_pendiente (idpedido, veces_reportado, tipo) values(" + idpedido + "," + vecesReportado + " , '" + tipo + "')";
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
	
	/**
	 * Método que nos indicará si debo o no reportar un pedido que lleva 20 minutos sin hacerse el pago, la idea es reportarlo una sola vez 
	 * y por esto se realiza el control conrrespondiente
	 * @param idpedido
	 * @return
	 */
	public static boolean seDebeReportarPagoVirtual(int idpedido)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		boolean seDebeReportar = false;
		try
		{
			Statement stm = con1.createStatement();
			Statement stm1 = con1.createStatement();
			// Actualizamos la tabla pedido con el numero pedido pixel y le ponemos estado al pedido = 1, indicando que ya fue enviado a la tienda.
			String select = "select * from pedidos_pendiente_pago_virtual where idpedido =" + idpedido;
			ResultSet rs = stm.executeQuery(select);
			boolean reportado = false;
			while(rs.next())
			{
				reportado = true;
				break;	
			}
			if(!reportado)
			{
				seDebeReportar = true;
				String insert = "insert into pedidos_pendiente_pago_virtual (idpedido) values(" + idpedido + ")";
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
	
	public static boolean seDebeReportarPagoWhatsApp(int idpedido)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		boolean seDebeReportar = false;
		try
		{
			Statement stm = con1.createStatement();
			Statement stm1 = con1.createStatement();
			// Actualizamos la tabla pedido con el numero pedido pixel y le ponemos estado al pedido = 1, indicando que ya fue enviado a la tienda.
			String select = "select * from pedidos_pendiente_whatsapp where idpedido =" + idpedido;
			ResultSet rs = stm.executeQuery(select);
			boolean reportado = false;
			while(rs.next())
			{
				reportado = true;
				break;	
			}
			if(!reportado)
			{
				seDebeReportar = true;
				String insert = "insert pedidos_pendiente_whatsapp (idpedido) values(" + idpedido + ")";
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
				String consulta = "SELECT c.descripcion, COUNT(*) cantidad, SUM(b.valortotal) total, c.tamano FROM pedido a, detalle_pedido b , producto c WHERE a.idpedidotienda = b.idpedidotienda AND b.idproducto = c.idproducto AND b.iddetalle_pedido_master = 0 and c.tamano IN ('MD', 'GD', 'XL', 'PZ','LASAG MIXTA', 'NUGGETS', 'DEDITOS','MADURITO') AND a.fechapedido >= '" + fechaInicial + "' and a.fechapedido <= '" + fechaFinal +"' GROUP BY c.descripcion, c.tamano";
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
				String consulta = "select SUM(d.total_neto) - (select IFNULL(SUM(b.valortotal),0) AS  total_neto FROM pedido a, detalle_pedido b, producto c WHERE a.idpedidotienda = b.idpedidotienda AND b.idproducto = c.idproducto AND a.fechapedido >= '"+ fechaAnterior +"' AND a.fechapedido <=  '" + fechaPosterior + "'  AND a.idmotivoanulacion IS NULL AND c.no_venta = 1) AS total_neto FROM pedido d WHERE d.fechapedido >= '" + fechaAnterior + "' AND d.fechapedido <=  '" + fechaPosterior +"'  AND d.idmotivoanulacion IS NULL"; 
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
		
		public static int obtenerCantFacturas(String fechaAnterior, String fechaPosterior , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			int cantidad = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select count(*) from pedido where fechapedido >= '" + fechaAnterior + "' and fechapedido <=  '" + fechaPosterior + "'  and idmotivoanulacion IS NULL"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					cantidad  = rs.getInt(1);
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
			return(cantidad);
		}
		
		//Comenzamos a generar una consulta por cada promoción
		
		/**
		 * Método que nos trae la cantidad de promociones vendida para una fecha en específica con base en la fecha y el 
		 * String de conexión de la tienda
		 * @param fecha
		 * @param url
		 * @return
		 */
		public static double obtenerTotalesPromoMediana(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT \r\n" + 
						"(SELECT SUM(dp.valorunitario) /17495\r\n" + 
						"FROM detalle_pedido dp, producto pr , pedido pe\r\n" + 
						"WHERE pr.idproducto = dp.idproducto\r\n" + 
						"AND pe.idpedidotienda = dp.idpedidotienda\r\n" + 
						"AND pe.fechapedido = tabla.dia\r\n" + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE\r\n" + 
						"pr.descripcion like '%Combo 2 Medianas%')) AS MdDobleOnline\r\n" + 
						"FROM\r\n" + 
						"(\r\n" + 
						"SELECT pe.fechapedido as dia\r\n" + 
						"FROM pedido pe\r\n" + 
						"WHERE pe.fechapedido = '" + fecha + "'\r\n" + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
					total = total / 2;
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
			return(total);
		}
		
		/**
		 * Método que nos trae la cantidad de promociones vendida para una fecha en específica con base en la fecha y el 
		 * String de conexión de la tienda
		 * @param fecha
		 * @param url
		 * @return
		 */
		public static double obtenerTotalesPromoMediana20(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT \r\n" + 
						"(SELECT SUM(dp.valorunitario) /19990\r\n" + 
						"FROM detalle_pedido dp, producto pr , pedido pe\r\n" + 
						"WHERE pr.idproducto = dp.idproducto\r\n" + 
						"AND pe.idpedidotienda = dp.idpedidotienda\r\n" + 
						"AND pe.fechapedido = tabla.dia\r\n" + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE\r\n" + 
						"pr.descripcion like '%MD SUPER DOMICILIO%')) AS MdDobleOnline\r\n" + 
						"FROM\r\n" + 
						"(\r\n" + 
						"SELECT pe.fechapedido as dia\r\n" + 
						"FROM pedido pe\r\n" + 
						"WHERE pe.fechapedido = '" + fecha + "'\r\n" + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		
		/**
		 * Cantidad de promociones para el Mejor Combo 19900
		 * @param fecha
		 * @param url
		 * @return
		 */
		public static double obtenerTotalesPromoMDPizzaton(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT \r\n" + 
						"(SELECT SUM(dp.valorunitario) /21990\r\n" + 
						"FROM detalle_pedido dp, producto pr , pedido pe\r\n" + 
						"WHERE pr.idproducto = dp.idproducto\r\n" + 
						"AND pe.idpedidotienda = dp.idpedidotienda\r\n" + 
						"AND pe.fechapedido = tabla.dia\r\n" + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE\r\n" + 
						"pr.descripcion like '%Promo mediana plus%')) AS MdDobleOnline\r\n" + 
						"FROM\r\n" + 
						"(\r\n" + 
						"SELECT pe.fechapedido as dia\r\n" + 
						"FROM pedido pe\r\n" + 
						"WHERE pe.fechapedido = '" + fecha + "'\r\n" + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		

		public static double obtenerTotalesPromoMejorCombo(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT \r\n" + 
						"(SELECT SUM(dp.valorunitario) /31990\r\n" + 
						"FROM detalle_pedido dp, producto pr , pedido pe\r\n" + 
						"WHERE pr.idproducto = dp.idproducto\r\n" + 
						"AND pe.idpedidotienda = dp.idpedidotienda\r\n" + 
						"AND pe.fechapedido = tabla.dia\r\n" + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE\r\n" + 
						"pr.descripcion like '%Combo Deli Grande%')) AS MdDobleOnline\r\n" + 
						"FROM\r\n" + 
						"(\r\n" + 
						"SELECT pe.fechapedido as dia\r\n" + 
						"FROM pedido pe\r\n" + 
						"WHERE pe.fechapedido = '" + fecha + "'\r\n" + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		
		public static double obtenerTotalesEstofada(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT \r\n" + 
						"(SELECT count(*)\r\n" + 
						"FROM detalle_pedido dp, producto pr , pedido pe\r\n" + 
						"WHERE pr.idproducto = dp.idproducto\r\n" + 
						"AND pe.idpedidotienda = dp.idpedidotienda\r\n" + 
						"AND pe.fechapedido = tabla.dia\r\n" + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE\r\n" + 
						"pr.descripcion like '%Estofada%')) AS MdDobleOnline\r\n" + 
						"FROM\r\n" + 
						"(\r\n" + 
						"SELECT pe.fechapedido as dia\r\n" + 
						"FROM pedido pe\r\n" + 
						"WHERE pe.fechapedido = '" + fecha + "'\r\n" + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		
		public static double obtenerTotalesCodigoFlash(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT \r\n" + 
						"(SELECT SUM(dp.valorunitario) /9900\r\n" + 
						"FROM detalle_pedido dp, producto pr , pedido pe\r\n" + 
						"WHERE pr.idproducto = dp.idproducto\r\n" + 
						"AND pe.idpedidotienda = dp.idpedidotienda\r\n" + 
						"AND pe.fechapedido = tabla.dia\r\n" + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE\r\n" + 
						"pr.descripcion like '%GD código flash%')) AS MdDobleOnline\r\n" + 
						"FROM\r\n" + 
						"(\r\n" + 
						"SELECT pe.fechapedido as dia\r\n" + 
						"FROM pedido pe\r\n" + 
						"WHERE pe.fechapedido = '" + fecha + "'\r\n" + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		
		public static double obtenerTotalesPromoMediana19(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT \r\n" + 
						"(SELECT SUM(dp.valorunitario) /19900\r\n" + 
						"FROM detalle_pedido dp, producto pr , pedido pe\r\n" + 
						"WHERE pr.idproducto = dp.idproducto\r\n" + 
						"AND pe.idpedidotienda = dp.idpedidotienda\r\n" + 
						"AND pe.fechapedido = tabla.dia\r\n" + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE\r\n" + 
						"pr.descripcion like '%Mediana Al Mejor Precio%')) AS MdDobleOnline\r\n" + 
						"FROM\r\n" + 
						"(\r\n" + 
						"SELECT pe.fechapedido as dia\r\n" + 
						"FROM pedido pe\r\n" + 
						"WHERE pe.fechapedido = '" + fecha + "'\r\n" + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		
		
		public static double obtenerTotalesPromoPizzeta20(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT \r\n" + 
						"(SELECT SUM(dp.valorunitario) /9995\r\n" + 
						"FROM detalle_pedido dp, producto pr , pedido pe\r\n" + 
						"WHERE pr.idproducto = dp.idproducto\r\n" + 
						"AND pe.idpedidotienda = dp.idpedidotienda\r\n" + 
						"AND pe.fechapedido = tabla.dia\r\n" + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE\r\n" + 
						"pr.descripcion like '%PZ 2 x19.990%')) AS MdDobleOnline\r\n" + 
						"FROM\r\n" + 
						"(\r\n" + 
						"SELECT pe.fechapedido as dia\r\n" + 
						"FROM pedido pe\r\n" + 
						"WHERE pe.fechapedido = '" + fecha + "'\r\n" + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
					total = total / 2;
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
			return(total);
		}
		
		
		/**
		 * Método que nos trae la cantidad de promociones vendida para una fecha en específica con base en la fecha y el 
		 * String de conexión de la tienda
		 * @param fecha
		 * @param url
		 * @return
		 */
		public static double obtenerTotalesPromoVolante(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT " + 
						"(SELECT COUNT(*) " + 
						"FROM detalle_pedido dp, producto pr , pedido pe " + 
						"WHERE pr.idproducto = dp.idproducto " + 
						"AND pe.idpedidotienda = dp.idpedidotienda " + 
						"AND pe.fechapedido = tabla.dia " + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE " + 
						"pr.descripcion like '%Prom mediana Vol 2ING%')) AS pizzavolante " + 
						"FROM " + 
						"( " + 
						"SELECT pe.fechapedido as dia " + 
						"FROM pedido pe " + 
						"WHERE pe.fechapedido = '" + fecha + "' " + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		
		/**
		 * Método que nos trae la cantidad de promociones vendida para una fecha en específica con base en la fecha y el 
		 * String de conexión de la tienda
		 * @param fecha
		 * @param url
		 * @return
		 */
		public static double obtenerTotalesPromoRappi(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT " + 
						"(SELECT COUNT(*) " + 
						"FROM detalle_pedido dp, producto pr , pedido pe " + 
						"WHERE pr.idproducto = dp.idproducto " + 
						"AND pe.idpedidotienda = dp.idpedidotienda " + 
						"AND pe.fechapedido = tabla.dia " + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE " + 
						"pr.descripcion like '%Rappi%')) AS pizzavolante " + 
						"FROM " + 
						"( " + 
						"SELECT pe.fechapedido as dia " + 
						"FROM pedido pe " + 
						"WHERE pe.fechapedido = '" + fecha + "' " + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		
		/**
		 * Método genérico que nos entrega la información por promoción, origen del canal y fecha.
		 * @param fecha
		 * @param idExcepcion
		 * @param origen
		 * @return
		 */
		public static double obtenerTotalesPromo(String fecha, int idExcepcion, String origen)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT COUNT(*) FROM pedido a, detalle_pedido b WHERE a.idpedido = b.idpedido AND " + 
						" a.origen = '" + origen + "' AND fechapedido = '" + fecha +"' AND b.idexcepcion = " + idExcepcion; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
					if(idExcepcion == 20 || idExcepcion == 26)
					{
						total = total / 2;
					}
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
			return(total);
		}
		
		public static double obtenerTotalesPromoTienda(String fecha, int idExcepcion, String origen, int idTienda)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT COUNT(*) FROM pedido a, detalle_pedido b WHERE a.idpedido = b.idpedido AND " + 
						" a.origen = '" + origen + "' AND fechapedido = '" + fecha +"' AND b.idexcepcion = " + idExcepcion + " AND a.idtienda = " + idTienda; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
					if(idExcepcion == 20 || idExcepcion == 26)
					{
						total = total / 2;
					}
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
			return(total);
		}
		
		/**
		 * Método que retorna la cantidad de productos vendido por canal para un determinado producto
		 * @param fecha
		 * @param idProducto
		 * @param origen
		 * @param idTienda
		 * @return
		 */
		public static double obtenerTotalesProductoTienda(String fecha, int idProducto1,int idProducto2, String origen, int idTienda)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "";
				if(idProducto1 > 0 && idProducto2 == 0)
				{
					consulta = "SELECT COUNT(*) FROM pedido a, detalle_pedido b WHERE a.idpedido = b.idpedido AND " + 
							" a.origen = '" + origen + "' AND fechapedido = '" + fecha +"' AND b.idproducto = " + idProducto1 + " AND a.idtienda = " + idTienda;
				}
				else
				{
					consulta = "SELECT COUNT(*) FROM pedido a, detalle_pedido b WHERE a.idpedido = b.idpedido AND " + 
							" a.origen = '" + origen + "' AND fechapedido = '" + fecha +"' AND b.idproducto IN ( " + idProducto1 + "," + idProducto2 + ") AND a.idtienda = " + idTienda;
				}
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		
		/**
		 * Método que nos trae la cantidad de promociones vendida para una fecha en específica con base en la fecha y el 
		 * String de conexión de la tienda
		 * @param fecha
		 * @param url
		 * @return
		 */
		public static double obtenerTotalesPromoGrandeDomicilios(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT " + 
						"(SELECT SUM(dp.valorunitario) /26500\r\n" + 
						"FROM detalle_pedido dp, producto pr , pedido pe\r\n" + 
						"WHERE pr.idproducto = dp.idproducto\r\n" + 
						"AND pe.idpedidotienda = dp.idpedidotienda\r\n" + 
						"AND pe.fechapedido = tabla.dia\r\n" + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE\r\n" + 
						"pr.descripcion like '%GD Promo Domi.com%')) AS GDaPrecioMD\r\n" + 
						"FROM\r\n" + 
						"(\r\n" + 
						"SELECT pe.fechapedido as dia\r\n" + 
						"FROM pedido pe\r\n" + 
						"WHERE pe.fechapedido = '" + fecha + "' \r\n" + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		
		/**
		 * Método que nos trae la cantidad de promociones vendida para una fecha en específica con base en la fecha y el 
		 * String de conexión de la tienda
		 * @param fecha
		 * @param url
		 * @return
		 */
		public static double obtenerTotalesPromoXLDeditos(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT \r\n" + 
						"(SELECT SUM(dp.valorunitario)/34900\r\n" + 
						"FROM detalle_pedido dp, producto pr , pedido pe\r\n" + 
						"WHERE pr.idproducto = dp.idproducto\r\n" + 
						"AND pe.idpedidotienda = dp.idpedidotienda\r\n" + 
						"AND pe.fechapedido = tabla.dia\r\n" + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE\r\n" + 
						"pr.descripcion like '%XL Promo Deditos%')) AS XLPromoDeditos\r\n" + 
						"FROM\r\n" + 
						"(\r\n" + 
						"SELECT pe.fechapedido as dia\r\n" + 
						"FROM pedido pe\r\n" + 
						"WHERE pe.fechapedido = '" + fecha + "'\r\n " + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		
		public static double obtenerTotalesPromoFamiliar(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT \r\n" + 
						"(SELECT SUM(dp.valorunitario)/49990\r\n" + 
						"FROM detalle_pedido dp, producto pr , pedido pe\r\n" + 
						"WHERE pr.idproducto = dp.idproducto\r\n" + 
						"AND pe.idpedidotienda = dp.idpedidotienda\r\n" + 
						"AND pe.fechapedido = tabla.dia\r\n" + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE\r\n" + 
						"pr.descripcion like '%XL Combo para todos%')) AS XLPromoDeditos\r\n" + 
						"FROM\r\n" + 
						"(\r\n" + 
						"SELECT pe.fechapedido as dia\r\n" + 
						"FROM pedido pe\r\n" + 
						"WHERE pe.fechapedido = '" + fecha + "'\r\n " + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		
		public static double obtenerTotales40K(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT \r\n" + 
						"(SELECT SUM(dp.valorunitario)/49990\r\n" + 
						"FROM detalle_pedido dp, producto pr , pedido pe\r\n" + 
						"WHERE pr.idproducto = dp.idproducto\r\n" + 
						"AND pe.idpedidotienda = dp.idpedidotienda\r\n" + 
						"AND pe.fechapedido = tabla.dia\r\n" + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE\r\n" + 
						"pr.descripcion like '%XL Combo salva una vida%')) AS XLPromoDeditos\r\n" + 
						"FROM\r\n" + 
						"(\r\n" + 
						"SELECT pe.fechapedido as dia\r\n" + 
						"FROM pedido pe\r\n" + 
						"WHERE pe.fechapedido = '" + fecha + "'\r\n " + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		
		
		public static double obtenerTotalesComboInse(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT \r\n" + 
						"(SELECT SUM(dp.valorunitario)/9900\r\n" + 
						"FROM detalle_pedido dp, producto pr , pedido pe\r\n" + 
						"WHERE pr.idproducto = dp.idproducto\r\n" + 
						"AND pe.idpedidotienda = dp.idpedidotienda\r\n" + 
						"AND pe.fechapedido = tabla.dia\r\n" + 
						"AND dp.idproducto IN(SELECT pr.idproducto FROM producto pr WHERE\r\n" + 
						"pr.descripcion like '%Pizzeta Combo Insep%')) AS XLPromoDeditos\r\n" + 
						"FROM\r\n" + 
						"(\r\n" + 
						"SELECT pe.fechapedido as dia\r\n" + 
						"FROM pedido pe\r\n" + 
						"WHERE pe.fechapedido = '" + fecha + "'\r\n " + 
						"GROUP BY pe.fechapedido) as tabla"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		
		/**
		 * Método que no retornará la cantidad de códigos promocionales redimidos en la fecha determinada
		 * @param fecha
		 * @return
		 */
		public static ArrayList consultarUsoCodigosPromocionales(String fecha)
		{
			ArrayList<String[]> codigosRedimidos = new ArrayList();
			String consulta = "";
			consulta = "SELECT COUNT(*), c.nombre  FROM oferta_cliente a, cliente b, tienda c WHERE a.idcliente = b.idcliente and b.idtienda = c.idtienda and a.uso_oferta >= '" + fecha +" 00:00:00' AND " + 
					"a.uso_oferta <= '" + fecha + " 23:59:00' group by c.nombre";
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
					filaTemp[0] = Integer.toString(rs.getInt(1));
					filaTemp[1] = rs.getString(2);
					codigosRedimidos.add(filaTemp);
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
			return(codigosRedimidos);
		}
		
		
		/**
		 * Método que no retornará la cantidad de pedidos domicilios.com en la fecha determinada
		 * @param fecha
		 * @return
		 */
		public static int consultarPedidosDomicilios(String fecha)
		{
			int cantidad = 0;
			String consulta = "";
			consulta = "SELECT COUNT(*) FROM pedido a, detalle_pedido b WHERE a.idpedido = b.idpedido AND b.idexcepcion = 23 " + 
					"AND a.fechapedido = '" + fecha + "'";
			ConexionBaseDatos con = new ConexionBaseDatos();
			//Llamamos metodo de conexión asumiendo que corremos en el servidor de aplicaciones de manera local
			Connection con1 = con.obtenerConexionBDContactLocal();
			try
			{
				Statement stm = con1.createStatement();
				ResultSet rs = stm.executeQuery(consulta);

				while(rs.next())
				{
					
					cantidad = rs.getInt(1);
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
			return(cantidad);
		}
		
		
		public static int consultarPedidosDirectorioPublicar(String fecha)
		{
			int cantidad = 0;
			String consulta = "";
			consulta = "SELECT COUNT(*) FROM pedido a, detalle_pedido b WHERE a.idpedido = b.idpedido AND b.idexcepcion in (2, 4, 5) " + 
					"AND a.fechapedido = '" + fecha + "'";
			ConexionBaseDatos con = new ConexionBaseDatos();
			//Llamamos metodo de conexión asumiendo que corremos en el servidor de aplicaciones de manera local
			Connection con1 = con.obtenerConexionBDContactLocal();
			try
			{
				Statement stm = con1.createStatement();
				ResultSet rs = stm.executeQuery(consulta);

				while(rs.next())
				{
					
					cantidad = rs.getInt(1);
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
			return(cantidad);
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
				System.out.println(consulta);
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
		
		
		/**
		 * Método que se encargaría tener la cantidad de pedidos en un estado determinado para una fecha determinada
		 * @param fechaSistema
		 * @param idEstado
		 * @param url
		 * @return
		 */
		public static int obtenerCantidadPedidoPorEstado(String fechaSistema, int idEstado , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			int cantPedidos = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select count(*) from pedido where fechapedido = '" + fechaSistema  + "'  and idmotivoanulacion IS NULL and idestado = " + idEstado; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					cantPedidos = rs.getInt(1);
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
			return(cantPedidos);
		}
		
		public static int obtenerCantidadPedidoCocina(String fechaSistema, String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			int cantPedidoEnEspera = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select count(*) from pedido a, estado b where a.fechapedido  = '" + fechaSistema  + "'  and a.idmotivoanulacion IS NULL and a.idestado = b.idestado and b.estado_cocina = 1 ";
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					 cantPedidoEnEspera = rs.getInt(1);
					break;
				}
				rs.close();
				stm.close();
				con1.close();
			}
			catch (Exception e){
				try
				{
					con1.close();
				}catch(Exception e1)
				{
				}
				return(0);
			}
			return(cantPedidoEnEspera);
		}
		
		/**
		 * Métodoq que se encarga desde la capa DAO de obtener la cantidad de pedidos desde la última hora
		 * @param fechaSistema
		 * @param fechaHora
		 * @param url
		 * @return
		 */
		public static int obtenerCantidadPedidoDespuesHoraDomicilio(String fechaSistema, String fechaHora, String url, int idTipoDomicilio)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			int cantPedidos = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select count(*) from pedido where fechapedido = '" + fechaSistema  +"' and fechainsercion >= '" + fechaHora +"' and idmotivoanulacion IS NULL and total_neto > 10000 and idtipopedido =" + idTipoDomicilio; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					cantPedidos = rs.getInt(1);
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
			return(cantPedidos);
		}
		
		/*
		 * Método que retorna la cantidad de pedidos no domicilio que se tienen despues de una hora determinaada
		 * 
		 */
		public static int obtenerCantidadPedidoDespuesHoraNoDomicilio(String fechaSistema, String fechaHora, String url , int idTipoDomicilio)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			int cantPedidos = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select count(*) from pedido where fechapedido = '" + fechaSistema  +"' and fechainsercion >= '" + fechaHora +"' and idmotivoanulacion IS NULL and total_neto > 10000 and idtipopedido != " + idTipoDomicilio; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					cantPedidos = rs.getInt(1);
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
			return(cantPedidos);
		}
		
		/**
		 * Método que se encarga de obtener el tiempo en minutos del pedido más antiguo en el estado y fecha enviada como parámetros
		 * @param fechaSistema
		 * @param fechaHora
		 * @param idEstado
		 * @param url
		 * @return
		 */
		public static String obtenerTiempoUltimoPedidoEstado(String fechaSistema,  int idEstado, String pedidosProg, String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd H:m:s");
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			int cantidadMinutos = 0;
			int idPedido = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "";
				if(pedidosProg.equals(new String("()")))
				{
					consulta = "select * from pedido where fechapedido = '" + fechaSistema  +"' and idmotivoanulacion IS NULL and idestado = " + idEstado + " order by idpedidotienda asc limit 1"; 
				}else
				{
					consulta = "select * from pedido where fechapedido = '" + fechaSistema  +"' and idmotivoanulacion IS NULL and idestado = " + idEstado + " and idpedidotienda not in " + pedidosProg + " order by idpedidotienda asc limit 1"; 
				}
				ResultSet rs = stm.executeQuery(consulta);
				String fechaInsercion = "";
				Date datFechaInsercion = new Date();
				
				while(rs.next()){
					idPedido = rs.getInt("idpedidotienda");
					fechaInsercion = rs.getString("fechainsercion");
					datFechaInsercion = dateFormat.parse(fechaInsercion);
					break;
				}
				//Con la fecha capturada, vamos a realizar los cálculos correspondientes
				Date fechaActual = new Date();
				int difTiempo =(int) (fechaActual.getTime() - datFechaInsercion.getTime());
				cantidadMinutos = (int)TimeUnit.MILLISECONDS.toMinutes(difTiempo );
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
				return("0");
			}
			return(Integer.toString(cantidadMinutos) + " min - Ped:" + Integer.toString(idPedido) );
		}
		
		public static double obtenerTotalesPedidosSemanaTarjeta(String fechaAnterior, String fechaPosterior , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double totalVenta = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select sum(a.total_neto) from pedido a, pedido_forma_pago b, forma_pago c where a.idpedidotienda = b.idpedidotienda and b.idforma_pago = c.idforma_pago and a.fechapedido >= '" + fechaAnterior + "' and a.fechapedido <=  '" + fechaPosterior + "'  and a.idmotivoanulacion IS NULL and c.tipoformapago = 'TARJETA'"; 
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
		
		
		public static ArrayList obtenerPedidosProgramadosTienda(int idTienda)
		{
			ArrayList pedidosProgramados = new ArrayList();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select a.idpedido, a.total_neto, a.hora_programado, a.numposheader from pedido a "
						+"where a.idtienda = " + idTienda 
						+" and a.fechapedido = CURDATE() and a.programado = 'S' AND HOUR(NOW()) <= (CAST(SUBSTR(a.hora_programado, 1,2) AS UNSIGNED)) and a.numposheader > 0";
				ResultSet rs = stm.executeQuery(consulta);
				System.out.println(consulta);
				String[] resTemp = new String[9];
				while(rs.next()){
					resTemp = new String[4];
					resTemp[0] = rs.getString("idpedido");
					resTemp[1] = rs.getString("numposheader");
					resTemp[2] = rs.getString("total_neto");
					resTemp[3] = rs.getString("hora_programado");
					pedidosProgramados.add(resTemp);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("falle lanzando la consulta de pedidos posfechados");
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle cerrando la conexion");
				}
			}
			return(pedidosProgramados);
		}
		
		/**
		 * Método que se encargará de retornar los clientes a los que les aplicaría el código promocional para su envío según
		 * los parámetros enviados para su ejecución.
		 * @param idTienda
		 * @param fechaInicial
		 * @param fechaFinal
		 * @param cantidad
		 * @return
		 */
		public static ArrayList obtenerClientesCodPromoTienda(int idTienda, String fechaInicial, String fechaFinal, int cantidad)
		{
			ArrayList clientesCodPromo = new ArrayList();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "	SELECT idcliente, telefono, telefono_celular FROM cliente a WHERE a.idtienda = " + idTienda + " AND (SUBSTR(a.telefono, 1, 1) = '3' OR LENGTH(telefono_celular) > 0) AND  a.idcliente IN (SELECT b.idcliente FROM pedido b WHERE fechapedido >= '" + fechaInicial + "' AND fechapedido <= '" + fechaFinal + "') and a.idcliente not in(select idcliente from oferta_cliente) limit " + cantidad;
				ResultSet rs = stm.executeQuery(consulta);
				System.out.println(consulta);
				String[] resTemp = new String[9];
				while(rs.next()){
					resTemp = new String[3];
					resTemp[0] = rs.getString("idcliente");
					resTemp[1] = rs.getString("telefono");
					resTemp[2] = rs.getString("telefono_celular");
					if(resTemp[2] == null)
					{
						resTemp[2] = "";
					}
					clientesCodPromo.add(resTemp);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("falle lanzando la consulta de cod promo para clientes");
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle cerrando la conexion");
				}
			}
			return(clientesCodPromo);
		}
		
		public static ArrayList<ClienteCampana> obtenerClientesCampana(String consulta)
		{
			ArrayList<ClienteCampana> clientesCampana = new ArrayList();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			ClienteCampana clienteCampana = new ClienteCampana(0,"", "", "", "", "");
			try
			{
				Statement stm = con1.createStatement();
				ResultSet rs = stm.executeQuery(consulta);
				String[] resTemp = new String[9];
				int idCliente;
				String nombres;
				String apellidos;
				String telefono;
				String telefonoCelular;
				String email;
				while(rs.next()){
					resTemp = new String[6];
					//idpersona
					idCliente = Integer.parseInt(rs.getString(1));
					//nombre
					nombres = rs.getString(2);
					//apellidos
					apellidos = rs.getString(3);
					//telefono
					telefono = rs.getString(4);
					//telefono_celular
					telefonoCelular = rs.getString(5);
					//correo
					email = rs.getString(6);
					clienteCampana = new ClienteCampana(idCliente, nombres, apellidos, telefono, telefonoCelular, email);
					clientesCampana.add(clienteCampana);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("falle lanzando la consulta de cod promo para clientes " + e.toString());
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle cerrando la conexion");
				}
			}
			return(clientesCampana);
		}
		
		/**
		 * Método que retorna los domicilios que llevo un domiciliario determinado en un rango de fechas
		 * @param fechaInicial
		 * @param fechaFinal
		 * @param idDomiciliario
		 * @param BDRemota
		 * @return
		 */
		public static int obtenerPedidosEntregados(String fechaInicial, String fechaFinal, int idDomiciliario, String BDRemota)
		{
			ArrayList pedidosProgramados = new ArrayList();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(BDRemota);
			int cantPedidos = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT COUNT(*) FROM pedido a WHERE a.fechainsercion >= '" + fechaInicial + "' AND " + 
						"a.fechainsercion <= '" + fechaFinal + "' AND a.iddomiciliario = " + idDomiciliario;
				ResultSet rs = stm.executeQuery(consulta);
				String[] resTemp = new String[9];
				while(rs.next()){
					cantPedidos = rs.getInt(1);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("falle lanzando la consulta de cantidad de pedidos de un domiciliario temporal");
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle cerrando la conexion");
				}
			}
			return(cantPedidos);
		}
		

		public static ArrayList<ClienteFiel> obtenerClientesFielesPedido(int diasPedido, int cantidadPedidos)
		{
			Logger logger = Logger.getLogger("log_file");
			ArrayList<ClienteFiel> clientesFieles = new ArrayList<>();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
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
						+ " from pedido a, cliente b, tienda c where a.total_neto > 0 and a.idcliente = b.idcliente and b.idtienda = c.idtienda and fechapedido >= '" + fecha + "' " + 
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
			Connection con1 = con.obtenerConexionBDContactLocal();
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
						+ " from pedido a, cliente b, tienda c where a.idcliente = b.idcliente and b.idtienda = c.idtienda and fechapedido < '" + fecha +"' and fechapedido > '" + fechaInferior + "' " +
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
		
		
		public static boolean actualizarPedidosPayu(String fechaInicial)
		{
			boolean respuesta = false;
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			try
			{
				Statement stm = con1.createStatement();
				String update = "UPDATE pedido SET tipopago = 'CARD' WHERE idpedido IN ( SELECT * from(SELECT a.idpedido FROM pedido a, pedido_forma_pago b  WHERE a.idpedido = b.idpedido AND b.idforma_pago = 6 AND a.fechapedido >= '" + fechaInicial +"')tbltemp);";
				stm.executeUpdate(update);
				respuesta = true;
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("Actualización de pagos con PAYU");
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle cerrando la conexion");
				}
			}
			return(respuesta);
		}
		
		
		public static ArrayList<Pedido> ConsultarPedidosProgramados(String fechaPed)
		{
			ArrayList <Pedido> consultaPedidos = new ArrayList();
			int idtienda = 0;
			String consulta = "";
			consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido, f.valorformapago, a.descuento, c.memcode, a.idtienda, a.hora_programado, a.fechapagovirtual from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.programado = 'S'";
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
				double valorFormaPago;
				double descuento;
				String motivoDescuento;
				int memcode;
				int idTienda;
				String horaProgramado;
				String fechaPagoVirtual;
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
					valorFormaPago = rs.getDouble("valorformapago");
					descuento = rs.getDouble("descuento");
					motivoDescuento = "";
					memcode = rs.getInt("memcode");
					idTienda = rs.getInt("idtienda");
					horaProgramado = rs.getString("hora_programado");
					fechaPagoVirtual = rs.getString("fechapagovirtual");
					Pedido cadaPedido = new Pedido(idpedido,  nombreTienda,totalBruto, impuesto, totalNeto,
							estadoPedido, fechaPedido, nombreCliente, idcliente, enviadopixel,numposheader, null, stringpixel, fechainsercion, usuariopedido, direccion, telefono, formapago, idformapago, tiempopedido,valorFormaPago, descuento, motivoDescuento, memcode);
					cadaPedido.setIdtienda(idTienda);
					cadaPedido.setHoraProgramado(horaProgramado);
					cadaPedido.setFechaPagoVirtual(fechaPagoVirtual);
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
		
		/**
		 * Método que retorna la cantidad de pedidos cumplidos dentro del rango de fechas usando logística.
		 * @param fechaAnterior
		 * @param fechaPosterior
		 * @param url
		 * @return
		 */
		public static double obtenerTotalPedidosNoCumplidos(String fechaAnterior, String fechaPosterior , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double noCumplidos = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT COUNT(*) FROM (SELECT  round(TIME_TO_SEC(TIMEDIFF((SELECT b.fechacambio FROM cambios_estado_pedido b WHERE b.idpedidotienda = a.idpedidotienda AND b.idestadoposterior = 8 ORDER BY idcambioestado DESC LIMIT 1), a.fechainsercion))/60) as tiempo_real, a.tiempopedido AS tiempo_dado FROM pedido a, usuario c WHERE a.iddomiciliario = c.id and a.fechapedido >= '"+ fechaAnterior +"' AND a.fechapedido <= '" + fechaPosterior +"' AND a.idtipopedido = 1 AND a.logistica = 1 AND  a.tiempopedido != 0) AS notiempo WHERE tiempo_real > tiempo_dado"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					noCumplidos = rs.getDouble(1);
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
			return(noCumplidos);
		}
		
		/**
		 * Método que retorna la cantidad de pedidos no cumplidos dentro del rango de fechas usando logística.
		 * @param fechaAnterior
		 * @param fechaPosterior
		 * @param url
		 * @return
		 */
		public static double obtenerTotalPedidosCumplidos(String fechaAnterior, String fechaPosterior , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double cumplidos = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT COUNT(*) FROM (SELECT  round(TIME_TO_SEC(TIMEDIFF((SELECT b.fechacambio FROM cambios_estado_pedido b WHERE b.idpedidotienda = a.idpedidotienda AND b.idestadoposterior = 8 ORDER BY idcambioestado DESC LIMIT 1), a.fechainsercion))/60) as tiempo_real, a.tiempopedido AS tiempo_dado FROM pedido a, usuario c WHERE a.iddomiciliario = c.id and a.fechapedido >= '"+ fechaAnterior +"' AND a.fechapedido <= '" + fechaPosterior +"' AND a.idtipopedido = 1 AND a.logistica = 1 AND  a.tiempopedido != 0) AS notiempo WHERE tiempo_real <= tiempo_dado"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					cumplidos = rs.getDouble(1);
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
			return(cumplidos);
		}
		
		public static double obtenerTotalesPromoDeditosLocos(String fecha , String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			double total = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT count(*) FROM pedido a, detalle_pedido b WHERE a.idpedidotienda = b.idpedidotienda AND a.fechapedido = '"+ fecha +"' AND b.idproducto IN (480,481,482)"; 
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					total = rs.getDouble(1);
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
			return(total);
		}
		
		
		/**
		 * Método que retorna el valor del descuento asumido por las plataformas.
		 * @param fechaInicial
		 * @param fechaFinal
		 * @param idTienda
		 * @return
		 */
		public static double obtenerTotalDescuentosReembolsables(String fechaInicial, String fechaFinal, int idTienda)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDContactLocal();
			double totalDescuento = 0;
			
			try
			{
				Statement stm = con1.createStatement();
				//En esta consulta incluimos los pedidos anulados como se puede ver no tiene la condición idmotivoanulacion IS NULL
				String consulta = "SELECT SUM(b.descuento_plataforma) FROM pedido a, marcacion_pedido b WHERE a.idpedido = b.idpedido AND a.fechapedido>= '" + fechaInicial + "' AND a.fechapedido <= '" + fechaFinal + "' AND idtienda =" + idTienda;
				System.out.println(consulta);
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					totalDescuento = rs.getDouble(1);
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
			return(totalDescuento);
			
		}
		
}
