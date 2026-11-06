package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import CapaDAOSer.ConsignacionEstimadaDAO;
import CapaDAOSer.GastoSemanalDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.GastoSemanal;
import capaDAOCC.MarcacionComisionDAO;
import capaDAOCC.PedidoDAO;
import capaDAOCC.RazonSocialDAO;
import capaModeloCC.MarcacionComision;
import capaModeloCC.RazonSocial;
import capaModeloCC.Tienda;
import utilidadesSer.ControladorEnvioCorreo;

/**
 * Reporte semanal de facturación de RAPPI, por razón social.
 *
 * Dos cosas sobre la estructura, para que no se vuelva a torcer.
 *
 * La primera: toda la lógica vive en generar(fechaAnterior, fechaActual), y
 * generarReporteRappi() solo calcula las fechas de la semana que cerró.
 * ReporteSemanalRappiReproceso ya no es una copia de este archivo -eran 277
 * líneas duplicadas que solo se diferenciaban en de dónde salía la fecha-, sino
 * que llama a generar() con la fecha del parámetro. Así un arreglo aquí llega
 * al reproceso sin que nadie se acuerde de copiarlo.
 *
 * La segunda: los import de Apache POI que tenía este archivo se quitaron. Eran
 * quince clases HSSF y XSSF y no se usaba ninguna; el reporte siempre ha sido
 * HTML dentro del correo.
 *
 * El correo se manda con charset utf-8, así que los acentos van bien. El
 * archivo tenía trece caracteres de reemplazo grabados -"Comisi?n Total",
 * "CONSIGNACI?N APROXIMADA"- que salían así en el correo a gerencia.
 */
public class ReporteSemanalRappi {

	/** La marcación con la que se identifican los pedidos de RAPPI. */
	private static final int MARCACION_RAPPI = 2;

	/** Azul y rojo de Pizza Americana, y los grises de apoyo. */
	private static final String AZUL = "#20287F";

	private static final String ROJO = "#B70000";

	private static final String GRIS_FONDO = "#F4F4F6";

	private static final String GRIS_BORDE = "#D9D9E0";

	/** Costo de los pagos en línea, como porcentaje del recaudo. */
	private static final double PORCENTAJE_COSTO_PAGO_ONLINE = 6;

	/** Conceptos de gasto_semanal que alimenta este reporte. */
	protected static final int CONCEPTO_RECAUDO_ONLINE = 34;

	protected static final int CONCEPTO_COMISION = 18;

	protected static final int CONCEPTO_DESCUENTO_ASUMIDO = 23;

	/**
	 * RESPALDO para el domicilio de Cargo al que no se le encuentre tarifa real.
	 *
	 * Hasta el 2026-11-06 este parámetro era la tarifa de TODOS los domicilios,
	 * plana. Medido del 3 al 30 de octubre de 2026 contra el portal de Cargo,
	 * esa plana de $8.500 subestimaba el costo en $384.600 (13,2%): solo el 24%
	 * de los pedidos cuesta la tarifa base y hubo siete de más de $15.000.
	 *
	 * Ahora la tarifa real de cada pedido sale del webhook -ver
	 * PedidoDAO.obtenerTercerizadosPorTienda- y esto solo se usa para el pedido
	 * que no la traiga. En octubre no hubo ninguno, pero más vale que exista.
	 */
	private static final String PARAM_VALOR_DOMICILIO_CARGO = "VALORDOMICILIOCARGO";

	private static final double VALOR_DOMICILIO_CARGO_DEFECTO = 10000;

	/**
	 * Lo que cobra Rappi Cargo por manejar el efectivo que recauda, en porcentaje.
	 *
	 * YA NO ENTRA EN LA CUENTA, solo se muestra en el detalle: la tarifa del
	 * webhook ya lo trae adentro (estimated_price = tarifa base + cobro por
	 * distancia + este porcentaje sobre el valor del pedido, comprobado al peso
	 * contra pedidos de octubre). Restarlo aparte sería contarlo dos veces.
	 */
	private static final String PARAM_PORCENTAJE_EFECTIVO_CARGO = "PORCENTAJEEFECTIVOCARGO";

	private static final double PORCENTAJE_EFECTIVO_CARGO_DEFECTO = 2.85;

	/**
	 * IVA de Cargo, en porcentaje. OJO: NO ES 19.
	 *
	 * Cargo no grava toda la tarifa. De las 289 órdenes de octubre, 41 salieron
	 * con IVA en CERO y en el resto el IVA va en proporción a la parte del
	 * servicio que sí es gravada. El IVA efectivo del mes fue $281.116 sobre
	 * $2.623.403 = 10,72%.
	 *
	 * Es un promedio, no una fórmula: el IVA exacto de un pedido solo se sabe
	 * cuando llega el reporte del portal. Conviene recalibrarlo con la
	 * conciliación de los miércoles.
	 */
	private static final String PARAM_PORCENTAJE_IVA_CARGO = "PORCENTAJEIVACARGO";

	private static final double PORCENTAJE_IVA_CARGO_DEFECTO = 10.72;

	/**
	 * Cuánto cobra Cargo de un domicilio cancelado, como porcentaje de lo que
	 * habría costado.
	 *
	 * Rappi cobra la tarifa base completa o el 35% de ella, y cuál de las dos no
	 * se sabe desde el webhook. En octubre cobró $47.502 en 11 de las 26
	 * cancelaciones, que sobre los $249.703 de tarifa estimada de esas 26 da el
	 * 19%. Otro promedio para recalibrar.
	 *
	 * Antes las canceladas sencillamente no se contaban y el costo salía corto.
	 */
	private static final String PARAM_PORCENTAJE_CANCELACION_CARGO = "PORCENTAJECANCELACIONCARGO";

