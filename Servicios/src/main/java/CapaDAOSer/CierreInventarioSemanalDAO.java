package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.CierreInventarioSemanal;
import ModeloSer.EstadisticaProducto;
import ModeloSer.GastoConfiguracion;
import ModeloSer.GastoSemanal;
import ModeloSer.VentaSemanalTienda;

public class CierreInventarioSemanalDAO {
	
	public static void insertarCierreInventarioSemanal(CierreInventarioSemanal cierreInv)
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDDatamartLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into cierre_inventario_semanal (idinsumo,fecha,idtienda,inventario_inicial,enviado_tienda,retiro,inventario_final,consumo,costo_unitario,costo_total, costo_sin_consumir) values (" + cierreInv.getIdInsumo() + " ,'" + cierreInv.getFecha() + "' , " + cierreInv.getIdTienda() + " , " + cierreInv.getInventarioInicial() + " , " + cierreInv.getEnviadoTienda() + " , " + cierreInv.getRetiro() + " , " + cierreInv.getInventarioFinal() + " , " + cierreInv.getConsumo() + " , " + cierreInv.getCostoUnitario() + " , " + cierreInv.getCostoTotal() + " , " + cierreInv.getCostoSinConsumir() + ")" ;
			System.out.println(insert);
			stm.executeUpdate(insert);
			stm.close();
			con1.close();
		}
		catch (Exception e){
			System.out.println(e.getMessage());
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
