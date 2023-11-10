package ServiciosSer;

import capaControladorPOS.PedidoCtrl;
import capaDAOFirebase.CrudFirebase;

public class ServicioDepuracionPedidosFirebase {
	
	public static void main(String[] args)
	{
		PedidoCtrl pedCtrl = new PedidoCtrl(false);
		pedCtrl.depurarPedidos();
	}

}
