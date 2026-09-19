package ServiciosSer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.TiendaDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.Tienda;
import utilidadesSer.ControladorEnvioCorreo;
import utilidadesSer.CorreoIdentificacion;

/**
 * Publica cuanto se identifica al cliente en el mostrador, por tienda y cajero.
 *
 * Es la medicion del paso 3 de "Un Solo Cliente". Ese paso es el unico que
 * depende de la gente y no del codigo: el POS ya permite identificar al cliente
 * -en Para Llevar se hace el 90% de las veces- pero en Punto de Venta no se
 * pregunta. Medido el 2026-09-15 sobre 30 dias: 43% general, con tiendas entre
 * 22% y 65% usando el mismo programa.
 *
 * Sin la medicion publicada esto se desinfla en dos semanas, por eso el correo
 * esta armado como una tabla de posiciones y no como un informe.
 *
 * Corre una vez por semana con los ultimos 7 dias. Una tienda que no responda
 * no tumba el reporte de las demas.
 */
public class ReporteIdentificacionMostrador {

	/** Como se llama el tipo de pedido de mostrador en tipo_pedido. */
	private static final String TIPO_MOSTRADOR = "Punto de Venta";

	/** Cuantos dias hacia atras se miran. */
	private static final int DIAS = 7;

	/** Un cajero con menos pedidos que esto no se lista: no dice nada. */
	private static final int MINIMO_PEDIDOS_CAJERO = 10;

	/**
	 * El rango de siete dias que le toca a una corrida hecha ese dia.
	 *
	 * Termina el dia ANTERIOR al de la corrida, y son siete dias exactos.
	 *
	 * Antes hacia "hasta = hoy, desde = hoy - 7", que tenia dos problemas: eran
	 * OCHO dias contando los dos extremos, y el ultimo era el dia en curso. Si
	 * el proceso corre un domingo de madrugada, ese domingo no ha vendido nada
	 * todavia: entraba un dia casi vacio que bajaba el porcentaje y hacia ver la
	 * semana peor de lo que fue. Y como el rango se corria un dia cada vez, dos
	 * semanas seguidas no eran comparables.
	 *
	 * Es la misma cuenta de ReporteVarianzaSemanal.rangoParaCorridaDel, a
	 * proposito: dos reportes semanales que cubran semanas distintas se leen uno
	 * contra otro y no cuadran.
	 *
	 * Es publico para poder probarlo: es la clase de cuenta que se ve obvia y
	 * sale corrida por un dia.
	 *
	 * @return {desde, hasta} en yyyy-MM-dd
	 */
	public static String[] rangoParaCorridaDel(final Calendar diaDeCorrida) {
		final SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
		final Calendar hasta = (Calendar) diaDeCorrida.clone();
		hasta.add(Calendar.DAY_OF_YEAR, -1);
		final Calendar desde = (Calendar) hasta.clone();
		desde.add(Calendar.DAY_OF_YEAR, -(DIAS - 1));
		return (new String[] {formato.format(desde.getTime()), formato.format(hasta.getTime())});
	}

	public static void main(final String[] args) {
		//Se aceptan las dos fechas por argumento para poder repetir una semana
		//sin tocar parametros, igual que el reporte de varianza.
		final String[] rango = (args != null && args.length >= 2)
				? new String[] {args[0], args[1]}
				: rangoParaCorridaDel(Calendar.getInstance());
		final String desde = rango[0];
		final String hasta = rango[1];
		System.out.println("Identificacion en mostrador del " + desde + " al " + hasta);

		final ArrayList<CorreoIdentificacion.FilaTienda> filas =
				new ArrayList<CorreoIdentificacion.FilaTienda>();

		//Se usa la lista LOCAL, que es la que trae el host de cada tienda: es la
		//misma que usa el resumen de operacion para conectarse a las doce.
		final ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		for (int i = 0; i < tiendas.size(); i++) {
			final Tienda tienda = tiendas.get(i);
			if (tienda.getHostBD() == null || tienda.getHostBD().trim().length() == 0) {
				continue;
			}
			final ArrayList<capaDAOPOS.PedidoDAO.IdentificacionCajero> cajeros =
					capaDAOPOS.PedidoDAO.obtenerIdentificacionMostrador(desde, hasta, tienda.getHostBD(),
							TIPO_MOSTRADOR);
			if (cajeros.isEmpty()) {
				continue;
			}
			final CorreoIdentificacion.FilaTienda fila = new CorreoIdentificacion.FilaTienda();
			fila.tienda = tienda.getNombreTienda();
			for (int j = 0; j < cajeros.size(); j++) {
				final capaDAOPOS.PedidoDAO.IdentificacionCajero c = cajeros.get(j);
				fila.pedidos = fila.pedidos + c.pedidos;
				fila.identificados = fila.identificados + c.identificados;
				fila.seNegaron = fila.seNegaron + c.preguntadosSinDar;
				//El detalle por cajero solo para los que tienen volumen: con tres
				//pedidos un porcentaje no significa nada y estorba en la lista.
				if (c.pedidos >= MINIMO_PEDIDOS_CAJERO) {
					fila.cajeros.add(new String[] {c.cajero, Integer.toString(c.pedidos),
							Integer.toString(c.identificados), Integer.toString(c.porcentaje()),
							Integer.toString(c.preguntadosSinDar), Integer.toString(c.sinPreguntar())});
				}
			}
			filas.add(fila);
		}

		if (filas.isEmpty()) {
			System.out.println("No se pudo leer ninguna tienda. No se envia el reporte.");
			return;
		}

		//De mejor a peor. La gracia del reporte es que se puedan comparar.
		Collections.sort(filas, new Comparator<CorreoIdentificacion.FilaTienda>() {
			@Override
			public int compare(final CorreoIdentificacion.FilaTienda a,
					final CorreoIdentificacion.FilaTienda b) {
				return (b.porcentaje() - a.porcentaje());
			}
		});

		try {
			final ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEIDENTIFICACION");
			if (correos.size() == 0) {
				System.out.println("La lista REPORTEIDENTIFICACION esta vacia. No se envia.");
				return;
			}
			final CorreoElectronico info = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES",
					"CLAVECORREOREPORTE");
			final Correo correo = new Correo();
			correo.setAsunto(CorreoIdentificacion.asunto(desde, hasta));
			correo.setUsuarioCorreo(info.getCuentaCorreo());
			correo.setContrasena(info.getClaveCorreo());
			correo.setMensaje(CorreoIdentificacion.cuerpo(filas, desde, hasta));
			final ControladorEnvioCorreo envio = new ControladorEnvioCorreo(correo, correos);
			envio.enviarCorreoHTML();
			System.out.println("Reporte de identificacion enviado: " + filas.size() + " tienda(s).");
		} catch (final Exception e) {
			System.out.println("Fallo el envio del reporte de identificacion: " + e.toString());
		}
	}
}
