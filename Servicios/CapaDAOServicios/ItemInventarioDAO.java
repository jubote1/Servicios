package CapaDAOServicios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import com.mysql.jdbc.ResultSetMetaData;
import ConexionServicios.ConexionBaseDatos;
import Modelo.Insumo;

public class ItemInventarioDAO {

	//Crearemos método que obtendrá la información base para desplegar en el informe
		public static ArrayList obtenerCierreSemanalInsumos(String fechaActual, String fechaAnterior, String tipoItemInventario, String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			ArrayList itemsInvCierre = new ArrayList();
			int cantItems = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT a.iditem,a.nombre_item, a.unidad_medida," + 
						" ifnull((SELECT b.cantidad FROM item_inventario_historico b WHERE a.iditem = b.iditem AND B.fecha = '" + fechaAnterior + "'),0) AS INVENTARIO_INICIAL " +
						" ,ifnull( (SELECT SUM(d.cantidad) FROM ingreso_inventario c, ingreso_inventario_detalle d WHERE c.idingreso_inventario = d.idingreso_inventario AND d.iditem = a.iditem AND c.fecha_real >= '" + fechaAnterior + "' AND c.fecha_real <= '" + fechaActual + "'),0)  AS ENVIADO_A_TIENDA" +
						" ,ifnull( (select sum(c.cantidad) from retiro_inventario d, retiro_inventario_detalle c where c.idretiro_inventario = d.idretiro_inventario and c.iditem = a.iditem and d.fecha_sistema >= '" + fechaAnterior +"' AND d.fecha_sistema <= '" + fechaActual + "' ),0) AS RETIROS_OTRAS_TIENDAS " + 
						" , a.cantidad AS INVENTARIO_FINAL   from item_inventario a WHERE " +
						" a.categoria LIKE '" + tipoItemInventario + "%'";
				ResultSet rs = stm.executeQuery(consulta);
				ResultSetMetaData rsMd = (ResultSetMetaData) rs.getMetaData();
				int numeroColumnas = rsMd.getColumnCount();
				while(rs.next()){
					String [] fila = new String[numeroColumnas];
					for(int y = 0; y < numeroColumnas; y++)
					{
						fila[y] = rs.getString(y+1);
					}
					itemsInvCierre.add(fila);
					
				}
				stm.close();
				con1.close();
			}
			catch (Exception e){
				System.out.println(e.toString());
				try
				{
					con1.close();
				}catch(Exception e1)
				{
				}
				
			}
			return(itemsInvCierre);
		}
	
		//En este punto vamos a incluir el manejo de Insumos inventarios que para la tienda no tiene el nombre de
		//item inventario sino insumo

		//Crearemos método que obtendrá la información base para desplegar en el informe
		public static ArrayList<Insumo> obtenerInfoBasicaInsumos()
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDInventarioLocal();
			ArrayList <Insumo> insumos = new ArrayList();
			int cantItems = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT idinsumo, nombre_insumo, unidad_medida, categoria, costo_unidad FROM insumo";
				ResultSet rs = stm.executeQuery(consulta);
				int idInsumo;
				String nombreInsumo;
				String unidadMedida;
				String categoria;
				double costoUnidad;
				Insumo insumoTemp;
				while(rs.next()){
					idInsumo = rs.getInt("idinsumo");
					nombreInsumo = rs.getString("nombre_insumo");
					unidadMedida = rs.getString("unidad_medida");
					categoria = rs.getString("categoria");
					costoUnidad = rs.getDouble("costo_unidad");
					insumoTemp = new Insumo(idInsumo, nombreInsumo, unidadMedida, categoria, costoUnidad);
					insumos.add(insumoTemp);
				}
				stm.close();
				con1.close();
			}
			catch (Exception e){
				System.out.println(e.toString());
				try
				{
					con1.close();
				}catch(Exception e1)
				{
				}
				
			}
			return(insumos);
		}		
		
}
