package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.GeneralDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import capaDAOCC.ParametrosDAO;
import capaDAOPOS.PedidoDAO;
import capaModeloCC.Tienda;
import capaModeloPOS.ClienteEnvioEncuesta;
import capaModeloPOS.DesempenoDomiciliario;
import capaModeloPOS.Usuario;
import utilidadesSer.ControladorEnvioCorreo;


public class ReporteOperacionDomiciliarios {
	
	public static void main( String[] args )
	{

		// 1. Recuperamos la fecha actual del sistema con la fecha apertura
		String fechaActual = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			fechaActual = dateFormat.format(calendarioActual.getTime());
		}catch(Exception exc)
		{
			System.out.println(exc.toString());
		}
		
		ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendas();
		//Variables para la labor de cada tienda
		Tienda tiendaTemp;
		ClienteEnvioEncuesta clienteTemp;
		String respuesta = "";
		for(int j = 0; j < tiendas.size(); j++)
		{
			tiendaTemp = tiendas.get(j);
			respuesta = respuesta + "<h1>" + tiendaTemp.getNombreTienda() + "</h1>";
			//Obtenemos los domiciliarios que laboraron en dicha tienda dicha fecha
			ArrayList<DesempenoDomiciliario> desempenos = PedidoDAO.obtenerDesempenoDomiciliarioFecha(fechaActual, tiendaTemp.getHosbd(), false);
			//Sacamos un listado de los domiciliaros que laboraron dicho dia
			respuesta = respuesta + "<table WIDTH='500' border='2'> <TH COLSPAN='5'> DESEMPENO DE DOMICILIARIOS QUE LABORARON EN " + tiendaTemp.getNombreTienda() + "</TH> </tr>";
			respuesta = respuesta + "<tr>"
					+  "<td><strong>Domiciliario</strong></td>"
					+  "<td><strong>Empresa</strong></td>"
					+  "<td><strong>Cant Pedidos</strong></td>"
					+  "<td><strong>Promedio</strong></td>"
					+  "<td><strong>Horas Trabajadas</strong></td>"
					+  "</tr>";
			DesempenoDomiciliario desTemp;
			for(int i = 0; i < desempenos.size(); i++)
			{
				desTemp = desempenos.get(i);
				respuesta = respuesta + "<tr>"
						+  "<td width='150' nowrap><strong>"+ desTemp.getNombreDomiciliario() +"</strong></td>" + "<td width='100'>"+ desTemp.getEmpresa() + "</td>" + "<td>"+ desTemp.getCantidadPedidos() + "</td>" + "<td>"+ desTemp.getPromedio() + "</td>" + "<td>"+ desTemp.getHorasTrabajadas() + "</td>"
						+  "</tr>";
			}
			respuesta = respuesta + "</table> <br/>";
		}	
		//Al final enviamos correo con el resultado del proceso
		//Recuperar la lista de distribuci�n para este correo
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEOPERACIONDOMICILIARIOS");
		Date fecha = new Date();
		Correo correo = new Correo();
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto(fechaActual + " REPORTE DIARIO OPERACION DOMICILIARIOS ");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("REPORTE DIARIO OPERACION DOMICILIARIOS : \n" + respuesta);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}

}
