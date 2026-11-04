package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.Tienda;

/**
 * La varianza de una semana, por tienda e insumo y costeada en pesos.
 *
 * LEE DIRECTO DE LAS TIENDAS (desde noviembre de 2026)
 *
 * Antes leia datamart.varianza_resumen_historico, que ServicioDiarioReplicaVarianza
 * llena cada noche con el dia ANTERIOR. Eso obligaba a correr el reporte el martes
 * de madrugada para tener el domingo. Ahora se consulta varianza_resumen de cada
 * tienda -la misma tabla de donde sale la replica-, asi que el reporte puede salir
 * el domingo a las 23:50 con la semana completa.
 *
 * El precio de salir a las tiendas es que una puede estar caida. Antes eso no podia
 * pasar y por eso el reporte no tenia que decirlo; ahora SI, y se dice: el resultado
 * trae las tiendas que no respondieron y las que respondieron sin los siete dias
 * (por ejemplo, porque el conteo del domingo todavia no estaba cargado). Un correo
 * que parece completo y no lo es, es peor que no mandarlo.
 *
 * DE DONDE SALE CADA COSA
 *
 *  - La varianza (cantidad por item y por dia) sale de varianza_resumen de CADA tienda.
 *  - Que insumo es cada item de la tienda, su grupo, su unidad y su costo salen del
 *    central: inventarioamericana.insumo_homologacion_tienda e insumo. Las tiendas
 *    no tienen el costo.
 *
 * LA FORMULA DEL COSTO
 *
 * costo_unidad no esta en la unidad de la varianza. La columna que dice a
 * cuanto equivale es embalaje_costo, y NO siempre vale mil: el queso trae 1.000
 * -20.200 el kilo-, la carne molida trae 400 -11.047 por 400 gramos, o sea
 * 27.618 el kilo- y el tomate y la cebolla traen 500. En unidad y paquete vale
 * 0 en los 95 insumos que se miden asi, y ahi el costo es por unidad.
 *
 * Es la misma formula de la pantalla de monitoreo de varianzas del proyecto de
 * inventarios. Si alguna vez hay que cambiarla, hay que cambiarla en los dos
 * lados o el correo y la pantalla van a decir cosas distintas.
 *
 * EL SIGNO
 *
 * Varianza negativa es faltante: plata que se perdio. Positiva es sobrante, que
 * casi nunca es ganancia; suele ser un conteo malo o una receta mal
 * parametrizada. Por eso se llevan separados y no solo el neto, que se compensa
 * y esconde el problema. El faltante y el sobrante se separan DIA A DIA, no sobre la
 * suma de la semana: un dia que falta y otro que sobra no se anulan.
 */
public class VarianzaSemanalDAO {

	/** Los intentos de conexion a cada tienda antes de darla por caida. */
	private static final int INTENTOS = 2;

	/** Una linea: un insumo de una tienda en la semana. */
	public static class Linea {
		public int idTienda;
		public String tienda = "";
		public String grupo = "";
		public int idInsumo;
		public String insumo = "";
		public String unidad = "";
		public double cantidad;
		public double faltante;
		public double sobrante;
		public double neto;
	}

	/** Lo que devuelve una consulta de la semana: las lineas y lo que no se pudo traer completo. */
	public static class Semana {
		public ArrayList<Linea> lineas = new ArrayList<Linea>();
		/** Tiendas a las que no se pudo conectar o cuya consulta fallo. */
		public ArrayList<String> sinRespuesta = new ArrayList<String>();
		/** Tiendas que respondieron pero sin todos los dias del rango, p. ej. "Bello: 6 de 7 dias (ultimo dato 2026-11-07)". */
		public ArrayList<String> incompletas = new ArrayList<String>();
		/** Cuantas tiendas respondieron con datos. */
		public int tiendasConDatos = 0;

		/** true si todo lo que se esperaba llego completo. */
		public boolean completa() {
			return (sinRespuesta.isEmpty() && incompletas.isEmpty());
		}
	}

	/** El insumo del central al que corresponde un item de una tienda. */
	private static class Insumo {
		int idInsumo;
		String nombre = "";
		String unidad = "";
		String grupo = "OTROS";
		double costoUnidad;
		double embalajeCosto;

