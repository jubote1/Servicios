package ServiciosSer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.CampanaDAO;
import CapaDAOSer.ClienteDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaCodigoPromocionalDAO;
import ModeloSer.Campana;
import ModeloSer.ClienteCampana;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.TiendaCodigoPromocional;
import capaControladorCC.PromocionesCtrl;
import capaModeloCC.OfertaCliente;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioNotificacionCampana {
	
	
	
	
public static void main(String[] args)
{
	ServicioNotificacionCampana reporteConsumosUsuarios = new ServicioNotificacionCampana();
	reporteConsumosUsuarios.generarPromociones();
	
}

public void generarPromociones()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCI�N NOTIFICACION CAMPA�A");
	//Capturamos el par�metro del proceso que se va a ejecutar
	int idCampana = 0;
	try
	{
		//OJO
		//fechaActual = dateFormat.format(calendarioActual.getTime());
		//fechaActual = "2020-07-26";
		idCampana = ParametrosDAO.retornarValorNumerico("IDCAMPANA");
	}catch(Exception exc)
	{
		idCampana = 1;
		System.out.println(exc.toString());
	}
	//Generamos String de tiendas exitosas y tiendas no exitosas para mandar correo.
	String respuesta = "";
	
	//Generamos la fecha en la que corre el proceso
	Date fechaActual = new Date();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//Formateamos la fecha Actual para consulta
	String strFechaActual = dateFormat.format(fechaActual);
	
	Calendar calendarioActual = Calendar.getInstance();
	int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
	
	//Debemos recuperar la informaci�n de las tiendas codigos promocionales
	Campana campana = CampanaDAO.retornarCampana(idCampana);

	//Variable controladora de la soluci�n de Contact Center
	PromocionesCtrl promoCtrl = new PromocionesCtrl();
	//Variable donde almacenaremos el resultado del env�o de la campana
	String respuestaMensaje = "";
	
	//Vamos a recuperar los clientes seg�n el query para recuperar a quienes les aplica la promoci�n
	
	//Vamos a revisar los contadores para determinar los totales procesados y enviados de correo y mensaje de texto
	int clientesProcesados = 0;
	int clientesCorreo = 0;
	int clientesMensaje = 0;
	
	//Posteriormente realizamos la consulta para recuperar los clientes que interesar�an
	ArrayList<ClienteCampana> clientesCampanas = PedidoDAO.obtenerClientesCampana(campana.getQuery());
	
	//Por cada clientes debemos de realizar una serie de acciones de notificaci�n al cliente
	for(int j = 0; j < clientesCampanas.size(); j++)
	{
		clientesProcesados++;
		ClienteCampana clienteCampanaTemp =clientesCampanas.get(j);
		//Realizaremos una serie de controles con los datos, el primero ser� verificar si no tiene celular
		// pero lo tiene en el campo de telefono, en cuyo caso realizaremos una actualizaci�n para llevarlo tambi�n
		// telefono celular
		if(clienteCampanaTemp.getTelefonoCelular().equals(new String("")))
		{
			if((clienteCampanaTemp.getTelefono().substring(0, 1).equals(new String("3"))) && (clienteCampanaTemp.getTelefono().length() == 10))
			{
				ClienteDAO.actualizarTelCelularCliente(clienteCampanaTemp.getIdCliente(), clienteCampanaTemp.getTelefono());
			}
		}
		
		//Realizaremos el env�o del mensaje de texto y del correo electr�nico
		if(clienteCampanaTemp.getTelefonoCelular().length() > 0)
		{
			if((clienteCampanaTemp.getTelefonoCelular().substring(0, 1).equals(new String("3"))) && (clienteCampanaTemp.getTelefonoCelular().length() == 10))
			{
				respuestaMensaje = promoCtrl.ejecutarPHPEnvioMensaje( "57"+ clienteCampanaTemp.getTelefonoCelular(), campana.getMensajeTexto());
				clientesMensaje++;
			}
		}
		else if(clienteCampanaTemp.getTelefono().length() > 0)
		{
			if((clienteCampanaTemp.getTelefono().substring(0, 1).equals(new String("3"))) && (clienteCampanaTemp.getTelefono().length() == 10))
			{
				respuestaMensaje = promoCtrl.ejecutarPHPEnvioMensaje( "57"+ clienteCampanaTemp.getTelefono(), campana.getMensajeTexto());
				clientesMensaje++;
			}
		}
		//Posteriormente Intentamos realizar el env�o del correo electr�nico
		if(clienteCampanaTemp.getEmail().length() > 0)
		{
			if(clienteCampanaTemp.getEmail().contains("@"))
			{
				clientesCorreo++;
				String cuentaCorreo = ParametrosDAO.retornarValorAlfanumerico("CUENTACORREOPRIVADA");
				String claveCorreo = ParametrosDAO.retornarValorAlfanumerico("CLAVECORREOPRIVADA");
				Correo correo = new Correo();
				correo.setAsunto("PIZZA AMERICANA TIENE ALGO ESPECIAL PARA TI");
				ArrayList correos = new ArrayList();
				String correoEle = clienteCampanaTemp.getEmail();
				correos.add(correoEle);
				correo.setContrasena(claveCorreo);
				correo.setUsuarioCorreo(cuentaCorreo);
				String mensajeCuerpoCorreo = campana.getPlantilla();
				correo.setMensaje(mensajeCuerpoCorreo);
				ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
				//Agregamos control para que verifique con que m�todo debe hacer el env�o
				contro.enviarCorreoHTML();
				//Realizamos un retardo de un segundo para no tener problema con los correos
				try
				{
	    		  Thread.sleep(1000);
				}catch(Exception e)
				{
					System.out.println("Problemas en la pausa de 1 segundo");
				}	  
			}
		}
	}
		
		
		
	//Realizamos el env�o del correo electr�nico con los archivos
	Correo correo = new Correo();
	correo.setAsunto("GENERACI�N CORREOS Y MENSAJES CAMPA�A " + campana.getNombreCampana() + " " + fechaActual.toString());
	CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
	correo.setContrasena(infoCorreo.getClaveCorreo());
	//Tendremos que definir los destinatarios de este correo
	ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPGENERACIONPROMOCIONES");
	correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
	String mensaje = "A continuaci�n se informaci�n del proceso de env�o de campa�as a clientes, de los cuales se procesaron " + clientesProcesados + " clientes, se enviaron " + clientesMensaje + " mensajes y se enviaron " + clientesCorreo + " correos.";
	correo.setMensaje(mensaje);
	ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
	contro.enviarCorreoHTML();
}


}




