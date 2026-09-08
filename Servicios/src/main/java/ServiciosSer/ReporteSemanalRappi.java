package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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

	/** Lo que se le paga a Rappi Cargo por cada domicilio que lleva. */
	private static final String PARAM_VALOR_DOMICILIO_CARGO = "VALORDOMICILIOCARGO";

	private static final double VALOR_DOMICILIO_CARGO_DEFECTO = 8500;

	/** Lo que cobra Rappi Cargo por manejar el efectivo que recauda, en porcentaje. */
	private static final String PARAM_PORCENTAJE_EFECTIVO_CARGO = "PORCENTAJEEFECTIVOCARGO";

	private static final double PORCENTAJE_EFECTIVO_CARGO_DEFECTO = 2.8;

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
		final double valorDomicilioCargo = ParametrosDAO.retornarValorNumericoDouble(
				PARAM_VALOR_DOMICILIO_CARGO, VALOR_DOMICILIO_CARGO_DEFECTO);
		final double porcentajeEfectivoCargo = ParametrosDAO.retornarValorNumericoDouble(
				PARAM_PORCENTAJE_EFECTIVO_CARGO, PORCENTAJE_EFECTIVO_CARGO_DEFECTO);
		for(RazonSocial razTemp : razonesSociales)
		{
			try
			{
				this.generarRazon(razTemp, fechaAnterior, fechaActual, marcacionesComision,
						porcentajeIvaComision, valorDomicilioCargo, porcentajeEfectivoCargo);
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
			final double valorDomicilioCargo, final double porcentajeEfectivoCargo)
	{
		final ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendasxRazon(razTemp.getIdRazon());
		final ArrayList pedidosONLINE = PedidoDAO.obtenerPedidosPlataformasONLINETienda(
				razTemp.getIdRazon(), fechaAnterior, fechaActual, MARCACION_RAPPI);
		final ArrayList tercerizados = PedidoDAO.obtenerTercerizadosPorTienda(
				razTemp.getIdRazon(), fechaAnterior, fechaActual);

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
			double efectivoCargo = 0;
			double noEfectivoCargo = 0;
			for(int k = 0; k < tercerizados.size(); k++)
			{
				final String[] resCargo = (String[]) tercerizados.get(k);
				if(resCargo[0].equals(Integer.toString(tiendaTemp.getIdTienda())))
				{
					cantidadCargo = Integer.parseInt(resCargo[1]);
					efectivoCargo = Double.parseDouble(resCargo[2]);
					noEfectivoCargo = Double.parseDouble(resCargo[3]);
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

			filasDetalle.append(this.filaDetalle(tiendaTemp.getNombreTienda(), totalPedidoTienda,
					totalPagosONLINE, totalDescuento, totalTarifaServicio, totalPropina, totalComision,
					totalGastoPagoONLINE, false));
			if(cantidadCargo > 0)
			{
				filasCargo.append(this.filaCargo(tiendaTemp.getNombreTienda(), cantidadCargo, efectivoCargo,
						noEfectivoCargo, valorDomicilioCargo, porcentajeEfectivoCargo));
			}

			//Que conceptos se registran cambia entre reportes: ver insertarGastos.
			this.insertarGastos(tiendaTemp.getIdTienda(), fechaActual, totalPagosONLINE, totalComision,
					totalGastoPagoONLINE, totalDescuento);
		}

		//===================================================================
		//La cuenta de la consignación
		//===================================================================
		//Lo nuevo son las tres últimas líneas. Rappi Cargo lleva domicilios
		//NUESTROS, así que a nosotros nos toca pagarle: ese costo no se factura
		//aparte, se descuenta de lo que ellos nos van a consignar. Y como el
		//domiciliario de Cargo recauda el efectivo del cliente, esa plata queda
		//en poder de Rappi y también entra en la consignación, menos el
		//porcentaje que ellos cobran por manejarla.
		final double consignacionBruta = totalConsignacion;
		final double costoCargo = cantidadCargoFinal * valorDomicilioCargo;
		final double comisionEfectivoCargo = efectivoCargoFinal * (porcentajeEfectivoCargo / 100);
		final double consignacionAproximada = consignacionBruta
				- totalComisionFinal
				- totalGastoPagoONLINEFinal
				- (this.restarTarifaServicio() ? totalTarifaServicioFinal : 0)
				+ totalDescuentoFinal
				- costoCargo
				+ efectivoCargoFinal
				- comisionEfectivoCargo;

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
		html.append(this.filaResumen("&minus;", "Domicilios de Rappi Cargo (" + cantidadCargoFinal + " x "
				+ this.moneda(valorDomicilioCargo) + ")", costoCargo, false));
		html.append(this.filaResumen("+", "Efectivo recaudado por Rappi Cargo", efectivoCargoFinal, false));
		html.append(this.filaResumen("&minus;", "Manejo del efectivo de Cargo ("
				+ new DecimalFormat("###.##").format(porcentajeEfectivoCargo) + "%)",
				comisionEfectivoCargo, false));
		html.append(this.filaResumen("=", "CONSIGNACIÓN APROXIMADA", consignacionAproximada, true));
		html.append("</table>");

		//--- Domicilios llevados por Rappi Cargo ---
		html.append(this.subtitulo("DOMICILIOS LLEVADOS POR RAPPI CARGO"));
		if(cantidadCargoFinal == 0)
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
					costoCargo, comisionEfectivoCargo));
			html.append("</table>");
			html.append("<p style=\"margin:6px 0 18px 0;color:#777;font-size:11px\">El costo de los "
					+ "domicilios se le paga a Rappi y sale de la consignación. El efectivo lo recauda el "
					+ "domiciliario de Cargo, así que entra a la consignación menos el manejo.</p>");
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
		final String[] titulos = {"Tienda", "Domicilios", "Recaudo en efectivo", "Recaudo que no es efectivo",
				"Costo de los domicilios", "Manejo del efectivo"};
		final StringBuilder fila = new StringBuilder("<tr>");
		for(int i = 0; i < titulos.length; i++)
		{
			fila.append("<td style=\"background:").append(AZUL).append(";color:#FFFFFF;font-weight:bold;")
					.append(i == 0 ? "text-align:left" : "text-align:right").append(";font-size:11px\">")
					.append(titulos[i]).append("</td>");
		}
		return(fila.append("</tr>").toString());
	}

	private String filaCargo(final String tienda, final int cantidad, final double efectivo,
			final double noEfectivo, final double valorDomicilio, final double porcentajeEfectivo)
	{
		return("<tr>" + this.celdaTexto(tienda, false)
				+ "<td style=\"text-align:right;border-top:1px solid " + GRIS_BORDE + "\">" + cantidad + "</td>"
				+ this.celdaValor(efectivo, false) + this.celdaValor(noEfectivo, false)
				+ this.celdaValor(cantidad * valorDomicilio, false)
				+ this.celdaValor(efectivo * (porcentajeEfectivo / 100), false) + "</tr>");
	}

	private String filaCargoTotal(final int cantidad, final double efectivo, final double noEfectivo,
			final double costo, final double manejo)
	{
		return("<tr>" + this.celdaTexto("TOTAL", true)
				+ "<td style=\"text-align:right;background:" + GRIS_FONDO + ";font-weight:bold;border-top:2px "
				+ "solid " + AZUL + "\">" + cantidad + "</td>"
				+ this.celdaValor(efectivo, true) + this.celdaValor(noEfectivo, true)
				+ this.celdaValor(costo, true) + this.celdaValor(manejo, true) + "</tr>");
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
