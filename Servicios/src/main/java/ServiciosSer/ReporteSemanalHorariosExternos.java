package ServiciosSer;

/**
 * Cumplimiento de horarios del personal externo
 *
 * Todo el trabajo lo hace ReporteSemanalHorariosBase: aqui solo vive lo que
 * distingue este reporte de sus hermanos. Hasta el 2026-09-22 esto eran 886
 * lineas copiadas.
 *
 * EL NOMBRE DE LA CLASE NO SE PUEDE CAMBIAR: el crontab del servidor la llama
 * asi.
 */
public class ReporteSemanalHorariosExternos {

	public static void main(final String[] args) {
		final ReporteSemanalHorariosBase.Config cfg = new ReporteSemanalHorariosBase.Config();
		cfg.nombre = "Cumplimiento de horarios del personal externo";
		cfg.parametroCorreo = "REPORTEHORAS";
		cfg.asuntoPrincipal = "GENERAL CUMPLIMIENTO DE HORARIOS EMPLEADOS-EXTERNOS SEMANAL";
		cfg.asuntoNoUso = null;
		cfg.fechaDesdeReproceso = false;
		cfg.poblacion = ReporteSemanalHorariosBase.EXTERNOS;
		//Sin segundo correo: un domiciliario externo no marca huellero.
		cfg.topeFestivoAlto = 35;
		cfg.topeFestivoBajo = 17.5;
		ReporteSemanalHorariosBase.generar(cfg);
	}
}
