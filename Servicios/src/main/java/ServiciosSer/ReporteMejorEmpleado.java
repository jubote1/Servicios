package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import capaModeloCC.Tienda;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteMejorEmpleado {
	
	public static void main( String[] args )
	{
		//TRABAJO CON LAS FECHAS///////
		//Recuperamos la fecha actual del sistema con la fecha apertura
		String fechaActual = "";
		String asuntoCorreo = "";
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
		//int diaActual = 1;
		int diaActual = calendarioActual.get(Calendar.DAY_OF_MONTH);
		//Recuperamos todos los valores parametrizables para el proceso
		int diaComiteConv = ParametrosDAO.retornarValorNumericoLocal("DIACOMITECONVIVENCIA");
		if(diaActual <= diaComiteConv)
		{
			if(diaActual < diaComiteConv)
			{
				asuntoCorreo = "RESULTADOS PARCIAL ENCUESTA MEJOR EMPLEADO - COMITE CONVIVENCIA " + fechaActual;
			}else if(diaActual == diaComiteConv)
			{
				asuntoCorreo = "RESULTADOS FINALES ENCUESTA MEJOR EMPLEADO - COMITE CONVIVENCIA " + fechaActual;
			}
			//Sacamos información resumida de total de pedidos en general y por tienda en la semana y por tienda y por día en la semana
			String respuesta = "";		
			//Vamos a sacar ahora el reporte por cada tienda con el detalle de los pagos
			//Comenzamos por obtener todas las tiendas y realizar un recorrido de cada una
			ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendas();
			//Variables para la labor de cada tienda
			Tienda tiendaTemp;
			String[] resultadoTemp;
			
			capaControladorCC.EmpleadoCtrl empCtrl = new capaControladorCC.EmpleadoCtrl();
			ArrayList<String[]> resultadosEncuesta;
			//Tendremos que separar las encuestas que son por tiendas la UNICA ES EMPLEADOS PV
			for(int j = 0; j < tiendas.size(); j++)
			{
				tiendaTemp = tiendas.get(j);
				if(!tiendaTemp.getHosbd().equals(new String("")))
				{
					respuesta = respuesta + "<table border='2'> <tr> <td colspan='7'> RESULTADOS ENCUESTA PARA " + tiendaTemp.getNombreTienda()+ "</td></tr>";
					respuesta = respuesta + "<tr>"
							+  "<td><strong>Nombre Empleado</strong></td>"
							+  "<td><strong>Promedio</strong></td>"
							+  "<td><strong>Cantidad Encuesta</strong></td>"
							+  "</tr>";
					resultadosEncuesta = empCtrl.obtenerResultadoEncuestaArreglo(tiendaTemp.getIdTienda(),4);
					for(int i = 0; i < resultadosEncuesta.size(); i++)
					{
						resultadoTemp = (String[]) resultadosEncuesta.get(i);
						respuesta = respuesta + "<tr><td>" + resultadoTemp[0] + "</td><td>" + resultadoTemp[1] + "</td><td>" + resultadoTemp[2]+ "</td><tr>";
					}
					respuesta = respuesta + "</table> <br/>";
				}	
			}
			
			//Despues de esto totalizaremos las encuestas QUE NO SON POR TIENDA.
			//SEDE ADMINISTRATIVA
			respuesta = respuesta + "<table border='2'> <tr> <td colspan='7'> RESULTADOS ENCUESTA PARA CENTRO DE PRODUCCIÓN</td></tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Nombre Empleado</strong></td>"
					+  "<td><strong>Promedio</strong></td>"
					+  "<td><strong>Cantidad Encuesta</strong></td>"
					+  "</tr>";
			resultadosEncuesta = empCtrl.obtenerResultadoEncuestaArreglo(7);
			for(int i = 0; i < resultadosEncuesta.size(); i++)
			{
				resultadoTemp = (String[]) resultadosEncuesta.get(i);
				respuesta = respuesta + "<tr><td>" + resultadoTemp[0] + "</td><td>" + resultadoTemp[1] + "</td><td>" + resultadoTemp[2]+ "</td><tr>";
			}
			respuesta = respuesta + "</table> <br/>";
			
			//CONTACT
			respuesta = respuesta + "<table border='2'> <tr> <td colspan='7'> RESULTADOS ENCUESTA PARA CONTACT</td></tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Nombre Empleado</strong></td>"
					+  "<td><strong>Promedio</strong></td>"
					+  "<td><strong>Cantidad Encuesta</strong></td>"
					+  "</tr>";
			resultadosEncuesta = empCtrl.obtenerResultadoEncuestaArreglo(8);
			for(int i = 0; i < resultadosEncuesta.size(); i++)
			{
				resultadoTemp = (String[]) resultadosEncuesta.get(i);
				respuesta = respuesta + "<tr><td>" + resultadoTemp[0] + "</td><td>" + resultadoTemp[1] + "</td><td>" + resultadoTemp[2]+ "</td><tr>";
			}
			respuesta = respuesta + "</table> <br/>";
			
			//ADMINISTRADORES
			respuesta = respuesta + "<table border='2'> <tr> <td colspan='7'> RESULTADOS ENCUESTA PARA ADMINISTRADORES</td></tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Nombre Empleado</strong></td>"
					+  "<td><strong>Promedio</strong></td>"
					+  "<td><strong>Cantidad Encuesta</strong></td>"
					+  "</tr>";
			resultadosEncuesta = empCtrl.obtenerResultadoEncuestaArreglo(9);
			for(int i = 0; i < resultadosEncuesta.size(); i++)
			{
				resultadoTemp = (String[]) resultadosEncuesta.get(i);
				respuesta = respuesta + "<tr><td>" + resultadoTemp[0] + "</td><td>" + resultadoTemp[1] + "</td><td>" + resultadoTemp[2]+ "</td><tr>";
			}
			respuesta = respuesta + "</table> <br/>";
			
			//ADMINISTRATIVOS
			respuesta = respuesta + "<table border='2'> <tr> <td colspan='7'> RESULTADOS ENCUESTA PARA ADMINISTRATIVOS</td></tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Nombre Empleado</strong></td>"
					+  "<td><strong>Promedio</strong></td>"
					+  "<td><strong>Cantidad Encuesta</strong></td>"
					+  "</tr>";
			resultadosEncuesta = empCtrl.obtenerResultadoEncuestaArreglo(10);
			for(int i = 0; i < resultadosEncuesta.size(); i++)
			{
				resultadoTemp = (String[]) resultadosEncuesta.get(i);
				respuesta = respuesta + "<tr><td>" + resultadoTemp[0] + "</td><td>" + resultadoTemp[1] + "</td><td>" + resultadoTemp[2]+ "</td><tr>";
			}
			respuesta = respuesta + "</table> <br/>";
			
			
			
			//Al final el envío del correo
			//Procedemos al envío del correo
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto(asuntoCorreo);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("COMITECONVIVENCIA");
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuación el detalle de las encuestas de mejor empleado " + " - " + fechaActual +  ": \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
	}

}
