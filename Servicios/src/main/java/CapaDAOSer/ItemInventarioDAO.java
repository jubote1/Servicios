package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import com.mysql.cj.jdbc.result.ResultSetMetaData;
import ConexionSer.ConexionBaseDatos;
import ModeloSer.ConsumoInventario;
import ModeloSer.EmpleadoEvento;
import ModeloSer.Insumo;

public class ItemInventarioDAO {

	//Crearemos m�todo que obtendr� la informaci�n base para desplegar en el informe
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
						" ,ifnull( (SELECT SUM(d.cantidad) FROM ingreso_inventario c, ingreso_inventario_detalle d WHERE c.idingreso_inventario = d.idingreso_inventario AND d.iditem = a.iditem AND c.fecha_sistema >= '" + fechaAnterior + "' AND c.fecha_sistema <= '" + fechaActual + "'),0)  AS ENVIADO_A_TIENDA" +
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
		
		
		/**
		 * Método que trae los insumos para realizar reporte de Cierre pero teniendo en cuenta que el lunes no se abrió
		 * @param fechaActual
		 * @param fechaAnterior
		 * @param tipoItemInventario
		 * @param url
		 * @return
		 */
		public static ArrayList obtenerCierreSemanalInsumosSinLunes(String fechaActual, String fechaAnterior, String preFechaAnterior, String tipoItemInventario, String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			ArrayList itemsInvCierre = new ArrayList();
			int cantItems = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT a.iditem,a.nombre_item, a.unidad_medida," + 
						" ifnull((SELECT b.cantidad FROM item_inventario_varianza b, inventario_varianza iv WHERE iv.idinventario_varianza = b.idinventario_varianza and a.iditem = b.iditem AND iv.fecha_sistema = '" + preFechaAnterior + "' order by b.idinventario_varianza desc limit 1),0) AS INVENTARIO_INICIAL " +
						" ,ifnull( (SELECT SUM(d.cantidad) FROM ingreso_inventario c, ingreso_inventario_detalle d WHERE c.idingreso_inventario = d.idingreso_inventario AND d.iditem = a.iditem AND c.fecha_sistema >= '" + fechaAnterior + "' AND c.fecha_sistema <= '" + fechaActual + "'),0)  AS ENVIADO_A_TIENDA" +
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
		
		
		/**
		 * Este m�todo tiene como objetivo la generaci�n de la informaci�n en un ambiente de reproceso, deber� pedir tambien la informaci�n en un ambiente de d�a siguiente
		 * con el fin de traer el hist�tico del d�a siguiente dado que ya ese d�a se aperturo.
		 * @param fechaActual
		 * @param fechaAnterior
		 * @param tipoItemInventario
		 * @param url
		 * @return
		 */
		public static ArrayList obtenerCierreSemanalInsumosReproceso(String fechaActual, String fechaAnterior, String tipoItemInventario, String url, String fechaActualSiguiente)
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
						" ,ifnull( (SELECT SUM(d.cantidad) FROM ingreso_inventario c, ingreso_inventario_detalle d WHERE c.idingreso_inventario = d.idingreso_inventario AND d.iditem = a.iditem AND c.fecha_sistema >= '" + fechaAnterior + "' AND c.fecha_sistema <= '" + fechaActual + "'),0)  AS ENVIADO_A_TIENDA" +
						" ,ifnull( (select sum(c.cantidad) from retiro_inventario d, retiro_inventario_detalle c where c.idretiro_inventario = d.idretiro_inventario and c.iditem = a.iditem and d.fecha_sistema >= '" + fechaAnterior +"' AND d.fecha_sistema <= '" + fechaActual + "' ),0) AS RETIROS_OTRAS_TIENDAS " + 
						" ,ifnull((SELECT e.cantidad FROM item_inventario_historico e WHERE a.iditem = e.iditem AND e.fecha = '" + fechaActualSiguiente + "'),0) AS INVENTARIO_FINAL   from item_inventario a WHERE " +
						" a.categoria LIKE '" + tipoItemInventario + "%'";
				System.out.println(consulta);
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

		//Crearemos m�todo que obtendr� la informaci�n base para desplegar en el informe
		public static ArrayList<Insumo> obtenerInfoBasicaInsumos()
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDInventarioLocal();
			ArrayList <Insumo> insumos = new ArrayList();
			int cantItems = 0;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT idinsumo, nombre_insumo, unidad_medida, categoria, costo_unidad,embalaje_costo FROM insumo";
				ResultSet rs = stm.executeQuery(consulta);
				int idInsumo;
				String nombreInsumo;
				String unidadMedida;
				String categoria;
				double costoUnidad;
				double embalajeCosto;
				Insumo insumoTemp;
				while(rs.next()){
					idInsumo = rs.getInt("idinsumo");
					nombreInsumo = rs.getString("nombre_insumo");
					unidadMedida = rs.getString("unidad_medida");
					categoria = rs.getString("categoria");
					costoUnidad = rs.getDouble("costo_unidad");
					embalajeCosto = rs.getDouble("embalaje_costo");
					insumoTemp = new Insumo(idInsumo, nombreInsumo, unidadMedida, categoria, costoUnidad);
					insumoTemp.setEmbalajeCosto(embalajeCosto);
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

		
		/**
		 * M�todo que desde el proyecto Servicios recupera los consumos de inventario de la tienda en cuesti�n con el fin de llevarlos
		 * hacia el sistema de inventarios de bodega.
		 * @param fecha
		 * @param hostBD
		 * @return
		 */
		public static ArrayList<ConsumoInventario> recuperarConsumosInventario(String fecha, String hostBD)
		{
			ConexionSer.ConexionBaseDatos con = new ConexionSer.ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(hostBD);
			ArrayList<ConsumoInventario> consumosInventarios = new ArrayList();
			ConsumoInventario consuTemp = new ConsumoInventario(0,0);
			int idInsumo;
			double consumo;
			String consulta = "select a.iditem, ifnull((select b.cantidad from item_inventario_historico b " + 
					"where b.iditem = a.iditem and b.fecha = '" + fecha + "'),0) - ifnull( (select sum(c.cantidad) from retiro_inventario d, " + 
					"retiro_inventario_detalle c where c.idretiro_inventario = d.idretiro_inventario and c.iditem = a.iditem " + 
					"and d.fecha_sistema ='" + fecha + "' ),0) + ifnull((select sum(f.cantidad) " + 
					"from ingreso_inventario e, ingreso_inventario_detalle f where e.idingreso_inventario = f.idingreso_inventario " + 
					"and f.iditem = a.iditem  and e.fecha_sistema = '" + fecha + "' ) ,0)  - ifnull((select sum(h.cantidad) " + 
					"from inventario_varianza g, item_inventario_varianza h WHERE g.idinventario_varianza = h.idinventario_varianza " + 
					"AND a.iditem = h.iditem and g.fecha_sistema = '" + fecha + "' and h.idinventario_varianza = (select max(idinventario_varianza) from inventario_varianza where fecha_sistema = '" + fecha + "') " +
					") ,0) as consumo from item_inventario a  order by a.orden " + 
					"";
			Statement stm;
			ResultSet rs;
			try
			{
				stm = con1.createStatement();
				rs = stm.executeQuery(consulta);
				while(rs.next())
				{
					idInsumo = rs.getInt("iditem");
					consumo = rs.getDouble("consumo");
					consuTemp = new ConsumoInventario(idInsumo, consumo);
					consumosInventarios.add(consuTemp);
				}
				rs.close();
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
			return(consumosInventarios);
			
		}
		
		/**
		 * Metodo que valida si un día esta abierto o no mirando si dejo restro de inventario antes de abrir.
		 * @param fecha
		 * @return
		 */
		public static boolean validarSiDiaAbrio(String fecha, String url)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = con.obtenerConexionBDTiendaRemota(url);
			boolean respuesta = false;
			try
			{
				Statement stm = con1.createStatement();
				String consulta = "SELECT * FROM item_inventario_historico where fecha = '" + fecha + "'";
				ResultSet rs = stm.executeQuery(consulta);
				while(rs.next()){
					respuesta = true;
					break;
				}
				rs.close();
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
			return(respuesta);
		}		
		
}
