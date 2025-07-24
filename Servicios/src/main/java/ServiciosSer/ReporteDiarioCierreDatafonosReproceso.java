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

import CapaDAOSer.CierreDatafonoPOSDAO;
import CapaDAOSer.EmpleadoTemporalDiaDAO;
import CapaDAOSer.EmpresaTemporalDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiempoPedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.CierreDatafonoPOS;
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
import capaModeloPOS.DatafonoCierre;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteDiarioCierreDatafonosReproceso {
	
			
		
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
		
		//Realizamos la operaci�n para restar un d�a a la fecha teniendo en cuenta que correr� m�s tarde
		Calendar calendarioActual = Calendar.getInstance();
		try
		{
			//OJO
			//fechaActual = dateFormat.format(calendarioActual.getTime());
			//fechaActual = "2021-02-01";
			fechaActual = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
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
		datFechaActual = calendarioActual.getTime();
		
		//Comenzamos a traer la fecha actual como un String
		fechaActual = dateFormat.format(datFechaActual);
		//El proceso correra  las 11:50 pm
		
		
		String respuesta = "";
		
		//Recuperaremos las tiendas y empezaremos a ir consultando una a una las tiendas para extraer la informaci�n
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		ArrayList detallePedido;
		String[] fila;
		for(Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				respuesta = respuesta + "<table border='2'> <tr><td colspan ='5'>" + tien.getNombreTienda() + " CIERRE DE DAT�FONOS " + fechaActual  + "</td></tr>";
				respuesta = respuesta + "<tr>"
						+  "<td><strong>DATAFONO</strong></td>"
						+  "<td><strong>TERMINAL</strong></td>"
						+  "<td><strong>Valor Calculado</strong></td>"
						+  "<td><strong>Valor Ingresado</strong></td>"
						+  "<td><strong>Observaci�n</strong></td>"
						+  "</tr>";
				//Recuperamos los evento de empleados para la semana en cuesti�n
				ArrayList<DatafonoCierre> datafonosCierre = DatafonoCierreDAO.consultarDatafonosCierre(fechaActual, tien.getHostBD(), false);
				//Comenzamos a recorrer para ir presetnando la informaci�n
				for(DatafonoCierre datTemp : datafonosCierre)
				{
					respuesta = respuesta + "<tr>"
							+  "<td>" + datTemp.getNombreDatafono() + "</td>"
							+  "<td>" + datTemp.getTerminal() + "</td>"
							+  "<td>" + datTemp.getValorCalculado() + "</td>"
							+  "<td>" + datTemp.getValorIngresado() + "</td>"
							+  "<td>" + datTemp.getObservacion() + "</td>";
					CapaDAOSer.DatafonoCierreDAO.insertarDatafonoCierre(datTemp, tien.getIdTienda(), fechaActual, false);
				}
				respuesta = respuesta + "</table> <br/>";
				
				//Agregamos todas los pedidos de datáfono
				respuesta = respuesta + "<table WIDTH='600' border='2'> <tr><td colspan ='6'>DETALLE DATAFONOS " + tien.getNombreTienda()  + " </td></tr>";
				respuesta = respuesta + "<tr>"
						+  "<td WIDTH='70'><strong>PEDIDO</strong></td>"
						+  "<td WIDTH='130'><strong>VALOR DE PAGO</strong></td>"
						+  "<td WIDTH='200'><strong>CLIENTE</strong></td>"
						+  "<td WIDTH='70'><strong>TELEFONO</strong></td>"
						+  "<td WIDTH='70'><strong>HORA TOMA PEDIDO</strong></td>"
						+  "<td WIDTH='60'><strong>DATAFONO</strong></td></tr>";
				detallePedido = TiendaDAO.obtenerPedidosFormaPagoPar(fechaActual,2, tien.getHostBD(), false);
				double totalDatafono = 0;
				for(int i = 0; i < detallePedido.size(); i++)
				{
					fila = (String[]) detallePedido.get(i);
					totalDatafono = totalDatafono + Double.parseDouble(fila[1]);
					respuesta = respuesta + "<tr>"
							+  "<td>" + fila[0] + "</td>"
							+  "<td>" + fila[1] + "</td>"
							+  "<td>" + fila[2] + "</td>"
							+  "<td>" + fila[3] + "</td>"
							+  "<td>" + fila[4] + "</td>"
							+  "<td>" + fila[5] + "</td></tr>";
				}
				respuesta = respuesta + "<tr><td colspan ='6'> TOTAL : " + formatea.format(totalDatafono) +"</td></tr>";
				respuesta = respuesta + "</table> <br/>";
				//Realizamos inserción de dato total de datáfono en el datamart histórico
				CierreDatafonoPOS cierre = new CierreDatafonoPOS(tien.getIdTienda(),fechaActual,totalDatafono);
				CierreDatafonoPOSDAO.insertarCierreDatafonoPOS(cierre);
			}
		}
			//Recuperar la lista de distribuci�n para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("CIERREDATAFONO");
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("REPORTE DIARIO CIERRE DATAFONOS " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuaci�n el resumen de cierres de dat�fono diario para la fecha " + fechaActual +": \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		
	}
	
}

