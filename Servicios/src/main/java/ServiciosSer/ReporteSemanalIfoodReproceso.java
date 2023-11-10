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

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import conexionINV.ConexionBaseDatos;
import utilidadesSer.ControladorEnvioCorreo;
import ModeloSer.CorreoElectronico;
import capaDAOCC.MarcacionAnulacionPedidoDAO;
import capaDAOCC.MarcacionCambioPedidoDAO;
import capaDAOCC.MarcacionComisionDAO;
import capaDAOCC.PedidoDAO;
import capaDAOCC.RazonSocialDAO;
import capaModeloCC.MarcacionAnulacionPedido;
import capaModeloCC.MarcacionCambioPedido;
import capaModeloCC.MarcacionComision;
import capaModeloCC.RazonSocial;
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

public class ReporteSemanalIfoodReproceso {
	
	
	public void generarReporteIfood()
	{
		DecimalFormat formatea = new DecimalFormat("###,###");
		//Obtenemos las razones sociales que vamos a procesar
		ArrayList<RazonSocial> razonesSociales = RazonSocialDAO.obtenerTiendas();
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
		
		//Domingo
		if(diaActual == 1)
		{
			calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
		}
		else if(diaActual == 2)
		{
			calendarioActual.add(Calendar.DAY_OF_YEAR, -7);
		}
		else if(diaActual == 3)
		{
			//Si es martes se resta uno solo
			calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
		}
		else if(diaActual == 4)
		{
			//Si es miercoles se resta dos
			calendarioActual.add(Calendar.DAY_OF_YEAR, -2);
		}
		else if(diaActual == 5)
		{
			//Si es jueves se resta tres
			calendarioActual.add(Calendar.DAY_OF_YEAR, -3);
		}
		else if(diaActual == 6)
		{
			//Si es viernes se resta cuatro
			calendarioActual.add(Calendar.DAY_OF_YEAR, -4);
		}
		else if(diaActual == 7)
		{
			//Si es sabado se resta cinco
			calendarioActual.add(Calendar.DAY_OF_YEAR, -5);
		}
		//Llevamos a un string la fecha anterior para el cálculo de la venta
		datFechaAnterior = calendarioActual.getTime();
		fechaAnterior = dateFormat.format(datFechaAnterior);
		
		//En este punto ya tenemos FechaActual y fechaAnterior, con estas dos iremos a obtener los pedidos para la presentación pero esto lo haremos en un ciclo for por razón social.
		//Recuperamos el idProducto asociado a domicilios.com
		for(int i = 0; i < razonesSociales.size(); i++)
		{
			razTemp = razonesSociales.get(i);
			//Obtenemos un total por tienda de los pedidos
			ArrayList pedidosDomCOMTienda = PedidoDAO.obtenerPedidosPlataformasTiendaFull(razTemp.getIdRazon(), fechaAnterior, fechaActual,1);
			//Obtenemos totales de pago online por tienda
			ArrayList pedidosDomCOMONLINETienda = PedidoDAO.obtenerPedidosPlataformasONLINETienda(razTemp.getIdRazon(), fechaAnterior, fechaActual,1);
			//Procedemos a procesar la información y a enviar el correo con el reporte
			String respuesta = "";
			//Agregamos en este apartado el total de pedidos por tienda para poder extraer la comisión por tienda
			respuesta = respuesta + "<table border='2'> <tr>IFOOD TOTAL POR TIENDA " + razTemp.getNombreRazon() +  " </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Tienda</strong></td>"
					+  "<td><strong>Total Pedidos</strong></td>"
					+  "<td><strong>Total Pedidos en LINEA</strong></td>"
					+  "<td><strong>Total Descuentos</strong></td>"
					+  "<td><strong>Comisión Total</strong></td>"
					+  "<td><strong>Costo Pagos en Linea</strong></td>"
					+"</tr>";
			String[] resTotalTienda;
			String[] resTotalONLINE;
			double totalPagosONLINE = 0;
			double totalPedidoTienda = 0;
			double totalComision = 0;
			double totalComisionFinal = 0;
			double totalGastoPagoONLINE = 0;
			double totalGastoPagoONLINEFinal = 0;
			double totalConsignacion = 0;
			int comision = 0;
			for(int j = 0; j < pedidosDomCOMTienda.size(); j++)
			{
				resTotalTienda = (String[]) pedidosDomCOMTienda.get(j);
				//Buscamos la tienda para saber los pedidos ONLINE
				totalPagosONLINE = 0;
				for(int k = 0; k < pedidosDomCOMONLINETienda.size(); k++ )
				{
					resTotalONLINE = (String[]) pedidosDomCOMONLINETienda.get(k);
					if(resTotalONLINE[3].equals(new String(resTotalTienda[0])))
					{
						totalPagosONLINE = Double.parseDouble(resTotalONLINE[1]);
						break;
					}
				}
				//Revisar a que corresponde la Marcacion Comision
				comision = 0;
				for(MarcacionComision marComTemp: marcacionesComision)
				{
					if(marComTemp.getIdTienda() == Integer.parseInt(resTotalTienda[0]))
					{
						comision = marComTemp.getComision();
						break;
					}
				}
				totalConsignacion = totalConsignacion + totalPagosONLINE;
				totalPedidoTienda = Double.parseDouble(resTotalTienda[2]);
				totalComision = (totalPedidoTienda * ((double)comision/100)) + ((totalPedidoTienda * ((double)comision/100))*(porcentajeIvaComision/100));
				totalComisionFinal = totalComisionFinal + totalComision;
				totalGastoPagoONLINE = (totalPagosONLINE * 0.06);
				totalGastoPagoONLINEFinal = totalGastoPagoONLINEFinal + totalGastoPagoONLINE;
				respuesta = respuesta + "<tr><td>" + resTotalTienda[1] + "</td><td>" + formatea.format(totalPedidoTienda) +  "</td><td>" + formatea.format(totalPagosONLINE) + "</td><td>" + formatea.format(Double.parseDouble(resTotalTienda[3])) + "</td><td>" + formatea.format(totalComision) + "</td><td>" + totalGastoPagoONLINE +"</td></tr>";
			}
			respuesta = respuesta + "</table> <br/>";
			totalConsignacion = totalConsignacion - totalComisionFinal - totalGastoPagoONLINEFinal;
			respuesta = respuesta + "<b>TOTAL GASTO COMISIÓN " + formatea.format(totalComisionFinal) +"</b><br/>";
			respuesta = respuesta + "<b>TOTAL GASTO PAGOS ON LINE " + formatea.format(totalGastoPagoONLINEFinal) +"</b><br/>";
			respuesta = respuesta + "<b>CONSIGNACIÓN APROXIMADA " + formatea.format(totalConsignacion) +"</b><br/>";
			//Procedemos al envío del correo
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("Reporte Facturación Semanal IFOOD de la Razón Social " + razTemp.getNombreRazon() + " " + razTemp.getIdentificacion());
			correo.setContrasena(infoCorreo.getClaveCorreo());
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEDOMICILIOSCOM");
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuación el reporte semanal de pedidos tomados para IFOOD separados por razones sociales entre las fechas " + fechaAnterior + " - " + fechaActual +  ": \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
			
		}
		
	}
	
	

public static void main(String[] args)
{
	ReporteSemanalIfoodReproceso reporteDomicios = new ReporteSemanalIfoodReproceso();
	reporteDomicios.generarReporteIfood();
	//ConexionBaseDatos con = new ConexionBaseDatos();
	//Connection con1 = con.obtenerConexionBDTienda("");
	
}

}




