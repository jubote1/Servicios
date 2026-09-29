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
	 * AHORA FILTRA POR activo, QUE ANTES NO HACIA NADA
	 *
	 * Esto era un SELECT * sin WHERE: la columna activo estaba ahi pero nadie la
	 * miraba, y lo unico que decidia si un concepto corria era que su consulta
	 * no dijera 'NA'. El resultado es que la tabla decia lo contrario de lo que
	 * pasaba -los quince conceptos que se ejecutaban estaban marcados activo = 0,
	 * y los veinte marcados activo = 1 no tenian consulta-, y nadie podia
	 * apagar un calculo sin borrarle el texto SQL.
	 *
	 * Con el filtro, apagar un concepto es poner activo = 0 y la consulta queda
	 * guardada para saber que se hacia antes.
	 *
	 * OJO CON EL ORDEN DE DESPLIEGUE: primero hay que correr
	 * 2026_09_29_03_gasto_configuracion_una_sola_fuente.sql, que deja las
	 * banderas como deben quedar. Si este jar sube antes, el proceso no
	 * calcularia nada, porque hoy los quince conceptos reales estan en cero.
	 */
	public static ArrayList<GastoConfiguracion> obtenerGastorConfiguracionTienda()
	{

		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDInventarioLocal();
		ArrayList<GastoConfiguracion> gastoConfiguraciones = new ArrayList();
		try
		{
			Statement stm = con1.createStatement();
			String select = "SELECT * FROM gasto_configuracion WHERE activo = 1 ORDER BY idgasto_conf" ;
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
