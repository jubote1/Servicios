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

public class ServicioRevisionCierres {
	
	
	
/**
 * Este programa se encargar� de correr como un servicio todos los d�as a las 12:50 am, con el fin de revisar
 * si los sistemas se encuentran cerrados y enviar un mensaje al correo con la revisi�n.
 * @param args
 */
public static void main(String[] args)
{
	ServicioRevisionCierres reporteRevisionCierres = new ServicioRevisionCierres();
	reporteRevisionCierres.generarRevisionCierres();
	
}

public void generarRevisionCierres()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCI�N");
	//Generamos la fecha en la que corre el proceso
	Date fechaActual = new Date();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//Formateamos la fecha Actual para consulta
	String strFechaActual = dateFormat.format(fechaActual);
	//Vamos a recuperar el d�a anterior que seg�n esto es el d�a real de trabajo
	Calendar calendarioActual = Calendar.getInstance();
	calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
	Date fechaAnterior = calendarioActual.getTime();
	String strFechaAnterior = dateFormat.format(fechaAnterior);
	//Con lo anterior ya tenemos las variables para el proceso
	
	//Generamos String de tiendas exitosas y tiendas no exitosas para mandar correo.
	String noExitoso = "";
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	//Retornamos los objetos de empleados y la biometria, primero debemos retornar
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			//Recuperamos el indicador de cierre de la tienda
			String indicadorCierre = ParametrosDAO.retornarValorAlfanumericoTienda(tien.getHostBD(), "INDICADORCIERRE");
			if(indicadorCierre.equals(new String("ERROR")))
			{
				noExitoso = noExitoso + " " + tien.getNombreTienda() + " no se tuvo conexi�n.";
			}else
			{
				//Recuperaremos el valor de la fecha del sistema para compararla	
				String fechaApertura = TiendaDAO.retornarFechaTiendaRemota(tien.getHostBD());
				//Hacemos la comparaci�n de las fechas
				if(fechaApertura.equals(new String(indicadorCierre)))
				{
					
				}else if(fechaApertura.trim().equals(new String(strFechaAnterior.trim())))
				{
					noExitoso = noExitoso + " <p>" + tien.getNombreTienda() + " NOK el sistema est� abierto al d�a anterior." + "</p>";
				}else if(fechaApertura.trim().equals(new String(strFechaActual.trim())))
				{
					noExitoso = noExitoso + " <p>" + tien.getNombreTienda() + " NOK el sistema est� abierto al d�a siguiente." + "</p>";
				}else
				{
					noExitoso = noExitoso + " <p>" + tien.getNombreTienda() + " NOK el sistema est� abierto a una fecha no explicable." + "</p>";
				}
			}
		}
	}
	if(!noExitoso.equals(new String("")))
	{
		//Realizamos el env�o del correo electr�nico con los archivos
		Correo correo = new Correo();
		correo.setAsunto("PROBLEMA REVISI�N CIERRE DIARIO TIENDAS " + fechaAnterior.toString());
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		//Tendremos que definir los destinatarios de este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REVISIONCIERRE");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		String mensaje = "A continuaci�n informamos las tiendas que presentan problemas con la revisi�n del cierre  " + noExitoso ;
		correo.setMensaje(mensaje);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}
	
}


}




