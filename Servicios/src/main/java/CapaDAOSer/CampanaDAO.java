package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.Campana;
import ModeloSer.EmpresaTemporal;
import ModeloSer.TiendaCodigoPromocional;

public class CampanaDAO {
	
	public static Campana retornarCampana(int idCampana )
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		Campana campana  = new Campana(0,"","","","");
		
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select *  from campana where idcampana = " + idCampana;
			ResultSet rs = stm.executeQuery(consulta);
			String nombreCampana;
			String query;
			String plantilla;
			String mensajeTexto;
			while(rs.next()){
				nombreCampana = rs.getString("nombre");
				query = rs.getString("query");
				plantilla = rs.getString("plantilla");
				mensajeTexto = rs.getString("mensaje_texto");
				campana = new Campana(idCampana, nombreCampana, query, plantilla, mensajeTexto);
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
		return(campana);
	}

}
