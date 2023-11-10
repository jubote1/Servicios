package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.EmpresaTemporal;
import ModeloSer.TiendaCodigoPromocional;

public class TiendaCodigoPromocionalDAO {
	
	public static ArrayList<TiendaCodigoPromocional> retornarTiendaCodigoPromocional(int idProcesoPromocion )
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDContactLocal();
		ArrayList <TiendaCodigoPromocional> tiendaCodPromos = new ArrayList();
		
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select *  from tienda_codigo_promocional where idproceso = " + idProcesoPromocion;
			ResultSet rs = stm.executeQuery(consulta);
			TiendaCodigoPromocional tiendaCodTemp;
			int idTienda;
			String fechaInicial;
			String fechaFinal;
			int lunClientes;
			int marClientes;
			int mieClientes;
			int jueClientes;
			int vieClientes;
			int sabClientes;
			int domClientes;
			int idOferta;
			while(rs.next()){
				idTienda = rs.getInt("idtienda");
				fechaInicial = rs.getString("fecha_inicial");
				fechaFinal = rs.getString("fecha_final");
				lunClientes = rs.getInt("lun_clientes");
				marClientes = rs.getInt("mar_clientes");
				mieClientes = rs.getInt("mie_clientes");
				jueClientes = rs.getInt("jue_clientes");
				vieClientes = rs.getInt("vie_clientes");
				sabClientes = rs.getInt("sab_clientes");
				domClientes = rs.getInt("dom_clientes");
				idOferta = rs.getInt("idoferta");
				tiendaCodTemp = new TiendaCodigoPromocional(idTienda, fechaInicial, fechaFinal, lunClientes, marClientes, mieClientes, jueClientes, vieClientes, sabClientes, domClientes, idOferta);
				tiendaCodPromos.add(tiendaCodTemp);
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(tiendaCodPromos);
	}

}
