package ServiciosSer;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import CapaDAOSer.GeneralDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import capaControladorCC.TiendaBloqueadaCtrl;
import capaControladorPOS.PedidoCtrl;
import capaDAOCC.LogBloqueoTiendaDAO;
import capaDAOCC.TiendaBloqueadaDAO;
import capaDAOFirebase.CrudFirebase;
import capaModeloCC.LogBloqueoTienda;
import capaModeloCC.Tienda;
import capaModeloCC.TiendaBloqueada;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteDatosClientesPuntoVenta {
	
	public static void main(String[] args)
	{
		
		String fechaActual = "";
		//Variables donde manejaremos la fecha anerior con el fin realizar los cálculos de ventas
		Date datFechaAnterior;
		String fechaAnterior = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//Formato para mostrar las cantidades
		DecimalFormat formatea = new DecimalFormat("###,###");
		//Obtenemos la fecha Actual
		try
		{
			//OJO
			fechaActual = dateFormat.format(calendarioActual.getTime());
			//fechaActual = "2020-07-26";
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
		//Comenzamos por obtener todas las tiendas y realizar un recorrido de cada una
		ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendas();
		String respuesta = "";
		long minutosBloqueo = 0;
		double horasBloqueo = 0;
		String motivo = "";
		String observacion = "";
		for(Tienda tiendaTemp: tiendas)
		{
			respuesta = respuesta + "<table WIDTH='700' border='2'> <tr> <td colspan='4'> REPORTE BLOQUEOS DE TIENDA - " + tiendaTemp.getNombreTienda() + " " + fechaAnterior + "  -  " + fechaActual +  "</td></tr>";
			respuesta = respuesta + "<tr>"
					+  "<td WIDTH='100'><strong>FECHA</strong></td>"
					+  "<td WIDTH='160'><strong>HORA BLOQUEO</strong></td>"
					+  "<td WIDTH='160'><strong>HORA DESBLOQUEO</strong></td>"
					+  "<td WIDTH='120'><strong>MOTIVO</strong></td>"
					+  "<td WIDTH='130'><strong>OBS</strong></td>"
					+  "<td WIDTH='30'><strong>MINUTOS BLOQUEO</strong></td>"
					+  "</tr>";
			//Obtendremos los bloqueos de cada tienda para revisar la situaciones
			ArrayList<LogBloqueoTienda> bloqueosTienda = LogBloqueoTiendaDAO.obtenerHistorialTienda(tiendaTemp.getIdTienda(), fechaAnterior, fechaActual);
			//Recorremos los registros del bloqueo
			//Recorremos para encontrar el primer bloqueo
			boolean primerBloqueo = false;
			//Se definen las variables Date para almacenar las fechas de bloqueo
			Date fechaInicial = null;
			Date fechaBloqueo = null;
			Date fechaDesbloqueo;
			for(LogBloqueoTienda logTemp: bloqueosTienda)
			{
				//Es porque no ha encontrado el primer bloqueo que es vital para encontrar el primer bloqueo
				if(!primerBloqueo)
				{
					if(logTemp.getAccion().equals(new String("DESBLOQUEO")))
					{
						continue;
					}else if(logTemp.getAccion().equals(new String("BLOQUEO")))
					{
						primerBloqueo = true;
					}
				}
				if(logTemp.getAccion().equals(new String("BLOQUEO")))
				{
					try
					{
						fechaInicial = dateFormat.parse(logTemp.getFechaAccion());
						fechaBloqueo = dateFormatHora.parse(logTemp.getFechaAccion());
						motivo = logTemp.getMotivo();
						observacion = logTemp.getObservacion();
					}catch(Exception e)
					{
						fechaInicial = new Date();
						fechaBloqueo = new Date();
					}
					
				}else if(logTemp.getAccion().equals(new String("DESBLOQUEO")))
				{
					try
					{
						fechaDesbloqueo = dateFormatHora.parse(logTemp.getFechaAccion());
					}catch(Exception e)
					{
						fechaDesbloqueo = new Date();
					}
					//Necesitamos calcular la diferencia en minutos de 2 fechas para tomar la decisión del desbloqueo
					long dif = fechaDesbloqueo.getTime() - fechaBloqueo.getTime();
					long difMinutos = TimeUnit.MILLISECONDS.toMinutes(dif);
					minutosBloqueo = minutosBloqueo + difMinutos;
					//Generamos el registro para la información y su presentación
					respuesta = respuesta + "<tr><td>" + dateFormat.format(fechaInicial) +  "</td><td>" + dateFormatHora.format(fechaBloqueo) + "</td><td>" + dateFormatHora.format(fechaDesbloqueo) + "</td><td>" + motivo + "</td><td>" + observacion + "</td><td>" + Long.toString(difMinutos)  + "</td></tr>";
					fechaBloqueo = null;
					fechaDesbloqueo = null;
					motivo = "";
					observacion = "";
				}
				
				
			}
			horasBloqueo = ((double)minutosBloqueo)/60;
			respuesta = respuesta + "<tr> <td colspan='4'> TOTAL MINUTOS BLOQUEO " + minutosBloqueo +  "</td></tr>";
			respuesta = respuesta + "<tr> <td colspan='4'> TOTAL HORAS BLOQUEO " + horasBloqueo +  "</td></tr>";
			respuesta = respuesta + "</table> <br/>";
			minutosBloqueo = 0;
			horasBloqueo = 0;
		}
		//Realizamos el envío del correo electrónico
		//Al final el envío del correo
		//Procedemos al envío del correo
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("REPORTE BLOQUEO TIENDAS " + fechaAnterior + " HASTA "  + fechaActual);
		correo.setContrasena(infoCorreo.getClaveCorreo());
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEBLOQUEOTIENDAS");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuación el reporte de bloqueo de las tiendas entre las fechas " + fechaAnterior + " - " + fechaActual +  ": \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();

	}
}
