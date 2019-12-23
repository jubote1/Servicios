package Servicios;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.Socket;
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
import CapaDAOServicios.IngresoInventarioDetalleTmpDAO;
import CapaDAOServicios.IngresoInventarioTmpDAO;
import CapaDAOServicios.InsumoDespachoTiendaDAO;
import CapaDAOServicios.InsumoDespachoTiendaDetalleDAO;
import CapaDAOServicios.ItemInventarioDAO;
import CapaDAOServicios.ParametrosDAO;
import CapaDAOServicios.PedidoDAO;
import CapaDAOServicios.TiendaDAO;
import CapaDAOServicios.UsuarioDAO;
import Modelo.Correo;
import Modelo.EmpleadoBiometria;
import Modelo.Insumo;
import Modelo.InsumoDespachoTienda;
import Modelo.InsumoDespachoTiendaDetalle;
import Modelo.ModificadorInventario;
import Modelo.Tienda;
import Modelo.Usuario;

public class ServicioActualizacionEnvInv {
	
	
	
	
public static void main(String[] args)
{
	ServicioActualizacionEnvInv reporteReplicaUsuarios = new ServicioActualizacionEnvInv();
	reporteReplicaUsuarios.actualizacionInventarios();
	
}

public void actualizacionInventarios()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCIÓN DE LA REPLICA DE INVENTARIOS");
	//Generamos la fecha en la que corre el proceso
	Date fechaActual = new Date();
	//Debemos de pasar la fecha al formato para consulta
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	String strFechaActual = dateFormat.format(fechaActual);
	//Generamos String de tiendas exitosas y tiendas no exitosas para mandar correo en el momento de recepción o error del proceso
	String exitoso = "", noExitoso = "";
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	//Obtenemos el listado de correos para cuando hay algún error en el proceso
	ArrayList correosError = GeneralDAO.obtenerCorreosParametro("ERRORREPLICAINV");
	ArrayList correosExitoso = GeneralDAO.obtenerCorreosParametro("REPLICAINV");
	for(Tienda tien : tiendas)
	{
			//Realizamos la recuperación de la homologaciones para la tienda que estamos procesando
			
			//Verificamos si hay conectividad con el punto de venta
			 String dirConTact = tien.getHostBD();
			 int puerto = 3306;
			 //Variable que nos dirá si hay o no conectividad
			 boolean hayConectividad = false;
			 try{
				  Socket s = new Socket(dirConTact, puerto);
				  if(s.isConnected()){
					  System.out.println("Conexión establecida con la dirección: " +  dirConTact + " a travéz del puerto: " + puerto);
					  hayConectividad = true;
				  }
			 }catch(Exception e)
			 {
				 System.out.println("CONEXIÓN NO establecida con la dirección: " +  dirConTact + " a travéz del puerto: " + puerto);
				 hayConectividad = false;
			 }
			 if (hayConectividad)
			 {
				//Verificar si la tienda tiene algo pendiente
					boolean existeInvPendiente = InsumoDespachoTiendaDAO.existeInsumoDespachadoTienda(tien.getIdTienda(), strFechaActual);
					//Si existe inventarios para despachar
					if(existeInvPendiente)
					{
						//Recuperamos los despachos pendientes
						ArrayList<InsumoDespachoTienda> insumoDespachos = InsumoDespachoTiendaDAO.obtenerInsumoDespachoTienda(tien.getIdTienda(), strFechaActual);
						//Recorremos los despachos pendientes de la tienda para la fecha
						//Variable donde almacenamos el resultado de la inserción del encabezado
						boolean insercionEnc = false;
						//Vamos llevando un control de la inserción de cada detalle
						boolean insercionDet = false;
						//Variable para controlar si hubo error en la inserción del despacho en la tienda
						boolean huboError = false;
						boolean verificarExisteDespacho = false;
						for(InsumoDespachoTienda encabezadoDespacho : insumoDespachos)
						{
							//Verificamos que el despacho a ingresar no haya sido ya ingresado en la tienda
							verificarExisteDespacho = IngresoInventarioTmpDAO.existeIngresoInventarioTmp(encabezadoDespacho.getIdDespacho(), tien.getHostBD());
							//En el caso correcto que no exista el despacho
							if(!verificarExisteDespacho)
							{
								huboError = false;
								//Recuperamos los detalles de los insumos del despacho para ser insertados en la tienda.
								ArrayList<InsumoDespachoTiendaDetalle> detallesDespacho  = InsumoDespachoTiendaDetalleDAO.obtenerDetalleDespachoTienda(encabezadoDespacho.getIdDespacho());
								//Comenzamos a recorrer el detalle y a insertarlo en tienda.
								//Verificaremos si hay más de un detalle para insertar
								
								if(detallesDespacho.size() > 0)
								{
									//En este punto hacemos la inserción del encabezado
									insercionEnc = IngresoInventarioTmpDAO.insertarIngresoInventarioTmp(strFechaActual, encabezadoDespacho.getIdDespacho(), tien.getHostBD(), encabezadoDespacho.getObservacion());
								}
								for(InsumoDespachoTiendaDetalle detalleDespacho: detallesDespacho)
								{
									//Se valida que se haya insertado el encabezado
									if(insercionEnc)
									{
										//Realizamos la homologación del idInsumo entre bodega y tienda
										ModificadorInventario modIngreso = new ModificadorInventario(detalleDespacho.getIdInsumo(), detalleDespacho.getCantidad() );
										insercionDet = IngresoInventarioDetalleTmpDAO.insertarIngresoInventarioDetTmp(encabezadoDespacho.getIdDespacho(), modIngreso, tien.getHostBD());
										//Es porque hubo error en la inserción de un detalle, por lo cual devolvemos y notificamos el error
										if(!insercionDet)
										{
											//Borramos los detalles
											IngresoInventarioDetalleTmpDAO.borrarIngresoInventarioDetallesTmp(encabezadoDespacho.getIdDespacho(), tien.getHostBD());
											//Borramos el encabezado
											IngresoInventarioTmpDAO.borrarIngresoInventarioTmp(encabezadoDespacho.getIdDespacho(), tien.getHostBD());
											Correo correo = new Correo();
											correo.setAsunto("ERROR REPLICA DETALLE DESPACHO " + fechaActual.toString());
											correo.setContrasena("Pizzaamericana2017");
											//Tendremos que definir los destinatarios de este correo
											correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
											correo.setMensaje("A continuación informamos que la tienda " + tien.getNombreTienda() + " tuvo problemas en la creación del detalle del despacho de pedido. ");
											ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correosError);
											contro.enviarCorreoHTML();
											huboError = true;
											break;
										}
									}else
									{
										//Se borra el encabezado del despacho y se quiebra el ciclo for
										IngresoInventarioTmpDAO.borrarIngresoInventarioTmp(encabezadoDespacho.getIdDespacho(), tien.getHostBD());
										Correo correo = new Correo();
										correo.setAsunto("ERROR REPLICA ENCABEZADO DESPACHO " + fechaActual.toString());
										correo.setContrasena("Pizzaamericana2017");
										//Tendremos que definir los destinatarios de este correo
										correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
										correo.setMensaje("A continuación informamos que la tienda " + tien.getNombreTienda() + " tuvo problemas en la creación del encabezado del despacho de pedido. ");
										ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correosError);
										contro.enviarCorreoHTML();
										break;
									}
								}
								//En caso de que no hayamos tenido problemas en la inserción del encabezado es posible el envío
								//del correo
								if(insercionEnc)
								{
									//Validamos si no hubo error en la inserción del despacho y si no es así, enviamos correos con la confirmación
									Correo correo = new Correo();
									correo.setAsunto("REPLICA DE DESPACHO " + tien.getNombreTienda() + " " + fechaActual.toString());
									correo.setContrasena("Pizzaamericana2017");
									//Tendremos que definir los destinatarios de este correo
									correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
									correo.setMensaje("Se ha replicado correctamente en la tienda " + tien.getNombreTienda() + " el despacho de Inventario número " + encabezadoDespacho.getIdDespacho());
									ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correosExitoso);
									contro.enviarCorreoHTML();
									// En este punto debemos de cambiar el estado del despacho para ponerlo en estado preingresado
									InsumoDespachoTiendaDAO.cambiarEstadoInsumoDespachadoTienda(encabezadoDespacho.getIdDespacho(), "PREINGRESADO");
								}
							}else
							{
								//Enviamos correo indicando que se está intentando ingresar un despacho que ya existe
								Correo correo = new Correo();
								correo.setAsunto("ERROR DESPACHO REPETIDO " + tien.getNombreTienda() + " , despacho " + encabezadoDespacho.getIdDespacho()  + fechaActual.toString());
								correo.setContrasena("Pizzaamericana2017");
								//Tendremos que definir los destinatarios de este correo
								correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
								correo.setMensaje("A continuación informamos que la tienda " + tien.getNombreTienda() + " tuvo problemas se está intentando ingresar un posible despacho que ya existe. El despacho es el  número " + encabezadoDespacho.getIdDespacho());
								ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correosError);
								contro.enviarCorreoHTML();
								break;
							}
						}
					}
			 }


	}
}


}




