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
		
		cfg.topeFestivoAlto = 35;	//jornada de 42 h: semana con festivo = 42 - 7 (antes 37, de la jornada de 44 h)
		cfg.topeFestivoBajo = 17.5;	//la mitad de 35
		ReporteSemanalHorariosBase.generar(cfg);
	}
}
