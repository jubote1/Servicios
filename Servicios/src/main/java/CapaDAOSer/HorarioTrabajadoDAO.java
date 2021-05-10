package CapaDAOSer;

import java.sql.Connection;
import java.sql.Statement;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.HorarioTrabajado;

public class HorarioTrabajadoDAO {
	
	public static void insertarHorarioTrabajado(HorarioTrabajado horario)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into horario_trabajado (id, fecha, dia, ingreso, salida, horas, idtienda) values (" + horario.getIdEmpleado() + "  , '" + horario.getFecha() + "' , '" + horario.getDia() + "' , '" + horario.getIngreso() + "' , '" + horario.getSalida() + "' , " + horario.getHoras() + " , " + horario.getIdTienda() + ")";
			stm.executeUpdate(insert);
			stm.close();
			con1.close();
		}catch (Exception e)
		{
			
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				
			}
		}
	}

}
