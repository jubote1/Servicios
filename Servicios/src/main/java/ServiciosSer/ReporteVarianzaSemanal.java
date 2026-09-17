package ServiciosSer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.VarianzaSemanalDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import utilidadesSer.ControladorEnvioCorreo;
import utilidadesSer.CorreoVarianzaSemanal;

/**
 * Publica los domingos cuanta plata se perdio en varianza de inventario.
 *
 * POR QUE EXISTE
 *
 * La varianza se viene ingresando en las tiendas todos los dias y se replica al
 * central desde marzo de 2025, pero nadie la miraba junta. Medido el 2026-09-16
 * sobre 30 dias: 34,0 millones de faltante y 14,0 de sobrante entre las once
 * tiendas, con La Mota en 4,9 millones ella sola. Eso estaba guardado y nadie
 * lo estaba leyendo.
 *
 * COMO ESTA ARMADO EL CORREO
 *
 * Los insumos costosos -queso, pina, las masas y la pasta- van al detalle, uno por
 * uno. Las carnes van acumuladas en una linea, y el resto en otra. No es
 * capricho: los costosos son el 64% del faltante y el queso solo es la mitad de
 * todo, asi que listar los 120 insumos esconderia eso entre ceros.
 *
 * Los grupos salen de insumo.grupo_varianza, que se administra desde la
 * pantalla de insumos. Si alguien marca un insumo distinto, este reporte lo
 * respeta sin tocar codigo.
 *
 * EL RANGO
 *
 * Los ultimos siete dias COMPLETOS. Como la replica de varianza corre a las
 * 23:50 con el dia anterior, el dia en que corre el proceso todavia no tiene
 * datos: incluirlo meteria un dia vacio que baja los totales y hace ver la
 * semana mejor de lo que fue.
 *
 * Se le pueden pasar dos fechas en yyyy-MM-dd para volver a sacar una semana
 * vieja:  java ReporteVarianzaSemanal 2026-09-07 2026-09-13
 */
public class ReporteVarianzaSemanal {

	/** Cuantos dias cubre el reporte. */
	private static final int DIAS = 7;

	/** La lista de distribucion, en general.parametros_correo. */
	private static final String LISTA_CORREO = "REPORTEDESECHOSDIDI";

	public static void main(final String[] args) {
		//Dos fechas por argumento ganan sobre todo lo demas. Sirve para sacar
		//una semana puntual sin tener que tocar un parametro que comparten
		//todos los reprocesos.
		if (args != null && args.length >= 2) {
			ejecutar(args[0], args[1]);
			return;
		}
		final String[] rango = rangoParaCorridaDel(Calendar.getInstance());
		ejecutar(rango[0], rango[1]);
	}

