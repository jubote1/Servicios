package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.Campana15MinAplicado;

/**
 * Deteccion de incumplimientos de la campana "15 minutos o gratis". Vive toda
 * en el central (pizzaamericana): campana_15min_aplicado la escribe el POS al
 * enviar el pedido a cocina, campana_15min_incumplimiento es la cola que
 * revisa Servicio al Cliente.
 *
 * No hay forma de saber si la pizza realmente se entrego a tiempo -eso es un
 * evento fisico de cocina/mostrador, no algo que el sistema capture-, asi que
 * la deteccion no intenta confirmar el incumplimiento: solo marca como
 * "posible incumplimiento" todo pedido que supero los minutos prometidos sin
 * evaluar, para que un humano decida. Es la revision silenciosa que se pidio:
 * no bloquea nada, no avisa por correo, solo deja el caso en la cola.
 */
public class Campana15MinDAO {

	public static ArrayList<Campana15MinAplicado> obtenerVencidosSinEvaluar() {
		final ConexionBaseDatos conexion = new ConexionBaseDatos();
		final Connection con = conexion.obtenerConexionBDContact();
		final ArrayList<Campana15MinAplicado> lista = new ArrayList<Campana15MinAplicado>();
		try {
			final PreparedStatement pst = con.prepareStatement("select a.idpedidotienda, a.idtienda, a.idcampana,"
					+ " a.fecha_hora_inicio, a.valor_base_pizza, a.idformapago_virtual"
					+ " from campana_15min_aplicado a join campana_15min_config c on c.idcampana = a.idcampana"
					+ " where a.estado_cumplido is null"
					+ " and timestampdiff(minute, a.fecha_hora_inicio, now()) > c.minutos_promesa");
			final ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				lista.add(new Campana15MinAplicado(rs.getInt("idpedidotienda"), rs.getInt("idtienda"),
						rs.getInt("idcampana"), rs.getString("fecha_hora_inicio"), rs.getDouble("valor_base_pizza"),
						rs.getString("idformapago_virtual")));
			}
			rs.close();
			pst.close();
			con.close();
		} catch (final Exception e) {
			System.out.println("Campana15MinDAO.obtenerVencidosSinEvaluar: " + e.toString());
			try {
				con.close();
			} catch (final Exception e1) {
			}
		}
		return (lista);
	}

	public static double obtenerPorcentajeRetencion(int idCampana) {
		final ConexionBaseDatos conexion = new ConexionBaseDatos();
		final Connection con = conexion.obtenerConexionBDContact();
		double porcentaje = 0;
		try {
			final PreparedStatement pst = con.prepareStatement(
					"select porcentaje_retencion_medio_virtual from campana_15min_config where idcampana = ?");
			pst.setInt(1, idCampana);
			final ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				porcentaje = rs.getDouble(1);
			}
			rs.close();
			pst.close();
			con.close();
		} catch (final Exception e) {
			System.out.println("Campana15MinDAO.obtenerPorcentajeRetencion: " + e.toString());
			try {
				con.close();
			} catch (final Exception e1) {
			}
		}
		return (porcentaje);
	}

	public static boolean existeIncumplimiento(int idPedidoTienda, int idTienda) {
		final ConexionBaseDatos conexion = new ConexionBaseDatos();
		final Connection con = conexion.obtenerConexionBDContact();
		boolean existe = false;
		try {
			final PreparedStatement pst = con.prepareStatement(
					"select 1 from campana_15min_incumplimiento where idpedidotienda = ? and idtienda = ? limit 1");
			pst.setInt(1, idPedidoTienda);
			pst.setInt(2, idTienda);
			final ResultSet rs = pst.executeQuery();
			existe = rs.next();
			rs.close();
			pst.close();
			con.close();
		} catch (final Exception e) {
			System.out.println("Campana15MinDAO.existeIncumplimiento: " + e.toString());
			try {
				con.close();
			} catch (final Exception e1) {
			}
		}
		return (existe);
	}

	public static void insertarIncumplimiento(int idPedidoTienda, int idTienda, String fechaHoraInicio,
			double valorBasePizza, double retencionAplicada, double valorADevolver) {
		final ConexionBaseDatos conexion = new ConexionBaseDatos();
		final Connection con = conexion.obtenerConexionBDContact();
		try {
			final PreparedStatement pst = con.prepareStatement("insert into campana_15min_incumplimiento"
					+ " (idpedidotienda, idtienda, fecha_hora_inicio, valor_base_pizza, retencion_aplicada, valor_a_devolver)"
					+ " values (?,?,?,?,?,?)");
			pst.setInt(1, idPedidoTienda);
			pst.setInt(2, idTienda);
			pst.setString(3, fechaHoraInicio);
			pst.setDouble(4, valorBasePizza);
			pst.setDouble(5, retencionAplicada);
			pst.setDouble(6, valorADevolver);
			pst.executeUpdate();
			pst.close();
			con.close();
		} catch (final Exception e) {
			System.out.println("Campana15MinDAO.insertarIncumplimiento: " + e.toString());
			try {
				con.close();
			} catch (final Exception e1) {
			}
		}
	}

	public static void marcarEvaluado(int idPedidoTienda, int idTienda, boolean cumplido) {
		final ConexionBaseDatos conexion = new ConexionBaseDatos();
		final Connection con = conexion.obtenerConexionBDContact();
		try {
			final PreparedStatement pst = con.prepareStatement(
					"update campana_15min_aplicado set estado_cumplido = ? where idpedidotienda = ? and idtienda = ?");
			pst.setString(1, cumplido ? "S" : "N");
			pst.setInt(2, idPedidoTienda);
			pst.setInt(3, idTienda);
			pst.executeUpdate();
			pst.close();
			con.close();
		} catch (final Exception e) {
			System.out.println("Campana15MinDAO.marcarEvaluado: " + e.toString());
			try {
				con.close();
			} catch (final Exception e1) {
			}
		}
	}
}
