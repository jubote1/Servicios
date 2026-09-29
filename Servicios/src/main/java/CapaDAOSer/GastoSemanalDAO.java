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
	
		/**
		 * Ejecuta la consulta de un concepto contra la tienda o contra inventario.
		 *
		 * UN ERROR YA NO SE GUARDA COMO UN CERO
		 *
		 * Antes, si la tienda no respondia, si la consulta estaba mal escrita o si
		 * la tabla habia cambiado de nombre, este metodo atrapaba la excepcion y
		 * devolvia 0. Ese cero se guardaba en gasto_semanal igual que un cero de
		 * verdad, y desde afuera no habia forma de distinguir "esa semana no hubo
		 * gasto" de "no se pudo preguntar".
		 *
		 * Asi es exactamente como la linea de Rappi paso cuatro anos en cero sin
		 * que nadie lo notara: la consulta filtraba por una estacion que ya no se
		 * llamaba asi, no devolvia filas, y el cero se veia perfectamente normal.
		 *
		 * Ahora devuelve NaN cuando no se pudo calcular. Quien llama tiene que
		 * revisarlo con Double.isNaN y NO guardar la fila: una fila que falta se
		 * ve, un cero se suma.
		 *
		 * Ojo con la diferencia entre las dos cosas que devuelven cero legitimo:
		 * una consulta que corre y no trae filas -SUM sobre cero registros, que
		 * en SQL es NULL y aqui queda en 0- y una que corre y trae un cero. Las
		 * dos son respuestas validas y se guardan. Lo que no se guarda es la que
		 * no llego a correr.
		 *
		 * @return el valor, o Double.NaN si la consulta no se pudo ejecutar
		 */
		public static double obtenerValorCalculo(String hostBD, String consulta, String origen)
		{
			ConexionBaseDatos con = new ConexionBaseDatos();
			Connection con1 = null;
			if(origen != null && origen.contains("TIENDA"))
			{
				con1 = con.obtenerConexionBDTiendaRemota(hostBD);
			}else if(origen != null && origen.contains("INVENTARIO"))
			{
				con1 = con.obtenerConexionBDInventarioLocal();
			}
			//Un origen que no se reconoce dejaba con1 en null y reventaba mas
			//abajo con un NullPointerException que tambien terminaba en cero.
			if(con1 == null)
			{
				System.out.println("obtenerValorCalculo: sin conexion para origen [" + origen
						+ "] host [" + hostBD + "]");
				return(Double.NaN);
			}

			double valorCalculado = Double.NaN;
			try
			{
				Statement stm = con1.createStatement();
				ResultSet rs = stm.executeQuery(consulta);
				//Si la consulta corrio, el resultado vale aunque venga vacio o en
				//cero. Se arranca en 0 aca adentro, ya sabiendo que no hubo error.
				valorCalculado = 0;
				while(rs.next()){

					valorCalculado = rs.getDouble(1);

				}
				rs.close();
				stm.close();
			}catch (Exception e)
			{
				System.out.println("obtenerValorCalculo fallo contra [" + hostBD + "]: " + e.toString());
				valorCalculado = Double.NaN;
			}
			finally
			{
				try
				{
					con1.close();
				}catch(Exception e1)
				{
					System.out.println("obtenerValorCalculo: cerrando conexion " + e1.toString());
				}
			}
			return(valorCalculado);
		}
	

}
