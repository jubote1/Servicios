package ServiciosSer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import CapaDAOSer.ConsignacionBoldDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.TiendaDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.Tienda;
import utilidadesSer.ControladorEnvioCorreo;
import utilidadesSer.CorreoConsignacion;

/**
 * Consignacion diaria de BOLD (QR): lo que se cobro por QR en cada tienda el
 * dia anterior y el total general que Bold va a consignar.
 *
 * A diferencia de la de Wompi, que solo corre en dias habiles y cubre desde el
 * ultimo dia habil, esta corre TODOS los dias y cubre un solo dia: el de ayer.
 * El reproceso (ReporteConsignacionBoldReproceso) toma la fecha de ejecucion
 * de general.parametros.FECHAREPROCESO, igual que los demas reprocesos, y
 * reporta el dia anterior a esa fecha.
 *
 * Correo a REPORTECONSIGNACIONBOLD (general.parametros_correo); si ese
 * parametro todavia no tiene destinatarios usa los de REPORTECONSIGNACIONWOMPI.
 */
public class ReporteConsignacionBold {

	public static void main(final String[] args) {
		generar(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
	}

	/** @param fechaEjecucion yyyy-MM-dd, el dia en que "corre" el proceso; se reporta el dia anterior */
	public static void generar(final String fechaEjecucion) {
		final SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		final Calendar calendario = Calendar.getInstance();
		try {
			calendario.setTime(formatoFecha.parse(fechaEjecucion));
		} catch (final Exception e) {
			System.out.println("ReporteConsignacionBold: fecha de ejecucion ilegible '" + fechaEjecucion + "'");
			return;
		}
		calendario.add(Calendar.DAY_OF_YEAR, -1);
		final String fechaConsignacion = formatoFecha.format(calendario.getTime());
		final String fechaLarga = fechaLarga(calendario.getTime());

		final ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		final StringBuilder filas = new StringBuilder();
		final ArrayList<String> sinRespuesta = new ArrayList<String>();
		double totalGeneral = 0;
		int cantidadGeneral = 0;
		for (final Tienda tienda : tiendas) {
			final String host = tienda.getHostBD();
			if (host == null || host.trim().length() == 0) {
				continue;
			}
			final double[] qr = ConsignacionBoldDAO.obtenerQRDia(fechaConsignacion, host);
			if (qr == null) {
				sinRespuesta.add(tienda.getNombreTienda());
				filas.append(CorreoConsignacion.fila(tienda.getNombreTienda(), "sin respuesta", "-"));
				continue;
			}
			totalGeneral += qr[0];
			cantidadGeneral += (int) qr[1];
			filas.append(CorreoConsignacion.fila(tienda.getNombreTienda(),
					CorreoConsignacion.entero((long) qr[1]), CorreoConsignacion.pesos(qr[0])));
		}

		final StringBuilder cuerpo = new StringBuilder();
		cuerpo.append(CorreoConsignacion.abrir("Consignación diaria BOLD",
				"Consignación del " + fechaLarga + " por parte de SONO QR BOLD"));
		if (!sinRespuesta.isEmpty()) {
			cuerpo.append(CorreoConsignacion.aviso("No respondieron estas tiendas y su QR NO está sumado en el total: "
					+ join(sinRespuesta) + ". Verifique que el computador esté encendido y reprocese el día."));
		}
		cuerpo.append(CorreoConsignacion.tarjetaTotal("Total a consignar BOLD", totalGeneral,
				CorreoConsignacion.entero(cantidadGeneral) + " pagos QR del " + fechaConsignacion));
		cuerpo.append(CorreoConsignacion.abrirTabla("Cobrado por QR en cada tienda", "Tienda", "Pagos QR",
				"Valor"));
		cuerpo.append(filas);
		cuerpo.append(CorreoConsignacion.filaTotal("TOTAL GENERAL", CorreoConsignacion.entero(cantidadGeneral),
				CorreoConsignacion.pesos(totalGeneral)));
		cuerpo.append(CorreoConsignacion.cerrarTabla());
		cuerpo.append(CorreoConsignacion.cerrar("Generado automáticamente por Servicios Pizza Americana el "
				+ new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "."));

		enviar("CONSIGNACION DIARIA BOLD QR " + fechaConsignacion, cuerpo.toString());
	}

	private static void enviar(final String asunto, final String html) {
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTECONSIGNACIONBOLD");
		if (correos.isEmpty()) {
			System.out.println("REPORTECONSIGNACIONBOLD sin destinatarios, se usan los de REPORTECONSIGNACIONWOMPI");
			correos = GeneralDAO.obtenerCorreosParametro("REPORTECONSIGNACIONWOMPI");
		}
		if (correos.isEmpty()) {
			System.out.println("Sin destinatarios para la consignacion Bold, no se envia el correo");
			return;
		}
		final Correo correo = new Correo();
		correo.setAsunto(asunto);
		final CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES",
				"CLAVECORREOREPORTE");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje(html);
		new ControladorEnvioCorreo(correo, correos).enviarCorreoHTML();
	}

	private static String fechaLarga(final Date fecha) {
		final String texto = new SimpleDateFormat("EEEE d 'de' MMMM 'de' yyyy", new Locale("es", "CO")).format(fecha);
		return texto.substring(0, 1).toUpperCase() + texto.substring(1);
	}

	private static String join(final ArrayList<String> nombres) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nombres.size(); i++) {
			sb.append(i == 0 ? "" : ", ").append(nombres.get(i));
		}
		return sb.toString();
	}

}
