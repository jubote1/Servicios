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
	
	//PROMOCIÓN DE MEDIANAS
	//Creamos el String temporal para las medianas
	String respuestaMediana = "";
	respuestaMediana = respuestaMediana + "<table border='2'> <tr> REPORTE DE VENTA DE PROMOCIONES  MEDIANAS 50%  " + fechaActual + " </tr>";
	respuestaMediana = respuestaMediana + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromoMediana = 0;
	double totalFinalMediana = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorMedianas = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoMediana  = PedidoDAO.obtenerTotalesPromoMediana(strFechaActual, tien.getHostBD());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoMediana > 0)
			{
				indicadorMedianas = true;
			}
			respuestaMediana = respuestaMediana + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoMediana) + "</td></tr>";
			totalFinalMediana = totalFinalMediana + totalPromoMediana;
		}
	}
	respuestaMediana = respuestaMediana + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 20, "C")) + "</td></tr>";
	respuestaMediana = respuestaMediana + "<tr><td> TOTAL TIENDA VIRTUAL PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 20, "T") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 20, "TK")) + "</td></tr>";
	respuestaMediana = respuestaMediana + "<tr><td> TOTAL TIENDA VIRTUAL NUEVA</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
	respuestaMediana = respuestaMediana + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalMediana) + "</td></tr>";
	respuestaMediana = respuestaMediana + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorMedianas)
	{
		respuesta = respuesta + respuestaMediana;
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
	double totalFinalMediana20 = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorMedianas20 = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoMediana20  = PedidoDAO.obtenerTotalesPromoMediana20(strFechaActual, tien.getHostBD());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoMediana20 > 0)
			{
				indicadorMedianas20 = true;
			}
			respuestaMediana20 = respuestaMediana20 + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoMediana20) + "</td></tr>";
			totalFinalMediana20 = totalFinalMediana20 + totalPromoMediana20;
		}
	}
	respuestaMediana20 = respuestaMediana20 + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 27, "C")) + "</td></tr>";
	respuestaMediana20 = respuestaMediana20 + "<tr><td> TOTAL TIENDA VIRTUAL PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 27, "T") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 27, "TK")) + "</td></tr>";
	respuestaMediana20 = respuestaMediana20 + "<tr><td> TOTAL TIENDA VIRTUAL NUEVA</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
	respuestaMediana20 = respuestaMediana20 + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalMediana20) + "</td></tr>";
	respuestaMediana20 = respuestaMediana20 + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorMedianas20)
	{
		respuesta = respuesta + respuestaMediana20;
	}
	
	//PROMOCIÓN DE MEDIANAS 11990
	//Creamos el String temporal para las medianas
	String respuestaMediana12 = "";
	respuestaMediana12 = respuestaMediana12 + "<table border='2'> <tr> REPORTE DE VENTA DE PROMOCIONES  MEDIANAx11990   " + fechaActual + " </tr>";
	respuestaMediana12 = respuestaMediana12 + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromoMediana12 = 0;
	double totalFinalMediana12 = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorMedianas12 = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoMediana12  = PedidoDAO.obtenerTotalesPromoMediana12(strFechaActual, tien.getHostBD());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoMediana12 > 0)
			{
				indicadorMedianas12 = true;
			}
			respuestaMediana12 = respuestaMediana12 + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoMediana12) + "</td></tr>";
			totalFinalMediana12 = totalFinalMediana12 + totalPromoMediana12;
		}
	}
	respuestaMediana12 = respuestaMediana12 + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalMediana12) + "</td></tr>";
	respuestaMediana12 = respuestaMediana12 + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorMedianas12)
	{
		respuesta = respuesta + respuestaMediana12;
	}
	
	
	//PROMOCIÓN DE PIZZETAS
	//Creamos el String temporal para las medianas
	String respuestaPizzeta20 = "";
	respuestaPizzeta20 = respuestaPizzeta20 + "<table border='2'> <tr> REPORTE DE VENTA DE PROMOCIONES  PIZZETAS 2X1   " + fechaActual + " </tr>";
	respuestaPizzeta20 = respuestaPizzeta20 + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromoPizzeta20 = 0;
	double totalFinalPizzeta20 = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorPizzeta20 = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoPizzeta20  = PedidoDAO.obtenerTotalesPromoPizzeta20(strFechaActual, tien.getHostBD());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoPizzeta20 > 0)
			{
				indicadorPizzeta20 = true;
			}
			respuestaPizzeta20 = respuestaPizzeta20 + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoPizzeta20) + "</td></tr>";
			totalFinalPizzeta20 = totalFinalPizzeta20 + totalPromoPizzeta20;
		}
	}
	respuestaPizzeta20 = respuestaPizzeta20 + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 26, "C")) + "</td></tr>";
	respuestaPizzeta20 = respuestaPizzeta20 + "<tr><td> TOTAL TIENDA VIRTUAL PROMO </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 26, "T") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 26, "TK") ) + "</td></tr>";
	respuestaPizzeta20 = respuestaPizzeta20 + "<tr><td> TOTAL TIENDA VIRTUAL NUEVA</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
	respuestaPizzeta20 = respuestaPizzeta20 + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalPizzeta20) + "</td></tr>";
	respuestaPizzeta20 = respuestaPizzeta20 + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorPizzeta20)
	{
		respuesta = respuesta + respuestaPizzeta20;
	}
	
	
	//PROMOCIÓN DE GRANDES
	//Creamos el String temporal para las grandes
	String respuestaGrande = "";
	respuestaGrande =  respuestaGrande + "<table border='2'> <tr> REPORTE DE VENTA DE PROMOCIONES  GRANDE DOMICILIOS.COM  " + fechaActual + " </tr>";
	respuestaGrande =  respuestaGrande + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromoGrande = 0;
	double totalFinalGrande = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorGrandes = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoGrande  = PedidoDAO.obtenerTotalesPromoGrandeDomicilios(strFechaActual, tien.getHostBD());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoGrande > 0)
			{
				indicadorGrandes = true;
			}
			respuestaGrande = respuestaGrande + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoGrande) + "</td></tr>";
			totalFinalGrande = totalFinalGrande + totalPromoGrande;
		} 
	}
	respuestaGrande = respuestaGrande + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalGrande) + "</td></tr>";
	
	respuestaGrande = respuestaGrande + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorGrandes)
	{
		respuesta = respuesta + respuestaGrande;
	}
	
	
	//PROMOCIÓN DE EXTRAGRANDES DEDITOS
	//Creamos el String temporal para las extragrandes
	String respuestaExtra = "";
	respuestaExtra =  respuestaExtra + "<table border='2'> <tr> REPORTE DE VENTA DE PROMOCIONES  EXTRAGRANDE DEDITOS   " + fechaActual + " </tr>";
	respuestaExtra =  respuestaExtra + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromoExtra = 0;
	double totalFinalExtra = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorExtras = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoExtra  = PedidoDAO.obtenerTotalesPromoXLDeditos(strFechaActual, tien.getHostBD());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoExtra > 0)
			{
				indicadorExtras = true;
			}
			respuestaExtra = respuestaExtra + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoExtra) + "</td></tr>";
			totalFinalExtra = totalFinalExtra + totalPromoExtra;
		}
	}
	respuestaExtra = respuestaExtra + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 22, "C")) + "</td></tr>";
	respuestaExtra = respuestaExtra + "<tr><td> TOTAL TIENDA VIRTUAL PROMO</td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 22, "T") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 22, "TK")) + "</td></tr>";
	respuestaExtra = respuestaExtra + "<tr><td> TOTAL TIENDA VIRTUAL NUEVA</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
	respuestaExtra = respuestaExtra + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalExtra) + "</td></tr>";
	respuestaExtra = respuestaExtra + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorExtras)
	{
		respuesta = respuesta + respuestaExtra;
	}
	
	
	//PROMOCIÓN DE EXTRAGRANDES COMBO FAMILIAR
	//Creamos el String temporal para las extragrandes
	String respuestaExtraCompartir = "";
	respuestaExtraCompartir =  respuestaExtraCompartir + "<table border='2'> <tr> REPORTE DE VENTA DE COMBO FAMILIAR   " + fechaActual + " </tr>";
	respuestaExtraCompartir =  respuestaExtraCompartir + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalPromoExtraComp = 0;
	double totalFinalExtraComp = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorExtrasComp = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalPromoExtraComp  = PedidoDAO.obtenerTotalesPromoFamiliar(strFechaActual, tien.getHostBD());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalPromoExtraComp > 0)
			{
				indicadorExtrasComp = true;
			}
			respuestaExtraCompartir = respuestaExtraCompartir + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalPromoExtraComp) + "</td></tr>";
			totalFinalExtraComp = totalFinalExtraComp+ totalPromoExtraComp;
		}
	}
	respuestaExtraCompartir = respuestaExtraCompartir + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 29, "C") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 34, "C")) + "</td></tr>";
	respuestaExtraCompartir = respuestaExtraCompartir + "<tr><td> TOTAL TIENDA VIRTUAL PROMO</td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 34, "TK") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 29, "TK") ) + "</td></tr>";
	respuestaExtraCompartir = respuestaExtraCompartir + "<tr><td> TOTAL TIENDA VIRTUAL NUEVA</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
	respuestaExtraCompartir = respuestaExtraCompartir + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalExtraComp) + "</td></tr>";
	respuestaExtraCompartir = respuestaExtraCompartir + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorExtrasComp)
	{
		respuesta = respuesta + respuestaExtraCompartir;
	}
	
	//PROMOCIÓN COMBO INSEPARABLE
	//Creamos el String temporal para las extragrandes
	String respuestaComboInse = "";
	respuestaComboInse =  respuestaComboInse + "<table border='2'> <tr> REPORTE DE VENTA DE COMBO INSEPARABLE   " + fechaActual + " </tr>";
	respuestaComboInse =  respuestaComboInse + "<tr>"
			+  "<td><strong>Tienda</strong></td>"
			+  "<td><strong>Cant Pizzas</strong></td>"
			+  "</tr>";
	double totalComboInse = 0;
	double totalFinalComboInse = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorComboInse = false;
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			totalComboInse  = PedidoDAO.obtenerTotalesComboInse(strFechaActual, tien.getHostBD());
			//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
			if(totalComboInse > 0)
			{
				indicadorComboInse = true;
			}
			respuestaComboInse = respuestaComboInse + "<tr><td>" +  tien.getNombreTienda() + "</td><td>" + formatea.format(totalComboInse) + "</td></tr>";
			totalFinalComboInse = totalFinalComboInse + totalComboInse;
		}
	}
	respuestaComboInse = respuestaComboInse + "<tr><td> TOTAL CONTACT </td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 30, "C")) + "</td></tr>";
	respuestaComboInse = respuestaComboInse + "<tr><td> TOTAL TIENDA VIRTUAL PROMO</td><td>" + formatea.format(PedidoDAO.obtenerTotalesPromo(strFechaActual, 30, "T") + PedidoDAO.obtenerTotalesPromo(strFechaActual, 30, "TK") ) + "</td></tr>";
	respuestaComboInse = respuestaComboInse + "<tr><td> TOTAL TIENDA VIRTUAL NUEVA</td><td>" + formatea.format(cantidadPedidosTiendaVirtualNueva) + "</td></tr>";
	respuestaComboInse = respuestaComboInse + "<tr><td> TOTAL </td><td>" + formatea.format(totalFinalComboInse) + "</td></tr>";
	respuestaComboInse = respuestaComboInse + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorComboInse)
	{
		respuesta = respuesta + respuestaComboInse;
	}
		
		
	
	//Resumen de promociones de volante físico, se engloban los tamaños de MD, GD y XL.
	//Creamos el String temporal para las extragrandes
	String respuestaVolante = "";
	respuestaVolante =  respuestaVolante + "<table border='2'> <tr> REPORTE DE VENTA DE PROMOCIONES  VOLANTE   " + fechaActual + " </tr>";
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
	
	
	//PROMOCIÓN DE DOMICILIOS.COM
	//Creamos el String temporal para domicilios
	String respuestaDomicilios = "";
	respuestaDomicilios =  respuestaDomicilios + "<table border='2'> <tr> CANTIDAD VENDIDA DE PROMOCIONES DOMICILIOS.COM   " + fechaActual + " </tr>";
	respuestaDomicilios = respuestaDomicilios + "<tr>"
			+  "<td><strong>PROMOCIÓN</strong></td>"
			+  "<td><strong>Cant de usos</strong></td>"
			+  "</tr>";
	double totalDomicilios = 0;
	//Tendremos un indicador para saber si hubo venta de promoción de medianas
	boolean indicadorDomicilios = false;
	totalDomicilios = PedidoDAO.consultarPedidosDomicilios(strFechaActual);
	//Si por lo menos en alguna se tuvo venta se prenderá el indicador de promoción de medianas
	if(totalDomicilios > 0)
	{
		indicadorDomicilios = true;
	}
	respuestaDomicilios = respuestaDomicilios + "<tr><td>PROMOS DOMICILIOS.COM</td><td>" + formatea.format(totalDomicilios) + "</td></tr>";
	
	respuestaDomicilios = respuestaDomicilios + "</table> <br/>";
	
	//Verificamos si el indicador esta prendido para agregar a la respuesta final
	if(indicadorDomicilios)
	{
		respuesta = respuesta + respuestaDomicilios;
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




