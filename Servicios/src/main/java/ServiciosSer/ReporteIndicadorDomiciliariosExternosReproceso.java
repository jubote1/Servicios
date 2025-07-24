package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.GeneralDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import capaDAOCC.ParametrosDAO;
import capaDAOPOS.PedidoDAO;
import capaModeloCC.Parametro;
import capaModeloCC.Tienda;
import capaModeloPOS.ClienteEnvioEncuesta;
import capaModeloPOS.DesempenoDomiciliario;
import capaModeloPOS.DesempenoResumenDomExt;
import capaModeloPOS.Usuario;
import utilidadesSer.ControladorEnvioCorreo;


public class ReporteIndicadorDomiciliariosExternosReproceso {
	
	public static void main( String[] args )
	{

		// 1. Recuperamos la fecha actual del sistema con la fecha apertura
		String fechaActual = "";
		String fechaAnterior = "";
		Date datFechaAnterior;
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Parametro parametroPedidos = ParametrosDAO.obtenerParametro("CANTIDADPEDIDOSHORA");
		int cantidadPedidosHora = parametroPedidos.getValorNumerico();
		try
		{
			fechaActual = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
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
		//Llevamos a un string la fecha anterior para el c�lculo de la venta
		datFechaAnterior = calendarioActual.getTime();
		fechaAnterior = dateFormat.format(datFechaAnterior);
		
		
		ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendas();
		//Variables para la labor de cada tienda
		Tienda tiendaTemp;
		ClienteEnvioEncuesta clienteTemp;
		String respuesta = "<table WIDTH='500' border='2'> <tr><TH COLSPAN='5'> INDICADOR DE EFICIENCIA DOMICILIARIOS EXTERNOS </TH> </tr>";
		respuesta = respuesta + "<tr><TH> TIENDA </TH><TH> HORAS </TH> <TH> PEDIDOS LLEVADOS </TH> <TH> PEDIDOS ESPERADOS </TH> <TH> PORCENTAJE CUMPLIMIENTO </TH> </tr>";
		double pedidosEsperados = 0;
		double porcentajeIndicador = 0;
		for(int j = 0; j < tiendas.size(); j++)
		{
			tiendaTemp = tiendas.get(j);
			respuesta = respuesta + "<tr><td colspan='5'>" + tiendaTemp.getNombreTienda().toUpperCase() + "</td></tr>";
			//Obtenemos los domiciliarios que laboraron en dicha tienda dicha fecha
			DesempenoResumenDomExt desempeno = PedidoDAO.obtenerDesempenoDomiciliarioExternoFechas(fechaAnterior, fechaActual, tiendaTemp.getHosbd(), false);
			pedidosEsperados = desempeno.getHoras() * cantidadPedidosHora;
			porcentajeIndicador = (desempeno.getPedidos()/pedidosEsperados)*100;
			respuesta = respuesta +  "<tr><td ><strong>"+ tiendaTemp.getNombreTienda() +"</strong></td>" + "<td >"+ desempeno.getHoras() + "</td>" + "<td>"+ desempeno.getPedidos() + "</td>" + "<td>"+ pedidosEsperados + "</td>" + "<td>"+ porcentajeIndicador + "</td>"
					+  "</tr>";
			
		}	
		respuesta = respuesta + "</table> <br/>";
		//Al final enviamos correo con el resultado del proceso
		//Recuperar la lista de distribuci�n para este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEOPERACIONDOMICILIARIOS");
		Date fecha = new Date();
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto(fechaAnterior +"-" + fechaActual +"-" + " INDICADOR SEMANAL EFICIENCIA DOMICILIARIOS EXTERNOS ");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("REPORTE semanal eficiencia domiciliarios externos : \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}

}
