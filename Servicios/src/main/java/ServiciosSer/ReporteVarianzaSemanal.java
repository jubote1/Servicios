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
 * Los insumos caros -queso, pina, las masas y la pasta- van al detalle, uno por
 * uno. Las carnes van acumuladas en una linea, y el resto en otra. No es
 * capricho: los caros son el 64% del faltante y el queso solo es la mitad de
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
		final SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
		String desde;
		String hasta;

		if (args != null && args.length >= 2) {
			desde = args[0];
			hasta = args[1];
		} else {
			//Hasta AYER, no hasta hoy: lo de hoy todavia no se ha replicado.
			final Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_YEAR, -1);
			hasta = formato.format(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, -(DIAS - 1));
			desde = formato.format(cal.getTime());
		}
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
				//Un caro que no se movio no se lista. Son quince, y en una
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

		//Dentro de cada tienda, el caro que mas plata se llevo primero.
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