	private static final double PORCENTAJE_CANCELACION_CARGO_DEFECTO = 19;

	/**
	 * Qué parte de un pedido de la PLATAFORMA Rappi cancelado nos cobra Rappi.
	 *
	 * No confundir con el de Cargo, que es otro renglón: este es un pedido de
	 * Rappi que no se entregó y aparece en su reporte como "Costo Canceladas".
	 *
	 * En octubre nuestra base tenía 34 pedidos de Rappi cancelados por
	 * $1.799.650 y Rappi cobró $1.483.658, el 82,4%. Ojo: Rappi cobró 46
	 * cancelaciones y nosotros solo tenemos 34 marcadas, así que el porcentaje
	 * también está tapando ese hueco.
	 */
	private static final String PARAM_PORCENTAJE_CANCELADAS_RAPPI = "PORCENTAJECANCELADASRAPPI";

	private static final double PORCENTAJE_CANCELADAS_RAPPI_DEFECTO = 82;

	/**
	 * Las compensaciones de Rappi, como porcentaje de la venta.
	 *
	 * Cuando un cliente se queja -pizza fría, producto faltante, producto
	 * cambiado- Rappi le devuelve la plata al cliente y nos la descuenta. Son
	 * las mismas del correo semanal de quejas.
	 *
	 * ESTE ES UN PROMEDIO PURO, no un cálculo: no hay forma de saber desde
	 * nuestra base cuánto va a compensar Rappi, porque es decisión de ellos caso
	 * por caso y nos enteramos cuando llega el reporte. $958.290 sobre
	 * $122.563.000 de octubre = 0,78%. Poniéndolo en 0 se apaga la estimación.
	 */
	private static final String PARAM_PORCENTAJE_COMPENSACIONES = "PORCENTAJECOMPENSACIONESRAPPI";

	private static final double PORCENTAJE_COMPENSACIONES_DEFECTO = 0.78;

	private final DecimalFormat formatea = new DecimalFormat("$ ###,###,###");
	/** Calcula el período que cerró con el corte de hoy y genera el reporte. */
	public void generarReporteRappi()
	{
		this.generarParaCorte(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
	}

	/**
	 * Genera el reporte para una fecha de corte, sea la de hoy o la que traiga
	 * el reproceso. Es el punto de entrada de las dos formas de ejecutarlo.
	 */
	public void generarParaCorte(final String fechaCorte)
	{
		final String[] rango = this.periodo(fechaCorte);
		if(rango == null)
		{
			return;
		}
		System.out.println(this.getClass().getSimpleName() + ": período " + rango[0] + " a " + rango[1]);
		this.generar(rango[0], rango[1]);
	}

	/** Parámetro con la fecha de corte a reprocesar, en aaaa-mm-dd. */
	private static final String PARAM_FECHA_REPROCESO = "FECHAREPROCESO";

	/**
	 * Corre el reporte con la fecha de corte guardada en FECHAREPROCESO.
	 *
	 * Lo usan los dos reprocesos, y por eso vive aquí: la lógica del reproceso
	 * era una copia del programa completo en cada uno, y era justo donde menos
	 * se notaba que estuviera vieja, porque un reproceso solo se ejecuta cuando
	 * algo ya salió mal y ahí nadie está comparando.
	 *
	 * La validación del día la hace periodo(), que cada reporte sobrescribe con
	 * su propio corte. Así el reproceso no necesita saber si el corte va
	 * domingo o miércoles.
	 */
	public void reprocesar()
	{
		final String corte = ParametrosDAO.retornarValorAlfanumerico(PARAM_FECHA_REPROCESO);
		if(corte == null || corte.trim().length() < 10)
		{
			System.out.println(this.getClass().getSimpleName() + ": el parámetro " + PARAM_FECHA_REPROCESO
					+ " no tiene una fecha utilizable: '" + corte + "'");
			return;
		}
		this.generarParaCorte(corte.trim());
	}

	/**
	 * El período que corresponde a una fecha de corte: {inicio, fin}, o null si
	 * esa fecha no es un día válido de corte.
	 *
	 * La regla de RAPPI es la que siempre ha corrido: el corte va domingo o
	 * lunes, y se retroceden 6 días si es domingo y 7 si es lunes. Son los
	 * cortes de cierre de Rappi y no una decisión nuestra, así que no se
	 * "corrigen" aunque con lunes el período quede de 8 días y repita el lunes
	 * anterior. Verificado en gasto_semanal: todas las corridas historicas han
	 * sido en domingo, esa rama nunca se ha usado.
	 *
	 * Ojo: el mensaje anterior decía "debe correr es lo miercoles", que era
	 * falso aquí y estaba copiado de ReporteSemanalPagoRappi, donde sí es
	 * miércoles. Costaba tiempo cada vez que alguien intentaba reprocesar.
	 *
	 * ReporteSemanalPagoRappi lo sobrescribe: tiene otro corte y otro período.
	 */
	protected String[] periodo(final String fechaCorte)
	{
		final Calendar calendario = this.aCalendario(fechaCorte);
		if(calendario == null)
		{
			return(null);
		}
		final int dia = calendario.get(Calendar.DAY_OF_WEEK);
		if(dia != Calendar.SUNDAY && dia != Calendar.MONDAY)
		{
			System.out.println(this.getClass().getSimpleName() + ": el corte " + fechaCorte
					+ " no cae domingo ni lunes, no se procesa.");
			return(null);
		}
		final SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		final String fin = formatoFecha.format(calendario.getTime());
		calendario.add(Calendar.DAY_OF_YEAR, (dia == Calendar.SUNDAY ? -6 : -7));
		return(new String[]{ formatoFecha.format(calendario.getTime()), fin });
	}

	/** La fecha en un Calendar, o null avisando por qué no se pudo. */
	protected Calendar aCalendario(final String fecha)
	{
		if(fecha == null || fecha.trim().length() < 10)
		{
			System.out.println(this.getClass().getSimpleName()
					+ ": la fecha de corte no es utilizable: '" + fecha + "'");
			return(null);
		}
		final Calendar calendario = Calendar.getInstance();
		try
		{
			calendario.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(fecha.trim()));
		}catch(Exception e)
		{
			System.out.println(this.getClass().getSimpleName()
					+ ": no se pudo interpretar la fecha '" + fecha + "': " + e);
			return(null);
		}
		return(calendario);
	}

