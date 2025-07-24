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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import capaDAOCC.SolicitudPQRSDAO;
import capaDAOPOS.DatafonoCierreDAO;
import capaModeloCC.ComentarioPqrs;
import capaModeloCC.SolicitudPQRS;
import capaModeloPOS.DatafonoCierre;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteDiarioPQRS {
	
			
		
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

		String respuesta = "";
		
		ArrayList<SolicitudPQRS> consultaSolicitudes = SolicitudPQRSDAO.ConsultaSolicitudesReportePQRS(fechaActual);

		respuesta = respuesta + "<table border='2'> <tr><td colspan ='7'> REPORTE DIARIO PQRS " + fechaActual  + "</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<th width='30'><strong>NUMERO PQRS</strong></td>"
				+  "<th width='50'><strong>PRIORIDAD</strong></td>"
				+  "<th width='50'><strong>TIPO DE SOLICITUD</strong></td>"
				+  "<th width='50'><strong>FECHA</strong></td>"
				+  "<th width='60'><strong>TIENDA</strong></td>"
				+  "<th width='60'><strong>AREA RESPONSABLE</strong></td>"
				+  "<th width='300'><strong>OBSERVACION</strong></td></tr>";
		for(SolicitudPQRS solTemp : consultaSolicitudes)
		{
			String comentarios = SolicitudPQRSDAO.obtenerComentariosStrPqrs(solTemp.getIdsolicitud());
			respuesta = respuesta + "<tr>"
					+  "<td><strong>" +  solTemp.getIdsolicitud()+ "</strong></td>"
					+  "<td> " + solTemp.getPrioridad() +"</td>"
					+  "<td> " + solTemp.getTipoSolicitud() +"</td>"
					+  "<td> " + solTemp.getFechaSolicitud() +"</td>"
					+  "<td> " + solTemp.getTienda() +"</td>"
					+  "<td> " + solTemp.getAreaResponsable() +"</td>"
					+  "<td> " + comentarios +"</td>"
					+  "</tr>";	
		}
		respuesta = respuesta + "</table> <br/>";
		if(consultaSolicitudes.size() > 0)
		{
			//Recuperar la lista de distribuci�n para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REGISTROPQRS");
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("REPORTE PQRS DIARIAS " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuacion el resumen de venta de productos especiales tiendas " + fechaActual +": \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
	}
	
}

