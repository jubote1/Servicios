package ServiciosSer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.DiaFestivo;
import ModeloSer.Tienda;
import utilidadesSer.ControladorEnvioCorreo;
import utilidadesSer.CorreoConsignacion;

/**
 * Consignacion diaria de WOMPI (pagos virtuales) mas los datafonos con
 * adquirencia Bancolombia. Solo corre en dias habiles: cubre desde el ultimo
 * dia habil anterior hasta hoy. El QR ya no va aqui: pasa por Bold y tiene su
 * propio correo (ReporteConsignacionBold).
 *
 * El reproceso (ReporteConsignacionWompiReproceso) reutiliza generar() con la
 * fecha de general.parametros.FECHAREPROCESO en vez de la de hoy.
 */
public class ReporteConsignacionWompi {

	public static void main(final String[] args) {
		generar(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
	}

	/** @param fechaActual yyyy-MM-dd, el dia en que "corre" el proceso */
	public static void generar(final String fechaActual) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		final Calendar calendarioActual = Calendar.getInstance();
		final ArrayList<DiaFestivo> festivos = GeneralDAO.obtenerDiasFestivos();
		try {
			calendarioActual.setTime(dateFormat.parse(fechaActual));
		} catch (final Exception e) {
			System.out.println("ReporteConsignacionWompi: fecha ilegible '" + fechaActual + "' " + e.toString());
			return;
		}
		// Solo se corre en dias habiles: si hoy es festivo no se hace nada.
		if (validarFestivo(festivos, fechaActual)) {
			return;
		}
		// El periodo arranca en el ultimo dia habil anterior (sin sabados, domingos ni festivos).
		String fechaAnterior = "";
		boolean buscando = true;
		while (buscando) {
			calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
			final int diaControl = calendarioActual.get(Calendar.DAY_OF_WEEK);
			fechaAnterior = dateFormat.format(calendarioActual.getTime());
			final boolean festivo = validarFestivo(festivos, fechaAnterior);
			// Domingo = 1 y Sabado = 7
			buscando = festivo || diaControl == 1 || diaControl == 7;
		}

		// Parametros de la liquidacion de Wompi
		final double comisionWompi = ParametrosDAO.retornarValorNumericoLocalDouble("COMISIONWOMPI");
		final double comisionWompiBanc = ParametrosDAO.retornarValorNumericoLocalDouble("COMISIONWOMPIBANC");
		final double adicionComisionWompi = ParametrosDAO.retornarValorNumericoLocalDouble("ADICIONCOMISIONWOMPI");
		final double ivaComisionWompi = ParametrosDAO.retornarValorNumericoLocalDouble("IVACOMISIONWOMPI");
		final double retencionFuenteWompi = ParametrosDAO.retornarValorNumericoLocalDouble("RETENCIONFUENTEWOMPI");
		final double retencionICAWompi = ParametrosDAO.retornarValorNumericoLocalDouble("RETENCIONICAWOMPI");

		double comisionTotal = 0;
		double ivaComisionTotal = 0;
		double retencionFuenteTotal = 0;
		double retencionIcaTotal = 0;
		double valorConsignacion = 0;
		double valorPedidos = 0;
		final ArrayList<capaModeloCC.Pedido> pedVirtualTienda = capaDAOCC.PedidoDAO
				.consultarPedidosVirtualRealizadosTienda(fechaAnterior, fechaActual, 0);
		for (int k = 0; k < pedVirtualTienda.size(); k++) {
			final capaModeloCC.Pedido pedTemp = pedVirtualTienda.get(k);
			final double comision;
			final double ivaComision;
			// El pago por Bancolombia (transferencia o QR de Wompi) cobra menos comision.
			if (pedTemp.getTipoPago().equals("BANCOLOMBIA_TRANSFER") || pedTemp.getTipoPago().equals("BANCOLOMBIA_QR")) {
				comision = (pedTemp.getTotal_neto() * (comisionWompiBanc / 100)) + adicionComisionWompi;
			} else {
				comision = (pedTemp.getTotal_neto() * (comisionWompi / 100)) + adicionComisionWompi;
			}
			ivaComision = comision * (ivaComisionWompi / 100);
			final double retencionFuente;
			final double retencionIca;
			if (pedTemp.getTipoPago().equals("CARD")) {
				retencionFuente = pedTemp.getTotal_neto() * (retencionFuenteWompi / 100);
				retencionIca = pedTemp.getTotal_neto() * (retencionICAWompi / 100);
			} else {
				retencionFuente = 0;
				retencionIca = 0;
			}
			comisionTotal += comision;
			ivaComisionTotal += ivaComision;
			retencionFuenteTotal += retencionFuente;
			retencionIcaTotal += retencionIca;
			valorConsignacion += pedTemp.getTotal_neto() - comision - ivaComision - retencionFuente - retencionIca;
			valorPedidos += pedTemp.getTotal_neto();
		}

		// Datafonos con adquirencia Bancolombia: venta menos comision e IVA.
		final ArrayList<Tienda> tiendasAdqBancolombia = TiendaDAO.obtenerTiendasAdqBancolombia();
		final StringBuilder filasDatafonos = new StringBuilder();
		double totalTarjetaTiendas = 0;
		for (int i = 0; i < tiendasAdqBancolombia.size(); i++) {
			final Tienda tiendaTemp = tiendasAdqBancolombia.get(i);
			if (tiendaTemp.getHostBD() != null && !tiendaTemp.getHostBD().equals("")) {
				double ventaTarjetaTienda = PedidoDAO.obtenerTotalesPedidosSemanaTarjeta(fechaAnterior, fechaActual,
						tiendaTemp.getHostBD());
				ventaTarjetaTienda = ventaTarjetaTienda - (ventaTarjetaTienda * 0.019)
						- (ventaTarjetaTienda * 0.019 * 0.19);
				totalTarjetaTiendas += ventaTarjetaTienda;
				filasDatafonos.append(CorreoConsignacion.fila(tiendaTemp.getNombreTienda(),
						CorreoConsignacion.pesos(ventaTarjetaTienda)));
			}
		}

		final StringBuilder cuerpo = new StringBuilder();
		cuerpo.append(CorreoConsignacion.abrir("Consignación diaria Wompi",
				"Pagos virtuales entre " + fechaAnterior + " y " + fechaActual));
		cuerpo.append(CorreoConsignacion.tarjetaTotal("Total a consignar", valorConsignacion + totalTarjetaTiendas,
				"Wompi " + CorreoConsignacion.pesos(valorConsignacion) + " + datafonos Bancolombia "
						+ CorreoConsignacion.pesos(totalTarjetaTiendas)));

		cuerpo.append(CorreoConsignacion.abrirTabla("Wompi: pagos virtuales", "Concepto", "Valor"));
		cuerpo.append(CorreoConsignacion.fila("Pedidos con pago virtual", CorreoConsignacion.entero(pedVirtualTienda.size())));
		cuerpo.append(CorreoConsignacion.fila("Total vendido", CorreoConsignacion.pesos(valorPedidos)));
		cuerpo.append(CorreoConsignacion.fila("(-) Comisión", CorreoConsignacion.pesos(comisionTotal)));
		cuerpo.append(CorreoConsignacion.fila("(-) IVA de la comisión", CorreoConsignacion.pesos(ivaComisionTotal)));
		cuerpo.append(CorreoConsignacion.fila("(-) Retención en la fuente", CorreoConsignacion.pesos(retencionFuenteTotal)));
		cuerpo.append(CorreoConsignacion.fila("(-) ReteICA", CorreoConsignacion.pesos(retencionIcaTotal)));
		cuerpo.append(CorreoConsignacion.filaTotal("Valor a consignar Wompi", CorreoConsignacion.pesos(valorConsignacion)));
		cuerpo.append(CorreoConsignacion.cerrarTabla());

		cuerpo.append(CorreoConsignacion.abrirTabla("Datafonos con adquirencia Bancolombia", "Tienda",
				"Venta menos comisión"));
		cuerpo.append(filasDatafonos);
		cuerpo.append(CorreoConsignacion.filaTotal("Total datafonos Bancolombia", CorreoConsignacion.pesos(totalTarjetaTiendas)));
		cuerpo.append(CorreoConsignacion.cerrarTabla());

		cuerpo.append(CorreoConsignacion.cerrar("Generado automáticamente por Servicios Pizza Americana el "
				+ new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())
				+ ". El QR ya no va en este correo: se consigna por Bold."));

		final Correo correo = new Correo();
		correo.setAsunto("CONSIGNACION DIARIA WOMPI PAGOS VIRTUALES DESDE " + fechaAnterior + " HASTA " + fechaActual);
		final CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES",
				"CLAVECORREOREPORTE");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje(cuerpo.toString());
		final ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTECONSIGNACIONWOMPI");
		new ControladorEnvioCorreo(correo, correos).enviarCorreoHTML();
	}

	public static boolean validarFestivo(final ArrayList<DiaFestivo> festivos, final String fechaActual) {
		for (int i = 0; i < festivos.size(); i++) {
			if (festivos.get(i).getFechaFestiva().equals(fechaActual)) {
				return true;
			}
		}
		return false;
	}

}
