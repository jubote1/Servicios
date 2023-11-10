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
 * Proceso que se encargar� diariamente de extraer la informaci�n de las ventas de promociones de una forma que tendr� que conectarse
 * a cada tienda y extraer la informaci�n
 * @author juanb
 *
 */
public class ReporteUsoAppLogisticaMensual {
	
	
	
/**
 * Este programa se encargar� de correr como un servicio todos los d�as a las 12:50 am, con el fin de revisar
 * si los sistemas se encuentran cerrados y enviar un mensaje al correo con la revisi�n.
 * @param args
 */
public static void main(String[] args)
{
	ReporteUsoAppLogisticaMensual reporteUsoApp = new ReporteUsoAppLogisticaMensual();
	reporteUsoApp.generarUsoApp();
	
}

public void generarUsoApp()
{
	//Definimos las variables
	Date datFechaAnterior;
	String fechaAnterior = "";
	Calendar calendarioActual = Calendar.getInstance();
	int mesActual = calendarioActual.get(Calendar.MONTH)+1;
	int anoActual = calendarioActual.get(Calendar.YEAR);
	int diaAct = calendarioActual.get(Calendar.DAY_OF_MONTH);
	int diaMaximoMesActual = Calendar.getInstance().getActualMaximum(calendarioActual.DAY_OF_MONTH);
	if(diaMaximoMesActual == diaAct)
	{
		int diaActual = 1;
		fechaAnterior = Integer.toString(anoActual) + "-" + Integer.toString(mesActual) + "-01";
		Date fechaActual = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		//Formateamos la fecha Actual para consulta
		String strFechaActual = dateFormat.format(fechaActual);
		//String strFechaActual = "2020-08-10";
		//Vamos a recuperar el d�a anterior que seg�n esto es el d�a real de trabajo
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		
		
		//Vamos a obtener los domiciliarios para con base en estos recorrer los d�as y saber la situaci�n
//		ArrayList<Usuario> domiciliarios = UsuarioDAO.obtenerTrabajoDomiciliariosFecha(fechaAnterior, strFechaActual);
//		int totPedDom = 0;
//		int totPedDomLog = 0;
//		String primeraFecha = "";
//		boolean bPrimeraFecha = false;
//		String ultimaFecha = "";
//		for(int i = 0; i < domiciliarios.size(); i++)
//		{
//			Usuario usuarioTemp = domiciliarios.get(i);
//			for(int j = 1; j <= 30; j++)
//			{
//				
//			}
//		}
		
		
		DecimalFormat formatea = new DecimalFormat("###,###");
		String respuesta = "";
		respuesta = respuesta + "<table border='2'> <tr> REPORTE DE USO APP LOG�STICA POR TIENDA DE " + fechaAnterior + " A " + strFechaActual + " </tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>PORCENTAJE DE USO</strong></td>"
				+  "</tr>";
		//Tendremos un indicador para saber si hubo venta de promoci�n de medianas
		double porcentajeUso = 0;
		PedidoCtrl pedCtrl = new PedidoCtrl(false);
		for(Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				porcentajeUso = pedCtrl.calcularUsoAppLogisticaFechas(fechaAnterior,strFechaActual, tien.getHostBD());
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
						+  "<td><strong>FECHA MINIMA</strong></td>"
						+  "<td><strong>FECHA MAXIMA</strong></td>"
						+  "</tr>";
				ArrayList resumenTotDomiciliario = pedCtrl.obtenerDomiciliarioUsoLogisticaFechas(fechaAnterior,strFechaActual,tien.getHostBD());
				for(int i = 0; i < resumenTotDomiciliario.size(); i++)
				{
					String[] filaTemp = (String[])  resumenTotDomiciliario.get(i);
					respuesta = respuesta + "<tr><td>" +  filaTemp[0] + "</td><td>" + filaTemp[1] + "</td><td>" + filaTemp[2] + "</td><td>" + filaTemp[3] + "</td><td>" + filaTemp[4] + "</td></tr>";
				}
				respuesta = respuesta + "</table> <br/>";
				
			}
		}
		
		if(respuesta.trim().equals(new String("")))
		{
			
		}else
		{
			//Realizamos el env�o del correo electr�nico con los archivos
			Correo correo = new Correo();
			correo.setAsunto("USO APP LOG�STICA MENSUAL DEL " + fechaAnterior + " AL " + strFechaActual);
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setContrasena(infoCorreo.getClaveCorreo());
			//Tendremos que definir los destinatarios de este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("APPLOGISTICA");
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			String mensaje = "A continuaci�n los indicadores para la fecha del uso de la APP de log�stica Pizza Americana  " + respuesta ;
			correo.setMensaje(mensaje);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
	}
	
}


}




