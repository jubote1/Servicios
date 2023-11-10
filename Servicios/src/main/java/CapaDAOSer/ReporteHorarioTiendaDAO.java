package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.ReporteHorarioTienda;
import ModeloSer.Tienda;

public class ReporteHorarioTiendaDAO {
	
		public static ArrayList<ReporteHorarioTienda> obtenerReporteHorarioTiendas()
		{
			ArrayList<ReporteHorarioTienda> reportes = new ArrayList<>();
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDGeneral();
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "select * from reporte_horario_tienda order by idtienda asc";
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					int idTienda = rs.getInt("idtienda");
					String email = rs.getString("email");
					ReporteHorarioTienda reporteTienda = new ReporteHorarioTienda(idTienda, email);
					reportes.add(reporteTienda);
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e){
				System.out.println("falle consultando reportes Horario Tienda");
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("falle consultando tiendas");
				}
			}
			return(reportes);
			
		}
}
