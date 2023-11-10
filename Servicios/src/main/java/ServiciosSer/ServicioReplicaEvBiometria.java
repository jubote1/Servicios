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
import CapaDAOSer.EmpleadoEncuestaDAO;
import CapaDAOSer.EmpleadoEncuestaDetalleDAO;
import CapaDAOSer.EmpleadoEventoDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ItemInventarioDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.EmpleadoEncuesta;
import ModeloSer.EmpleadoEncuestaDetalle;
import ModeloSer.EmpleadoEvento;
import ModeloSer.Insumo;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioReplicaEvBiometria {
	
	
	
	
public static void main(String[] args)
{
	ServicioReplicaEvBiometria reporteReplicaUsuarios = new ServicioReplicaEvBiometria();
	reporteReplicaUsuarios.generarReplicaEvBiometria();
	
}

public void generarReplicaEvBiometria()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCIÓN");
	//Generamos la fecha en la que corre el proceso
	Date fechaActual = new Date();
	//Teniendo en cuenta que este es un proceso que se correrá cada 15 minutos, la idea es que se alerte cuando se tengan
	//tiendas con un resultado no exitoso.
	String noExitoso = "";
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	//Variable para administrar las inserciones de evento en el general
	boolean insEventoGeneral;
	boolean marMigrado;
	//Variable donde almacenaremos si hay contingencias activadas
	String contingencia = "";
	//Obtenemos los eventos matriculados en la base de datos general
	ArrayList<EmpleadoEvento> eventosEmpGeneral = EmpleadoEventoDAO.obtenerEventosGeneral();
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			//Validamos si está en contingencia
			String estadoConting = ParametrosDAO.retornarValorAlfanumericoTienda(tien.getHostBD(), "CONFBIOMEREMOTA");
			if(estadoConting.equals(new String("N")))
			{
				contingencia = contingencia + " " + tien.getNombreTienda();
				//Se realiza la devolución de la contingencia de la tienda
				ParametrosDAO.EditarParametroTienda(tien.getHostBD(), "CONFBIOMEREMOTA", "S", 0);
			}
			//Borramos eventos de días anteriores, dado que en el local solo nos interesan eventos del día en cuestión
			boolean borradoLocal = EmpleadoEventoDAO.borrarEventoRegistroEmpleadoLocal(tien.getHostBD());
			//Si se tuvo un erro con el borrado local se salta a la siguiente iteración del for de tiendas
			if(!borradoLocal)
			{
				continue;
			}
			//Verificamos si existen registros para llevar a la central y los retornamos
			ArrayList<EmpleadoEvento> eventosLocales = EmpleadoEventoDAO.obtenerEventosPendientesLocal(tien.getHostBD());
			//Recorremos estos eventos para irlos insertando
			for(EmpleadoEvento evenTemp: eventosLocales)
			{
				//Realizamos la inserción en la base de datos general
				insEventoGeneral = EmpleadoEventoDAO.insertarEventoRegistroEmpleado(evenTemp);
				//Realizamos actualización de la fecha_hora_log en el sistema de contact
				EmpleadoEventoDAO.actualizarEventoRegistroEmpleadoGeneral(evenTemp);
				//Si la inserción es exitosa, se realizará el marcado de migrado
				if(insEventoGeneral)
				{
					marMigrado = EmpleadoEventoDAO.marcarEventoRegistroEmpleadoLocal(evenTemp.getId(), evenTemp.getTipoEvento(), evenTemp.getFecha(), tien.getHostBD());
					if(!marMigrado)
					{
						noExitoso = noExitoso + " " + tien.getNombreTienda();
						break;
					}
				}
				else
				{
					noExitoso = noExitoso + " " + tien.getNombreTienda();
					break;
				}
					
			}
			//Actualizamos el valro de eventosEmpGeneral que pudo haber cambiando en el fragmento anterior
			eventosEmpGeneral = EmpleadoEventoDAO.obtenerEventosGeneral();
			//Continuamos con la sincronización en la otra vía en donde basicamente buscamos sincronizar lo realizado en el servidor central para llevarlo a los puntos de venta
			for(EmpleadoEvento evenTemp : eventosEmpGeneral)
			{
				//Validamos si el registro no existe o existe, sino existe será insertado en la bd local con el fin de tener la bd sincronizadas
				boolean existe = EmpleadoEventoDAO.existeEventoEmpleadoLocal(evenTemp.getId(), evenTemp.getFecha(), evenTemp.getTipoEvento(), tien.getHostBD());
				if(!existe)
				{
					//Realizamos inserción del registro en la tienda
					EmpleadoEventoDAO.insertarEventoRegistroEmpleadoLocal(evenTemp, tien.getHostBD());
					//Realizamos actualización de la fecha_hora_log
					EmpleadoEventoDAO.actualizarEventoRegistroEmpleadoLocal(evenTemp, tien.getHostBD());
				}
			}
			
			//Obtenemos el encabezado de las encuestas para tienda en cuestion
			ArrayList<EmpleadoEncuesta> empleadosEncuesta = EmpleadoEncuestaDAO.obtenerEmpleadoEncuesta(tien.getHostBD());
			for(EmpleadoEncuesta empEncTemp : empleadosEncuesta)
			{
				//Realizamos la inserción del encabezado de la encuesta
				int idEmpleadoEncuesta = EmpleadoEncuestaDAO.insertarEmpleadoEncuesta(empEncTemp);
				//Recuperamos el detalle de la encuesta empleado
				ArrayList<EmpleadoEncuestaDetalle> empleadoEncuestaDetalle = EmpleadoEncuestaDetalleDAO.obtenerEmpleadoEncuesta(tien.getHostBD(), empEncTemp.getIdEmpleadoEncuesta());
				//Realizamos la inserción de los detalles anteponiendo el nuevo idEmpleadoEncuesta según el nuevo encabezado
				for(EmpleadoEncuestaDetalle empEncDetTemp: empleadoEncuestaDetalle)
				{
					empEncDetTemp.setIdEmpleadoEncuesta(idEmpleadoEncuesta);
					//Procedemos a la inserción de los deatlles
					EmpleadoEncuestaDetalleDAO.insertarEmpleadoEncuestaDetalle(empEncDetTemp);
				}
				//Procedemos a borrar los detalles de la encuesta empleado
				EmpleadoEncuestaDetalleDAO.borrarEmpleadoEncuestaDetalle(empEncTemp.getIdEmpleadoEncuesta(), tien.getHostBD());
				//Procedemos a borrar el encabezado de la encuesta empleado
				EmpleadoEncuestaDAO.borrarEmpleadoEncuesta(empEncTemp.getIdEmpleadoEncuesta(), tien.getHostBD());
			}
			
		}
	}
	
	//Realizamos el envío del correo electrónico con los archivos
	noExitoso = noExitoso.trim();
	//Controlamos que si se halla tenido algún error
	if(noExitoso.length() > 0)
	{
		Correo correo = new Correo();
		correo.setAsunto("REPLICA DE EVENTOS DE BIOMETRIA " + fechaActual.toString());
		correo.setContrasena("Pizzaamericana2017");
		//Tendremos que definir los destinatarios de este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPLICAUSUARIOS");
		correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
		correo.setMensaje("A continuación informamos las tiendas "
				+ " que no lograron la actualización  " + noExitoso);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}
	if(!contingencia.trim().equals(new String("")))
	{
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("HAY ACTIVADA CONTINGENCIA BIOMETRIA " + fechaActual.toString());
		correo.setContrasena(infoCorreo.getClaveCorreo());
		//Tendremos que definir los destinatarios de este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPLICAUSUARIOS");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuación informamos las tiendas que tienen actividad contingencia "
				+ contingencia);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}
}


}