	/**
	 * Vuelve a sacar el reporte de la semana que corresponde a FECHAREPROCESO.
	 *
	 * FECHAREPROCESO es el dia en que el proceso DEBIO correr, no el ultimo dia
	 * de la semana que se quiere. Es la misma lectura que tienen los otros
	 * reprocesos -en Rappi ese parametro es el dia del corte- y hace que
	 * reprocesar sea literalmente "hagalo como si hoy fuera ese domingo".
	 *
	 * OJO: FECHAREPROCESO es uno solo y lo comparten todos los reprocesos. Si
	 * alguien lo movio para reprocesar otra cosa, esto sale con esa fecha. Por
	 * eso el rango que se resolvio se imprime antes de hacer nada, y por eso el
	 * proceso normal acepta las dos fechas por argumento.
	 */
	public static void reprocesar() {
		final String fecha = CapaDAOSer.ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
		if (fecha == null || fecha.trim().length() == 0) {
			System.out.println("El parametro FECHAREPROCESO esta vacio. No se hace nada.");
			return;
		}
		final Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(formato().parse(fecha.trim()));
		} catch (final Exception e) {
			System.out.println("FECHAREPROCESO no se entiende: '" + fecha + "'. Se espera yyyy-MM-dd.");
			return;
		}
		System.out.println("Reproceso con FECHAREPROCESO = " + fecha.trim());
		final String[] rango = rangoParaCorridaDel(cal);
		ejecutar(rango[0], rango[1]);
	}

	/**
	 * El rango de siete dias que le toca a una corrida hecha ese dia.
	 *
	 * Termina el dia ANTERIOR al de la corrida, no el mismo dia: la replica de
	 * varianza corre a las 23:50 con el dia anterior, asi que el dia en curso
	 * no tiene datos. Incluirlo meteria un dia vacio que baja los totales y
	 * hace ver la semana mejor de lo que fue.
	 *
	 * Es publico para poder probarlo: es la clase de cuenta que se ve obvia y
	 * sale corrida por un dia.
	 *
	 * @return {desde, hasta} en yyyy-MM-dd
	 */
	public static String[] rangoParaCorridaDel(final Calendar diaDeCorrida) {
		final Calendar hasta = (Calendar) diaDeCorrida.clone();
		hasta.add(Calendar.DAY_OF_YEAR, -1);
		final Calendar desde = (Calendar) hasta.clone();
		desde.add(Calendar.DAY_OF_YEAR, -(DIAS - 1));
		return (new String[] {formato().format(desde.getTime()), formato().format(hasta.getTime())});
	}

	/** SimpleDateFormat no se puede compartir entre hilos, asi que se crea cada vez. */
	private static SimpleDateFormat formato() {
		return (new SimpleDateFormat("yyyy-MM-dd"));
	}

	/**
	 * Arma y manda el reporte de un rango. Es el unico sitio donde esta la
	 * logica: la corrida de los domingos y el reproceso entran por aca.
	 */
	public static void ejecutar(final String desde, final String hasta) {
		System.out.println("Varianza semanal del " + desde + " al " + hasta);

		final ArrayList<VarianzaSemanalDAO.Linea> lineas =
				VarianzaSemanalDAO.obtenerSemana(desde, hasta);
		if (lineas.isEmpty()) {
			//Sin datos no se manda un correo en ceros: un correo que dice cero
			//se lee como "esta semana no se perdio nada", que es lo contrario de
			//lo que pasa cuando la replica se cayo.
			System.out.println("No hay varianza en el rango. No se envia el reporte.");
			return;
		}

		final ArrayList<CorreoVarianzaSemanal.FilaTienda> filas = armarFilas(lineas);

		//De la que mas pierde a la que menos. La gracia es poder compararlas.
		Collections.sort(filas, new Comparator<CorreoVarianzaSemanal.FilaTienda>() {
			@Override
			public int compare(final CorreoVarianzaSemanal.FilaTienda a,
					final CorreoVarianzaSemanal.FilaTienda b) {
				return (Double.compare(a.neto(), b.neto()));
			}
		});

		double neto = 0;
		for (int i = 0; i < filas.size(); i++) {
			neto = neto + filas.get(i).neto();
		}

		enviar(filas, desde, hasta, neto);
	}

	/**
	 * Pasa las lineas de la consulta a una fila por tienda.
	 *
	 * Se usa LinkedHashMap y no HashMap para que las tiendas queden en el orden
	 * en que llegaron; igual despues se reordenan por perdida, pero un orden
	 * estable hace que dos corridas de la misma semana den el mismo correo.
	 */
	private static ArrayList<CorreoVarianzaSemanal.FilaTienda> armarFilas(
			final ArrayList<VarianzaSemanalDAO.Linea> lineas) {

		final LinkedHashMap<String, CorreoVarianzaSemanal.FilaTienda> porTienda =
				new LinkedHashMap<String, CorreoVarianzaSemanal.FilaTienda>();

		for (int i = 0; i < lineas.size(); i++) {
			final VarianzaSemanalDAO.Linea l = lineas.get(i);
			CorreoVarianzaSemanal.FilaTienda fila = porTienda.get(l.tienda);
			if (fila == null) {
				fila = new CorreoVarianzaSemanal.FilaTienda();
				fila.tienda = l.tienda;
				porTienda.put(l.tienda, fila);
			}

			fila.faltante = fila.faltante + l.faltante;
			fila.sobrante = fila.sobrante + l.sobrante;

			if ("CAROS".equals(l.grupo)) {
				fila.netoCaros = fila.netoCaros + l.neto;
				//Un costoso que no se movio no se lista. Son quince, y en una
				//semana normal la mitad queda en cero: listarlos todos por once
				//tiendas llena el correo de filas que no dicen nada.
				if (l.cantidad == 0 && Math.round(l.neto) == 0) {
					fila.carosEnCero = fila.carosEnCero + 1;
				} else {
					fila.caros.add(new Object[] {l.insumo, l.unidad,
							Double.valueOf(l.cantidad), Double.valueOf(l.neto)});
				}
			} else if ("CARNES".equals(l.grupo)) {
				fila.netoCarnes = fila.netoCarnes + l.neto;
			} else {
				fila.netoOtros = fila.netoOtros + l.neto;
			}
		}

		//Dentro de cada tienda, el costoso que mas plata se llevo primero.
		final ArrayList<CorreoVarianzaSemanal.FilaTienda> filas =
				new ArrayList<CorreoVarianzaSemanal.FilaTienda>(porTienda.values());
		for (int i = 0; i < filas.size(); i++) {
			Collections.sort(filas.get(i).caros, new Comparator<Object[]>() {
				@Override
				public int compare(final Object[] a, final Object[] b) {
					return (Double.compare(((Double) a[3]).doubleValue(),
							((Double) b[3]).doubleValue()));
				}
			});
		}
		return (filas);
	}

	private static void enviar(final ArrayList<CorreoVarianzaSemanal.FilaTienda> filas,
			final String desde, final String hasta, final double neto) {
		try {
			final ArrayList correos = GeneralDAO.obtenerCorreosParametro(LISTA_CORREO);
			if (correos.size() == 0) {
				//Vale la pena decirlo fuerte: un reporte que no le llega a nadie
				//se ve exactamente igual de exitoso que uno que si llego.
				System.out.println("La lista " + LISTA_CORREO + " esta vacia. NO SE ENVIO NADA.");
				return;
			}
			final CorreoElectronico info = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES",
					"CLAVECORREOREPORTE");
			final Correo correo = new Correo();
			correo.setAsunto(CorreoVarianzaSemanal.asunto(desde, hasta, neto));
			correo.setUsuarioCorreo(info.getCuentaCorreo());
			correo.setContrasena(info.getClaveCorreo());
			correo.setMensaje(CorreoVarianzaSemanal.cuerpo(filas, desde, hasta));
			final ControladorEnvioCorreo envio = new ControladorEnvioCorreo(correo, correos);
			envio.enviarCorreoHTML();
			System.out.println("Reporte de varianza enviado a " + correos.size() + " destinatario(s): "
					+ filas.size() + " tienda(s), neto " + CorreoVarianzaSemanal.pesos(neto));
		} catch (final Exception e) {
			System.out.println("Fallo el envio del reporte de varianza: " + e.toString());
		}
	}
}
