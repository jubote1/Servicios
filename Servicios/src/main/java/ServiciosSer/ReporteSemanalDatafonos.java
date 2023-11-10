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
import capaDAOPOS.DatafonoCierreDAO;
import capaDAOPOS.DatafonoDAO;
import capaModeloPOS.DatafonoCierre;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteSemanalDatafonos {
	
			
		
	public static void main( String[] args )
	        
	{
		//Requerimos primero que todo obtener el rango de fechas con el fin de tener dicho rango para las consultas
		//Definimos el formato como manejaremos las fechas
		
		double totalFacturaDat = 0;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DecimalFormat formatea = new DecimalFormat("###,###.##");
		//Traemos la fechaActual en Blanco
		String fechaActual = "";
		//Traemos la fecha actual en un date
		Date datFechaActual = new Date();
		//Comenzamos a traer la fecha actual como un String
		fechaActual = dateFormat.format(datFechaActual);
		//El proceso correra  las 11:50 pm
		double valorFacturarDat  = 0;
		
		String respuesta = "";
		
		//Recuperaremos las tiendas y empezaremos a ir consultando una a una las tiendas para extraer la información
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		for(Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				//Agregamos los datáfonos que se tienen para poder ver
				respuesta = respuesta + "<table border='2'> <tr><td colspan ='5'>" + tien.getNombreTienda() + " DATÁFONOS  " + fechaActual  + "</td></tr>";
				respuesta = respuesta + "<tr>"
						+  "<td><strong>id Datafono</strong></td>"
						+  "<td><strong>Nombre</strong></td>"
						+  "<td><strong>Placa</strong></td>"
						+  "<td><strong>Terminal</strong></td>"
						+  "<td><strong>Funcional</strong></td>"
						+  "<td><strong>Reporte Daño</strong></td>"
						+  "</tr>";
				ArrayList<String[]> datafonos = DatafonoDAO.obtenerDatafonosRemoto(tien.getHostBD(), false);
				for(String[] filaTemp : datafonos)
				{
					respuesta = respuesta + "<tr>"
							+  "<td>" + filaTemp[0] + "</td>"
							+  "<td>" + filaTemp[1] + "</td>"
							+  "<td>" + filaTemp[2] + "</td>"
							+  "<td>" + filaTemp[3] + "</td>"
							+  "<td>" + filaTemp[5] + "</td>"
							+  "<td>" + filaTemp[6] + "</td>"
									+ "</tr>";
				}
				respuesta = respuesta + "</table> <br/>";
				//Agregamos el estado actual de los datáfonos
				respuesta = respuesta + "<table border='2'> <tr><td colspan ='5'>" + tien.getNombreTienda() + " ESTADO ACTUAL DATÁFONOS  " + fechaActual  + "</td></tr>";
				respuesta = respuesta + "<tr>"
						+  "<td><strong>DATAFONO</strong></td>"
						+  "<td><strong>Tipo Datáfono</strong></td>"
						+  "<td><strong>Costo Datáfono</strong></td>"
						+  "<td><strong>Min Transacciones</strong></td>"
						+  "<td><strong>Cantidad Transacciones</strong></td>"
						+  "</tr>";
				//Recuperamos los evento de empleados para la semana en cuestión
				ArrayList<String[]> datafonosCierre = capaDAOPOS.PedidoDAO.obtenerTransaccionesDatafonoRemoto(tien.getHostBD(), false);
				double costoDatafono = 0;
				int cantTranDat = 0;
				int cantTranDatReal = 0;
				valorFacturarDat  = 0;
				//Comenzamos a recorrer para ir presetnando la información
				for(String[] filaTemp : datafonosCierre)
				{
					respuesta = respuesta + "<tr>"
							+  "<td>" + filaTemp[0] + "</td>"
							+  "<td>" + filaTemp[1] + "</td>"
							+  "<td>" + filaTemp[2] + "</td>"
							+  "<td>" + filaTemp[3] + "</td>"
							+  "<td>" + filaTemp[4] + "</td></tr>";
					//Vamos a procesar y saber el total que se tiene que pagar por los datáfonos hasta la fecha
					try
					{
						cantTranDat = Integer.parseInt(filaTemp[3]);
					}catch(Exception e)
					{
						cantTranDat = 0;
					}
					try
					{
						cantTranDatReal = Integer.parseInt(filaTemp[4]);
					}catch(Exception e)
					{
						cantTranDatReal = 0;
					}
					try
					{
						costoDatafono = Double.parseDouble(filaTemp[2]);
					}catch(Exception e)
					{
						costoDatafono = 0;
					}
					
					if(cantTranDat >= cantTranDatReal)
					{
						valorFacturarDat = valorFacturarDat + costoDatafono;
					}
				}
				//Total de los facturado
				respuesta = respuesta + "<tr>"
						+  "<td colspan = '2'> TOTAL COSTO ACTUAL </td> <td colspan = '3'>" + valorFacturarDat + "</td></tr>";
				respuesta = respuesta + "</table> <br/>";
			}
			totalFacturaDat = totalFacturaDat + valorFacturarDat;
		}
			//Agregamos el total de facturación
			respuesta = respuesta + "<table border='2'> <tr><td> TOTAL FACTURA DE DATÁFONOS A LA FECHA  " + fechaActual  + "</td></tr>";
			respuesta = respuesta + "<tr><td> " + totalFacturaDat + "</td></tr></table> <br/>";
		
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("CIERREDATAFONO");
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("ESTADO ACTUAL DATÁFONOS " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuación el resumen del estado actual de los datáfonos a  " + fechaActual +": \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		
	}
	
}

