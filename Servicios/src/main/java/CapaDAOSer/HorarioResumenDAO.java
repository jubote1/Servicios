package CapaDAOSer;

import java.sql.Connection;
import java.sql.Statement;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.HorarioResumen;
import ModeloSer.HorarioTrabajado;

public class HorarioResumenDAO {
	
	public static void insertarHorarioResumen(HorarioResumen horario)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into horario_resumen (id, total_horas, horas_extras_ord, horas_extras_domi, horas_festiva, horas_recargo_nocturno, fecha_inicial, fecha_final) values (" + horario.getIdEmpleado() + "  , " + horario.getTotalHoras() + " , " + horario.getHorasExtrasOrd() + " , " + horario.getHorasExtrasDomi() + " , " + horario.getHorasFestiva() + " , " + horario.getHorasRecargoNocturno() + " , '" + horario.getFechaInicial() + "' , '" + horario.getFechaFinal() + "')";
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
