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
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ItemInventarioDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import CapaDAOSer.VarianzaResumenDAO;
import CapaDAOSer.VarianzaResumenHistoricoDAO;
import ModeloSer.ConsumoInventario;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.Insumo;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import ModeloSer.VarianzaResumen;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioReplicaVarianzaHistorico {
	
	
	
	
public static void main(String[] args)
{
	ServicioReplicaVarianzaHistorico reporteConsumosUsuarios = new ServicioReplicaVarianzaHistorico();
	reporteConsumosUsuarios.generarReplicaVarianzasHistoricos();
	
}

public void generarReplicaVarianzasHistoricos()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCI�N");
	//Generamos la fecha en la que corre el proceso
	Date fechaActual = new Date();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//Formateamos la fecha Actual para consulta
	String strFechaActual = dateFormat.format(fechaActual);
	//String strFechaActual = "2020-08-20";
	
	//Restarle el d�a para que como se har� d�a atrasado
	Calendar calendarioActual = Calendar.getInstance();
	try
	{
		//Al objeto calendario le fijamos la fecha actual del sitema
		calendarioActual.setTime(fechaActual);
		calendarioActual.add(Calendar.DAY_OF_YEAR, -15);
	}catch(Exception e)
	{
		System.out.println(e.toString());
	}
	//Generamos String de tiendas exitosas y tiendas no exitosas para mandar correo.
	String respuesta = "";
	boolean existeVarianza = false;
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	VarianzaResumen varTemp;
	for(int i = 1; i <= 14; i++)
	{
		//Llevamos a un string la fecha anterior para el c�lculo de la venta
		fechaActual = calendarioActual.getTime();
		strFechaActual = dateFormat.format(fechaActual);
		//Comenzamos a recorrer una a una las tiendas
		for(Tienda tien : tiendas)
		{
			
			if(!tien.getHostBD().equals(new String("")))
			{
				//Validaremos si para la fecha y la tienda en cuesti�n ya hay informaci�n
				existeVarianza = VarianzaResumenHistoricoDAO.existeVarianza(strFechaActual, tien.getIdTienda());
				if(!existeVarianza)
				{
					try
					{
						ArrayList<VarianzaResumen> varResumen = VarianzaResumenDAO.consultarVarianzaResumen(tien.getHostBD(), strFechaActual);
						for(int j = 0; j  < varResumen.size(); j++)
						{
							varTemp = varResumen.get(j);
							VarianzaResumenHistoricoDAO.insertarVarianzaResumenHistorico(varTemp, tien.getIdTienda());
						}
						if(varResumen.size() > 0)
						{
							respuesta = respuesta + " <p>" + tien.getNombreTienda() + " EXITOSO EN REPROCESO " + fechaActual +  " </p>";
						}
						
					}catch(Exception e)
					{
						respuesta = respuesta + " <p>" + tien.getNombreTienda() + " ERROR " +  " </p>";
					}
				}
			}
			try
			{
				//Al objeto calendario le fijamos la fecha actual del sitema
				calendarioActual.setTime(fechaActual);
				calendarioActual.add(Calendar.DAY_OF_YEAR, 1);
			}catch(Exception e)
			{
				System.out.println(e.toString());
			}
		}
	}
	//Realizamos el env�o del correo electr�nico con los archivos
	Correo correo = new Correo();
	correo.setAsunto("REPLICA DE VARIANZA DE TIENDAS " + fechaActual.toString());
	CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
	correo.setContrasena(infoCorreo.getClaveCorreo());
	//Tendremos que definir los destinatarios de este correo
	ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPLICAUSUARIOS");
	correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
	String mensaje = "A continuacion se informacion del proceso de replica de consumo de tiendas " + respuesta;
	correo.setMensaje(mensaje);
	ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
	contro.enviarCorreoHTML();
}


}




