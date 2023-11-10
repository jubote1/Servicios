package ServiciosSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.EmpleadoTemporalDiaDAO;
import CapaDAOSer.EmpresaTemporalDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiempoPedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.DiaFestivo;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.EmpleadoTemporalDia;
import ModeloSer.EmpresaTemporal;
import ModeloSer.Pedido;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import capaControladorPOS.PedidoCtrl;
import capaDAOPOS.DatafonoCierreDAO;
import capaModeloPOS.DatafonoCierre;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteDiarioQRBancolombiaReproceso {
	
			
		
	public static void main( String[] args )
	        
	{
		//Requerimos primero que todo obtener el rango de fechas con el fin de tener dicho rango para las consultas
		//Definimos el formato como manejaremos las fechas
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DecimalFormat formatea = new DecimalFormat("###,###.##");
		//Traemos la fechaActual en Blanco
		String fechaActual = "";
		//Traemos la fecha actual en un date
		Date datFechaActual = new Date();
		fechaActual = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
		//El proceso correra  las 11:50 pm
		
		
		String respuesta = "";
		
		//Recuperaremos las tiendas y empezaremos a ir consultando una a una las tiendas para extraer la información
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		respuesta = respuesta + "<table border='2'> <tr><td colspan ='5'>CIERRE QR BANCOLOMBIA " + fechaActual  + "</td></tr>";
		respuesta = respuesta + "<tr>"
				+  "<td><strong>TIENDA</strong></td>"
				+  "<td><strong>VALOR DE VENTA</strong></td></tr>";
		PedidoCtrl pedCtrl = new PedidoCtrl(false);
		double totalFormaPago = 0;
		double totalGeneral = 0;
		for(Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				totalFormaPago = TiendaDAO.obtenerTotalFormaPago(fechaActual, tien.getHostBD(), false);
				totalGeneral = totalGeneral + totalFormaPago;
				respuesta = respuesta + "<tr>"
						+  "<td><strong>" + tien.getNombreTienda()+ "</strong></td>"
						+  "<td><strong> " + totalFormaPago +"</strong></td>"
						+  "</tr>";
			}
		}
		respuesta = respuesta + "<tr>"
				+  "<td><strong>TOTAL GENERAL</strong></td>"
				+  "<td><strong> " + totalGeneral +"</strong></td>"
				+  "</tr>";
		respuesta = respuesta + "</table> <br/>";
		//Realizamos la generación de la información del detalle de los QR por todas las tiendas
		ArrayList detallePedido;
		Long[] fila;
		for(Tienda tien : tiendas)
		{
			respuesta = respuesta + "<table border='2'> <tr><td colspan ='5'>DETALLE QR BANCOLOMBIA " + tien.getNombreTienda()  + " </td></tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>PEDIDO</strong></td>"
					+  "<td><strong>VALOR DE PAGO</strong></td></tr>";
			detallePedido = TiendaDAO.obtenerPedidosFormaPago(fechaActual, tien.getHostBD(), false);
			for(int i = 0; i < detallePedido.size(); i++)
			{
				fila = (Long[]) detallePedido.get(i);
				respuesta = respuesta + "<tr>"
						+  "<td>" + fila[0] + "</td>"
						+  "<td>" + fila[1] + "</td></tr>";
			}
			respuesta = respuesta + "</table> <br/>";
		}
		
		//Recuperar la lista de distribución para este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("CIERREQRBANCOLOMBIA");
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("REPORTE DIARIO VENTAS QR BANCOLOMBIA " + fechaActual);
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuación el resumen de ventas QR Bancolombia para la fecha " + fechaActual +": \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
		
	}
	
}

