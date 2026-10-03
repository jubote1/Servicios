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
			/*
			 * Va con ON DUPLICATE KEY UPDATE para que el REPROCESO si sirva.
			 *
			 * La tabla tiene llave primaria (idinsumo, fecha, idtienda). Con el
			 * insert pelado, reprocesar una semana ya cerrada chocaba contra esa
			 * llave en TODAS las filas, el catch de abajo se tragaba el error y
			 * el proceso seguia como si nada: el correo salia recalculado y
			 * correcto, pero la base del central se quedaba con las cifras
			 * viejas. Nadie se enteraba, porque no habia forma de notarlo.
			 *
			 * Paso de verdad: se corrigio el inventario de un sirope mal
			 * digitado en tres tiendas y el reproceso no habria actualizado
			 * nada.
			 */
			String insert = "insert into cierre_inventario_semanal (idinsumo,fecha,idtienda,inventario_inicial,enviado_tienda,retiro,inventario_final,consumo,costo_unitario,costo_total, costo_sin_consumir) values (" + cierreInv.getIdInsumo() + " ,'" + cierreInv.getFecha() + "' , " + cierreInv.getIdTienda() + " , " + cierreInv.getInventarioInicial() + " , " + cierreInv.getEnviadoTienda() + " , " + cierreInv.getRetiro() + " , " + cierreInv.getInventarioFinal() + " , " + cierreInv.getConsumo() + " , " + cierreInv.getCostoUnitario() + " , " + cierreInv.getCostoTotal() + " , " + cierreInv.getCostoSinConsumir() + ")"
					+ " on duplicate key update inventario_inicial = values(inventario_inicial),"
					+ " enviado_tienda = values(enviado_tienda), retiro = values(retiro),"
					+ " inventario_final = values(inventario_final), consumo = values(consumo),"
					+ " costo_unitario = values(costo_unitario), costo_total = values(costo_total),"
					+ " costo_sin_consumir = values(costo_sin_consumir)";
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
	


	/**
	 * Lo que esa tienda suele gastar en insumos por semana.
	 *
	 * Es el patron contra el que se juzga la semana que se esta cerrando. Mira
	 * SOLO semanas anteriores a la que se cierra -la de hoy ya quedo escrita
	 * unos renglones antes y se estaria comparando contra si misma- y descarta
	 * las semanas en cero, que son las que la tienda no reporto.
	 *
	 * @return el promedio, o 0 cuando no hay con que comparar todavia
	 */
	public static double promedioSemanalTienda(int idTienda, String fechaCierre, int semanas)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDDatamartLocal();
		double promedio = 0;
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT AVG(t.total) AS promedio FROM ("
					+ " SELECT fecha, SUM(costo_total) total"
					+ "   FROM cierre_inventario_semanal"
					+ "  WHERE idtienda = " + idTienda
					+ "    AND fecha < '" + fechaCierre + "'"
					+ "    AND fecha >= DATE_SUB('" + fechaCierre + "', INTERVAL " + semanas + " WEEK)"
					+ "  GROUP BY fecha HAVING total > 0) t";
			ResultSet rs = stm.executeQuery(consulta);
			if (rs.next())
			{
				promedio = rs.getDouble("promedio");
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e)
		{
			System.out.println("CierreInventarioSemanalDAO.promedioSemanalTienda: " + e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(promedio);
	}

}
