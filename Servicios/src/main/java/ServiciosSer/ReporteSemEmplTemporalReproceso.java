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
import CapaDAOSer.GastoEmpleadoTemporalDAO;
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
import ModeloSer.GastoEmpleadoTemporal;
import ModeloSer.Pedido;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteSemEmplTemporalReproceso {
	
			
		
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
		
		//Variables donde manejaremos la fecha anerior con el fin realizar el rango de los facturado por empleados temporales
		Date datFechaAnterior;
		String fechaAnterior = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		Calendar calendarioTrans = Calendar.getInstance();
		//Obtenemos la fecha Actual
		try
		{
			//OJO
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
		//Retormanos el d�a de la semana actual segun la fecha del calendario
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
		//Llevamos a un string la fecha anterior para el c�lculo de la venta
		datFechaAnterior = calendarioActual.getTime();
		fechaAnterior = dateFormat.format(datFechaAnterior);
		
		String respuesta = "";
		
		//Recuperaremos las tiendas y empezaremos a ir consultando una a una las tiendas para extraer la informaci�n
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		//Recuperamos las empresas temporales de la base de datos general
		ArrayList<EmpresaTemporal> empresasTemp = EmpresaTemporalDAO.retornarEmpresasTemporales();
		//Se recuperan los d�as festivos
		ArrayList<DiaFestivo> festivos = GeneralDAO.obtenerDiasFestivos();
		double valorHoraNormal;
		double valorHoraDominical;
		double totalEmpresa = 0;
		double horasTrabajadas = 0;
		double valorHoraTrabajada = 0;
		boolean errorConversion = false;
		boolean esDomingo = false;
		double totalTienda;
		for(Tienda tien : tiendas)
		{
			totalTienda = 0;
			if(!tien.getHostBD().equals(new String("")))
			{
				for(EmpresaTemporal empTemp: empresasTemp)
				{
					totalEmpresa = 0;
					valorHoraNormal = empTemp.getValorHoraNormal();
					valorHoraDominical = empTemp.getValorHoraDominical();
					//Creamos el encabezado para tienda y empresa
					respuesta = respuesta + "<table border='2'> <tr><td colspan ='6'>" + tien.getNombreTienda() + " - " + empTemp.getNombreEmpresa() + "-" + empTemp.getValorHoraNormal() + "-" + empTemp.getValorHoraDominical() + "</td></tr>";
					respuesta = respuesta + "<tr>"
							+  "<td><strong>Personal</strong></td>"
							+  "<td><strong>Fecha</strong></td>"
							+  "<td><strong>Hora Ingreso</strong></td>"
							+  "<td><strong>Hora Salida</strong></td>"
							+  "<td><strong>Horas Trabajadas</strong></td>"
							+  "<td><strong>Valor Pagar</strong></td>"
							+  "<td><strong>Observacion</strong></td>"
							+  "<td><strong># Pedidos</strong></td>"
							+  "</tr>";
					//Recuperamos los evento de empleados para la semana en cuesti�n
					ArrayList<capaModeloPOS.EmpleadoTemporalDia> empleadosTempDia = capaDAOPOS.EmpleadoTemporalDiaDAO.obtenerEmpleadoTemporalFecha(fechaActual, fechaAnterior, empTemp.getIdEmpresa(), tien.getHostBD());
					//Comenzamos a recorrer para ir presetnando la informaci�n
					for(capaModeloPOS.EmpleadoTemporalDia empleadoTemp : empleadosTempDia)
					{
						//Calculamos la cantidad de horas trabajadas
						//Intentamos realizar la conversi�n de las horas
						errorConversion = false;
						esDomingo = false;
						diaActual = 0;
						try
						{
							//Formateamos las fechas para posteriormente proceder a calcular el n�mero de horas trabajadas
							Date fechaIng = dateFormatHora.parse(empleadoTemp.getFechaSistema()+" "+empleadoTemp.getHoraIngreso());
							Date fechaSal = dateFormatHora.parse(empleadoTemp.getFechaSistema()+" "+empleadoTemp.getHoraSalida());
							//Pondremos un control por si hay error en la hora de salida del empleado temporal
							String hora = empleadoTemp.getHoraSalida().substring(0, 2);
							int intHora = 0;
							try
							{
								intHora = Integer.parseInt(hora);
							}catch(Exception e)
							{
								intHora  = 99;
							}
							//Si la hora es cero deberemos de sumar un d�a a la fechaSistema
							if(intHora == 0)
							{
								calendarioTrans.setTime(dateFormat.parse(empleadoTemp.getFechaSistema()));
								calendarioTrans.add(Calendar.DAY_OF_YEAR, 1);
								fechaSal = dateFormatHora.parse(dateFormat.format(calendarioTrans.getTime())+" "+empleadoTemp.getHoraSalida());
								//Realizaremos el env�o de un correo para notificar est� situaci�n
								//Recuperar la lista de distribuci�n para este correo
								ArrayList correos = GeneralDAO.obtenerCorreosParametro("ERRORREPLICAINV");
								Correo correo = new Correo();
								correo.setAsunto("OJO POSIBLE ERROR REPORTE EMPLEADO TEMPORALES" + fechaAnterior + " AL " + fechaActual);
								correo.setContrasena("Pizzaamericana2017");
								correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
								correo.setMensaje(" Hay un posible error en el registro de empleados temporales "+ empleadoTemp.getNombre() + " " + empleadoTemp.getFechaSistema());
								ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
								contro.enviarCorreoHTML();
							}
							horasTrabajadas = ((fechaSal.getTime()-fechaIng.getTime())/1000);
							horasTrabajadas =(horasTrabajadas)/3600;
							//Fijar la fecha en el calendario para posteriormente saber si es domingo o no
							calendarioActual.setTime(fechaIng);
							diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
							//En caso de ser domingo debemos de prender un indicador que nos servir� para saber el valor de la hora
							if(diaActual ==  1)
							{
								esDomingo = true;
							}
							//Luego de la validaci�n de si es domingo hacemos la validaci�n de si es festivo
							boolean esFestivo = validarFestivo(festivos, empleadoTemp.getFechaSistema());
							if(esFestivo)
							{
								esDomingo = true;
							}
						}catch(Exception e)
						{
							errorConversion = true;
						}
						//En caso de ser domingo se hace c�lculo con la hora dominicial
						if(esDomingo)
						{
							valorHoraTrabajada = horasTrabajadas * valorHoraDominical;
						}else
						{
							valorHoraTrabajada = horasTrabajadas * valorHoraNormal;
						}
						//Se acumula el total de la empresa
						totalEmpresa  = totalEmpresa  + valorHoraTrabajada;
						//Incluimos el c�lculo de la cantidad de pedidos del domiciliario
						int cantidadPedidos = PedidoDAO.obtenerPedidosEntregados(empleadoTemp.getFechaSistema()+" "+empleadoTemp.getHoraIngreso(), empleadoTemp.getFechaSistema()+" "+empleadoTemp.getHoraSalida(), empleadoTemp.getId(), tien.getHostBD());
						//Si hay error de conversi�n de las fechas se muestra diferente.
						if(errorConversion)
						{
							respuesta = respuesta + "<tr>"
									+  "<td>" + empleadoTemp.getNombre() + "</td>"
									+  "<td>" + empleadoTemp.getFechaSistema() + "</td>"
									+  "<td>" + empleadoTemp.getHoraIngreso() + "</td>"
									+  "<td>" + empleadoTemp.getHoraSalida() + "</td>"
									+  "<td>" + "ERROR CONVERSION" + "</td>"
									+  "<td>" + "0" + "</td>"
									+  "<td>" + empleadoTemp.getObservacion() + "</td>"
									+  "<td>" + cantidadPedidos + "</td>"
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
									+  "<td>" + empleadoTemp.getObservacion() + "</td>"
									+  "<td>" + cantidadPedidos + "</td>"
									+  "</tr>";
						}
					}
					respuesta = respuesta + "<tr><td colspan ='6'>  TOTAL " + formatea.format(totalEmpresa) + "</td></tr>";
					respuesta = respuesta + "</table> <br/>";
					//Acumulamos el total para la tienda
					totalTienda = totalTienda + totalEmpresa;
				}
			}
			//Realizamos la inserci�n del total para la tienda
			GastoEmpleadoTemporal gastEmpTem = new GastoEmpleadoTemporal(tien.getIdTienda(),fechaActual, totalTienda);
			GastoEmpleadoTemporalDAO.insertarGastoEmpresaTemporal(gastEmpTem);
		}
			//Recuperar la lista de distribuci�n para este correo
			ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPSEMEMPLTEMPORAL");
			Correo correo = new Correo();
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("REPORTE SEMANAL PERSONAL TEMPORAL-" + fechaAnterior + " AL " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuaci�n el resumen de la semana de personal temporal desde la fecha "+ fechaAnterior + " a la fecha " + fechaActual +": \n" + respuesta);
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

