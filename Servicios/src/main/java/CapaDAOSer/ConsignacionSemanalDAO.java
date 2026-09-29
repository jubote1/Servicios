package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.ConsignacionSemanal;

/**
 * Las consignaciones que una tienda registro en un rango de fechas, leidas de su
 * base local -tabla consignacion, la misma que llena VentPedAdmConsignacion en el
 * POS-.
 *
 * Devuelve null cuando la tienda no respondio (computador apagado o sin red), igual
 * que ConsignacionBoldDAO: un null se reporta como "sin respuesta", un cero diria
 * que la tienda no consigno nada esa semana y no es lo mismo -alguien podria dar por
 * cerrada la semana con una tienda entera faltando.
 */
public class ConsignacionSemanalDAO {

	/**
	 * @param fechaInicial yyyy-MM-dd (fecha_sistema, inclusive)
	 * @param fechaFinal   yyyy-MM-dd (fecha_sistema, inclusive)
	 * @param hostBD       tienda.hosbd de la tienda
	 * @return las consignaciones ordenadas por fecha y hora, o null si la tienda no respondio
	 */
	public static ArrayList<ConsignacionSemanal> obtenerConsignacionesRango(final String fechaInicial,
			final String fechaFinal, final String hostBD) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection con1 = con.obtenerConexionBDTiendaRemota(hostBD);
		if (con1 == null) {
			System.out.println("ConsignacionSemanalDAO: no se pudo conectar a la tienda " + hostBD);
			return null;
		}
		final ArrayList<ConsignacionSemanal> consignaciones = new ArrayList<ConsignacionSemanal>();
		try {
			final Statement stm = con1.createStatement();
			final String consulta = "select idconsignacion, fecha_sistema, descripcion, valor_consignacion,"
					+ " hora_consignacion, fecha_real, usuario, usuario_testigo from consignacion"
					+ " where fecha_sistema >= '" + fechaInicial + "' and fecha_sistema <= '" + fechaFinal + "'"
					+ " order by fecha_sistema, hora_consignacion";
			final ResultSet rs = stm.executeQuery(consulta);
			while (rs.next()) {
				final ConsignacionSemanal fila = new ConsignacionSemanal();
				fila.setIdConsignacion(rs.getInt("idconsignacion"));
				fila.setFechaSistema(rs.getString("fecha_sistema"));
				fila.setDescripcion(rs.getString("descripcion"));
				fila.setValorConsignacion(rs.getDouble("valor_consignacion"));
				fila.setHoraConsignacion(rs.getString("hora_consignacion"));
				fila.setFechaReal(rs.getString("fecha_real"));
				fila.setUsuario(rs.getString("usuario"));
				fila.setUsuarioTestigo(rs.getString("usuario_testigo"));
				consignaciones.add(fila);
			}
			rs.close();
			stm.close();
			con1.close();
		} catch (final Exception e) {
			System.out.println("ConsignacionSemanalDAO en " + hostBD + ": " + e.toString());
			try {
				con1.close();
			} catch (final Exception e1) {
			}
			return null;
		}
		return consignaciones;
	}

}
