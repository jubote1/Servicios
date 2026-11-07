package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import ConexionSer.ConexionBaseDatos;

/**
 * CUANTOS DOMICILIOS LLEVO UN DOMICILIARIO, para los procesos por lote.
 *
 * Es la MISMA regla de capaDAOPOS.ConteoDomiciliosDAO (POS) y de
 * capaDAOCC.ConteoDomiciliosDAO (central), sobre las mismas tablas de la
 * tienda. Son tres clases y no una porque cada proyecto tiene su propia clase
 * de conexion; la regla es una sola y si cambia, cambia en las tres.
 *
 * POR QUE EXISTE
 *
 * El correo semanal de temporales contaba asi:
 *
 *     select count(*) from pedido
 *      where fechainsercion between <hora de ingreso> and <hora de salida>
 *        and iddomiciliario = ?
 *
 * Tres problemas con eso, todos medidos del 31-oct al 6-nov de 2026:
 *
 *   1. Cortaba por fechainsercion DENTRO de la jornada. Un pedido tomado diez
 *      minutos antes de que el domiciliario marcara ingreso, y que el si
 *      llevo, no se le pagaba.
 *   2. Si el pedido se reasigno, el que lo saco perdia el credito: 55 pedidos
 *      en la semana en las once tiendas, 17 solo en Itagui.
 *   3. Contaba los pedidos del usuario "Caja", que no es un domiciliario (101
 *      pedidos en Manrique en la semana).
 *
 * LA REGLA, decidida por Juan David el 2026-11-07
 *
 *     domicilios = pedidos DISTINTOS que el domiciliario saco en algun despacho
 *                + los pedidos de domicilio asignados a el que no pasaron por
 *                  ningun despacho
 *
 *   - Pedido reasignado: SE LE CUENTA A LOS DOS, porque los dos hicieron el
 *     viaje. "Mejor que se pague uno de mas a que se pague uno de menos".
 *   - El mismo domiciliario que lo saca dos veces: cuenta UNA.
 *   - Pedido anulado que alcanzo a salir: CUENTA.
 *   - "Caja" y cualquiera que no sea domiciliario: FUERA.
 *
 * OJO CON EL PERIODO. Aqui se cuenta por DIA (fecha del despacho y
 * fechapedido), no por la ventana de horas de la jornada. Un domiciliario que
 * entro dos veces el mismo dia se cuenta una sola vez por dia: quien liquide
 * por turnos debe sumar por dia, no pedir dos veces el mismo dia.
 */
public class ConteoDomiciliosDAO {

	private static final String SQL_UNO =
			"select count(*) from ("
			+ "  select d.id_pedido"
			+ "    from despacho_real r"
			+ "    join despacho_real_det d on d.despacho_real_id = r.id"
			+ "   where r.fecha >= ? and r.fecha <= ? and r.id_domiciliario = ?"
			+ "  union"
			+ "  select p.idpedidotienda"
			+ "    from pedido p"
			+ "    join tipo_pedido tp on tp.idtipopedido = p.idtipopedido"
			+ "   where p.fechapedido >= ? and p.fechapedido <= ? and p.iddomiciliario = ?"
			+ "     and tp.esdomicilio = 1"
			+ "     and not exists (select 1 from despacho_real r2"
			+ "                       join despacho_real_det d2 on d2.despacho_real_id = r2.id"
			+ "                      where d2.id_pedido = p.idpedidotienda)"
			+ ") x";

