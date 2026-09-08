package ServiciosSer;

/**
 * Reprocesa el reporte semanal de RAPPI para la fecha de corte guardada en el
 * parámetro FECHAREPROCESO.
 *
 * Antes esto era una COPIA de las 277 líneas de ReporteSemanalRappi: las dos
 * versiones se diferenciaban en cuatro líneas -el nombre de la clase, de dónde
 * salía la fecha, el main y un espacio en blanco-. Cualquier arreglo en una
 * había que acordarse de copiarlo en la otra, y eso no pasa; peor aún aquí,
 * porque un reproceso solo se ejecuta cuando algo ya salió mal y en ese momento
 * nadie está comparando los dos archivos.
 *
 * Toda la lógica, incluida la regla del corte, vive en ReporteSemanalRappi.
 *
 * OJO con la fecha: este reporte corta DOMINGO o lunes. Si pone un miércoles en
 * FECHAREPROCESO no va a correr, y eso es correcto: el miércoles es el corte
 * del otro reporte, ReporteSemanalPagoRappiReproceso.
 */
public class ReporteSemanalRappiReproceso {

	public static void main(String[] args)
	{
		new ReporteSemanalRappi().reprocesar();
	}
}
