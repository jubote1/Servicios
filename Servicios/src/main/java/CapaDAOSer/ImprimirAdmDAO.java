package CapaDAOSer;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import ConexionSer.ConexionBaseDatos;
import ModeloSer.ImprimirAdm;
/**
 * Clase que se encarga de implementar todo lo relacionado con la base de datos de la entidad impuestos
 * @author JuanDavid
 *
 */
public class ImprimirAdmDAO {
	

	public static ArrayList<ImprimirAdm> pendientesImpresion()
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDLocal();
		ArrayList<ImprimirAdm> impresiones = new ArrayList();
		
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from imprimir_adm order by idimpresion asc";
			ResultSet rs = stm.executeQuery(consulta);
			int idImpresion;
			String imprimir;
			while(rs.next()){
				idImpresion = rs.getInt("idimpresion");
				imprimir = rs.getString("imprimir");
				ImprimirAdm colaImp = new ImprimirAdm(idImpresion,imprimir);
				impresiones.add(colaImp);
				
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(impresiones);
		
	}
	

	public static boolean borrarImpresion(int idImpresion, boolean auditoria)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDLocal();
		boolean respuesta = false;	
		try
		{
			Statement stm = con1.createStatement();
			String delete = "delete from imprimir_adm where idimpresion =" + idImpresion;
			stm.executeUpdate(delete);
			respuesta = true;
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println(e.toString());
			try
			{
				respuesta = false;
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(respuesta);
		
	}
	

	public static int insertarImpresion(String impresion, boolean auditoria)
	{
		int idImpresionIns = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into imprimir_adm (imprimir) values ('" + impresion + "')"; 
			stm.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = stm.getGeneratedKeys();
			if (rs.next()){
				idImpresionIns=rs.getInt(1);				
	        }
			stm.close();
			con1.close();
		}
		catch (Exception e){
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
			return(0);
		}
		return(idImpresionIns);
	}

}
