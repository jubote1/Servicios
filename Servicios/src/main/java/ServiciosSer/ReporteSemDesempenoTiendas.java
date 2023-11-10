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
import ModeloSer.Usuario;
import capaControladorPOS.PedidoCtrl;
import capaDAOPOS.DatafonoCierreDAO;
import capaDAOPOS.DatafonoDAO;
import capaModeloCC.Tienda;
import capaModeloPOS.DatafonoCierre;
import capaModeloPOS.Desempeno;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteSemDesempenoTiendas {
	
			
		
	public static void main( String[] args )
	        
	{

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DecimalFormat formatea = new DecimalFormat("###,###.##");
		//Traemos la fechaActual en Blanco
		String fechaActual = "";
		//Traemos la fecha actual en un date
		Date datFechaActual = new Date();
		Date datFechaAnterior;
		String fechaAnterior = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		try
		{
			//OJO
			fechaActual = dateFormat.format(calendarioActual.getTime());
		}catch(Exception exc)
		{
			System.out.println(exc.toString());
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
		String respuesta = "";
		//Ahora realizamos el procesamiento de lo que es como tal el corazón del reporte
		//Vamos a incluir la lógica para traer todas las tiendas y revisar las estadísticas de los domiciliarios en dicho día y
		// de la cocina
		capaControladorPOS.PedidoCtrl pedCtrl = new PedidoCtrl(false);
		ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendas();
		Tienda tiendaTemp;
		for(int j = 0; j < tiendas.size(); j++)
		{
			tiendaTemp = tiendas.get(j);
			if(!tiendaTemp.getHosbd().equals(new String("")))
			{
				//Realizamos la labor con cada una de las tiendas
				respuesta = respuesta + "<table WIDTH='250' border='2'> <TH COLSPAN='6'> DESEMPEÑO DOMICILIARIOS " + tiendaTemp.getNombreTienda()  + "</TH> </tr>";
				respuesta = respuesta + "<tr>"
						+  "<td width='190' nowrap><strong>NOMBRE DOMICILIARIO</strong></td>"
						+  "<td width='60' nowrap><strong>PEDIDOS INCORRECTOS</strong></td>"
						+  "<td width='60' nowrap><strong>MENOR TIEMPO</strong></td>"
						+  "<td width='60' nowrap><strong>MAYOR TIEMPO</strong></td>"
						+  "<td width='60' nowrap><strong>PEDIDOS</strong></td>"
						+  "<td width='60' nowrap><strong>PROMEDIO</strong></td>"
						+  "</tr>";
				ArrayList<capaModeloPOS.Usuario>domiciliarios = pedCtrl.obtenerDomiciliariosFecha(fechaAnterior,fechaActual, tiendaTemp.getHosbd());
				for(int z = 0; z < domiciliarios.size(); z++)
				{
					capaModeloPOS.Usuario domiTemp = domiciliarios.get(z);
					Desempeno desempeno = pedCtrl.obtenerDesemDom(fechaAnterior,fechaActual, domiTemp.getIdUsuario(), tiendaTemp.getHosbd());
					respuesta = respuesta + "<tr><td width='190' nowrap>" + domiTemp.getNombreLargo() + "</td><td width='60' nowrap> " + desempeno.getCantidadPedidosIncorrectos() + "</td><td width='60' nowrap> " + desempeno.getTiempoMenorPedido() + "</td><td width='60' nowrap> " + desempeno.getTiempoMayorPedido() + "</td><td width='60' nowrap> " + desempeno.getCantidadPedidos() + "</td><td width='60' nowrap> " + desempeno.getTiempoPromedioEntrega() +"</td></tr>";
				}
				respuesta = respuesta + "</table> <br/>";
			}
		}
		
		//Generamos el desempeño de las cocinas de las tiendas
		for(int j = 0; j < tiendas.size(); j++)
		{
			tiendaTemp = tiendas.get(j);
			if(!tiendaTemp.getHosbd().equals(new String("")))
			{
				//Realizamos la labor con cada una de las tiendas
				respuesta = respuesta + "<table WIDTH='250' border='2'> <TH COLSPAN='6'> DESEMPEÑO COCINA " + tiendaTemp.getNombreTienda()  + "</TH> </tr>";
				respuesta = respuesta + "<tr>"
						+  "<td width='190' nowrap><strong>TIENDA</strong></td>"
						+  "<td width='60' nowrap><strong>PEDIDOS INCORRECTOS</strong></td>"
						+  "<td width='60' nowrap><strong>MENOR TIEMPO</strong></td>"
						+  "<td width='60' nowrap><strong>MAYOR TIEMPO</strong></td>"
						+  "<td width='60' nowrap><strong>PEDIDOS</strong></td>"
						+  "<td width='60' nowrap><strong>PROMEDIO</strong></td>"
						+  "</tr>";
				Desempeno desempeno = pedCtrl.obtenerDesemCocina(fechaAnterior,fechaActual,  tiendaTemp.getHosbd());
				respuesta = respuesta + "<tr><td width='190' nowrap>" + tiendaTemp.getNombreTienda() + "</td><td width='60' nowrap> " + desempeno.getCantidadPedidosIncorrectos() + "</td><td width='60' nowrap> " + desempeno.getTiempoMenorPedido() + "</td><td width='60' nowrap> " + desempeno.getTiempoMayorPedido() + "</td><td width='60' nowrap> " + desempeno.getCantidadPedidos() + "</td><td width='60' nowrap> " + desempeno.getTiempoPromedioEntrega() +"</td></tr>";
				respuesta = respuesta + "</table> <br/>";
			}	
		}
		
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("DESEMPENOTIENDA");
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("REPORTE DESEMPEÑO TIENDAS entre fecha " + fechaAnterior + " y " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje(respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		
	}
	
}

