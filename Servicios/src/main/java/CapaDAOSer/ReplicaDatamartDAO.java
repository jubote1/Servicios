package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ConexionSer.ConexionBaseDatos;

/**
 * La replica de las tablas de cada tienda hacia el datamart (en el servidor central).
 *
 * POR QUE EXISTE, SI YA HABIA PedidoDAO.insertarLotePedidos Y COMPANIA
 *
 * Esos metodos se tragan el error: atrapan la SQLException, la imprimen en la consola del
 * servidor y devuelven normal. El proceso entonces escribia "EXITOSO" en el correo solo porque
 * HABIA LEIDO filas de la tienda, aunque no hubiera podido escribir ni una en el datamart (una
 * llave duplicada, una columna que falta). Aqui todo lo que falla LANZA, y quien llama sabe
 * exactamente que paso en cada tabla de cada tienda.
 *
 * COMO COPIA
 *
 * Por NOMBRE de columna, no con una lista escrita a mano: lee todas las columnas de la tabla de
 * la tienda y escribe las que tambien existen en la tabla del datamart. Asi una columna nueva
 * (por ejemplo despacho_real.origen) llega sola cuando se agrega en el datamart, y una columna que
 * el datamart no tiene no tumba la replica: se avisa.
 *
 * LA LLAVE DE CADA TIENDA
 *
 * Los ids (idpedidotienda, despacho_real.id, pedido_sugerencia.id...) se repiten de una tienda a
 * otra. En el datamart todo va acompanado de idtienda, y toda consulta de "ya esta replicado"
 * filtra por ella. Las tablas que en la tienda no traen idtienda (el detalle de los despachos y
 * de las sugerencias) lo reciben aqui.
 *
 * IDEMPOTENTE
 *
 * Antes de copiar se pregunta si el datamart ya tiene ese dia de esa tienda; si lo tiene no se
 * toca. Por eso se puede correr dos veces el mismo dia sin duplicar nada, y por eso el proceso
 * puede recuperar solo los dias en que una tienda estuvo apagada.
 */
public class ReplicaDatamartDAO {

	/** Cuantas filas se mandan juntas en cada lote. */
	private static final int TAMANO_LOTE = 500;

	/** Por que no se pudo replicar una tabla, y si es una falla o solo "no aplica todavia". */
	public static class ReplicaException extends Exception {
		private static final long serialVersionUID = 1L;
		private final boolean noAplica;

		public ReplicaException(String mensaje, boolean noAplica) {
			super(mensaje);
			this.noAplica = noAplica;
		}

		public ReplicaException(String mensaje, boolean noAplica, Throwable causa) {
			super(mensaje, causa);
			this.noAplica = noAplica;
		}

		/** true cuando falta una migracion y no es culpa de la tienda: se avisa en gris, no en rojo. */
		public boolean esNoAplica() {
			return noAplica;
		}
	}

	/** Lo que se hizo con una tabla en un dia. */
	public static class Resultado {
		public int filasLeidas = 0;
		public int filasEscritas = 0;
		/** Columnas de la tienda que el datamart no tiene (se copiaron sin ellas). */
		public List<String> columnasSinDestino = new ArrayList<String>();
	}

	/** Como se replica una tabla. */
	public static class Definicion {
		public final String tabla;
		/** SELECT en la tienda; el unico parametro es la fecha. */
		public final String sqlTienda;
		/** SELECT 1 ... LIMIT 1 en el datamart; los parametros son idtienda y fecha. */
		public final String sqlExiste;
		/** Si en la tienda la tabla no trae idtienda y hay que ponerselo. */
		public final boolean poneIdTienda;
		/** Si cero filas es normal (no se pinta de amarillo). */
		public final boolean ceroEsNormal;

		public Definicion(String tabla, String sqlTienda, String sqlExiste, boolean poneIdTienda,
				boolean ceroEsNormal) {
			this.tabla = tabla;
			this.sqlTienda = sqlTienda;
			this.sqlExiste = sqlExiste;
			this.poneIdTienda = poneIdTienda;
			this.ceroEsNormal = ceroEsNormal;
		}
	}

