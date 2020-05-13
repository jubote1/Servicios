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
import ModeloSer.DiaFestivo;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.EmpleadoTemporalDia;
import ModeloSer.EmpresaTemporal;
import ModeloSer.Pedido;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteQuinEmplTemporal {
	
			
		
	public static void main( String[] args )
	        
	{
		//Requerimos primero que todo obtener el rango de fechas con el fin de tener dicho rango para las consultas
		//Definimos el formato como manejaremos las fechas
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DecimalFormat formatea = new DecimalFormat("###,###.##");
		//Traemos la fechaActual en Blanco
		String fechaActual = "";
		//Traemos la fecha actual en un date
		Date datFechaActual = new Date();
		//Comenzamos a traer la fecha actual como un String
		fechaActual = dateFormat.format(datFechaActual);
		//Tomamos estas dos variables que nos ayudarán a fijar la fecha anterior para las operaciones
		int mesActual = 0;
		int diaActual = 0;
		int anoActual = 0;
		//Variables donde manejaremos la fecha anerior con el fin realizar el rango de los facturado por empleados temporales
		Date datFechaAnterior;
		String fechaAnterior = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		//Obtenemos la fecha Actual
		try
		{
			//OJO
			fechaActual = dateFormat.format(calendarioActual.getTime());
			//Fijamos los números para el mes actual y dia actual del mes
			mesActual = calendarioActual.get(Calendar.MONTH) + 1;
			diaActual = calendarioActual.get(Calendar.DAY_OF_MONTH);
			anoActual = calendarioActual.get(Calendar.YEAR);
			//fechaActual = "2019-05-13";
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
		//Procedemos a tener la lógica para fijar la fecha anterior
		if(diaActual >=1 && diaActual<= 15)
		{
			diaActual= 16;
			if(mesActual == 1)
			{
				mesActual = 12;
				anoActual = anoActual -1;
			}else
			{
				mesActual = mesActual -1;
			}
		}
		else if(diaActual > 15 && diaActual <= 31)
		{
			diaActual = 1;
		}
		//Con lo anterior fijamos cual es la quincena a trabajar
		
		
		
		fechaAnterior = anoActual+"-"+mesActual+"-"+diaActual;
		
		//OJO De forma temporal para el reporte CON UNA FECHA TEMPORAL
		//fechaActual = "2020-04-30";
		
		String respuesta = "";
		
		//Recuperaremos las tiendas y empezaremos a ir consultando una a una las tiendas para extraer la información
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		//Recuperamos las empresas temporales de la base de datos general
		ArrayList<EmpresaTemporal> empresasTemp = EmpresaTemporalDAO.retornarEmpresasTemporales();
		//Se recuperan los días festivos
		ArrayList<DiaFestivo> festivos = GeneralDAO.obtenerDiasFestivos();
		double valorHoraNormal;
		double valorHoraDominical;
		double totalEmpresa = 0;
		double horasTrabajadas = 0;
		double valorHoraTrabajada = 0;
		boolean errorConversion = false;
		boolean esDomingo = false;
		int diaActualSemana = 0;
		for(Tienda tien : tiendas)
		{
			
			if(!tien.getHostBD().equals(new String("")))
			{
				for(EmpresaTemporal empTemp: empresasTemp)
				{
					totalEmpresa = 0;
					valorHoraNormal = empTemp.getValorHoraNormal();
					valorHoraDominical = empTemp.getValorHoraDominical();
					//Creamos el encabezado para tienda y empresa
					respuesta = respuesta + "<table border='2'> <tr><td colspan ='6'> QUINCENAL " + tien.getNombreTienda() + " - " + empTemp.getNombreEmpresa() + "-" + empTemp.getValorHoraNormal() + "-" + empTemp.getValorHoraDominical() + "</td></tr>";
					respuesta = respuesta + "<tr>"
							+  "<td><strong>Personal</strong></td>"
							+  "<td><strong>Fecha</strong></td>"
							+  "<td><strong>Hora Ingreso</strong></td>"
							+  "<td><strong>Hora Salida</strong></td>"
							+  "<td><strong>Horas Trabajadas</strong></td>"
							+  "<td><strong>Valor Pagar</strong></td>"
							+  "</tr>";
					//Recuperamos los evento de empleados para la semana en cuestión
					ArrayList<EmpleadoTemporalDia> empleadosTempDia = EmpleadoTemporalDiaDAO.obtenerEmpleadoTemporalFecha(fechaActual, fechaAnterior, empTemp.getIdEmpresa(), tien.getHostBD());
					//Comenzamos a recorrer para ir presetnando la información
					for(EmpleadoTemporalDia empleadoTemp : empleadosTempDia)
					{
						//Calculamos la cantidad de horas trabajadas
						//Intentamos realizar la conversión de las horas
						errorConversion = false;
						esDomingo = false;
						diaActualSemana = 0;
						try
						{
							//Formateamos las fechas para posteriormente proceder a calcular el número de horas trabajadas
							Date fechaIng = dateFormatHora.parse(empleadoTemp.getFechaSistema()+" "+empleadoTemp.getHoraIngreso());
							Date fechaSal = dateFormatHora.parse(empleadoTemp.getFechaSistema()+" "+empleadoTemp.getHoraSalida());
							horasTrabajadas = ((fechaSal.getTime()-fechaIng.getTime())/1000);
							horasTrabajadas =(horasTrabajadas)/3600;
							//Fijar la fecha en el calendario para posteriormente saber si es domingo o no
							calendarioActual.setTime(fechaIng);
							diaActualSemana = calendarioActual.get(Calendar.DAY_OF_WEEK);
							//En caso de ser domingo debemos de prender un indicador que nos servirá para saber el valor de la hora
							if(diaActualSemana ==  1)
							{
								esDomingo = true;
							}
							//Luego de la validación de si es domingo hacemos la validación de si es festivo
							boolean esFestivo = validarFestivo(festivos, empleadoTemp.getFechaSistema());
							if(esFestivo)
							{
								esDomingo = true;
							}
						}catch(Exception e)
						{
							errorConversion = true;
						}
						//En caso de ser domingo se hace cálculo con la hora dominicial
						if(esDomingo)
						{
							valorHoraTrabajada = horasTrabajadas * valorHoraDominical;
						}else
						{
							valorHoraTrabajada = horasTrabajadas * valorHoraNormal;
						}
						//Se acumula el total de la empresa
						totalEmpresa  = totalEmpresa  + valorHoraTrabajada;
						//Si hay error de conversión de las fechas se muestra diferente.
						if(errorConversion)
						{
							respuesta = respuesta + "<tr>"
									+  "<td>" + empleadoTemp.getNombre() + "</td>"
									+  "<td>" + empleadoTemp.getFechaSistema() + "</td>"
									+  "<td>" + empleadoTemp.getHoraIngreso() + "</td>"
									+  "<td>" + empleadoTemp.getHoraSalida() + "</td>"
									+  "<td>" + "ERROR CONVERSION" + "</td>"
									+  "<td>" + "0" + "</td>"
									+  "</tr>";
						}else
						{
							respuesta = respuesta + "<tr>"
									+  "<td>" + empleadoTemp.getNombre() + "</td>"
									+  "<td>" + empleadoTemp.getFechaSistema() + "</td>"
									+  "<td>" + empleadoTemp.getHoraIngreso() + "</td>"
									+  "<td>" + empleadoTemp.getHoraSalida() + "</td>"
									+  "<td>" + formatea.format(horasTrabajadas) + "</td>"
									+  "<td>" + formatea.format(valorHoraTrabajada) + "</td>"
									+  "</tr>";
						}
					}
					respuesta = respuesta + "<tr><td colspan ='6'>  TOTAL " + formatea.format(totalEmpresa) + "</td></tr>";
					respuesta = respuesta + "</table> <br/>";
				}
			}
		}
			//Recuperar la lista de distribución para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPQUINPLTEMPORAL");
			Correo correo = new Correo();
			correo.setAsunto("REPORTE QUINCENAL PERSONAL TEMPORAL-" + fechaAnterior + " AL " + fechaActual);
			correo.setContrasena("Pizzaamericana2017");
			correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
			correo.setMensaje("A continuación el resumen quincenal de personal temporal desde la fecha "+ fechaAnterior + " a la fecha " + fechaActual +": \n" + respuesta);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		
	}
		
	public static boolean validarFestivo(ArrayList<DiaFestivo> festivos, String fechaActual )
	{
		DiaFestivo festivoTemp = new DiaFestivo(0,"");
		boolean respuesta = false;
		for(int i = 0; i < festivos.size(); i++)
		{
			festivoTemp = festivos.get(i);
			if(festivoTemp.getFechaFestiva().equals(fechaActual))
			{
				respuesta = true;
				break;
			}
		}
		return(respuesta);
	}
	
}

