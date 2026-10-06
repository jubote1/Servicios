package ServiciosSer;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import CapaDAOSer.ConsignacionRapydDAO;
import CapaDAOSer.ConsignacionRapydDAO.PagoRapyd;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import utilidadesSer.ControladorEnvioCorreo;
import utilidadesSer.CorreoConsignacion;

/**
 * Consignacion semanal de RAPYD (antes PAYU) por los pagos de la tienda virtual.
 *
 * Reemplaza a ReporteConsignacionPAYU, que era quincenal. Rapyd liquida UNA vez por semana, el martes, con lo
 * cobrado entre un lunes y un domingo a las 00:00 UTC. Por eso, por defecto, el reporte corre el martes y cubre
 * del lunes anterior (ejecucion - 8 dias) al domingo anterior (ejecucion - 2 dias).
 *
 * Lo que se descuenta es lo mismo que antes -comision, IVA de la comision, retencion en la fuente y ReteICA-,
 * con los mismos parametros (COMISIONEPAYCO, ADICIONCOMISIONEPAYCO, VALORMINIMOEPAYCO, VALORMINCOMISIONEPAYCO,
 * IVACOMISIONWOMPI, RETENCIONFUENTEWOMPI, RETENCIONICAWOMPI). Si Rapyd cambio las tarifas, se cambian ahi.
 *
 * Parametros nuevos, todos en general.parametros.valornumericod y todos opcionales:
 *   RAPYDDIASVENTANA  dias que cubre cada liquidacion                 (7)
 *   RAPYDDIASFIN      dias entre el ultimo dia cubierto y la ejecucion  (2: ejecutando el martes, termina el domingo)
 *   RAPYDCORTEUTC     1 = el dia termina a las 00:00 UTC (7 PM de Colombia); 0 = por jornada del pedido  (1)
 *
 * El correo trae una tabla por dia con la venta segun el criterio elegido y segun el otro, precisamente para
 * poder cuadrar contra lo que Rapyd consigno de verdad y decidir cual criterio es el correcto.
 *
 * El reproceso (ReporteConsignacionRapydReproceso) permite repetir una semana o pedir fechas exactas.
 */
public class ReporteConsignacionRapyd {

	private static final double VENTANA_DEFECTO = 7;
	private static final double DIAS_FIN_DEFECTO = 2;
	private static final double CORTE_UTC_DEFECTO = 1;
	/** Horas que hay que sumarle a la hora de Colombia para llevarla a UTC. Colombia no tiene horario de verano. */
	private static final int HORAS_COLOMBIA_A_UTC = 5;

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter FORMATO_INSTANTE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static void main(final String[] args) {
		if (args != null && args.length >= 2) {
			generarRango(args[0].trim(), args[1].trim());
			return;
		}
		generar(FORMATO_FECHA.format(LocalDate.now()));
	}

	/** @param fechaEjecucion yyyy-MM-dd, el dia en que "corre" el proceso; el periodo se calcula hacia atras. */
	public static void generar(final String fechaEjecucion) {
		final LocalDate ejecucion;
		try {
			ejecucion = LocalDate.parse(fechaEjecucion.trim(), FORMATO_FECHA);
		} catch (final Exception e) {
			System.out.println("ReporteConsignacionRapyd: fecha de ejecucion ilegible '" + fechaEjecucion + "'");
			return;
		}
		final int ventana = (int) Math.max(1, ParametrosDAO.retornarValorNumericoDouble("RAPYDDIASVENTANA", VENTANA_DEFECTO));
		final int diasFin = (int) Math.max(0, ParametrosDAO.retornarValorNumericoDouble("RAPYDDIASFIN", DIAS_FIN_DEFECTO));
		final LocalDate fin = ejecucion.minusDays(diasFin);
		final LocalDate inicio = fin.minusDays(ventana - 1);
		final boolean noEsMartes = ejecucion.getDayOfWeek() != java.time.DayOfWeek.TUESDAY;
		generar(inicio, fin, noEsMartes);
	}

	/** Un periodo exacto, ambos extremos incluidos (yyyy-MM-dd). Para reprocesar o para la primera liquidacion. */
	public static void generarRango(final String desde, final String hasta) {
		final LocalDate inicio;
		final LocalDate fin;
		try {
			inicio = LocalDate.parse(desde, FORMATO_FECHA);
			fin = LocalDate.parse(hasta, FORMATO_FECHA);
		} catch (final Exception e) {
			System.out.println("ReporteConsignacionRapyd: fechas ilegibles '" + desde + "' '" + hasta + "'");
			return;
		}
		if (fin.isBefore(inicio)) {
			System.out.println("ReporteConsignacionRapyd: la fecha final es anterior a la inicial");
			return;
		}
		generar(inicio, fin, false);
	}

