package ServiciosSer;

/**
 * Reproceso del cierre semanal de consignaciones: corre como si hoy fuera
 * general.parametros.FECHAREPROCESO (yyyy-MM-dd, debe caer domingo o lunes), y por
 * lo tanto reprocesa la semana lunes-domingo que termina en esa fecha (o el domingo
 * anterior, si FECHAREPROCESO cae lunes).
 *
 * UPDATE general.parametros SET valortexto = '2026-09-28'
 * WHERE valorparametro = 'FECHAREPROCESO';  -- reprocesa la semana 2026-09-21 a 2026-09-27
 */
public class ReporteSemanalConsignacionesReproceso {

	public static void main(final String[] args) {
		new ReporteSemanalConsignaciones().reprocesar();
	}

}
