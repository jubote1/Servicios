package ServiciosSer;

/**
 * Reprocesa el reporte de PAGO semanal de RAPPI para la fecha de corte guardada
 * en el parámetro FECHAREPROCESO.
 *
 * Antes era una copia de las 306 líneas de ReporteSemanalPagoRappi, que a su
 * vez era una copia de ReporteSemanalRappi. Cuatro archivos casi idénticos.
 * Ahora la lógica está una sola vez y aquí solo queda el punto de entrada.
 *
 * OJO con la fecha: este reporte corta MIÉRCOLES, y el período que liquida es
 * de sábado a viernes de la semana anterior. Si pone un domingo en
 * FECHAREPROCESO no va a correr; ese es el corte del otro reporte,
 * ReporteSemanalRappiReproceso.
 */
public class ReporteSemanalPagoRappiReproceso {

	public static void main(String[] args)
	{
		new ReporteSemanalPagoRappi().reprocesar();
	}
}
