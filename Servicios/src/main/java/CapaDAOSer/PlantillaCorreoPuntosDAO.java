package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;

/**
 * Que plantilla de Brevo le toca al correo nocturno de puntos, segun cuantos
 * puntos tenga acumulados el cliente.
 *
 * POR QUE EXISTE
 *
 * Los templateId estaban QUEMADOS y ademas repetidos: siete bloques iguales en
 * ServicioReplicaProgramaFidelizacion y otros siete en el gemelo de reproceso.
 * Cambiar una plantilla obligaba a tocar catorce sitios, compilar y desplegar,
 * y bastaba olvidar uno para que reprocesar una noche mandara la plantilla
 * vieja justo cuando alguien estaba mirando porque algo salio mal.
 *
 * Ahora los rangos viven en general.plantilla_correo_puntos y se cambian con un
 * UPDATE.
 *
 * LOS RANGOS SON INCLUSIVOS EN LOS DOS EXTREMOS
 *
 * Los cortes viejos estaban escritos como ">= 50 y < 99", ">= 100 y < 149" y
 * asi, de modo que 99, 149, 199, 249 y 299 no caian en ningun rango y esos
 * clientes NO recibian correo. Medido el 2026-11-04: 106 clientes estaban
 * parados justo en esos valores.
 *
 * Aqui puntos_hasta es inclusivo. Si aun asi la configuracion quedara con un
 * hueco, laQuePlantillaLeToca() devuelve null y quien llama debe DECIRLO en el
 * log en vez de dejar al cliente sin correo en silencio, que es exactamente lo
 * que pasaba antes.
 */
public class PlantillaCorreoPuntosDAO {

	/** Un rango de puntos con su plantilla. */
	public static class Rango {
		public int idPlantillaBrevo;
		public int puntosDesde;
		/** -1 cuando no tiene tope (el ultimo rango). */
		public int puntosHasta = -1;
		public String asunto = "";
		public String descripcion = "";

		public boolean sinTope() {
			return (puntosHasta < 0);
		}

		public boolean cubre(final int puntos) {
			return (puntos >= puntosDesde && (sinTope() || puntos <= puntosHasta));
		}

		@Override
		public String toString() {
			return ("[" + puntosDesde + " a " + (sinTope() ? "sin tope" : String.valueOf(puntosHasta))
					+ "] plantilla " + idPlantillaBrevo + " " + descripcion);
		}
	}

	/**
	 * Los rangos activos, del mas bajo al mas alto.
	 *
	 * Se lee UNA vez por corrida y se recorre en memoria: son seis filas y el
	 * proceso las consulta por cada destinatario.
	 */
	public static ArrayList<Rango> obtenerRangos() {
		final ArrayList<Rango> rangos = new ArrayList<Rango>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try {
			cn = con.obtenerConexionBDGeneral();
			final Statement stm = cn.createStatement();
			final String consulta =
					"select idplantilla_brevo, puntos_desde, puntos_hasta, asunto, descripcion"
					+ "  from plantilla_correo_puntos"
					+ " where activo = 'S'"
					+ " order by puntos_desde";
			final ResultSet rs = stm.executeQuery(consulta);
			while (rs.next()) {
				final Rango r = new Rango();
				r.idPlantillaBrevo = rs.getInt("idplantilla_brevo");
				r.puntosDesde = rs.getInt("puntos_desde");
				final int hasta = rs.getInt("puntos_hasta");
				r.puntosHasta = rs.wasNull() ? -1 : hasta;
				r.asunto = texto(rs.getString("asunto"));
				r.descripcion = texto(rs.getString("descripcion"));
				rangos.add(r);
			}
			rs.close();
			stm.close();
		} catch (final Exception e) {
			System.out.println("PlantillaCorreoPuntosDAO.obtenerRangos: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (rangos);
	}

	/**
	 * El rango que cubre esos puntos, o null si ninguno lo cubre.
	 *
	 * Devolver null a proposito y no "el mas parecido": si la configuracion
	 * tiene un hueco hay que enterarse, no taparlo.
	 */
	public static Rango laQuePlantillaLeToca(final ArrayList<Rango> rangos, final int puntos) {
		if (rangos == null) {
			return (null);
		}
		for (int i = 0; i < rangos.size(); i++) {
			if (rangos.get(i).cubre(puntos)) {
				return (rangos.get(i));
			}
		}
		return (null);
	}

	/**
	 * Revisa que los rangos no tengan huecos ni solapes y devuelve los
	 * problemas en texto. Vacio = esta bien.
	 *
	 * Se llama al arrancar el proceso y lo que salga se imprime en el log: una
	 * configuracion mala se nota como clientes que no reciben correo, que es
	 * justo lo que nadie ve.
	 */
	public static ArrayList<String> revisarCobertura(final ArrayList<Rango> rangos) {
		final ArrayList<String> problemas = new ArrayList<String>();
		if (rangos == null || rangos.isEmpty()) {
			problemas.add("No hay ningun rango activo en general.plantilla_correo_puntos:"
					+ " NO se enviara ningun correo de puntos.");
			return (problemas);
		}
		if (rangos.get(0).puntosDesde > 0) {
			problemas.add("El primer rango empieza en " + rangos.get(0).puntosDesde
					+ " y no en 0: los clientes con menos puntos se quedan sin correo.");
		}
		for (int i = 0; i < rangos.size() - 1; i++) {
			final Rango actual = rangos.get(i);
			final Rango siguiente = rangos.get(i + 1);
			if (actual.sinTope()) {
				problemas.add("El rango " + actual + " no tiene tope pero no es el ultimo:"
						+ " se come a los que vienen despues.");
				continue;
			}
			if (siguiente.puntosDesde > actual.puntosHasta + 1) {
				problemas.add("HUECO entre " + actual.puntosHasta + " y " + siguiente.puntosDesde
						+ ": los clientes con " + (actual.puntosHasta + 1)
						+ " a " + (siguiente.puntosDesde - 1) + " puntos no reciben correo.");
			} else if (siguiente.puntosDesde <= actual.puntosHasta) {
				problemas.add("SOLAPE entre " + actual + " y " + siguiente
						+ ": gana el primero y el segundo nunca se usa para esos puntos.");
			}
		}
		if (!rangos.get(rangos.size() - 1).sinTope()) {
			problemas.add("El ultimo rango termina en " + rangos.get(rangos.size() - 1).puntosHasta
					+ ": quien pase de ahi se queda sin correo. Dejelo en NULL.");
		}
		return (problemas);
	}

	private static String texto(final String valor) {
		return (valor == null ? "" : valor);
	}

	private static void cerrar(final Connection cn) {
		try {
			if (cn != null) {
				cn.close();
			}
		} catch (final Exception e) {
			System.out.println("PlantillaCorreoPuntosDAO: no cerro la conexion, " + e.toString());
		}
	}
}
