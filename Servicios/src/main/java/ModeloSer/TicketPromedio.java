package ModeloSer;

public class TicketPromedio {
	
	private String fecha;
	private int idTienda;
	private double valor;
	private int cantidadPedidos;
	
	
	
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public double getValor() {
		return valor;
	}
	public void setValor(double valor) {
		this.valor = valor;
	}
	public int getCantidadPedidos() {
		return cantidadPedidos;
	}
	public void setCantidadPedidos(int cantidadPedidos) {
		this.cantidadPedidos = cantidadPedidos;
	}
	public TicketPromedio(String fecha, int idTienda, double valor, int cantidadPedidos) {
		super();
		this.fecha = fecha;
		this.idTienda = idTienda;
		this.valor = valor;
		this.cantidadPedidos = cantidadPedidos;
	}
	
	
	
}
