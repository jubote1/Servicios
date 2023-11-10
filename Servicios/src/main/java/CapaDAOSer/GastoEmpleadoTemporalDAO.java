package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.EstadisticaProducto;
import ModeloSer.GastoConfiguracion;
import ModeloSer.GastoEmpleadoTemporal;
import ModeloSer.GastoSemanal;
import ModeloSer.VentaSemanalTienda;

public class GastoEmpleadoTemporalDAO {
	
	public static void insertarGastoEmpresaTemporal(GastoEmpleadoTemporal gastEmpTemp)
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventarioLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into gasto_empleado_temporal (idtienda,fecha,valor) values (" + gastEmpTemp.getIdTienda() + " ,'" + gastEmpTemp.getFecha() + "' , " + gastEmpTemp.getValor() + ")" ;
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
		public static double obtenerValorCalculo(String hostBD, String consulta)
		{
			String valor = "";
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(hostBD);
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
