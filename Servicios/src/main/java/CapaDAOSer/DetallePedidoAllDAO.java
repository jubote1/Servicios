package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.DetallePedidoAll;

public class DetallePedidoAllDAO {
	
	/**
	 * Método para recuperar el detalle de los pedidos de las tiendas para posteriormente insertar
	 * @param idPedidoTienda
	 * @param hostBD
	 * @return
	 */
	public static ArrayList<DetallePedidoAll> recuperarDetallePorPedido(String fecha, String hostBD) {
	    ArrayList<DetallePedidoAll> lista = new ArrayList<>();
	    // Ajustamos la query para traer los detalles asociados a un pedido específico
	    String sql = "SELECT d.* FROM detalle_pedido d " +
                "INNER JOIN pedido p ON d.idpedidotienda = p.idpedidotienda AND d.idtienda = p.idtienda " +
                "WHERE p.fechapedido = ?";
	    
	    ConexionSer.ConexionBaseDatos con = new ConexionSer.ConexionBaseDatos();
	    try (Connection conn = con.obtenerConexionBDTiendaRemota(hostBD);
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        
	    	ps.setString(1, fecha);
	        
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                DetallePedidoAll dp = new DetallePedidoAll();
	                dp.setIdDetallePedido(rs.getInt("iddetalle_pedido"));
	                dp.setIdPedidoTienda(rs.getInt("idpedidotienda"));
	                dp.setIdTienda(rs.getInt("idtienda"));
	                dp.setIdProducto(rs.getInt("idproducto"));
	                dp.setCantidad(rs.getDouble("cantidad"));
	                dp.setValorUnitario(rs.getDouble("valorunitario"));
	                dp.setValorTotal(rs.getDouble("valortotal"));
	                dp.setValorImpuesto(rs.getDouble("valorimpuesto"));
	                dp.setObservacion(rs.getString("observacion"));
	                dp.setIdDetallePedidoMaster(rs.getInt("iddetalle_pedido_master"));
	                dp.setIdDetalleModificador((Integer) rs.getObject("iddetalle_modificador"));
	                dp.setDescargoInventario(rs.getString("descargo_inventario"));
	                dp.setIdMotivoAnulacion((Integer) rs.getObject("idmotivoanulacion"));
	                dp.setObsAnulacion(rs.getString("obs_anulacion"));
	                dp.setUsuarioAnulacion(rs.getString("usuario_anulacion"));
	                dp.setUsuarioAutAnulacion(rs.getString("usuario_aut_anulacion"));
	                
	                lista.add(dp);
	            }
	        }
	        conn.close();
	    } catch (SQLException e) {
	        System.err.println("Error recuperando detalle: " + e.getMessage());
	    }
	    return lista;
	}
	
	
	/**
	 * Método que encarga de insertar en la tabla centralizadora de datamart la información de detalle_pedido.
	 * @param lista
	 * @param connCentral
	 */
	public static void insertarLoteDetallePedido(ArrayList<DetallePedidoAll> lista) {
	    String sql = "INSERT INTO detalle_pedido (iddetalle_pedido, idpedidotienda, idtienda, idproducto, cantidad, " +
	                 "valorunitario, valortotal, valorimpuesto, observacion, iddetalle_pedido_master, " +
	                 "iddetalle_modificador, descargo_inventario, idmotivoanulacion, obs_anulacion, " +
	                 "usuario_anulacion, usuario_aut_anulacion) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	    ConexionSer.ConexionBaseDatos con = new ConexionSer.ConexionBaseDatos();
	    Connection conn = con.obtenerConexionBDDatamartLocal();
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	conn.setAutoCommit(false);
	        for (DetallePedidoAll dp : lista) {
	            ps.setInt(1, dp.getIdDetallePedido());
	            ps.setInt(2, dp.getIdPedidoTienda());
	            ps.setInt(3, dp.getIdTienda());
	            ps.setInt(4, dp.getIdProducto());
	            ps.setDouble(5, dp.getCantidad());
	            ps.setDouble(6, dp.getValorUnitario());
	            ps.setDouble(7, dp.getValorTotal());
	            ps.setDouble(8, dp.getValorImpuesto());
	            ps.setString(9, dp.getObservacion());
	            ps.setInt(10, dp.getIdDetallePedidoMaster());
	            
	            if (dp.getIdDetalleModificador() != null) ps.setInt(11, dp.getIdDetalleModificador()); else ps.setNull(11, Types.INTEGER);
	            
	            ps.setString(12, dp.getDescargoInventario());
	            
	            if (dp.getIdMotivoAnulacion() != null) ps.setInt(13, dp.getIdMotivoAnulacion()); else ps.setNull(13, Types.INTEGER);
	            
	            ps.setString(14, dp.getObsAnulacion());
	            ps.setString(15, dp.getUsuarioAnulacion());
	            ps.setString(16, dp.getUsuarioAutAnulacion());
	            
	            ps.addBatch();
	        }
	        ps.executeBatch();
	        conn.commit();
	        conn.close();
	    } catch (SQLException e) {
	        try { conn.rollback(); } catch (SQLException ex) {}
	        e.printStackTrace();
	    }
	}
	
	
	public static boolean existeDetallePedidosFecha(String fecha, int idTienda)
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDDatamartLocal();
		boolean resultado = false;
		try
		{
			Statement stm = con1.createStatement();
			String select = "select * from pedido a, detalle_pedido b where a.idpedidotienda = b.idpedidotienda and a.fechapedido = '" + fecha +"' and a.idtienda = " + idTienda ;
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
