package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.PedidoAnulado;
import capaModeloPOS.Ingreso;

/**
 * Método que inserta un pedido anulado en la tabla centralizada para este fin
 */
public class PedidoAnuladoDAO {
	
	public static void insertarPedidoAnulado(PedidoAnulado pedIns)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into pedido_anulado (idpedidotienda, idtienda) values(" + pedIns.getIdPedidoTienda() + " ," + pedIns.getIdTienda()  + ")";
			stm.executeUpdate(insert);
			stm.close();
			con1.close();
		}catch (Exception e){
			e.toString();
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
	}

}