	/**
	 * Las tablas que se replican, en el orden en que se hacen. El detalle va despues de su
	 * encabezado: la consulta de "ya esta" del detalle se apoya en el encabezado.
	 */
	public static List<Definicion> definiciones() {
		List<Definicion> d = new ArrayList<Definicion>();
		d.add(new Definicion("pedido",
				"SELECT * FROM pedido WHERE fechapedido = ?",
				"SELECT 1 FROM pedido WHERE idtienda = ? AND fechapedido = ? LIMIT 1", false, false));
		d.add(new Definicion("detalle_pedido",
				"SELECT d.* FROM detalle_pedido d INNER JOIN pedido p ON d.idpedidotienda = p.idpedidotienda "
						+ "AND d.idtienda = p.idtienda WHERE p.fechapedido = ?",
				"SELECT 1 FROM detalle_pedido d INNER JOIN pedido p ON d.idpedidotienda = p.idpedidotienda "
						+ "AND d.idtienda = p.idtienda WHERE p.idtienda = ? AND p.fechapedido = ? LIMIT 1",
				false, false));
		d.add(new Definicion("despacho_real",
				"SELECT * FROM despacho_real WHERE fecha = ?",
				"SELECT 1 FROM despacho_real WHERE idtienda = ? AND fecha = ? LIMIT 1", false, true));
		d.add(new Definicion("despacho_real_det",
				"SELECT d.* FROM despacho_real_det d INNER JOIN despacho_real r ON d.despacho_real_id = r.id "
						+ "WHERE r.fecha = ?",
				"SELECT 1 FROM despacho_real_det d INNER JOIN despacho_real r ON d.despacho_real_id = r.id "
						+ "AND d.idtienda = r.idtienda WHERE r.idtienda = ? AND r.fecha = ? LIMIT 1",
				true, true));
		//El enrutamiento: las tablas las crea la migracion 2026_09_10_03 en cada tienda. Una tienda que no
		//la haya corrido no tiene la tabla, y eso no es una falla de la replica.
		d.add(new Definicion("pedido_sugerencia",
				"SELECT * FROM pedido_sugerencia WHERE fecha_jornada = ?",
				"SELECT 1 FROM pedido_sugerencia WHERE idtienda = ? AND fecha_jornada = ? LIMIT 1", false, true));
		d.add(new Definicion("pedido_sugerencia_det",
				"SELECT t.* FROM pedido_sugerencia_det t INNER JOIN pedido_sugerencia s "
						+ "ON s.id = t.pedido_sugerencia_id WHERE s.fecha_jornada = ?",
				"SELECT 1 FROM pedido_sugerencia_det t INNER JOIN pedido_sugerencia s "
						+ "ON s.id = t.pedido_sugerencia_id AND s.idtienda = t.idtienda "
						+ "WHERE s.idtienda = ? AND s.fecha_jornada = ? LIMIT 1",
				true, true));
		d.add(new Definicion("pedido_sugerencia_log",
				"SELECT l.* FROM pedido_sugerencia_log l INNER JOIN pedido_sugerencia s "
						+ "ON s.id = l.pedido_sugerencia_id WHERE s.fecha_jornada = ?",
				"SELECT 1 FROM pedido_sugerencia_log l INNER JOIN pedido_sugerencia s "
						+ "ON s.id = l.pedido_sugerencia_id AND s.idtienda = l.idtienda "
						+ "WHERE s.idtienda = ? AND s.fecha_jornada = ? LIMIT 1",
				true, true));
		return d;
	}

	// ------------------------------------------------------------------
	// Preguntar y copiar
	// ------------------------------------------------------------------

	/** Si el datamart ya tiene ese dia de esa tienda para esa tabla. */
	public static boolean yaEstaEnDatamart(Connection datamart, Definicion def, int idTienda, String fecha)
			throws ReplicaException {
		try (PreparedStatement ps = datamart.prepareStatement(def.sqlExiste)) {
			ps.setInt(1, idTienda);
			ps.setString(2, fecha);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			if (esTablaInexistente(e)) {
				throw new ReplicaException("Falta correr la migracion del datamart: no existe " + def.tabla
						+ " (o le falta la columna idtienda).", true, e);
			}
			throw new ReplicaException("No se pudo consultar el datamart: " + e.getMessage(), false, e);
		}
	}

