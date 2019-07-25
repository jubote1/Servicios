package Modelo;

public class PedidoFueraTiempo {
	
	private int idPedido;
	private int idTienda;
	private long transact;
	private double tiempoDado;
	private double tiempoActual;
	private double porcDesviacion;
	private String domiciliario;
	private String estadoPedido;
	private String observacion;
	
	
	
	public String getObservacion() {
		return observacion;
	}
	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
	public int getIdPedido() {
		return idPedido;
	}
	public void setIdPedido(int idPedido) {
		this.idPedido = idPedido;
	}
	public long getTransact() {
		return transact;
	}
	public void setTransact(long transact) {
		this.transact = transact;
	}
	public double getTiempoDado() {
		return tiempoDado;
	}
	public void setTiempoDado(double tiempoDado) {
		this.tiempoDado = tiempoDado;
	}
	public double getTiempoActual() {
		return tiempoActual;
	}
	public void setTiempoActual(double tiempoActual) {
		this.tiempoActual = tiempoActual;
	}
	public String getDomiciliario() {
		return domiciliario;
	}
	public void setDomiciliario(String domiciliario) {
		this.domiciliario = domiciliario;
	}
	public String getEstadoPedido() {
		return estadoPedido;
	}
	public void setEstadoPedido(String estadoPedido) {
		this.estadoPedido = estadoPedido;
	}
	
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public double getPorcDesviacion() {
		return porcDesviacion;
	}
	public void setPorcDesviacion(double porcDesviacion) {
		this.porcDesviacion = porcDesviacion;
	}
	public PedidoFueraTiempo(int idPedido, int idTienda,  long transact, double tiempoDado, double tiempoActual, double porcDesviacion, String domiciliario,
			String estadoPedido, String observacion) {
		this.idPedido = idPedido;
		this.idTienda = idTienda;
		this.transact = transact;
		this.tiempoDado = tiempoDado;
		this.tiempoActual = tiempoActual;
		this.porcDesviacion = porcDesviacion;
		this.domiciliario = domiciliario;
		this.estadoPedido = estadoPedido;
		this.observacion = observacion; 
		
	}
	
	
	

}