	/**
	 * Cuantos domicilios llevo un domiciliario entre dos fechas, en la tienda
	 * indicada.
	 *
	 * @param fechaInicial yyyy-MM-dd, inclusive. SOLO LA FECHA: si llega con
	 *                     hora se recorta, porque el conteo es por dia
	 * @param fechaFinal   yyyy-MM-dd, inclusive
	 * @param hostRemoto   host de la tienda
	 * @return el conteo, o 0 si algo falla. Nunca lanza.
	 */
	public static int contar(final String fechaInicial, final String fechaFinal, final int idDomiciliario,
			final String hostRemoto)
	{
		final String desde = soloFecha(fechaInicial);
		final String hasta = soloFecha(fechaFinal);
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		int cantidad = 0;
		try
		{
			cn = con.obtenerConexionBDTiendaRemota(hostRemoto);
			final PreparedStatement ps = cn.prepareStatement(SQL_UNO);
			ps.setString(1, desde);
			ps.setString(2, hasta);
			ps.setInt(3, idDomiciliario);
			ps.setString(4, desde);
			ps.setString(5, hasta);
			ps.setInt(6, idDomiciliario);
			final ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				cantidad = rs.getInt(1);
			}
			rs.close();
			ps.close();
		}catch(final Exception e)
		{
			System.out.println("ConteoDomiciliosDAO.contar: falle contando los domicilios del domiciliario "
					+ idDomiciliario + " entre " + desde + " y " + hasta + " en " + hostRemoto + ": " + e);
		}finally
		{
			cerrar(cn);
		}
		return(cantidad);
	}

	/**
	 * Todos los domiciliarios de la tienda de una sola pasada, para no abrir una
	 * conexion por persona cuando hay que armar una tabla de once tiendas.
	 *
	 * @return idDomiciliario -> cantidad. Vacio si algo falla.
	 */
	public static Map<Integer, Integer> contarPorDomiciliario(final String fechaInicial, final String fechaFinal,
			final String hostRemoto)
	{
		final Map<Integer, Integer> conteo = new HashMap<Integer, Integer>();
		final String sql =
				"select x.id, count(*) as domicilios from ("
				+ "  select r.id_domiciliario as id, d.id_pedido"
				+ "    from despacho_real r"
				+ "    join despacho_real_det d on d.despacho_real_id = r.id"
				+ "   where r.fecha >= ? and r.fecha <= ?"
				+ "  union"
				+ "  select p.iddomiciliario as id, p.idpedidotienda"
				+ "    from pedido p"
				+ "    join tipo_pedido tp on tp.idtipopedido = p.idtipopedido"
				+ "   where p.fechapedido >= ? and p.fechapedido <= ?"
				+ "     and tp.esdomicilio = 1 and ifnull(p.iddomiciliario,0) > 0"
				+ "     and not exists (select 1 from despacho_real r2"
				+ "                       join despacho_real_det d2 on d2.despacho_real_id = r2.id"
				+ "                      where d2.id_pedido = p.idpedidotienda)"
				+ ") x"
				+ " join usuario u on u.id = x.id"
				+ " join tipo_empleado t on t.idtipoempleado = u.idtipoempleado and t.es_domiciliario = 1"
				+ " group by x.id";
		final String desde = soloFecha(fechaInicial);
		final String hasta = soloFecha(fechaFinal);
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try
		{
			cn = con.obtenerConexionBDTiendaRemota(hostRemoto);
			final PreparedStatement ps = cn.prepareStatement(sql);
			ps.setString(1, desde);
			ps.setString(2, hasta);
			ps.setString(3, desde);
			ps.setString(4, hasta);
			final ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				conteo.put(Integer.valueOf(rs.getInt(1)), Integer.valueOf(rs.getInt(2)));
			}
			rs.close();
			ps.close();
		}catch(final Exception e)
		{
			System.out.println("ConteoDomiciliosDAO.contarPorDomiciliario: falle en " + hostRemoto
					+ " entre " + desde + " y " + hasta + ": " + e);
		}finally
		{
			cerrar(cn);
		}
		return(conteo);
	}

	/**
	 * Recorta la hora si viene pegada a la fecha.
	 *
	 * El correo de temporales llamaba con "yyyy-MM-dd HH:mm" porque contaba por
	 * ventana de jornada. Ahora el conteo es por dia, y comparar un
	 * "yyyy-MM-dd HH:mm" contra una columna date no falla pero tampoco hace lo
	 * que parece.
	 */
	private static String soloFecha(final String fecha)
	{
		if(fecha == null)
		{
			return("");
		}
		final String limpia = fecha.trim();
		return(limpia.length() > 10 ? limpia.substring(0, 10) : limpia);
	}

	private static void cerrar(final Connection cn)
	{
		try
		{
			if(cn != null)
			{
				cn.close();
			}
		}catch(final Exception e)
		{
			System.out.println("ConteoDomiciliosDAO: no cerro la conexion, " + e);
		}
	}
}
