package ServiciosSer;

/**
 * PAYU ahora es RAPYD y ya no liquida por quincena sino por semana (los martes). Se deja la clase para no romper
 * la tarea programada o el jar que la nombre: ahora simplemente corre el proceso nuevo.
 *
 * Ver ReporteConsignacionRapyd.
 */
public class ReporteConsignacionPAYU {

	public static void main(final String[] args) {
		ReporteConsignacionRapyd.main(args);
	}

}
