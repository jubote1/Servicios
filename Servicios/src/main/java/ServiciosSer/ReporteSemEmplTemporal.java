package ServiciosSer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.EmpleadoTemporalDiaDatamartDAO;
import CapaDAOSer.EmpresaTemporalDAO;
import CapaDAOSer.GastoEmpleadoTemporalDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.DiaFestivo;
import ModeloSer.EmpresaTemporal;
import ModeloSer.GastoEmpleadoTemporal;
import ModeloSer.Tienda;
import utilidadesSer.CorreoEmpleadoTemporal;
import utilidadesSer.CorreoEmpleadoTemporal.FilaEmpleado;
import utilidadesSer.CorreoEmpleadoTemporal.FilaEmpresa;
import utilidadesSer.CorreoEmpleadoTemporal.FilaTienda;
import utilidadesSer.ControladorEnvioCorreo;

/**
 * Reporte semanal de personal temporal: cuanto se le debe a cada empresa
 * temporal, tienda por tienda, con el detalle dia a dia de cada persona.
 *
 * El calculo (horas, tarifa dominical/festiva, cantidad de pedidos) no
 * cambio; lo unico que cambio es que el correo ahora lo arma
 * utilidadesSer.CorreoEmpleadoTemporal con el estilo de la casa, en vez de
 * una tabla HTML armada a mano.
 */
public class ReporteSemEmplTemporal {

	public static void main(final String[] args) {
		// Requerimos primero que todo obtener el rango de fechas con el fin de tener dicho rango para las consultas
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		final SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String fechaActual = "";
		final Date datFechaActual = new Date();
		fechaActual = dateFormat.format(datFechaActual);

		Date datFechaAnterior;
		String fechaAnterior = "";
		final Calendar calendarioActual = Calendar.getInstance();
		final Calendar calendarioTrans = Calendar.getInstance();
		try {
			fechaActual = dateFormat.format(calendarioActual.getTime());
		} catch (final Exception exc) {
			System.out.println(exc.toString());
		}
		try {
			calendarioActual.setTime(dateFormat.parse(fechaActual));
		} catch (final Exception e) {
			System.out.println(e.toString());
		}
		int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);

		// Domingo
		if (diaActual == 1) {
			calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
		} else if (diaActual == 2) {
			calendarioActual.add(Calendar.DAY_OF_YEAR, -7);
		} else if (diaActual == 3) {
			calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
		} else if (diaActual == 4) {
			calendarioActual.add(Calendar.DAY_OF_YEAR, -2);
		} else if (diaActual == 5) {
			calendarioActual.add(Calendar.DAY_OF_YEAR, -3);
		} else if (diaActual == 6) {
			calendarioActual.add(Calendar.DAY_OF_YEAR, -4);
		} else if (diaActual == 7) {
			calendarioActual.add(Calendar.DAY_OF_YEAR, -5);
		}
		datFechaAnterior = calendarioActual.getTime();
		fechaAnterior = dateFormat.format(datFechaAnterior);

		final ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		final ArrayList<EmpresaTemporal> empresasTemp = EmpresaTemporalDAO.retornarEmpresasTemporales();
		final ArrayList<DiaFestivo> festivos = GeneralDAO.obtenerDiasFestivos();
		double valorHoraNormal;
		double valorHoraDominical;
		double horasTrabajadas = 0;
		double valorHoraTrabajada = 0;
		boolean errorConversion = false;
		boolean esDomingo = false;
		boolean esFestivo = false;

		final ArrayList<FilaTienda> tiendasReporte = new ArrayList<FilaTienda>();

