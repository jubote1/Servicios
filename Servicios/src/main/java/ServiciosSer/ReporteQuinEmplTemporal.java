package ServiciosSer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.EmpresaTemporalDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.TiendaDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.DiaFestivo;
import ModeloSer.EmpresaTemporal;
import ModeloSer.Tienda;
import utilidadesSer.CorreoEmpleadoTemporal;
import utilidadesSer.CorreoEmpleadoTemporal.FilaEmpleado;
import utilidadesSer.CorreoEmpleadoTemporal.FilaEmpresa;
import utilidadesSer.CorreoEmpleadoTemporal.FilaTienda;
import utilidadesSer.ControladorEnvioCorreo;

/**
 * Reporte quincenal de personal temporal: igual proposito que el semanal,
 * pero por quincena y con tarifa propia de sabado (el semanal no la usa).
 *
 * El calculo no cambio; el correo ahora lo arma
 * utilidadesSer.CorreoEmpleadoTemporal con el estilo de la casa.
 */
public class ReporteQuinEmplTemporal {

	public static void main(final String[] args) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		final SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String fechaActual = "";
		final Date datFechaActual = new Date();
		fechaActual = dateFormat.format(datFechaActual);
		int mesActual = 0;
		int diaActual = 0;
		int anoActual = 0;
		String fechaAnterior = "";
		final Calendar calendarioActual = Calendar.getInstance();
		final Calendar calendarioTrans = Calendar.getInstance();
		try {
			fechaActual = dateFormat.format(calendarioActual.getTime());
			mesActual = calendarioActual.get(Calendar.MONTH) + 1;
			diaActual = calendarioActual.get(Calendar.DAY_OF_MONTH);
			anoActual = calendarioActual.get(Calendar.YEAR);
		} catch (final Exception exc) {
			System.out.println(exc.toString());
		}
		try {
			calendarioActual.setTime(dateFormat.parse(fechaActual));
		} catch (final Exception e) {
			System.out.println(e.toString());
		}
		// Procedemos a tener la lógica para fijar la fecha anterior
		if (diaActual >= 1 && diaActual <= 15) {
			diaActual = 16;
			if (mesActual == 1) {
				mesActual = 12;
				anoActual = anoActual - 1;
			} else {
				mesActual = mesActual - 1;
			}
		} else if (diaActual > 15 && diaActual <= 31) {
			diaActual = 1;
		}
		fechaAnterior = anoActual + "-" + mesActual + "-" + diaActual;

		final ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		final ArrayList<EmpresaTemporal> empresasTemp = EmpresaTemporalDAO.retornarEmpresasTemporales();
		final ArrayList<DiaFestivo> festivos = GeneralDAO.obtenerDiasFestivos();
		double valorHoraNormal;
		double valorHoraDominical;
		double valorHoraSabado;
		double horasTrabajadas = 0;
		double valorHoraTrabajada = 0;
		boolean errorConversion = false;
		boolean esDomingo = false;
		boolean esFestivo = false;
		boolean esSabado = false;
		int diaActualSemana = 0;

		final ArrayList<FilaTienda> tiendasReporte = new ArrayList<FilaTienda>();

		for (final Tienda tien : tiendas) {
			if (!tien.getHostBD().equals(new String(""))) {
				final FilaTienda filaTienda = new FilaTienda();
				filaTienda.nombreTienda = tien.getNombreTienda();

				for (final EmpresaTemporal empTemp : empresasTemp) {
					double totalEmpresa = 0;
					valorHoraNormal = empTemp.getValorHoraNormal();
					valorHoraDominical = empTemp.getValorHoraDominical();
					valorHoraSabado = empTemp.getValorHoraSabado();

					final FilaEmpresa filaEmpresa = new FilaEmpresa();
					filaEmpresa.nombreEmpresa = empTemp.getNombreEmpresa();
					filaEmpresa.valorHoraNormal = valorHoraNormal;
					filaEmpresa.valorHoraDominical = valorHoraDominical;
					filaEmpresa.valorHoraSabado = valorHoraSabado;

					final ArrayList<capaModeloPOS.EmpleadoTemporalDia> empleadosTempDia = capaDAOPOS.EmpleadoTemporalDiaDAO
							.obtenerEmpleadoTemporalFecha(fechaActual, fechaAnterior, empTemp.getIdEmpresa(),
									tien.getHostBD());
					for (final capaModeloPOS.EmpleadoTemporalDia empleadoTemp : empleadosTempDia) {
						errorConversion = false;
						esDomingo = false;
						esFestivo = false;
						esSabado = false;
						diaActualSemana = 0;
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
								final CorreoElectronico infoCorreoError = ControladorEnvioCorreo
										.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
								correo.setAsunto("OJO POSIBLE ERROR REPORTE EMPLEADO TEMPORALES" + fechaAnterior
										+ " AL " + fechaActual);
								correo.setContrasena(infoCorreoError.getClaveCorreo());
								correo.setUsuarioCorreo(infoCorreoError.getClaveCorreo());
								correo.setMensaje(" Hay un posible error en el registro de empleados temporales "
										+ empleadoTemp.getNombre() + " " + empleadoTemp.getFechaSistema());
								final ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
								contro.enviarCorreoHTML();
							}
							horasTrabajadas = (fechaSal.getTime() - fechaIng.getTime()) / 1000;
							horasTrabajadas = horasTrabajadas / 3600;
							calendarioActual.setTime(fechaIng);
							diaActualSemana = calendarioActual.get(Calendar.DAY_OF_WEEK);
							if (diaActualSemana == 1) {
								esDomingo = true;
							} else if (diaActualSemana == 7) {
								esSabado = true;
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
						} else if (esSabado) {
							valorHoraTrabajada = horasTrabajadas * valorHoraSabado;
						} else {
							valorHoraTrabajada = horasTrabajadas * valorHoraNormal;
						}
						totalEmpresa = totalEmpresa + valorHoraTrabajada;

						final FilaEmpleado dia = new FilaEmpleado();
						dia.nombre = empleadoTemp.getNombre();
						dia.fecha = empleadoTemp.getFechaSistema();
						dia.horaIngreso = empleadoTemp.getHoraIngreso();
						dia.horaSalida = empleadoTemp.getHoraSalida();
						dia.observacion = empleadoTemp.getObservacion();
						dia.errorConversion = errorConversion;
						if (!errorConversion) {
							dia.horas = horasTrabajadas;
							dia.valor = valorHoraTrabajada;
							dia.etiquetaDia = esDomingo && diaActualSemana == 1 ? "Domingo"
									: (esFestivo ? "Festivo" : (esSabado ? "Sabado" : ""));
						}
						filaEmpresa.dias.add(dia);
					}
					filaEmpresa.total = totalEmpresa;
					filaTienda.empresas.add(filaEmpresa);
				}
				tiendasReporte.add(filaTienda);
			}
		}

		double totalPeriodo = 0;
		for (int i = 0; i < tiendasReporte.size(); i++) {
			totalPeriodo = totalPeriodo + tiendasReporte.get(i).total();
		}

		final ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPQUINPLTEMPORAL");
		final Correo correo = new Correo();
		final CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES",
				"CLAVECORREOREPORTE");
		correo.setAsunto(CorreoEmpleadoTemporal.asunto(true, fechaAnterior, fechaActual, totalPeriodo));
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje(CorreoEmpleadoTemporal.cuerpo(tiendasReporte, true, false, fechaAnterior, fechaActual));
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
