package ServiciosSer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiempoPedidoDAO;
import CapaDAOSer.TiendaDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.FilaResumenOperacion;
import ModeloSer.TiempoPedido;
import ModeloSer.Tienda;
import capaControladorPOS.BiometriaCtrl;
import capaControladorPOS.EmpleadoCtrl;
import utilidadesSer.ControladorEnvioCorreo;
import utilidadesSer.CorreoResumenOperacion;

/**
 * Manda el correo de resumen de operacion a la lista REPRESUMENOPERACION.
 *
 * Antes el correo era una tabla de tiempos mas tres tablas por cada tienda: con
 * doce tiendas eran treinta y siete tablas apiladas y no habia forma de ver el
 * estado de la cadena sin bajar por todas. Ahora es una franja de totales y una
 * sola tabla, una fila por tienda, ordenada de la que tiene mas trabajo a la que
 * tiene menos.
 *
 * La consulta a cada tienda tambien cambio: era un llamado por dato, cada uno
 * abriendo su propia conexion remota. Ahora es una sola conexion por tienda.
 */
public class ReporteResumenOperacion {

	/** Minutos de promesa que se asumen si la tienda no tiene el tiempo configurado. */
	private static final int TIEMPO_NO_CONFIGURADO = -1;

	public static void main(final String[] args) {
		final SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		final SimpleDateFormat formatoFechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		final SimpleDateFormat formatoLegible = new SimpleDateFormat("dd/MM/yyyy HH:mm");

		final Date ahora = new Date();
		final String fechaActual = formatoFecha.format(ahora);
		final Calendar calendario = Calendar.getInstance();
		calendario.setTime(ahora);
		calendario.add(Calendar.HOUR, -1);
		final String fechaActualMenosHora = formatoFechaHora.format(calendario.getTime());

		//El tiempo que esta dando cada tienda. Antes era la primera tabla del correo,
		//doce filas repitiendo el mismo numero; ahora es una columna y la referencia
		//contra la que se pintan las barritas.
		final ArrayList<TiempoPedido> tiempos = TiempoPedidoDAO.retornarTiemposPedidosLocal();

		final ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		final int estadoPorSalir = ParametrosDAO.retornarValorNumericoLocal("EMPACADODOMICILIO");
		final int estadoEnRuta = ParametrosDAO.retornarValorNumericoLocal("ENRUTADOMICILIO");
		final int tipoDomicilio = ParametrosDAO.retornarValorNumericoLocal("TIPOPEDIDODOMICILIO");

		final ArrayList<FilaResumenOperacion> filas = new ArrayList<FilaResumenOperacion>();
		boolean hayPendientes = false;

		for (int i = 0; i < tiendas.size(); i++) {
			final Tienda tien = tiendas.get(i);
			if (tien.getHostBD() == null || tien.getHostBD().trim().length() == 0) {
				continue;
			}
			final FilaResumenOperacion fila = new FilaResumenOperacion(tien.getIdTienda(), tien.getNombreTienda());
			fila.setTiempoTienda(ReporteResumenOperacion.tiempoDeLaTienda(tiempos, tien.getIdTienda()));

			//Una sola conexion por tienda para todos los numeros de pedidos.
			final capaDAOPOS.PedidoDAO.ResumenTienda resumen = capaDAOPOS.PedidoDAO.obtenerResumenTienda(fechaActual,
					fechaActualMenosHora, estadoPorSalir, estadoEnRuta, tipoDomicilio, tien.getHostBD());
			fila.setSeLeyo(resumen.seLeyo);
			fila.setPedidosCocina(resumen.pedidosCocina);
			fila.setMinutosCocina(resumen.minutosCocina);
			fila.setPedidosPorSalir(resumen.pedidosPorSalir);
			fila.setMinutosPorSalir(resumen.minutosPorSalir);
			fila.setPedidosEnRuta(resumen.pedidosEnRuta);
			fila.setUltimaHoraDomicilio(resumen.ultimaHoraDomicilio);
			fila.setUltimaHoraOtros(resumen.ultimaHoraOtros);
			fila.setVentaDia(resumen.ventaDia);
			fila.setPedidosDescuadrados(resumen.pedidosDescuadrados);

			//Los domiciliarios: los de nomina salen del central y los temporales de la tienda.
			try {
				final BiometriaCtrl bioCtrl = new BiometriaCtrl(false);
				fila.setDomiciliariosInternos(bioCtrl.cantidadEmpleadoDomiciliario(fechaActual, tien.getIdTienda()));
			} catch (final Exception e) {
				System.out.println("No se pudo contar domiciliarios de nomina en " + tien.getNombreTienda() + ": " + e);
			}
			try {
				final EmpleadoCtrl empCtrl = new EmpleadoCtrl(false);
				fila.setDomiciliariosExternos(empCtrl.consultarCantEmpleadoTempDia(fechaActual, tien.getHostBD()));
			} catch (final Exception e) {
				System.out.println("No se pudo contar domiciliarios temporales en " + tien.getNombreTienda() + ": " + e);
			}

			//Los programados van aparte y no cuentan como pendientes por salir.
			try {
				final ArrayList programados = PedidoDAO.obtenerPedidosProgramadosTienda(tien.getIdTienda());
				final ArrayList<String[]> lista = new ArrayList<String[]>();
				for (int j = 0; j < programados.size(); j++) {
					lista.add((String[]) programados.get(j));
				}
				fila.setProgramados(lista);
			} catch (final Exception e) {
				System.out.println("No se pudieron leer los programados de " + tien.getNombreTienda() + ": " + e);
			}

			if (fila.getPendientes() > 0) {
				hayPendientes = true;
			}
			if (fila.getPedidosDescuadrados() > 0) {
				ReporteResumenOperacion.avisarDescuadre(tien.getNombreTienda(), fila.getPedidosDescuadrados(),
						fechaActual);
			}
			filas.add(fila);
		}

		//Primero la tienda con mas trabajo encima. Empatan por carga, desempata el
		//peor tiempo: entre dos tiendas con nueve pedidos, arriba la que va mas tarde.
		Collections.sort(filas, new Comparator<FilaResumenOperacion>() {
			@Override
			public int compare(final FilaResumenOperacion a, final FilaResumenOperacion b) {
				if (a.getCarga() != b.getCarga()) {
					return (b.getCarga() - a.getCarga());
				}
				if (a.getPeorTiempo() != b.getPeorTiempo()) {
					return (b.getPeorTiempo() - a.getPeorTiempo());
				}
				return (a.getNombreTienda().compareTo(b.getNombreTienda()));
			}
		});

		System.out.println("Tiendas en el resumen: " + filas.size() + ". Hay pendientes: " + hayPendientes);
		if (!hayPendientes) {
			System.out.println("Sin pedidos pendientes en ninguna tienda. No se envia el resumen.");
			return;
		}

		final ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPRESUMENOPERACION");
		if (correos.size() == 0) {
			System.out.println("La lista REPRESUMENOPERACION esta vacia. No se envia el resumen.");
			return;
		}
		try {
			final String fechaHora = formatoLegible.format(ahora);
			final CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES",
					"CLAVECORREOREPORTE");
			final Correo correo = new Correo();
			//Se conserva el prefijo OPERACION GENERAL para no romper los filtros de correo
			//que la gente ya tiene armados, pero con la fecha legible en vez del Date crudo.
			correo.setAsunto("OPERACION GENERAL " + fechaHora);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje(CorreoResumenOperacion.armar(filas, fechaHora));
			final ControladorEnvioCorreo envio = new ControladorEnvioCorreo(correo, correos);
			envio.enviarCorreoHTML();
			System.out.println("Resumen de operacion enviado a " + correos.size() + " destinatarios.");
		} catch (final Exception e) {
			System.out.println("Fallo el envio del resumen de operacion: " + e.toString());
		}
	}

	/** Los minutos que ofrece la tienda, o -1 si no los tiene configurados. */
	private static int tiempoDeLaTienda(final ArrayList<TiempoPedido> tiempos, final int idTienda) {
		for (int i = 0; i < tiempos.size(); i++) {
			if (tiempos.get(i).getIdtienda() == idTienda) {
				return (tiempos.get(i).getMinutosPedido());
			}
		}
		return (ReporteResumenOperacion.TIEMPO_NO_CONFIGURADO);
	}

	/**
	 * Aviso aparte por descuadres de forma de pago, para corregirlos antes del
	 * cierre. Va a otra lista y con otro asunto, igual que antes.
	 */
	private static void avisarDescuadre(final String nombreTienda, final int cantidad, final String fechaActual) {
		try {
			final ArrayList correos = GeneralDAO.obtenerCorreosParametro("ERRORREPLICAINV");
			if (correos.size() == 0) {
				System.out.println("La lista ERRORREPLICAINV esta vacia. No se avisa el descuadre.");
				return;
			}
			final CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES",
					"CLAVECORREOREPORTE");
			final Correo correo = new Correo();
			correo.setAsunto("OJO DESCUADRE FORMA PAGO " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("Ojo en la tienda: \n" + nombreTienda + " tiene descuadre en " + cantidad
					+ " descuadrados.");
			final ControladorEnvioCorreo envio = new ControladorEnvioCorreo(correo, correos);
			envio.enviarCorreoHTML();
		} catch (final Exception e) {
			//Que falle el aviso de descuadre no puede tumbar el resumen.
			System.out.println("Fallo el aviso de descuadre de " + nombreTienda + ": " + e.toString());
		}
	}
}
