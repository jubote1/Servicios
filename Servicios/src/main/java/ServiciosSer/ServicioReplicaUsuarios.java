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
	String noExitoso = "";
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocalSinBodega();
	//Retornamos los objetos de empleados y la biometria, primero debemos retornar
	ArrayList<Usuario> usuarios = UsuarioDAO.obtenerEmpleadosGeneral();
	//Obtenemos los empleados inactivos
	ArrayList<Usuario> usuariosIna = UsuarioDAO.obtenerEmpleadosInactivosGeneral();
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
					//En la inserción de cada usuario local se debe validar si se debe insertar o actualizar el usaurio ya existente
					//Validamos si el usuario tiene asignada clave rápida 
					if((usuTemp.getClaveRapida() != null) && (!usuTemp.getClaveRapida().equals(new String("null"))))
					{
						if(usuTemp.getClaveRapida().length() > 0)
						{
							//Validamos la existencia del usuario en la tienda con el idUsuario
							boolean usuarioExiste = UsuarioDAO.existeUsuarioLocal(usuTemp.getIdUsuario(), tien.getHostBD());
							if(usuarioExiste)
							{
								UsuarioDAO.actualizarUsuarioLocal(usuTemp, tien.getHostBD());
							}else
							{
								UsuarioDAO.insertarUsuarioLocal(usuTemp, tien.getHostBD());
							}
						}
					}
				}
				if(idUsuarioIns != -1)
				{
					for(EmpleadoBiometria empBioTemp: usuariosBiometria)
					{
						UsuarioDAO.insertarEmpleadoBiometriaLocal(empBioTemp, tien.getHostBD());
					}
				}
				//Continuamos con la verificación de eliminación de los empleados inactivos
				for(Usuario usuTemp: usuariosIna)
				{
					if(usuTemp.getClaveRapida() != null)
					{
						if(usuTemp.getClaveRapida().length() > 0)
						{
							//Validamos la existencia del usuario en la tienda con el idUsuario
							boolean usuarioExiste = UsuarioDAO.existeUsuarioLocal(usuTemp.getIdUsuario(), tien.getHostBD());
							if(usuarioExiste)
							{
								UsuarioDAO.eliminarUsuarioLocal(usuTemp.getIdUsuario(), tien.getHostBD());
							}
						}
					}
				}
			}
			else
			{
				noExitoso = noExitoso + " <p>" + tien.getNombreTienda() + " </p>";
			}
		}
	}
	
	if(!noExitoso.equals(new String("")))
	{
		//Realizamos el envío del correo electrónico con los archivos
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("PROBLEMAS REPLICA DE USUARIOS EN TIENDAS " + fechaActual.toString());
		correo.setContrasena(infoCorreo.getClaveCorreo());
		//Tendremos que definir los destinatarios de este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPLICAUSUARIOS");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		String mensaje = "Las tiendas que no lograron la actualización fueron " + noExitoso;
		correo.setMensaje(mensaje);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}
	
}


}




