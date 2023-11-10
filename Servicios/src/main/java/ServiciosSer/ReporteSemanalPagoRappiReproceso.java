package ServiciosSer;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import CapaDAOSer.GastoSemanalDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import conexionINV.ConexionBaseDatos;
import utilidadesSer.ControladorEnvioCorreo;
import ModeloSer.CorreoElectronico;
import ModeloSer.GastoSemanal;
import capaDAOCC.MarcacionAnulacionPedidoDAO;
import capaDAOCC.MarcacionCambioPedidoDAO;
import capaDAOCC.MarcacionComisionDAO;
import capaDAOCC.PedidoDAO;
import capaDAOCC.RazonSocialDAO;
import capaModeloCC.MarcacionAnulacionPedido;
import capaModeloCC.MarcacionCambioPedido;
import capaModeloCC.MarcacionComision;
import capaModeloCC.RazonSocial;
import capaModeloCC.Tienda;
import ModeloSer.Correo;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

public class ReporteSemanalPagoRappiReproceso {
	
	
	public void generarReporteRappi()
	{
		DecimalFormat formatea = new DecimalFormat("###,###");
		//Obtenemos las razones sociales que vamos a procesar
		ArrayList<RazonSocial> razonesSociales = RazonSocialDAO.obtenerRazones();
		RazonSocial razTemp;
		//Recuperamos la relación Marcación , tienda comisión
		ArrayList<MarcacionComision> marcacionesComision = MarcacionComisionDAO.obtenerMarcacionComision(2);
		//Posteriormente realizamos el procesamiento para definir el rango de fechas del cual deseamos procesar el reporte
		//Recuperamos la fecha actual del sistema con la fecha apertura
		String fechaActual = "";
		//Variables donde manejaremos la fecha anerior con el fin realizar los cálculos de ventas
		Date datFechaAnterior;
		String fechaAnterior = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		double porcentajeIvaComision = 19;
		//Obtenemos la fecha Actual
		try
		{
			//OJO
			fechaActual = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
			porcentajeIvaComision = (double)ParametrosDAO.retornarValorNumerico("PORCENTAJEIVACOMISION");
			//fechaActual = "2021-02-01";
		}catch(Exception exc)
		{
			System.out.println(exc.toString());
		}
		try
		{
			//Al objeto calendario le fijamos la fecha actual del sitema
			calendarioActual.setTime(dateFormat.parse(fechaActual));
			
		}catch(Exception e)
		{
			System.out.println(e.toString());
		}
		//Retormanos el día de la semana actual segun la fecha del calendario
		//OJO
		//int diaActual = 1;
		int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
		
		//Vamos a poner el proceso a correr los miercoles
		if(diaActual == 4)
		{
			//Si es miercoles se resta dos
			calendarioActual.add(Calendar.DAY_OF_YEAR, -5);
			fechaActual = dateFormat.format(calendarioActual.getTime());
			calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
			datFechaAnterior = calendarioActual.getTime();
			fechaAnterior = dateFormat.format(datFechaAnterior);
		}else
		{
			System.out.println("Recuerda que este proceso debe correr es lo miercoles");
			return;
		}
		
		
		//En este punto ya tenemos FechaActual y fechaAnterior, con estas dos iremos a obtener los pedidos para la presentación pero esto lo haremos en un ciclo for por razón social.
		//Recuperamos el idProducto asociado a domicilios.com
		for(int i = 0; i < razonesSociales.size(); i++)
		{
			razTemp = razonesSociales.get(i);
			//Obtenemos las tiendas
			ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendasxRazon(razTemp.getIdRazon());
			
			//Obtenemos un total por tienda de los pedidos
			ArrayList pedidosDomCOMTienda;
			//Obtenemos totales de pago online por tienda
			ArrayList pedidosDomCOMONLINETienda = PedidoDAO.obtenerPedidosPlataformasONLINETienda(razTemp.getIdRazon(), fechaAnterior, fechaActual,2);
			//Procedemos a procesar la información y a enviar el correo con el reporte
			String respuesta = "";
			//Agregamos en este apartado el total de pedidos por tienda para poder extraer la comisión por tienda
			respuesta = respuesta + "<table border='2'> <tr colspan='8'>RAPPI TOTAL POR TIENDA " + razTemp.getNombreRazon() +  " </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Tienda</strong></td>"
					+  "<td><strong>Total Pedidos</strong></td>"
					+  "<td><strong>Total Pedidos en LINEA</strong></td>"
					+  "<td><strong>Total Descuentos</strong></td>"
					+  "<td><strong>Total Tarifa Servicio</strong></td>"
					+  "<td><strong>Total Propina</strong></td>"
					+  "<td><strong>Comisión Total</strong></td>"
					+  "<td><strong>Costo Pagos en Linea</strong></td>"
					+"</tr>";
			String[] resTotalTienda;
			String[] resTotalONLINE;
			double totalPagosONLINE = 0;
			double totalComisionPedido = 0;
			double totalComision = 0;
			double totalComisionFinal = 0;
			double totalGastoPagoONLINE = 0;
			double totalGastoPagoONLINEFinal = 0;
			double totalTarifaServicioFinal = 0;
			double totalDescuentoFinal = 0;
			double totalConsignacion = 0;
			double comision = 0;
			double comisionfull = 0;
			double totalPedido = 0;
			double totalPedidoTienda = 0;
			double descuento = 0;
			double totalDescuento = 0;
			double totalTarifaServicio = 0;
			double totalPropina = 0;
			double tarifaServicio = 0;
			double propina = 0;
			String marketplace = "";
			String descuentoAsumido = "";
			for(Tienda tiendaTemp : tiendas)
			{
				totalPedidoTienda = 0;
				totalDescuento = 0;
				totalComision = 0;
				totalTarifaServicio = 0;
				totalPropina = 0;
				//Revisar a que corresponde la Marcacion Comision
				comision = 0;
				comisionfull = 0;
				for(MarcacionComision marComTemp: marcacionesComision)
				{
					if(marComTemp.getIdTienda() == tiendaTemp.getIdTienda())
					{
						comision = (double)marComTemp.getComision();
						comisionfull = (double)marComTemp.getComisionfull();
						break;
					}
				}
				pedidosDomCOMTienda = PedidoDAO.obtenerPedidosPlataformasTiendaDetallada(razTemp.getIdRazon(), fechaAnterior, fechaActual,2,tiendaTemp.getIdTienda());
				for(int z = 0; z < pedidosDomCOMTienda.size(); z++)
				{
					String[] pedTienda = (String[]) pedidosDomCOMTienda.get(z);
					totalPedido = Double.parseDouble(pedTienda[0]);
					totalPedidoTienda = totalPedidoTienda + totalPedido;
					descuento = Double.parseDouble(pedTienda[4]);
					tarifaServicio = Double.parseDouble(pedTienda[5]);
					propina = Double.parseDouble(pedTienda[6]);
					marketplace = pedTienda[2];
					totalDescuento = totalDescuento + descuento;
					totalTarifaServicio = totalTarifaServicio + tarifaServicio;
					totalPropina = totalPropina + propina;
					if(marketplace.equals(new String("S")))
					{
						totalComisionPedido = totalPedido*(comision/100);
					}else
					{
						totalComisionPedido = totalPedido*(comisionfull/100);
					}
					totalComision = totalComision + totalComisionPedido;
				}
				totalPagosONLINE = 0;
				for(int k = 0; k < pedidosDomCOMONLINETienda.size(); k++ )
				{
					resTotalONLINE = (String[]) pedidosDomCOMONLINETienda.get(k);
					if(resTotalONLINE[3].equals(Integer.toString(tiendaTemp.getIdTienda())))
					{
						totalPagosONLINE = Double.parseDouble(resTotalONLINE[1]);
						break;
					}
				}
				totalConsignacion = totalConsignacion + totalPagosONLINE;
				totalComision = totalComision + ((totalComision)*(porcentajeIvaComision/100));
				totalComisionFinal = totalComisionFinal + totalComision;
				totalGastoPagoONLINE = (totalPagosONLINE * 0.06);
				totalGastoPagoONLINEFinal = totalGastoPagoONLINEFinal + totalGastoPagoONLINE;
				totalTarifaServicioFinal = totalTarifaServicioFinal + totalTarifaServicio;
				totalDescuentoFinal = totalDescuentoFinal + totalDescuento;
				respuesta = respuesta + "<tr><td>" + tiendaTemp.getNombreTienda() + "</td><td>" + formatea.format(totalPedidoTienda) +  "</td><td>" + formatea.format(totalPagosONLINE) + "</td><td>" + formatea.format(totalDescuento) + "</td><td>" + formatea.format(totalTarifaServicio) + "</td><td>" + formatea.format(totalPropina) + "</td><td>" + formatea.format(totalComision) + "</td><td>" + formatea.format(totalGastoPagoONLINE) +"</td></tr>";
				//En este punto tenemos el total de la tienda y lo insertaremos en la tabla correspondiente
				GastoSemanal gastoSemanalTemp = new GastoSemanal(0,tiendaTemp.getIdTienda(),18,fechaActual,totalComision+totalGastoPagoONLINE,totalComision+totalGastoPagoONLINE);
				GastoSemanalDAO.insertarGastoSemanal(gastoSemanalTemp);
			}
			respuesta = respuesta + "</table> <br/>";
			double totalConsignacionBruto = totalConsignacion;
			respuesta = respuesta + "<b>TOTAL BRUTO CONSIGNACIÓN " + formatea.format(totalConsignacionBruto) +"</b><br/>";
			totalConsignacion = totalConsignacion - totalComisionFinal - totalGastoPagoONLINEFinal - totalTarifaServicioFinal + totalDescuentoFinal;
			respuesta = respuesta + "<b> - TOTAL GASTO COMISIÓN " + formatea.format(totalComisionFinal) +"</b><br/>";
			respuesta = respuesta + "<b> - TOTAL GASTO PAGOS ON LINE " + formatea.format(totalGastoPagoONLINEFinal) +"</b><br/>";
			respuesta = respuesta + "<b> - TOTAL TARIFA DE SERVICIO DE RAPPI " + formatea.format(totalTarifaServicioFinal) +"</b><br/>";
			respuesta = respuesta + "<b> + TOTAL DESCUENTOS ASUMIDOS POR RAPPI " + formatea.format(totalDescuentoFinal) +"</b><br/>";
			respuesta = respuesta + "<b>CONSIGNACIÓN APROXIMADA " + formatea.format(totalConsignacion) +"</b><br/>";
			//Procedemos al envío del correo
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("REPORTE PAGO SEMANAL RAPPI de la Razón Social " + razTemp.getNombreRazon() + " " + razTemp.getIdentificacion());
			correo.setContrasena(infoCorreo.getClaveCorreo());
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEPAGORAPPI");
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuación el reporte semanal de pedidos tomados para RAPPI separados por razones sociales entre las fechas " + fechaAnterior + " - " + fechaActual +  ": \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
			
		}
		
	}
	
	

public static void main(String[] args)
{
	ReporteSemanalPagoRappiReproceso reporteDomicios = new ReporteSemanalPagoRappiReproceso();
	reporteDomicios.generarReporteRappi();
	//ConexionBaseDatos con = new ConexionBaseDatos();
	//Connection con1 = con.obtenerConexionBDTienda("");
	
}

}




