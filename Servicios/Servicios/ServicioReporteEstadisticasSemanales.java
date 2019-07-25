package Servicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOServicios.GeneralDAO;
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
import utilidades.ControladorEnvioCorreo;

public class ServicioReporteEstadisticasSemanales {
	
			
	public static void main( String[] args )
	        
	{
		//TRABAJO CON LAS FECHAS///////
		//Recuperamos la fecha actual del sistema con la fecha apertura
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
					//fechaActual = "2019-05-13";
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
		///////////////////////////////
		DecimalFormat formatea = new DecimalFormat("###,###");
		//En respuesta guardaremos el html que guardará todo lo que se desplegará en el correo.
		String respuesta = "";
	    //obtenenemos todas las tiendas
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		Tienda tienda;
		double cantidadPizzasSemana = 0;
		//Realizamos un recorrido de las tiendas
		for(int i = 0; i < tiendas.size(); i++)
		{
			tienda = tiendas.get(i);
			String url = tienda.getHostBD();
			if(!tienda.getHostBD().equals(new String("")))
			{
				//Adicionaremos a la respuesta y al informe diario unas estádisticas de total de pizzas vendidas y total especialidades
				ArrayList resumenPizzasTamano = PedidoDAO.obtenerTotalPizzasFechas(fechaAnterior, fechaActual, url);
				respuesta = respuesta + "<table WIDTH='250' border='2'> <TH COLSPAN='2'> RESUMEN POR TAMAÑOS DE PIZZA " + tienda.getNombreTienda() + "</TH> </tr>";
				respuesta = respuesta + "<tr>"
						+  "<td width='150' nowrap><strong>TAMAÑO</strong></td>"
						+  "<td width='100' nowrap><strong>CANTIDAD</strong></td>"
						+  "</tr>";
				String[] fila;
				for(int y = 0; y < resumenPizzasTamano.size();y++)
				{
					fila = (String[]) resumenPizzasTamano.get(y);
					respuesta = respuesta + "<tr><td width='150' nowrap>" + fila[0] + "</td><td width='100' nowrap> " + fila[1]  +"</td></tr>";
					cantidadPizzasSemana = cantidadPizzasSemana + Double.parseDouble(fila[1]);
				}
				
				respuesta = respuesta + "</table> <br/>";
				//Adicionaremos un total de pizzas por tipo 
				ArrayList resumenPizzasTipo = PedidoDAO.obtenerTotalTipoFechas(fechaAnterior, fechaActual, url);
				respuesta = respuesta + "<table WIDTH='250' border='2'> <tr> <TH COLSPAN='2'> RESUMEN TIPO DE PIZZA " + tienda.getNombreTienda() + " </TH> </tr>";
				respuesta = respuesta + "<tr>"
						+  "<td width='150' nowrap><strong>TIPO PIZZA</strong></td>"
						+  "<td width='100' nowrap><strong>CANTIDAD</strong></td>"
						+  "</tr>";
				for(int y = 0; y < resumenPizzasTipo.size();y++)
				{
					fila = (String[]) resumenPizzasTipo.get(y);
					respuesta = respuesta + "<tr><td width='150' nowrap>" + fila[0] + "</td><td width='100' nowrap> " + fila[1]  +"</td></tr>";
				}
				respuesta = respuesta + "</table> <br/>";
			}
		}
		respuesta = respuesta + "<tr><td>" + "TOTAL DE PIZZAS DE LA SEMANA" + "</td><td> " + formatea.format(cantidadPizzasSemana)  +"</td></tr>";
		
	
		//Recuperar la lista de distribución para este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPESTADISTICASSEMANAL");
		Date fecha = new Date();
		Correo correo = new Correo();
		correo.setAsunto("REPORTE ESTADÍSTICAS SEMANAL DE " + fechaAnterior + " HASTA " + fechaActual);
		correo.setContrasena("Pizzaamericana2017");
		correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
		correo.setMensaje("Estádistica Semanal de Pizzas vendidas: \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();

	}
	
	
}