	/**
	 * Lee de la tienda el dia y lo escribe en el datamart, todo o nada. Si algo falla lanza y el
	 * datamart queda como estaba (la escritura es una sola transaccion).
	 */
	public static Resultado replicar(Connection tienda, Definicion def, int idTienda, String fecha)
			throws ReplicaException {
		Resultado res = new Resultado();

		//1. Leer de la tienda.
		List<Object[]> filas = new ArrayList<Object[]>();
		List<String> columnasTienda = new ArrayList<String>();
		try (PreparedStatement ps = tienda.prepareStatement(def.sqlTienda)) {
			ps.setString(1, fecha);
			try (ResultSet rs = ps.executeQuery()) {
				ResultSetMetaData md = rs.getMetaData();
				int n = md.getColumnCount();
				for (int i = 1; i <= n; i++) {
					columnasTienda.add(md.getColumnLabel(i));
				}
				while (rs.next()) {
					Object[] fila = new Object[n];
					for (int i = 1; i <= n; i++) {
						fila[i - 1] = rs.getObject(i);
					}
					filas.add(fila);
				}
			}
		} catch (SQLException e) {
			if (esTablaInexistente(e)) {
				throw new ReplicaException("La tabla " + def.tabla + " no existe en la tienda (falta su migracion).",
						true, e);
			}
			throw new ReplicaException("No se pudo leer de la tienda: " + e.getMessage(), false, e);
		}
		res.filasLeidas = filas.size();
		if (filas.isEmpty()) {
			return res;
		}

		//2. Escribir en el datamart.
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection dm = con.obtenerConexionBDDatamartLocal();
		if (dm == null) {
			throw new ReplicaException("No se pudo conectar con el datamart.", false);
		}
		try {
			//Las columnas del destino, para copiar solo las que existen alla.
			Map<String, String> destino = columnasDe(dm, def.tabla);
			List<Integer> posiciones = new ArrayList<Integer>();
			List<String> nombresDestino = new ArrayList<String>();
			boolean tieneIdTiendaOrigen = false;
			for (int i = 0; i < columnasTienda.size(); i++) {
				String col = columnasTienda.get(i);
				String real = destino.get(col.toLowerCase());
				if (real == null) {
					res.columnasSinDestino.add(col);
					continue;
				}
				if (col.equalsIgnoreCase("idtienda")) {
					tieneIdTiendaOrigen = true;
				}
				posiciones.add(Integer.valueOf(i));
				nombresDestino.add(real);
			}
			//Si el destino lleva idtienda y la tienda no lo trae, se pone aqui.
			String idTiendaDestino = destino.get("idtienda");
			boolean agregarIdTienda = !tieneIdTiendaOrigen && idTiendaDestino != null;
			if (agregarIdTienda) {
				nombresDestino.add(idTiendaDestino);
			}
			if (def.poneIdTienda && idTiendaDestino == null) {
				throw new ReplicaException("El datamart no tiene idtienda en " + def.tabla
						+ ": sin esa columna los ids de las tiendas se mezclan. Falta la migracion.", true);
			}

			StringBuilder insert = new StringBuilder("INSERT INTO ").append(def.tabla).append(" (");
			StringBuilder marcas = new StringBuilder();
			for (int i = 0; i < nombresDestino.size(); i++) {
				insert.append(i > 0 ? ", " : "").append('`').append(nombresDestino.get(i)).append('`');
				marcas.append(i > 0 ? ", " : "").append('?');
			}
			insert.append(") VALUES (").append(marcas).append(")");

			dm.setAutoCommit(false);
			try (PreparedStatement ps = dm.prepareStatement(insert.toString())) {
				int enLote = 0;
				for (Object[] fila : filas) {
					int p = 1;
					for (Integer pos : posiciones) {
						ps.setObject(p++, fila[pos.intValue()]);
					}
					if (agregarIdTienda) {
						ps.setInt(p++, idTienda);
					}
					ps.addBatch();
					enLote++;
					if (enLote >= TAMANO_LOTE) {
						ps.executeBatch();
						enLote = 0;
					}
				}
				if (enLote > 0) {
					ps.executeBatch();
				}
			}
			dm.commit();
			res.filasEscritas = filas.size();
		} catch (ReplicaException e) {
			deshacer(dm);
			throw e;
		} catch (SQLException e) {
			deshacer(dm);
			if (esTablaInexistente(e)) {
				throw new ReplicaException("Falta correr la migracion del datamart: no existe " + def.tabla + ".",
						true, e);
			}
			throw new ReplicaException("No se pudo escribir en el datamart: " + e.getMessage(), false, e);
		} finally {
			try {
				dm.close();
			} catch (SQLException ignorada) {
			}
		}
		return res;
	}

