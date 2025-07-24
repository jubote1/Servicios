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
import CapaDAOSer.VarianzaResumenDAO;
import CapaDAOSer.VarianzaResumenHistoricoDAO;
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
import ModeloSer.VarianzaResumen;
import capaDAOPOS.DatafonoCierreDAO;
import capaModeloPOS.DatafonoCierre;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioDiarioReplicaVarianza {
	
			
		
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
			//Al objeto calendario le fijamos la fecha actual del sitema
			calendarioActual.setTime(datFechaActual);
			
		}catch(Exception e)
		{
			System.out.println(e.toString());
		}
		calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
		datFechaActual = calendarioActual.getTime();
		
		//Comenzamos a traer la fecha actual como un String
		fechaActual = dateFormat.format(datFechaActual);
		//El proceso correra  las 11:50 pm
		
		
		String respuesta = "";
		
		//Recuperaremos las tiendas y empezaremos a ir consultando una a una las tiendas para extraer la informaci�n
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		ArrayList detallePedido;
		String[] fila;
		VarianzaResumen varTemp;
		for(Tienda tien : tiendas)
		{
			if(!tien.getHostBD().equals(new String("")))
			{
				ArrayList<VarianzaResumen> varResumen = VarianzaResumenDAO.consultarVarianzaResumen(tien.getHostBD(), fechaActual);
				for(int i = 0; i  < varResumen.size(); i++)
				{
					varTemp = varResumen.get(i);
					VarianzaResumenHistoricoDAO.insertarVarianzaResumenHistorico(varTemp, tien.getIdTienda());
				}
			}
		}
			
		
	}
	
}

