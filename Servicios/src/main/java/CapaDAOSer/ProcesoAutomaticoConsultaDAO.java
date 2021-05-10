package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import ConexionSer.ConexionBaseDatos;
import ModeloSer.ProcesoAutomatico;
import ModeloSer.ProcesoAutomaticoConsulta;



public class ProcesoAutomaticoConsultaDAO {
	
//	public static ArrayList<ProcesoAutomaticoConsulta> retornarProcesosAutomaticoConsulta(int idProceso)
//	{
//		ArrayList<ProcesoAutomaticoConsulta> procesoConsultas = new ArrayList();
//		ProcesoAutomaticoConsulta procesoConsultaTemp;
//		ConexionBaseDatos con = new ConexionBaseDatos();
//		Connection con1 = con.obtenerConexionBDGeneral();
//		try
//		{
//			int idProcesoConsulta;
//			String consultaEje;
//			String baseDatos;
//			String descripcion;
//			Statement stm = con1.createStatement();
//			String consulta = "select * from proceso_automatico ";
//			System.out.println(consulta);
//			ResultSet rs = stm.executeQuery(consulta);
//			while(rs.next()){
//				
//				consultaEje = 
//				procesoTemp = new ProcesoAutomatico(idProceso, nombreProceso, tipoProceso, tipoEjecucion,  horaInicio, horaFinal, ejeLun, ejeMar, ejeMie, ejeJue, ejeVie, ejeSab, ejeDom);
//				procesos.add(procesoTemp);
//			}
//			rs.close();
//			stm.close();
//			con1.close();
//		}catch (Exception e)
//		{
//			
//			try
//			{
//				con1.close();
//			}catch(Exception e1)
//			{
//				
//			}
//		}
//		return(procesos);
//	}
//	
}
