package ServiciosSer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.ClienteFidelizacionDAO;
import CapaDAOSer.GeneralDAO;
import ModeloSer.CasoSospechosoFidelizacion;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.FidelizacionRedencion;
import utilidadesSer.ControladorEnvioCorreo;
import utilidadesSer.CorreoPlanFidelizacion;

/**
 * Reporte Semanal del Plan de Fidelización de Pizza Americana.
 * 
 * Genera el informe institucional de afiliaciones, acumulación y redención de puntos,
 * e incluye el módulo de auditoría de seguridad para detectar los 5 casos más sospechosos
 * de mal manejo de puntos (personal de nómina, reincidencia diaria, frecuencia excesiva semanal
 * y auto-acumulación en caja propia).
 */
public class ReporteSemanalPlanFidelizacion {

	public static void main(final String[] args) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String fechaActual = "";
		String fechaAnterior = "";

		// Soporte para fechas por parámetro (para pruebas o reprocesos manuales)
		if (args != null && args.length >= 2 && args[0] != null && args[1] != null && !args[0].isEmpty() && !args[1].isEmpty()) {
			fechaAnterior = args[0].trim();
			fechaActual = args[1].trim();
		} else {
			final Date datFechaActual = new Date();
			fechaActual = dateFormat.format(datFechaActual);
			final Calendar calendarioActual = Calendar.getInstance();
			try {
				calendarioActual.setTime(dateFormat.parse(fechaActual));
			} catch (final Exception e) {
				System.out.println("Error parseando fecha: " + e.toString());
			}

			final int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
			// Cálculo de semana (Lunes a Domingo inmediatamente anterior)
			if (diaActual == 1) { // Domingo
				calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
			} else if (diaActual == 2) { // Lunes
				calendarioActual.add(Calendar.DAY_OF_YEAR, -7);
			} else if (diaActual == 3) { // Martes
				calendarioActual.add(Calendar.DAY_OF_YEAR, -8);
			} else if (diaActual == 4) { // Miércoles
				calendarioActual.add(Calendar.DAY_OF_YEAR, -2);
			} else if (diaActual == 5) { // Jueves
				calendarioActual.add(Calendar.DAY_OF_YEAR, -3);
			} else if (diaActual == 6) { // Viernes
				calendarioActual.add(Calendar.DAY_OF_YEAR, -4);
			} else if (diaActual == 7) { // Sábado
				calendarioActual.add(Calendar.DAY_OF_YEAR, -5);
			}
			fechaAnterior = dateFormat.format(calendarioActual.getTime());
		}

		System.out.println("Generando Reporte Semanal Plan Fidelización entre: " + fechaAnterior + " y " + fechaActual);

		// 1. Total histórico de personas en el plan de fidelización
		final int totalPersonasPlan = ClienteFidelizacionDAO.obtenerTotalClienteFidelizacion();

		// 2. Total de personas afiliadas en la última semana
		final int totalPersonaSemana = ClienteFidelizacionDAO.obtenerTotalClienteAfiliadosTiempo(fechaAnterior, fechaActual);

		// 3. Total de puntos acumulados en la última semana
		final double totalPuntosAcumulados = ClienteFidelizacionDAO.obtenerTotalPuntosAcumuladosTiempo(fechaAnterior, fechaActual);

		// 4. Total de puntos redimidos en la última semana
		final double totalPuntosRedimidos = ClienteFidelizacionDAO.obtenerTotalPuntosRedimidosTiempo(fechaAnterior, fechaActual);

		// 5. Análisis y detección de los 5 casos más sospechosos de mal manejo de puntos
		final ArrayList<CasoSospechosoFidelizacion> casosSospechosos = ClienteFidelizacionDAO.obtenerCasosSospechosos(fechaAnterior, fechaActual, 5);

		// 6. Detalle de redenciones efectuadas en la semana
		final ArrayList<FidelizacionRedencion> redenciones = ClienteFidelizacionDAO.obtenerRedencionesFecha(fechaAnterior, fechaActual);

		// 7. Construcción del cuerpo de correo con formato institucional
		final String htmlMensaje = CorreoPlanFidelizacion.armarReporte(
				fechaAnterior,
				fechaActual,
				totalPersonasPlan,
				totalPersonaSemana,
				totalPuntosAcumulados,
				totalPuntosRedimidos,
				casosSospechosos,
				redenciones);

		// 8. Envío del correo electrónico a la lista de distribución
		try {
			final ArrayList correos = GeneralDAO.obtenerCorreosParametro("FIDELIZACION");
			final Correo correo = new Correo();
			final CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("REPORTE SEMANAL PLAN DE FIDELIZACION ENTRE " + fechaAnterior + " Y " + fechaActual);
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje(htmlMensaje);
			final ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
			System.out.println("Reporte Semanal Plan Fidelización enviado con éxito a lista FIDELIZACION.");
		} catch (final Exception eEnvio) {
			System.out.println("Error enviando reporte fidelización: " + eEnvio.toString());
		}
	}
}
