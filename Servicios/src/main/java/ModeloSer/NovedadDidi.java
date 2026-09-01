package ModeloSer;

public class NovedadDidi {
	
	private int idPedidoTienda;
	private String novedad;
	private double valorAnulacion;
	private double valorDescuento;
	private double valorTotal;
	public int getIdPedidoTienda() {
		return idPedidoTienda;
	}
	public void setIdPedidoTienda(int idPedidoTienda) {
		this.idPedidoTienda = idPedidoTienda;
	}
	public String getNovedad() {
		return novedad;
	}
	public void setNovedad(String novedad) {
		this.novedad = novedad;
	}
	public double getValorAnulacion() {
		return valorAnulacion;
	}
	public void setValorAnulacion(double valorAnulacion) {
		this.valorAnulacion = valorAnulacion;
	}
	public double getValorDescuento() {
		return valorDescuento;
	}
	public void setValorDescuento(double valorDescuento) {
		this.valorDescuento = valorDescuento;
	}
	public double getValorTotal() {
		return valorTotal;
	}
	public void setValorTotal(double valorTotal) {
		this.valorTotal = valorTotal;
	}
	public NovedadDidi() {
		super();
	}
	public NovedadDidi(int idPedidoTienda, String novedad, double valorAnulacion, double valorDescuento,
			double valorTotal) {
		super();
		this.idPedidoTienda = idPedidoTienda;
		this.novedad = novedad;
		this.valorAnulacion = valorAnulacion;
		this.valorDescuento = valorDescuento;
		this.valorTotal = valorTotal;
	}

}
