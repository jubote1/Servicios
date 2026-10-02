package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import ConexionSer.ConexionBaseDatos;

/**
 * El reporte diario de promociones, leyendo el catalogo en vez del codigo.
 *
 * SE MIDE EN LAS TIENDAS, POR PRODUCTO, Y EN UNA SOLA CONSULTA
 *
 * El reporte viejo mide por dos lados -los productos en la base de cada tienda
 * y el idexcepcion en la base del central- y hace una consulta por promocion y
 * por tienda. Eso son dos listas que mantener, riesgo de contar dos veces, y
 * mas de cien consultas cada noche.
 *
 * Aca se baja todo de una: UNA consulta por tienda que trae todas las
 * promociones abiertas por canal. Once consultas en total.
 *
 * Y se mide solo en la tienda porque la base de la tienda tiene TODOS los
 * canales: el mostrador -que es el 41% que el central no ve-, el contact
 * center, el CRM, la app, la tienda virtual y las plataformas. El canal sale
 * de pedido.estacion.
 *
 * NO SE DIVIDE PLATA ENTRE UN PRECIO
 *
 * El reporte viejo saca la cantidad con SUM(valorunitario) / precio_quemado.
 * Hay doce precios escritos en el codigo, y el dia que una promocion cambia de
 * precio la cantidad queda mal sin que nadie se entere. Aca se suma
 * detalle_pedido.cantidad, que es la cantidad.
 */
public class PromocionReporteDAO {

	/** Una promocion del catalogo, con los productos que la componen. */
	public static class Promocion {
		public int idPromo;
		public String nombre = "";
		public boolean plataforma;
		public int orden;
		public ArrayList<Integer> productos = new ArrayList<Integer>();
	}

	/** Lo vendido de una promocion en un dia, por tienda y canal. */
	public static class Venta {
		public int idPromo;
		public int idTienda;
		public String canal = "";
		public double unidades;
		public double valor;
	}

	/** El acumulado de una promocion, para la tabla del correo. */
	public static class Resumen {
		public int idPromo;
		public String nombre = "";
		public boolean plataforma;
		public double unidades;
		public double valor;
		/** Promedio del mismo dia de las semanas anteriores. -1 = sin referencia. */
		public double promedioUnidades = -1;
		/** Sobre cuantos dias se saco el promedio. */
		public int diasDeReferencia;
	}

	/**
	 * El canal, a partir de la estacion del pedido.
	 *
	 * Va como CASE en SQL y no en Java para que el agrupado lo haga la base y
	 * no viaje una fila por pedido por la red.
	 */
	private static final String CANAL =
			"CASE"
			+ " WHEN p.estacion LIKE '%servid%'  THEN 'MOSTRADOR'"
			+ " WHEN p.estacion LIKE '%CONTACT%' THEN 'CONTACT'"
			+ " WHEN p.estacion LIKE '%CRM%'     THEN 'CRM'"
			+ " WHEN p.estacion LIKE '%DIDI%'    THEN 'DIDI'"
			+ " WHEN p.estacion LIKE '%RAPPI%'   THEN 'RAPPI'"
			+ " WHEN p.estacion LIKE '%APP%'     THEN 'APP'"
			+ " WHEN p.estacion LIKE '%VIRTUAL%' THEN 'VIRTUAL'"
			+ " ELSE 'OTRO' END";

	// =======================================================================
	// EL CATALOGO
	// =======================================================================

