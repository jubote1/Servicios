package ServiciosSer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import CapaDAOSer.ClienteFidelizacionDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import ModeloSer.CasoSospechosoFidelizacion;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.FidelizacionRedencion;
import utilidadesSer.ControladorEnvioCorreo;
import utilidadesSer.CorreoPlanFidelizacion;

/**
 * Reproceso del Reporte Semanal del Plan de Fidelización de Pizza Americana.
 * 
 * Regla de negocio estricta:
 * El usuario suministra un domingo de corte en el parámetro FECHAREPROCESO (ej. 2026-11-15).
 * El sistema toma ese domingo como fecha final (fechaActual) y retrocede exactamente 6 días
 * hasta el lunes anterior (fechaAnterior = 2026-11-09) para evaluar siempre una ventana semanal
 * estricta de 7 días (Lunes a Domingo).
 * 
 * NO se utiliza FECHAREPROCESO2 para evitar rangos distorsionados de meses o años anteriores.
 */
public class ReporteSemanalFidelizacionReproceso {

	public static void main(final String[] args) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String fechaActual = "";
		String fechaAnterior = "";
		final Calendar cal = Calendar.getInstance();

		// 1. Prioridad: argumentos de línea de comandos (opcionales)
		if (args != null && args.length >= 1 && args[0] != null && !args[0].trim().isEmpty()) {
			if (args.length >= 2 && args[1] != null && !args[1].trim().isEmpty()) {
				fechaAnterior = args[0].trim();
				fechaActual = args[1].trim();
			} else {
				fechaActual = args[0].trim();
			}
		} else {
			// 2. Tomar únicamente el domingo de corte desde el parámetro FECHAREPROCESO
			try {
				fechaActual = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
			} catch (final Exception exc) {
				System.out.println("Error obteniendo FECHAREPROCESO: " + exc.toString());
			}
		}

		if (fechaActual == null || fechaActual.trim().isEmpty()) {
			System.out.println("Error: El parámetro FECHAREPROCESO está vacío en la base de datos.");
			return;
		}

		fechaActual = fechaActual.trim();

		// 3. Si no se pasó fechaAnterior explícita, calcular estrictamente el lunes anterior (restando 6 días)
		if (fechaAnterior == null || fechaAnterior.trim().isEmpty()) {
			try {
				cal.setTime(dateFormat.parse(fechaActual));
				final int diaSemana = cal.get(Calendar.DAY_OF_WEEK);
				if (diaSemana == Calendar.SUNDAY) {
					// Si es Domingo, retrocede 6 días para llegar al Lunes de esa misma semana
					cal.add(Calendar.DAY_OF_YEAR, -6);
				} else if (diaSemana == Calendar.MONDAY) {
					// Si por alguna razón se pasa un Lunes, la semana previa es lunes a domingo anterior
					cal.add(Calendar.DAY_OF_YEAR, -7);
				} else {
					// Para cualquier otro día suministrado, retrocede 6 días para mantener corte de 7 días
					cal.add(Calendar.DAY_OF_YEAR, -6);
				}
				fechaAnterior = dateFormat.format(cal.getTime());
			} catch (final Exception e) {
				System.out.println("Error parseando fecha FECHAREPROCESO (" + fechaActual + "): " + e.toString());
				return;
			}
		}

		System.out.println("==========================================================================");
		System.out.println("EJECUTANDO REPROCESO SEMANAL PLAN FIDELIZACIÓN (VENTANA SEMANAL DE 7 DÍAS)");
		System.out.println("• Fecha Inicial (Lunes):   " + fechaAnterior);
		System.out.println("• Fecha Corte   (Domingo): " + fechaActual);
		System.out.println("==========================================================================");

		// 1. Total histórico de personas en el plan de fidelización
		final int totalPersonasPlan = ClienteFidelizacionDAO.obtenerTotalClienteFidelizacion();

		// 2. Total de personas afiliadas en la semana (Lunes a Domingo)
		final int totalPersonaSemana = ClienteFidelizacionDAO.obtenerTotalClienteAfiliadosTiempo(fechaAnterior, fechaActual);

		// 3. Total de puntos acumulados en la semana
		final double totalPuntosAcumulados = ClienteFidelizacionDAO.obtenerTotalPuntosAcumuladosTiempo(fechaAnterior, fechaActual);

		// 4. Total de puntos redimidos en la semana
		final double totalPuntosRedimidos = ClienteFidelizacionDAO.obtenerTotalPuntosRedimidosTiempo(fechaAnterior, fechaActual);

		// 5. Análisis y detección de los 5 casos más sospechosos en la semana evaluada
		final ArrayList<CasoSospechosoFidelizacion> casosSospechosos = ClienteFidelizacionDAO.obtenerCasosSospechosos(fechaAnterior, fechaActual, 5);

		// 6. Detalle de redenciones efectuadas en la semana evaluada
		final ArrayList<FidelizacionRedencion> redenciones = ClienteFidelizacionDAO.obtenerRedencionesFecha(fechaAnterior, fechaActual);

		// 7. Construcción del cuerpo de correo con formato institucional y auditoría
		final String htmlMensaje = CorreoPlanFidelizacion.armarReporte(
				fechaAnterior,
				fechaActual,
				totalPersonasPlan,
				totalPersonaSemana,
				totalPuntosAcumulados,
				totalPuntosRedimidos,
				casosSospechosos,
				redenciones);

		// 8. Envío del correo electrónico a la lista de distribución FIDELIZACION
		try {
			final ArrayList correos = GeneralDAO.obtenerCorreosParametro("FIDELIZACION");
			final Correo correo = new Correo();
			final CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setAsunto("REPORTE SEMANAL PLAN DE FIDELIZACION ENTRE " + fechaAnterior + " Y " + fechaActual + " (REPROCESO)");
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje(htmlMensaje);
			final ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
			System.out.println("Reproceso semanal enviado con éxito a la lista FIDELIZACION.");
		} catch (final Exception eEnvio) {
			System.out.println("Error enviando correo de reproceso fidelización: " + eEnvio.toString());
		}
	}
}
