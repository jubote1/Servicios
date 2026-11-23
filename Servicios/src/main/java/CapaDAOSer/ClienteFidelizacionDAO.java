package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.CasoSospechosoFidelizacion;
import ModeloSer.EmpresaTemporal;
import ModeloSer.FidelizacionRedencion;
import ModeloSer.TiendaCodigoPromocional;
import utilidadesSer.CorreoHtml;

public class ClienteFidelizacionDAO {
	
	/**
	 * Método que retorna el total de clientes que hay en el plan de fidelización
	 * @return
	 */
	public static int obtenerTotalClienteFidelizacion()
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		int totalClientes = 0;
		
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select count(*)  from cliente_fidelizacion ";
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				totalClientes = rs.getInt(1);
				break;
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			try
			{
				if (con1 != null && !con1.isClosed()) {
					con1.close();
				}
			}catch(Exception e1)
			{
			}
		}
		return(totalClientes);
	}
	
	
	
	/**
	 * Método que retorna el total de clientes que se han vinculado al plan de fidelización en un rango de fechas.
	 * @param fechaAnterior
	 * @param fechaPosterior
	 * @return
	 */
	public static int obtenerTotalClienteAfiliadosTiempo(String fechaAnterior, String fechaPosterior)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		int totalClientes = 0;
		
		try
		{
			String consulta = "select count(*)  from cliente_fidelizacion where fecha_vinculacion >= ? and fecha_vinculacion <= ?";
			PreparedStatement stmt = con1.prepareStatement(consulta);
			stmt.setString(1, fechaAnterior + " 00:00:00");
			stmt.setString(2, fechaPosterior + " 23:59:59");
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				totalClientes = rs.getInt(1);
				break;
			}
			rs.close();
			stmt.close();
			con1.close();
		}catch (Exception e){
			try
			{
				if (con1 != null && !con1.isClosed()) {
					con1.close();
				}
			}catch(Exception e1)
			{
			}
		}
		return(totalClientes);
	}
	
	
	/**
	 * Método que se encarga de retornar un acumulado de los puntos que fueron acumulados la semana inmediatamente anterior
	 * @param fechaAnterior
	 * @param fechaPosterior
	 * @return
	 */
	public static double obtenerTotalPuntosAcumuladosTiempo(String fechaAnterior, String fechaPosterior)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		double  totalPuntos = 0;
		
		try
		{
			String consulta = "select sum(puntos)  from fidelizacion_transaccion where fecha_transaccion >= ? and fecha_transaccion <= ?";
			PreparedStatement stmt = con1.prepareStatement(consulta);
			stmt.setString(1, fechaAnterior);
			stmt.setString(2, fechaPosterior);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				totalPuntos = rs.getDouble(1);
				break;
			}
			rs.close();
			stmt.close();
			con1.close();
		}catch (Exception e){
			try
			{
				if (con1 != null && !con1.isClosed()) {
					con1.close();
				}
			}catch(Exception e1)
			{
			}
		}
		return(totalPuntos);
	}
	
	
	/**
	 * Método que nos retorna un total de los puntos redimidos en la semana anterior	
	 * @param fechaAnterior
	 * @param fechaPosterior
	 * @return
	 */
	public static double obtenerTotalPuntosRedimidosTiempo(String fechaAnterior, String fechaPosterior)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		double  totalPuntos = 0;
		
		try
		{
			String consulta = "select sum(puntos_redimidos)  from fidelizacion_redencion where fecha_redencion >= ? and fecha_redencion <= ?";
			PreparedStatement stmt = con1.prepareStatement(consulta);
			stmt.setString(1, fechaAnterior + " 00:00:00");
			stmt.setString(2, fechaPosterior + " 23:59:59");
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				totalPuntos = rs.getDouble(1);
				break;
			}
			rs.close();
			stmt.close();
			con1.close();
		}catch (Exception e){
			try
			{
				if (con1 != null && !con1.isClosed()) {
					con1.close();
				}
			}catch(Exception e1)
			{
			}
		}
		return(totalPuntos);
	}
	
	/**
	 * Método que retorna las redenciones realizadas en un rango de fechas con tienda y empleado si aplica.
	 * @param fechaAnterior
	 * @param fechaPosterior
	 * @return
	 */
	public static ArrayList<FidelizacionRedencion> obtenerRedencionesFecha(String fechaAnterior, String fechaPosterior)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		ArrayList<FidelizacionRedencion> redenciones = new ArrayList<FidelizacionRedencion>();
		
		try
		{
			String consulta = "SELECT fr.*, IFNULL(t.nombre, '') AS nombre_tienda, "
					+ " IFNULL(emp.nombre_largo, '') AS empleado_nombre, "
					+ " IF(emp.id IS NOT NULL, 1, 0) AS es_empleado "
					+ "FROM fidelizacion_redencion fr "
					+ "LEFT JOIN tienda t ON fr.idtienda = t.idtienda "
					+ "LEFT JOIN general.empleado emp ON LOWER(TRIM(fr.correo)) = LOWER(TRIM(emp.email)) "
					+ "WHERE fr.fecha_redencion >= ? AND fr.fecha_redencion <= ? "
					+ "ORDER BY fr.fecha_redencion DESC";

			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				stmt = con1.prepareStatement(consulta);
				stmt.setString(1, fechaAnterior + " 00:00:00");
				stmt.setString(2, fechaPosterior + " 23:59:59");
				rs = stmt.executeQuery();
			} catch (Exception eFallback) {
				// Fallback si general.empleado no está en el mismo host o catálogo
				consulta = "SELECT fr.*, IFNULL(t.nombre, '') AS nombre_tienda "
						+ "FROM fidelizacion_redencion fr "
						+ "LEFT JOIN tienda t ON fr.idtienda = t.idtienda "
						+ "WHERE fr.fecha_redencion >= ? AND fr.fecha_redencion <= ? "
						+ "ORDER BY fr.fecha_redencion DESC";
				stmt = con1.prepareStatement(consulta);
				stmt.setString(1, fechaAnterior + " 00:00:00");
				stmt.setString(2, fechaPosterior + " 23:59:59");
				rs = stmt.executeQuery();
			}

			while(rs.next()){
				String correo = rs.getString("correo");
				String fechaRedencion = rs.getString("fecha_redencion");
				double puntosRedimidos = rs.getDouble("puntos_redimidos");
				String nombreTienda = "";
				try {
					nombreTienda = rs.getString("nombre_tienda");
				} catch (Exception ignored) {}
				String usuario = "";
				try {
					usuario = rs.getString("usuario");
				} catch (Exception ignored) {}
				String estado = "";
				try {
					estado = rs.getString("estado");
				} catch (Exception ignored) {}

				FidelizacionRedencion redencionTemp = new FidelizacionRedencion(correo, fechaRedencion, puntosRedimidos, nombreTienda, usuario, estado);
				try {
					redencionTemp.setIdpedidotienda(rs.getInt("idpedidotienda"));
				} catch (Exception ignored) {}
				try {
					if (rs.getInt("es_empleado") == 1) {
						redencionTemp.setEsEmpleado(true);
						redencionTemp.setEmpleadoNombre(rs.getString("empleado_nombre"));
					}
				} catch (Exception ignored) {}

				redenciones.add(redencionTemp);
			}
			rs.close();
			stmt.close();
			con1.close();
		}catch (Exception e){
			System.out.println("Error en obtenerRedencionesFecha: " + e.toString());
			try
			{
				if (con1 != null && !con1.isClosed()) {
					con1.close();
				}
			}catch(Exception e1)
			{
			}
		}
		return(redenciones);
	}

	/**
	 * Consulta y analiza los casos con indicios de mal manejo, uso indebido o fraude
	 * en el programa de fidelización durante el rango de fechas indicado.
	 * 
	 * Criterios de sospecha auditados:
	 * 1. Cuenta asociada a un empleado de nómina/tienda (general.empleado).
	 * 2. Auto-acumulación directa en caja (el cajero acumula puntos a su propio correo).
	 * 3. Más de 1 acumulación de puntos en un mismo día calendario (alerta de fraccionamiento).
	 * 4. Más de 3 acumulaciones de puntos en la semana.
	 * 5. Redención de puntos efectuada en la semana (agravante).
	 * 
	 * @param fechaAnterior Fecha inicial (YYYY-MM-DD)
	 * @param fechaPosterior Fecha final (YYYY-MM-DD)
	 * @param limite Máximo de casos sospechosos a retornar (Top N)
	 * @return Lista de casos sospechosos ordenados por severidad de riesgo
	 */
	public static ArrayList<CasoSospechosoFidelizacion> obtenerCasosSospechosos(String fechaAnterior, String fechaPosterior, int limite) {
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		ArrayList<CasoSospechosoFidelizacion> lista = new ArrayList<CasoSospechosoFidelizacion>();

		try {
			String sql = "SELECT "
					+ " ft.correo, "
					+ " IFNULL(emp.nombre_largo, '') AS empleado_nombre, "
					+ " IFNULL(emp.nombre, '') AS empleado_usuario, "
					+ " IF(emp.id IS NOT NULL, 1, 0) AS es_empleado, "
					+ " COUNT(*) AS total_acumulaciones, "
					+ " COUNT(DISTINCT ft.fecha_transaccion) AS dias_activos, "
					+ " MAX(t_dia.max_dia) AS max_dia, "
					+ " SUM(ft.puntos) AS total_puntos, "
					+ " SUM(ft.valor_neto) AS total_venta, "
					+ " GROUP_CONCAT(DISTINCT t.nombre ORDER BY t.nombre SEPARATOR ', ') AS tiendas, "
					+ " GROUP_CONCAT(DISTINCT ft.usuario ORDER BY ft.usuario SEPARATOR ', ') AS cajeros, "
					+ " GROUP_CONCAT(DISTINCT ft.idpedidotienda ORDER BY ft.idpedidotienda SEPARATOR ', ') AS pedidos, "
					+ " SUM(IF(ft.usuario = emp.nombre OR ft.usuario = CAST(emp.id AS CHAR), 1, 0)) AS auto_cajero_count, "
					+ " IFNULL(red.puntos_redimidos_semana, 0) AS puntos_redimidos_semana "
					+ "FROM fidelizacion_transaccion ft "
					+ "LEFT JOIN tienda t ON ft.idtienda = t.idtienda "
					+ "LEFT JOIN ( "
					+ "    SELECT correo, fecha_transaccion, COUNT(*) AS max_dia "
					+ "    FROM fidelizacion_transaccion "
					+ "    WHERE fecha_transaccion >= ? AND fecha_transaccion <= ? "
					+ "    GROUP BY correo, fecha_transaccion "
					+ ") t_dia ON ft.correo = t_dia.correo AND ft.fecha_transaccion = t_dia.fecha_transaccion "
					+ "LEFT JOIN ( "
					+ "    SELECT correo, SUM(puntos_redimidos) AS puntos_redimidos_semana "
					+ "    FROM fidelizacion_redencion "
					+ "    WHERE fecha_redencion >= ? AND fecha_redencion <= ? "
					+ "    GROUP BY correo "
					+ ") red ON LOWER(TRIM(ft.correo)) = LOWER(TRIM(red.correo)) "
					+ "LEFT JOIN general.empleado emp ON LOWER(TRIM(ft.correo)) = LOWER(TRIM(emp.email)) "
					+ "WHERE ft.fecha_transaccion >= ? AND ft.fecha_transaccion <= ? "
					+ "GROUP BY ft.correo, emp.nombre_largo, emp.nombre, emp.id, red.puntos_redimidos_semana "
					+ "HAVING es_empleado = 1 OR max_dia > 1 OR total_acumulaciones > 3 "
					+ "ORDER BY "
					+ "    (IF(emp.id IS NOT NULL, 100, 0) "
					+ "     + IF(SUM(IF(ft.usuario = emp.nombre OR ft.usuario = CAST(emp.id AS CHAR), 1, 0)) > 0, 50, 0) "
					+ "     + IF(MAX(t_dia.max_dia) > 1, 40, 0) "
					+ "     + IF(COUNT(*) > 3, 30, 0) "
					+ "     + IF(IFNULL(red.puntos_redimidos_semana, 0) > 0, 40, 0) "
					+ "     + COUNT(*) * 5) DESC "
					+ "LIMIT ?";

			PreparedStatement pst = null;
			ResultSet rs = null;

			try {
				pst = con1.prepareStatement(sql);
				pst.setString(1, fechaAnterior);
				pst.setString(2, fechaPosterior);
				pst.setString(3, fechaAnterior + " 00:00:00");
				pst.setString(4, fechaPosterior + " 23:59:59");
				pst.setString(5, fechaAnterior);
				pst.setString(6, fechaPosterior);
				pst.setInt(7, limite);
				rs = pst.executeQuery();
			} catch (Exception eFallback) {
				// Fallback si general.empleado no está accesible en la conexión
				String sqlFallback = "SELECT "
						+ " ft.correo, "
						+ " '' AS empleado_nombre, "
						+ " '' AS empleado_usuario, "
						+ " 0 AS es_empleado, "
						+ " COUNT(*) AS total_acumulaciones, "
						+ " COUNT(DISTINCT ft.fecha_transaccion) AS dias_activos, "
						+ " MAX(t_dia.max_dia) AS max_dia, "
						+ " SUM(ft.puntos) AS total_puntos, "
						+ " SUM(ft.valor_neto) AS total_venta, "
						+ " GROUP_CONCAT(DISTINCT t.nombre ORDER BY t.nombre SEPARATOR ', ') AS tiendas, "
						+ " GROUP_CONCAT(DISTINCT ft.usuario ORDER BY ft.usuario SEPARATOR ', ') AS cajeros, "
						+ " GROUP_CONCAT(DISTINCT ft.idpedidotienda ORDER BY ft.idpedidotienda SEPARATOR ', ') AS pedidos, "
						+ " 0 AS auto_cajero_count, "
						+ " IFNULL(red.puntos_redimidos_semana, 0) AS puntos_redimidos_semana "
						+ "FROM fidelizacion_transaccion ft "
						+ "LEFT JOIN tienda t ON ft.idtienda = t.idtienda "
						+ "LEFT JOIN ( "
						+ "    SELECT correo, fecha_transaccion, COUNT(*) AS max_dia "
						+ "    FROM fidelizacion_transaccion "
						+ "    WHERE fecha_transaccion >= ? AND fecha_transaccion <= ? "
						+ "    GROUP BY correo, fecha_transaccion "
						+ ") t_dia ON ft.correo = t_dia.correo AND ft.fecha_transaccion = t_dia.fecha_transaccion "
						+ "LEFT JOIN ( "
						+ "    SELECT correo, SUM(puntos_redimidos) AS puntos_redimidos_semana "
						+ "    FROM fidelizacion_redencion "
						+ "    WHERE fecha_redencion >= ? AND fecha_redencion <= ? "
						+ "    GROUP BY correo "
						+ ") red ON LOWER(TRIM(ft.correo)) = LOWER(TRIM(red.correo)) "
						+ "WHERE ft.fecha_transaccion >= ? AND ft.fecha_transaccion <= ? "
						+ "GROUP BY ft.correo, red.puntos_redimidos_semana "
						+ "HAVING max_dia > 1 OR total_acumulaciones > 3 "
						+ "ORDER BY (IF(MAX(t_dia.max_dia) > 1, 40, 0) + IF(COUNT(*) > 3, 30, 0) + COUNT(*) * 5) DESC "
						+ "LIMIT ?";
				pst = con1.prepareStatement(sqlFallback);
				pst.setString(1, fechaAnterior);
				pst.setString(2, fechaPosterior);
				pst.setString(3, fechaAnterior + " 00:00:00");
				pst.setString(4, fechaPosterior + " 23:59:59");
				pst.setString(5, fechaAnterior);
				pst.setString(6, fechaPosterior);
				pst.setInt(7, limite);
				rs = pst.executeQuery();
			}

			while (rs.next()) {
				CasoSospechosoFidelizacion caso = new CasoSospechosoFidelizacion();
				caso.setCorreo(rs.getString("correo"));
				caso.setEmpleadoNombre(rs.getString("empleado_nombre"));
				caso.setEmpleadoUsuario(rs.getString("empleado_usuario"));
				caso.setEsEmpleado(rs.getInt("es_empleado") == 1);
				caso.setTotalAcumulaciones(rs.getInt("total_acumulaciones"));
				caso.setDiasActivos(rs.getInt("dias_activos"));
				caso.setMaxDia(rs.getInt("max_dia"));
				caso.setTotalPuntos(rs.getDouble("total_puntos"));
				caso.setTotalVenta(rs.getDouble("total_venta"));
				caso.setTiendas(rs.getString("tiendas"));
				caso.setCajeros(rs.getString("cajeros"));
				try {
					caso.setPedidos(rs.getString("pedidos"));
				} catch (Exception ignored) {}
				caso.setAutoCajeroCount(rs.getInt("auto_cajero_count"));
				caso.setPuntosRedimidosSemana(rs.getDouble("puntos_redimidos_semana"));

				// Construir motivos de sospecha
				if (caso.isEsEmpleado()) {
					caso.agregarMotivo("Empleado activo en nómina (" + caso.getEmpleadoNombre() + ")");
				}
				if (caso.getAutoCajeroCount() > 0) {
					caso.agregarMotivo("Auto-acumulación en caja propia (" + caso.getAutoCajeroCount() + " pedido" + (caso.getAutoCajeroCount() > 1 ? "s" : "") + ")");
				}
				if (caso.getMaxDia() > 1) {
					caso.agregarMotivo(caso.getMaxDia() + " acumulaciones en 1 solo día");
				}
				if (caso.getTotalAcumulaciones() > 3) {
					caso.agregarMotivo(caso.getTotalAcumulaciones() + " acumulaciones en la semana (>3 límite)");
				}
				if (caso.getPuntosRedimidosSemana() > 0) {
					caso.agregarMotivo("Redimió " + CorreoHtml.decimales(caso.getPuntosRedimidosSemana()) + " pts en la semana");
				}

				// Determinar nivel de riesgo
				if (caso.isEsEmpleado() && (caso.getAutoCajeroCount() > 0 || caso.getMaxDia() > 1 || caso.getTotalAcumulaciones() > 3 || caso.getPuntosRedimidosSemana() > 0)) {
					caso.setNivelRiesgo("CRÍTICO");
				} else if (caso.isEsEmpleado() || caso.getMaxDia() >= 3 || caso.getTotalAcumulaciones() >= 5) {
					caso.setNivelRiesgo("ALTO");
				} else {
					caso.setNivelRiesgo("MEDIO");
				}

				lista.add(caso);
			}

			rs.close();
			pst.close();
			con1.close();
		} catch (Exception e) {
			System.out.println("Error en obtenerCasosSospechosos: " + e.toString());
			try {
				if (con1 != null && !con1.isClosed()) {
					con1.close();
				}
			} catch (Exception e1) {
			}
		}
		return lista;
	}

}
