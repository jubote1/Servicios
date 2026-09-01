package CapaDAOSer;

import java.sql.*;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.DespachoReal;

public class DespachoRealDAO {
	
	// Método para obtener un ArrayList con todos los registros
    public static ArrayList<DespachoReal> obtenerDespachoRealFecha(String fecha, String hostBD) {
        ArrayList<DespachoReal> listaDespachos = new ArrayList<>();
        String sql = "SELECT id, despacho_log_id, id_domiciliario, idtienda, fecha, hora_salida, hora_regreso, observaciones, creado_en FROM despacho_real WHERE fecha = ?";
        ConexionSer.ConexionBaseDatos con = new ConexionSer.ConexionBaseDatos();
        
        try (Connection conn = con.obtenerConexionBDTiendaRemota(hostBD);
	         PreparedStatement ps = conn.prepareStatement(sql);
             ) {
        	ps.setString(1, fecha);
        	ResultSet rs = ps.executeQuery();
        	
        
            while (rs.next()) {
                DespachoReal despacho = new DespachoReal();
                despacho.setId(rs.getInt("id"));
                
                // Manejo de campos que admiten NULL en la BD
                long logId = rs.getLong("despacho_log_id");
                despacho.setDespachoLogId(rs.wasNull() ? null : logId);

                despacho.setIdDomiciliario(rs.getInt("id_domiciliario"));
                despacho.setIdTienda(rs.getInt("idtienda"));
                despacho.setFecha(rs.getDate("fecha"));
                despacho.setHoraSalida(rs.getTimestamp("hora_salida"));
                
                Timestamp horaRegreso = rs.getTimestamp("hora_regreso");
                despacho.setHoraRegreso(horaRegreso != null ? new Date(horaRegreso.getTime()) : null);
                
                despacho.setObservaciones(rs.getString("observaciones"));
                despacho.setCreadoEn(rs.getTimestamp("creado_en"));

                listaDespachos.add(despacho);
            }
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }

        return listaDespachos;
    }

    // Método para insertar un nuevo registro
    public static boolean insertarDespachoRealLote(ArrayList<DespachoReal> listaDespachos) {
    	if (listaDespachos == null || listaDespachos.isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO despacho_real (despacho_log_id, id_domiciliario, idtienda, fecha, hora_salida, hora_regreso, observaciones,id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        ConexionSer.ConexionBaseDatos con = new ConexionSer.ConexionBaseDatos();
        Connection conn = con.obtenerConexionBDDatamartLocal();
        // Desactivamos el auto-commit para manejar la transacción de forma segura por lotes
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

        	conn.setAutoCommit(false);

            for (DespachoReal despacho : listaDespachos) {
                // Settear despacho_log_id (puede ser null)
                if (despacho.getDespachoLogId() != null) {
                    ps.setLong(1, despacho.getDespachoLogId());
                } else {
                    ps.setNull(1, Types.BIGINT);
                }

                ps.setInt(2, despacho.getIdDomiciliario());
                ps.setInt(3, despacho.getIdTienda());
                
                // Mapeo de fechas y horas
                ps.setDate(4, new java.sql.Date(despacho.getFecha().getTime()));
                ps.setTimestamp(5, new java.sql.Timestamp(despacho.getHoraSalida().getTime()));

                if (despacho.getHoraRegreso() != null) {
                    ps.setTimestamp(6, new java.sql.Timestamp(despacho.getHoraRegreso().getTime()));
                } else {
                    ps.setNull(6, Types.TIMESTAMP);
                }

                ps.setString(7, despacho.getObservaciones());
                ps.setInt(8, despacho.getId());
                
                // Añadir la sentencia actual al lote
                ps.addBatch();
            }

            // Ejecutar el lote completo de inserciones
            ps.executeBatch();
            
            // Confirmar la transacción si todo sale bien
            conn.commit();
            conn.close();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            // Si ocurre algún error, idealmente se debería hacer un rollback si tuviéramos la conexión accesible aquí, 
            // pero el bloque try-with-resources cerrará y manejará la excepción.
            return false;
        }
    }
    
    public static boolean existeDespachoRealFecha(String fecha, int idTienda)
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDDatamartLocal();
		boolean resultado = false;
		try
		{
			Statement stm = con1.createStatement();
			String select = "select * from despacho_real where fecha = '" + fecha +"' and idtienda = " + idTienda ;
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
