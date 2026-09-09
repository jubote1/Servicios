package utilidadesSer;

import java.util.ArrayList;

import ModeloSer.EmpleadoEvento;
import ModeloSer.NovedadBiometria;

/**
 * Empareja los eventos de entrada y salida del huellero.
 *
 * Vive aparte del proceso que manda los correos por dos razones: no depende de
 * nada -ni de base de datos ni de correo-, asi que se puede probar sola; y el
 * emparejamiento lo van a necesitar tambien la pantalla del central y la del POS.
 */
public class EmparejadorBiometria {
	/**
	 * Empareja los eventos de cada empleado en cada dia y devuelve los que quedaron
	 * sin pareja, mas los ingresos a hora tardia.
	 *
	 * Soporta VARIOS pares en un mismo dia -en bodega es lo normal-: cada ingreso se
	 * cierra con la salida que le sigue y solo se reporta el que se queda abierto.
	 *
	 * Lo que hacia antes y por que se cambio:
	 *  - Preguntaba solo por la salida, asi que quien olvidaba el INGRESO no aparecia.
	 *  - Usaba una bandera que, una vez veia una salida, se quedaba encendida: por eso
	 *    el 8 de septiembre no reporto a quien marco ingreso, ingreso y salida.
	 *  - Reportaba el PRIMER evento del dia y no el que quedo sin pareja, asi que
	 *    mandaba al empleado a revisar la hora equivocada.
	 *  - La verificacion ocurria solo al cambiar de empleado y nunca al final, asi que
	 *    el ultimo empleado de la lista no se revisaba.
	 */
	public static ArrayList<NovedadBiometria> detectarNovedades(final ArrayList<EmpleadoEvento> eventos,
			final int horaTardia) {
		final ArrayList<NovedadBiometria> novedades = new ArrayList<NovedadBiometria>();
		if (eventos == null) {
			return(novedades);
		}
		EmpleadoEvento ingresoAbierto = null;
		int idCorte = 0;
		String fechaCorte = "";
		for (int i = 0; i < eventos.size(); i++) {
			final EmpleadoEvento evento = eventos.get(i);
			if (evento == null || evento.getTipoEvento() == null) {
				continue;
			}
			final String fechaEvento = (evento.getFecha() == null) ? "" : evento.getFecha();
			//Se reinicia por empleado Y por dia: una jornada no se arrastra al dia siguiente.
			if (evento.getId() != idCorte || !fechaEvento.equals(fechaCorte)) {
				if (ingresoAbierto != null) {
					novedades.add(crearNovedad(ingresoAbierto, NovedadBiometria.FALTA_SALIDA));
				}
				ingresoAbierto = null;
				idCorte = evento.getId();
				fechaCorte = fechaEvento;
			}
			if (evento.getTipoEvento().equals("INGRESO")) {
				//Dos ingresos seguidos: al primero le falto la salida.
				if (ingresoAbierto != null) {
					novedades.add(crearNovedad(ingresoAbierto, NovedadBiometria.FALTA_SALIDA));
				}
				ingresoAbierto = evento;
				final NovedadBiometria tardio = crearNovedad(evento, NovedadBiometria.INGRESO_TARDIO);
				if (tardio.getHoraNumero() >= horaTardia) {
					novedades.add(tardio);
				}
			} else if (evento.getTipoEvento().equals("SALIDA")) {
				if (ingresoAbierto == null) {
					novedades.add(crearNovedad(evento, NovedadBiometria.FALTA_INGRESO));
				} else {
					//Pareja completa: no hay novedad.
					ingresoAbierto = null;
				}
			}
		}
		//El ultimo empleado de la lista tambien se revisa.
		if (ingresoAbierto != null) {
			novedades.add(crearNovedad(ingresoAbierto, NovedadBiometria.FALTA_SALIDA));
		}
		return(novedades);
	}

	private static NovedadBiometria crearNovedad(final EmpleadoEvento evento, final String tipoNovedad) {
		return(new NovedadBiometria(evento.getId(), evento.getNombreEmpleado(), evento.getFecha(),
				evento.getDia(), evento.getIdTienda(), tipoNovedad, evento.getTipoEvento(),
				evento.getFechaHoraLog()));
	}
}
