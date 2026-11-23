package ServiciosSer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.ParametrosDAO;

/**
 * Servicio de reproceso del Reporte Semanal de Bloqueos de Tienda.
 * Toma como fecha de corte el parametro general 'FECHAREPROCESO' y delega
 * toda la logica analitica, persistencia de conocimiento y envio de correo
 * a ReporteBloqueosTienda.procesarReporte.
 */
public class ReporteBloqueosTiendaReproceso {

	public static void main(String[] args) {
		String fechaActual = "";
		Date datFechaAnterior;
		String fechaAnterior = "";
		Calendar calendarioActual = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		// Si se pasan parametros por linea de comandos, se priorizan
		if (args != null && args.length >= 2 && args[0] != null && args[1] != null) {
			fechaAnterior = args[0].trim();
			fechaActual = args[1].trim();
		} else {
			try {
				fechaActual = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
			} catch (Exception exc) {
				System.err.println("Error obteniendo FECHAREPROCESO: " + exc.getMessage());
			}

			if (fechaActual == null || fechaActual.trim().isEmpty()) {
				fechaActual = dateFormat.format(new Date());
			}

			try {
				calendarioActual.setTime(dateFormat.parse(fechaActual));
			} catch (Exception e) {
				System.err.println("Error parseando fechaActual: " + e.getMessage());
			}

			int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);

			// Calcular lunes anterior
			if (diaActual == Calendar.SUNDAY) {
				calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
			} else if (diaActual == Calendar.MONDAY) {
				calendarioActual.add(Calendar.DAY_OF_YEAR, -7);
			} else if (diaActual == Calendar.TUESDAY) {
				calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
			} else if (diaActual == Calendar.WEDNESDAY) {
				calendarioActual.add(Calendar.DAY_OF_YEAR, -2);
			} else if (diaActual == Calendar.THURSDAY) {
				calendarioActual.add(Calendar.DAY_OF_YEAR, -3);
			} else if (diaActual == Calendar.FRIDAY) {
				calendarioActual.add(Calendar.DAY_OF_YEAR, -4);
			} else if (diaActual == Calendar.SATURDAY) {
				calendarioActual.add(Calendar.DAY_OF_YEAR, -5);
			}

			datFechaAnterior = calendarioActual.getTime();
			fechaAnterior = dateFormat.format(datFechaAnterior);
		}

		System.out.println("Iniciando ReporteBloqueosTiendaReproceso para el periodo: " + fechaAnterior + " al " + fechaActual);
		ReporteBloqueosTienda.procesarReporte(fechaAnterior, fechaActual);
	}
}
