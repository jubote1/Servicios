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


public class ServicioEnvioEncuestas {
	
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
		int hora = calendarioActual.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
		int minutos = calendarioActual.get(Calendar.MINUTE);
		//Realizamos la validación para no envío de la encuesta en horarios prohibidos la hora debe estar entre 10:00 am y 9:00 pm
		if(hora >= 10 && hora <= 21 )
		{
			if(hora >= 10 && hora < 12)
			{
				calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
				fechaActual = dateFormat.format(calendarioActual.getTime());
			}
			ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendas();
			//Variables para la labor de cada tienda
			Tienda tiendaTemp;
			ClienteEnvioEncuesta clienteTemp;
			boolean debeEnviarEncuesta = false;
			for(int j = 0; j < tiendas.size(); j++)
			{
				tiendaTemp = tiendas.get(j);
				ArrayList <ClienteEnvioEncuesta> clientesDomicilio = PedidoDAO.obtenerClientesEnvioEncuesta(tiendaTemp.getHosbd(), fechaActual, false);
				if(clientesDomicilio.size() > 0)
				{
					//Recuperamos el correo para envío del parsing
					ArrayList correos = GeneralDAO.obtenerCorreosParametro("PARSERENCUESTASERVICIO");
					for(int i = 0; i < clientesDomicilio.size(); i++)
					{
						clienteTemp = clientesDomicilio.get(i);
						//Tomado el cliente hacemos la validación de si se debe enviar o no
						debeEnviarEncuesta = LogEncuestaServicioDAO.validarEncuestaServicio(clienteTemp.getTelefonoCelular());
						if(debeEnviarEncuesta)
						{
							Correo correo = new Correo();
							CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
							correo.setContrasena(infoCorreo.getClaveCorreo());
							correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
							correo.setAsunto("ENCUESTA PEDIDO # " + clienteTemp.getIdPedidoTienda());
							String mensajeCuerpoCorreo = "Numero Pedido:" + clienteTemp.getIdPedidoTienda() + " \n <br>"
									+ "Nombre Cliente:" + clienteTemp.getNombreCliente() + " \n <br>"
									+ "Numero Telefono:" + clienteTemp.getTelefonoCelular() + " \n <br>"
									+ "email:" + clienteTemp.getEmail() + " \n <br>"
									+ "idtienda:" + tiendaTemp.getIdTienda() + " \n <br>"
									+ "Nombre:" + clienteTemp.getNombreCliente() + " \n <br>"
									+ "Telefono:" + clienteTemp.getTelefonoCelular() + " \n <br>";
							correo.setMensaje(mensajeCuerpoCorreo);ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
							contro.enviarCorreoHTML();
							//Agregamos log servicio encuesta
							LogEncuestaServicio encuestaServicio = new LogEncuestaServicio(0, clienteTemp.getIdPedidoTienda(), clienteTemp.getNombreCliente(),  clienteTemp.getTelefonoCelular(), tiendaTemp.getIdTienda(),
									"", "", "");
							LogEncuestaServicioDAO.insertarLogEncuestaServicio(encuestaServicio);
							try
							{
								Thread.sleep(3000);
							}catch(Exception e)
							{
								
							}
							//Una vez enviado el correo electrónico marcarmos le encuesta como enviada
							PedidoDAO.actualizarPedidoEncuestaEnviada(tiendaTemp.getHosbd(), clienteTemp.getIdPedidoTienda(), false);
						}
					}
				}
				//Una vez terminado los clientes a domicilio continuamos con los clientes de punto de venta
				ArrayList <ClienteEnvioEncuesta> clientesPV = PedidoDAO.obtenerClientesEnvioEncuestaPV(tiendaTemp.getHosbd(), fechaActual, false);
				if(clientesPV.size() > 0)
				{
					ArrayList correos = GeneralDAO.obtenerCorreosParametro("PARSERENCUESTASERVICIOPV");
					for(int i = 0; i < clientesPV.size(); i++)
					{
						clienteTemp = clientesPV.get(i);
						//Tomado el cliente hacemos la validación de si se debe enviar o no
						debeEnviarEncuesta = LogEncuestaServicioDAO.validarEncuestaServicio(clienteTemp.getTelefonoCelular());
						if(debeEnviarEncuesta)
						{
							Correo correo = new Correo();
							CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
							correo.setContrasena(infoCorreo.getClaveCorreo());
							correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
							correo.setAsunto("ENCUESTA PEDIDO # " + clienteTemp.getIdPedidoTienda());
							String mensajeCuerpoCorreo = "Numero Pedido:" + clienteTemp.getIdPedidoTienda() + " \n <br>"
									+ "Nombre Cliente:" + clienteTemp.getNombreCliente() + " \n <br>"
									+ "Numero Telefono:" + clienteTemp.getTelefonoCelular() + " \n <br>"
									+ "email:" + clienteTemp.getEmail() + " \n <br>"
									+ "idtienda:" + tiendaTemp.getIdTienda() + " \n <br>"
									+ "Nombre:" + clienteTemp.getNombreCliente() + " \n <br>"
									+ "Telefono:" + clienteTemp.getTelefonoCelular() + " \n <br>";
							correo.setMensaje(mensajeCuerpoCorreo);ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
							contro.enviarCorreoHTML();
							//Agregamos log servicio encuesta
							LogEncuestaServicio encuestaServicio = new LogEncuestaServicio(0, clienteTemp.getIdPedidoTienda(), clienteTemp.getNombreCliente(),  clienteTemp.getTelefonoCelular(), tiendaTemp.getIdTienda(),
									"", "", "");
							LogEncuestaServicioDAO.insertarLogEncuestaServicio(encuestaServicio);
							try
							{
								Thread.sleep(3000);
							}catch(Exception e)
							{
								
							}
							//Una vez enviado el correo electrónico marcarmos le encuesta como enviada
							PedidoDAO.actualizarPedidoEncuestaEnviada(tiendaTemp.getHosbd(), clienteTemp.getIdPedidoTienda(), false);
						}
					}
				}
			}
		}
		
				
	}

}
