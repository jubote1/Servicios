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
	 * Horas que hay que restarle a sonoqr_movimiento.fecha_evento para llevarla
	 * a hora colombiana.
	 *
	 * Bold manda el evento en UTC. Verificado el 2026-09-22 contra los pedidos
	 * de Manrique del dia anterior: el pago de 23.500 que el POS registro a las
	 * 17:26 aparece en fecha_evento como 22:27, y asi los ocho de esa noche,
	 * cuadrando al minuto. Colombia no tiene horario de verano, asi que el
	 * desfase es fijo todo el ano y no hace falta CONVERT_TZ -que ademas exige
	 * las tablas de zonas horarias cargadas en cada tienda-.
	 *
	 * SIN ESTA CORRECCION EL COMPARATIVO MIENTE TODOS LOS DIAS: los cobros
	 * despues de las 7 de la noche caen en el dia UTC siguiente. Solo en
	 * Manrique el 2026-09-21 eran cuatro cobros y 118.500 pesos que le habrian
	 * faltado al dia.
	 */
	private static final int HORAS_UTC_A_COLOMBIA = 5;

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

	/**
	 * Lo que Bold dice que cobro ese dia, leido de sonoqr_movimiento.
	 *
	 * Es la otra cara de obtenerQRDia: alla esta lo que el cajero REGISTRO como
	 * pago QR, aca lo que Bold efectivamente COBRO. Comparar las dos es lo que
	 * permite ver si a un pedido le quedo un valor distinto del que se cobro, o
	 * si hubo un cobro que nunca se registro en un pedido.
	 *
	 * EL FILTRO POR FECHA VA POR RANGO Y NO CON DATE(), a proposito: escribir
	 * DATE(fecha_evento - INTERVAL 5 HOUR) = ? obliga a MySQL a calcular la
	 * expresion fila por fila y a dejar de usar el indice. Con los limites
	 * calculados afuera, la consulta sigue sirviendose del indice cuando la
	 * tabla crezca.
	 *
	 * SOLO SE SUMAN LAS VENTAS APROBADAS. Los demas tipos de evento
	 * -anulaciones, devoluciones- se cuentan aparte en la tercera posicion: hoy
	 * no llega ninguno, pero el dia que lleguen no pueden desaparecer en
	 * silencio, porque serian plata que se devolvio y el comparativo quedaria
	 * mintiendo sin que nadie lo note.
	 *
	 * @param fecha  yyyy-MM-dd en hora COLOMBIANA
	 * @param hostBD tienda.hosbd de la tienda
	 * @return {total, cantidad de cobros, cantidad de otros eventos}, o null si
	 *         la tienda no respondio o no tiene la tabla
	 */
	public static double[] obtenerMovimientosBoldDia(final String fecha, final String hostBD) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection con1 = con.obtenerConexionBDTiendaRemota(hostBD);
		if (con1 == null) {
			System.out.println("ConsignacionBoldDAO: no se pudo conectar a la tienda " + hostBD);
			return null;
		}
		try {
			//Los limites en UTC del dia colombiano: de 05:00 a 05:00 del
			//siguiente. El mismo calculo hecho una vez, no una vez por fila.
			final String desde = fecha + " " + dosDigitos(HORAS_UTC_A_COLOMBIA) + ":00:00";
			final String hasta = sumarUnDia(fecha) + " " + dosDigitos(HORAS_UTC_A_COLOMBIA) + ":00:00";

			final PreparedStatement ps = con1.prepareStatement(
					"SELECT IFNULL(SUM(CASE WHEN tipo_evento = 'SALE_APPROVED' THEN monto ELSE 0 END),0),"
							+ " SUM(tipo_evento = 'SALE_APPROVED'),"
							+ " SUM(tipo_evento <> 'SALE_APPROVED')"
							+ " FROM sonoqr_movimiento"
							+ " WHERE metodo = 'QR' AND fecha_evento >= ? AND fecha_evento < ?");
			ps.setString(1, desde);
			ps.setString(2, hasta);
			final ResultSet rs = ps.executeQuery();
			double total = 0;
			int cobros = 0;
			int otros = 0;
			if (rs.next()) {
				total = rs.getDouble(1);
				cobros = rs.getInt(2);
				otros = rs.getInt(3);
			}
			rs.close();
			ps.close();
			con1.close();
			return new double[] { total, cobros, otros };
		} catch (final Exception e) {
			//Tambien cae aca si la tienda todavia no tiene la tabla. Devolver
			//null y no cero es lo correcto: un cero diria que Bold no cobro
			//nada, y lo que pasa es que no sabemos.
			System.out.println("ConsignacionBoldDAO movimientos en " + hostBD + ": " + e.toString());
			try {
				con1.close();
			} catch (final Exception e1) {
			}
			return null;
		}
	}

	private static String dosDigitos(final int valor) {
		return (valor < 10 ? "0" + valor : String.valueOf(valor));
	}

	/** El dia siguiente de un yyyy-MM-dd, sin pelear con los fines de mes. */
	private static String sumarUnDia(final String fecha) {
		try {
			final java.text.SimpleDateFormat formato = new java.text.SimpleDateFormat("yyyy-MM-dd");
			final java.util.Calendar c = java.util.Calendar.getInstance();
			c.setTime(formato.parse(fecha));
			c.add(java.util.Calendar.DAY_OF_YEAR, 1);
			return formato.format(c.getTime());
		} catch (final Exception e) {
			return fecha;
		}
	}

}
