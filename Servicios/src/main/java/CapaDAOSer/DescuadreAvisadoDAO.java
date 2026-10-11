package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;

import ConexionSer.ConexionBaseDatos;
import capaDAOPOS.PedidoDAO.Descuadre;

/**
 * La memoria de los descuadres que ya se avisaron.
 *
 * El resumen de operacion corre cada 30 minutos. Mientras la tienda no corrija
 * el descuadre, la consulta lo sigue encontrando y antes se mandaba el mismo
 * correo otra vez, y otra, y otra. Un aviso repetido veinte veces deja de ser
 * un aviso: se vuelve ruido que nadie abre.
 *
 * Aqui se anota lo avisado en general.descuadre_avisado. La clave incluye el
 * total y lo pagado, no solo el numero de pedido: si la tienda corrige a medias
 * y las cifras cambian, es otro descuadre y vuelve a avisar.
 */
public final class DescuadreAvisadoDAO {

	private DescuadreAvisadoDAO() {
		super();
	}

	/** Lo nuevo que hay que avisar, y cuantos de los viejos siguen sin corregir. */
	public static final class Novedad {
		public final ArrayList<Descuadre> nuevos = new ArrayList<Descuadre>();
		public int repetidos = 0;
	}

	/**
	 * Separa los descuadres que todavia no se han avisado y los deja anotados.
	 *
	 * Si la base no responde devuelve TODOS como nuevos: es preferible un correo
	 * repetido a que el aviso se pierda en silencio.
	 *
	 * @param idTienda    la tienda
	 * @param fechaPedido la fecha de los pedidos, en aaaa-MM-dd
	 * @param descuadres  lo que encontro la consulta en esta corrida
	 * @return los que hay que avisar y cuantos ya se habian avisado
	 */
	public static Novedad soloNuevos(final int idTienda, final String fechaPedido,
			final ArrayList<Descuadre> descuadres) {
		final Novedad novedad = new Novedad();
		if (descuadres == null || descuadres.isEmpty()) {
			return (novedad);
		}

		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = null;
		try {
			con1 = con.obtenerConexionBDGeneralLocal();

			final HashSet<String> yaAvisados = new HashSet<String>();
			final PreparedStatement lectura = con1.prepareStatement(
					"select idpedidotienda, total, pagado from descuadre_avisado"
					+ " where idtienda = ? and fechapedido = ?");
			lectura.setInt(1, idTienda);
			lectura.setString(2, fechaPedido);
			final ResultSet rs = lectura.executeQuery();
			while (rs.next()) {
				yaAvisados.add(DescuadreAvisadoDAO.llave(rs.getInt("idpedidotienda"),
						rs.getDouble("total"), rs.getDouble("pagado")));
			}
			rs.close();
			lectura.close();

			final PreparedStatement anotar = con1.prepareStatement(
					"insert ignore into descuadre_avisado"
					+ " (idtienda, fechapedido, idpedidotienda, total, pagado)"
					+ " values (?, ?, ?, ?, ?)");

			for (int i = 0; i < descuadres.size(); i++) {
				final Descuadre d = descuadres.get(i);
				if (yaAvisados.contains(DescuadreAvisadoDAO.llave(d.idPedido, d.total, d.pagado))) {
					novedad.repetidos = novedad.repetidos + 1;
					continue;
				}
				novedad.nuevos.add(d);
				anotar.setInt(1, idTienda);
				anotar.setString(2, fechaPedido);
				anotar.setInt(3, d.idPedido);
				anotar.setDouble(4, DescuadreAvisadoDAO.redondear(d.total));
				anotar.setDouble(5, DescuadreAvisadoDAO.redondear(d.pagado));
				anotar.addBatch();
			}
			if (!novedad.nuevos.isEmpty()) {
				anotar.executeBatch();
			}
			anotar.close();
			con1.close();
		} catch (final Exception e) {
			//Si no se pudo leer la memoria, se avisa todo. El ruido se aguanta;
			//un descuadre que nadie ve, no.
			System.out.println("No se pudo revisar los descuadres ya avisados de la tienda " + idTienda
					+ ": " + e.toString());
			novedad.nuevos.clear();
			novedad.nuevos.addAll(descuadres);
			novedad.repetidos = 0;
			try {
				if (con1 != null) {
					con1.close();
				}
			} catch (final Exception e1) {
				//Ya no hay nada que hacer con la conexion.
			}
		}
		return (novedad);
	}

	/**
	 * La llave de un descuadre. Va redondeada a dos decimales porque en la tabla
	 * es DECIMAL(14,2): sin redondear, el doble que sale de MySQL y el que trae
	 * la tienda no se parecen lo suficiente y todo seria siempre "nuevo".
	 */
	private static String llave(final int idPedido, final double total, final double pagado) {
		return (idPedido + "|" + DescuadreAvisadoDAO.redondear(total) + "|"
				+ DescuadreAvisadoDAO.redondear(pagado));
	}

	private static double redondear(final double valor) {
		return (Math.round(valor * 100.0) / 100.0);
	}
}
