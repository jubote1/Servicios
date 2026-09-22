package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import ConexionSer.ConexionBaseDatos;

/**
 * Las horas PROGRAMADAS de cada persona, de general.horario_planificado.
 *
 * Es la otra mitad del reporte de horarios: horario_trabajado dice lo que la
 * persona efectivamente marco en el huellero, y esta tabla dice lo que se le
 * habia programado. Sin las dos, un total de horas no se puede juzgar: 40 horas
 * pueden ser mucho o poco segun cuantas se hayan programado.
 *
 * DOS TRAMPAS QUE HAY QUE RESPETAR AL SUMAR
 *
 * 1. LOS TURNOS PARTIDOS SON DOS FILAS Y VAN LOS DOS. El empleado 456 el
 *    2026-09-23 tiene "5-9/10:00-10:30": 17:00 a 21:00 y 22:00 a 22:30. Son
 *    siete horas y media en dos pedazos, y agrupar por (empleado, fecha) se
 *    comeria el segundo.
 *
 * 2. PERO HAY FILAS EXACTAMENTE REPETIDAS, y esas no. El empleado 799 el
 *    2026-09-23 aparece dos veces con el MISMO turno 6-10 y las mismas horas.
 *    Sumarlas dobla sus horas. Al 2026-09-22 hay 7 grupos asi.
 *
 * La regla que distingue las dos: contar DISTINTAS por hora de entrada y
 * salida. El turno partido tiene horas distintas y sobrevive; la fila repetida
 * es identica y se colapsa. Por eso el SELECT DISTINCT del subquery, que no
 * sobra.
 *
 * LOS DESCANSOS NO SUMAN, Y NO SON UN ERROR
 *
 * 371 de 1.820 filas no tienen horas: 276 son 'D' -descanso- y 73 son 'v'
 * -vacaciones-. Quedan por fuera del total solas, porque TIMEDIFF sobre NULL da
 * NULL, pero se cuentan aparte: un total de horas programadas sin decir cuanta
 * gente descanso esa semana se lee mal.
 */
public class HorarioPlanificadoDAO {

	/** Lo programado de una persona en el periodo. */
	public static class Programado {
		public double horas;
		/** Turnos con horario. No incluye descansos ni vacaciones. */
		public int turnos;
		/** Dias marcados como descanso o vacaciones. */
		public int diasSinTurno;
	}

	/**
	 * Lo programado de TODAS las personas del periodo, por idempleado.
	 *
	 * Se trae todo de una y no persona por persona a proposito: el reporte
	 * recorre entre 90 y 110 empleados, y una consulta por cada uno serian cien
	 * viajes a la base para algo que cabe en uno.
	 *
	 * @param desde yyyy-MM-dd
	 * @param hasta yyyy-MM-dd, incluido
	 */
	public static HashMap<Integer, Programado> obtenerProgramadoPorEmpleado(final String desde,
			final String hasta) {
		final HashMap<Integer, Programado> mapa = new HashMap<Integer, Programado>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = null;
		try {
			con1 = con.obtenerConexionBDGeneralLocal();
			final PreparedStatement ps = con1.prepareStatement(
					"SELECT idempleado,"
					+ " IFNULL(SUM(TIME_TO_SEC(TIMEDIFF(hora_salida_teorica, hora_ingreso_teorica))/3600),0),"
					+ " SUM(hora_ingreso_teorica IS NOT NULL),"
					+ " SUM(hora_ingreso_teorica IS NULL)"
					+ " FROM (SELECT DISTINCT idempleado, fecha, hora_ingreso_teorica, hora_salida_teorica"
					+ "         FROM horario_planificado WHERE fecha BETWEEN ? AND ?) x"
					+ " GROUP BY idempleado");
			ps.setString(1, desde);
			ps.setString(2, hasta);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Programado p = new Programado();
				p.horas = rs.getDouble(2);
				p.turnos = rs.getInt(3);
				p.diasSinTurno = rs.getInt(4);
				mapa.put(Integer.valueOf(rs.getInt(1)), p);
			}
			rs.close();
			ps.close();
			con1.close();
		} catch (final Exception e) {
			//Si esto falla el reporte sale igual, solo que sin la columna de
			//programado. Es informacion adicional: tumbar el reporte semanal
			//completo por ella seria peor que no tenerla.
			System.out.println("HorarioPlanificadoDAO.obtenerProgramadoPorEmpleado: " + e.toString());
			try {
				if (con1 != null) {
					con1.close();
				}
			} catch (final Exception e1) {
			}
		}
		return mapa;
	}

	/**
	 * Lo programado de una persona, o un Programado en ceros si no aparece.
	 *
	 * Devolver ceros y no null es deliberado: quien llama esta armando una fila
	 * de Excel y no puede quedarse a mitad de camino preguntando. Que una
	 * persona no este programada es un caso normal -entro esta semana, es
	 * externo, o sencillamente no le armaron turno-.
	 */
	public static Programado de(final HashMap<Integer, Programado> mapa, final int idEmpleado) {
		final Programado p = mapa.get(Integer.valueOf(idEmpleado));
		return (p == null ? new Programado() : p);
	}

	/** La suma de todo lo programado en el periodo, para el total del reporte. */
	public static Programado total(final HashMap<Integer, Programado> mapa) {
		final Programado t = new Programado();
		for (final Programado p : mapa.values()) {
			t.horas += p.horas;
			t.turnos += p.turnos;
			t.diasSinTurno += p.diasSinTurno;
		}
		return t;
	}
}