	/** Las columnas de una tabla del datamart: nombre en minusculas -> nombre real. */
	private static Map<String, String> columnasDe(Connection dm, String tabla) throws SQLException {
		Map<String, String> columnas = new HashMap<String, String>();
		try (PreparedStatement ps = dm.prepareStatement("SELECT * FROM " + tabla + " WHERE 1 = 0");
				ResultSet rs = ps.executeQuery()) {
			ResultSetMetaData md = rs.getMetaData();
			for (int i = 1; i <= md.getColumnCount(); i++) {
				columnas.put(md.getColumnLabel(i).toLowerCase(), md.getColumnLabel(i));
			}
		}
		return columnas;
	}

	private static void deshacer(Connection dm) {
		try {
			dm.rollback();
		} catch (SQLException ignorada) {
		}
	}

	/** 42S02: la tabla no existe. 42S22: la columna no existe. */
	private static boolean esTablaInexistente(SQLException e) {
		String estado = e.getSQLState();
		return estado != null && (estado.equals("42S02") || estado.equals("42S22"));
	}

	// ------------------------------------------------------------------
	// El registro (datamart.replica_log)
	// ------------------------------------------------------------------

	/**
	 * Deja constancia de lo que paso con una tabla de una tienda. Es del que "tenga la migracion":
	 * si la tabla replica_log no existe, el proceso sigue sin ella (el correo ya lo dice todo).
	 */
	public static void registrar(int idTienda, String tabla, String fechaDatos, String estado, int filas,
			String detalle) {
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection dm = con.obtenerConexionBDDatamartLocal();
		if (dm == null) {
			return;
		}
		try (PreparedStatement ps = dm.prepareStatement("INSERT INTO replica_log (idtienda, tabla, fecha_datos, "
				+ "estado, filas, detalle) VALUES (?, ?, ?, ?, ?, ?)")) {
			ps.setInt(1, idTienda);
			ps.setString(2, tabla);
			ps.setString(3, fechaDatos);
			ps.setString(4, estado);
			ps.setInt(5, filas);
			ps.setString(6, detalle == null ? "" : (detalle.length() > 400 ? detalle.substring(0, 400) : detalle));
			ps.executeUpdate();
		} catch (SQLException e) {
			//Sin replica_log no pasa nada.
		} finally {
			try {
				dm.close();
			} catch (SQLException ignorada) {
			}
		}
	}

	/** La ultima fecha de datos con replica buena para una tienda y tabla, o "" si no se sabe. */
	public static String ultimaFechaBuena(int idTienda, String tabla) {
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection dm = con.obtenerConexionBDDatamartLocal();
		if (dm == null) {
			return "";
		}
		try (PreparedStatement ps = dm.prepareStatement("SELECT MAX(fecha_datos) FROM replica_log WHERE idtienda = ? "
				+ "AND tabla = ? AND estado IN ('OK', 'YA', 'CERO')")) {
			ps.setInt(1, idTienda);
			ps.setString(2, tabla);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next() && rs.getString(1) != null) {
					return rs.getString(1);
				}
			}
		} catch (SQLException e) {
			return "";
		} finally {
			try {
				dm.close();
			} catch (SQLException ignorada) {
			}
		}
		return "";
	}

	/** Util para armar un mapa ordenado tabla -> definicion. */
	public static Map<String, Definicion> porNombre() {
		Map<String, Definicion> m = new LinkedHashMap<String, Definicion>();
		for (Definicion d : definiciones()) {
			m.put(d.tabla, d);
		}
		return m;
	}
}
