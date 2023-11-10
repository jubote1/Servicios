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
import ModeloSer.PedidoAPP;
import ModeloSer.PedidoFueraTiempo;
import ModeloSer.PedidoPixel;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteSemanalAPP {
	
			
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
		
		
		//Cantidad de Pedidos por Persona
		int totalPedContact = ReporteContactCenterDAO.obtenerCantidadPedidosContact(fechaAnterior, fechaActual);
		int totalPedVirtual = ReporteContactCenterDAO.obtenerCantidadPedidosVirtual(fechaAnterior, fechaActual);
		int totalPedAPP = ReporteContactCenterDAO.obtenerCantidadPedidosAPP(fechaAnterior, fechaActual);
		int totalPedidos = totalPedContact + totalPedVirtual + totalPedAPP;
		ArrayList<PedidoAPP> pedidosAPP = ReporteContactCenterDAO.obtenerCantidadClientesPedidosAPP(fechaAnterior, fechaActual);
		double clientesExistentes = 0;
		double clientesNuevos = 0;
		double totalClientes = pedidosAPP.size();
		for(PedidoAPP pedidoTemp:pedidosAPP)
		{
			if(pedidoTemp.getCantidad() == 1)
			{
				clientesNuevos++;
			}else
			{
				clientesExistentes++;
			}
		}
		double porcentajeAPP = ((double)totalPedAPP/(double)totalPedidos)*100;
		double porCliNuevos = (clientesNuevos/totalClientes)*100;
		double porCliExistentes = (clientesExistentes/totalClientes)*100;
		respuesta = respuesta + "<table WIDTH='350' border='4'> <TH COLSPAN='4'> RESUMEN SEMANAL APP "  + "</TH> </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td width='60' nowrap><strong>CANT PEDIDOS APP</strong></td>"
				+  "<td width='60' nowrap><strong>% PARTICICPACION</strong></td>"
				+  "<td width='60' nowrap><strong>% CLIENTES NUEVOS</strong></td>"
				+  "<td width='60' nowrap><strong>% CLIENTES EXISTENTES</strong></td>"
				+  "</tr>";
		respuesta = respuesta + "<tr><td>" + totalPedAPP + "</td><td> " + formatea.format(porcentajeAPP) + "</td><td>" + formatea.format(porCliNuevos) + "</td><td> " + formatea.format(porCliExistentes)  +"</td></tr>";
		respuesta = respuesta + "</table> <br/>";
		try
		{
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTESEMANALAPP");
			Date fecha = new Date();
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("REPORTE SEMANAL APP " + fechaAnterior + " HASTA " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("Resumen Semanal de pedidos totales Contact Center y virtual: \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}catch (Exception e)
		{
			System.out.println(e.toString());
		}
		

	}
	
	
}