	//=======================================================================
	//Los puntos que cambian entre este reporte y ReporteSemanalPagoRappi.
	//Estan aqui como metodos para que la subclase los sobrescriba, en vez de
	//que exista una segunda copia del programa completo, que es lo que habia.
	//=======================================================================

	/** Título de la banda azul del correo. */
	protected String tituloReporte()
	{
		return("REPORTE SEMANAL RAPPI");
	}

	/** Asunto del correo. */
	protected String asuntoCorreo(final RazonSocial razTemp)
	{
		return("Reporte Facturación Semanal RAPPI de la Razón Social "
				+ razTemp.getNombreRazon() + " " + razTemp.getIdentificacion());
	}

	/** Parámetro que trae la lista de destinatarios. */
	protected String parametroCorreos()
	{
		return("REPORTEDOMICILIOSRAPPI");
	}

	/**
	 * Si el detalle muestra todas las columnas.
	 *
	 * En el reporte de pago, con RAPPIFULLSERVICE en S, se ocultan los pagos en
	 * línea, la tarifa de servicio y la propina porque en ese modelo no aplican.
	 */
	protected boolean columnasCompletas()
	{
		return(true);
	}

	/** Si la tarifa de servicio se descuenta de la consignación. */
	protected boolean restarTarifaServicio()
	{
		return(true);
	}

	/** Los gastos que este reporte deja registrados por tienda. */
	protected void insertarGastos(final int idTienda, final String fechaActual, final double pagosONLINE,
			final double comision, final double gastoPagoONLINE, final double descuento)
	{
		GastoSemanalDAO.insertarGastoSemanal(new GastoSemanal(0, idTienda,
				CONCEPTO_RECAUDO_ONLINE, fechaActual, pagosONLINE, pagosONLINE));
		GastoSemanalDAO.insertarGastoSemanal(new GastoSemanal(0, idTienda, CONCEPTO_COMISION,
				fechaActual, comision + gastoPagoONLINE, comision + gastoPagoONLINE));
		GastoSemanalDAO.insertarGastoSemanal(new GastoSemanal(0, idTienda,
				CONCEPTO_DESCUENTO_ASUMIDO, fechaActual, descuento, descuento));
	}

	/**
	 * Los cuatro números con que se liquida Rappi Cargo, leídos una sola vez por
	 * corrida.
	 *
	 * Van juntos porque se usan juntos y porque pasarlos sueltos dejaba a
	 * generarRazon con ocho parámetros, donde es fácil cruzar dos doubles sin
	 * que el compilador diga nada.
	 */
	protected static final class TarifasCargo
	{
		/** Respaldo para el domicilio sin tarifa real en el webhook. */
		final double valorDomicilio;
		/** Solo informativo: la tarifa del webhook ya lo trae adentro. */
		final double porcentajeEfectivo;
		/** IVA efectivo de Cargo. No es 19: ver la constante. */
		final double porcentajeIva;
		/** Qué parte de la tarifa cobra Cargo cuando el pedido se cancela. */
		final double porcentajeCancelacion;
		/** Qué parte de un pedido de la PLATAFORMA Rappi cancelado nos cobran. */
		final double porcentajeCanceladasRappi;
		/** Compensaciones estimadas, como porcentaje de la venta. */
		final double porcentajeCompensaciones;

		private TarifasCargo(final double valorDomicilio, final double porcentajeEfectivo,
				final double porcentajeIva, final double porcentajeCancelacion,
				final double porcentajeCanceladasRappi, final double porcentajeCompensaciones)
		{
			this.valorDomicilio = valorDomicilio;
			this.porcentajeEfectivo = porcentajeEfectivo;
			this.porcentajeIva = porcentajeIva;
			this.porcentajeCancelacion = porcentajeCancelacion;
			this.porcentajeCanceladasRappi = porcentajeCanceladasRappi;
			this.porcentajeCompensaciones = porcentajeCompensaciones;
		}

		static TarifasCargo leer()
		{
			return(new TarifasCargo(
					ParametrosDAO.retornarValorNumericoDouble(
							PARAM_VALOR_DOMICILIO_CARGO, VALOR_DOMICILIO_CARGO_DEFECTO),
					ParametrosDAO.retornarValorNumericoDouble(
							PARAM_PORCENTAJE_EFECTIVO_CARGO, PORCENTAJE_EFECTIVO_CARGO_DEFECTO),
					ParametrosDAO.retornarValorNumericoDouble(
							PARAM_PORCENTAJE_IVA_CARGO, PORCENTAJE_IVA_CARGO_DEFECTO),
					ParametrosDAO.retornarValorNumericoDouble(
							PARAM_PORCENTAJE_CANCELACION_CARGO, PORCENTAJE_CANCELACION_CARGO_DEFECTO),
					ParametrosDAO.retornarValorNumericoDouble(
							PARAM_PORCENTAJE_CANCELADAS_RAPPI, PORCENTAJE_CANCELADAS_RAPPI_DEFECTO),
					ParametrosDAO.retornarValorNumericoDouble(
							PARAM_PORCENTAJE_COMPENSACIONES, PORCENTAJE_COMPENSACIONES_DEFECTO)));
		}

