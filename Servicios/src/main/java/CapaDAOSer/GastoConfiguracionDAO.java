package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.GastoConfiguracion;

public class GastoConfiguracionDAO {

	/**
	 * Los gastos que ReporteConsolidacionRentabilidad tiene que calcular.
	 *
	 * NO FILTRA POR activo, Y ESO ES A PROPOSITO
	 *
	 * El 2026-09-29 le puse aqui un WHERE activo = 1 y le volteé la bandera a
	 * la tabla, porque la columna parecia no servir para nada: los quince
	 * conceptos que de verdad corrian estaban en activo = 0 y los veinte
	 * marcados en 1 no tenian consulta. Parecia un error que nadie habia
	 * notado.
	 *
	 * No lo era: era la convencion que esperaban otros lectores, y uno de
	 * ellos -un reporte- dejo de traer conceptos. Lo revertí el 2026-10-08.
	 *
	 * Lo que decide si un concepto se calcula sigue siendo lo de siempre: que
	 * su consulta_sql no diga 'NA'. Quien quiera apagar uno, le pone 'NA'.
	 *
	 * SI HACE FALTA MARCAR CONCEPTOS PARA ALGO NUEVO, COLUMNA NUEVA.
	 * El tablero de rentabilidad del inventario usa `rentabilidad_calcula`,
	 * que es suya y de nadie mas. Reusar una columna que ya tiene duenio es
	 * exactamente lo que rompio el reporte.
	 */
	public static ArrayList<GastoConfiguracion> obtenerGastorConfiguracionTienda()
	{

		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventarioLocal();
		ArrayList<GastoConfiguracion> gastoConfiguraciones = new ArrayList();
		try
		{
			Statement stm = con1.createStatement();
			String select = "SELECT * FROM gasto_configuracion ORDER BY idgasto_conf" ;
			int idGastoConf;
			String nombreGasto;
			String consultaSQL;
			double porcentajeGasto;
			String origen;
			ResultSet rs = stm.executeQuery(select);
			GastoConfiguracion gastConf = new GastoConfiguracion();
			while(rs.next())
			{
				idGastoConf = rs.getInt("idgasto_conf");
				nombreGasto = rs.getString("nombre_gasto");
				consultaSQL = rs.getString("consulta_sql");
				porcentajeGasto = rs.getDouble("porcentaje_gasto");
				origen = rs.getString("origen");
				gastConf = new GastoConfiguracion(idGastoConf, nombreGasto, consultaSQL, porcentajeGasto, origen);
				gastoConfiguraciones.add(gastConf);
			}
			rs.close();
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
		return(gastoConfiguraciones);
	}
	

}
