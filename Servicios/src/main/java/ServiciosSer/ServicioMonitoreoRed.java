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

public class ServicioMonitoreoRed {
	
	
	
	
public static void main(String[] args)
{
	ServicioMonitoreoRed monitoreoRed = new ServicioMonitoreoRed();
	monitoreoRed.monitorearRed();
	
}

public void monitorearRed()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCIÓN");
	//Generamos la fecha en la que corre el proceso
	Date fechaActual = new Date();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//Formateamos la fecha Actual para consulta
	String strFechaActual = dateFormat.format(fechaActual);
	//Generamos String de tiendas exitosas y tiendas no exitosas para mandar correo.
	String respuesta = "";
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	//Comenzamos a recorrer una a una las tiendas
	ArrayList<ConsumoInventario> consumosInventario = new ArrayList();
	for(Tienda tien : tiendas)
	{
		
		if(!tien.getHostBD().equals(new String("")))
		{
			//Debemos de realizarle un tratamiento a la IP
			String dirIp = tien.getHostBD();
			StringTokenizer tokenDirIp = new StringTokenizer(dirIp, ".");
			dirIp= tokenDirIp.nextToken()+"."+tokenDirIp.nextToken()+"."+tokenDirIp.nextToken()+"."+"254";
			boolean resultado =  isReachableByPing(dirIp);
			if(!resultado)
			{
				respuesta = respuesta + " <p>" + tien.getNombreTienda() + " ERROR DE CONECTIVIDAD " +  " </p>";
			}
			
		}
	}
	
	//Realizamos el envío del correo electrónico con los archivos
	if(respuesta.length() > 0)
	{
		Correo correo = new Correo();
		correo.setAsunto("INTERNET FALLANDO " + fechaActual.toString());
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		//Tendremos que definir los destinatarios de este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("ERRORINTERNET");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		String mensaje = "Se presentan inconvenientes con el internet en  " + respuesta;
		correo.setMensaje(mensaje);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}
	
}


public static boolean isReachableByPing(String Host) {
    try{
               String cmd = "";
               if(System.getProperty("os.name").startsWith("Windows")) {   
                       // For Windows
                       cmd = "ping -n 1 " + Host;
               } else {
                       // For Linux and OSX
                       cmd = "ping -c 1 " + Host;
               }

               Process myProcess = Runtime.getRuntime().exec(cmd);
               myProcess.waitFor();

               if(myProcess.exitValue() == 0) {

                       return true;
               } else {

                       return false;
               }

       } catch( Exception e ) {

               e.printStackTrace();
               return false;
       }
}


}




