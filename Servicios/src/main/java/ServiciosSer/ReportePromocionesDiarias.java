package ServiciosSer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
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

import CapaDAOSer.EstadisticaPromocionDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ItemInventarioDAO;
import CapaDAOSer.OfertaClienteDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.ReporteContactCenterDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.EstadisticaPromocion;
import ModeloSer.Insumo;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import utilidadesSer.ControladorEnvioCorreo;

/**
 * Proceso que se encargará diariamente de extraer la información de las ventas de promociones de una forma que tendrá que conectarse
 * a cada tienda y extraer la información
 * @author juanb
 *
 */
public class ReportePromocionesDiarias {
	
	
	
/**
 * Este programa se encargará de correr como un servicio todos los días a las 12:50 am, con el fin de revisar
 * si los sistemas se encuentran cerrados y enviar un mensaje al correo con la revisión.
 * @param args
 */
public static void main(String[] args)
{
	ReportePromocionesDiarias reportePromoDiarias = new ReportePromocionesDiarias();
	reportePromoDiarias.generarInfoPromociones();
	
}

public void generarInfoPromociones()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCIÓN");
	//Generamos la fecha en la que corre el proceso
	Date fechaActual = new Date();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//Formateamos la fecha Actual para consulta
	String strFechaActual = dateFormat.format(fechaActual);
	//String strFechaActual = "2020-08-10";
	//Vamos a recuperar el día anterior que según esto es el día real de trabajo
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	DecimalFormat formatea = new DecimalFormat("###,###");
	String respuesta = "";
	int cantidadPedidosTiendaVirtualNueva = ReporteContactCenterDAO.obtenerPedidosVirtualNuevaTotalDia(strFechaActual);
	int cantidadPedidosAPP = ReporteContactCenterDAO.obtenerPedidosAPP(strFechaActual);
	EstadisticaPromocion est = new EstadisticaPromocion("",0,0,0,0,0);
	//PROMOCIÓN DE MEDIANAS
	//Creamos el String temporal para las medianas
	String respuestaMediana = "";
	respuestaMediana = respuestaMediana + "<table border='2'> <tr> REPORTE DE VENTA DE PROMOCIONES  Combo 2 Medianas  " + fechaActual + " </tr>";
	respuestaMediana = respuestaMediana + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromoMediana = 0;
	double totalPromoMedianaContact = 0;
	double totalPromoMedianaTV = 0;
	double totalFinalMediana = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorMedianas = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoMediana  = PedidoDAO.obtenerTotalesPromoMediana(strFechaActual, tien.getHostBD());
			totalPromoMedianaContact = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 20, "C", tien.getIdTienda());
			totalPromoMedianaTV = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 20, "TK", tien.getIdTienda());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoMediana > 0)
			{
				indicadorMedianas = true;
				est = new EstadisticaPromocion(strFechaActual, tien.getIdTienda(),20,(int)totalPromoMedianaContact,(int)totalPromoMedianaTV,(int)totalPromoMediana);
				EstadisticaPromocionDAO.insertarEstadisticaPromocion(est, false);
			}
			respuestaMediana = respuestaMediana + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoMediana) + "</td></tr>";
			totalFinalMediana = totalFinalMediana + totalPromoMediana;
		}
	}
	respuestaMediana = respuestaMediana + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 20, "C")) + "</td></tr>";
	respuestaMediana = respuestaMediana + "<tr><td> TOTAL TIENDA VIRTUAL PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 20, "T") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 20, "TK")) + "</td></tr>";
	respuestaMediana = respuestaMediana + "<tr><td> TOTAL APP PROMO </td><td>" + PedidoDAO.obtenerTotalesPromo(strFechaActual, 20, "APP") + "</td></tr>";
	respuestaMediana = respuestaMediana + "<tr><td> TOTAL TIENDA VIRTUAL</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
	respuestaMediana = respuestaMediana + "<tr><td> TOTAL APP</td><td>" + formatea.format(cantidadPedidosAPP) + "</td></tr>";
	respuestaMediana = respuestaMediana + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalMediana) + "</td></tr>";
	respuestaMediana = respuestaMediana + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorMedianas)
	{
		respuesta = respuesta + respuestaMediana;
	}
	
	//PROMOCIÓN DE EL MEJOR COMBO 19900
	String respuestaMejorCombo = "";
	respuestaMejorCombo = respuestaMejorCombo + "<table border='2'> <tr> REPORTE DE VENTA DE PROMOCIONES COMBO DELI GRANDE   " + fechaActual + " </tr>";
	respuestaMejorCombo = respuestaMejorCombo + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromoMejorCombo = 0;
	double totalPromoMejorComboContact = 0;
	double totalPromoMejorComboTV = 0;
	double totalFinalMejorCombo = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorMejorCombo = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoMejorCombo  = PedidoDAO.obtenerTotalesPromoMejorCombo(strFechaActual, tien.getHostBD());
			totalPromoMejorComboContact = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 24, "C", tien.getIdTienda());
			totalPromoMejorComboTV = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 24, "TK", tien.getIdTienda());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoMejorCombo > 0)
			{
				indicadorMejorCombo = true;
				est = new EstadisticaPromocion(strFechaActual, tien.getIdTienda(),24,(int)totalPromoMejorComboContact,(int)totalPromoMejorComboTV,(int)totalPromoMejorCombo);
				EstadisticaPromocionDAO.insertarEstadisticaPromocion(est, false);

			}
			respuestaMejorCombo = respuestaMejorCombo + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoMejorCombo) + "</td></tr>";
			totalFinalMejorCombo = totalFinalMejorCombo + totalPromoMejorCombo;
		}
	}
	respuestaMejorCombo = respuestaMejorCombo + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 24, "C")) + "</td></tr>";
	respuestaMejorCombo = respuestaMejorCombo + "<tr><td> TOTAL TIENDA VIRTUAL PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 24, "T") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 24, "TK")) + "</td></tr>";
	respuestaMejorCombo = respuestaMejorCombo + "<tr><td> TOTAL APP PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 24, "APP")) + "</td></tr>";
	respuestaMejorCombo = respuestaMejorCombo + "<tr><td> TOTAL TIENDA VIRTUAL</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
	respuestaMejorCombo = respuestaMejorCombo + "<tr><td> TOTAL APP</td><td>" + formatea.format(cantidadPedidosAPP) + "</td></tr>";
	respuestaMejorCombo = respuestaMejorCombo + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalMejorCombo) + "</td></tr>";
	respuestaMejorCombo = respuestaMejorCombo + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorMejorCombo)
	{
		respuesta = respuesta + respuestaMejorCombo;
	}
	
	
		//PROMOCIÓN DE EL CODIGO FLASH
		String respuestaCodigoFlash = "";
		respuestaCodigoFlash = respuestaCodigoFlash + "<table border='2'> <tr> REPORTE DE VENTA DE CODIGO FLASH   " + fechaActual + " </tr>";
		respuestaCodigoFlash = respuestaCodigoFlash + "<tr>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>Cant Pizzas</strong></td>"
				+  "</tr>";
		double totalPromoCodigoFlash = 0;
		double totalPromoCodigoFlashContact = 0;
		double totalPromoCodigoFlashTV = 0;
		double totalFinalCodigoFlash = 0;
		//Tendremos un indicador para saber si hubo venta de promoción de medianas
		boolean indicadorCodigoFlash = false;
		for(Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				totalPromoCodigoFlash  = PedidoDAO.obtenerTotalesCodigoFlash(strFechaActual, tien.getHostBD());
				totalPromoCodigoFlashContact = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 40, "C", tien.getIdTienda());
				totalPromoCodigoFlashTV = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 40, "TK", tien.getIdTienda());
				//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
				if(totalPromoCodigoFlash > 0)
				{
					indicadorCodigoFlash = true;
					est = new EstadisticaPromocion(strFechaActual, tien.getIdTienda(),40,(int)totalPromoCodigoFlashContact,(int)totalPromoCodigoFlashTV,(int)totalPromoCodigoFlash);
					EstadisticaPromocionDAO.insertarEstadisticaPromocion(est, false);
				}
				respuestaCodigoFlash = respuestaCodigoFlash + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoCodigoFlash) + "</td></tr>";
				totalFinalCodigoFlash = totalFinalCodigoFlash + totalPromoCodigoFlash;
			}
		}
		respuestaCodigoFlash = respuestaCodigoFlash + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 40, "C")) + "</td></tr>";
		respuestaCodigoFlash = respuestaCodigoFlash + "<tr><td> TOTAL TIENDA VIRTUAL PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 40, "T") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 40, "TK")) + "</td></tr>";
		respuestaCodigoFlash = respuestaCodigoFlash + "<tr><td> TOTAL APP PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 40, "APP")) + "</td></tr>";
		respuestaCodigoFlash = respuestaCodigoFlash + "<tr><td> TOTAL TIENDA VIRTUAL</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
		respuestaCodigoFlash = respuestaCodigoFlash + "<tr><td> TOTAL APP</td><td>" + formatea.format(cantidadPedidosAPP) + "</td></tr>";
		respuestaCodigoFlash = respuestaCodigoFlash + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalCodigoFlash) + "</td></tr>";
		respuestaCodigoFlash = respuestaCodigoFlash + "</table> <br/>";
		
		//Verificamos si el indicador esta prendido para agregar a la respuesta final
		if(indicadorCodigoFlash)
		{
			respuesta = respuesta + respuestaCodigoFlash;
		}
	
	
	//PROMOCIÓN DE MD Promo mediana plus
		String respuestaMDPizzaton = "";
		respuestaMDPizzaton = respuestaMDPizzaton + "<table border='2'> <tr> REPORTE DE VENTA DE Promo mediana plus   " + fechaActual + " </tr>";
		respuestaMDPizzaton = respuestaMDPizzaton + "<tr>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>Cant Pizzas</strong></td>"
				+  "</tr>";
		double totalPromoMDPizzaton = 0;
		double totalPromoMDPizzatonContact = 0;
		double totalPromoMDPizzatonTV = 0;
		double totalFinalMDPizzaton = 0;
		//Tendremos un indicador para saber si hubo venta de promoción de medianas
		boolean indicadorMDPizzaton = false;
		for(Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				totalPromoMDPizzaton  = PedidoDAO.obtenerTotalesPromoMDPizzaton(strFechaActual, tien.getHostBD());
				totalPromoMDPizzatonContact = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 41, "C", tien.getIdTienda());
				totalPromoMDPizzatonTV = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 41, "TK", tien.getIdTienda());
				//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
				if(totalPromoMDPizzaton > 0)
				{
					indicadorMDPizzaton = true;
					est = new EstadisticaPromocion(strFechaActual, tien.getIdTienda(),41,(int)totalPromoMDPizzatonContact,(int)totalPromoMDPizzatonTV,(int)totalPromoMDPizzaton);
					EstadisticaPromocionDAO.insertarEstadisticaPromocion(est, false);
				}
				respuestaMDPizzaton = respuestaMDPizzaton + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoMDPizzaton) + "</td></tr>";
				totalFinalMDPizzaton = totalFinalMDPizzaton + totalPromoMDPizzaton;
			}
		}
		respuestaMDPizzaton = respuestaMDPizzaton + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 41, "C")) + "</td></tr>";
		respuestaMDPizzaton = respuestaMDPizzaton + "<tr><td> TOTAL TIENDA VIRTUAL PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 41, "T") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 41, "TK")) + "</td></tr>";
		respuestaMDPizzaton = respuestaMDPizzaton + "<tr><td> TOTAL APP PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 41, "APP")) + "</td></tr>";
		respuestaMDPizzaton = respuestaMDPizzaton + "<tr><td> TOTAL TIENDA VIRTUAL</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
		respuestaMDPizzaton = respuestaMDPizzaton + "<tr><td> TOTAL APP</td><td>" + formatea.format(cantidadPedidosAPP) + "</td></tr>";
		respuestaMDPizzaton = respuestaMDPizzaton + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalMDPizzaton) + "</td></tr>";
		respuestaMDPizzaton = respuestaMDPizzaton + "</table> <br/>";
		
		//Verificamos si el indicador esta prendido para agregar a la respuesta final
		if(indicadorMDPizzaton)
		{
			respuesta = respuesta + respuestaMDPizzaton;
		}
	
	
	//PROMOCIÓN DE MEDIANAS 19990
	//Creamos el String temporal para las medianas
	String respuestaMediana20 = "";
	respuestaMediana20 = respuestaMediana20 + "<table border='2'> <tr> REPORTE DE VENTA DE PROMOCIONES  MEDIANAx19990   " + fechaActual + " </tr>";
	respuestaMediana20 = respuestaMediana20 + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromoMediana20 = 0;
	double totalPromoMediana20Contact = 0;
	double totalPromoMediana20TV = 0;
	double totalFinalMediana20 = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorMedianas20 = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoMediana20  = PedidoDAO.obtenerTotalesPromoMediana20(strFechaActual, tien.getHostBD());
			totalPromoMediana20Contact = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 27, "C", tien.getIdTienda());
			totalPromoMediana20TV = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 27, "TK", tien.getIdTienda());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoMediana20 > 0)
			{
				indicadorMedianas20 = true;
				est = new EstadisticaPromocion(strFechaActual, tien.getIdTienda(),27,(int)totalPromoMediana20Contact,(int)totalPromoMediana20TV,(int)totalPromoMediana20);
				EstadisticaPromocionDAO.insertarEstadisticaPromocion(est, false);
			}
			respuestaMediana20 = respuestaMediana20 + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoMediana20) + "</td></tr>";
			totalFinalMediana20 = totalFinalMediana20 + totalPromoMediana20;
		}
	}
	respuestaMediana20 = respuestaMediana20 + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 27, "C")) + "</td></tr>";
	respuestaMediana20 = respuestaMediana20 + "<tr><td> TOTAL TIENDA VIRTUAL PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 27, "T") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 27, "TK")) + "</td></tr>";
	respuestaMediana20 = respuestaMediana20 + "<tr><td> TOTAL APP PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 27, "APP")) + "</td></tr>";
	respuestaMediana20 = respuestaMediana20 + "<tr><td> TOTAL TIENDA VIRTUAL</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
	respuestaMediana20 = respuestaMediana20 + "<tr><td> TOTAL APP</td><td>" + formatea.format(cantidadPedidosAPP) + "</td></tr>";
	respuestaMediana20 = respuestaMediana20 + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalMediana20) + "</td></tr>";
	respuestaMediana20 = respuestaMediana20 + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorMedianas20)
	{
		respuesta = respuesta + respuestaMediana20;
	}
	
	//PROMOCIÓN DE MEDIANAS 14900
	//Creamos el String temporal para las medianas
	String respuestaMediana14 = "";
	respuestaMediana14 = respuestaMediana14 + "<table border='2'> <tr> REPORTE DE VENTA DE PROMOCIONES  MEDIANAx 19.900   " + fechaActual + " </tr>";
	respuestaMediana14 = respuestaMediana14 + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromoMediana14 = 0;
	double totalPromoMediana14Contact = 0;
	double totalPromoMediana14TV = 0;
	double totalFinalMediana14 = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorMedianas14 = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoMediana14  = PedidoDAO.obtenerTotalesPromoMediana19(strFechaActual, tien.getHostBD());
			totalPromoMediana14Contact = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 11, "C", tien.getIdTienda());
			totalPromoMediana14TV = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 11, "TK", tien.getIdTienda());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoMediana14 > 0)
			{
				indicadorMedianas14 = true;
				est = new EstadisticaPromocion(strFechaActual, tien.getIdTienda(),11,(int)totalPromoMediana14Contact,(int)totalPromoMediana14TV,(int)totalPromoMediana14);
				EstadisticaPromocionDAO.insertarEstadisticaPromocion(est, false);
			}
			respuestaMediana14 = respuestaMediana14 + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoMediana14) + "</td></tr>";
			totalFinalMediana14 = totalFinalMediana14 + totalPromoMediana14;
		}
	}
	respuestaMediana14 = respuestaMediana14 + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 11, "C")) + "</td></tr>";
	respuestaMediana14 = respuestaMediana14 + "<tr><td> TOTAL TIENDA VIRTUAL PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 11, "T") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 11, "TK")) + "</td></tr>";
	respuestaMediana14 = respuestaMediana14 + "<tr><td> TOTAL APP PROMO </td><td>" + formatea.format( PedidoDAO.obtenerTotalesPromo(strFechaActual, 11, "APP")) + "</td></tr>";
	respuestaMediana14 = respuestaMediana14 + "<tr><td> TOTAL TIENDA VIRTUAL</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
	respuestaMediana14 = respuestaMediana14 + "<tr><td> TOTAL APP</td><td>" + formatea.format(cantidadPedidosAPP) + "</td></tr>";
	respuestaMediana14 = respuestaMediana14 + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalMediana14) + "</td></tr>";
	respuestaMediana14 = respuestaMediana14 + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorMedianas14)
	{
		respuesta = respuesta + respuestaMediana14;
	}
	
	
	//PROMOCIÓN DE PIZZETAS
	//Creamos el String temporal para las medianas
	String respuestaPizzeta20 = "";
	respuestaPizzeta20 = respuestaPizzeta20 + "<table border='2'> <tr> REPORTE DE VENTA DE PROMOCIONES  PIZZETAS 2X19.990   " + fechaActual + " </tr>";
	respuestaPizzeta20 = respuestaPizzeta20 + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromoPizzeta20 = 0;
	double totalPromoPizzeta20Contact = 0;
	double totalPromoPizzeta20TV = 0;
	double totalFinalPizzeta20 = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorPizzeta20 = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoPizzeta20  = PedidoDAO.obtenerTotalesPromoPizzeta20(strFechaActual, tien.getHostBD());
			totalPromoPizzeta20Contact = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 26, "C", tien.getIdTienda());
			totalPromoPizzeta20TV = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 26, "TK", tien.getIdTienda());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoPizzeta20 > 0)
			{
				indicadorPizzeta20 = true;
				est = new EstadisticaPromocion(strFechaActual, tien.getIdTienda(),26,(int)totalPromoPizzeta20Contact,(int)totalPromoPizzeta20TV,(int)totalPromoPizzeta20);
				EstadisticaPromocionDAO.insertarEstadisticaPromocion(est, false);
			}
			respuestaPizzeta20 = respuestaPizzeta20 + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoPizzeta20) + "</td></tr>";
			totalFinalPizzeta20 = totalFinalPizzeta20 + totalPromoPizzeta20;
		}
	}
	respuestaPizzeta20 = respuestaPizzeta20 + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 26, "C")) + "</td></tr>";
	respuestaPizzeta20 = respuestaPizzeta20 + "<tr><td> TOTAL TIENDA VIRTUAL PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 26, "T") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 26, "TK") ) + "</td></tr>";
	respuestaPizzeta20 = respuestaPizzeta20 + "<tr><td> TOTAL APP PROMO </td><td>" + formatea.format( PedidoDAO.obtenerTotalesPromo(strFechaActual, 26, "APP") ) + "</td></tr>";
	respuestaPizzeta20 = respuestaPizzeta20 + "<tr><td> TOTAL TIENDA VIRTUAL </td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
	respuestaPizzeta20 = respuestaPizzeta20 + "<tr><td> TOTAL APP</td><td>" + formatea.format(cantidadPedidosAPP) + "</td></tr>";
	respuestaPizzeta20 = respuestaPizzeta20 + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalPizzeta20) + "</td></tr>";
	respuestaPizzeta20 = respuestaPizzeta20 + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorPizzeta20)
	{
		respuesta = respuesta + respuestaPizzeta20;
	}
	
	//PROMOCIÓN DE EXTRAGRANDES COMBO PARA TODOS
	//Creamos el String temporal para las extragrandes
	String respuestaExtraCompartir = "";
	respuestaExtraCompartir =  respuestaExtraCompartir + "<table border='2'> <tr> REPORTE DE VENTA DE COMBO PARA TODOS   " + fechaActual + " </tr>";
	respuestaExtraCompartir =  respuestaExtraCompartir + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromoExtraComp = 0;
	double totalPromoExtraCompContact = 0;
	double totalPromoExtraCompTV = 0;
	double totalFinalExtraComp = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorExtrasComp = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoExtraComp  = PedidoDAO.obtenerTotalesPromoFamiliar(strFechaActual, tien.getHostBD());
			totalPromoExtraCompContact = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 29, "C", tien.getIdTienda()) + PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 34, "C", tien.getIdTienda());
			totalPromoExtraCompTV = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 29, "TK", tien.getIdTienda()) + PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 34, "TK", tien.getIdTienda());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoExtraComp > 0)
			{
				indicadorExtrasComp = true;
				est = new EstadisticaPromocion(strFechaActual, tien.getIdTienda(),29,(int)totalPromoExtraCompContact,(int)totalPromoExtraCompTV,(int)totalPromoExtraComp);
				EstadisticaPromocionDAO.insertarEstadisticaPromocion(est, false);
			}
			respuestaExtraCompartir = respuestaExtraCompartir + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoExtraComp) + "</td></tr>";
			totalFinalExtraComp = totalFinalExtraComp+ totalPromoExtraComp;
		}
	}
	respuestaExtraCompartir = respuestaExtraCompartir + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 29, "C") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 34, "C")) + "</td></tr>";
	respuestaExtraCompartir = respuestaExtraCompartir + "<tr><td> TOTAL TIENDA VIRTUAL PROMO</td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 34, "TK") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 29, "TK") ) + "</td></tr>";
	respuestaExtraCompartir = respuestaExtraCompartir + "<tr><td> TOTAL APP PROMO</td><td>" + formatea.format( PedidoDAO.obtenerTotalesPromo(strFechaActual, 29, "APP") ) + "</td></tr>";
	respuestaExtraCompartir = respuestaExtraCompartir + "<tr><td> TOTAL TIENDA VIRTUAL</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
	respuestaExtraCompartir = respuestaExtraCompartir + "<tr><td> TOTAL APP</td><td>" + formatea.format(cantidadPedidosAPP) + "</td></tr>";
	respuestaExtraCompartir = respuestaExtraCompartir + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalExtraComp) + "</td></tr>";
	respuestaExtraCompartir = respuestaExtraCompartir + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorExtrasComp)
	{
		respuesta = respuesta + respuestaExtraCompartir;
	}
	
	
	//PROMOCIÓN DE EXTRAGRANDES SALVA UNA VIDA
	//Creamos el String temporal para las extragrandes
	String respuesta40K = "";
	respuesta40K =  respuesta40K + "<table border='2'> <tr> REPORTE DE VENTA DE COMBO SALVA UNA VIDA  " + fechaActual + " </tr>";
	respuesta40K =  respuesta40K + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromo40K = 0;
	double totalPromo40KContact = 0;
	double totalPromo40KTV = 0;
	double totalFinal40K = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicador40K = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromo40K  = PedidoDAO.obtenerTotales40K(strFechaActual, tien.getHostBD());
			totalPromo40KContact = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 39, "C", tien.getIdTienda());
			totalPromo40KTV = PedidoDAO.obtenerTotalesPromoTienda(strFechaActual, 39, "TK", tien.getIdTienda());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromo40K > 0)
			{
				indicador40K = true;
				est = new EstadisticaPromocion(strFechaActual, tien.getIdTienda(),39,(int)totalPromo40KContact,(int)totalPromo40KTV,(int)totalPromo40K);
				EstadisticaPromocionDAO.insertarEstadisticaPromocion(est, false);
			}
			respuesta40K = respuesta40K + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromo40K) + "</td></tr>";
			totalFinal40K = totalFinal40K+ totalPromo40K;
		}
	}
	respuesta40K = respuesta40K + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 39, "C")) + "</td></tr>";
	respuesta40K = respuesta40K + "<tr><td> TOTAL TIENDA VIRTUAL PROMO</td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 39, "TK")) + "</td></tr>";
	respuesta40K = respuesta40K + "<tr><td> TOTAL APP PROMO</td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 39, "APP")) + "</td></tr>";
	respuesta40K = respuesta40K + "<tr><td> TOTAL TIENDA VIRTUAL</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
	respuesta40K = respuesta40K + "<tr><td> TOTAL APP</td><td>" + formatea.format(cantidadPedidosAPP) + "</td></tr>";
	respuesta40K = respuesta40K + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinal40K) + "</td></tr>";
	respuesta40K = respuesta40K + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicador40K)
	{
		respuesta = respuesta + respuesta40K;
	}
	
	//Resumen de promociones de volante físico, se engloban los tamaños de MD, GD y XL.
	//Creamos el String temporal para las extragrandes
	String respuestaVolante = "";
	respuestaVolante =  respuestaVolante + "<table border='2'> <tr> REPORTE DE VENTA DE PROMOCIONES MEDIANA  VOLANTE   " + fechaActual + " </tr>";
	respuestaVolante =  respuestaVolante + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromoVola = 0;
	double totalFinalVola = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorVolantes = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoVola  = PedidoDAO.obtenerTotalesPromoVolante(strFechaActual, tien.getHostBD());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoVola > 0)
			{
				indicadorVolantes = true;
				est = new EstadisticaPromocion(strFechaActual, tien.getIdTienda(),42,0,0,(int)totalPromoVola);
				EstadisticaPromocionDAO.insertarEstadisticaPromocion(est, false);
			}
			respuestaVolante = respuestaVolante + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoVola) + "</td></tr>";
			totalFinalVola = totalFinalVola + totalPromoVola;
		}
	}
	respuestaVolante = respuestaVolante + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalVola) + "</td></tr>";
	respuestaVolante = respuestaVolante + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorVolantes)
	{
		respuesta = respuesta + respuestaVolante;
	}
	
	
	
	//Resumen de promociones de Rappi, se engloban los tamaños de MD, GD y XL.
		String respuestaRappi = "";
		respuestaRappi =  respuestaRappi + "<table border='2'> <tr> REPORTE DE VENTA DE PROMOCIONES  RAPPI   " + fechaActual + " </tr>";
		respuestaRappi =  respuestaRappi + "<tr>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>Cant Pizzas</strong></td>"
				+  "</tr>";
		double totalPromoRappi = 0;
		double totalFinalRappi = 0;
		//Tendremos un indicador para saber si hubo venta de promoción de medianas
		boolean indicadorRappi = false;
		for(Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				totalPromoRappi  = PedidoDAO.obtenerTotalesPromoRappi(strFechaActual, tien.getHostBD());
				//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
				if(totalPromoRappi > 0)
				{
					indicadorRappi = true;
				}
				respuestaRappi = respuestaRappi + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoRappi) + "</td></tr>";
				totalFinalRappi = totalFinalRappi + totalPromoRappi;
			}
		}
		respuestaRappi = respuestaRappi + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalRappi) + "</td></tr>";
		respuestaRappi = respuestaRappi + "</table> <br/>";
		
		//Verificamos si el indicador esta prendido para agregar a la respuesta final
		if(indicadorRappi)
		{
			respuesta = respuesta + respuestaRappi;
		}
	
	
	//Resumen de los códigos promocionales enviados por Tienda
	String respuestaCodigosEnviados = "";
	respuestaCodigosEnviados =  respuestaCodigosEnviados + "<table border='2'> <tr> CODIGOS PROMOCIONALES ENVIADOS   " + fechaActual + " </tr>";
	respuestaCodigosEnviados = respuestaCodigosEnviados + "<tr>"
			+  "<td><strong>TIENDA</strong></td>"
			+  "<td><strong>CANT CÓDIGOS ENVIADOS</strong></td>"
			+  "</tr>";
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorPromocionalEnv = false;
	ArrayList<String[]> codigosPorTiendaEnv  = OfertaClienteDAO.consultarCodigosPromocionalesEnviados(strFechaActual);
	//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
	if(codigosPorTiendaEnv.size() > 0)
	{
		indicadorPromocionalEnv = true;
	}
	
	for(int z = 0; z < codigosPorTiendaEnv.size(); z++)
	{
		String[] filaTemp = codigosPorTiendaEnv.get(z);
		respuestaCodigosEnviados = respuestaCodigosEnviados + "<tr><td>" + filaTemp[0] + "</td><td>" + filaTemp[1]  + "</td></tr>";
	}
	respuestaCodigosEnviados = respuestaCodigosEnviados + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorPromocionalEnv)
	{
		respuesta = respuesta + respuestaCodigosEnviados;
	}
	
	
	//USO DE CÓDIGOS PROMOCIONALES
	//Creamos el String temporal para códigos promocionales
	String respuestaCodigos = "";
	respuestaCodigos =  respuestaCodigos + "<table border='2'> <tr> REPORTE USO DE CÓDIGOS PROMOCIONALES   " + fechaActual + " </tr>";
	respuestaCodigos = respuestaCodigos + "<tr>"
			+  "<td><strong>Código Promocional</strong></td>"
			+  "<td><strong>Cant de usos</strong></td>"
			+  "<td><strong>TIENDA</strong></td>"
			+  "</tr>";
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorPromocional = false;
	ArrayList<String[]> codigosPorTienda  = PedidoDAO.consultarUsoCodigosPromocionales(strFechaActual);
	//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
	if(codigosPorTienda.size() > 0)
	{
		indicadorPromocional = true;
	}
	
	for(int z = 0; z < codigosPorTienda.size(); z++)
	{
		String[] filaTemp = codigosPorTienda.get(z);
		respuestaCodigos = respuestaCodigos + "<tr><td>Códigos redimidos</td><td>" + filaTemp[0] + "</td><td>" + filaTemp[1]  + "</td></tr>";
	}
	respuestaCodigos = respuestaCodigos + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorPromocional)
	{
		respuesta = respuesta + respuestaCodigos;
	}
	
	
	//Reporte de estofadas
	//PROMOCIÓN DE EL MEJOR COMBO 19900
		String respuestaEstofada= "";
		respuestaEstofada = respuestaEstofada + "<table border='2'> <tr> REPORTE DE VENTA DE ESTOFADAS  " + fechaActual + " </tr>";
		respuestaEstofada = respuestaEstofada + "<tr>"
				+  "<td><strong>Tienda</strong></td>"
				+  "<td><strong>Cant Pizzas</strong></td>"
				+  "</tr>";
		double totalEstofadas= 0;
		double totalEstofadasContact = 0;
		double totalEstofadasTV = 0;
		double totalFinalEstofadas = 0;
		//Tendremos un indicador para saber si hubo venta de promoción de medianas
		boolean indicadorEstofada = false;
		for(Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				totalEstofadas  = PedidoDAO.obtenerTotalesEstofada(strFechaActual, tien.getHostBD());
				totalEstofadasContact = PedidoDAO.obtenerTotalesProductoTienda(strFechaActual, 311,312, "C", tien.getIdTienda());
				totalEstofadasTV = PedidoDAO.obtenerTotalesProductoTienda(strFechaActual, 311, 312, "TK", tien.getIdTienda());
				//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
				if(totalEstofadas > 0)
				{
					indicadorEstofada = true;
				}
				respuestaEstofada = respuestaEstofada + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalEstofadas) + "</td></tr>";
				totalFinalEstofadas = totalFinalEstofadas + totalEstofadas;
			}
		}
