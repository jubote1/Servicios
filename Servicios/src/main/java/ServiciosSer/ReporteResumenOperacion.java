package ServiciosSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
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
import capaControladorPOS.PedidoCtrl;
import capaControladorPOS.BiometriaCtrl;
import capaControladorPOS.EmpleadoCtrl;

public class ReporteResumenOperacion {
	
			
		
	public static void main( String[] args )  
	{
		//Requerimos primero que todo obtener el rango de fechas con el fin de tener dicho rango para las consultas
		//Definimos el formato como manejaremos las fechas
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DecimalFormat formatea = new DecimalFormat("###,###");
		//Traemos la fechaActual en Blanco
		String fechaActual = "";
		//Traemos la fecha actual en un date
		Date datFechaActual = new Date();
		//Comenzamos a traer la fecha actual como un String
		fechaActual = dateFormat.format(datFechaActual);
		//Con la fecha actual vamos a realizar la consulta de los pedidos
		
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
		int tipoPedidoDomicilio = ParametrosDAO.retornarValorNumericoLocal("TIPOPEDIDODOMICILIO");
		//Con los valores recuperados con anterioridad se realizará la consulta a cada una de las tiendas
		int cantPedEmp = 0;
		int cantPedPen = 0;
		int cantPedHoraDom = 0;
		int cantPedHoraNoDom = 0;
		String cantMinutos = "";
		for(Tienda tien : tiendas)
		{
			
			if(!tien.getHostBD().equals(new String("")))
			{
				respuesta = respuesta + "<table border='2'><tr><td colspan='4'>" + tien.getNombreTienda() + "</td></tr>";
				respuesta = respuesta + "<tr>"
						+  "<td><strong>Ped Pend Salir Tienda</strong></td>"
						+  "<td><strong>Cant de Ped Últ Hora Domicilio</strong></td>"
						+  "<td><strong>Cant de Ped Últ Hora No Domicilio</strong></td>"
						+  "<td><strong>Tiempo último Ped Pend</strong></td>"
						+  "</tr>";
				//Comenzamos a validar los parámetros de cada tienda 
				// LA MEJOR ESTRATEGIA SERÍA TENER UN SOLO MÉTODO PARA MEJORAR EL PERFORMANCE
				//Cantidad de pedidos pendientes por salir de la tienda
				cantPedEmp = PedidoDAO.obtenerCantidadPedidoPorEstado(fechaActual, pedidoEmpacado, tien.getHostBD());
				//Cantidad de pedidos pendientes de la tienda
				cantPedPen =  cantPedEmp + PedidoDAO.obtenerCantidadPedidoPorEstado(fechaActual, pedidoEnRuta, tien.getHostBD());
				//Cantidad de pedidos de la última hora Domicilio
				cantPedHoraDom = PedidoDAO.obtenerCantidadPedidoDespuesHoraDomicilio(fechaActual, fechaActualMenosHora, tien.getHostBD(),tipoPedidoDomicilio );
				//Cantidad de pedidos de la última hora Domicilio
				cantPedHoraNoDom = PedidoDAO.obtenerCantidadPedidoDespuesHoraNoDomicilio(fechaActual, fechaActualMenosHora, tien.getHostBD(),tipoPedidoDomicilio );
				//Tiempo del último pedimo por salir
				cantMinutos = PedidoDAO.obtenerTiempoUltimoPedidoEstado(fechaActual, pedidoEmpacado, tien.getHostBD());
				//Luego de obtenidos los datos pintamos el html
				respuesta = respuesta + "<tr>"
						+  "<td>" + cantPedEmp + "</td>"
						+  "<td>" + cantPedHoraDom + "</td>"
						+  "<td>" + cantPedHoraNoDom + "</td>"
						+  "<td>" + cantMinutos + "</td>"
						+  "</tr>";
				respuesta = respuesta + "</table> <br/>";
				
				//Generamos una segunda tabla por tienda
				respuesta = respuesta + "<table border='2'><tr><td colspan='3'>" + tien.getNombreTienda() + "</td></tr>";
				respuesta = respuesta + "<tr>"
						+  "<td><strong>VENTA</strong></td>"
						+  "<td><strong>Cant Domiciliario Interno</strong></td>"
						+  "<td><strong>Cant Domiciliario Externos</strong></td>"
						+  "</tr>";
				//En este punto verificamos vendido de la tienda hasta el momento
				PedidoCtrl pedCtrl = new PedidoCtrl(false);
				double totalVentaDia = pedCtrl.obtenerTotalesPedidosDia(fechaActual, tien.getHostBD());
				BiometriaCtrl bioCtrl = new BiometriaCtrl(false);
				int cantDomInt = bioCtrl.cantidadEmpleadoDomiciliario(fechaActual, tien.getIdTienda());
				EmpleadoCtrl empCtrl  = new EmpleadoCtrl(false);
				int cantDomExt = empCtrl.consultarCantEmpleadoTempDia(fechaActual, tien.getHostBD());
				respuesta = respuesta + "<tr>"
						+  "<td>" + formatea.format(totalVentaDia) + "</td>"
						+  "<td>" + cantDomInt + "</td>"
						+  "<td>" + cantDomExt + "</td>"
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

