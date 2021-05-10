package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;

public class ClienteDAO {
	
	public static void actualizarTelCelularCliente(int idCliente, String telefonoCelular)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		try
		{
			Statement stm = con1.createStatement();
			String update = "update cliente set telefono_celular = '" + telefonoCelular + "' where idcliente = " + idCliente;
			stm.executeUpdate(update);
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
