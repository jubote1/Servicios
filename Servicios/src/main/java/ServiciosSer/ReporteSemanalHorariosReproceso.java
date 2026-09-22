package ServiciosSer;

/**
 * Reproceso del cumplimiento de horarios del personal interno
 *
 * Todo el trabajo lo hace ReporteSemanalHorariosBase: aqui solo vive lo que
 * distingue este reporte de sus hermanos. Hasta el 2026-09-22 esto eran 886
 * lineas copiadas.
 *
 * EL NOMBRE DE LA CLASE NO SE PUEDE CAMBIAR: el crontab del servidor la llama
 * asi.
 */
public class ReporteSemanalHorariosReproceso {

	public static void main(final String[] args) {
		final ReporteSemanalHorariosBase.Config cfg = new ReporteSemanalHorariosBase.Config();
		cfg.nombre = "Reproceso del cumplimiento de horarios del personal interno";
		cfg.parametroCorreo = "REPORTEHORAS";
		cfg.asuntoPrincipal = "GENERAL CUMPLIMIENTO DE HORARIOS EMPLEADOS-INTERNOS SEMANAL";
		cfg.asuntoNoUso = "GENERAL PERSONAS EMPLEADOS Y MOMENTOS DE NO USO DEL HUELLERO DACTILAR";
		cfg.fechaDesdeReproceso = true;
		cfg.poblacion = ReporteSemanalHorariosBase.INTERNOS;
		
		cfg.topeFestivoAlto = 37;
		cfg.topeFestivoBajo = 18.5;
		ReporteSemanalHorariosBase.generar(cfg);
	}
}
