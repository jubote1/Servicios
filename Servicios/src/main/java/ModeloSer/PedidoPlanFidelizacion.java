package ModeloSer;

public class PedidoPlanFidelizacion {
	
	String fechaPedido;
	String correo;
	int idTienda;
	int idPedidoTienda;
	double valorNeto;
	String nombreCliente;
	double puntosAcumulados;
	String fechaInsercion;
	

	public String getNombreCliente() {
		return nombreCliente;
	}
	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}
	public double getPuntosAcumulados() {
		return puntosAcumulados;
	}
	public void setPuntosAcumulados(double puntosAcumulados) {
		this.puntosAcumulados = puntosAcumulados;
	}
	public String getFechaPedido() {
		return fechaPedido;
	}
	public void setFechaPedido(String fechaPedido) {
		this.fechaPedido = fechaPedido;
	}
	public String getCorreo() {
		return correo;
	}
	public void setCorreo(String correo) {
		this.correo = correo;
	}
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public int getIdPedidoTienda() {
		return idPedidoTienda;
	}
	public void setIdPedidoTienda(int idPedidoTienda) {
		this.idPedidoTienda = idPedidoTienda;
	}
	public double getValorNeto() {
		return valorNeto;
	}
	public void setValorNeto(double valorNeto) {
		this.valorNeto = valorNeto;
	}
	
	public String getFechaInsercion() {
		return fechaInsercion;
	}
	public void setFechaInsercion(String fechaInsercion) {
		this.fechaInsercion = fechaInsercion;
	}
	public PedidoPlanFidelizacion(String fechaPedido, String correo, int idTienda, int idPedidoTienda,
			double valorNeto, String nombres, String fechaInsercion) {
		super();
		this.fechaPedido = fechaPedido;
		this.correo = correo;
		this.idTienda = idTienda;
		this.idPedidoTienda = idPedidoTienda;
		this.valorNeto = valorNeto;
		this.nombreCliente = nombres;
		this.fechaInsercion = fechaInsercion;
	}
	public PedidoPlanFidelizacion() {
		super();
	}
	
	
	

}
