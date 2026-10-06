package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;

/**
 * Los pagos de la tienda virtual que cobra Rapyd (antes PAYU), leidos de la base central.
 *
 * Son los pedidos con idforma_pago 6 en pedido_forma_pago que ya llegaron a una tienda (numposheader > 0): el
 * mismo criterio que usaba el reporte de PAYU, para que las cifras de antes y de ahora se puedan comparar.
 *
 * Se trae un dia de margen a cada lado del periodo pedido, porque el corte de Rapyd es a las 00:00 UTC (las 7 PM
 * de Colombia) y un pago de la noche de un dia pertenece al dia UTC siguiente. Quien llama decide, con el
 * instante de cada pago, a que periodo va.
 */
public class ConsignacionRapydDAO {

	/** idforma_pago de la tienda virtual (PAYU / Rapyd) en pedido_forma_pago. */
	public static final int IDFORMAPAGO_RAPYD = 6;

	/** Un pago de la tienda virtual. */
	public static class PagoRapyd {
		public String tienda;
		public int idPedido;
		/** Jornada del pedido, yyyy-MM-dd. */
		public String fechaPedido;
		/** Cuando se pago, yyyy-MM-dd HH:mm:ss, hora de Colombia: fechapagovirtual y, si no hay, fechainsercion. */
		public String instante;
		public double total;
		/** CARD, PSE... o null si el pedido no lo trae. */
		public String tipoPago;
	}

	/**
	 * @param fechaDesde yyyy-MM-dd, primer dia que interesa
	 * @param fechaHasta yyyy-MM-dd, ultimo dia que interesa
	 * @return los pagos con fechapedido entre un dia antes de fechaDesde y un dia despues de fechaHasta; null si
	 *         no se pudo consultar (distinto de una lista vacia: un correo de consignacion en cero por un fallo
	 *         de conexion haria creer que no hubo ventas)
	 */
	public static ArrayList<PagoRapyd> obtenerPagos(final String fechaDesde, final String fechaHasta) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection con1 = con.obtenerConexionBDContactLocal();
		if (con1 == null) {
			System.out.println("ConsignacionRapydDAO: no se pudo conectar a la base central");
			return null;
		}
		try {
			final PreparedStatement ps = con1.prepareStatement(
					"SELECT b.nombre, a.idpedido, a.fechapedido, a.total_neto, a.tipopago,"
							+ " DATE_FORMAT(IFNULL(a.fechapagovirtual, a.fechainsercion), '%Y-%m-%d %H:%i:%s') instante"
							+ " FROM pedido a, tienda b, pedido_forma_pago f"
							+ " WHERE a.idtienda = b.idtienda AND f.idpedido = a.idpedido"
							+ " AND a.fechapedido >= DATE_SUB(?, INTERVAL 1 DAY)"
							+ " AND a.fechapedido <= DATE_ADD(?, INTERVAL 1 DAY)"
							+ " AND a.numposheader > 0 AND f.idforma_pago = ?");
			ps.setString(1, fechaDesde);
			ps.setString(2, fechaHasta);
			ps.setInt(3, IDFORMAPAGO_RAPYD);
			final ResultSet rs = ps.executeQuery();
			final ArrayList<PagoRapyd> pagos = new ArrayList<PagoRapyd>();
			while (rs.next()) {
				final PagoRapyd p = new PagoRapyd();
				p.tienda = rs.getString("nombre");
				p.idPedido = rs.getInt("idpedido");
				p.fechaPedido = rs.getString("fechapedido");
				p.instante = rs.getString("instante");
				p.total = rs.getDouble("total_neto");
				p.tipoPago = rs.getString("tipopago");
				pagos.add(p);
			}
			rs.close();
			ps.close();
			con1.close();
			return pagos;
		} catch (final Exception e) {
			System.out.println("ConsignacionRapydDAO.obtenerPagos: " + e);
			try {
				con1.close();
			} catch (final Exception e1) {
				// Ya estaba cerrada.
			}
			return null;
		}
	}
}
