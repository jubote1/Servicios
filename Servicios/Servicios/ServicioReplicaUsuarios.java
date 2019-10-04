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

public class ServicioReplicaUsuarios {
	
	
	
	
public static void main(String[] args)
{
	ServicioReplicaUsuarios reporteReplicaUsuarios = new ServicioReplicaUsuarios();
	reporteReplicaUsuarios.generarReplicaUsuarios();
	
}

public void generarReplicaUsuarios()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCIÓN");
	//Generamos la fecha en la que corre el proceso
	Date fechaActual = new Date();
	//Generamos String de tiendas exitosas y tiendas no exitosas para mandar correo.
	String exitoso = "", noExitoso = "";
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	//Retornamos los objetos de empleados y la biometria, primero debemos retornar
	ArrayList<Usuario> usuarios = UsuarioDAO.obtenerEmpleadosGeneral();
	ArrayList<EmpleadoBiometria> usuariosBiometria = UsuarioDAO.obtenerEmpleadosBiometriaGeneral();
	//Variable idUsuarioIns para controlar si hay error insertando
	int idUsuarioIns;
	for(Tienda tien : tiendas)
	{
		idUsuarioIns = 0;
		if(!tien.getHostBD().equals(new String("")))
		{
			//Realizamos clareo de las tablas locales
			boolean respuestaEliminacion = UsuarioDAO.eliminarInfoEmpleadoLocal(tien.getHostBD());
			if(respuestaEliminacion)
			{
				//Realizamos el recorrido para la inserción de todos los empleados
				for(Usuario usuTemp: usuarios)
				{
					idUsuarioIns = UsuarioDAO.insertarEmpleadoLocal(usuTemp, tien.getHostBD());
					if(idUsuarioIns == -1)
					{
						noExitoso = noExitoso + " " + tien.getNombreTienda();
						break;
					}
				}
				if(idUsuarioIns != -1)
				{
					for(EmpleadoBiometria empBioTemp: usuariosBiometria)
					{
						UsuarioDAO.insertarEmpleadoBiometriaLocal(empBioTemp, tien.getHostBD());
					}
					exitoso = exitoso + " " + tien.getNombreTienda();
				}
			}
			else
			{
				noExitoso = noExitoso + " " + tien.getNombreTienda();
			}
		}
	}
	
	//Realizamos el envío del correo electrónico con los archivos
	Correo correo = new Correo();
	correo.setAsunto("REPLICA DE USUARIOS EN TIENDAS " + fechaActual.toString());
	correo.setContrasena("Pizzaamericana2017");
	//Tendremos que definir los destinatarios de este correo
	ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPLICAUSUARIOS");
	correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
	correo.setMensaje("A continuación informamos que las tiendas que actualizaron correctamente los usuarios fueron " + exitoso + " , y las tiendas"
			+ " que no lograron la actualización fueron " + noExitoso);
	ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
	contro.enviarCorreoHTML();
}


}




