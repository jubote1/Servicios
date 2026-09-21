package ServiciosSer;

import CapaDAOSer.UbicacionDomiciliarioDAO;

/**
 * Depura ubicacion_domiciliario, dejando solo los ultimos 90 dias (3 meses)
 * de historial de posiciones de domiciliarios. Antes esto corria empotrado
 * dentro del reporte semanal de estadisticas; ahora es su propio proceso
 * para poder correrlo a diario desde crontab, acorde al volumen que mueve
 * esta tabla.
 *
 * Empacar como jar aparte (Main-Class = ServiciosSer.ServicioDiarioDepuracionUbicacionDomiciliario)
 * y agregar una linea de crontab diaria, siguiendo el mismo patron de los
 * demas procesos ya desplegados (java -jar /media/raid/<carpeta>/<jar>).
 */
public class ServicioDiarioDepuracionUbicacionDomiciliario {

	public static void main(String[] args) {
		System.out.println("Depurando ubicacion_domiciliario (dejando solo los ultimos 90 dias)...");
		UbicacionDomiciliarioDAO.depurarUbicacionDomiciliario();
		System.out.println("Depuracion de ubicacion_domiciliario finalizada.");
	}

}
