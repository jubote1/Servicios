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
 * Permite regenerar el informe semanal y la auditoría antifraude para una fecha histórica
 * tomando el rango a partir de los parámetros:
 * - FECHAREPROCESO: Fecha final del corte a evaluar (o parámetro único para calcular la semana).
 * - FECHAREPROCESO2 (opcional): Fecha inicial explícita del corte. Si no está definido,
 *   calcula automáticamente el lunes anterior según el día de la semana de FECHAREPROCESO.
 * 
 * También admite pasar las fechas por argumentos de línea de comandos:
 * args[0] = fechaAnterior (YYYY-MM-DD), args[1] = fechaActual (YYYY-MM-DD).
 */
public class ReporteSemanalFidelizacionReproceso {

	public static void main(final String[] args) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String fechaActual = "";
		String fechaAnterior = "";
		final Calendar calendarioActual = Calendar.getInstance();

		// 1. Prioridad: argumentos de línea de comandos si se suministran
		if (args != null && args.length >= 2 && args[0] != null && args[1] != null && !args[0].trim().isEmpty() && !args[1].trim().isEmpty()) {
			fechaAnterior = args[0].trim();
			fechaActual = args[1].trim();
		} else {
			// 2. Consulta de parámetros en base de datos
			try {
				fechaActual = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
			} catch (final Exception exc) {
				System.out.println("Error obteniendo FECHAREPROCESO: " + exc.toString());
			}

			try {
				fechaAnterior = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO2");
			} catch (final Exception exc) {
				System.out.println("Error obteniendo FECHAREPROCESO2: " + exc.toString());
			}

			// Si FECHAREPROCESO2 no está configurado, calcular la semana hacia atrás desde FECHAREPROCESO
			if (fechaAnterior == null || fechaAnterior.trim().isEmpty() || "null".equalsIgnoreCase(fechaAnterior.trim())) {
				try {
					if (fechaActual != null && !fechaActual.trim().isEmpty()) {
						calendarioActual.setTime(dateFormat.parse(fechaActual.trim()));
						final int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);

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
				} catch (final Exception e) {
					System.out.println("Error calculando fecha inicial en reproceso: " + e.toString());
				}
			}
		}

		if (fechaActual == null || fechaActual.trim().isEmpty() || fechaAnterior == null || fechaAnterior.trim().isEmpty()) {
			System.out.println("Error: No se pudo determinar el rango de fechas para el reproceso (FECHAREPROCESO / FECHAREPROCESO2).");
			return;
		}

		System.out.println("Ejecutando Reproceso Reporte Semanal Plan Fidelización entre: " + fechaAnterior + " y " + fechaActual);

		// 1. Total histórico de personas en el plan de fidelización
		final int totalPersonasPlan = ClienteFidelizacionDAO.obtenerTotalClienteFidelizacion();

		// 2. Total de personas afiliadas en la semana reprocesada
		final int totalPersonaSemana = ClienteFidelizacionDAO.obtenerTotalClienteAfiliadosTiempo(fechaAnterior, fechaActual);

		// 3. Total de puntos acumulados en la semana reprocesada
		final double totalPuntosAcumulados = ClienteFidelizacionDAO.obtenerTotalPuntosAcumuladosTiempo(fechaAnterior, fechaActual);

		// 4. Total de puntos redimidos en la semana reprocesada
		final double totalPuntosRedimidos = ClienteFidelizacionDAO.obtenerTotalPuntosRedimidosTiempo(fechaAnterior, fechaActual);

		// 5. Análisis y detección de los 5 casos más sospechosos en el período evaluado
		final ArrayList<CasoSospechosoFidelizacion> casosSospechosos = ClienteFidelizacionDAO.obtenerCasosSospechosos(fechaAnterior, fechaActual, 5);

		// 6. Detalle de redenciones efectuadas en la semana reprocesada
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
			System.out.println("Reproceso Reporte Semanal Plan Fidelización enviado con éxito a lista FIDELIZACION.");
		} catch (final Exception eEnvio) {
			System.out.println("Error enviando correo de reproceso fidelización: " + eEnvio.toString());
		}
	}
}
