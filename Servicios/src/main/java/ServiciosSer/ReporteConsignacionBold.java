package ServiciosSer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import CapaDAOSer.ConsignacionBoldDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.TiendaDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.Tienda;
import utilidadesSer.ControladorEnvioCorreo;
import utilidadesSer.CorreoConsignacion;

/**
 * Consignacion diaria de BOLD (QR): lo que se cobro por QR en cada tienda el
 * dia anterior y el total general que Bold va a consignar, ya descontada su
 * comision (general.parametros.COMISIONBOLD en valornumericod, 1.5 por
 * defecto, con el IVA incluido).
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

	/** Comision de Bold en porcentaje, IVA incluido, si general.parametros.COMISIONBOLD no existe. */
	private static final double COMISION_BOLD_DEFECTO = 1.5;

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

		// Bold descuenta su comision de lo que consigna. El porcentaje ya incluye el IVA.
		final double porcentajeComision = ParametrosDAO.retornarValorNumericoDouble("COMISIONBOLD",
				COMISION_BOLD_DEFECTO);
		final String textoComision = CorreoConsignacion.porcentaje(porcentajeComision) + " IVA incluido";

		final ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		final StringBuilder filas = new StringBuilder();
		final StringBuilder filasComparativo = new StringBuilder();
		final ArrayList<String> sinRespuesta = new ArrayList<String>();
		final ArrayList<String> conOtrosEventos = new ArrayList<String>();
		double totalGeneral = 0;
		int cantidadGeneral = 0;
		int tiendasConDiferencia = 0;
		for (final Tienda tienda : tiendas) {
			final String host = tienda.getHostBD();
			if (host == null || host.trim().length() == 0) {
				continue;
			}
			final double[] qr = ConsignacionBoldDAO.obtenerQRDia(fechaConsignacion, host);
			if (qr == null) {
				sinRespuesta.add(tienda.getNombreTienda());
				filas.append(CorreoConsignacion.fila(tienda.getNombreTienda(), "sin respuesta", "-", "-"));
				filasComparativo.append(CorreoConsignacion.filaComparativo(tienda.getNombreTienda(),
						"-", "-", "-", "-", null));
				continue;
			}
			totalGeneral += qr[0];
			cantidadGeneral += (int) qr[1];
			filas.append(CorreoConsignacion.fila(tienda.getNombreTienda(),
					CorreoConsignacion.entero((long) qr[1]), CorreoConsignacion.pesos(qr[0]),
					CorreoConsignacion.pesos(qr[0] - qr[0] * porcentajeComision / 100)));

			//Lo que Bold dice que cobro, para contrastarlo con lo que quedo
			//registrado en los pedidos. Va en su propia consulta y no en la de
			//arriba porque son dos tablas distintas y una puede responder sin
			//la otra: la tienda podria tener pedidos y no tener todavia el
			//webhook de Bold configurado.
			final double[] bold = ConsignacionBoldDAO.obtenerMovimientosBoldDia(fechaConsignacion, host);
			if (bold == null) {
				filasComparativo.append(CorreoConsignacion.filaComparativo(tienda.getNombreTienda(),
						CorreoConsignacion.entero((long) qr[1]), CorreoConsignacion.pesos(qr[0]),
						"-", "-", null));
				continue;
			}
			if (bold[2] > 0) {
				conOtrosEventos.add(tienda.getNombreTienda());
			}
			final double diferencia = bold[0] - qr[0];
			if (diferencia != 0) {
				tiendasConDiferencia++;
			}
			filasComparativo.append(CorreoConsignacion.filaComparativo(tienda.getNombreTienda(),
					CorreoConsignacion.entero((long) qr[1]), CorreoConsignacion.pesos(qr[0]),
					CorreoConsignacion.entero((long) bold[1]), CorreoConsignacion.pesos(bold[0]),
					Double.valueOf(diferencia)));
		}
		final double comisionTotal = totalGeneral * porcentajeComision / 100;
		final double totalAConsignar = totalGeneral - comisionTotal;

		final StringBuilder cuerpo = new StringBuilder();
		cuerpo.append(CorreoConsignacion.abrir("Consignación diaria BOLD",
				"Consignación del " + fechaLarga + " por parte de SONO QR BOLD"));
		if (!sinRespuesta.isEmpty()) {
			cuerpo.append(CorreoConsignacion.aviso("No respondieron estas tiendas y su QR NO está sumado en el total: "
					+ join(sinRespuesta) + ". Verifique que el computador esté encendido y reprocese el día."));
		}
		cuerpo.append(CorreoConsignacion.tarjetaTotal("Total a consignar BOLD", totalAConsignar,
				"Venta QR " + CorreoConsignacion.pesos(totalGeneral) + " menos comisión "
						+ CorreoConsignacion.pesos(comisionTotal) + " (" + textoComision + ")"));

		cuerpo.append(CorreoConsignacion.abrirTabla("Liquidación", "Concepto", "Valor"));
		cuerpo.append(CorreoConsignacion.fila("Pagos QR del " + fechaConsignacion,
				CorreoConsignacion.entero(cantidadGeneral)));
		cuerpo.append(CorreoConsignacion.fila("Venta QR", CorreoConsignacion.pesos(totalGeneral)));
		cuerpo.append(CorreoConsignacion.fila("(-) Comisión BOLD " + textoComision,
				CorreoConsignacion.pesos(comisionTotal)));
		cuerpo.append(CorreoConsignacion.filaTotal("Valor a consignar", CorreoConsignacion.pesos(totalAConsignar)));
		cuerpo.append(CorreoConsignacion.cerrarTabla());

		cuerpo.append(CorreoConsignacion.abrirTabla("Cobrado por QR en cada tienda", "Tienda", "Pagos QR",
				"Venta QR", "Neto (menos comisión)"));
		cuerpo.append(filas);
		cuerpo.append(CorreoConsignacion.filaTotal("TOTAL GENERAL", CorreoConsignacion.entero(cantidadGeneral),
				CorreoConsignacion.pesos(totalGeneral), CorreoConsignacion.pesos(totalAConsignar)));
		cuerpo.append(CorreoConsignacion.cerrarTabla());

		/*
		 * El comparativo: lo que el cajero REGISTRO como pago QR contra lo que
		 * Bold dice que COBRO.
		 *
		 * Las dos cifras salen de la misma tienda pero de tablas distintas, y
		 * por eso sirven de control cruzado: pedido_forma_pago es lo que se
		 * digito, sonoqr_movimiento es lo que el datafono efectivamente proceso.
		 *
		 * La primera corrida contra Manrique del 2026-09-21 encontro dos cosas
		 * en una sola noche: un pedido con 14.000 registrados contra 14.500
		 * cobrados, y un cobro de 1.000 que no quedo en ningun pedido.
		 *
		 * CUIDADO CON EL TOTAL: no se suma una fila de total, a proposito. Los
		 * dos lados se miden en ventanas que casi siempre coinciden pero no
		 * son la misma -los pedidos van por jornada y los cobros por dia
		 * corrido-, asi que un gran total invitaria a restar dos numeros que
		 * no son exactamente comparables. Lo que importa es la fila por tienda.
		 */
		cuerpo.append(CorreoConsignacion.abrirTabla("Pedidos contra cobros de Bold", "Tienda",
				"Pagos QR", "Valor en pedidos", "Cobros Bold", "Valor Bold", "Diferencia"));
		cuerpo.append(filasComparativo);
		cuerpo.append(CorreoConsignacion.cerrarTabla());
		cuerpo.append(CorreoConsignacion.leyendaComparativo());
		if (tiendasConDiferencia > 0) {
			cuerpo.append(CorreoConsignacion.aviso(tiendasConDiferencia == 1
					? "Una tienda no cuadra entre lo registrado en los pedidos y lo que cobró Bold."
					: tiendasConDiferencia + " tiendas no cuadran entre lo registrado en los pedidos"
							+ " y lo que cobró Bold."));
		}
		if (!conOtrosEventos.isEmpty()) {
			cuerpo.append(CorreoConsignacion.aviso("Bold reportó eventos distintos de una venta aprobada"
					+ " (anulaciones o devoluciones) en: " + join(conOtrosEventos)
					+ ". Esos NO están sumados en la columna de Bold; revíselos en la tienda."));
		}

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
