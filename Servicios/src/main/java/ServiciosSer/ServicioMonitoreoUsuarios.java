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

import CapaDAOSer.ConsumoInventarioDAO;
import CapaDAOSer.ConsumoPorcionesDAO;
import CapaDAOSer.EmpleadoEventoDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ItemInventarioDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.ReporteHorariosDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.ConsumoInventario;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.EmpleadoEvento;
import ModeloSer.Insumo;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioMonitoreoUsuarios {
	
	
	
	
public static void main(String[] args)
{
	ServicioMonitoreoUsuarios servicioMonitoreoUsuarios = new ServicioMonitoreoUsuarios();
	servicioMonitoreoUsuarios.monitoreoUsuarios();
}

public void monitoreoUsuarios()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCI�N");
	//Generamos la fecha en la que corre el proceso
	Date fechaActual = new Date();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//Formateamos la fecha Actual para consulta
	String strFechaActual = dateFormat.format(fechaActual);
	//Restarle el d�a para que como se har� d�a atrasado
	Calendar calendarioActual = Calendar.getInstance();
	try
	{
		//Al objeto calendario le fijamos la fecha actual del sitema
		calendarioActual.setTime(fechaActual);
		calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
	}catch(Exception e)
	{
		System.out.println(e.toString());
	}
	
	//Llevamos a un string la fecha anterior para el c�lculo de la venta
	fechaActual = calendarioActual.getTime();
	strFechaActual = dateFormat.format(fechaActual);
	//Generamos String de tiendas exitosas y tiendas no exitosas para mandar correo	
	ArrayList<EmpleadoEvento>  repEntradasSalidas = ReporteHorariosDAO.obtenerEntradasSalidasEmpleadosEventos(strFechaActual,strFechaActual);
	boolean entrada = false;
	boolean salida = false;
	EmpleadoEvento empEventoAnterior = new EmpleadoEvento(0, "", "", "", 0, "");
	String resultado = "";
	for(int i = 0; i < repEntradasSalidas.size(); i++)
	{
		EmpleadoEvento empEventoTemp = repEntradasSalidas.get(i);
		//La idea es que esto pasar� una �nica vez, o la primera vez
		if(empEventoAnterior.getId() == 0)
		{
			empEventoAnterior = repEntradasSalidas.get(i);
		}
		if(empEventoAnterior.getId() == empEventoTemp.getId())
		{
			if(empEventoTemp.getTipoEvento().equals(new String("INGRESO")))
			{
				entrada = true;
				salida = false;
			}
			else if(empEventoTemp.getTipoEvento().equals(new String("SALIDA")))
			{
				salida = true;
			}
			if(entrada && salida)
			{
				//Realizaremos un substring de la hora y si esa hora es despuesde las 17 de la noche, sospechar que las cosas no andan
				//bien
				String strHora = empEventoAnterior.getFechaHoraLog().substring(11,13);
				int intHora = 0;
				try
				{
					intHora = Integer.parseInt(strHora);
				}catch(Exception e)
				{
					intHora = 0;
				}
				if(intHora >= 20)
				{
					resultado = resultado + " " + empEventoAnterior.getId()+ "-" + empEventoAnterior.getNombreEmpleado() + " INGRESO APARENTEMENTE TARD�O " + empEventoAnterior.getFechaHoraLog();
					String email = EmpleadoEventoDAO.obtenerCorreoElectronico(empEventoAnterior.getId());
					//Enviamos correo notificando al empleado que tiene problemas con al biometr�a
					Correo correo = new Correo();
					correo.setAsunto("POSIBLE INCONVENIENTE BIOMETRIA EN D�A " + fechaActual.toString());
					CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
					correo.setContrasena(infoCorreo.getClaveCorreo());
					//Tendremos que definir los destinatarios de este correo
					ArrayList correos = new ArrayList();
					correos.add(email);
					correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
					String mensaje = "Se�or Empleado el d�a de ayer tuvo problemas con el registro de su biometr�a, por favor revise apenas pueda y notifique la situaci�n. Tuvo INGRESO AL PARECER TARD�O " + empEventoAnterior.getFechaHoraLog();
					correo.setMensaje(mensaje);
					ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
					contro.enviarCorreoHTML();
				}
			}
		}else
		{
			if(salida == false)
			{
				resultado = resultado + " " + empEventoAnterior.getId()+ "-" + empEventoAnterior.getNombreEmpleado() + " INGRESO " + empEventoAnterior.getFechaHoraLog() + " - SALIDA NO HAY";
				String email = EmpleadoEventoDAO.obtenerCorreoElectronico(empEventoAnterior.getId());
				//Enviamos correo notificando al empleado que tiene problemas con al biometr�a
				Correo correo = new Correo();
				correo.setAsunto("POSIBLE INCONVENIENTE BIOMETRIA EN D�A " + fechaActual.toString());
				CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
				correo.setContrasena(infoCorreo.getClaveCorreo());
				//Tendremos que definir los destinatarios de este correo
				ArrayList correos = new ArrayList();
				correos.add(email);
				correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
				String mensaje = "Se�or Empleado el d�a de ayer tuvo problemas con el registro de su biometr�a, por favor revise apenas pueda y notifique la situaci�n. Tuvo INGRESO " + empEventoAnterior.getFechaHoraLog()+ " - SALIDA NO HAY";
				correo.setMensaje(mensaje);
				ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
				contro.enviarCorreoHTML();
			}
			entrada = false;
			salida = false;
			empEventoAnterior = repEntradasSalidas.get(i);
			if(empEventoTemp.getTipoEvento().equals(new String("INGRESO")))
			{
				entrada = true;
				salida = false;
			}
			else if(empEventoTemp.getTipoEvento().equals(new String("SALIDA")))
			{
				salida = true;
			}
		}
		
		
	}
	
	
	//Realizamos el env�o del correo electr�nico con los archivos
	Correo correo = new Correo();
	correo.setAsunto("EMPLEADOS CON POSIBLES PROBLEMAS BIOMETRIA EN D�A " + fechaActual.toString());
	CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
	correo.setContrasena(infoCorreo.getClaveCorreo());
	//Tendremos que definir los destinatarios de este correo
	ArrayList correos = GeneralDAO.obtenerCorreosParametro("ERRORBIOMETRIA");
	correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
	String mensaje = "A continuaci�n la informaci�n de las personas que posiblemente tienen problemas con el acceso " + resultado;
	correo.setMensaje(mensaje);
	ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
	contro.enviarCorreoHTML();
}


}




