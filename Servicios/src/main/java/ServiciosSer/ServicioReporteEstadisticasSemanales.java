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

import CapaDAOSer.EstadisticaProductoDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.PedidoFueraTiempoDAO;
import CapaDAOSer.PedidoPOSPMDAO;
import CapaDAOSer.PedidoPixelDAO;
import CapaDAOSer.TiempoPedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UbicacionDomiciliarioDAO;
import ConexionSer.ConexionBaseDatos;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.EstadisticaProducto;
import ModeloSer.Pedido;
import ModeloSer.PedidoFueraTiempo;
import ModeloSer.PedidoPixel;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioReporteEstadisticasSemanales {
	
			
	public static void main( String[] args )
	        
	{
		//TRABAJO CON LAS FECHAS///////
		//Recuperamos la fecha actual del sistema con la fecha apertura
				String fechaActual = "";
				//Variables donde manejaremos la fecha anerior con el fin realizar los c�lculos de ventas
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
				//Retormanos el d�a de la semana actual segun la fecha del calendario
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
				//Llevamos a un string la fecha anterior para el c�lculo de la venta
				datFechaAnterior = calendarioActual.getTime();
				fechaAnterior = dateFormat.format(datFechaAnterior);
		///////////////////////////////
		DecimalFormat formatea = new DecimalFormat("###,###");
		//En respuesta guardaremos el html que guardar� todo lo que se desplegar� en el correo.
		String respuesta = "";
		String respuestaProblema = "";
	    //obtenenemos todas las tiendas
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		Tienda tienda;
		String fila[];
		EstadisticaProducto estProducto = new EstadisticaProducto();
		//Realizamos un recorrido de las tiendas
		for(int i = 0; i < tiendas.size(); i++)
		{
			tienda = tiendas.get(i);
			String url = tienda.getHostBD();
			if(!tienda.getHostBD().equals(new String("")))
			{
				//Adicionaremos un total de pizzas por tipo 
				ArrayList resumenPizzasTipo = PedidoDAO.obtenerTotalTipoFechas(fechaAnterior, fechaActual, url);
				for(int y = 0; y < resumenPizzasTipo.size();y++)
				{
					fila = (String[]) resumenPizzasTipo.get(y);
					estProducto = new EstadisticaProducto();
					estProducto.setIdTienda(tienda.getIdTienda());
					estProducto.setFecha(fechaActual);
					estProducto.setDescripcion(fila[0]);
					estProducto.setCantidad(Double.parseDouble(fila[1]));
					estProducto.setTotal(Double.parseDouble(fila[2]));
					estProducto.setTamano(fila[3]);
					EstadisticaProductoDAO.insertarEstadisticaProducto(estProducto);
				}
				if(resumenPizzasTipo.size()>0)
				{
					respuesta = respuesta + " " + tienda.getNombreTienda();
				}else
				{
					respuestaProblema = respuestaProblema + " " + tienda.getNombreTienda();
				}
			}else
			{
				respuestaProblema = respuestaProblema + " " + tienda.getNombreTienda();
			}
		}
		//Realizamos la depuraci�n de la tabla insertar ubicaci�n domiciliario
		UbicacionDomiciliarioDAO.depurarUbicacionDomiciliario();
		
		//Recuperar la lista de distribuci�n para este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPESTADISTICASSEMANAL");
		Date fecha = new Date();
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("REPORTE ESTAD�STICAS SEMANAL DE " + fechaAnterior + " HASTA " + fechaActual);
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("La informaci�n de las estad�sticas ha sido replicada de manera correcta en: \n" + respuesta + " y de manera incorrecta en " + respuestaProblema);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();

	}
	
	
}

