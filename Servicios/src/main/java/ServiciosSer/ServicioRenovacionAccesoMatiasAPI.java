package ServiciosSer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
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
import capaControladorCC.PedidoCtrl;
import capaDAOCC.IntegracionCRMDAO;
import capaModeloCC.IntegracionCRM;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioRenovacionAccesoMatiasAPI {
	
	
	
/**
 * Este programa se encargar� de correr como un servicio todos los d�as a las 12:50 am, con el fin de revisar
 * si los sistemas se encuentran cerrados y enviar un mensaje al correo con la revisi�n.
 * @param args
 * @throws IOException 
 */
public static void main(String[] args) throws IOException
{
	PedidoCtrl pedCtrl = new PedidoCtrl();
	pedCtrl.actualizarAccesoMatiasAPI("MATIAS");
	//Requerimos actualizar variable en todos los puntos de venta con el valor del token
	IntegracionCRM intMatias = IntegracionCRMDAO.obtenerInformacionIntegracion("MATIAS");
	//Controlaremos si el token de acceso fue vacío
	if(intMatias.getAccessToken().equals(new String("")))
	{
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPESTADISTICASSEMANAL");
		Date fecha = new Date();
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("ERROR GRAVE ACTUALIZACIÓN TOKEN FACTURA ELECTRONICA " + fecha.toString());
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("Error grave, la actualización del TOKEN de factura electrónica dió un valor vacío, por favor revisar urgentemente.");
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
		return;
	}
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	String respuesta = "";
	Date fechaActual = new Date();
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			boolean actualiza = ParametrosDAO.EditarParametroTiendaRemota(tien.getHostBD(),"MATIASAPITOKEN", intMatias.getAccessToken());
			if(actualiza)
			{
				respuesta = respuesta + " " + tien.getNombreTienda() + " EXITOSO <BR>";
			}else 
			{
				respuesta = respuesta + " " + tien.getNombreTienda() + " ERROR <BR>";
			}
				
		}
	}
	//Realizamos envío de correo indicando el resultado del proceso
	//Recuperar la lista de distribuci�n para este correo
	ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPESTADISTICASSEMANAL");
	Date fecha = new Date();
	Correo correo = new Correo();
	CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
	correo.setAsunto("INFORME ACTUALIZACIÓN TOKEN FACTURA ELECTRONICA " + fechaActual);
	correo.setContrasena(infoCorreo.getClaveCorreo());
	correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
	correo.setMensaje("La informacion de actualizacion de token de Facturacion electronica \n" + respuesta);
	ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
	contro.enviarCorreoHTML();
}

}




