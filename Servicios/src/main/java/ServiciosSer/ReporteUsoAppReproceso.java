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
import CapaDAOSer.OfertaClienteDAO;
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
import capaControladorPOS.PedidoCtrl;
import utilidadesSer.ControladorEnvioCorreo;

/**
 * Proceso que se encargará diariamente de extraer la información de las ventas de promociones de una forma que tendrá que conectarse
 * a cada tienda y extraer la información
 * @author juanb
 *
 */
public class ReporteUsoAppReproceso {
	
	
	
/**
 * Este programa se encargará de correr como un servicio todos los días a las 12:50 am, con el fin de revisar
 * si los sistemas se encuentran cerrados y enviar un mensaje al correo con la revisión.
 * @param args
 */
public static void main(String[] args)
{
	ReporteUsoAppReproceso reporteUsoApp = new ReporteUsoAppReproceso();
	reporteUsoApp.generarUsoApp();
	
}

public void generarUsoApp()
{
	String strFechaActual = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
	//String strFechaActual = "2020-08-10";
	//Vamos a recuperar el día anterior que según esto es el día real de trabajo
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	DecimalFormat formatea = new DecimalFormat("###,###");
	String respuesta = "";
	respuesta = respuesta + "<table border='2'> <tr> REPORTE DE USO APP LOGÍSTICA  " + strFechaActual + " </tr>";
	respuesta = respuesta + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>PORCENTAJE DE USO</strong></td>"
			+  "</tr>";
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	double porcentajeUso = 0;
	PedidoCtrl pedCtrl = new PedidoCtrl(false);
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			
			porcentajeUso = pedCtrl.calcularUsoAppLogistica(strFechaActual, tien.getHostBD());
			respuesta = respuesta + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(porcentajeUso) + "</td></tr>";
		}
	}
	respuesta = respuesta + "</table> <br/>";
	
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			respuesta = respuesta + "<table border='2'> <tr> REPORTE DE USO APP POR DOMICILIARO TIENDA  " + tien.getNombreTienda() + "-" + strFechaActual + " </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Nombre Domiciliario</strong></td>"
					+  "<td><strong>PEDIDOS LLEVADOS</strong></td>"
					+  "<td><strong>PEDIDOS CON LOGISTICA</strong></td>"
					+  "</tr>";
			ArrayList resumenTotDomiciliario = pedCtrl.obtenerDomiciliarioUsoLogistica(strFechaActual,tien.getHostBD());
			for(int i = 0; i < resumenTotDomiciliario.size(); i++)
			{
				String[] filaTemp = (String[])  resumenTotDomiciliario.get(i);
				respuesta = respuesta + "<tr><td>" +  filaTemp[0] + "</td><td>" + filaTemp[1] + "</td><td>" + filaTemp[2] + "</td></tr>";
			}
			respuesta = respuesta + "</table> <br/>";
			
		}
	}
	
	
	if(respuesta.trim().equals(new String("")))
	{
		
	}else
	{
		//Realizamos el envío del correo electrónico con los archivos
		Correo correo = new Correo();
		correo.setAsunto("USO APP LOGÍSTICA " + strFechaActual);
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		//Tendremos que definir los destinatarios de este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("APPLOGISTICA");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		String mensaje = "A continuación los indicadores para la fecha del uso de la APP de logística Pizza Americana  " + respuesta ;
		correo.setMensaje(mensaje);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}
	
}


}




