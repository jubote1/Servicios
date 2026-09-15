package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import capaModeloCC.VentaIntegralCategoria;

/**
 * Lado "tienda" de Venta Integral: arma dinamicamente, segun el catalogo
 * parametrizado en el central (capaDAOCC.VentaIntegralCategoriaDAO), el mismo
 * SQL que antes se corria a mano contra la base local de cada tienda, y lo
 * ejecuta contra la conexion remota de esa tienda.
 *
 * No se usa conexionCC (esa es del central): la conexion a la base local de
 * cada tienda es la de este repo, ConexionSer.ConexionBaseDatos, la misma que
 * usa ServicioReplicaProgramaFidelizacion para leer cada tienda.
 */
public class VentaIntegralTiendaDAO {

	public static double consultarTotalTienda(String hostBD, VentaIntegralCategoria categoria,
			String semanaIniISO, String semanaFinISO) {
		String listaValores = listaSeparadaPorComas(categoria.getItemsTienda());
		if (listaValores.isEmpty()) {
			return (0);
		}
		String fechaInicial = semanaIniISO + " 00:00:00";
		String fechaFinal = semanaFinISO + " 23:59:59";
		boolean excluyeAnulados = categoria.isExcluyeAnuladosTienda();
		String filtroEstacion = categoria.getFiltroEstacionTienda();
		String consulta;
		if (VentaIntegralCategoria.MEDICION_TIENDA_SUMA_CANTIDAD.equals(categoria.getMedicionTienda())) {
			consulta = "select sum(d.cantidad) as total from pedido a, detalle_pedido d "
					+ "where a.fechapedido >= '" + fechaInicial + "' and a.fechapedido <= '" + fechaFinal + "' "
					+ "and a.idpedidotienda = d.idpedidotienda "
					+ (excluyeAnulados ? "and d.idmotivoanulacion is null " : "")
					+ "and d.idproducto in (" + listaValores + ") and a.estacion like '" + filtroEstacion + "'";
		} else {
			consulta = "select count(*) as total from pedido a, detalle_pedido b "
					+ "where a.fechapedido >= '" + fechaInicial + "' and a.fechapedido <= '" + fechaFinal + "' "
					+ "and a.idpedidotienda = b.idpedidotienda "
					+ (excluyeAnulados ? "and b.idmotivoanulacion is null " : "")
					+ "and a.estacion like '" + filtroEstacion + "' and b.idproducto in (" + listaValores + ")";
		}
		double total = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(hostBD);
		try {
			Statement stm = con1.createStatement();
			ResultSet rs = stm.executeQuery(consulta);
			if (rs.next()) {
				total = rs.getDouble("total");
			}
			rs.close();
			stm.close();
			con1.close();
		} catch (Exception e) {
			System.out.println("VentaIntegralTiendaDAO: fallo consultando " + hostBD + " categoria "
					+ categoria.getNombre() + ": " + e);
			try {
				con1.close();
			} catch (Exception e1) {
			}
		}
		return (total);
	}

	private static String listaSeparadaPorComas(ArrayList<Integer> valores) {
		if (valores == null || valores.isEmpty()) {
			return ("");
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < valores.size(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(valores.get(i));
		}
		return (sb.toString());
	}

}
