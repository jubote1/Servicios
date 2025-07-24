package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import capaModeloPOS.EmpleadoTemporalDia;
import capaModeloPOS.Ingreso;

public class EmpleadoTemporalDiaDatamartDAO {
	
	public static boolean validarInsercionEmpleadoTemporalDiaDatamart(String fechaAnterior, String fechaActual, int idTienda)
	{
		boolean respuesta = false;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDDatamartLocal();
		try
		{
			Statement stm = con1.createStatement();
			String select = "select count(*) from empleado_temporal_dia where fecha_sistema >= '" + fechaAnterior + "' and fecha_sistema <= '" + fechaActual +"' and idtienda =" + idTienda;
			ResultSet rs = stm.executeQuery(select);
			while(rs.next())
			{
				int resultado = rs.getInt(1);
				if(resultado > 0)
				{
					respuesta = true;
				}
				break;
			}
			stm.close();
			con1.close();
		}catch (Exception e){
			e.toString();
			try
			{
				con1.close();
			}catch(Exception e1)
			{
				System.out.println("falle cerrando la conexion");
			}
		}
		return(respuesta);
	}
	
	
	public static boolean insertarEmpleadoTemporalDiaDatamart(capaModeloPOS.EmpleadoTemporalDia empTemporal, int idTienda) {
	    ConexionBaseDatos con = new ConexionBaseDatos();
	    Connection con1 = con.obtenerConexionBDDatamartLocal();
	    String insert = "insert into empleado_temporal_dia (idtienda, id,identificacion, nombre, telefono, empresa, fecha_sistema, horaingreso, horasalida, idempresa, observacion, pedidos) values (" + idTienda + "," + empTemporal.getId() + " , '" + empTemporal.getIdentificacion() + "' , '" + empTemporal.getNombre() + "' , '" + empTemporal.getTelefono() + "' , '" + empTemporal.getEmpresa() + "' , '" + empTemporal.getFechaSistema() + "' , '" + empTemporal.getHoraIngreso() + "' , '" + empTemporal.getHoraSalida() + "' , " + empTemporal.getIdEmpresa() + " , '" + empTemporal.getObservacion() + "' ," + empTemporal.getPedidos() + ")";
	    try {
	      Statement stm = con1.createStatement();
	      stm.executeUpdate(insert);
	      stm.close();
	      con1.close();
	    } catch (Exception e) {
	      System.out.println(e.toString());
	      try {
	        con1.close();
	        return false;
	      } catch (Exception e1) {
	        return false;
	      } 
	    } 
	    return true;
	  }
	

}