//		respuestaEstofada = respuestaEstofada + "<tr><td> TOTAL CONTACT PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 24, "C")) + "</td></tr>";
//		respuestaEstofada = respuestaEstofada + "<tr><td> TOTAL TIENDA VIRTUAL PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 24, "T") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 24, "TK")) + "</td></tr>";
//		respuestaEstofada = respuestaEstofada + "<tr><td> TOTAL APP PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 24, "APP")) + "</td></tr>";
		respuestaEstofada = respuestaEstofada + "<tr><td> TOTAL TIENDA VIRTUAL</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
		respuestaEstofada = respuestaEstofada + "<tr><td> TOTAL APP</td><td>" + formatea.format(cantidadPedidosAPP) + "</td></tr>";
		respuestaEstofada = respuestaEstofada + "<tr><td> TOTAL ESTOFADAS </td><td>" + formatea.format(totalFinalEstofadas) + "</td></tr>";
		respuestaEstofada = respuestaEstofada + "</table> <br/>";
		
		//Verificamos si el indicador esta prendido para agregar a la respuesta final
		if(indicadorEstofada)
		{
			respuesta = respuesta + respuestaEstofada;
		}
	
	
	//PROMOCIÓN DE DIRECTORIO
	//Creamos el String temporal para domicilios
	String respuestaDirectorio = "";
	respuestaDirectorio =  respuestaDirectorio + "<table border='2'> <tr> CANTIDAD VENDIDA DE PROMOCIONES DIRECTORIO PUBLICAR   " + fechaActual + " </tr>";
	respuestaDirectorio = respuestaDirectorio + "<tr>"
			+  "<td><strong>PROMOCIÓN</strong></td>"
			+  "<td><strong>Cant de usos</strong></td>"
			+  "</tr>";
	//OJO double totalDirectorio = 0;
	double totalDirectorio = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	//OJO boolean indicadorDomicilios = false;
	boolean indicadorDirectorio = false;
	totalDirectorio = PedidoDAO.consultarPedidosDirectorioPublicar(strFechaActual);
	//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
	if(totalDirectorio > 0)
	{
		indicadorDirectorio = true;
	}
	respuestaDirectorio = respuestaDirectorio + "<tr><td>PROMOS DIRECTORIO TELEFÓNICO</td><td>" + formatea.format(totalDirectorio) + "</td></tr>";
	
	respuestaDirectorio = respuestaDirectorio + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorDirectorio)
	{
		respuesta = respuesta + respuestaDirectorio;
	}
	
	//Agregaremos la información de las promociones vendidas por tienda virtual y contact center
	int cantVirtualTienda = 0;
	int cantNoFisicosTienda = 0;
	double porcVirtualTienda = 0;
	respuesta = respuesta + "<table border='2'> <tr> ANÁLISIS DE VENTA POR CANALES NO FÍSICOS  " + fechaActual + " </tr>";
	respuesta = respuesta + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Ped Tienda Virtual</strong></td>"
			+  "<td><strong>Ped Canales No Físicos</strong></td>"
			+  "<td><strong>% Tienda Virtual</strong></td>"
			+  "</tr>";
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			cantVirtualTienda = ReporteContactCenterDAO.obtenerPedidosVirtualTotalDiaTienda(strFechaActual, tien.getIdTienda());
			cantNoFisicosTienda = ReporteContactCenterDAO.obtenerPedidosNoFisicosTotalDiaTienda(strFechaActual, tien.getIdTienda());
			porcVirtualTienda = ((double)cantVirtualTienda/(double)cantNoFisicosTienda)*100;
			respuesta = respuesta + "<tr><td>" + tien.getNombreTienda() + "</td><td>" + cantVirtualTienda + "</td><td>" + cantNoFisicosTienda + "</td><td>" + formatea.format(porcVirtualTienda)  + "</td></tr>";
		}
	}
	respuesta = respuesta + "</table> <br/>";
	
	//Realizamos inclusión de información DEDITOS LOCOS SAN ANTONIO
	String respuestaDeditosLocos = "";
	respuestaDeditosLocos =  respuestaDeditosLocos + "<table border='2'> <tr> REPORTE DE VENTA DE PROMOCION DEDITOS LOCOS   " + fechaActual + " </tr>";
	respuestaDeditosLocos =  respuestaDeditosLocos + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromoDeditosLocos = 0;
	double totalFinalDeditosLocos = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorDeditosLocos = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoDeditosLocos  = PedidoDAO.obtenerTotalesPromoDeditosLocos(strFechaActual, tien.getHostBD());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoDeditosLocos > 0)
			{
				indicadorDeditosLocos = true;
			}
			respuestaDeditosLocos = respuestaDeditosLocos + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoDeditosLocos) + "</td></tr>";
			totalFinalDeditosLocos = totalFinalDeditosLocos + totalPromoDeditosLocos;
		}
	}
	respuestaDeditosLocos = respuestaDeditosLocos + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalDeditosLocos) + "</td></tr>";
	respuestaDeditosLocos = respuestaDeditosLocos + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorDeditosLocos)
	{
		respuesta = respuesta + respuestaDeditosLocos;
	}
	
	
	//Al finalizar verificamos si hay información para enviar
	if(respuesta.trim().equals(new String("")))
	{
		
	}else
	{
		//Realizamos el envío del correo electrónico con los archivos
		Correo correo = new Correo();
		correo.setAsunto("REPORTE DIARIO DE PROMOCIONES " + strFechaActual);
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		//Tendremos que definir los destinatarios de este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("PROMODIARIA");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		String mensaje = "A continuación la información del movimiento de las promociones en el día en particular  " + respuesta ;
		correo.setMensaje(mensaje);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}
	
}


}




