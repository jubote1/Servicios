package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import ConexionSer.ConexionBaseDatos;

/**
 * Lo cobrado por QR (Bold) en una tienda un dia, leido de la base local de la
 * tienda.
 *
 * Devuelve null cuando no se pudo consultar la tienda (computador apagado o
 * sin red). Es distinto de cero: en un correo de consignacion un cero por
 * tienda apagada haria creer que se vendio menos de lo que se vendio.
 */
public class ConsignacionBoldDAO {

	/** idforma_pago del QR en pedido_forma_pago. Confirmado con el usuario. */
	public static final int IDFORMAPAGO_QR = 7;

	/**
	 * @param fecha  yyyy-MM-dd, la fecha de jornada (pedido.fechapedido)
	 * @param hostBD tienda.hosbd de la tienda
	 * @return {total, cantidad de pagos}, o null si la tienda no respondio
	 */
	public static double[] obtenerQRDia(final String fecha, final String hostBD) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection con1 = con.obtenerConexionBDTiendaRemota(hostBD);
		if (con1 == null) {
			System.out.println("ConsignacionBoldDAO: no se pudo conectar a la tienda " + hostBD);
			return null;
		}
		try {
			final PreparedStatement ps = con1.prepareStatement(
					"SELECT IFNULL(SUM(b.valordisminuido),0), COUNT(*) FROM pedido a, pedido_forma_pago b"
							+ " WHERE a.idpedidotienda = b.idpedidotienda AND b.idforma_pago = ?"
							+ " AND a.fechapedido = ?");
			ps.setInt(1, IDFORMAPAGO_QR);
			ps.setString(2, fecha);
			final ResultSet rs = ps.executeQuery();
			double total = 0;
			int cantidad = 0;
			if (rs.next()) {
				total = rs.getDouble(1);
				cantidad = rs.getInt(2);
			}
			rs.close();
			ps.close();
			con1.close();
			return new double[] { total, cantidad };
		} catch (final Exception e) {
			System.out.println("ConsignacionBoldDAO en " + hostBD + ": " + e.toString());
			try {
				con1.close();
			} catch (final Exception e1) {
			}
			return null;
		}
	}

}
