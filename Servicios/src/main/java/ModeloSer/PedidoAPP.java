package ModeloSer;

public class PedidoAPP {

	int idCliente;
	int cantidad;
	public int getIdCliente() {
		return idCliente;
	}
	public void setIdCliente(int idCliente) {
		this.idCliente = idCliente;
	}
	public int getCantidad() {
		return cantidad;
	}
	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}
	public PedidoAPP(int idCliente, int cantidad) {
		super();
		this.idCliente = idCliente;
		this.cantidad = cantidad;
	}
}
