package ModeloSer;

public class PedidoAnulado {
	
	private int idPedidoTienda;
	private int idTienda;
	public int getIdPedidoTienda() {
		return idPedidoTienda;
	}
	public void setIdPedidoTienda(int idPedidoTienda) {
		this.idPedidoTienda = idPedidoTienda;
	}
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public PedidoAnulado(int idPedidoTienda, int idTienda) {
		super();
		this.idPedidoTienda = idPedidoTienda;
		this.idTienda = idTienda;
	}
	
	

}
