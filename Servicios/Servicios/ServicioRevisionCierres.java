package Servicios;

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
import utilidades.ControladorEnvioCorreo;
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

import CapaDAOServicios.GeneralDAO;
import CapaDAOServicios.ItemInventarioDAO;
import CapaDAOServicios.ParametrosDAO;
import CapaDAOServicios.PedidoDAO;
import CapaDAOServicios.TiendaDAO;
import CapaDAOServicios.UsuarioDAO;
import Modelo.Correo;
import Modelo.EmpleadoBiometria;
import Modelo.Insumo;
import Modelo.Tienda;
import Modelo.Usuario;

public class ServicioRevisionCierres {
	
	
	
/**
 * Este programa se encargará de correr como un servicio todos los días a las 12:50 am, con el fin de revisar
 * si los sistemas se encuentran cerrados y enviar un mensaje al correo con la revisión.
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
	System.out.println("EMPEZAMOS LA EJECUCIÓN");
	//Generamos la fecha en la que corre el proceso
	Date fechaActual = new Date();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//Formateamos la fecha Actual para consulta
	String strFechaActual = dateFormat.format(fechaActual);
	//Vamos a recuperar el día anterior que según esto es el día real de trabajo
	Calendar calendarioActual = Calendar.getInstance();
	calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
	Date fechaAnterior = calendarioActual.getTime();
	String strFechaAnterior = dateFormat.format(fechaAnterior);
	//Con lo anterior ya tenemos las variables para el proceso
	
	//Generamos String de tiendas exitosas y tiendas no exitosas para mandar correo.
	String exitoso = "", noExitoso = "";
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
				noExitoso = noExitoso + " " + tien.getNombreTienda() + " no se tuvo conexión.";
			}else
			{
				//Recuperaremos el valor de la fecha del sistema para compararla	
				String fechaApertura = TiendaDAO.retornarFechaTiendaRemota(tien.getHostBD());
				//Hacemos la comparación de las fechas
				if(fechaApertura.trim().equals(new String(indicadorCierre.trim())))
				{
					exitoso = exitoso + " <p>" + tien.getNombreTienda() + " se encuentra OK Cerrado." + "</p>";
				}else if(fechaApertura.trim().equals(new String(strFechaAnterior.trim())))
				{
					noExitoso = noExitoso + " <p>" + tien.getNombreTienda() + " NOK el sistema está abierto al día anterior." + "</p>";
				}else if(fechaApertura.trim().equals(new String(strFechaActual.trim())))
				{
					noExitoso = noExitoso + " <p>" + tien.getNombreTienda() + " NOK el sistema está abierto al día siguiente." + "</p>";
				}else
				{
					noExitoso = noExitoso + " <p>" + tien.getNombreTienda() + " NOK el sistema está abierto a una fecha no explicable." + "</p>";
				}
			}
		}
	}
	
	//Realizamos el envío del correo electrónico con los archivos
	Correo correo = new Correo();
	correo.setAsunto("REVISIÓN CIERRE DIARIO TIENDAS " + fechaAnterior.toString());
	correo.setContrasena("Pizzaamericana2017");
	//Tendremos que definir los destinatarios de este correo
	ArrayList correos = GeneralDAO.obtenerCorreosParametro("REVISIONCIERRE");
	correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
	String mensaje = "A continuación informamos el estado de los cierres de las tiendas  " + exitoso ;
	if(noExitoso.trim().length() > 0)
	{
		mensaje = mensaje + " , y las tiendas"
				+ " con problemas fueron " + noExitoso;
	}
	correo.setMensaje(mensaje);
	ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
	contro.enviarCorreoHTML();
}


}




