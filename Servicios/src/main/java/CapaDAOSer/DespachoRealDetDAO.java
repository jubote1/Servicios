package CapaDAOSer;

import java.sql.*;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.DespachoRealDet;

public class DespachoRealDetDAO {

    // Método para obtener un ArrayList con todos los registros de la tabla
    public static ArrayList<DespachoRealDet> obtenerDespachoRealDetFecha(String fecha, String hostBD) {
        ArrayList<DespachoRealDet> listaDetalles = new ArrayList<>();
        String sql = "SELECT d.* FROM despacho_real_det d INNER JOIN despacho_real p ON d.despacho_real_id = p.id  WHERE p.fecha = ?";
        ConexionSer.ConexionBaseDatos con = new ConexionSer.ConexionBaseDatos();
        
        try (Connection conexion = con.obtenerConexionBDTiendaRemota(hostBD);
             PreparedStatement ps = conexion.prepareStatement(sql);
             ) {
        	
        	ps.setString(1, fecha);
        	ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DespachoRealDet det = new DespachoRealDet();
                det.setId(rs.getInt("id"));
                det.setDespachoRealId(rs.getInt("despacho_real_id"));
                det.setIdPedido(rs.getInt("id_pedido"));

                // Manejo de campos nulos para tipos numéricos envolventes (Long / Integer)
                long planRutaId = rs.getLong("plan_ruta_det_id");
                det.setPlanRutaDetId(rs.wasNull() ? null : planRutaId);

                int ordenE = rs.getInt("orden_entrega");
                det.setOrdenEntrega(rs.wasNull() ? null : ordenE);

                int ordenP = rs.getInt("orden_planificada");
                det.setOrdenPlanificada(rs.wasNull() ? null : ordenP);

                Timestamp horaEntrega = rs.getTimestamp("hora_entrega");
                det.setHoraEntrega(horaEntrega != null ? new Date(horaEntrega.getTime()) : null);

                det.setIntento(rs.getInt("intento"));

                int estadoEntrega = rs.getInt("id_estado_entrega");
                det.setIdEstadoEntrega(rs.wasNull() ? null : estadoEntrega);

                Timestamp horaLlegada = rs.getTimestamp("hora_llegada_tienda");
                det.setHoraLlegadaTienda(horaLlegada != null ? new Date(horaLlegada.getTime()) : null);

                int incidenciaId = rs.getInt("incidencia_tipo_despacho_id");
                det.setIncidenciaTipoDespachoId(rs.wasNull() ? null : incidenciaId);

                det.setCreadoEn(rs.getTimestamp("creado_en"));

                listaDetalles.add(det);
            }
            conexion.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }

        return listaDetalles;
    }

    // Método para insertar una lista de registros por lote (Batch Processing)
    public static boolean insertarDespachoRealDetLote(ArrayList<DespachoRealDet> listaDetalles, int idTienda) {
        if (listaDetalles == null || listaDetalles.isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO despacho_real_det (despacho_real_id, id_pedido, plan_ruta_det_id, orden_entrega, " +
                     "orden_planificada, hora_entrega, intento, id_estado_entrega, hora_llegada_tienda, incidencia_tipo_despacho_id, idtienda, id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        ConexionSer.ConexionBaseDatos con = new ConexionSer.ConexionBaseDatos();
        Connection conn = con.obtenerConexionBDDatamartLocal();
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            // Desactivar el auto-commit para asegurar el rendimiento y atomicidad del lote
        	conn.setAutoCommit(false);

            for (DespachoRealDet det : listaDetalles) {
                ps.setInt(1, det.getDespachoRealId());
                ps.setInt(2, det.getIdPedido());

                // Manejo de parámetros que pueden ser nulos
                if (det.getPlanRutaDetId() != null) {
                    ps.setLong(3, det.getPlanRutaDetId());
                } else {
                    ps.setNull(3, Types.BIGINT);
                }

                if (det.getOrdenEntrega() != null) {
                    ps.setInt(4, det.getOrdenEntrega());
                } else {
                    ps.setNull(4, Types.INTEGER);
                }

                if (det.getOrdenPlanificada() != null) {
                    ps.setInt(5, det.getOrdenPlanificada());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }

                if (det.getHoraEntrega() != null) {
                    ps.setTimestamp(6, new java.sql.Timestamp(det.getHoraEntrega().getTime()));
                } else {
                    ps.setNull(6, Types.TIMESTAMP);
                }

                ps.setInt(7, det.getIntento());

                if (det.getIdEstadoEntrega() != null) {
                    ps.setInt(8, det.getIdEstadoEntrega());
                } else {
                    ps.setNull(8, Types.INTEGER);
                }

                if (det.getHoraLlegadaTienda() != null) {
                    ps.setTimestamp(9, new java.sql.Timestamp(det.getHoraLlegadaTienda().getTime()));
                } else {
                    ps.setNull(9, Types.TIMESTAMP);
                }

                if (det.getIncidenciaTipoDespachoId() != null) {
                    ps.setInt(10, det.getIncidenciaTipoDespachoId());
                } else {
                    ps.setNull(10, Types.INTEGER);
                }
                ps.setInt(11, idTienda);
                ps.setInt(12, det.getId());

                // Agrega la sentencia actual al lote
                ps.addBatch();
            }

            // Ejecuta el conjunto acumulado de consultas
            ps.executeBatch();
            
            // Confirma los cambios en la base de datos
            conn.commit();
            conn.close();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean existeDespachoRealDetFecha(String fecha, int idTienda)
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDDatamartLocal();
		boolean resultado = false;
		try
		{
			Statement stm = con1.createStatement();
			String select = "select * from despacho_real a, despacho_real_det b where a.id = b.despacho_real_id and a.fecha = '" + fecha +"' and a.idtienda = " + idTienda ;
			ResultSet rs = stm.executeQuery(select);
			resultado = false;
			while(rs.next())
			{
				resultado = true;
			}
			rs.close();
			stm.close();
			con1.close();
		}
		catch (Exception e){
			resultado = false;
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(resultado);
	}
}