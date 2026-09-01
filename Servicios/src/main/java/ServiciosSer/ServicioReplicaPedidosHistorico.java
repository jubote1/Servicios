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
import CapaDAOSer.DespachoRealDAO;
import CapaDAOSer.DespachoRealDetDAO;
import CapaDAOSer.DetallePedidoAllDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ItemInventarioDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.ConsumoInventario;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.DespachoReal;
import ModeloSer.DespachoRealDet;
import ModeloSer.DetallePedidoAll;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.Insumo;
import ModeloSer.PedidoAll;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioReplicaPedidosHistorico {
	
	
	
	
public static void main(String[] args)
{
	ServicioReplicaPedidosHistorico reporteConsumosUsuarios = new ServicioReplicaPedidosHistorico();
	reporteConsumosUsuarios.generarReplicaPedidosHistoricos();
	
}

public void generarReplicaPedidosHistoricos()
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
	int cantidadDias = 0;
	try
	{
		cantidadDias = ParametrosDAO.retornarValorNumerico("CANTIDADDIASPEDIDOS");
	}catch(Exception e)
	{
		cantidadDias = 20;
	}
	//Generamos String de tiendas exitosas y tiendas no exitosas para mandar correo.
	String respuesta = "";
	String respuestaDetalle = "";
	String respuestaDespReal = "";
	String respuestaDespRealDet = "";
	boolean existePedidos = false;
	boolean existeDetallePedidos = false;
	boolean existeDespachoReal = false;
	boolean existeDespachoRealDet = false;
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	for(int i = cantidadDias; i >= 1; i--)
	{
		// Calculamos la fecha correspondiente restando 'i' días a la fecha actual
        calendarioActual = Calendar.getInstance();
        calendarioActual.add(Calendar.DAY_OF_YEAR, -i);
        Date fechaProceso = calendarioActual.getTime();
        strFechaActual = dateFormat.format(fechaProceso);
        
        System.out.println("Procesando fecha: " + strFechaActual);
		//Comenzamos a recorrer una a una las tiendas
		ArrayList<PedidoAll> pedidos = new ArrayList();
		ArrayList<DetallePedidoAll> detallePedidos = new ArrayList();
		ArrayList<DespachoReal> despachosReales = new ArrayList();
		ArrayList<DespachoRealDet> despachosRealesDet = new ArrayList();
		for(Tienda tien : tiendas)
		{
			if (tien.getIdTienda() != 12)
			{
				if(!tien.getHostBD().equals(new String("")))
				{
					//Validaremos si para la fecha y la tienda en cuesti�n ya hay informaci�n
					existePedidos = PedidoDAO.existePedidosFecha(strFechaActual, tien.getIdTienda());
					existeDetallePedidos = DetallePedidoAllDAO.existeDetallePedidosFecha(strFechaActual, tien.getIdTienda());
					existeDespachoReal = DespachoRealDAO.existeDespachoRealFecha(strFechaActual,  tien.getIdTienda());
					existeDespachoRealDet = DespachoRealDetDAO.existeDespachoRealDetFecha(strFechaActual, tien.getIdTienda());
					if(!existePedidos)
					{
						try
						{
							//Una vez obtenidos los pedidos del d�a en cuesti�n realizaremos la inserci�n en el sistema de Bodega
							pedidos = PedidoDAO.recuperarPedidosPorFecha(strFechaActual, tien.getHostBD());
							PedidoDAO.insertarLotePedidos(pedidos);
							if(pedidos.size() > 0)
							{
								respuesta = respuesta + " <p>" + tien.getNombreTienda() + " EXITOSO - PEDIDOS " + strFechaActual +  " </p>";
							}else
							{
								respuesta = respuesta + " <p>" + tien.getNombreTienda() + " CUIDADO SE REPLICO CERO EN PEDIDOS  - PEDIDOS" + strFechaActual +  " </p>";
							}
							
						}catch(Exception e)
						{
							respuesta = respuesta + " <p>" + tien.getNombreTienda() + " ERROR - PEDIDOS " + strFechaActual +  " </p>";
						}
					}
					if(!existeDetallePedidos)
					{
						
						try
						{
							//Una vez obtenidos los detalles pedidos del d�a en cuesti�n realizaremos la inserci�n en el sistema de Bodega
							detallePedidos = DetallePedidoAllDAO.recuperarDetallePorPedido(strFechaActual, tien.getHostBD());
							DetallePedidoAllDAO.insertarLoteDetallePedido(detallePedidos);
							if(detallePedidos.size() > 0)
							{
								respuestaDetalle = respuestaDetalle + " <p>" + tien.getNombreTienda() + " EXITOSO - DETALLE PEDIDOS " + strFechaActual +  " </p>";
							}else
							{
								respuestaDetalle = respuestaDetalle + " <p>" + tien.getNombreTienda() + " CUIDADO SE REPLICO CERO EN PEDIDOS  - DETALLE PEDIDOS" + strFechaActual +  " </p>";
							}
							
						}catch(Exception e)
						{
							respuestaDetalle = respuestaDetalle + " <p>" + tien.getNombreTienda() + " ERROR - DETALLE PEDIDOS " + strFechaActual +  " </p>";
						}
					}
					if(!existeDespachoReal)
					{
						try
						{
							despachosReales = DespachoRealDAO.obtenerDespachoRealFecha(strFechaActual, tien.getHostBD());
							DespachoRealDAO.insertarDespachoRealLote(despachosReales);
							if(despachosReales.size() > 0)
							{
								respuestaDespReal = respuestaDespReal + " <p>" + tien.getNombreTienda() + " EXITOSO - DESPACHO-REAL " + strFechaActual +  " </p>";
							}else
							{
								respuestaDespReal = respuestaDespReal + " <p>" + tien.getNombreTienda() + " CUIDADO SE REPLICO CERO EN DESPACHO-REAL" + strFechaActual +  " </p>";
							}
							
						}catch(Exception e)
						{
							respuestaDespReal = respuestaDespReal + " <p>" + tien.getNombreTienda() + " ERROR - DESPACHO-REAL " + strFechaActual +  " </p>";
						}
					}
					if(!existeDespachoRealDet)
					{
						try
						{
							despachosRealesDet = DespachoRealDetDAO.obtenerDespachoRealDetFecha(strFechaActual, tien.getHostBD());
							DespachoRealDetDAO.insertarDespachoRealDetLote(despachosRealesDet, tien.getIdTienda());
							if(despachosRealesDet.size() > 0)
							{
								respuestaDespRealDet = respuestaDespRealDet + " <p>" + tien.getNombreTienda() + " EXITOSO - DESPACHO-REAL-DET " + strFechaActual + " </p>";
							}else
							{
								respuestaDespRealDet = respuestaDespRealDet + " <p>" + tien.getNombreTienda() + " CUIDADO SE REPLICO CERO EN DESPACHO-REAL-DET" + strFechaActual + " </p>";
							}
							
						}catch(Exception e)
						{
							respuestaDespRealDet = respuestaDespRealDet + " <p>" + tien.getNombreTienda() + " ERROR - DESPACHO-REAL-DET " + strFechaActual + " </p>";
						}
					}
				}
			}
		}
	}

	//Realizamos el env�o del correo electr�nico con los archivos
	Correo correo = new Correo();
	correo.setAsunto("REPLICA DE PEDIDOS EN SISTEMA CENTRALIZADO HISTÓRICO" + fechaActual.toString());
	CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
	correo.setContrasena(infoCorreo.getClaveCorreo());
	//Tendremos que definir los destinatarios de este correo
	ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPLICAUSUARIOS");
	correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
	String mensaje = "A continuacion se informaci�n del proceso de replica de PEDIDOS CENTRALIZADOS " + respuesta + respuestaDetalle;
	correo.setMensaje(mensaje);
	ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
	contro.enviarCorreoHTML();
}


}