		/** Le aplica el IVA a un valor de Cargo. */
		double conIva(final double valor)
		{
			return(valor * (1 + (this.porcentajeIva / 100)));
		}

		@Override
		public String toString()
		{
			return("tarifas de Cargo: respaldo $" + this.valorDomicilio + " por domicilio, efectivo "
					+ this.porcentajeEfectivo + "%, IVA " + this.porcentajeIva + "%, cancelación "
					+ this.porcentajeCancelacion + "%; plataforma Rappi: canceladas "
					+ this.porcentajeCanceladasRappi + "%, compensaciones "
					+ this.porcentajeCompensaciones + "% de la venta");
		}
	}

	/**
	 * Genera y envía el reporte para el rango de fechas indicado, una razón
	 * social por correo. Este es el punto de entrada que usa el reproceso.
	 */
	public void generar(final String fechaAnterior, final String fechaActual)
	{
		final ArrayList<RazonSocial> razonesSociales = RazonSocialDAO.obtenerRazones();
		final ArrayList<MarcacionComision> marcacionesComision =
				MarcacionComisionDAO.obtenerMarcacionComision(MARCACION_RAPPI);
		double porcentajeIvaComision = 19;
		try
		{
			porcentajeIvaComision = (double) ParametrosDAO.retornarValorNumerico("PORCENTAJEIVACOMISION");
		}catch(Exception e)
		{
			System.out.println("ReporteSemanalRappi: sin PORCENTAJEIVACOMISION, se usa 19: " + e);
		}
		final TarifasCargo tarifas = TarifasCargo.leer();
		System.out.println("ReporteSemanalRappi: " + tarifas);
		for(RazonSocial razTemp : razonesSociales)
		{
			try
			{
				this.generarRazon(razTemp, fechaAnterior, fechaActual, marcacionesComision,
						porcentajeIvaComision, tarifas);
			}catch(Exception e)
			{
				//Que una razón social falle no puede dejar sin reporte a las demás.
				System.out.println("ReporteSemanalRappi: fallo la razón " + razTemp.getNombreRazon() + ": " + e);
			}
		}
	}

