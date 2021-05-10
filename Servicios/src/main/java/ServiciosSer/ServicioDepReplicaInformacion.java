package ServiciosSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiempoPedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.Correo;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.Pedido;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import utilidadesSer.ControladorEnvioCorreo;

/**
 * Este manual tiene como objetivo realizar la replica de la información de la tienda, en un sistema 
 * consolidador de contact center, y adicionalmente realizar una depuración de las tablas en los sistemas de las tiendas
 * @author juanb
 *
 */
public class ServicioDepReplicaInformacion {
	
			
		
	public static void main( String[] args )
	        
	{
		//Requerimos primero que todo obtener el rango de fechas con el fin de tener dicho rango para las consultas
		//Definimos el formato como manejaremos las fechas
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//Traemos la fechaActual en Blanco
		String fechaActual = "";
		//Traemos la fecha actual en un date
		Date datFechaActual = new Date();
		//Comenzamos a traer la fecha actual como un String
		fechaActual = dateFormat.format(datFechaActual);
		//Con la fecha actual vamos a realizar la consulta de los pedidos
		
		//Dependiendo la fecha realizamos una resta para la fecha inicial para llevar los movimientos
		
		
		//Necesitamos obtener la fecha hora actual menos una hora en el formato para consultar en mysql
		Calendar calendario = Calendar.getInstance();
		calendario.setTime(datFechaActual); 
		calendario.add(Calendar.HOUR, -1);
		Date datFechaMenosHora = calendario.getTime();
		String fechaActualMenosHora = dateFormatHora.format(datFechaMenosHora);
		
		
		//Realizamos la extracción de los tiempos pedidos
		String respuesta = "";
		boolean indicadorCorreo = false;
		ArrayList<TiempoPedido> tiempos = TiempoPedidoDAO.retornarTiemposPedidosLocal();
		respuesta = respuesta + "<table border='2'> <tr> RESUMEN ACTUAL DE TIEMPO DE LAS TIENDAS " + " </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>Tiempo Pedido Actual</strong></td>"
				+  "</tr>";
		for(int i = 0; i < tiempos.size(); i++)
		{
			TiempoPedido tiempoTemp = tiempos.get(i);
			respuesta = respuesta + "<tr><td>" +  tiempoTemp.getTienda() + "</td><td>" +  tiempoTemp.getMinutosPedido() + "</td></tr>";
		}
		
		respuesta = respuesta + "</table> <br/>";
		
		//Recuperaremos las tiendas y empezaremos a ir consultando una a una las tiendas para extraer la información
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		//Vamos a recuperar de manera centralizada los valores de las variables de pedido en espera y pedido en ruta
		int pedidoEmpacado = ParametrosDAO.retornarValorNumericoLocal("EMPACADODOMICILIO");
		int pedidoEnRuta = ParametrosDAO.retornarValorNumericoLocal("ENRUTADOMICILIO");
		//Con los valores recuperados con anterioridad se realizará la consulta a cada una de las tiendas
		int cantPedEmp = 0;
		int cantPedPen = 0;
		int cantPedHora = 0;
		String cantMinutos = "";
		for(Tienda tien : tiendas)
		{
			
			if(!tien.getHostBD().equals(new String("")))
			{
				respuesta = respuesta + "<table border='2'> <tr>" + tien.getNombreTienda() + " </tr>";
				respuesta = respuesta + "<tr>"
						+  "<td><strong>Pedidos Pendientes Salir Tienda</strong></td>"
						+  "<td><strong>Cantidad de Pedidos Última Hora</strong></td>"
						+  "<td><strong>Tiempo último Pedido Pendiente</strong></td>"
						+  "</tr>";
				//Comenzamos a validar los parámetros de cada tienda 
				// LA MEJOR ESTRATEGIA SERÍA TENER UN SOLO MÉTODO PARA MEJORAR EL PERFORMANCE
				//Cantidad de pedidos pendientes por salir de la tienda
				cantPedEmp = PedidoDAO.obtenerCantidadPedidoPorEstado(fechaActual, pedidoEmpacado, tien.getHostBD());
				//Cantidad de pedidos pendientes de la tienda
				cantPedPen =  cantPedEmp + PedidoDAO.obtenerCantidadPedidoPorEstado(fechaActual, pedidoEnRuta, tien.getHostBD());
				//Cantidad de pedidos de la última hora
				cantPedHora = PedidoDAO.obtenerCantidadPedidoDespuesHoraDomicilio(fechaActual, fechaActualMenosHora, tien.getHostBD(),0);
				//Tiempo del último pedimo por salir
				cantMinutos = PedidoDAO.obtenerTiempoUltimoPedidoEstado(fechaActual, pedidoEmpacado, tien.getHostBD(),"");
				//Luego de obtenidos los datos pintamos el html
				respuesta = respuesta + "<tr>"
						+  "<td>" + cantPedEmp + "</td>"
						+  "<td>" + cantPedHora + "</td>"
						+  "<td>" + cantMinutos + "</td>"
						+  "</tr>";
				respuesta = respuesta + "</table> <br/>";
				if(cantPedPen > 0)
				{
					indicadorCorreo = true;
				}
			}
		}
		
		
		
		//Si hay indicador de que se debe enviar correo se recuperará la variable y se enviará el pedido
		if(indicadorCorreo)
		{
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPRESUMENOPERACION");
			Date fecha = new Date();
			Correo correo = new Correo();
			correo.setAsunto("OPERACION GENERAL " + fecha.toString());
			correo.setContrasena("Pizzaamericana2017");
			correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
			correo.setMensaje("A continuación el detalle de la operación de Pizza Americana: \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
	}
		
	
}

