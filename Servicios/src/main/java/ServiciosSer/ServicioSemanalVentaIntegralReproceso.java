package ServiciosSer;

/**
 * Reprocesa el cierre semanal de Venta Integral para la fecha de corte
 * guardada en el parametro FECHAREPROCESOVENTAINTEGRAL (formato aaaa-mm-dd,
 * debe caer domingo o lunes). Toda la logica vive en
 * ServicioSemanalVentaIntegral, igual que ReporteSemanalRappi /
 * ReporteSemanalRappiReproceso: aqui no se duplica nada, solo se dispara.
 */
public class ServicioSemanalVentaIntegralReproceso {

	public static void main(String[] args) {
		new ServicioSemanalVentaIntegral().reprocesar();
	}
}
