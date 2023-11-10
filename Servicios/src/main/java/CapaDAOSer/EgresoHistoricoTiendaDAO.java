package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import capaModeloPOS.Egreso;
import capaModeloPOS.Ingreso;

public class EgresoHistoricoTiendaDAO {
	
	public static void insertarEgresoHistoricoTienda(int idTienda, Egreso egreso)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDDatamartLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into egreso_historico_tienda (idegreso, idtienda, valoregreso, fecha, descripcion, usuario, idtipo_egreso) values(" + egreso.getIdEgreso() + " ," + idTienda + ", " + egreso.getValorEgreso() + " , '"+ egreso.getFecha() + "', '" +  egreso.getDescripcion()+"' , '" + egreso.getUsuario() + "' , " + egreso.getIdTipoEgreso() + ")";
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
