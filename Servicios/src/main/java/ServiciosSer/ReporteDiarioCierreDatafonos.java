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
import ModeloSer.Pedido;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import capaDAOPOS.DatafonoCierreDAO;
import capaModeloPOS.DatafonoCierre;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteDiarioCierreDatafonos {
	
			
		
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
		
		//Realizamos la operación para restar un día a la fecha teniendo en cuenta que correrá más tarde
		Calendar calendarioActual = Calendar.getInstance();
		try
		{
			//Al objeto calendario le fijamos la fecha actual del sitema
			calendarioActual.setTime(datFechaActual);
			
		}catch(Exception e)
		{
			System.out.println(e.toString());
		}
		calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
		datFechaActual = calendarioActual.getTime();
		
		//Comenzamos a traer la fecha actual como un String
		fechaActual = dateFormat.format(datFechaActual);
		//El proceso correra  las 11:50 pm
		
		
		String respuesta = "";
		
		//Recuperaremos las tiendas y empezaremos a ir consultando una a una las tiendas para extraer la información
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		for(Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				respuesta = respuesta + "<table border='2'> <tr><td colspan ='5'>" + tien.getNombreTienda() + " CIERRE DE DATÁFONOS " + fechaActual  + "</td></tr>";
				respuesta = respuesta + "<tr>"
						+  "<td><strong>DATAFONO</strong></td>"
						+  "<td><strong>TERMINAL</strong></td>"
						+  "<td><strong>Valor Calculado</strong></td>"
						+  "<td><strong>Valor Ingresado</strong></td>"
						+  "<td><strong>Observación</strong></td>"
						+  "</tr>";
				//Recuperamos los evento de empleados para la semana en cuestión
				ArrayList<DatafonoCierre> datafonosCierre = DatafonoCierreDAO.consultarDatafonosCierre(fechaActual, tien.getHostBD(), false);
				//Comenzamos a recorrer para ir presetnando la información
				for(DatafonoCierre datTemp : datafonosCierre)
				{
					respuesta = respuesta + "<tr>"
							+  "<td>" + datTemp.getNombreDatafono() + "</td>"
							+  "<td>" + datTemp.getTerminal() + "</td>"
							+  "<td>" + datTemp.getValorCalculado() + "</td>"
							+  "<td>" + datTemp.getValorIngresado() + "</td>"
							+  "<td>" + datTemp.getObservacion() + "</td>";
					CapaDAOSer.DatafonoCierreDAO.insertarDatafonoCierre(datTemp, tien.getIdTienda(), fechaActual, false);
				}
				respuesta = respuesta + "</table> <br/>";
			}
		}
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("CIERREDATAFONO");
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("REPORTE DIARIO CIERRE DATAFONOS " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuación el resumen de cierres de datáfono diario para la fecha " + fechaActual +": \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		
	}
	
}

