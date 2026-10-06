package ServiciosSer;

import CapaDAOSer.ParametrosDAO;

/**
 * Reproceso de la consignacion semanal de RAPYD. Dos formas de usarlo:
 *
 * 1. Con fechas exactas (ambas incluidas), por ejemplo la primera liquidacion o una semana que no cuadra:
 *      java -jar ReporteConsignacionRapydReproceso.jar 2026-09-28 2026-10-04
 *
 * 2. Sin argumentos: corre como si hoy fuera general.parametros.FECHAREPROCESO (yyyy-MM-dd); el periodo se
 *    calcula igual que en el proceso normal. Para un martes:
 *      UPDATE general.parametros SET valortexto = '2026-10-06' WHERE valorparametro = 'FECHAREPROCESO';
 *    reporta del lunes 2026-09-28 al domingo 2026-10-04.
 */
public class ReporteConsignacionRapydReproceso {

	public static void main(final String[] args) {
		if (args != null && args.length >= 2) {
			ReporteConsignacionRapyd.generarRango(args[0].trim(), args[1].trim());
			return;
		}
		ReporteConsignacionRapyd.generar(ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO"));
	}

}