	/** Arma y envía el correo de una razón social. */
	protected void generarRazon(final RazonSocial razTemp, final String fechaAnterior, final String fechaActual,
			final ArrayList<MarcacionComision> marcacionesComision, final double porcentajeIvaComision,
			final TarifasCargo tarifas)
	{
		final ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendasxRazon(razTemp.getIdRazon());
		final ArrayList pedidosONLINE = PedidoDAO.obtenerPedidosPlataformasONLINETienda(
				razTemp.getIdRazon(), fechaAnterior, fechaActual, MARCACION_RAPPI);
		final ArrayList tercerizados = PedidoDAO.obtenerTercerizadosPorTienda(
				razTemp.getIdRazon(), fechaAnterior, fechaActual);
		final ArrayList canceladosPlataforma = PedidoDAO.obtenerCanceladosPlataformaPorTienda(
				razTemp.getIdRazon(), fechaAnterior, fechaActual, MARCACION_RAPPI);

		final StringBuilder filasDetalle = new StringBuilder();
		final StringBuilder filasCargo = new StringBuilder();

		double totalComisionFinal = 0;
		double totalGastoPagoONLINEFinal = 0;
		double totalTarifaServicioFinal = 0;
		double totalDescuentoFinal = 0;
		double totalConsignacion = 0;
		double totalPedidosFinal = 0;
		double totalPropinaFinal = 0;
		int cantidadCargoFinal = 0;
		double efectivoCargoFinal = 0;
		double noEfectivoCargoFinal = 0;
		//La tarifa real que Cargo le puso a cada pedido, sumada. Antes esto no
		//existia: el costo era cantidad x una tarifa plana.
		double tarifaCargoFinal = 0;
		int canceladosCargoFinal = 0;
		double tarifaCanceladosFinal = 0;
		int sinTarifaCargoFinal = 0;
		//Pedidos de la PLATAFORMA Rappi cancelados: otro renglon, no son los de Cargo.
		int canceladosPlataformaFinal = 0;
		double valorCanceladosPlataformaFinal = 0;

		for(Tienda tiendaTemp : tiendas)
		{
			double comision = 0;
			double comisionfull = 0;
			for(MarcacionComision marComTemp : marcacionesComision)
			{
				if(marComTemp.getIdTienda() == tiendaTemp.getIdTienda())
				{
					comision = (double) marComTemp.getComision();
					comisionfull = (double) marComTemp.getComisionfull();
					break;
				}
			}
			double totalPedidoTienda = 0;
			double totalDescuento = 0;
			double totalComision = 0;
			double totalTarifaServicio = 0;
			double totalPropina = 0;
			final ArrayList pedidosTienda = PedidoDAO.obtenerPedidosPlataformasTiendaDetallada(
					razTemp.getIdRazon(), fechaAnterior, fechaActual, MARCACION_RAPPI, tiendaTemp.getIdTienda());
			for(int z = 0; z < pedidosTienda.size(); z++)
			{
				final String[] pedTienda = (String[]) pedidosTienda.get(z);
				final double totalPedido = Double.parseDouble(pedTienda[0]);
				totalPedidoTienda = totalPedidoTienda + totalPedido;
				totalDescuento = totalDescuento + Double.parseDouble(pedTienda[4]);
				totalTarifaServicio = totalTarifaServicio + Double.parseDouble(pedTienda[5]);
				totalPropina = totalPropina + Double.parseDouble(pedTienda[6]);
				//El marketplace paga una comisión distinta a la de pedido propio.
				final boolean esMarketplace = "S".equals(pedTienda[2]);
				totalComision = totalComision + (totalPedido * ((esMarketplace ? comision : comisionfull) / 100));
			}
			double totalPagosONLINE = 0;
			for(int k = 0; k < pedidosONLINE.size(); k++)
			{
				final String[] resTotalONLINE = (String[]) pedidosONLINE.get(k);
				if(resTotalONLINE[3].equals(Integer.toString(tiendaTemp.getIdTienda())))
				{
					totalPagosONLINE = Double.parseDouble(resTotalONLINE[1]);
					break;
				}
			}
			int cantidadCargo = 0;
			double tarifaCargo = 0;
			int canceladosCargo = 0;
			double tarifaCancelados = 0;
			int sinTarifaCargo = 0;
			double efectivoCargo = 0;
			double noEfectivoCargo = 0;
			for(int k = 0; k < tercerizados.size(); k++)
			{
				final String[] resCargo = (String[]) tercerizados.get(k);
				if(resCargo[0].equals(Integer.toString(tiendaTemp.getIdTienda())))
				{
					cantidadCargo = Integer.parseInt(resCargo[1]);
					tarifaCargo = Double.parseDouble(resCargo[2]);
					canceladosCargo = Integer.parseInt(resCargo[3]);
					tarifaCancelados = Double.parseDouble(resCargo[4]);
					sinTarifaCargo = Integer.parseInt(resCargo[5]);
					efectivoCargo = Double.parseDouble(resCargo[6]);
					noEfectivoCargo = Double.parseDouble(resCargo[7]);
					break;
				}
			}
			for(int k = 0; k < canceladosPlataforma.size(); k++)
			{
				final String[] resCanc = (String[]) canceladosPlataforma.get(k);
				if(resCanc[0].equals(Integer.toString(tiendaTemp.getIdTienda())))
				{
					canceladosPlataformaFinal = canceladosPlataformaFinal + Integer.parseInt(resCanc[1]);
					valorCanceladosPlataformaFinal =
							valorCanceladosPlataformaFinal + Double.parseDouble(resCanc[2]);
					break;
				}
			}
			totalConsignacion = totalConsignacion + totalPagosONLINE;
			totalComision = totalComision + (totalComision * (porcentajeIvaComision / 100));
			final double totalGastoPagoONLINE = totalPagosONLINE * (PORCENTAJE_COSTO_PAGO_ONLINE / 100);
			totalComisionFinal = totalComisionFinal + totalComision;
			totalGastoPagoONLINEFinal = totalGastoPagoONLINEFinal + totalGastoPagoONLINE;
			totalTarifaServicioFinal = totalTarifaServicioFinal + totalTarifaServicio;
			totalDescuentoFinal = totalDescuentoFinal + totalDescuento;
			totalPedidosFinal = totalPedidosFinal + totalPedidoTienda;
			totalPropinaFinal = totalPropinaFinal + totalPropina;
			cantidadCargoFinal = cantidadCargoFinal + cantidadCargo;
			efectivoCargoFinal = efectivoCargoFinal + efectivoCargo;
			noEfectivoCargoFinal = noEfectivoCargoFinal + noEfectivoCargo;
			tarifaCargoFinal = tarifaCargoFinal + tarifaCargo;
			canceladosCargoFinal = canceladosCargoFinal + canceladosCargo;
			tarifaCanceladosFinal = tarifaCanceladosFinal + tarifaCancelados;
			sinTarifaCargoFinal = sinTarifaCargoFinal + sinTarifaCargo;

			filasDetalle.append(this.filaDetalle(tiendaTemp.getNombreTienda(), totalPedidoTienda,
					totalPagosONLINE, totalDescuento, totalTarifaServicio, totalPropina, totalComision,
					totalGastoPagoONLINE, false));
			if(cantidadCargo > 0 || canceladosCargo > 0)
			{
				filasCargo.append(this.filaCargo(tiendaTemp.getNombreTienda(), cantidadCargo,
						this.costoCargo(tarifaCargo, sinTarifaCargo, tarifas), canceladosCargo,
						tarifas.conIva(tarifaCancelados * (tarifas.porcentajeCancelacion / 100)),
						efectivoCargo, noEfectivoCargo));
			}

			//Que conceptos se registran cambia entre reportes: ver insertarGastos.
			this.insertarGastos(tiendaTemp.getIdTienda(), fechaActual, totalPagosONLINE, totalComision,
					totalGastoPagoONLINE, totalDescuento);
		}

		//===================================================================
		//La cuenta de la consignación
		//===================================================================
		//Las cuatro últimas líneas son las de Cargo. Rappi Cargo lleva domicilios
		//NUESTROS, así que a nosotros nos toca pagarle: ese costo no se factura
		//aparte, se descuenta de lo que ellos nos van a consignar. Y como el
		//domiciliario de Cargo recauda el efectivo del cliente, esa plata queda
		//en poder de Rappi y también entra en la consignación.
		//
		//QUÉ CAMBIÓ EL 2026-11-06
		//
		//  - El costo sale de la tarifa REAL de cada pedido (la del webhook) y ya
		//    no de una tarifa plana que subestimaba un 13,2%.
		//  - YA NO SE RESTA LA COMISIÓN DE EFECTIVO APARTE: la tarifa del webhook
		//    la trae adentro. Restarla otra vez era contarla dos veces.
		//  - Los cancelados SÍ cuentan. Antes se ignoraban y Rappi igual los
		//    cobraba: $47.502 en octubre.
		//
		//Lo que todavía no se ve es el cobro por espera del repartidor, porque la
		//tarifa se fija al crear la orden y la espera pasa después. En octubre esa
		//brecha fue del 4,4% ($192.400 en 87 pedidos). Si se vuelve grande, el
		//camino es un parámetro de ajuste, no adivinar aquí.
		final double consignacionBruta = totalConsignacion;
		final double costoCargo = this.costoCargo(tarifaCargoFinal, sinTarifaCargoFinal, tarifas);
		final double costoCancelados = tarifas.conIva(
				tarifaCanceladosFinal * (tarifas.porcentajeCancelacion / 100));
		final double comisionEfectivoCargo = efectivoCargoFinal * (tarifas.porcentajeEfectivo / 100);
		//Dos descuentos de la PLATAFORMA Rappi que el reporte nunca restaba y que
		//en octubre de 2026 valieron $2.441.948 en cuatro semanas. Ninguno de los
		//dos se sabe exacto antes de que llegue el reporte de Rappi -son decisión
		//de ellos-, así que van estimados y se recalibran con la conciliación.
		final double costoCanceladasPlataforma =
				valorCanceladosPlataformaFinal * (tarifas.porcentajeCanceladasRappi / 100);
		final double compensaciones = totalPedidosFinal * (tarifas.porcentajeCompensaciones / 100);
		final double consignacionAproximada = consignacionBruta
				- totalComisionFinal
				- totalGastoPagoONLINEFinal
				- (this.restarTarifaServicio() ? totalTarifaServicioFinal : 0)
				+ totalDescuentoFinal
				- costoCanceladasPlataforma
				- compensaciones
				- costoCargo
				- costoCancelados
				+ efectivoCargoFinal;

		//Queda guardado para que la conciliación de los miércoles pueda comparar
		//esto contra lo que Rappi consignó de verdad sin ir a buscar el correo.
		ConsignacionEstimadaDAO.guardar(this.nombreProceso(), razTemp, fechaAnterior, fechaActual,
				consignacionBruta, totalComisionFinal, totalGastoPagoONLINEFinal,
				(this.restarTarifaServicio() ? totalTarifaServicioFinal : 0), totalDescuentoFinal,
				costoCanceladasPlataforma, compensaciones,
				cantidadCargoFinal, costoCargo, canceladosCargoFinal, costoCancelados,
				efectivoCargoFinal, comisionEfectivoCargo, consignacionAproximada);

		//===================================================================
		//El correo: primero el resumen, después el detalle
		//===================================================================
		final StringBuilder html = new StringBuilder();
		html.append("<div style=\"font-family:Verdana,Arial,sans-serif;font-size:12px;color:#222\">");
		html.append(this.banda(this.tituloReporte() + " &nbsp;&middot;&nbsp; " + razTemp.getNombreRazon()));
		html.append("<p style=\"margin:8px 0 16px 0;color:#555\">Razón social ")
				.append(razTemp.getIdentificacion())
				.append(" &nbsp;&middot;&nbsp; período del <b>").append(fechaAnterior)
				.append("</b> al <b>").append(fechaActual).append("</b></p>");

		//--- Resumen ---
		html.append(this.subtitulo("RESUMEN DE LA CONSIGNACIÓN"));
		html.append("<table cellspacing=\"0\" cellpadding=\"7\" style=\"border-collapse:collapse;width:520px;"
				+ "border:1px solid " + GRIS_BORDE + "\">");
		html.append(this.filaResumen("", "Recaudo bruto de pagos en línea", consignacionBruta, false));
		html.append(this.filaResumen("&minus;", "Comisión de Rappi (incluye IVA)", totalComisionFinal, false));
		html.append(this.filaResumen("&minus;", "Costo de los pagos en línea", totalGastoPagoONLINEFinal, false));
		if(this.restarTarifaServicio())
		{
			html.append(this.filaResumen("&minus;", "Tarifa de servicio de Rappi",
					totalTarifaServicioFinal, false));
		}
		html.append(this.filaResumen("+", "Descuentos asumidos por Rappi", totalDescuentoFinal, false));
		if(canceladosPlataformaFinal > 0)
		{
			html.append(this.filaResumen("&minus;", "Pedidos de Rappi cancelados ("
					+ canceladosPlataformaFinal + " al "
					+ new DecimalFormat("###.##").format(tarifas.porcentajeCanceladasRappi) + "%)",
					costoCanceladasPlataforma, false));
		}
		if(compensaciones > 0)
		{
			html.append(this.filaResumen("&minus;", "Compensaciones a clientes (estimadas, "
					+ new DecimalFormat("###.##").format(tarifas.porcentajeCompensaciones) + "% de la venta)",
					compensaciones, false));
		}
		html.append(this.filaResumen("&minus;", "Domicilios de Rappi Cargo entregados ("
				+ cantidadCargoFinal + ", tarifa real + IVA)", costoCargo, false));
		if(canceladosCargoFinal > 0)
		{
			html.append(this.filaResumen("&minus;", "Domicilios de Cargo cancelados ("
					+ canceladosCargoFinal + " al "
					+ new DecimalFormat("###.##").format(tarifas.porcentajeCancelacion) + "%)",
					costoCancelados, false));
		}
		html.append(this.filaResumen("+", "Efectivo recaudado por Rappi Cargo", efectivoCargoFinal, false));
		html.append(this.filaResumen("=", "CONSIGNACIÓN APROXIMADA", consignacionAproximada, true));
		html.append("</table>");
		//La comisión del efectivo ya está dentro de la tarifa de cada domicilio,
		//así que se muestra para que se entienda la cuenta pero NO se resta otra
		//vez. Si algún día se vuelve a poner como renglón, se estaría cobrando dos
		//veces y la consignación saldría baja.
		html.append("<p style=\"margin:6px 0 18px 0;color:#777;font-size:11px\">El manejo del efectivo de "
				+ "Cargo (" + new DecimalFormat("###.##").format(tarifas.porcentajeEfectivo) + "%, unos "
				+ this.moneda(comisionEfectivoCargo) + ") ya viene incluido en la tarifa de cada domicilio: "
				+ "no se descuenta aparte.</p>");

		//--- Domicilios llevados por Rappi Cargo ---
		html.append(this.subtitulo("DOMICILIOS LLEVADOS POR RAPPI CARGO"));
		if(cantidadCargoFinal == 0 && canceladosCargoFinal == 0)
		{
			html.append("<p style=\"margin:0 0 18px 0;color:#777\">Ninguna tienda tuvo domicilios "
					+ "tercerizados en el período.</p>");
		}else
		{
			html.append("<table cellspacing=\"0\" cellpadding=\"7\" style=\"border-collapse:collapse;"
					+ "border:1px solid " + GRIS_BORDE + "\">");
			html.append(this.encabezadoCargo());
			html.append(filasCargo);
			html.append(this.filaCargoTotal(cantidadCargoFinal, efectivoCargoFinal, noEfectivoCargoFinal,
					costoCargo, canceladosCargoFinal, costoCancelados));
			html.append("</table>");
			html.append("<p style=\"margin:6px 0 18px 0;color:#777;font-size:11px\">El costo sale de la "
					+ "tarifa que Cargo le puso a CADA pedido, no de un promedio, e incluye el IVA y el "
					+ "manejo del efectivo. El efectivo lo recauda el domiciliario de Cargo, así que queda "
					+ "en poder de Rappi y entra completo a la consignación. Un domicilio cancelado también "
					+ "se cobra: Rappi cobra la tarifa base o el 35% de ella según el caso.</p>");
			if(sinTarifaCargoFinal > 0)
			{
				html.append("<p style=\"margin:6px 0 18px 0;color:#a00;font-size:11px\">Ojo: "
						+ sinTarifaCargoFinal + " domicilio(s) sin tarifa en el webhook de Cargo. A esos se "
						+ "les aplicó el valor de respaldo de " + this.moneda(tarifas.valorDomicilio)
						+ ", así que ese pedazo es estimado.</p>");
			}
		}

		//--- Detalle por tienda ---
		html.append(this.subtitulo("DETALLE POR TIENDA"));
		html.append("<table cellspacing=\"0\" cellpadding=\"7\" style=\"border-collapse:collapse;"
				+ "border:1px solid " + GRIS_BORDE + "\">");
		html.append(this.encabezadoDetalle());
		html.append(filasDetalle);
		html.append(this.filaDetalle("TOTAL", totalPedidosFinal, consignacionBruta, totalDescuentoFinal,
				totalTarifaServicioFinal, totalPropinaFinal, totalComisionFinal,
				totalGastoPagoONLINEFinal, true));
		html.append("</table>");
		html.append("</div>");

		final Correo correo = new Correo();
		final CorreoElectronico infoCorreo =
				ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto(this.asuntoCorreo(razTemp));
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		final ArrayList correos = GeneralDAO.obtenerCorreosParametro(this.parametroCorreos());
		correo.setMensaje(html.toString());
		final ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreoHTML();
	}

