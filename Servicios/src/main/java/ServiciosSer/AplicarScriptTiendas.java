package ServiciosSer;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import CapaDAOSer.TiendaDAO;
import ConexionSer.ConexionBaseDatos;
import ModeloSer.Tienda;

/**
 * Aplica un script SQL a la base local (tiendaamericana) de cada tienda con
 * hosbd. Pensado para migraciones idempotentes del POS.
 *
 * Uso: AplicarScriptTiendas <ruta.sql> <tabla-a-verificar> [--aplicar]
 *
 * Sin --aplicar solo mira: dice por tienda si responde y si la tabla ya
 * existe, sin escribir nada. Con --aplicar ejecuta el script en las tiendas
 * que responden y vuelve a verificar. Una tienda que no responde se salta y
 * queda en el resumen.
 */
public class AplicarScriptTiendas {

	public static void main(final String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Uso: AplicarScriptTiendas <ruta.sql> <tabla-a-verificar> [--aplicar]");
			return;
		}
		final boolean aplicar = args.length > 2 && args[2].equals("--aplicar");
		final String tabla = args[1];
		final ArrayList<String> sentencias = leerSentencias(new File(args[0]));
		System.out.println("Script: " + args[0] + " (" + sentencias.size() + " sentencia(s)). Modo: "
				+ (aplicar ? "APLICAR" : "SOLO LECTURA"));

		final ArrayList<String> yaTenian = new ArrayList<String>();
		final ArrayList<String> creadas = new ArrayList<String>();
		final ArrayList<String> sinRespuesta = new ArrayList<String>();
		final ArrayList<String> faltan = new ArrayList<String>();
		final ArrayList<String> fallaron = new ArrayList<String>();

		for (final String[] tienda : obtenerTiendasCentral()) {
			final String host = tienda[1];
			if (host == null || host.trim().length() == 0) {
				continue;
			}
			final String nombre = tienda[0] + " (" + host + ")";
			final Connection con = new ConexionBaseDatos().obtenerConexionBDTiendaRemota(host);
			if (con == null) {
				System.out.println("SIN RESPUESTA  " + nombre);
				sinRespuesta.add(nombre);
				continue;
			}
			try {
				final boolean antes = existe(con, tabla);
				if (antes) {
					System.out.println("YA EXISTE      " + nombre);
					yaTenian.add(nombre);
				} else if (!aplicar) {
					System.out.println("FALTA          " + nombre);
					faltan.add(nombre);
				} else {
					final Statement stm = con.createStatement();
					for (final String sentencia : sentencias) {
						stm.execute(sentencia);
					}
					stm.close();
					if (existe(con, tabla)) {
						System.out.println("CREADA         " + nombre);
						creadas.add(nombre);
					} else {
						System.out.println("NO QUEDO       " + nombre);
						fallaron.add(nombre);
					}
				}
			} catch (final Exception e) {
				System.out.println("ERROR          " + nombre + ": " + e.toString());
				fallaron.add(nombre + ": " + e.getMessage());
			} finally {
				try {
					con.close();
				} catch (final Exception e1) {
				}
			}
		}

		System.out.println();
		System.out.println("RESUMEN");
		System.out.println("  Ya la tenian:  " + yaTenian.size());
		System.out.println("  Creadas:       " + creadas.size());
		System.out.println("  Faltan:        " + faltan.size() + (faltan.isEmpty() ? "" : " " + faltan));
		System.out.println("  Sin respuesta: " + sinRespuesta.size() + (sinRespuesta.isEmpty() ? "" : " " + sinRespuesta));
		System.out.println("  Fallaron:      " + fallaron.size() + (fallaron.isEmpty() ? "" : " " + fallaron));
	}

	/**
	 * Las tiendas funcionales, leidas de la base central por la conexion remota
	 * (la de Servicios apunta a localhost porque corre en el servidor, y desde
	 * un equipo cualquiera esa base no existe).
	 */
	private static ArrayList<String[]> obtenerTiendasCentral() throws Exception {
		final ArrayList<String[]> tiendas = new ArrayList<String[]>();
		final Connection con = new capaConexionPOS.ConexionBaseDatos().obtenerConexionBDPrincipal();
		if (con == null) {
			System.out.println("No se pudo conectar a la base central para listar las tiendas.");
			return tiendas;
		}
		final Statement stm = con.createStatement();
		final ResultSet rs = stm.executeQuery("select nombre, hosbd from tienda where funcional = 'S' order by nombre");
		while (rs.next()) {
			tiendas.add(new String[] { rs.getString("nombre"), rs.getString("hosbd") });
		}
		rs.close();
		stm.close();
		con.close();
		return tiendas;
	}

	private static boolean existe(final Connection con, final String tabla) throws Exception {
		final PreparedStatement ps = con.prepareStatement(
				"select count(*) from information_schema.tables where table_schema = database() and table_name = ?");
		ps.setString(1, tabla);
		final ResultSet rs = ps.executeQuery();
		final boolean existe = rs.next() && rs.getInt(1) > 0;
		rs.close();
		ps.close();
		return existe;
	}

	/** Quita los comentarios de linea (--) y parte por punto y coma. Sirve para scripts simples, sin ; dentro de textos. */
	private static ArrayList<String> leerSentencias(final File archivo) throws Exception {
		final StringBuilder sb = new StringBuilder();
		for (final String linea : Files.readAllLines(archivo.toPath(), StandardCharsets.UTF_8)) {
			if (!linea.trim().startsWith("--")) {
				sb.append(linea).append('\n');
			}
		}
		final ArrayList<String> sentencias = new ArrayList<String>();
		for (final String parte : sb.toString().split(";")) {
			if (parte.trim().length() > 0) {
				sentencias.add(parte.trim());
			}
		}
		return sentencias;
	}

}
