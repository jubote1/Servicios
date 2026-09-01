package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.GeneralDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import capaDAOCC.LogEncuestaServicioDAO;
import capaDAOCC.ParametrosDAO;
import capaDAOPOS.PedidoDAO;
import capaModeloCC.LogEncuestaServicio;
import capaModeloCC.Tienda;
import capaModeloPOS.ClienteEnvioEncuesta;
import utilidadesSer.ControladorEnvioCorreo;


public class ServicioEnvioEncuestasProductos {
	
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
		//Realizaremos el ejercio para la fecha anterior
		calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
		fechaActual = dateFormat.format(calendarioActual.getTime());
		ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendas();
		//Variables para la labor de cada tienda
		Tienda tiendaTemp;
		ClienteEnvioEncuesta clienteTemp;
		boolean debeEnviarEncuesta = false;
		for(int j = 0; j < tiendas.size(); j++)
		{
			tiendaTemp = tiendas.get(j);
			ArrayList <ClienteEnvioEncuesta> clientesDomicilio = PedidoDAO.obtenerClientesEnvioEncuestaProducto(tiendaTemp.getHosbd(), fechaActual, 0, false);
			if(clientesDomicilio.size() > 0)
			{
				//Recuperamos el correo para envío del parsing
				ArrayList correos = GeneralDAO.obtenerCorreosParametro("PARSERENCUESTAPRODUCTO");
				for(int i = 0; i < clientesDomicilio.size(); i++)
				{
					clienteTemp = clientesDomicilio.get(i);
					//Tomado el cliente hacemos la validación de si se debe enviar o no
					Correo correo = new Correo();
					CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
					correo.setContrasena(infoCorreo.getClaveCorreo());
					correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
					correo.setAsunto("ENCUESTA PRODUCTOS NUEVA ACTIVIDAD");
					String mensajeCuerpoCorreo = "Nombre cliente:" + clienteTemp.getNombreCliente() + " \n <br>"
							+ "Numero de telefono:" + clienteTemp.getTelefonoCelular() + " \n <br>"
							+ "Tipo de actividad:Pizza Artesanal Hawaiana" +  " \n <br>"
							+ "Numero Pedido:" + clienteTemp.getIdPedidoTienda() + " \n <br>";
					correo.setMensaje(mensajeCuerpoCorreo);ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
					contro.enviarCorreoHTML();

				}
			}
		}
		
	}

}
