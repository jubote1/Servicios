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
	private static final int CONCEPTO_RECAUDO_ONLINE = 34;

	private static final int CONCEPTO_COMISION = 18;

	private static final int CONCEPTO_DESCUENTO_ASUMIDO = 23;

	/** Lo que se le paga a Rappi Cargo por cada domicilio que lleva. */
	private static final String PARAM_VALOR_DOMICILIO_CARGO = "VALORDOMICILIOCARGO";

	private static final double VALOR_DOMICILIO_CARGO_DEFECTO = 8500;

	/** Lo que cobra Rappi Cargo por manejar el efectivo que recauda, en porcentaje. */
	private static final String PARAM_PORCENTAJE_EFECTIVO_CARGO = "PORCENTAJEEFECTIVOCARGO";

	private static final double PORCENTAJE_EFECTIVO_CARGO_DEFECTO = 2.8;

	private final DecimalFormat formatea = new DecimalFormat("$ ###,###,###");

	/**
	 * Calcula las fechas de la semana que cerró y genera el reporte.
	 *
	 * OJO con el día: el mensaje decía "este proceso debe correr es lo miercoles"
	 * pero la condición acepta DAY_OF_WEEK 1 y 2, que son domingo y lunes. Se
	 * respeta la condición, que es la que ha estado corriendo, y se corrige el
	 * mensaje para que diga la verdad.
	 */
	public void generarReporteRappi()
	{
		final SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		final String fechaActual = formatoFecha.format(Calendar.getInstance().getTime());
		final String fechaAnterior = fechaInicioDelPeriodo(fechaActual);
		if(fechaAnterior == null)
		{
			return;
		}
		this.generar(fechaAnterior, fechaActual);
	}

	/**
	 * La fecha en que arranca el período que cierra en fechaCorte, o null si esa
	 * fecha no es un día válido de corte.
	 *
	 * La regla es la que siempre ha corrido: el corte va domingo o lunes, y se
	 * retroceden 6 días si es domingo y 7 si es lunes. Vive aquí y no repetida en
	 * el reproceso, que es donde estaba copiada y donde se podía torcer sin que
	 * nadie lo notara: el reproceso solo se ejecuta cuando algo ya salió mal.
	 */
	public static String fechaInicioDelPeriodo(final String fechaCorte)
	{
		if(fechaCorte == null || fechaCorte.trim().length() < 10)
		{
			System.out.println("ReporteSemanalRappi: la fecha de corte no es utilizable: '" + fechaCorte + "'");
			return(null);
		}
		final SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		final Calendar calendario = Calendar.getInstance();
		try
		{
			calendario.setTime(formatoFecha.parse(fechaCorte.trim()));
		}catch(Exception e)
		{
			System.out.println("ReporteSemanalRappi: no se pudo interpretar la fecha '" + fechaCorte + "': " + e);
			return(null);
		}
		final int dia = calendario.get(Calendar.DAY_OF_WEEK);
		if(dia != Calendar.SUNDAY && dia != Calendar.MONDAY)
		{
			System.out.println("ReporteSemanalRappi: el corte " + fechaCorte
					+ " no cae domingo ni lunes, no se procesa.");
			return(null);
		}
		calendario.add(Calendar.DAY_OF_YEAR, (dia == Calendar.SUNDAY ? -6 : -7));
		return(formatoFecha.format(calendario.getTime()));
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
	private void generarRazon(final RazonSocial razTemp, final String fechaAnterior, final String fechaActual,
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

			filasDetalle.append(this.filaDetalle(tiendaTemp.getNombreTienda(), totalPedidoTienda, totalPagosONLINE,
					totalDescuento, totalTarifaServicio, totalPropina, totalComision, totalGastoPagoONLINE));
			if(cantidadCargo > 0)
			{
				filasCargo.append(this.filaCargo(tiendaTemp.getNombreTienda(), cantidadCargo, efectivoCargo,
						noEfectivoCargo, valorDomicilioCargo, porcentajeEfectivoCargo));
			}

			GastoSemanalDAO.insertarGastoSemanal(new GastoSemanal(0, tiendaTemp.getIdTienda(),
					CONCEPTO_RECAUDO_ONLINE, fechaActual, totalPagosONLINE, totalPagosONLINE));
			GastoSemanalDAO.insertarGastoSemanal(new GastoSemanal(0, tiendaTemp.getIdTienda(), CONCEPTO_COMISION,
					fechaActual, totalComision + totalGastoPagoONLINE, totalComision + totalGastoPagoONLINE));
			GastoSemanalDAO.insertarGastoSemanal(new GastoSemanal(0, tiendaTemp.getIdTienda(),
					CONCEPTO_DESCUENTO_ASUMIDO, fechaActual, totalDescuento, totalDescuento));
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
				- totalTarifaServicioFinal
				+ totalDescuentoFinal
				- costoCargo
				+ efectivoCargoFinal
				- comisionEfectivoCargo;

		//===================================================================
		//El correo: primero el resumen, después el detalle
		//===================================================================
		final StringBuilder html = new StringBuilder();
		html.append("<div style=\"font-family:Verdana,Arial,sans-serif;font-size:12px;color:#222\">");
		html.append(this.banda("REPORTE SEMANAL RAPPI &nbsp;&middot;&nbsp; " + razTemp.getNombreRazon()));
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
		html.append(this.filaResumen("&minus;", "Tarifa de servicio de Rappi", totalTarifaServicioFinal, false));
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
		html.append(this.filaDetalleTotal(totalPedidosFinal, consignacionBruta, totalDescuentoFinal,
				totalTarifaServicioFinal, totalPropinaFinal, totalComisionFinal, totalGastoPagoONLINEFinal));
		html.append("</table>");
		html.append("</div>");

		final Correo correo = new Correo();
		final CorreoElectronico infoCorreo =
				ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		correo.setAsunto("Reporte Facturación Semanal RAPPI de la Razón Social "
				+ razTemp.getNombreRazon() + " " + razTemp.getIdentificacion());
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		final ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEDOMICILIOSRAPPI");
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

	private String encabezadoDetalle()
	{
		final String[] titulos = {"Tienda", "Total pedidos", "Pagos en línea", "Descuentos",
				"Tarifa servicio", "Propina", "Comisión total", "Costo pagos en línea"};
		final StringBuilder fila = new StringBuilder("<tr>");
		for(int i = 0; i < titulos.length; i++)
		{
			fila.append("<td style=\"background:").append(AZUL).append(";color:#FFFFFF;font-weight:bold;")
					.append(i == 0 ? "text-align:left" : "text-align:right").append(";font-size:11px\">")
					.append(titulos[i]).append("</td>");
		}
		return(fila.append("</tr>").toString());
	}

	private String filaDetalle(final String tienda, final double pedidos, final double online,
			final double descuento, final double tarifa, final double propina, final double comision,
			final double costoOnline)
	{
		return("<tr>" + this.celdaTexto(tienda, false)
				+ this.celdaValor(pedidos, false) + this.celdaValor(online, false)
				+ this.celdaValor(descuento, false) + this.celdaValor(tarifa, false)
				+ this.celdaValor(propina, false) + this.celdaValor(comision, false)
				+ this.celdaValor(costoOnline, false) + "</tr>");
	}

	private String filaDetalleTotal(final double pedidos, final double online, final double descuento,
			final double tarifa, final double propina, final double comision, final double costoOnline)
	{
		return("<tr>" + this.celdaTexto("TOTAL", true)
				+ this.celdaValor(pedidos, true) + this.celdaValor(online, true)
				+ this.celdaValor(descuento, true) + this.celdaValor(tarifa, true)
				+ this.celdaValor(propina, true) + this.celdaValor(comision, true)
				+ this.celdaValor(costoOnline, true) + "</tr>");
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
