package ServiciosSer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import CapaDAOSer.ClienteDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaCodigoPromocionalDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.TiendaCodigoPromocional;
import capaControladorCC.PromocionesCtrl;
import capaModeloCC.OfertaCliente;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioPromociones {
	
	
	
	
public static void main(String[] args)
{
	ServicioPromociones reporteConsumosUsuarios = new ServicioPromociones();
	reporteConsumosUsuarios.generarPromociones();
	
}

public void generarPromociones()
{
	//Obtengo las tiendas parametrizadas en el sistema de inventarios
	System.out.println("EMPEZAMOS LA EJECUCIÓN PROCESO DE PROMOCIONES");
	//Capturamos el parámetro del proceso que se va a ejecutar
	int idProcesoPromocion = 0;
	try
	{
		//OJO
		//fechaActual = dateFormat.format(calendarioActual.getTime());
		//fechaActual = "2020-07-26";
		idProcesoPromocion = ParametrosDAO.retornarValorNumerico("IDPROCESOPROMOCION");
	}catch(Exception exc)
	{
		idProcesoPromocion = 1;
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
	
	//Debemos recuperar la información de las tiendas codigos promocionales
	ArrayList<TiendaCodigoPromocional> tiendaCodPromos = TiendaCodigoPromocionalDAO.retornarTiendaCodigoPromocional(idProcesoPromocion);

	//Las variables temporales para el procesamiento
	int idTiendaTemp;
	int codPromosTemp;
	PromocionesCtrl promoCtrl = new PromocionesCtrl();
	//Realizamos el recorrido de esta promoción con el fin de realizar las actividades por cada promoción
	for(int i = 0; i < tiendaCodPromos.size(); i++)
	{
		TiendaCodigoPromocional tiendaCodTemp = tiendaCodPromos.get(i);
		idTiendaTemp = tiendaCodTemp.getIdTienda();
		codPromosTemp = 0;
		if(diaActual == 2)
		{
			codPromosTemp = tiendaCodTemp.getLunClientes();
		}else if(diaActual == 3)
		{
			codPromosTemp = tiendaCodTemp.getMarClientes();
		}else if(diaActual == 4)
		{
			codPromosTemp = tiendaCodTemp.getMieClientes();
		}else if(diaActual == 5)
		{
			codPromosTemp = tiendaCodTemp.getJueClientes();
		}else if(diaActual == 6)
		{
			codPromosTemp = tiendaCodTemp.getVieClientes();
		}else if(diaActual == 7)
		{
			codPromosTemp = tiendaCodTemp.getSabClientes();
		}else if(diaActual == 1)
		{
			codPromosTemp = tiendaCodTemp.getDomClientes();
		}
		//Posteriormente realizamos la consulta para recuperar los clientes que interesarían
		ArrayList clientesPromos = PedidoDAO.obtenerClientesCodPromoTienda(idTiendaTemp, tiendaCodTemp.getFechaInicial(), tiendaCodTemp.getFechaFinal(), codPromosTemp);
		
		//Por cada clientes debemos de realizar una serie de acciones hasta incluso crear la oferta
		for(int j = 0; j < clientesPromos.size(); j++)
		{
			String[] clientePromoTemp =(String[]) clientesPromos.get(j);
			//Realizaremos una serie de controles con los datos, el primero será verificar si no tiene celular
			// pero lo tiene en el campo de telefono, en cuyo caso realizaremos una actualización para llevarlo también
			// telefono celular
			if(clientePromoTemp[2].equals(new String("")))
			{
				if((clientePromoTemp[1].substring(0, 1).equals(new String("3"))) && (clientePromoTemp[1].length() == 10))
				{
					ClienteDAO.actualizarTelCelularCliente(Integer.parseInt(clientePromoTemp[0]), clientePromoTemp[1]);
				}
			}
			//Crearemos la oferta para el cliente
			OfertaCliente ofertaCli = new OfertaCliente(0,tiendaCodTemp.getIdOferta(), Integer.parseInt(clientePromoTemp[0]), "", 0, "", "", "CODIGO PROMOCIONAL AUTOMATICO", "AUTOMATICO");
			
			promoCtrl.insertarOfertaCliente(ofertaCli);
		}
		promoCtrl.enviarMensajesOferta(tiendaCodTemp.getIdOferta());
		respuesta = respuesta + " <p> Id Tienda enviada " + tiendaCodTemp.getIdTienda() + " EXITOSO con " + codPromosTemp + " códigos." + " </p>";
	}
	//Realizamos el envío del correo electrónico con los archivos
	Correo correo = new Correo();
	correo.setAsunto("GENERACIÓN AUTOMÁTICO CÓDIGOS PROMOCIONALES " + fechaActual.toString());
	CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
	correo.setContrasena(infoCorreo.getClaveCorreo());
	//Tendremos que definir los destinatarios de este correo
	ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPGENERACIONPROMOCIONES");
	correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
	String mensaje = "A continuación se información del proceso de replica de consumo de tiendas " + respuesta;
	correo.setMensaje(mensaje);
	ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
	contro.enviarCorreoHTML();
}


}