	//=======================================================================
	//Armado del HTML. Todo con estilos en línea y tablas, no con hojas de
	//estilo ni flexbox: los clientes de correo -sobre todo Outlook- ignoran
	//buena parte del CSS moderno, y un reporte que se ve bien en el navegador
	//puede llegar descuadrado a la bandeja de quien lo tiene que leer.
	//=======================================================================

	/** Banda azul de encabezado con el título del reporte. */
	private String banda(final String titulo)
	{
		return("<div style=\"background:" + AZUL + ";color:#FFFFFF;padding:12px 16px;font-size:15px;"
				+ "font-weight:bold;letter-spacing:0.5px\">" + titulo + "</div>");
	}

	/** Subtítulo de sección, en rojo y con una línea debajo. */
	private String subtitulo(final String titulo)
	{
		return("<div style=\"margin:18px 0 8px 0;padding-bottom:4px;color:" + ROJO + ";font-size:13px;"
				+ "font-weight:bold;border-bottom:2px solid " + ROJO + ";width:520px\">" + titulo + "</div>");
	}

	/** Una línea del resumen: signo, concepto y valor. La última va destacada. */
	private String filaResumen(final String signo, final String concepto, final double valor,
			final boolean destacada)
	{
		final String fondo = (destacada ? AZUL : "#FFFFFF");
		final String color = (destacada ? "#FFFFFF" : "#222222");
		final String peso = (destacada ? "bold" : "normal");
		final String tamano = (destacada ? "14px" : "12px");
		return("<tr style=\"background:" + fondo + "\">"
				+ "<td style=\"width:24px;text-align:center;color:" + color + ";font-weight:bold;"
				+ "border-top:1px solid " + GRIS_BORDE + "\">" + signo + "</td>"
				+ "<td style=\"color:" + color + ";font-weight:" + peso + ";font-size:" + tamano + ";"
				+ "border-top:1px solid " + GRIS_BORDE + "\">" + concepto + "</td>"
				+ "<td style=\"text-align:right;color:" + color + ";font-weight:" + peso + ";font-size:"
				+ tamano + ";border-top:1px solid " + GRIS_BORDE + "\">" + this.moneda(valor) + "</td></tr>");
	}

