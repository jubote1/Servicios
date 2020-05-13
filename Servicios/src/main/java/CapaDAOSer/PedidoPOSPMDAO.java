package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.PedidoPixel;

public class PedidoPOSPMDAO {
	
	
	/**
	 * Método en la capa de acceso a datos que se encarga de lanzar la consulta para conocer los estados de los pedidos de una
	 * tienda en el día en curso, retorma la información de estos pedidos en un ArrayList con objetos de tipo EstadoPedidoTienda
	 * @param dsnODBC Se recibe como parámetro el string con dsn para la conexión a la tienda.
	 * @return Se retorna un arrayList con objetos tipo EstadoPedidoTienda.
	 */
	public static ArrayList<PedidoPixel> obtenerPedidosPOSPM()
	{
		 ConexionBaseDatos conexion = new ConexionBaseDatos();
		 Connection con = conexion.obtenerConexionBDLocal();
		 ArrayList<PedidoPixel> pedidosPOS = new ArrayList();
		 try
			{
				Statement state = con.createStatement();
				String consulta = "SELECT b.nombre_largo as domiciliario,"
						+"a.idpedidotienda AS Transaccion,a.fechainsercion	 as horapedido, "
						+" IF(e.estado_final = 1, 'Finalizado' ,CASE when e.ruta_domicilio = 1 then 'En Ruta' when e.entrega_domicilio = 1 then 'Finalizado' ELSE 'Esperando' END) as estatus, " 
						+"   IF(e.ruta_domicilio = 1, TIMESTAMPDIFF(MINUTE,(select g.fechacambio from cambios_estado_pedido g where g.idpedidotienda = a.idpedidotienda and g.idestadoposterior = e.idestado order by g.fechacambio desc limit 1),NOW()), 0) as tiempoenruta, "
						+"  TIMESTAMPDIFF(MINUTE,(select f.fechacambio from cambios_estado_pedido f where f.idpedidotienda = a.idpedidotienda and f.idestadoanterior = 0 and f.idestadoposterior = 0  ), NOW()) as tiempototal , "
						+"g.nombre as tomadordepedido, "
						+"c.direccion as direccion,c.telefono AS telefono, "
						+"concat(c.nombre,' ',c.apellido) as nombrecompleto, "
						+"i.nombre as formapago "
						+"FROM pedido a join cliente c ON a.idcliente = c.idcliente "
						+"JOIN tipo_pedido j ON a.idtipopedido = j.idtipopedido and j.esdomicilio = 1 "
						+"JOIN usuario g ON g.nombre = a.usuariopedido "
						+"JOIN pedido_forma_pago h ON h.idpedidotienda = a.idpedidotienda "
						+"JOIN forma_pago i ON i.idforma_pago = h.idforma_pago "
						+"JOIN tienda d ON a.fechapedido = d.fecha_apertura " 
						+"JOIN estado e ON a.idestado = e.idestado "
						+"LEFT JOIN  usuario b ON a.iddomiciliario = b.id WHERE e.estado_final <> 1 "
						+"ORDER BY a.idpedidotienda DESC  ";
				ResultSet rs = state.executeQuery(consulta);
				String domiciliario = "";
				long transact;
				double tiempoPedido;
				String estadoPedido;
				System.out.println(consulta);
				while(rs.next())
				{
					domiciliario = rs.getString("domiciliario");
					transact = rs.getLong("Transaccion");
					tiempoPedido = rs.getDouble("tiempototal");
					estadoPedido = rs.getString("estatus");
					PedidoPixel pedTemp = new PedidoPixel(domiciliario,transact, tiempoPedido, estadoPedido);
					pedidosPOS.add(pedTemp);
					
				}
				state.close();
				con.close();
				
			}catch(Exception e)
			{
				System.out.println(e.getMessage());
				
			}
		 	
		 	return(pedidosPOS);
	}
		
	public static double obtenerTotalNetoPOSPM(int numPosHeader)
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conTiendaPixel = conexion.obtenerConexionBDLocal();
		String consulta = "select total_neto from pedido where idpedidotienda = " + numPosHeader;
		Statement stmTiendaPixel;
		ResultSet rsTiendaPixel;
		double totalNeto = 0;
		try
		{
			stmTiendaPixel = conTiendaPixel.createStatement();
			rsTiendaPixel = stmTiendaPixel.executeQuery(consulta);
			while(rsTiendaPixel.next())
			{
				totalNeto = rsTiendaPixel.getDouble("total_neto");
			}
			rsTiendaPixel.close();
			stmTiendaPixel.close();
			conTiendaPixel.close();
		}catch(Exception e)
		{
			System.out.println(e.toString());
		}
		return(totalNeto);
	}

}
