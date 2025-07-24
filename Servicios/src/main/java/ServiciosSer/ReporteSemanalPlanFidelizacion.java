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

import CapaDAOSer.ClienteFidelizacionDAO;
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
import ModeloSer.FidelizacionRedencion;
import ModeloSer.Pedido;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import capaDAOPOS.DatafonoCierreDAO;
import capaDAOPOS.DatafonoDAO;
import capaModeloPOS.DatafonoCierre;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteSemanalPlanFidelizacion {
	
			
		
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
		Date datFechaAnterior;
		String fechaAnterior = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		//Comenzamos a traer la fecha actual como un String
		fechaActual = dateFormat.format(datFechaActual);
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
		//Llevamos a un string la fecha anterior para el c�lculo de la venta
		datFechaAnterior = calendarioActual.getTime();
		fechaAnterior = dateFormat.format(datFechaAnterior);
		
		
		String respuesta = "";
		
		//TOTAL DE PERSONAS EN EL PLAN DE FIDELIZACIÓN
		int totalPersonasPlan = ClienteFidelizacionDAO.obtenerTotalClienteFidelizacion();
		respuesta = respuesta + "<table border='2'> <tr><td width='300'>TOTAL CLIENTES EN PROGRAMA</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>" + totalPersonasPlan +"</strong></td></tr></table> <br/>";
		
		//TOTAL DE PERSONAS AFILIADAS EN EL PLAN DE FIDELIZACION LA ULTIMA SEMANA
		int totalPersonaSemana = ClienteFidelizacionDAO.obtenerTotalClienteAfiliadosTiempo(fechaAnterior, fechaActual);
		respuesta = respuesta + "<table border='2'> <tr><td width='300' >TOTAL CLIENTES VINCULADOS LA ÚLTIMA SEMANA</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>" + totalPersonaSemana +"</strong></td></tr></table> <br/>";
		
		//TOTAL DE PUNTOS ACUMULADOS LA ÚLTIMA SEMANA EN EL PLAN DE FIDELIZACION
		double totalPuntosAcumulados = ClienteFidelizacionDAO.obtenerTotalPuntosAcumuladosTiempo(fechaAnterior, fechaActual);
		respuesta = respuesta + "<table border='2'> <tr><td width='300'>TOTAL PUNTOS ACUMULADOS LA ÚLTIMA SEMANA</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>" + totalPuntosAcumulados +"</strong></td></tr></table> <br/>";
		
		//TOTAL PUNTOS REDIMIDOS LA ÚLTIMA SEMANA EN EL PLAN DE FIDELIZACION
		double totalPuntosRedimidos = ClienteFidelizacionDAO.obtenerTotalPuntosRedimidosTiempo(fechaAnterior, fechaActual);
		respuesta = respuesta + "<table border='2'> <tr><td width='300'>TOTAL PUNTOS REDIMIDOS LA ÚLTIMA SEMANA</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>" + totalPuntosRedimidos +"</strong></td></tr></table> <br/>";
		
		//TRANSACCIONES DE FIDELIZACION
		ArrayList<FidelizacionRedencion> redenciones = ClienteFidelizacionDAO.obtenerRedencionesFecha(fechaAnterior, fechaActual);
		respuesta = respuesta + "<table border='2'> <tr><td colspan='3'>REDENCIONES DE LA SEMANA</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<th width='170'><strong>CORREO</strong></td>"
				+  "<th width='60'><strong>FECHA</strong></td>"
				+  "<th width='70'><strong>PUNTOS REDIMIDOS</strong></td></tr>";
		for(FidelizacionRedencion redencionTemp : redenciones)
		{
			respuesta = respuesta + "<tr>"
					+  "<td><strong>" + redencionTemp.getCorreo() +"</strong></td><td>" + redencionTemp.getFechaRedencion() + "</td><td>" + redencionTemp.getPuntosRedimidos()  +"</td></tr>";
		}
		respuesta = respuesta + "</table> <br/>";
		//Recuperar la lista de distribuci�n para este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("FIDELIZACION");
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("REPORTE SEMANAL PLAN DE FIDELIZACION ENTRE " + fechaAnterior + " Y " + fechaActual);
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuacion el estado actual del plan de fidelizacion: \n " + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
		
	}
	
}

