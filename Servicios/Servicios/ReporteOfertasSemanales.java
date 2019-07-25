package Servicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOServicios.GeneralDAO;
import CapaDAOServicios.OfertaClienteDAO;
import CapaDAOServicios.ParametrosDAO;
import CapaDAOServicios.PedidoDAO;
import CapaDAOServicios.PedidoFueraTiempoDAO;
import CapaDAOServicios.PedidoPOSPMDAO;
import CapaDAOServicios.PedidoPixelDAO;
import CapaDAOServicios.TiempoPedidoDAO;
import CapaDAOServicios.TiendaDAO;
import ConexionServicios.ConexionBaseDatos;
import Modelo.Pedido;
import Modelo.PedidoFueraTiempo;
import Modelo.PedidoPixel;
import Modelo.TiempoPedido;
import Modelo.Tienda;
import Modelo.Correo;
import Modelo.OfertaCliente;
import utilidades.ControladorEnvioCorreo; 

public class ReporteOfertasSemanales {
	
			
		
	public static void main( String[] args )
	        
	{
		//Requerimos primero que todo obtener el rango de fechas con el fin de tener dicho rango para las consultas
		//Definimos el formato como manejaremos las fechas
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		//Traemos la fechaActual en Blanco
		String fechaActual = "";
		String fechaAnterior = "";
		//Traemos la fecha actual en un date
		Date datFechaActual = new Date();
		Date datFechaAnterior;
		//Comenzamos a traer la fecha actual como un String
		fechaActual = dateFormat.format(datFechaActual);
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		try
		{
			//Al objeto calendario le fijamos la fecha actual del sitema
			calendarioActual.setTime(dateFormat.parse(fechaActual));
			
		}catch(Exception e)
		{
			System.out.println(e.toString());
		}
		//Retormanos el día de la semana actual segun la fecha del calendario
		int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
		//Domingo
		if(diaActual == 1)
		{
			calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
		}
		else if(diaActual == 2)
		{
			calendarioActual.add(Calendar.DAY_OF_YEAR, -7);
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
		System.out.println("LA FECHA ANTERIOR ES " + fechaAnterior);
		//En este punto ya tenemos las dos fechas de interés por el momento nos interesará retornar las ofertas dadas
		// y las ofertas redimidas en estos rango de tiempo
		ArrayList<OfertaCliente> ofertasNuevas = OfertaClienteDAO.obtenerOfertasNuevasSemana(fechaActual, fechaAnterior);
		ArrayList<OfertaCliente> ofertasRedimidas = OfertaClienteDAO.obtenerOfertasRedimidasSemana(fechaActual, fechaAnterior);
		
		//Se crea la variable que se encargará de la respuesta
		String respuesta = "";
		
		//ESPACIO PARA EXTRAER LAS OFERTAS NUEVAS
		respuesta = respuesta + "<table border='2'> <tr> RESUMEN ACTUAL DE LAS OFERTAS DADAS A CLIENTES " + " </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Oferta</strong></td>"
				+  "<td><strong>Utilizada</strong></td>"
				+  "<td><strong>Ingreso Oferta</strong></td>"
				+  "<td><strong>Observacion</strong></td>"
				+  "<td><strong>PQRS</strong></td>"
				+  "</tr>";
		for(int i = 0; i < ofertasNuevas.size(); i++)
		{
			OfertaCliente ofertaNueva = ofertasNuevas.get(i);
			respuesta = respuesta + "<tr><td>" +  ofertaNueva.getNombreOferta() + "</td><td>" +  ofertaNueva.getUtilizada() + "</td><td>" + ofertaNueva.getIngresoOferta() + "</td><td>" + ofertaNueva.getObservacion() + "</td><td>" + ofertaNueva.getPQRS() + "</td></tr>";
		}
		
		respuesta = respuesta + "</table> <br/>";
		
		//ESPACIO PARA EXTRAER LAS OFERTAS REDIMIDAS
		respuesta = respuesta + "<table border='2'> <tr> RESUMEN ACTUAL DE LAS OFERTAS REDIMIDAS " + " </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Oferta</strong></td>"
				+  "<td><strong>Utilizada</strong></td>"
				+  "<td><strong>Ingreso Oferta</strong></td>"
				+  "<td><strong>Ingreso Usada</strong></td>"
				+  "<td><strong>Observacion</strong></td>"
				+  "<td><strong>PQRS</strong></td>"
				+  "</tr>";
		for(int i = 0; i < ofertasRedimidas.size(); i++)
		{
			OfertaCliente ofertaRedi = ofertasRedimidas.get(i);
			respuesta = respuesta + "<tr><td>" +  ofertaRedi.getNombreOferta() + "</td><td>" +  ofertaRedi.getUtilizada() + "</td><td>" + ofertaRedi.getIngresoOferta() + "</td><td>" + ofertaRedi.getUsoOferta() + "</td><td>" + ofertaRedi.getObservacion() + "</td><td>" + ofertaRedi.getPQRS() + "</td></tr>";
		}
		
		respuesta = respuesta + "</table> <br/>";
		
		//Recuperar la lista de distribución para este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPUSOOFERTAS");
		Date fecha = new Date();
		Correo correo = new Correo();
		correo.setAsunto("REPORTE SEMANAL ASIGNACIÓN/USO OFERTAS CLIENTES " + fecha.toString() + " ENTRE FECHAS " + fechaAnterior + "-" + fechaActual);
		correo.setContrasena("Pizzaamericana2017");
		correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
		correo.setMensaje("Informe semanal para conocer la creación y uso de ofertas de clientes: \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
		

	}
		
	
}

