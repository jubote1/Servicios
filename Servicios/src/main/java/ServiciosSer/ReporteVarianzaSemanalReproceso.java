package ServiciosSer;

/**
 * Vuelve a sacar el reporte semanal de varianza de una semana determinada.
 *
 * Es una clase de tres lineas a proposito. La costumbre vieja en este proyecto
 * era copiar el reporte entero y cambiarle cuatro lineas -el nombre de la
 * clase, de donde sale la fecha y el main-, y eso obliga a acordarse de repetir
 * cada arreglo en las dos copias. No pasa. Y duele mas justo aqui, porque un
 * reproceso solo se corre cuando algo ya salio mal, y en ese momento nadie esta
 * comparando dos archivos.
 *
 * Toda la logica vive en ReporteVarianzaSemanal.
 *
 * QUE SEMANA SACA
 *
 * La de FECHAREPROCESO, en general.parametros. Esa fecha es el dia en que el
 * proceso DEBIO correr, no el ultimo dia de la semana que se quiere: el rango
 * se arma igual que en la corrida normal, los siete dias completos anteriores.
 * Si se pone el domingo 2026-09-14, saca del 2026-09-07 al 2026-09-13.
 *
 * OJO: FECHAREPROCESO es uno solo y lo comparten todos los reprocesos del
 * servidor. Antes de correr esto hay que mirar que no quedo puesto para otra
 * cosa. El rango que se resolvio se imprime antes de mandar nada.
 *
 * Si se quiere una semana puntual sin tocar ese parametro, el proceso normal
 * recibe las dos fechas por argumento:
 *
 *   java -jar ReporteVarianzaSemanal.jar 2026-09-07 2026-09-13
 */
public class ReporteVarianzaSemanalReproceso {

	public static void main(final String[] args) {
		ReporteVarianzaSemanal.reprocesar();
	}
}
