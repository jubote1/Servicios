package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import ConexionSer.ConexionBaseDatos;
import ModeloSer.ProcesoAutomatico;



public class ProcesoAutomaticoDAO {
	
	public static ArrayList<ProcesoAutomatico> retornarProcesosAutomaticos()
	{
		ArrayList<ProcesoAutomatico> procesos = new ArrayList();
		ProcesoAutomatico procesoTemp;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDGeneral();
		try
		{
			 int idProceso;
			 String nombreProceso;
			 String tipoProceso;
			 String tipoEjecucion;
			 String horaInicio;
			 String horaFinal;
			 String ejeLun;
			 String ejeMar;
			 String ejeMie;
			 String ejeJue;
			 String ejeVie;
			 String ejeSab;
			 String ejeDom;
			Statement stm = con1.createStatement();
			String consulta = "select * from proceso_automatico ";
			System.out.println(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				
				idProceso = rs.getInt("idproceso");
				nombreProceso = rs.getString("nombre_proceso");
				tipoProceso = rs.getString("tipo_proceso");
				tipoEjecucion = rs.getString("tipo_ejecucion");
				horaInicio = rs.getString("hora_inicio");
				horaFinal = rs.getString("hora_final");
				ejeLun = rs.getString("ejelun");
				ejeMar = rs.getString("ejemar");
				ejeMie = rs.getString("ejemie");
				ejeJue = rs.getString("ejejue");
				ejeVie = rs.getString("ejevie");
				ejeSab = rs.getString("ejesab");
				ejeDom = rs.getString("ejedom");
				procesoTemp = new ProcesoAutomatico(idProceso, nombreProceso, tipoProceso, tipoEjecucion,  horaInicio, horaFinal, ejeLun, ejeMar, ejeMie, ejeJue, ejeVie, ejeSab, ejeDom);
				procesos.add(procesoTemp);
			}
			rs.close();
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
		return(procesos);
	}
	
}
