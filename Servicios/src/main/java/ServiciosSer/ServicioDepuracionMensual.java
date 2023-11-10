package ServiciosSer;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
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
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import CapaDAOSer.DepuracionDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TicketPromedioMesDAO;
import CapaDAOSer.TiendaDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.TicketPromedioMes;
import ModeloSer.Tienda;
import capaControladorPOS.PedidoCtrl;
import capaDAOCC.RazonSocialDAO;
import capaModeloCC.RazonSocial;
import capaModeloPOS.TicketPromedio;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioDepuracionMensual {
	
	/**
	 * Partimos de la premisa que el proceso corre el 30 de cada mes
	 */
	public void generarDepuracion()
	{
		DecimalFormat formatea = new DecimalFormat("###,###");
		//Posteriormente realizamos el procesamiento para definir el rango de fechas del cual deseamos procesar el reporte
		//Recuperamos la fecha actual del sistema con la fecha apertura
		String fechaActual = "";
		Calendar calendarioActual = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		//Obtenemos la fecha Actual
		
		try
		{
			//OJO
			fechaActual = dateFormat.format(calendarioActual.getTime());
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
		//Obtenemos el mes actual y año actual
		int mesActual = calendarioActual.get(Calendar.MONTH)+1;
		int anoActual = calendarioActual.get(Calendar.YEAR);
		int diaActual = calendarioActual.get(Calendar.DAY_OF_MONTH);
		int diaMaximoMesActual = Calendar.getInstance().getActualMaximum(calendarioActual.DAY_OF_MONTH);
		if(diaMaximoMesActual == diaActual)
		{
			ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
			//Construimos la respuesta para desplegar en el correo
			String respuesta = "";
			respuesta = respuesta + "<table border='2'> <tr> DEPURACION MENSUAL DE TIENDAS </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>NOMBRE TIENDA</strong></td>"
					+  "<td><strong>ESTADO DEPURACION</strong></td>"
					+  "</tr>";
			for(Tienda tien : tiendas)
			{
				if(!tien.getHostBD().equals(new String("")))
				{
					boolean resultado = DepuracionDAO.depuracionPOS(tien.getHostBD());
					if(resultado)
					{
						respuesta = respuesta + "<tr><td>" +  tien.getNombreTienda() + "</td><td>EXITOSO</td></tr>";
					}else
					{
						respuesta = respuesta + "<tr><td>" +  tien.getNombreTienda() + "</td><td>CON ERROR</td></tr>";
					}
					
				}
			}
			//Posteriormente realizamos el envío del correo
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("ESTADO PROCESO DEPURACION MENSUAL " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEPAGOSVIRTUAL");
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuación el reporte Mensual de cantidad de pedidos - " + fechaActual +  ": \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		}
		
	}
	
	

public static void main(String[] args)
{
	ServicioDepuracionMensual reporteDomicios = new ServicioDepuracionMensual();
	reporteDomicios.generarDepuracion();
	//ConexionBaseDatos con = new ConexionBaseDatos();
	//Connection con1 = con.obtenerConexionBDTienda("");
	
}

}