	/**
	 * Encabezado del detalle. Las columnas dependen de columnasCompletas(): en
	 * full service no se muestran los pagos en línea, la tarifa ni la propina.
	 */
	private String encabezadoDetalle()
	{
		final StringBuilder fila = new StringBuilder("<tr>");
		fila.append(this.celdaTitulo("Tienda", true));
		fila.append(this.celdaTitulo("Total pedidos", false));
		if(this.columnasCompletas())
		{
			fila.append(this.celdaTitulo("Pagos en línea", false));
		}
		fila.append(this.celdaTitulo("Descuentos", false));
		if(this.columnasCompletas())
		{
			fila.append(this.celdaTitulo("Tarifa servicio", false));
			fila.append(this.celdaTitulo("Propina", false));
		}
		fila.append(this.celdaTitulo("Comisión total", false));
		fila.append(this.celdaTitulo("Costo pagos en línea", false));
		return(fila.append("</tr>").toString());
	}

	/** Una celda de encabezado, sobre la banda azul. */
	private String celdaTitulo(final String titulo, final boolean izquierda)
	{
		return("<td style=\"background:" + AZUL + ";color:#FFFFFF;font-weight:bold;"
				+ (izquierda ? "text-align:left" : "text-align:right") + ";font-size:11px\">"
				+ titulo + "</td>");
	}

