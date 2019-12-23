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
import Modelo.EmpleadoEvento;
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
				SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
		
		//En este punto vamos a replicar la lógica para procesar y generar el reporte
		
		
		//Instanciamos la respuesta ArrayList
				ArrayList<String[]> respuestaReporte = new ArrayList();
				//Recuperamos el arreglo con los eventos deberemos reprocesarlos para tener la vista qeu requerimos
				ArrayList<EmpleadoEvento>  repEntradasSalidas = ReporteHorariosDAO.obtenerEntradasSalidasEmpleadosEventos(fechaAnterior,fechaActual);
				//Variables necesarias para el recorrido
				EmpleadoEvento eventoTemp;
				//Arreglo donde iremos dejando cada fila
				String[] filaTemp = new String[7];
				//Variables que nos permitiran saber si hubo error en la conversión de las fechas
				boolean errorInicial = false;
				boolean errorFinal = false;
				//Variables qeu nos permitiran saber en que punto vamos de la formación del registro
				boolean ingreso = false;
				//Salida empezará prendido dado que iniciamos con uno nuevo
				boolean salida = true;
				for(int i = 0; i < repEntradasSalidas.size(); i++)
				{
					//Retomamos el evento que vamos a procesar
					eventoTemp = repEntradasSalidas.get(i);
					//Hacemos la verificación de si el evento es de ingreso o de salida
					if(eventoTemp.getTipoEvento().equals(new String("INGRESO")))
					{
						//Esto quiere decir que solo hay un ingreso por lo que llenamos el arreglo
						if(ingreso)
						{
							filaTemp[4] = "0";
							filaTemp[5] = "0";
							respuestaReporte.add(filaTemp);
							filaTemp = new String[7];
							filaTemp[0] = eventoTemp.getNombreEmpleado();
							filaTemp[1] = eventoTemp.getFecha();
							filaTemp[2] = eventoTemp.getDia();
							filaTemp[3] = eventoTemp.getFechaHoraLog();
							filaTemp[6] = Integer.toString(eventoTemp.getIdTienda());
						}if(salida)
						{
							filaTemp = new String[7];
							filaTemp[0] = eventoTemp.getNombreEmpleado();
							filaTemp[1] = eventoTemp.getFecha();
							filaTemp[2] = eventoTemp.getDia();
							filaTemp[3] = eventoTemp.getFechaHoraLog();
							filaTemp[6] = Integer.toString(eventoTemp.getIdTienda());
						}
						ingreso = true;
						salida = false;
					}else if(eventoTemp.getTipoEvento().equals(new String("SALIDA")))
					{
						filaTemp[4] = eventoTemp.getFechaHoraLog();
						//Hacer la resta de tiempos para lo cual formateamos las fechas
						Date fechaFinal = new Date(), fechaInicial = new Date();
						double horas = 0;
						//Intentamos la conversión de las fechas
						try
						{
							fechaInicial=dateFormatHora.parse(filaTemp[3]);
						}catch(Exception e)
						{
							errorInicial = true;
						}
						try
						{
							fechaFinal=dateFormatHora.parse(filaTemp[4]);
						}catch(Exception e)
						{
							errorFinal = true;
						}
				        if(!errorInicial && !errorFinal)
				        {
				        	  horas = ((fechaFinal.getTime()-fechaInicial.getTime())/1000);
				        	  horas =(horas)/3600;
				        }
				        //DecimalFormat df = new DecimalFormat("#.00");
				        filaTemp[5] = Double.toString(horas);
						respuestaReporte.add(filaTemp);
						//volvemos a iniciarlizar las banderas de inicio y final
						errorInicial = false;
						errorFinal = false;
						//Prendemos la variable de salida
						salida = true;
						ingreso = false;
					}
				}
				//A la salida del for damos una revisa si no hay salida entonces se agrega al arreglo del resultado
				if(ingreso && !salida)
				{
					filaTemp[4] = "0";
					filaTemp[5] = "0";
					respuestaReporte.add(filaTemp);
				}
		
		
		//Obtenemos la información consolidada por persona y día
		ArrayList reporteHorarios = respuestaReporte;
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		
		//Comenzamos toda la lógica para recorrer el arreglo de empleados por fecha y pintar la inforación como lo requerimos
		//Variables que nos permitirán almacenar el empleado anterior y revisar si está cambiando con el fin de ir mostrando un camboi
		String empleadoAnterior = "";
		String empleadoActual = "";
		double horas = 0;
		String strHoras = "";
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
				
				horas = Double.parseDouble(fila[5]);
				DecimalFormat df = new DecimalFormat("#.00");
				strHoras = df.format(horas);
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
			respuesta = respuesta + "<tr><td width='120' nowrap>" + fila[0] + "</td><td width='50' nowrap> " + fila[1] + "</td><td width='50' nowrap> " + fila[2] + "</td><td width='50' nowrap> " + fila[3] + "</td><td width='50' nowrap> "+ fila[4] + "</td><td width='50' nowrap> " + strHoras + "</td><td width='50' nowrap> " + tienda +"</td></tr>";
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

