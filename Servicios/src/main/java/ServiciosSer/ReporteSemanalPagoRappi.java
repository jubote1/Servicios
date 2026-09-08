package ServiciosSer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import CapaDAOSer.GastoSemanalDAO;
import CapaDAOSer.ParametrosDAO;
import ModeloSer.GastoSemanal;
import capaModeloCC.RazonSocial;

/**
 * Reporte de PAGO semanal de RAPPI.
 *
 * Es el mismo reporte que ReporteSemanalRappi con cuatro diferencias, y por eso
 * HEREDA de él en vez de ser otra copia del programa completo. Antes eran dos
 * gemelos de casi 300 líneas: un arreglo en uno no llegaba al otro, y nadie se
 * daba cuenta.
 *
 * Lo que cambia:
 *
 *   - El corte va MIÉRCOLES y el período liquidado es de sábado a viernes de la
 *     semana anterior. El otro reporte corta domingo, de lunes a domingo. Son
 *     los cortes de cierre de Rappi, no una decisión nuestra.
 *   - Con RAPPIFULLSERVICE en S se ocultan los pagos en línea, la tarifa de
 *     servicio y la propina, y la tarifa NO se descuenta de la consignación.
 *   - Solo registra el concepto de comisión en gasto_semanal, no los tres.
 *   - Otro asunto y otra lista de correo.
 *
 * El bloque de Rappi Cargo es el mismo, calculado con el período de este
 * reporte. Ojo con eso: los dos reportes descuentan el costo de Cargo de su
 * propia consignación estimada, con períodos distintos, porque son dos vistas
 * independientes que cada área concilia por separado.
 */
public class ReporteSemanalPagoRappi extends ReporteSemanalRappi {

	/** Parámetro que dice si la operación va bajo el modelo full service. */
	private static final String PARAM_FULL_SERVICE = "RAPPIFULLSERVICE";

	/** Del miércoles del corte hacia atrás hasta el viernes en que cierra la semana. */
	private static final int DIAS_HASTA_EL_CIERRE = 5;

	/** Y de ese viernes hacia atrás hasta el sábado en que abre. */
	private static final int DIAS_DEL_PERIODO = 6;

	/**
	 * Si la operación es full service.
	 *
	 * Se resuelve una vez al construir y no se vuelve a leer: si el parámetro
	 * cambiara en medio de la generación, media razón social saldría con unas
	 * columnas y la otra mitad con otras.
	 */
	private final boolean fullService;

	public ReporteSemanalPagoRappi()
	{
		super();
		this.fullService = "S".equalsIgnoreCase(this.leerFullService());
		System.out.println("ReporteSemanalPagoRappi: full service = " + this.fullService);
	}

	/**
	 * Lee el parámetro sin que un null tumbe el proceso.
	 *
	 * El código anterior hacía strRappiFullService.equals(...) directo sobre lo
	 * que devolvía el DAO, así que un parámetro ausente lanzaba
	 * NullPointerException; como estaba dentro del try general, el proceso
	 * seguía con el valor inicial de la variable -true, full service- SIN avisar
	 * de nada. Aquí se avisa y se asume lo contrario, que es lo conservador:
	 * mostrar todas las columnas y descontar la tarifa.
	 */
	private String leerFullService()
	{
		try
		{
			final String valor = ParametrosDAO.retornarValorAlfanumerico(PARAM_FULL_SERVICE);
			return(valor == null ? "" : valor.trim());
		}catch(Exception e)
		{
			System.out.println("ReporteSemanalPagoRappi: no se pudo leer " + PARAM_FULL_SERVICE
					+ ", se asume que NO es full service: " + e);
			return("");
		}
	}

	/** El corte va miércoles, y la semana que se liquida es de sábado a viernes. */
	@Override
	protected String[] periodo(final String fechaCorte)
	{
		final Calendar calendario = this.aCalendario(fechaCorte);
		if(calendario == null)
		{
			return(null);
		}
		if(calendario.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY)
		{
			System.out.println("ReporteSemanalPagoRappi: el corte " + fechaCorte
					+ " no cae miércoles, no se procesa.");
			return(null);
		}
		final SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		calendario.add(Calendar.DAY_OF_YEAR, -DIAS_HASTA_EL_CIERRE);
		final String fin = formatoFecha.format(calendario.getTime());
		calendario.add(Calendar.DAY_OF_YEAR, -DIAS_DEL_PERIODO);
		return(new String[]{ formatoFecha.format(calendario.getTime()), fin });
	}

	@Override
	protected String tituloReporte()
	{
		return("REPORTE PAGO SEMANAL RAPPI");
	}

	@Override
	protected String asuntoCorreo(final RazonSocial razTemp)
	{
		return("REPORTE PAGO SEMANAL RAPPI de la Razón Social "
				+ razTemp.getNombreRazon() + " " + razTemp.getIdentificacion());
	}

	@Override
	protected String parametroCorreos()
	{
		return("REPORTEPAGORAPPI");
	}

	/** En full service no aplican los pagos en línea, ni la tarifa, ni la propina. */
	@Override
	protected boolean columnasCompletas()
	{
		return(!this.fullService);
	}

	/** En full service la tarifa de servicio no se descuenta de la consignación. */
	@Override
	protected boolean restarTarifaServicio()
	{
		return(!this.fullService);
	}

	/** Este reporte solo registra la comisión: ni el recaudo ni los descuentos. */
	@Override
	protected void insertarGastos(final int idTienda, final String fechaActual, final double pagosONLINE,
			final double comision, final double gastoPagoONLINE, final double descuento)
	{
		GastoSemanalDAO.insertarGastoSemanal(new GastoSemanal(0, idTienda, CONCEPTO_COMISION,
				fechaActual, comision + gastoPagoONLINE, comision + gastoPagoONLINE));
	}

	public static void main(String[] args)
	{
		new ReporteSemanalPagoRappi().generarReporteRappi();
	}
}
