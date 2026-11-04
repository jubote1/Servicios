package ServiciosSer;

import CapaDAOSer.ParametrosDAO;

/**
 * Reproceso de la conciliacion semanal de pagos virtuales: corre como si hoy
 * fuera general.parametros.FECHAREPROCESO (yyyy-MM-dd).
 *
 * UPDATE general.parametros SET valortexto = '2026-11-03'
 * WHERE valorparametro = 'FECHAREPROCESO';
 *
 * Es una clase de tres lineas a proposito. Hasta noviembre de 2026 era una
 * COPIA COMPLETA de ReporteConciliacionWompi -324 lineas contra 325- que solo
 * se diferenciaba en de donde salia la fecha. Mantener dos copias obliga a
 * acordarse de repetir cada arreglo en las dos, y no pasa: al reporte de Bold
 * le ocurrio justamente eso.
 *
 * Toda la logica vive en ReporteConciliacionWompi.generar(), incluida la
 * advertencia sobre gasto_semanal al reprocesar una semana ya cargada.
 *
 * OJO: FECHAREPROCESO es uno solo y lo comparten todos los reprocesos del
 * servidor. Antes de correr esto hay que mirar que no quedo puesto para otra
 * cosa; por eso la fecha que se resolvio se imprime antes de hacer nada.
 */
public class ReporteConciliacionWompiReproceso {

	public static void main(final String[] args) {
		final String fechaReproceso = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
		if (fechaReproceso == null || fechaReproceso.trim().length() == 0) {
			System.out.println("El parametro FECHAREPROCESO esta vacio. No se hace nada.");
			return;
		}
		System.out.println("Reproceso con FECHAREPROCESO = " + fechaReproceso.trim());
		ReporteConciliacionWompi.generar(fechaReproceso.trim());
	}

}
