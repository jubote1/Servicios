package ModeloSer;

public class PedidoPixel {
	
	private String domiciliario;
	private long transact;
	private double tiempoPedido;
	String estadoPedido;
	public String getDomiciliario() {
		return domiciliario;
	}
	public void setDomiciliario(String domiciliario) {
		this.domiciliario = domiciliario;
	}
	public long getTransact() {
		return transact;
	}
	public void setTransact(long transact) {
		this.transact = transact;
	}
	public double getTiempoPedido() {
		return tiempoPedido;
	}
	public void setTiempoPedido(double tiempoPedido) {
		this.tiempoPedido = tiempoPedido;
	}
	public String getEstadoPedido() {
		return estadoPedido;
	}
	public void setEstadoPedido(String estadoPedido) {
		this.estadoPedido = estadoPedido;
	}
	public PedidoPixel(String domiciliario, long transact, double tiempoPedido, String estadoPedido) {
		super();
		this.domiciliario = domiciliario;
		this.transact = transact;
		this.tiempoPedido = tiempoPedido;
		this.estadoPedido = estadoPedido;
	}
	
	public PedidoPixel()
	{
		
	}
	
	
	

}
