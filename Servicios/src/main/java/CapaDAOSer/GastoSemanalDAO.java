package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.GastoConfiguracion;
import ModeloSer.GastoSemanal;

public class GastoSemanalDAO {
	
	public static void insertarGastoSemanal(GastoSemanal gastoSemanal)
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventarioLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into gasto_semanal (idtienda, idgasto_conf,fecha,valor_calculo,valor_gasto) values (" + gastoSemanal.getIdTienda() + " ," + gastoSemanal.getIdGastoConf() + ", '" + gastoSemanal.getFecha() + "' ," + gastoSemanal.getValorCalculo() + " , " + gastoSemanal.getValorGasto() + ")" ;
			stm.executeUpdate(insert);
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
		}
	}
	
		//Método creado para retornar el valor de variable desde sistema tienda
		public static double obtenerValorCalculo(String hostBD, String consulta, String origen)
		{
			String valor = "";
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = null;
			if(origen.contains("TIENDA"))
			{
				con1 = con.obtenerConexionBDTiendaRemota(hostBD);
			}else if(origen.contains("INVENTARIO"))
			{
				con1 = con.obtenerConexionBDInventarioLocal();
			}
			
			double valorCalculado = 0;
			try
			{
				Statement stm = con1.createStatement();
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					
					valorCalculado = rs.getDouble(1);
					
				}
				rs.close();
				stm.close();
				con1.close();
			}catch (Exception e)
			{
				System.out.println(e.toString());
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					
				}
			}
			return(valorCalculado);
		}
	

}
