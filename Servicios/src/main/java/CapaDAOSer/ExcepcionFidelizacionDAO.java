package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.Campana;
import ModeloSer.EmpresaTemporal;
import ModeloSer.ExcepcionFidelizacion;
import ModeloSer.TiendaCodigoPromocional;

public class ExcepcionFidelizacionDAO {
	
	public static ExcepcionFidelizacion retornarExcepcionesFidelizacion(String fecha)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		ExcepcionFidelizacion excepcion  = new ExcepcionFidelizacion(0, "", "", "", 0);
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select *  from excepcion_fidelizacion where fecha_excepcion = '" + fecha +"'";
			ResultSet rs = stm.executeQuery(consulta);
			int idExcepcion;
			String horaInicio;
			String horaFin;
			double valorPunto;
			while(rs.next()){
				idExcepcion = rs.getInt("idexcepcion");	
				horaInicio = rs.getString("hora_inicio");
				horaFin = rs.getString("hora_fin");
				valorPunto = rs.getDouble("valorpunto");
				excepcion = new ExcepcionFidelizacion(idExcepcion, fecha, horaInicio, horaFin, valorPunto);
				break;
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(excepcion);
	}

}
