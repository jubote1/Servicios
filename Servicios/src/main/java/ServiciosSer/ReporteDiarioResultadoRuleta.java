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
import CapaDAOSer.ResultadoRuletaDAO;
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
import ModeloSer.ResultadoRuleta;
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

public class ReporteDiarioResultadoRuleta {
	
			
		
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
		
		ArrayList<ResultadoRuleta> resultadoNoGanador = ResultadoRuletaDAO.obtenerResultadoDiarioRuletaNoGanadores(fechaActual, false);
		ArrayList<ResultadoRuleta> resultadoGanador = ResultadoRuletaDAO.obtenerResultadoDiarioRuletaGanadores(fechaActual, false);
		int cantidadEncuestasEnviadas = ResultadoRuletaDAO.cantidadEncuestasServicioEnviadas(fechaActual, false);
		//Contamos los premios que siguen sin entregar, para que el resumen lo diga y
		//nadie los deje olvidados esperando que alguien entre a la pantalla.
		int pendientesPorDispersar = 0;
		for(ResultadoRuleta resTemp : resultadoGanador)
		{
			if("PENDIENTE".equals(resTemp.getEstadoDispersion()))
			{
				pendientesPorDispersar++;
			}
		}

		//Recopilamos la información resumen
		respuesta = respuesta + "<table border='2'> <tr><td colspan ='4'> RESUMEN-" + fechaActual  + "</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<th width='30'><strong>CANTIDAD ENCUESTAS ENVIADAS</strong></th>"
				+  "<th width='50'><strong>ENCUESTAS CONTESTADAS</strong></th>"
				+  "<th width='50'><strong>GANADORES</strong></th>"
				+  "<th width='50'><strong>PENDIENTES POR DISPERSAR</strong></th>"
				+  "</tr>";
		respuesta = respuesta + "<tr>"
				+  "<td>" + cantidadEncuestasEnviadas + "</td>"
				+  "<td>" + (resultadoGanador.size() +resultadoNoGanador.size()) + "</td>"
				+  "<td>" + (resultadoGanador.size()) + "</td>"
				//Se resalta en rojo cuando hay pendientes: es lo unico del correo que
				//pide una accion, y en un correo diario que casi siempre se ve igual
				//lo que no resalta no se lee.
				+  "<td>" + (pendientesPorDispersar > 0
						? "<strong style='color:#c0392b;'>" + pendientesPorDispersar + "</strong>"
						: "0") + "</td>"
				+  "</tr>";
		respuesta = respuesta + "</table> <br/>";

		respuesta = respuesta + "<table border='2'> <tr><td colspan ='6'> REPORTE DIARIO NO GANADORES " + fechaActual  + "</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<th width='30'><strong>ID PEDIDO</strong></td>"
				+  "<th width='50'><strong>TIENDA</strong></td>"
				+  "<th width='50'><strong>PREMIO</strong></td>"
				+  "<th width='50'><strong>TELEFONO</strong></td>"
				+  "<th width='60'><strong>CORREO</strong></td>"
				+  "<th width='60'><strong>NOMBRE CLIENTE</strong></td>"
				+  "</tr>";
		for(ResultadoRuleta resTemp : resultadoNoGanador)
		{
			
			respuesta = respuesta + "<tr>"
					+  "<td><strong>" +  resTemp.getIdPedido() + "</strong></td>"
					+  "<td> " + resTemp.getTienda() +"</td>"
					+  "<td> " + resTemp.getPremio() +"</td>"
					+  "<td> " + resTemp.getTelefono() +"</td>"
					+  "<td> " + resTemp.getCorreo() +"</td>"
					+  "<td> " + resTemp.getNombreCliente() +"</td>"
					+  "</tr>";	
		}
		respuesta = respuesta + "</table> <br/>";
		
		//Agregamos los ganadores
		respuesta = respuesta + "<table border='2'> <tr><td colspan ='7'> REPORTE DIARIO GANADORES " + fechaActual  + "</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<th width='30'><strong>ID PEDIDO</strong></td>"
				+  "<th width='50'><strong>TIENDA</strong></td>"
				+  "<th width='50'><strong>PREMIO</strong></td>"
				+  "<th width='50'><strong>TELEFONO</strong></td>"
				+  "<th width='60'><strong>CORREO</strong></td>"
				+  "<th width='60'><strong>NOMBRE CLIENTE</strong></td>"
				+  "<th width='50'><strong>ENTREGA</strong></td>"
				//Faltaba el cierre de fila: sin el, algunos clientes de correo pegan la
				//primera fila de datos contra los titulos.
				+  "</tr>";
		for(ResultadoRuleta resTemp : resultadoGanador)
		{

			respuesta = respuesta + "<tr>"
					+  "<td><strong>" +  resTemp.getIdPedido() + "</strong></td>"
					+  "<td> " + resTemp.getTienda() +"</td>"
					+  "<td> " + resTemp.getPremio() +"</td>"
					+  "<td> " + resTemp.getTelefono() +"</td>"
					+  "<td> " + resTemp.getCorreo() +"</td>"
					+  "<td> " + resTemp.getNombreCliente() +"</td>"
					+  "<td> " + ("PENDIENTE".equals(resTemp.getEstadoDispersion())
							? "<strong style='color:#c0392b;'>PENDIENTE</strong>"
							: resTemp.getEstadoDispersion()) + "</td>"
					+  "</tr>";
		}
		respuesta = respuesta + "</table> <br/>";
		
		if(resultadoNoGanador.size() > 0 || resultadoGanador.size() > 0)
		{
			//Recuperar la lista de distribuci�n para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("RESULTADORULETA");
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("REPORTE DIARIO RESULTADOS RULETA " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuacion el resumen de los resultados diarios de la ruleta de premios " + fechaActual +": \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
	}
	
}