	/** Las promociones activas, con sus productos, en el orden de la pantalla. */
	public static ArrayList<Promocion> obtenerCatalogo() {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		final LinkedHashMap<Integer, Promocion> porId = new LinkedHashMap<Integer, Promocion>();
		try {
			cn = con.obtenerConexionBDContactLocal();
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT p.idpromo, p.nombre, p.plataforma, p.orden, i.idproducto"
					+ " FROM promocion_reporte p"
					+ " LEFT JOIN promocion_reporte_item i"
					+ "        ON i.idpromo = p.idpromo AND i.activo = 'S'"
					+ " WHERE p.activo = 'S'"
					+ " ORDER BY p.orden, p.idpromo, i.idproducto");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final int id = rs.getInt("idpromo");
				Promocion promo = porId.get(Integer.valueOf(id));
				if (promo == null) {
					promo = new Promocion();
					promo.idPromo = id;
					promo.nombre = rs.getString("nombre");
					promo.plataforma = "S".equalsIgnoreCase(rs.getString("plataforma"));
					promo.orden = rs.getInt("orden");
					porId.put(Integer.valueOf(id), promo);
				}
				final int idProducto = rs.getInt("idproducto");
				if (!rs.wasNull() && idProducto > 0) {
					promo.productos.add(Integer.valueOf(idProducto));
				}
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("PromocionReporteDAO.obtenerCatalogo: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (new ArrayList<Promocion>(porId.values()));
	}

	// =======================================================================
	// LA MEDICION
	// =======================================================================

	/**
	 * Lo vendido de TODAS las promociones en una tienda, en un dia.
	 *
	 * Una sola consulta por tienda. Devuelve null -y no una lista vacia- cuando
	 * la tienda no responde: el proceso tiene que poder distinguir "no vendio
	 * nada" de "no contesto", porque lo segundo no se puede guardar como cero.
	 */
	public static ArrayList<Venta> obtenerVentaDelDia(final String hostBD, final int idTienda,
			final String fecha, final ArrayList<Promocion> catalogo) {

		final LinkedHashMap<Integer, Integer> promoDeProducto = new LinkedHashMap<Integer, Integer>();
		final StringBuilder lista = new StringBuilder();
		for (int i = 0; i < catalogo.size(); i++) {
			final Promocion promo = catalogo.get(i);
			for (int j = 0; j < promo.productos.size(); j++) {
				final Integer idProducto = promo.productos.get(j);
				promoDeProducto.put(idProducto, Integer.valueOf(promo.idPromo));
				if (lista.length() > 0) {
					lista.append(",");
				}
				lista.append(idProducto);
			}
		}
		if (lista.length() == 0) {
			return (new ArrayList<Venta>());
		}

		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		ArrayList<Venta> ventas = null;
		try {
			cn = con.obtenerConexionBDTiendaRemota(hostBD);
			if (cn == null) {
				return (null);
			}
			//La lista de productos sale del catalogo, no de nada que escriba un
			//usuario, asi que va concatenada: con IN dinamico un PreparedStatement
			//obligaria a rearmar la sentencia igual.
			final String consulta =
					"SELECT d.idproducto, " + CANAL + " AS canal,"
					+ " SUM(d.cantidad) AS unidades, SUM(d.valortotal) AS valor"
					+ " FROM pedido p, detalle_pedido d"
					+ " WHERE p.idpedidotienda = d.idpedidotienda"
					+ "   AND p.fechapedido = '" + fecha + "'"
					+ "   AND d.idmotivoanulacion IS NULL"
					+ "   AND d.idproducto IN (" + lista.toString() + ")"
					+ " GROUP BY d.idproducto, canal";
			final Statement stm = cn.createStatement();
			final ResultSet rs = stm.executeQuery(consulta);
			ventas = new ArrayList<Venta>();
			while (rs.next()) {
				final Integer idPromo = promoDeProducto.get(Integer.valueOf(rs.getInt("idproducto")));
				if (idPromo == null) {
					continue;
				}
				final Venta v = new Venta();
				v.idPromo = idPromo.intValue();
				v.idTienda = idTienda;
				v.canal = rs.getString("canal");
				v.unidades = rs.getDouble("unidades");
				v.valor = rs.getDouble("valor");
				ventas.add(v);
			}
			rs.close();
			stm.close();
		} catch (final Exception e) {
			System.out.println("PromocionReporteDAO: fallo consultando " + hostBD + ": " + e.toString());
			//null, no lista vacia: no se pudo preguntar.
			ventas = null;
		} finally {
			cerrar(cn);
		}
		return (ventas);
	}

	// =======================================================================
	// LA HISTORIA
	// =======================================================================

	/**
	 * Guarda lo del dia.
	 *
	 * Con ON DUPLICATE KEY UPDATE para que reprocesar un dia lo corrija en vez
	 * de duplicarlo. Varias filas de la misma promocion y canal -una por
	 * producto- se suman antes de llegar aca.
	 */
	public static int guardarDia(final String fecha, final ArrayList<Venta> ventas) {
		if (ventas == null || ventas.isEmpty()) {
			return (0);
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		int guardadas = 0;
		try {
			cn = con.obtenerConexionBDDatamartLocal();
			final PreparedStatement ps = cn.prepareStatement(
					"INSERT INTO promocion_dia (fecha, idpromo, idtienda, canal, unidades, valor)"
					+ " VALUES (?, ?, ?, ?, ?, ?)"
					+ " ON DUPLICATE KEY UPDATE unidades = ?, valor = ?, grabado_en = NOW()");
			for (int i = 0; i < ventas.size(); i++) {
				final Venta v = ventas.get(i);
				ps.setString(1, fecha);
				ps.setInt(2, v.idPromo);
				ps.setInt(3, v.idTienda);
				ps.setString(4, v.canal);
				ps.setDouble(5, v.unidades);
				ps.setDouble(6, v.valor);
				ps.setDouble(7, v.unidades);
				ps.setDouble(8, v.valor);
				ps.addBatch();
			}
			final int[] filas = ps.executeBatch();
			guardadas = filas.length;
			ps.close();
		} catch (final Exception e) {
			System.out.println("PromocionReporteDAO.guardarDia: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (guardadas);
	}

	/**
	 * El promedio del MISMO DIA DE LA SEMANA en las cuatro semanas anteriores.
	 *
	 * Se compara contra el mismo dia y no contra ayer porque un domingo no se
	 * parece a un martes: una promocion puede caer a la mitad el lunes y estar
	 * perfecta.
	 *
	 * Solo cuentan los dias que de verdad tienen historia. Si la tabla arranco
	 * hace una semana, el promedio es sobre un dia y el correo lo dice; no se
	 * finge una referencia de cuatro semanas que no existe.
	 */
	public static void completarPromedios(final String fecha, final ArrayList<Resumen> resumenes) {
		if (resumenes == null || resumenes.isEmpty()) {
			return;
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try {
			cn = con.obtenerConexionBDDatamartLocal();
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT idpromo, SUM(unidades) AS unidades, COUNT(DISTINCT fecha) AS dias"
					+ " FROM promocion_dia"
					+ " WHERE fecha IN (DATE_SUB(?, INTERVAL 7 DAY), DATE_SUB(?, INTERVAL 14 DAY),"
					+ "                 DATE_SUB(?, INTERVAL 21 DAY), DATE_SUB(?, INTERVAL 28 DAY))"
					+ " GROUP BY idpromo");
			ps.setString(1, fecha);
			ps.setString(2, fecha);
			ps.setString(3, fecha);
			ps.setString(4, fecha);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final int idPromo = rs.getInt("idpromo");
				final double unidades = rs.getDouble("unidades");
				final int dias = rs.getInt("dias");
				for (int i = 0; i < resumenes.size(); i++) {
					final Resumen r = resumenes.get(i);
					if (r.idPromo == idPromo && dias > 0) {
						r.promedioUnidades = unidades / dias;
						r.diasDeReferencia = dias;
					}
				}
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("PromocionReporteDAO.completarPromedios: " + e.toString());
		} finally {
			cerrar(cn);
		}
	}

	private static void cerrar(final Connection cn) {
		try {
			if (cn != null) {
				cn.close();
			}
		} catch (final Exception e) {
			System.out.println("PromocionReporteDAO: cerrando conexion " + e.toString());
		}
	}
}
