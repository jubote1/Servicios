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

import CapaDAOSer.EmpleadoTemporalDiaDAO;
import CapaDAOSer.EmpresaTemporalDAO;
import CapaDAOSer.EstadisticaVentaProductoDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiempoPedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.DiaFestivo;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.EmpleadoTemporalDia;
import ModeloSer.EmpresaTemporal;
import ModeloSer.EstadisticaVentaProducto;
import ModeloSer.Pedido;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import capaControladorPOS.PedidoCtrl;
import capaDAOPOS.DatafonoCierreDAO;
import capaModeloPOS.DatafonoCierre;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteDiarioEstadisticaVenta {
	
			
		
	public static void main( String[] args )
	        
	{
		//Requerimos primero que todo obtener el rango de fechas con el fin de tener dicho rango para las consultas
		//Definimos el formato como manejaremos las fechas
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DecimalFormat formatea = new DecimalFormat("###,###.##");
		//Traemos la fechaActual en Blanco
		String fechaActual = "";
		//Traemos la fecha actual en un date
		Date datFechaActual = new Date();
		
		//Realizamos la operaci�n para restar un d�a a la fecha teniendo en cuenta que correr� m�s tarde
		Calendar calendarioActual = Calendar.getInstance();
		try
		{
			//Al objeto calendario le fijamos la fecha actual del sitema
			calendarioActual.setTime(datFechaActual);
			
		}catch(Exception e)
		{
			System.out.println(e.toString());
		}
		//calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
		datFechaActual = calendarioActual.getTime();
		
		//Comenzamos a traer la fecha actual como un String
		fechaActual = dateFormat.format(datFechaActual);
		//El proceso correra  las 11:50 pm
		
		
		String respuesta = "";
		
		//Recuperaremos las tiendas y empezaremos a ir consultando una a una las tiendas para extraer la informaci�n
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		//Obtendremos los productos a los cuales hay que sacarle estadisticas
		ArrayList<EstadisticaVentaProducto> estadisticas = EstadisticaVentaProductoDAO.retornarEstadisticasVentaProducto("DIARIA");
		PedidoCtrl pedCtrl = new PedidoCtrl(false);
		String nombreProducto = "";
		int cantidadVentas = 0;
		for(Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				respuesta = respuesta + "<table border='2'> <tr><td colspan ='2'>" + tien.getNombreTienda() + " ESTADISTICA VENTA DE PRODUCTOS " + fechaActual  + "</td></tr>";
				respuesta = respuesta + "<tr>"
						+  "<td><strong>PRODUCTO</strong></td>"
						+  "<td><strong>CANTIDAD VENTAS</strong></td></tr>";
				for(EstadisticaVentaProducto est : estadisticas)
				{
					nombreProducto = TiendaDAO.obtenerNombreProducto(est.getIdProducto(), tien.getHostBD(), false);
					cantidadVentas = TiendaDAO.obtenerCantidadVentas(est.getIdProducto(), fechaActual, fechaActual, tien.getHostBD(), false);
					respuesta = respuesta + "<tr>"
							+  "<td><strong>" + nombreProducto+ "</strong></td>"
							+  "<td><strong> " + cantidadVentas +"</strong></td>"
							+  "</tr>";
				}
				respuesta = respuesta + "</table> <br/>";
				
			}
		}
		//Recuperar la lista de distribuci�n para este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("ESTADISTICAVENTATIENDA");
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("REPORTE ESTADISTICA VENTAS TIENDA " + fechaActual);
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuacion el resumen de venta de productos especiales tiendas " + fechaActual +": \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
		
	}
	
}