	/**
	 * Una fila del detalle. Con total en true sale resaltada.
	 *
	 * Antes eran dos métodos idénticos salvo ese booleano, y las columnas
	 * estaban fijas. Ahora los dos usos comparten el mismo armado, que es lo que
	 * garantiza que el encabezado y las filas no se desalineen cuando el modelo
	 * de full service esconde tres columnas.
	 */
	private String filaDetalle(final String tienda, final double pedidos, final double online,
			final double descuento, final double tarifa, final double propina, final double comision,
			final double costoOnline, final boolean total)
	{
		final StringBuilder fila = new StringBuilder("<tr>");
		fila.append(this.celdaTexto(tienda, total));
		fila.append(this.celdaValor(pedidos, total));
		if(this.columnasCompletas())
		{
			fila.append(this.celdaValor(online, total));
		}
		fila.append(this.celdaValor(descuento, total));
		if(this.columnasCompletas())
		{
			fila.append(this.celdaValor(tarifa, total));
			fila.append(this.celdaValor(propina, total));
		}
		fila.append(this.celdaValor(comision, total));
		fila.append(this.celdaValor(costoOnline, total));
		return(fila.append("</tr>").toString());
	}

	private String encabezadoCargo()
	{
		final String[] titulos = {"Tienda", "Entregados", "Recaudo en efectivo", "Recaudo que no es efectivo",
				"Costo de los entregados", "Cancelados", "Costo de los cancelados"};
		final StringBuilder fila = new StringBuilder("<tr>");
		for(int i = 0; i < titulos.length; i++)
		{
			fila.append("<td style=\"background:").append(AZUL).append(";color:#FFFFFF;font-weight:bold;")
					.append(i == 0 ? "text-align:left" : "text-align:right").append(";font-size:11px\">")
					.append(titulos[i]).append("</td>");
		}
		return(fila.append("</tr>").toString());
	}

	/**
	 * Lo que nos cuestan los domicilios ENTREGADOS de una tienda, con IVA.
	 *
	 * Se suma la tarifa real que Cargo le puso a cada pedido y, para el que no
	 * la traiga, el respaldo del parámetro. Lo normal es que sinTarifa sea cero:
	 * en los 289 pedidos de octubre todos traían tarifa.
	 */
	protected double costoCargo(final double tarifaReal, final int sinTarifa, final TarifasCargo tarifas)
	{
		return(tarifas.conIva(tarifaReal + (sinTarifa * tarifas.valorDomicilio)));
	}

	/** Cómo se llama este proceso en consignacion_semanal_estimada. */
	protected String nombreProceso()
	{
		return("SEMANALRAPPI");
	}

	private String filaCargo(final String tienda, final int entregados, final double costoEntregados,
			final int cancelados, final double costoCancelados, final double efectivo, final double noEfectivo)
	{
		return("<tr>" + this.celdaTexto(tienda, false)
				+ "<td style=\"text-align:right;border-top:1px solid " + GRIS_BORDE + "\">" + entregados + "</td>"
				+ this.celdaValor(efectivo, false) + this.celdaValor(noEfectivo, false)
				+ this.celdaValor(costoEntregados, false)
				+ "<td style=\"text-align:right;border-top:1px solid " + GRIS_BORDE + "\">" + cancelados + "</td>"
				+ this.celdaValor(costoCancelados, false) + "</tr>");
	}

	private String filaCargoTotal(final int entregados, final double efectivo, final double noEfectivo,
			final double costo, final int cancelados, final double costoCancelados)
	{
		final String celdaEntera = "<td style=\"text-align:right;background:" + GRIS_FONDO
				+ ";font-weight:bold;border-top:2px solid " + AZUL + "\">";
		return("<tr>" + this.celdaTexto("TOTAL", true)
				+ celdaEntera + entregados + "</td>"
				+ this.celdaValor(efectivo, true) + this.celdaValor(noEfectivo, true)
				+ this.celdaValor(costo, true)
				+ celdaEntera + cancelados + "</td>"
				+ this.celdaValor(costoCancelados, true) + "</tr>");
	}

	private String celdaTexto(final String texto, final boolean total)
	{
		return("<td style=\"border-top:" + (total ? "2px solid " + AZUL : "1px solid " + GRIS_BORDE) + ";"
				+ (total ? "background:" + GRIS_FONDO + ";font-weight:bold;" : "") + "\">" + texto + "</td>");
	}

	private String celdaValor(final double valor, final boolean total)
	{
		return("<td style=\"text-align:right;border-top:" + (total ? "2px solid " + AZUL : "1px solid "
				+ GRIS_BORDE) + ";" + (total ? "background:" + GRIS_FONDO + ";font-weight:bold;" : "")
				+ "\">" + this.moneda(valor) + "</td>");
	}

	/** El valor en pesos, y un guión cuando es cero, que se lee mejor que "$ 0". */
	private String moneda(final double valor)
	{
		if(valor == 0)
		{
			return("&mdash;");
		}
		return(this.formatea.format(valor));
	}

	public static void main(String[] args)
	{
		final ReporteSemanalRappi reporte = new ReporteSemanalRappi();
		reporte.generarReporteRappi();
	}
}
