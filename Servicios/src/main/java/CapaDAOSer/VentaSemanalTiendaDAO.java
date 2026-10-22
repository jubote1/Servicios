package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.EstadisticaProducto;
import ModeloSer.GastoConfiguracion;
import ModeloSer.GastoSemanal;
import ModeloSer.VentaSemanalTienda;

public class VentaSemanalTiendaDAO {
	
	/**
	 * Guarda la venta de la semana de una tienda, o la corrige si ya estaba.
	 *
	 * ES UN UPSERT A PROPOSITO
	 *
	 * La llave primaria es (idtienda, fecha). Mientras esto fue un insert
	 * pelado, una fila mala quedaba mala para siempre: el reproceso chocaba
	 * contra la llave, el error se lo tragaba este mismo catch sin imprimir
	 * nada, y el proceso terminaba diciendo que todo salio bien. Asi quedaron
	 * 98 semanas en cero desde 2021.
	 *
	 * Sobrescribir no pierde nada bueno porque quien llama ya no manda ceros de
	 * fallo: ReporteCierreInventario solo escribe cuando la tienda respondio.
	 */
	public static void insertarVentaSemanalTienda(VentaSemanalTienda ventSemanal)
	{
		
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDDatamartLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into venta_semanal_tienda (idtienda,fecha,valor,meta) values (" + ventSemanal.getIdTienda() + " ,'" + ventSemanal.getFecha() + "' , " + ventSemanal.getValor() + " , " + ventSemanal.getMeta() + ")"
					+ " on duplicate key update valor = " + ventSemanal.getValor() + " , meta = " + ventSemanal.getMeta() ;
			stm.executeUpdate(insert);
			stm.close();
			con1.close();
		}
		catch (Exception e){
			//Sin este println la tabla se escribia a ciegas: nadie se enteraba
			//de una semana que no se guardo.
			System.out.println("VentaSemanalTiendaDAO.insertarVentaSemanalTienda tienda "
					+ ventSemanal.getIdTienda() + " fecha " + ventSemanal.getFecha() + ": " + e.toString());
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
