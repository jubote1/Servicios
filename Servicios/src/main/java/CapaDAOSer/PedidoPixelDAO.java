package CapaDAOSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ConexionSer.ConexionBaseDatos;
import ModeloSer.PedidoPixel;

public class PedidoPixelDAO {
	
	public static ArrayList<PedidoPixel> obtenerPedidosPOS()
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conTiendaPixel = conexion.obtenerConexionBDTienda("");
		String consulta = "SELECT b.POSNAME as domiciliario,"
				+"a.Transact AS transaccion,d.timeend as horapedido, "
				+"CASE when (a.DeliveryStatus = 0) then string('Esperando') " 
				+"when (a.DeliveryStatus = 1) then string('En Ruta') " 
				+"when (a.DeliveryStatus = 2) then string('Finalizado')  END as estatus, "
				+"CASE when (a.DeliveryStatus = 0) then string('Pediente de entrega') "
				+"when (a.DeliveryStatus = 1) then string(DATEDIFF(mi,a.TimeOut,now()),' min') " 
				+"when (a.DeliveryStatus = 2) then string(DATEDIFF(mi,a.TimeOut,a.TimeIn),' min')  END as tiempoenruta, "
				+"CASE WHEN (a.DeliveryStatus > 2) THEN 0 "
				+"WHEN (a.DeliveryStatus = 2) THEN DATEDIFF(mi,d.timeend,a.TimeIn) "
				+"WHEN (a.DeliveryStatus < 2) THEN DATEDIFF(mi,d.timeend,now()) END as tiempototal , "
				+"g.POSNAME as tomadordepedido, "
				+"string(c.ADRESS1,' ',c.ADRESS2,' ',c.Directions) as direccion,c.HOMETELE AS telefono, "
				+"string(c.FIRSTNAME,' ',c.LASTNAME) as nombrecompleto, "
				+"i.DESCRIPT as formapago "
				+"FROM dba.PosHDelivery a join dba.Member c ON a.MemCode = c.MEMCODE "
				+"JOIN dba.POSHEADER d ON a.Transact = d.transact "
				+"JOIN dba.employee g ON g.EmpNum = d.whostart "
				+"JOIN dba.Howpaid h ON h.transact = d.transact and h.voidedlink = 0 "
				+"JOIN dba.MethodPay i ON i.METHODNUM = h.METHODNUM "
				+"LEFT JOIN  dba.employee b ON a.EmpNum = b.EMPNUM "
				+"WHERE a.OpenDate = dba.PixOpenDate() and  a.DeliveryStatus <> 2 "
				+"ORDER BY a.Transact DESC  ";
		Statement stmTiendaPixel;
		ResultSet rsTiendaPixel;
		ArrayList<PedidoPixel> pedidosPOS = new ArrayList();
		String domiciliario = "";
		long transact;
		double tiempoPedido;
		String estadoPedido;
		System.out.println(consulta);
		try
		{
			stmTiendaPixel = conTiendaPixel.createStatement();
			rsTiendaPixel = stmTiendaPixel.executeQuery(consulta);
			while(rsTiendaPixel.next())
			{
				domiciliario = rsTiendaPixel.getString("domiciliario");
				transact = rsTiendaPixel.getLong("transaccion");
				tiempoPedido = rsTiendaPixel.getDouble("tiempototal");
				estadoPedido = rsTiendaPixel.getString("estatus");
				PedidoPixel pedTemp = new PedidoPixel(domiciliario,transact, tiempoPedido, estadoPedido);
				pedidosPOS.add(pedTemp);
			}
			rsTiendaPixel.close();
			stmTiendaPixel.close();
			conTiendaPixel.close();
		}catch(Exception e)
		{
			
			System.out.println("ERROR EN PEDIDOPIXELDAO" +  e.toString());
		}
		return(pedidosPOS);
	}
	
	
	public static double obtenerTotalNetoPixel(int numPosHeader)
	{
		ConexionBaseDatos conexion = new ConexionBaseDatos();
		Connection conTiendaPixel = conexion.obtenerConexionBDTienda("");
		String consulta = "select finaltotal from dba.posheader where transact = " + numPosHeader;
		Statement stmTiendaPixel;
		ResultSet rsTiendaPixel;
		double totalNeto = 0;
		try
		{
			stmTiendaPixel = conTiendaPixel.createStatement();
			rsTiendaPixel = stmTiendaPixel.executeQuery(consulta);
			while(rsTiendaPixel.next())
			{
				totalNeto = rsTiendaPixel.getDouble("finaltotal");
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
