package ServiciosSer;

import CapaDAOSer.ParametrosDAO;

/**
 * Reprocesa el reporte semanal de RAPPI para la fecha de corte guardada en el
 * parámetro FECHAREPROCESO.
 *
 * Antes esto era una COPIA de las 277 líneas de ReporteSemanalRappi. Las dos
 * versiones se diferenciaban en cuatro líneas: el nombre de la clase, de dónde
 * salía la fecha, el main y un espacio en blanco. Cualquier arreglo en una
 * había que acordarse de copiarlo en la otra, y eso no pasa: se arregla una y
 * la otra queda vieja sin que nadie se dé cuenta, justamente porque el
 * reproceso solo se corre cuando algo ya salió mal y ahí nadie está para
 * comparar.
 *
 * Ahora toda la lógica vive en ReporteSemanalRappi y aquí solo se resuelve de
 * dónde sale la fecha. La regla de cuántos días se retroceden tambien es
 * compartida, en fechaInicioDelPeriodo.
 */
public class ReporteSemanalRappiReproceso {

	/** Parámetro con la fecha de corte a reprocesar, en aaaa-mm-dd. */
	private static final String PARAM_FECHA_REPROCESO = "FECHAREPROCESO";

	public void generarReporteRappi()
	{
		final String fechaActual = ParametrosDAO.retornarValorAlfanumerico(PARAM_FECHA_REPROCESO);
		if(fechaActual == null || fechaActual.trim().length() < 10)
		{
			System.out.println("ReporteSemanalRappiReproceso: el parámetro " + PARAM_FECHA_REPROCESO
					+ " no tiene una fecha utilizable: '" + fechaActual + "'");
			return;
		}
		final String corte = fechaActual.trim();
		final String fechaAnterior = ReporteSemanalRappi.fechaInicioDelPeriodo(corte);
		if(fechaAnterior == null)
		{
			return;
		}
		System.out.println("ReporteSemanalRappiReproceso: reprocesando de " + fechaAnterior + " a " + corte);
		new ReporteSemanalRappi().generar(fechaAnterior, corte);
	}

	public static void main(String[] args)
	{
		new ReporteSemanalRappiReproceso().generarReporteRappi();
	}
}
