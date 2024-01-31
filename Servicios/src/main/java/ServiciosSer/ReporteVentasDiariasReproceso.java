package ServiciosSer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFRegionUtil;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ItemInventarioDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.Insumo;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteVentasDiariasReproceso {
	
	
	
/**
 * Este programa se encargará de correr como un servicio todos los días a las 12:50 am, con el fin de revisar
 * si los sistemas se encuentran cerrados y enviar un mensaje al correo con la revisión.
 * @param args
 */
public static void main(String[] args)
{
	ReporteVentasDiariasReproceso reporteRevisionCierres = new ReporteVentasDiariasReproceso();
	reporteRevisionCierres.generarRevisionVentas();
	
}

public void generarRevisionVentas()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCIÓN");
	String strFechaActual = "";
	Date fechaActual;
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	try
	{
		//OJO
		strFechaActual = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
	}catch(Exception exc)
	{
		System.out.println(exc.toString());
	}
	
	//Formateamos la fecha Actual para consulta
	
	//Vamos a recuperar el día anterior que según esto es el día real de trabajo
	Calendar calendarioActual = Calendar.getInstance();
	//Fijamos el calendario actual con la fecha de reproceso
	try
	{
		//Al objeto calendario le fijamos la fecha actual del sitema
		calendarioActual.setTime(dateFormat.parse(strFechaActual));
		
	}catch(Exception e)
	{
		System.out.println(e.toString());
	}
	Date fechaAnterior = new Date();
	String strFechaAnterior = "";
	//Con lo anterior ya tenemos las variables para el proceso
	int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
	DecimalFormat formatea = new DecimalFormat("###,###");
	//Domingo
	if(diaActual == 1)
	{
		calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
	}
	else if(diaActual == 2)
	{	//Si es lunes, no hacemos resta
		//calendarioActual.add(Calendar.DAY_OF_YEAR, -7);
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
	fechaAnterior = calendarioActual.getTime();
	strFechaAnterior = dateFormat.format(fechaAnterior);
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	//Retornamos los objetos de empleados y la biometria, primero debemos retornar
	String respuesta = "";
	respuesta = respuesta + "<table border='2'> <tr> RESUMEN DE VENTAS DEL DÍA  " + strFechaActual + " </tr>";
	respuesta = respuesta + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Venta del Día</strong></td>"
			+  "<td><strong>Acumulado Semana</strong></td>"
			+  "</tr>";
	double ventaDelDia = 0;
	double ventaSemana = 0;
	for(Tienda tien : tiendas)
	{
		ventaDelDia = 0;
		ventaSemana = 0;
		if(!tien.getHostBD().equals(new String("")))
		{
			ventaDelDia = PedidoDAO.obtenerTotalesPedidosSemana(strFechaActual , strFechaActual, tien.getHostBD());
			ventaSemana = PedidoDAO.obtenerTotalesPedidosSemana(strFechaAnterior , strFechaActual, tien.getHostBD());
			respuesta = respuesta + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(ventaDelDia) + "</td><td>" + formatea.format(ventaSemana) + "</td></tr>";
		}
	}
	
	respuesta = respuesta + "</table> <br/>";
	
	//Realizamos el envío del correo electrónico con los archivos
	Correo correo = new Correo();
	CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
	correo.setAsunto("REPORTE VENTAS TIENDAS " + strFechaActual);
	correo.setContrasena(infoCorreo.getClaveCorreo());
	//Tendremos que definir los destinatarios de este correo
	ArrayList correos = GeneralDAO.obtenerCorreosParametro("VENTADIARIA");
	correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
	String mensaje = "A continuación informamos las ventas totatales de las tiendas y sus acumulados  " + respuesta ;
	correo.setMensaje(mensaje);
	ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
	contro.enviarCorreoHTML();
}


}




