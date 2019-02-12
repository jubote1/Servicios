package CapaDAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAO.TiendaDAO;
import Modelo.Pedido;
import Modelo.Tienda;
import Conexion.ConexionBaseDatos;

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

}