		/** La varianza llevada a pesos: ver "LA FORMULA DEL COSTO". */
		double factor() {
			return (costoUnidad / (embalajeCosto > 0 ? embalajeCosto : 1));
		}
	}

	/**
	 * Todo lo que se movio en el rango, una linea por tienda e insumo, y el estado de
	 * cada tienda.
	 *
	 * Se devuelve al detalle y no ya sumado por grupo porque el correo muestra
	 * los caros insumo por insumo, y agrupar aca obligaria a una segunda
	 * consulta para lo mismo.
	 *
	 * Los insumos sin grupo quedan como OTROS y no como nulo, para que el que
	 * arme el correo no tenga que preguntar por nulos en cada comparacion.
	 *
	 * @param fechaDesde primer dia, inclusive, yyyy-MM-dd
	 * @param fechaHasta ultimo dia, inclusive, yyyy-MM-dd
	 * @param diasEsperados cuantos dias trae el rango (para decir si una tienda llego incompleta)
	 */
	public static Semana obtenerSemana(final String fechaDesde, final String fechaHasta, final int diasEsperados) {
		final Semana semana = new Semana();

		//El catalogo y los costos: una sola consulta al central, para todas las tiendas.
		final HashMap<Integer, HashMap<Integer, Insumo>> catalogo = obtenerCatalogo();
		if (catalogo.isEmpty()) {
			//Sin catalogo no se puede costear nada: se deja constancia en lugar de armar un
			//correo en ceros. Quien llama ve la lista vacia y no envia.
			System.out.println("VarianzaSemanalDAO: no se pudo leer el catalogo de insumos del central. Nada que costear.");
			return (semana);
		}

		for (final Tienda t : TiendaDAO.obtenerTiendasLocal()) {
			final String host = (t.getHostBD() == null ? "" : t.getHostBD().trim());
			//La Bodega no lleva varianza de tienda y las tiendas sin host no se consultan.
			if (host.length() == 0 || "BODEGA".equalsIgnoreCase(t.getNombreTienda())) {
				continue;
			}
			final HashMap<Integer, Insumo> deEstaTienda = catalogo.get(Integer.valueOf(t.getIdTienda()));
			if (deEstaTienda == null) {
				System.out.println("VarianzaSemanalDAO: la tienda " + t.getNombreTienda()
						+ " no tiene insumos homologados en el central; se omite.");
				continue;
			}
			consultarTienda(t, host, deEstaTienda, fechaDesde, fechaHasta, diasEsperados, semana);
		}
		return (semana);
	}

