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

import com.mysql.cj.jdbc.result.ResultSetMetaData;

import CapaDAOSer.TiendaDAO;
import ConexionSer.ConexionBaseDatos;
import ModeloSer.Pedido;
import ModeloSer.Tienda;

public class PedidoDAO {
	
	/**
	 * M�todo que permite la consulta de pedidos de acuerdo a los par�metros enviados para la consulta, esta consulta es exclusiva para los 
	 * productos que son registrados dentro del sistema contact center.
	 * @param fechainicial Fecha inicial de los pedidos a consultar.
	 * @param fechafinal Fecha final de los pedidos a consultar.
	 * @param tienda nombre de la tienda que se desea filtrar para la consulta de los pedidos.
	 * @param numeropedido En caso de desearlo se puede filtrar por un n�mero de pedido en espec�fico.
	 * @return Se retorna un ArrayList con objetos de tipo pedido con la informaci�n de los pedidos consultados.
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
		//Variables donde manejaremos la fecha anerior con el fin realizar los c�lculos de ventas
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
		//Retormanos el d�a de la semana actual segun la fecha del calendario
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
		//Llevamos a un string la fecha anterior para el c�lculo de la venta
		datFechaAnterior = calendarioActual.getTime();
		fechaAnterior = dateFormat.format(datFechaAnterior);
		//Teniendo el rango de rechas ejecutamos la consulta que se encargar� de retornar los pedidos con 
		//estas caracter�sticas de domicilios.com
		
		//Luego de construidas las fechas realizamos limpieza de las tablas de anulaci�n y cambio para las fechas en cuesti�n
		
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
	 * M�todo que se encargar� de consultar los pedidos pendientes dada una fecha determinada, con el fin de alertar posteriormente en correo electr�nico
	 * @param fechaPed
	 * @return
	 */
	public static ArrayList<Pedido> ConsultarPedidosPendientes(String fechaPed)
	{
		ArrayList <Pedido> consultaPedidos = new ArrayList();
		int idtienda = 0;
		String consulta = "";
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido, f.valorformapago, a.descuento, c.memcode, a.idtienda from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 2 and a.enviadopixel = 0 AND TIMESTAMPDIFF(MINUTE, a.fechainsercion, NOW()) > 5 and b.alertarpedidos = 1";
		ConexionBaseDatos con = new ConexionBaseDatos();
		//Llamamos metodo de conexi�n asumiendo que corremos en el servidor de aplicaciones de manera local
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
	 * M�todo que devuelve los pagos virtuales que ya fueron realizados en la fecha y que est�n en el estado PENDIENTE PAGO VIRTUAL
	 * @param fechaPed
	 * @return
	 */
	public static ArrayList<Pedido> ConsultarPedidosVirtualRealizados(String fechaPed)
	{
		ArrayList <Pedido> consultaPedidos = new ArrayList();
		int idtienda = 0;
		String consulta = "";
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido, f.valorformapago, a.descuento, c.memcode, a.idtienda, a.idlink from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 2 and a.enviadopixel = 2 and a.fechapagovirtual IS NOT NULL ";
		ConexionBaseDatos con = new ConexionBaseDatos();
		//Llamamos metodo de conexi�n asumiendo que corremos en el servidor de aplicaciones de manera local
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
	 * M�todo que se encarga de retornar los pedidos virtuales que llevan un tiempo de 15 minutos o m�s y no han sido realizados.
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
		//Llamamos metodo de conexi�n asumiendo que corremos en el servidor de aplicaciones de manera local
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
	 * M�todo que se encarga de retornar los pedidos virtuales que llevan un tiempo de 15 minutos o m�s y no han sido realizados.
	 * @param fechaPed
	 * @return
	 */
	public static ArrayList<Pedido> ConsultarPagosVirtualSinLink(String fechaPed)
	{
		ArrayList <Pedido> consultaPedidos = new ArrayList();
		int idtienda = 0;
		String consulta = "";
		consulta = "select a.idpedido, b.nombre, a.total_bruto, a.impuesto, a.total_neto, concat (c.nombre , '-' , c.apellido) nombrecliente, c.direccion, c.telefono, d.descripcion, a.fechapedido, c.idcliente, a.enviadopixel, a.numposheader, b.idtienda, b.url, a.stringpixel, a.fechainsercion, a.usuariopedido, e.nombre formapago, e.idforma_pago, a.tiempopedido, f.valorformapago, a.descuento, c.memcode, a.idtienda, a.idlink from pedido a, tienda b, cliente c, estado_pedido d, forma_pago e, pedido_forma_pago f where a.idtienda = b.idtienda and a.idcliente = c.idcliente and a.idestadopedido = d.idestadopedido and e.idforma_pago = f.idforma_pago and f.idpedido = a.idpedido and a.fechapedido = '" + fechaPed + "' and a.idestadopedido = 2 and a.enviadopixel = 2 AND e.virtual = 'S' and a.fechapagovirtual IS NULL and a.idlink = ''";
		ConexionBaseDatos con = new ConexionBaseDatos();
		//Llamamos metodo de conexi�n asumiendo que corremos en el servidor de aplicaciones de manera local
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
		//Llamamos metodo de conexi�n asumiendo que corremos en el servidor de aplicaciones de manera local
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
	 * Los tipos de pedidos recibidos ser�n C en curso y F finalizado.
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
	 * M�todo que nos indicar� si debo o no reportar un pedido que lleva 20 minutos sin hacerse el pago, la idea es reportarlo una sola vez 
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
	
	//M�todo que retona totales por tama�o de pizzas en un d�a determinado
	public static ArrayList obtenerTotalPizzasFechas(String fechaInicial, String fechaFinal, String url)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(url);
		ArrayList pedidos = new ArrayList();
		
		try
		{
			Statement stm = con1.createStatement();
			//En esta consulta incluimos los pedidos anulados como se puede ver no tiene la condici�n idmotivoanulacion IS NULL
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
	
	//M�todo que retona totales por especialidades de pizzas en un d�a determinado
		public static ArrayList obtenerTotalTipoFechas(String fechaInicial, String fechaFinal, String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			ArrayList pedidos = new ArrayList();
			
			try
			{
				Statement stm = con1.createStatement();
				//En esta consulta incluimos los pedidos anulados como se puede ver no tiene la condici�n idmotivoanulacion IS NULL
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
		 * M�todo que se encargar�a tener la cantidad de pedidos en un estado determinado para una fecha determinada
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
		
		/**
		 * M�todoq que se encarga desde la capa DAO de obtener la cantidad de pedidos desde la �ltima hora
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
		 * M�todo que retorna la cantidad de pedidos no domicilio que se tienen despues de una hora determinaada
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
		 * M�todo que se encarga de obtener el tiempo en minutos del pedido m�s antiguo en el estado y fecha enviada como par�metros
		 * @param fechaSistema
		 * @param fechaHora
		 * @param idEstado
		 * @param url
		 * @return
		 */
		public static String obtenerTiempoUltimoPedidoEstado(String fechaSistema,  int idEstado, String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd H:m:s");
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			int cantidadMinutos = 0;
			int idPedido = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select * from pedido where fechapedido = '" + fechaSistema  +"' and idmotivoanulacion IS NULL and idestado = " + idEstado + " order by idpedidotienda asc limit 1"; 
				ResultSet rs = stm.executeQuery(consulta);
				String fechaInsercion = "";
				Date datFechaInsercion = new Date();
				
				while(rs.next()){
					idPedido = rs.getInt("idpedidotienda");
					fechaInsercion = rs.getString("fechainsercion");
					datFechaInsercion = dateFormat.parse(fechaInsercion);
					break;
				}
				//Con la fecha capturada, vamos a realizar los c�lculos correspondientes
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

}