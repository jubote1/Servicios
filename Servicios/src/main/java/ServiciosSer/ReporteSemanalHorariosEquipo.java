package ServiciosSer;

/**
 * Cumplimiento de horarios del equipo de tecnologia y mercadeo
 *
 * Todo el trabajo lo hace ReporteSemanalHorariosBase: aqui solo vive lo que
 * distingue este reporte de sus hermanos. Hasta el 2026-09-22 esto eran 886
 * lineas copiadas.
 *
 * EL NOMBRE DE LA CLASE NO SE PUEDE CAMBIAR: el crontab del servidor la llama
 * asi.
 */
public class ReporteSemanalHorariosEquipo {

	public static void main(final String[] args) {
		final ReporteSemanalHorariosBase.Config cfg = new ReporteSemanalHorariosBase.Config();
		cfg.nombre = "Cumplimiento de horarios del equipo de tecnologia y mercadeo";
		cfg.parametroCorreo = "REPORTEHORASEQUIPO";
		cfg.asuntoPrincipal = "GENERAL CUMPLIMIENTO DE HORARIOS EQUIPO TECNOLOGIA Y MERCADEO SEMANAL";
		cfg.asuntoNoUso = "GENERAL PERSONAS TECNOLOGIA Y MERCADEO Y MOMENTOS DE NO USO DEL HUELLERO DACTILAR";
		cfg.fechaDesdeReproceso = false;
		cfg.poblacion = ReporteSemanalHorariosBase.INTERNOS_MIOS;
		
		cfg.topeFestivoAlto = 37;
		cfg.topeFestivoBajo = 18.5;
		ReporteSemanalHorariosBase.generar(cfg);
	}
}
