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
import CapaDAOSer.PedidoFueraTiempoDAO;
import CapaDAOSer.PedidoPOSPMDAO;
import CapaDAOSer.PedidoPixelDAO;
import CapaDAOSer.ReporteContactCenterDAO;
import CapaDAOSer.TiempoPedidoDAO;
import CapaDAOSer.TiendaDAO;
import ConexionSer.ConexionBaseDatos;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.Pedido;
import ModeloSer.PedidoFueraTiempo;
import ModeloSer.PedidoPixel;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioReporteContactCenter {
	
			
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
					calendarioActual.add(Calendar.DAY_OF_YEAR, -8);
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
		
		//Cantidad de pedidos tomados en la SEMANA
		int cantidadPedidos = ReporteContactCenterDAO.obtenerCantidadPedidos(fechaAnterior, fechaActual);
		respuesta = respuesta + "<table WIDTH='250' border='2'> <TH COLSPAN='2'> CANTIDAD PEDIDOS TOMADOS EN CONTACT" + "</TH> </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td width='250' nowrap><strong>CANTIDAD</strong></td>"
				+  "</tr>";
		respuesta = respuesta + "<tr><td width='250' nowrap>" + cantidadPedidos + "</td></tr>";
		respuesta = respuesta + "</table> <br/>";

		//Cantidad de Pedidos por Persona
		ArrayList cantPedPersona = ReporteContactCenterDAO.obtenerPedidosUsuario(fechaAnterior, fechaActual);
		respuesta = respuesta + "<table WIDTH='250' border='2'> <TH COLSPAN='2'> PEDIDOS TOMADOS POR PERSONA "  + "</TH> </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td width='190' nowrap><strong>NOMBRE PERSONA</strong></td>"
				+  "<td width='60' nowrap><strong>CANTIDAD</strong></td>"
				+  "</tr>";
		String[] fila;
		for(int y = 0; y < cantPedPersona.size();y++)
		{
			fila = (String[]) cantPedPersona.get(y);
			respuesta = respuesta + "<tr><td width='190' nowrap>" + fila[0] + "</td><td width='60' nowrap> " + fila[1]  +"</td></tr>";
		}
		respuesta = respuesta + "</table> <br/>";
		
		
		//Cantidad de Pedidos por Fecha
				ArrayList cantPedFecha = ReporteContactCenterDAO.obtenerPedidosDia(fechaAnterior, fechaActual);
				respuesta = respuesta + "<table WIDTH='250' border='2'> <TH COLSPAN='2'> PEDIDOS TOMADOS POR FECHA"  + "</TH> </tr>";
				respuesta = respuesta + "<tr>"
						+  "<td width='140' nowrap><strong>FECHA</strong></td>"
						+  "<td width='110' nowrap><strong>CANTIDAD</strong></td>"
						+  "</tr>";
				for(int y = 0; y < cantPedFecha.size();y++)
				{
					fila = (String[]) cantPedFecha.get(y);
					respuesta = respuesta + "<tr><td width='140' nowrap>" + fila[0] + "</td><td width='110' nowrap> " + fila[1] +"</td></tr>";
				}
				respuesta = respuesta + "</table> <br/>";		
		
		
		//Cantidad de Pedidos por Fecha y Hora
		ArrayList cantPedFechaHora = ReporteContactCenterDAO.obtenerPedidosDiaHora(fechaAnterior, fechaActual);
		respuesta = respuesta + "<table WIDTH='250' border='2'> <TH COLSPAN='2'> PEDIDOS TOMADOS POR FECHA Y HORA "  + "</TH> </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td width='140' nowrap><strong>FECHA</strong></td>"
				+  "<td width='50' nowrap><strong>HORA</strong></td>"
				+  "<td width='60' nowrap><strong>CANTIDAD</strong></td>"
				+  "</tr>";
		for(int y = 0; y < cantPedFechaHora.size();y++)
		{
			fila = (String[]) cantPedFechaHora.get(y);
			respuesta = respuesta + "<tr><td width='140' nowrap>" + fila[0] + "</td><td width='50' nowrap> " + fila[1] + "</td><td width='60' nowrap> " + fila[2]  +"</td></tr>";
		}
		respuesta = respuesta + "</table> <br/>";		

		//Cantidad de Pedidos por mes de los últimos 18 meses
		ArrayList cantPedMes = ReporteContactCenterDAO.obtenerCantidadPedidosMes();
		respuesta = respuesta + "<table WIDTH='250' border='2'> <TH COLSPAN='2'> CANTIDAD PEDIDOS DE LOS ÚLTIMOS MESES "  + "</TH> </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td width='140' nowrap><strong>AÑO - MES</strong></td>"
				+  "<td width='60' nowrap><strong>CANTIDAD</strong></td>"
				+  "</tr>";
		int contador = 1;
		for(int y = 0; y < cantPedMes.size();y++)
		{
			if(contador > 19)
			{
				break;
			}
			fila = (String[]) cantPedMes .get(y);
			respuesta = respuesta + "<tr><td width='140' nowrap>" + fila[0] + "</td><td width='110' nowrap> " + fila[1] + "</td></tr>";
			contador = contador +1 ;
		}
		respuesta = respuesta + "</table> <br/>";
		
		//Agregamos la información de tienda virtual
		ArrayList cantVirtualPersona = ReporteContactCenterDAO.obtenerPedidosVirtualPorUsuario(fechaAnterior, fechaActual);
		respuesta = respuesta + "<table WIDTH='250' border='2'> <TH COLSPAN='2'> CANTIDAD PEDIDOS TIENDA VIRTUAL POR USUARIO "  + "</TH> </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td width='140' nowrap><strong>USUARIO QUE REALIZÓ ENVIO</strong></td>"
				+  "<td width='60' nowrap><strong>CANTIDAD</strong></td>"
				+  "</tr>";
		for(int y = 0; y < cantVirtualPersona.size();y++)
		{
			fila = (String[]) cantVirtualPersona .get(y);
			respuesta = respuesta + "<tr><td width='140' nowrap>" + fila[0] + "</td><td width='110' nowrap> " + fila[1] + "</td></tr>";
			contador = contador +1 ;
		}
		respuesta = respuesta + "</table> <br/>";
		
		
	
		//Recuperar la lista de distribución para este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTESEMANALCONTACT");
		Date fecha = new Date();
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("CONTACT CENTER SEMANAL DE " + fechaAnterior + " HASTA " + fechaActual);
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("Resumen Semanal de estadísticas Contact Center: \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();

	}
	
	
}

