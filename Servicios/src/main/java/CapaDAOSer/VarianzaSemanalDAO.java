package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;

/**
 * La varianza de una semana, por tienda e insumo y costeada en pesos.
 *
 * Lee datamart.varianza_resumen_historico, que es donde
 * ServicioDiarioReplicaVarianza deja cada noche lo de las once tiendas. No sale
 * a consultar tiendas: si una estuviera caida el reporte saldria incompleto y
 * nadie se enteraria.
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
 * y esconde el problema.
 */
public class VarianzaSemanalDAO {

	/** La varianza llevada a pesos. Ver arriba por que el divisor es embalaje_costo. */
	private static final String VALOR =
			" (v.varianza * i.costo_unidad / IF(i.embalaje_costo > 0, i.embalaje_costo, 1)) ";

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

	/**
	 * Todo lo que se movio en el rango, una linea por tienda e insumo.
	 *
	 * Se devuelve al detalle y no ya sumado por grupo porque el correo muestra
	 * los caros insumo por insumo, y agrupar aca obligaria a una segunda
	 * consulta para lo mismo.
	 *
	 * Los insumos sin grupo quedan como OTROS y no como nulo, para que el que
	 * arme el correo no tenga que preguntar por nulos en cada comparacion.
	 */
	public static ArrayList<Linea> obtenerSemana(final String fechaDesde, final String fechaHasta) {
		final ArrayList<Linea> lineas = new ArrayList<Linea>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try {
			cn = con.obtenerConexionBDDatamartLocal();
			final String sql =
					"SELECT v.idtienda,"
					+ "      IFNULL(t.nombre, CONCAT('Tienda ', v.idtienda)) AS tienda,"
					+ "      IFNULL(i.grupo_varianza, 'OTROS') AS grupo,"
					+ "      i.idinsumo, i.nombre_insumo, i.unidad_medida,"
					+ "      SUM(v.varianza) AS cantidad,"
					+ "      SUM(IF(v.varianza < 0," + VALOR + ", 0)) AS faltante,"
					+ "      SUM(IF(v.varianza > 0," + VALOR + ", 0)) AS sobrante,"
					+ "      SUM(" + VALOR + ") AS neto"
					+ "  FROM datamart.varianza_resumen_historico v"
					+ "  JOIN inventarioamericana.insumo_homologacion_tienda h"
					+ "    ON h.idtienda = v.idtienda AND h.insumotienda = v.iditem"
					+ "  JOIN inventarioamericana.insumo i ON i.idinsumo = h.idinsumo"
					+ "  LEFT JOIN pizzaamericana.tienda t ON t.idtienda = v.idtienda"
					+ " WHERE v.fecha BETWEEN ? AND ?"
					+ " GROUP BY v.idtienda, t.nombre, i.grupo_varianza, i.idinsumo,"
					+ "          i.nombre_insumo, i.unidad_medida"
					+ " ORDER BY tienda, grupo, neto";

			final PreparedStatement ps = cn.prepareStatement(sql);
			ps.setString(1, fechaDesde);
			ps.setString(2, fechaHasta);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Linea l = new Linea();
				l.idTienda = rs.getInt("idtienda");
				l.tienda = texto(rs.getString("tienda"));
				l.grupo = texto(rs.getString("grupo"));
				l.idInsumo = rs.getInt("idinsumo");
				l.insumo = texto(rs.getString("nombre_insumo"));
				l.unidad = texto(rs.getString("unidad_medida"));
				l.cantidad = rs.getDouble("cantidad");
				l.faltante = rs.getDouble("faltante");
				l.sobrante = rs.getDouble("sobrante");
				l.neto = rs.getDouble("neto");
				lineas.add(l);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("VarianzaSemanalDAO.obtenerSemana: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (lineas);
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
