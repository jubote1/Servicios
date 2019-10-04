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
import CapaDAOServicios.ReporteContactCenterDAO;
import CapaDAOServicios.ReporteHorariosDAO;
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

public class ReporteSemanalHorarios {
	
			
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
		DecimalFormat formatea = new DecimalFormat("###,###.##");
		//En respuesta guardaremos el html que guardará todo lo que se desplegará en el correo.
		String respuesta = "";
		
		//Obtenemos la información consolidada por persona y día
		ArrayList reporteHorarios = ReporteHorariosDAO.obtenerReporteHorarios(fechaAnterior, fechaActual);
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		
		//Comenzamos toda la lógica para recorrer el arreglo de empleados por fecha y pintar la inforación como lo requerimos
		//Variables que nos permitirán almacenar el empleado anterior y revisar si está cambiando con el fin de ir mostrando un camboi
		String empleadoAnterior = "";
		String empleadoActual = "";
		double horas = 0;
		double acumuladoHoras = 0;
		String tienda = "";
		int idTienda;
		for(int i = 0; i < reporteHorarios.size(); i++)
		{
			String[] fila = (String[]) reporteHorarios.get(i);
			empleadoActual = fila[0];
			if(empleadoAnterior.equals(new String("")))
			{
				empleadoAnterior = fila[0];
				respuesta = respuesta + "<table WIDTH='400' border='2'> <TH COLSPAN='6'> " + empleadoActual  + "</TH> </tr>";
				respuesta = respuesta + "<tr>"
						+  "<td width='120' nowrap><strong>NOMBRE</strong></td>"
						+  "<td width='50' nowrap><strong>FECHA</strong></td>"
						+  "<td width='50' nowrap><strong>DIA</strong></td>"
						+  "<td width='50' nowrap><strong>INGRESO</strong></td>"
						+  "<td width='50' nowrap><strong>SALIDA</strong></td>"
						+  "<td width='40' nowrap><strong>HORAS</strong></td>"
						+  "<td width='40' nowrap><strong>TIENDA</strong></td>"
						+  "</tr>";
			}
			
			if(!empleadoAnterior.equals(empleadoActual))
			{
				respuesta = respuesta + "<tr> <td COLSPAN='6' width='400' nowrap><strong>TOTAL HORAS " + acumuladoHoras + "</strong></td> </tr>";
				respuesta = respuesta + "</table> <br/>";
				respuesta = respuesta + "<table WIDTH='400' border='2'> <TH COLSPAN='6'> " + empleadoActual  + "</TH> </tr>";
				respuesta = respuesta + "<tr>"
						+  "<td width='120' nowrap><strong>NOMBRE</strong></td>"
						+  "<td width='50' nowrap><strong>FECHA</strong></td>"
						+  "<td width='50' nowrap><strong>DIA</strong></td>"
						+  "<td width='50' nowrap><strong>INGRESO</strong></td>"
						+  "<td width='50' nowrap><strong>SALIDA</strong></td>"
						+  "<td width='40' nowrap><strong>HORAS</strong></td>"
						+  "<td width='40' nowrap><strong>TIENDA</strong></td>"
						+  "</tr>";
				acumuladoHoras = 0;
			}
			
			//Debemos de cambiar de minutos a horas y debemos de consultar la tienda
			try
			{
				horas = Double.parseDouble(fila[5])/60;
			}catch(Exception e)
			{
				horas = 0;
			}
			acumuladoHoras = acumuladoHoras + horas;
			//Revisamos el tema de la tienda
			try {
				idTienda = Integer.parseInt(fila[6]);
			}catch(Exception e)
			{
				idTienda = 0;
			}
			if(idTienda > 0)
			{
				for(int j = 0; j < tiendas.size(); j++)
				{
					Tienda tiendaTemp = tiendas.get(j);
					if (tiendaTemp.getIdTienda() == idTienda)
					{
						tienda = tiendaTemp.getNombreTienda();
						break;
					}
				}
			}else
			{
				tienda = "No Identificada";
			}
			//Realizamos el pintado de la fila
			respuesta = respuesta + "<tr><td width='120' nowrap>" + fila[0] + "</td><td width='50' nowrap> " + fila[1] + "</td><td width='50' nowrap> " + fila[2] + "</td><td width='50' nowrap> " + fila[3] + "</td><td width='50' nowrap> "+ fila[4] + "</td><td width='50' nowrap> " + formatea.format(horas) + "</td><td width='50' nowrap> " + tienda +"</td></tr>";
			//Al final del procesamiento decimos que el empleadoAnterior es el actual
			empleadoAnterior = empleadoActual;
		}
		respuesta = respuesta + "<tr> <td COLSPAN='6' width='400' nowrap><strong>TOTAL HORAS " + formatea.format(acumuladoHoras) + "</strong></td> </tr>";
		respuesta = respuesta + "</table> <br/>";
		
		
		//Recuperar la lista de distribución para este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEHORAS");
		Date fecha = new Date();
		Correo correo = new Correo();
		correo.setAsunto("GENERAL CUMPLIMIENTO DE HORARIOS SEMANAL DE " + fechaAnterior + " HASTA " + fechaActual);
		correo.setContrasena("Pizzaamericana2017");
		correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
		correo.setMensaje("Resumen de los horarios cumplidos por Empleado: \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
		
		
		//Generamos otro correo con el fin de revisar las personas que no usaron biometria dentro de la semana que acaba de finalizar
		respuesta = "";
		respuesta = respuesta + "<table WIDTH='350' border='2'> <TH COLSPAN='5'> " + "NO REGISTRO DE HUELLA DACTILAR"  + "</TH> </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td width='150' nowrap><strong>NOMBRE</strong></td>"
				+  "<td width='50' nowrap><strong>FECHA</strong></td>"
				+  "<td width='50' nowrap><strong>DIA</strong></td>"
				+  "<td width='50' nowrap><strong>EVENTO</strong></td>"
				+  "<td width='50' nowrap><strong>TIENDA</strong></td>"
				+  "</tr>";
		ArrayList reporteNoUso = ReporteHorariosDAO.obtenerReporteNoUsoHuellero(fechaAnterior, fechaActual);
		for(int i = 0; i < reporteNoUso.size(); i++)
		{
			String[] fila = (String[])reporteNoUso.get(i);
			try {
				idTienda = Integer.parseInt(fila[4]);
			}catch(Exception e)
			{
				idTienda = 0;
			}
			if(idTienda > 0)
			{
				for(int j = 0; j < tiendas.size(); j++)
				{
					Tienda tiendaTemp = tiendas.get(j);
					if (tiendaTemp.getIdTienda() == idTienda)
					{
						tienda = tiendaTemp.getNombreTienda();
						break;
					}
				}
			}else
			{
				tienda = "No Identificada";
			}
			respuesta = respuesta + "<tr><td width='150' nowrap>" + fila[0] + "</td><td width='50' nowrap> " + fila[1] + "</td><td width='50' nowrap> " + fila[2] + "</td><td width='50' nowrap> " + fila[3] + "</td><td width='50' nowrap> " + tienda +"</td></tr>";
		}
		correo.setAsunto("GENERAL PERSONAS Y MOMENTOS DE NO USO DEL HUELLERO DACTILAR DE " + fechaAnterior + " HASTA " + fechaActual);
		correo.setMensaje("Resumen de momentos y empleados que no usaron el huellero: \n" + respuesta);
		contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}
	
	
}