	private static void generar(final LocalDate inicio, final LocalDate fin, final boolean ejecutadoFueraDeMartes) {
		final boolean corteUtc = ParametrosDAO.retornarValorNumericoDouble("RAPYDCORTEUTC", CORTE_UTC_DEFECTO) != 0;
		final String sInicio = FORMATO_FECHA.format(inicio);
		final String sFin = FORMATO_FECHA.format(fin);

		// El tipo de pago (tarjeta o PSE) se marca antes de liquidar, como siempre se hizo.
		PedidoDAO.actualizarPedidosPayu(FORMATO_FECHA.format(inicio.minusDays(1)));

		final ArrayList<PagoRapyd> pagos = ConsignacionRapydDAO.obtenerPagos(sInicio, sFin);
		if (pagos == null) {
			enviar("CONSIGNACION SEMANAL RAPYD DESDE " + sInicio + " HASTA " + sFin + " - ERROR",
					CorreoConsignacion.abrir("Consignación semanal RAPYD", "Periodo " + sInicio + " a " + sFin)
							+ CorreoConsignacion.aviso("No se pudo consultar la base de datos. NO hay cifras: repita el "
									+ "reproceso cuando la base esté disponible.")
							+ CorreoConsignacion.cerrar("Generado automáticamente por Servicios Pizza Americana."));
			return;
		}

		// Tarifas: las mismas que se usaban con PAYU. Si Rapyd cambio algo, se cambia en estos parametros.
		final Tarifas t = new Tarifas();
		t.comision = ParametrosDAO.retornarValorNumericoLocalDouble("COMISIONEPAYCO");
		t.adicion = ParametrosDAO.retornarValorNumericoLocalDouble("ADICIONCOMISIONEPAYCO");
		t.valorMinimo = ParametrosDAO.retornarValorNumericoLocalDouble("VALORMINIMOEPAYCO");
		t.comisionMinima = ParametrosDAO.retornarValorNumericoLocalDouble("VALORMINCOMISIONEPAYCO");
		t.iva = ParametrosDAO.retornarValorNumericoLocalDouble("IVACOMISIONWOMPI");
		t.retefuente = ParametrosDAO.retornarValorNumericoLocalDouble("RETENCIONFUENTEWOMPI");
		t.reteica = ParametrosDAO.retornarValorNumericoLocalDouble("RETENCIONICAWOMPI");

		final Totales general = new Totales();
		final Map<String, Totales> porTienda = new TreeMap<String, Totales>();
		// Por dia, segun el criterio elegido; y la venta de cada dia segun el criterio contrario.
		final Map<LocalDate, Totales> porDia = new TreeMap<LocalDate, Totales>();
		final Map<LocalDate, Double> ventaOtroCriterio = new TreeMap<LocalDate, Double>();
		int sinTipoPago = 0;
		int sinInstante = 0;
		for (final PagoRapyd p : pagos) {
			final LocalDate diaJornada = parseFecha(p.fechaPedido);
			final LocalDate diaUtc = diaUtc(p, diaJornada);
			if (p.instante == null) {
				sinInstante++;
			}
			final LocalDate diaElegido = corteUtc ? diaUtc : diaJornada;
			final LocalDate diaOtro = corteUtc ? diaJornada : diaUtc;
			if (diaElegido == null || diaOtro == null) {
				continue;
			}
			sumar(ventaOtroCriterio, diaOtro, p.total);
			final double[] liq = liquidar(p, t);
			if (p.tipoPago == null) {
				sinTipoPago++;
			}
			Totales dia = porDia.get(diaElegido);
			if (dia == null) {
				dia = new Totales();
				porDia.put(diaElegido, dia);
			}
			dia.sumar(p.total, liq);
			if (diaElegido.isBefore(inicio) || diaElegido.isAfter(fin)) {
				continue;
			}
			general.sumar(p.total, liq);
			Totales tienda = porTienda.get(p.tienda);
			if (tienda == null) {
				tienda = new Totales();
				porTienda.put(p.tienda, tienda);
			}
			tienda.sumar(p.total, liq);
		}

		final String criterio = corteUtc
				? "corte a las 00:00 UTC (7:00 PM hora Colombia)"
				: "por jornada del pedido";
		final StringBuilder cuerpo = new StringBuilder();
		cuerpo.append(CorreoConsignacion.abrir("Consignación semanal RAPYD",
				"Pagos de la tienda virtual del " + sInicio + " al " + sFin + ", " + criterio));
		if (ejecutadoFueraDeMartes) {
			cuerpo.append(CorreoConsignacion.aviso("Este reporte se ejecutó un día distinto al martes. Verifique que "
					+ "el periodo " + sInicio + " a " + sFin + " es el que Rapyd liquida."));
		}
		if (sinTipoPago > 0) {
			cuerpo.append(CorreoConsignacion.aviso(sinTipoPago + " pedido(s) no traen tipo de pago y se liquidaron "
					+ "como tarjeta."));
		}
		if (sinInstante > 0) {
			cuerpo.append(CorreoConsignacion.aviso(sinInstante + " pedido(s) no traen hora de pago; se ubicaron por "
					+ "su jornada."));
		}
		cuerpo.append(CorreoConsignacion.tarjetaTotal("Total a consignar RAPYD", general.neto,
				CorreoConsignacion.entero(general.pedidos) + " pedido(s), venta "
						+ CorreoConsignacion.pesos(general.venta)));

		cuerpo.append(CorreoConsignacion.abrirTabla("Liquidación", "Concepto", "Valor"));
		cuerpo.append(CorreoConsignacion.fila("Pedidos pagados", CorreoConsignacion.entero(general.pedidos)));
		cuerpo.append(CorreoConsignacion.fila("Venta", CorreoConsignacion.pesos(general.venta)));
		cuerpo.append(CorreoConsignacion.fila("(-) Comisión", CorreoConsignacion.pesos(general.comision)));
		cuerpo.append(CorreoConsignacion.fila("(-) IVA de la comisión", CorreoConsignacion.pesos(general.iva)));
		cuerpo.append(CorreoConsignacion.fila("(-) Retención en la fuente",
				CorreoConsignacion.pesos(general.retefuente)));
		cuerpo.append(CorreoConsignacion.fila("(-) ReteICA", CorreoConsignacion.pesos(general.reteica)));
		cuerpo.append(CorreoConsignacion.filaTotal("Valor a consignar", CorreoConsignacion.pesos(general.neto)));
		cuerpo.append(CorreoConsignacion.cerrarTabla());

		cuerpo.append(CorreoConsignacion.abrirTabla("Por tienda", "Tienda", "Pedidos", "Venta", "A consignar"));
		for (final Map.Entry<String, Totales> e : porTienda.entrySet()) {
			cuerpo.append(CorreoConsignacion.fila(e.getKey(), CorreoConsignacion.entero(e.getValue().pedidos),
					CorreoConsignacion.pesos(e.getValue().venta), CorreoConsignacion.pesos(e.getValue().neto)));
		}
		cuerpo.append(CorreoConsignacion.filaTotal("TOTAL", CorreoConsignacion.entero(general.pedidos),
				CorreoConsignacion.pesos(general.venta), CorreoConsignacion.pesos(general.neto)));
		cuerpo.append(CorreoConsignacion.cerrarTabla());

		// Para cuadrar contra lo que Rapyd consigno de verdad: dia por dia, con un dia de margen a cada lado.
		cuerpo.append(CorreoConsignacion.abrirTabla("Día por día (para cuadrar contra Rapyd)", "Día", "Pedidos",
				"Venta", "A consignar", corteUtc ? "Venta por jornada" : "Venta con corte UTC"));
		final Map<LocalDate, Boolean> dias = new LinkedHashMap<LocalDate, Boolean>();
		for (LocalDate d = inicio.minusDays(1); !d.isAfter(fin.plusDays(1)); d = d.plusDays(1)) {
			dias.put(d, Boolean.valueOf(!d.isBefore(inicio) && !d.isAfter(fin)));
		}
		for (final Map.Entry<LocalDate, Boolean> e : dias.entrySet()) {
			final Totales dia = porDia.containsKey(e.getKey()) ? porDia.get(e.getKey()) : new Totales();
			final Double otro = ventaOtroCriterio.get(e.getKey());
			cuerpo.append(CorreoConsignacion.fila(
					FORMATO_FECHA.format(e.getKey()) + (e.getValue().booleanValue() ? "" : " (fuera del periodo)"),
					CorreoConsignacion.entero(dia.pedidos), CorreoConsignacion.pesos(dia.venta),
					CorreoConsignacion.pesos(dia.neto), CorreoConsignacion.pesos(otro == null ? 0 : otro.doubleValue())));
		}
		cuerpo.append(CorreoConsignacion.cerrarTabla());

		cuerpo.append(CorreoConsignacion.aviso("Tarifas usadas: comisión " + CorreoConsignacion.porcentaje(t.comision)
				+ " + " + CorreoConsignacion.pesos(t.adicion) + " por pago (PSE por debajo de "
				+ CorreoConsignacion.pesos(t.valorMinimo) + ": " + CorreoConsignacion.pesos(t.comisionMinima)
				+ "), IVA " + CorreoConsignacion.porcentaje(t.iva) + ", retención en la fuente "
				+ CorreoConsignacion.porcentaje(t.retefuente) + " y ReteICA " + CorreoConsignacion.porcentaje(t.reteica)
				+ " (estas dos solo en tarjeta)."));
		cuerpo.append(CorreoConsignacion.cerrar("Generado automáticamente por Servicios Pizza Americana el "
				+ new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "."));

		enviar("CONSIGNACION SEMANAL RAPYD DESDE " + sInicio + " HASTA " + sFin, cuerpo.toString());
	}

