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
import CapaDAOSer.PedidoAnuladoDAO;
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
import ModeloSer.PedidoAnulado;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioReplicaPedidos {
	
	
	
	
public static void main(String[] args)
{
	ServicioReplicaPedidos reporteConsumosUsuarios = new ServicioReplicaPedidos();
	reporteConsumosUsuarios.generarReplicaPedidos();
	
}

public void generarReplicaPedidos()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCI�N REPLICA PEDIDOS");
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
		calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
	}catch(Exception e)
	{
		System.out.println(e.toString());
	}
	
	//Llevamos a un string la fecha anterior para el c�lculo de la venta
	fechaActual = calendarioActual.getTime();
	strFechaActual = dateFormat.format(fechaActual);
	
	
	//Generamos String de tiendas exitosas y tiendas no exitosas para mandar correo.
	String respuesta = "";
	String respuestaDetalle = "";
	String respuestaDespReal = "";
	String respuestaDespRealDet = "";
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	//Comenzamos a recorrer una a una las tiendas
	ArrayList<PedidoAll> pedidos = new ArrayList();
	ArrayList<DetallePedidoAll> detallePedidos = new ArrayList();
	ArrayList<DespachoReal> despachosReales = new ArrayList();
	ArrayList<DespachoRealDet> despachosRealesDet = new ArrayList();
	for(Tienda tien : tiendas)
	{
		
		if(!tien.getHostBD().equals(new String("")))
		{
			try
			{
				//Una vez obtenidos los pedidos del d�a en cuesti�n realizaremos la inserci�n en el sistema de Bodega
				pedidos = PedidoDAO.recuperarPedidosPorFecha(strFechaActual, tien.getHostBD());
				PedidoDAO.insertarLotePedidos(pedidos);
				if(pedidos.size() > 0)
				{
					respuesta = respuesta + " <p>" + tien.getNombreTienda() + " EXITOSO - PEDIDOS " +  " </p>";
				}else
				{
					respuesta = respuesta + " <p>" + tien.getNombreTienda() + " CUIDADO SE REPLICO CERO EN PEDIDOS  - PEDIDOS" +  " </p>";
				}
				
			}catch(Exception e)
			{
				respuesta = respuesta + " <p>" + tien.getNombreTienda() + " ERROR - PEDIDOS " +  " </p>";
			}
			
			try
			{
				//Una vez obtenidos los detalles pedidos del d�a en cuesti�n realizaremos la inserci�n en el sistema de Bodega
				detallePedidos = DetallePedidoAllDAO.recuperarDetallePorPedido(strFechaActual, tien.getHostBD());
				DetallePedidoAllDAO.insertarLoteDetallePedido(detallePedidos);
				if(detallePedidos.size() > 0)
				{
					respuestaDetalle = respuestaDetalle + " <p>" + tien.getNombreTienda() + " EXITOSO - DETALLE PEDIDOS " +  " </p>";
				}else
				{
					respuestaDetalle = respuestaDetalle + " <p>" + tien.getNombreTienda() + " CUIDADO SE REPLICO CERO EN PEDIDOS  - DETALLE PEDIDOS" +  " </p>";
				}
				
			}catch(Exception e)
			{
				respuestaDetalle = respuestaDetalle + " <p>" + tien.getNombreTienda() + " ERROR - DETALLE PEDIDOS " +  " </p>";
			}
			
			try
			{
				despachosReales = DespachoRealDAO.obtenerDespachoRealFecha(strFechaActual, tien.getHostBD());
				DespachoRealDAO.insertarDespachoRealLote(despachosReales);
				if(despachosReales.size() > 0)
				{
					respuestaDespReal = respuestaDespReal + " <p>" + tien.getNombreTienda() + " EXITOSO - DESPACHO-REAL " +  " </p>";
				}else
				{
					respuestaDespReal = respuestaDespReal + " <p>" + tien.getNombreTienda() + " CUIDADO SE REPLICO CERO EN DESPACHO-REAL" +  " </p>";
				}
				
			}catch(Exception e)
			{
				respuestaDespReal = respuestaDespReal + " <p>" + tien.getNombreTienda() + " ERROR - DESPACHO-REAL " +  " </p>";
			}
			
			try
			{
				despachosRealesDet = DespachoRealDetDAO.obtenerDespachoRealDetFecha(strFechaActual, tien.getHostBD());
				DespachoRealDetDAO.insertarDespachoRealDetLote(despachosRealesDet, tien.getIdTienda());
				if(despachosRealesDet.size() > 0)
				{
					respuestaDespRealDet = respuestaDespRealDet + " <p>" + tien.getNombreTienda() + " EXITOSO - DESPACHO-REAL-DET " +  " </p>";
				}else
				{
					respuestaDespRealDet = respuestaDespRealDet + " <p>" + tien.getNombreTienda() + " CUIDADO SE REPLICO CERO EN DESPACHO-REAL-DET" +  " </p>";
				}
				
			}catch(Exception e)
			{
				respuestaDespRealDet = respuestaDespRealDet + " <p>" + tien.getNombreTienda() + " ERROR - DESPACHO-REAL-DET " +  " </p>";
			}
			
		}
	}
	
	//Realizamos el env�o del correo electr�nico con los archivos
	Correo correo = new Correo();
	correo.setAsunto("REPLICA DE PEDIDOS EN SISTEMA CENTRALIZADO " + fechaActual.toString());
	CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
	correo.setContrasena(infoCorreo.getClaveCorreo());
	//Tendremos que definir los destinatarios de este correo
	ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPLICAUSUARIOS");
	correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
	String mensaje = "A continuacion se informaci�n del proceso de replica de PEDIDOS CENTRALIZADOS " + respuesta + respuestaDetalle + respuestaDespReal + respuestaDespRealDet;
	correo.setMensaje(mensaje);
	ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
	contro.enviarCorreoHTML();
}


}