	/** Los insumos homologados de cada tienda: idtienda -> (item de la tienda -> insumo del central). */
	private static HashMap<Integer, HashMap<Integer, Insumo>> obtenerCatalogo() {
		final HashMap<Integer, HashMap<Integer, Insumo>> catalogo = new HashMap<Integer, HashMap<Integer, Insumo>>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try {
			cn = con.obtenerConexionBDDatamartLocal();
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT h.idtienda, h.insumotienda, i.idinsumo, i.nombre_insumo, i.unidad_medida,"
					+ "      IFNULL(i.grupo_varianza, 'OTROS') AS grupo, i.costo_unidad, i.embalaje_costo"
					+ "  FROM inventarioamericana.insumo_homologacion_tienda h"
					+ "  JOIN inventarioamericana.insumo i ON i.idinsumo = h.idinsumo");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Insumo in = new Insumo();
				in.idInsumo = rs.getInt("idinsumo");
				in.nombre = texto(rs.getString("nombre_insumo"));
				in.unidad = texto(rs.getString("unidad_medida"));
				in.grupo = texto(rs.getString("grupo"));
				in.costoUnidad = rs.getDouble("costo_unidad");
				in.embalajeCosto = rs.getDouble("embalaje_costo");
				final Integer idTienda = Integer.valueOf(rs.getInt("idtienda"));
				HashMap<Integer, Insumo> deTienda = catalogo.get(idTienda);
				if (deTienda == null) {
					deTienda = new HashMap<Integer, Insumo>();
					catalogo.put(idTienda, deTienda);
				}
				deTienda.put(Integer.valueOf(rs.getInt("insumotienda")), in);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("VarianzaSemanalDAO.obtenerCatalogo: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (catalogo);
	}

	/**
	 * Trae la varianza de una tienda y la suma en la semana. Si la tienda no responde, la anota en
	 * sinRespuesta; si responde sin todos los dias, en incompletas.
	 */
	private static void consultarTienda(final Tienda tienda, final String host,
			final HashMap<Integer, Insumo> insumos, final String fechaDesde, final String fechaHasta,
			final int diasEsperados, final Semana semana) {
		Connection cn = null;
		try {
			final ConexionBaseDatos con = new ConexionBaseDatos();
			//Un intento mas antes de darla por caida: una conexion que se corta una vez no es una tienda caida.
			for (int intento = 1; intento <= INTENTOS && cn == null; intento++) {
				cn = con.obtenerConexionBDTiendaRemota(host);
				if (cn == null && intento < INTENTOS) {
					Thread.sleep(3000L);
				}
			}
			if (cn == null) {
				semana.sinRespuesta.add(tienda.getNombreTienda());
				return;
			}

			//Los dias que de verdad tiene la tienda en el rango.
			int dias = 0;
			String ultimo = "";
			final PreparedStatement psDias = cn.prepareStatement(
					"SELECT COUNT(DISTINCT fecha) AS dias, MAX(fecha) AS ultimo FROM varianza_resumen WHERE fecha BETWEEN ? AND ?");
			psDias.setString(1, fechaDesde);
			psDias.setString(2, fechaHasta);
			final ResultSet rsDias = psDias.executeQuery();
			if (rsDias.next()) {
				dias = rsDias.getInt("dias");
				ultimo = texto(rsDias.getString("ultimo"));
			}
			rsDias.close();
			psDias.close();

			//Por item: la suma, y los dias que faltaron y los que sobraron por separado.
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT iditem, SUM(varianza) AS cantidad,"
					+ "      SUM(IF(varianza < 0, varianza, 0)) AS negativa,"
					+ "      SUM(IF(varianza > 0, varianza, 0)) AS positiva"
					+ "  FROM varianza_resumen WHERE fecha BETWEEN ? AND ? GROUP BY iditem");
			ps.setString(1, fechaDesde);
			ps.setString(2, fechaHasta);
			final ResultSet rs = ps.executeQuery();

			//Varios items de una tienda pueden ser el mismo insumo del central: se suman.
			final Map<Integer, Linea> porInsumo = new LinkedHashMap<Integer, Linea>();
			while (rs.next()) {
				final Insumo in = insumos.get(Integer.valueOf(rs.getInt("iditem")));
				if (in == null) {
					continue;
				}
				Linea l = porInsumo.get(Integer.valueOf(in.idInsumo));
				if (l == null) {
					l = new Linea();
					l.idTienda = tienda.getIdTienda();
					l.tienda = tienda.getNombreTienda();
					l.grupo = in.grupo;
					l.idInsumo = in.idInsumo;
					l.insumo = in.nombre;
					l.unidad = in.unidad;
					porInsumo.put(Integer.valueOf(in.idInsumo), l);
				}
				final double factor = in.factor();
				l.cantidad = l.cantidad + rs.getDouble("cantidad");
				l.faltante = l.faltante + rs.getDouble("negativa") * factor;
				l.sobrante = l.sobrante + rs.getDouble("positiva") * factor;
				l.neto = l.neto + rs.getDouble("cantidad") * factor;
			}
			rs.close();
			ps.close();

			if (dias == 0 || porInsumo.isEmpty()) {
				//Respondio pero sin nada del rango: para el reporte es lo mismo que no haber respondido.
				semana.incompletas.add(tienda.getNombreTienda() + ": sin datos de varianza en el rango");
				return;
			}
			semana.lineas.addAll(porInsumo.values());
			semana.tiendasConDatos++;
			if (dias < diasEsperados) {
				semana.incompletas.add(tienda.getNombreTienda() + ": " + dias + " de " + diasEsperados
						+ " dias (ultimo dato " + ultimo + ")");
			}
		} catch (final Exception e) {
			System.out.println("VarianzaSemanalDAO.consultarTienda " + tienda.getNombreTienda() + ": " + e.toString());
			semana.sinRespuesta.add(tienda.getNombreTienda());
		} finally {
			cerrar(cn);
		}
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
			System.out.println("VarianzaSemanalDAO: no cerro la conexion, " + e.toString());
		}
	}
}