		for (final Tienda tien : tiendas) {
			double totalTienda = 0;
			if (!tien.getHostBD().equals(new String(""))) {
				/**
				 * Incluiremos el rescate de toda la información de la tienda en esta semana y llevada a la tabla datamart
				 */
				final ArrayList<capaModeloPOS.EmpleadoTemporalDia> empleadosTempTotal = capaDAOPOS.EmpleadoTemporalDiaDAO
						.obtenerEmpleadoTemporalEmpresasFecha(fechaActual, fechaAnterior, tien.getHostBD());

				final boolean hayRegistros = EmpleadoTemporalDiaDatamartDAO
						.validarInsercionEmpleadoTemporalDiaDatamart(fechaAnterior, fechaActual, tien.getIdTienda());
				if (!hayRegistros) {
					for (final capaModeloPOS.EmpleadoTemporalDia empTodos : empleadosTempTotal) {
						EmpleadoTemporalDiaDatamartDAO.insertarEmpleadoTemporalDiaDatamart(empTodos,
								tien.getIdTienda());
					}
				}

				final FilaTienda filaTienda = new FilaTienda();
				filaTienda.nombreTienda = tien.getNombreTienda();

				for (final EmpresaTemporal empTemp : empresasTemp) {
					double totalEmpresa = 0;
					valorHoraNormal = empTemp.getValorHoraNormal();
					valorHoraDominical = empTemp.getValorHoraDominical();

					final FilaEmpresa filaEmpresa = new FilaEmpresa();
					filaEmpresa.nombreEmpresa = empTemp.getNombreEmpresa();
					filaEmpresa.valorHoraNormal = valorHoraNormal;
					filaEmpresa.valorHoraDominical = valorHoraDominical;

					final ArrayList<capaModeloPOS.EmpleadoTemporalDia> empleadosTempDia = capaDAOPOS.EmpleadoTemporalDiaDAO
							.obtenerEmpleadoTemporalFecha(fechaActual, fechaAnterior, empTemp.getIdEmpresa(),
									tien.getHostBD());
					for (final capaModeloPOS.EmpleadoTemporalDia empleadoTemp : empleadosTempDia) {
						errorConversion = false;
						esDomingo = false;
						esFestivo = false;
						diaActual = 0;
						try {
							final Date fechaIng = dateFormatHora
									.parse(empleadoTemp.getFechaSistema() + " " + empleadoTemp.getHoraIngreso());
							Date fechaSal = dateFormatHora
									.parse(empleadoTemp.getFechaSistema() + " " + empleadoTemp.getHoraSalida());
							final String hora = empleadoTemp.getHoraSalida().substring(0, 2);
							int intHora = 0;
							try {
								intHora = Integer.parseInt(hora);
							} catch (final Exception e) {
								intHora = 99;
							}
							if (intHora == 0) {
								calendarioTrans.setTime(dateFormat.parse(empleadoTemp.getFechaSistema()));
								calendarioTrans.add(Calendar.DAY_OF_YEAR, 1);
								fechaSal = dateFormatHora.parse(
										dateFormat.format(calendarioTrans.getTime()) + " " + empleadoTemp.getHoraSalida());
								final ArrayList correos = GeneralDAO.obtenerCorreosParametro("ERRORREPLICAINV");
								final Correo correo = new Correo();
								correo.setAsunto("OJO POSIBLE ERROR REPORTE EMPLEADO TEMPORALES" + fechaAnterior
										+ " AL " + fechaActual);
								correo.setContrasena("Pizzaamericana2017");
								correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
								correo.setMensaje(" Hay un posible error en el registro de empleados temporales "
										+ empleadoTemp.getNombre() + " " + empleadoTemp.getFechaSistema());
								final ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
								contro.enviarCorreoHTML();
							}
							horasTrabajadas = (fechaSal.getTime() - fechaIng.getTime()) / 1000;
							horasTrabajadas = horasTrabajadas / 3600;
							calendarioActual.setTime(fechaIng);
							diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
							if (diaActual == 1) {
								esDomingo = true;
							}
							esFestivo = validarFestivo(festivos, empleadoTemp.getFechaSistema());
							if (esFestivo) {
								esDomingo = true;
							}
						} catch (final Exception e) {
							errorConversion = true;
						}
						if (esDomingo) {
							valorHoraTrabajada = horasTrabajadas * valorHoraDominical;
						} else {
							valorHoraTrabajada = horasTrabajadas * valorHoraNormal;
						}
						totalEmpresa = totalEmpresa + valorHoraTrabajada;
						final int cantidadPedidos = PedidoDAO.obtenerPedidosEntregados(
								empleadoTemp.getFechaSistema() + " " + empleadoTemp.getHoraIngreso(),
								empleadoTemp.getFechaSistema() + " " + empleadoTemp.getHoraSalida(),
								empleadoTemp.getId(), tien.getHostBD());

						final FilaEmpleado dia = new FilaEmpleado();
						dia.nombre = empleadoTemp.getNombre();
						dia.fecha = empleadoTemp.getFechaSistema();
						dia.horaIngreso = empleadoTemp.getHoraIngreso();
						dia.horaSalida = empleadoTemp.getHoraSalida();
						dia.observacion = empleadoTemp.getObservacion();
						dia.pedidos = cantidadPedidos;
						dia.errorConversion = errorConversion;
						if (!errorConversion) {
							dia.horas = horasTrabajadas;
							dia.valor = valorHoraTrabajada;
							dia.promedio = horasTrabajadas == 0 ? 0 : cantidadPedidos / horasTrabajadas;
							dia.etiquetaDia = diaActual == 1 ? "Domingo" : (esFestivo ? "Festivo" : "");
						}
						filaEmpresa.dias.add(dia);
					}
					filaEmpresa.total = totalEmpresa;
					filaTienda.empresas.add(filaEmpresa);
					totalTienda = totalTienda + totalEmpresa;
				}
				tiendasReporte.add(filaTienda);
			}
			// Realizamos la inserción del total para la tienda
			final GastoEmpleadoTemporal gastEmpTem = new GastoEmpleadoTemporal(tien.getIdTienda(), fechaActual,
					totalTienda);
			GastoEmpleadoTemporalDAO.insertarGastoEmpresaTemporal(gastEmpTem);
		}

		double totalPeriodo = 0;
		for (int i = 0; i < tiendasReporte.size(); i++) {
			totalPeriodo = totalPeriodo + tiendasReporte.get(i).total();
		}

		final ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPSEMEMPLTEMPORAL");
		final Correo correo = new Correo();
		final CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES",
				"CLAVECORREOREPORTE");
		correo.setAsunto(CorreoEmpleadoTemporal.asunto(false, fechaAnterior, fechaActual, totalPeriodo));
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje(CorreoEmpleadoTemporal.cuerpo(tiendasReporte, false, true, fechaAnterior, fechaActual));
		final ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}

	public static boolean validarFestivo(final ArrayList<DiaFestivo> festivos, final String fechaActual) {
		DiaFestivo festivoTemp = new DiaFestivo(0, "");
		boolean respuesta = false;
		for (int i = 0; i < festivos.size(); i++) {
			festivoTemp = festivos.get(i);
			if (festivoTemp.getFechaFestiva().equals(fechaActual)) {
				respuesta = true;
				break;
			}
		}
		return respuesta;
	}

}
