package ServiciosSer;

/**
 * Reprocesa el cierre semanal de Venta Integral para la fecha de corte
 * guardada en general.parametros.FECHAREPROCESO (formato aaaa-mm-dd, debe caer
 * domingo o lunes). Toda la logica vive en ServicioSemanalVentaIntegral, igual
 * que ReporteSemanalRappi / ReporteSemanalRappiReproceso: aqui no se duplica
 * nada, solo se dispara.
 *
 * UPDATE general.parametros SET valortexto = '2026-09-28'
 * WHERE valorparametro = 'FECHAREPROCESO';  -- reprocesa la semana 2026-09-21 a 2026-09-27
 *
 * Es el MISMO parametro que usan los demas reprocesos, a proposito: uno solo
 * para toda la casa. Este comentario decia FECHAREPROCESOVENTAINTEGRAL, que
 * nunca existio ni en el codigo ni en la base, y costo creer que faltaba un
 * parametro cuando lo que fallaba era el comentario.
 */
public class ServicioSemanalVentaIntegralReproceso {

	public static void main(String[] args) {
		new ServicioSemanalVentaIntegral().reprocesar();
	}
}