	/** Comision, IVA, retencion en la fuente y ReteICA de un pago, en ese orden. Igual que en PAYU. */
	private static double[] liquidar(final PagoRapyd p, final Tarifas t) {
		final boolean pse = "PSE".equals(p.tipoPago);
		final double comision;
		if (pse && p.total < t.valorMinimo) {
			comision = t.comisionMinima;
		} else {
			comision = p.total * (t.comision / 100) + t.adicion;
		}
		final double iva = comision * (t.iva / 100);
		// Tarjeta, o sin tipo de pago: se liquida como tarjeta, que es lo que dejaba el proceso de marcado.
		final boolean tarjeta = !pse && (p.tipoPago == null || "CARD".equals(p.tipoPago));
		final double retefuente = tarjeta ? p.total * (t.retefuente / 100) : 0;
		final double reteica = tarjeta ? p.total * (t.reteica / 100) : 0;
		return new double[] { comision, iva, retefuente, reteica };
	}

	private static LocalDate parseFecha(final String fecha) {
		try {
			return LocalDate.parse(fecha.substring(0, 10), FORMATO_FECHA);
		} catch (final Exception e) {
			return null;
		}
	}

	/** El dia UTC en que cae el pago; si no hay hora, el de su jornada. */
	private static LocalDate diaUtc(final PagoRapyd p, final LocalDate diaJornada) {
		if (p.instante == null) {
			return diaJornada;
		}
		try {
			return LocalDateTime.parse(p.instante, FORMATO_INSTANTE).plusHours(HORAS_COLOMBIA_A_UTC).toLocalDate();
		} catch (final Exception e) {
			return diaJornada;
		}
	}

