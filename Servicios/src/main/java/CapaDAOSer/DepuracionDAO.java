package CapaDAOSer;

import java.sql.Connection;
import java.sql.Statement;

import org.apache.log4j.Logger;

import capaConexionPOS.ConexionBaseDatos;

public class DepuracionDAO {
	
	public static boolean depuracionPOS(String hostBD)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(hostBD);
		try
		{
			Statement stm = con1.createStatement();
			String update = "DELETE FROM cambios_estado_pedido WHERE idpedidotienda < (SELECT MIN(idpedidotienda) FROM pedido WHERE fechapedido = CAST(DATE_ADD(NOW(), INTERVAL -365 DAY) AS DATE))";
			stm.executeUpdate(update);
			update = "DELETE FROM consumo_inventario_pedido WHERE idpedido < (SELECT MIN(idpedidotienda) FROM pedido WHERE fechapedido = CAST(DATE_ADD(NOW(), INTERVAL -180 DAY) AS DATE))";
			stm.executeUpdate(update);
			update = "DELETE FROM detalle_pedido_impuesto WHERE idpedido < (SELECT MIN(idpedidotienda) FROM pedido WHERE fechapedido = CAST(DATE_ADD(NOW(), INTERVAL -365 DAY) AS DATE))";
			stm.executeUpdate(update);
			update = "DELETE FROM egreso WHERE fecha < CAST(DATE_ADD(NOW(), INTERVAL -365 DAY) AS DATE)";
			stm.executeUpdate(update);
			update = "DELETE FROM ingreso_inventario_detalle WHERE idingreso_inventario IN (SELECT idingreso_inventario FROM ingreso_inventario WHERE fecha_sistema < CAST(DATE_ADD(NOW(), INTERVAL -365 DAY) AS DATE))";
			stm.executeUpdate(update);
			update = "DELETE FROM item_inventario_historico WHERE fecha < CAST(DATE_ADD(NOW(), INTERVAL -365 DAY) AS DATE)";
			stm.executeUpdate(update);
			update = "DELETE FROM item_inventario_varianza WHERE idinventario_varianza IN (SELECT idinventario_varianza FROM inventario_varianza WHERE fecha_sistema < CAST(DATE_ADD(NOW(), INTERVAL -365 DAY) AS DATE))";
			stm.executeUpdate(update);
			update = "DELETE FROM log_notificacion_cliente WHERE CAST(fecha_hora AS DATE) < CAST(DATE_ADD(NOW(), INTERVAL -365 DAY) AS DATE)";
			stm.executeUpdate(update);
			update = "DELETE FROM pedido_contact_center WHERE idpedidotienda < (SELECT MIN(idpedidotienda) FROM pedido WHERE fechapedido = CAST(DATE_ADD(NOW(), INTERVAL -365 DAY) AS DATE))";
			stm.executeUpdate(update);
			stm.close();
			con1.close();
		}
		catch (Exception e){
			logger.error(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
			return(false);
		}
		return(true);
	}

}
