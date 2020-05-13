package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import ConexionSer.ConexionBaseDatos;
import ModeloSer.EmpresaTemporal;

public class EmpresaTemporalDAO {
	
	/**
	 * Método que se encarga de retornar las empresas temporales existentes en el sistema general
	 * @param bdGeneral
	 * @param auditoria
	 * @return
	 */
	public static ArrayList<EmpresaTemporal> retornarEmpresasTemporales()
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneralLocal();
		ArrayList <EmpresaTemporal> empresasTemporales = new ArrayList();
		
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select *  from empresa_temporal";
			ResultSet rs = stm.executeQuery(consulta);
			EmpresaTemporal empTemp;
			int idEmpresa;
			String nombreEmpresa;
			double valorHoraNormal, valorHoraDominical;
			while(rs.next()){
				idEmpresa = rs.getInt("idempresa");
				nombreEmpresa = rs.getString("nombre_empresa");
				valorHoraNormal = rs.getDouble("valor_hora_normal");
				valorHoraDominical = rs.getDouble("valor_hora_dominical");
				empTemp = new EmpresaTemporal(idEmpresa, nombreEmpresa, valorHoraNormal, valorHoraDominical);
				empresasTemporales.add(empTemp);
				
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
		return(empresasTemporales);
	}

}
