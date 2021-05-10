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
import CapaDAOSer.ReporteContactCenterDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.Insumo;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteDesempenoContactCenter {
	
	
	
/**
 * Este programa se encargará de correr como un servicio todos los días a las 12:50 am, con el fin de revisar
 * si los sistemas se encuentran cerrados y enviar un mensaje al correo con la revisión.
 * @param args
 */
public static void main(String[] args)
{
	ReporteDesempenoContactCenter reporteRevisionCierres = new ReporteDesempenoContactCenter();
	reporteRevisionCierres.generarRevisionCierres();
	
}

public void generarRevisionCierres()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCIÓN");
	//Generamos la fecha en la que corre el proceso
	Date fechaActual = new Date();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//Formateamos la fecha Actual para consulta
	String strFechaActual = dateFormat.format(fechaActual);
	//Vamos a recuperar el día anterior que según esto es el día real de trabajo
	Calendar calendarioActual = Calendar.getInstance();
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
	//Con lo anterior ya tenemos las variables para el proceso
	String respuesta = "";
	
	//Cantidad de pedidos tomados en el día
	int cantidadPedidos = ReporteContactCenterDAO.obtenerCantidadPedidos(strFechaActual, strFechaActual);
	int cantidadPedidosVirtual = ReporteContactCenterDAO.obtenerPedidosVirtualTotalDia(strFechaActual);
	int cantidadPedidosVirtualNueva = ReporteContactCenterDAO.obtenerPedidosVirtualNuevaTotalDia(strFechaActual);
	double promedioTiendaVirtual = ReporteContactCenterDAO.obtenerPromedioVirtualTotalDia(strFechaActual);

	respuesta = respuesta + "<table WIDTH='250' border='2'> <TH COLSPAN='2'> CANTIDAD PEDIDOS TOMADOS EN CONTACT " + strFechaActual + "</TH> </tr>";
	respuesta = respuesta + "<tr>"
			+  "<td width='250' nowrap><strong>CANTIDAD</strong></td>"
			+  "</tr>";
	respuesta = respuesta + "<tr><td width='250' nowrap>" + cantidadPedidos + "</td></tr>";
	respuesta = respuesta + "</table> <br/>";
	
	//Tienda virtual
	respuesta = respuesta + "<table WIDTH='250' border='2'> <TH COLSPAN='2'> CANTIDAD PEDIDOS TIENDA VIRTUAL " + strFechaActual + "</TH> </tr>";
	respuesta = respuesta + "<tr>"
			+  "<td width='250' nowrap><strong>CANTIDAD</strong></td>"
			+  "</tr>";
	respuesta = respuesta + "<tr><td width='250' nowrap>" + cantidadPedidosVirtual + "</td></tr>";
	respuesta = respuesta + "<tr>"
			+  "<td width='250' nowrap><strong>PROMEDIO ATENCIÓN EN MINUTOS</strong></td>"
			+  "</tr>";
	respuesta = respuesta + "<tr><td width='250' nowrap>" + formatea.format(promedioTiendaVirtual) + "</td></tr>";
	respuesta = respuesta + "</table> <br/>";
	
	//Tienda virtual Nueva
	respuesta = respuesta + "<table WIDTH='250' border='2'> <TH COLSPAN='2'> CANTIDAD PEDIDOS TIENDA VIRTUAL NUEVA " + strFechaActual + "</TH> </tr>";
	respuesta = respuesta + "<tr>"
			+  "<td width='250' nowrap><strong>CANTIDAD</strong></td>"
			+  "</tr>";
	respuesta = respuesta + "<tr><td width='250' nowrap>" + cantidadPedidosVirtualNueva + "</td></tr>";
	respuesta = respuesta + "</table> <br/>";
	
	//Cantidad de pedidos tomamos en lo que va de la semana
	ArrayList cantPedPersona = ReporteContactCenterDAO.obtenerPedidosUsuario(strFechaAnterior, strFechaActual);
	respuesta = respuesta + "<table WIDTH='250' border='2'> <TH COLSPAN='2'> PEDIDOS TOMADOS POR PERSONA EN LO QUE VA DE LA SEMANA "  + "</TH> </tr>";
	respuesta = respuesta + "<tr>"
			+  "<td width='190' nowrap><strong>NOMBRE PERSONA</strong></td>"
			+  "<td width='60' nowrap><strong>CANTIDAD</strong></td>"
			+  "<td width='60' nowrap><strong>TELETRABAJADOR</strong></td>"
			+  "</tr>";
	String[] fila;
	for(int y = 0; y < cantPedPersona.size();y++)
	{
		fila = (String[]) cantPedPersona.get(y);
		respuesta = respuesta + "<tr><td width='190' nowrap>" + fila[0] + "</td><td width='60' nowrap> " + fila[1] + "</td><td width='20' nowrap> " + fila[2]  +"</td></tr>";
	}
	respuesta = respuesta + "</table> <br/>";
	
	//Cantidad de pedidos tomamos en el día
		cantPedPersona = ReporteContactCenterDAO.obtenerPedidosUsuario(strFechaActual, strFechaActual);
		respuesta = respuesta + "<table WIDTH='250' border='2'> <TH COLSPAN='2'> PEDIDOS TOMADOS HOY "  + "</TH> </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td width='190' nowrap><strong>NOMBRE PERSONA</strong></td>"
				+  "<td width='60' nowrap><strong>CANTIDAD</strong></td>"
				+  "<td width='60' nowrap><strong>TELETRABAJADOR</strong></td>"
				+  "</tr>";
		for(int y = 0; y < cantPedPersona.size();y++)
		{
			fila = (String[]) cantPedPersona.get(y);
			respuesta = respuesta + "<tr><td width='190' nowrap>" + fila[0] + "</td><td width='60' nowrap> " + fila[1] + "</td><td width='20' nowrap> " + fila[2]  +"</td></tr>";
		}
		respuesta = respuesta + "</table> <br/>";

	
	//Realizamos el envío del correo electrónico con los archivos
	Correo correo = new Correo();
	CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
	correo.setAsunto("REPORTE DESEMPEÑO CONTACT CENTER " + fechaActual.toString());
	correo.setContrasena(infoCorreo.getClaveCorreo());
	//Tendremos que definir los destinatarios de este correo
	ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEDESEMPENOCONTACT");
	correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
	String mensaje = "A continuación informamos el estado de los cierres de las tiendas  " + respuesta ;
	correo.setMensaje(mensaje);
	ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
	contro.enviarCorreoHTML();
}


}