	private static void sumar(final Map<LocalDate, Double> mapa, final LocalDate dia, final double valor) {
		final Double actual = mapa.get(dia);
		mapa.put(dia, Double.valueOf((actual == null ? 0 : actual.doubleValue()) + valor));
	}

	private static void enviar(final String asunto, final String html) {
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTECONSIGNACIONRAPYD");
		if (correos.isEmpty()) {
			System.out.println("REPORTECONSIGNACIONRAPYD sin destinatarios, se usan los de REPORTECONSIGNACIONWOMPI");
			correos = GeneralDAO.obtenerCorreosParametro("REPORTECONSIGNACIONWOMPI");
		}
		if (correos.isEmpty()) {
			System.out.println("Sin destinatarios para la consignacion Rapyd, no se envia el correo");
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

	private static class Tarifas {
		double comision;
		double adicion;
		double valorMinimo;
		double comisionMinima;
		double iva;
		double retefuente;
		double reteica;
	}

	private static class Totales {
		int pedidos;
		double venta;
		double comision;
		double iva;
		double retefuente;
		double reteica;
		double neto;

		void sumar(final double total, final double[] liq) {
			pedidos++;
			venta += total;
			comision += liq[0];
			iva += liq[1];
			retefuente += liq[2];
			reteica += liq[3];
			neto += total - liq[0] - liq[1] - liq[2] - liq[3];
		}
	}
}
