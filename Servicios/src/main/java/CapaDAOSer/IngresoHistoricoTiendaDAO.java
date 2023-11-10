package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import capaModeloPOS.Ingreso;

public class IngresoHistoricoTiendaDAO {
	
	public static void insertarIngresoHistoricoTienda(int idTienda, Ingreso ingreso)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDDatamartLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into ingreso_historico_tienda (idingreso, idtienda, valoringreso, fecha, descripcion, tipoingreso, usuario) values(" + ingreso.getIdIngreso() + " ," + idTienda + ", " + ingreso.getValorIngreso() + " , '"+ ingreso.getFecha() + "', '" +  ingreso.getDescripcion()+"' , '" + ingreso.getTipoIngreso() + "' , '" + ingreso.getUsuario() + "')";
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
