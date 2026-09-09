package ServiciosSer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

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

import CapaDAOSer.ConsumoInventarioDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ItemInventarioDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.ConsumoInventario;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.Insumo;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioMonitoreoBaseDatos {

//Umbrales por defecto, por si el parametro no esta creado en la tabla. Van aqui y
//no quemados en el if: retornarValorNumericoLocal devuelve 0 cuando el parametro no
//existe, y un umbral en 0 haria que la alerta se disparara siempre.
private static final int PORCENTAJE_ALERTA_POR_DEFECTO = 60;
private static final int DORMIDAS_ALERTA_POR_DEFECTO = 10;
private static final int MINUTOS_DORMIDA_POR_DEFECTO = 60;

public static void main(String[] args)
{
	ServicioMonitoreoBaseDatos monitoreoRed = new ServicioMonitoreoBaseDatos();
	monitoreoRed.monitorearBaseDatos();
	
}

/**
 * Revisa el estado de las conexiones de la base de datos principal y avisa por
 * correo cuando hay saturacion o cuando hay conexiones abandonadas.
 *
 * Antes esto comparaba un COUNT(*) del PROCESSLIST completo contra un 20 quemado
 * en el codigo. Ese conteo incluia las conexiones dormidas y el demonio del motor,
 * y el 20 era el 13% de las 151 que aguanta el servidor: la alerta sonaba todos los
 * dias al mediodia sin que pasara nada, y una alerta que suena siempre no se lee.
 *
 * Ahora se miran dos cosas distintas:
 *   - Saturacion: que tanto del maximo de conexiones se esta usando, en porcentaje.
 *   - Conexiones abandonadas: cuantas llevan dormidas mas de X minutos. Esta es la
 *     que detecta que algun programa dejo de cerrar sus conexiones, que es lo que
 *     en realidad hacia subir el numero.
 */
public void monitorearBaseDatos()
{
	SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	String fechaTexto = formatoFecha.format(new Date());

	int porcentajeAlerta = ParametrosDAO.retornarValorNumericoLocal("MONITOREOBDPORCENTAJE");
	if (porcentajeAlerta <= 0)
	{
		porcentajeAlerta = PORCENTAJE_ALERTA_POR_DEFECTO;
		System.out.println("El parametro MONITOREOBDPORCENTAJE no esta definido, se usa " + porcentajeAlerta);
	}
	int dormidasAlerta = ParametrosDAO.retornarValorNumericoLocal("MONITOREOBDDORMIDAS");
	if (dormidasAlerta <= 0)
	{
		dormidasAlerta = DORMIDAS_ALERTA_POR_DEFECTO;
		System.out.println("El parametro MONITOREOBDDORMIDAS no esta definido, se usa " + dormidasAlerta);
	}
	int minutosDormida = ParametrosDAO.retornarValorNumericoLocal("MONITOREOBDMINUTOS");
	if (minutosDormida <= 0)
	{
		minutosDormida = MINUTOS_DORMIDA_POR_DEFECTO;
		System.out.println("El parametro MONITOREOBDMINUTOS no esta definido, se usa " + minutosDormida);
	}

	TiendaDAO.EstadoConexiones estado = TiendaDAO.obtenerEstadoConexionesBD(minutosDormida);

	//Si no se pudo consultar el estado no se manda nada: un correo con ceros seria
	//peor que ninguno, porque parece un servidor vacio y no una falla de consulta.
	if (!estado.consultaExitosa)
	{
		System.out.println("No se pudo leer el estado de las conexiones. No se envia alerta.");
		return;
	}

	boolean haySaturacion = (estado.porcentajeUso() >= porcentajeAlerta);
	boolean hayAbandonadas = (estado.dormidasViejas >= dormidasAlerta);

	System.out.println("Conexiones: " + estado.total + " de " + estado.maximo
			+ " (" + estado.porcentajeUso() + "%), trabajando " + estado.trabajando
			+ ", dormidas hace mas de " + minutosDormida + " min: " + estado.dormidasViejas);

	if (!haySaturacion && !hayAbandonadas)
	{
		System.out.println("Todo en rango. No se envia alerta.");
		return;
	}

	String asunto;
	if (haySaturacion)
	{
		asunto = "ALERTA BASE DE DATOS: conexiones al " + estado.porcentajeUso() + "% ("
				+ estado.total + " de " + estado.maximo + ")";
	}
	else
	{
		asunto = "AVISO BASE DE DATOS: " + estado.dormidasViejas
				+ " conexiones abandonadas hace mas de " + minutosDormida + " minutos";
	}

	Correo correo = new Correo();
	correo.setAsunto(asunto + " - " + fechaTexto);
	CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
	correo.setContrasena(infoCorreo.getClaveCorreo());
	correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
	ArrayList correos = GeneralDAO.obtenerCorreosParametro("ERRORINTERNET");
	correo.setMensaje(this.armarMensaje(estado, porcentajeAlerta, dormidasAlerta, fechaTexto, haySaturacion, hayAbandonadas));
	ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
	contro.enviarCorreoHTML();
}

/**
 * Arma el cuerpo del correo. Va con tablas y estilos en linea a proposito: Outlook
 * ignora buena parte del CSS moderno y un correo que se ve bien en el navegador
 * llega descuadrado a la bandeja de quien lo tiene que leer. Los atributos usan
 * comillas simples, que en HTML son validas, para no llenar el Java de escapes.
 */
private String armarMensaje(TiendaDAO.EstadoConexiones estado, int porcentajeAlerta,
		int dormidasAlerta, String fechaTexto, boolean haySaturacion, boolean hayAbandonadas)
{
	StringBuilder m = new StringBuilder();
	m.append("<div style='font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;'>");
	m.append("<table cellpadding='12' cellspacing='0' border='0' width='620' style='border-collapse:collapse;'>");
	m.append("<tr><td style='background-color:#102F6F;color:#FFFFFF;font-size:17px;font-weight:bold;'>");
	m.append("Estado de las conexiones de la base de datos</td></tr>");
	m.append("<tr><td style='font-size:12px;color:#666666;'>").append(fechaTexto).append("</td></tr>");
	m.append("</table>");

	m.append("<table cellpadding='8' cellspacing='0' border='0' width='620' style='border-collapse:collapse;border:1px solid #DDDDDD;'>");
	m.append(this.fila("Conexiones abiertas", String.valueOf(estado.total), false));
	m.append(this.fila("Maximo que aguanta el servidor", String.valueOf(estado.maximo), false));
	m.append(this.fila("Uso", estado.porcentajeUso() + "%   (umbral: " + porcentajeAlerta + "%)", haySaturacion));
	m.append(this.fila("Trabajando en este momento", String.valueOf(estado.trabajando), false));
	m.append(this.fila("Dormidas hace mas de " + estado.minutosDormida + " min",
			estado.dormidasViejas + "   (umbral: " + dormidasAlerta + ")", hayAbandonadas));
	m.append("</table>");

	m.append("<table cellpadding='12' cellspacing='0' border='0' width='620'>");
	if (haySaturacion)
	{
		m.append("<tr><td style='background-color:#FDF3F3;border-left:5px solid #E42528;'>");
		m.append("<b>Hay saturacion.</b> El uso de conexiones paso del umbral. Antes de subir ");
		m.append("el maximo del motor, revise que proceso esta abriendo tantas conexiones.");
		m.append("</td></tr>");
	}
	if (hayAbandonadas)
	{
		m.append("<tr><td style='background-color:#FFFBEA;border-left:5px solid #FDC806;'>");
		m.append("<b>Hay conexiones abandonadas.</b> ").append(estado.dormidasViejas);
		m.append(" conexiones llevan dormidas mas de ").append(estado.minutosDormida);
		m.append(" minutos, o sea que algun programa las abrio y no las cerro. Eso no es ");
		m.append("saturacion, es una fuga, y el numero va a seguir subiendo solo. Para ver de ");
		m.append("donde vienen:<br><br>");
		m.append("<span style='font-family:Courier New,monospace;font-size:12px;'>");
		m.append("SELECT id, host, db, command, time FROM information_schema.processlist ");
		m.append("WHERE command = 'Sleep' ORDER BY time DESC;");
		m.append("</span></td></tr>");
	}
	m.append("</table>");

	m.append("<p style='font-size:11px;color:#888888;'>Los umbrales se cambian en la tabla ");
	m.append("parametros del esquema general, en MONITOREOBDPORCENTAJE, MONITOREOBDDORMIDAS ");
	m.append("y MONITOREOBDMINUTOS. No hay que recompilar el proceso.</p>");
	m.append("</div>");
	return(m.toString());
}

private String fila(String etiqueta, String valor, boolean resaltar)
{
	String fondo = "#FFFFFF";
	String peso = "normal";
	if (resaltar)
	{
		fondo = "#FFFBEA";
		peso = "bold";
	}
	return("<tr style='background-color:" + fondo + ";'>"
			+ "<td style='border-bottom:1px solid #EEEEEE;'>" + etiqueta + "</td>"
			+ "<td style='border-bottom:1px solid #EEEEEE;text-align:right;font-weight:" + peso + ";'>"
			+ valor + "</td></tr>");
}

}




