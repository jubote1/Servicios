package capaModelo;

public class MarcacionAnulacionPedido {
	
	private int idMarcacionAnulacion;
	private int idMarcacion;
	private int idPedido;
	private int numPosHeader;
	private String fechaPedido;
	private double totalNeto;
	public int getIdMarcacionAnulacion() {
		return idMarcacionAnulacion;
	}
	public void setIdMarcacionAnulacion(int idMarcacionAnulacion) {
		this.idMarcacionAnulacion = idMarcacionAnulacion;
	}
	public int getIdMarcacion() {
		return idMarcacion;
	}
	public void setIdMarcacion(int idMarcacion) {
		this.idMarcacion = idMarcacion;
	}
	public int getIdPedido() {
		return idPedido;
	}
	public void setIdPedido(int idPedido) {
		this.idPedido = idPedido;
	}
	public int getNumPosHeader() {
		return numPosHeader;
	}
	public void setNumPosHeader(int numPosHeader) {
		this.numPosHeader = numPosHeader;
	}
	public String getFechaPedido() {
		return fechaPedido;
	}
	public void setFechaPedido(String fechaPedido) {
		this.fechaPedido = fechaPedido;
	}
	public double getTotalNeto() {
		return totalNeto;
	}
	public void setTotalNeto(double totalNeto) {
		this.totalNeto = totalNeto;
	}
	public MarcacionAnulacionPedido(int idMarcacionAnulacion, int idMarcacion, int idPedido, int numPosHeader,
			String fechaPedido, double totalNeto) {
		super();
		this.idMarcacionAnulacion = idMarcacionAnulacion;
		this.idMarcacion = idMarcacion;
		this.idPedido = idPedido;
		this.numPosHeader = numPosHeader;
		this.fechaPedido = fechaPedido;
		this.totalNeto = totalNeto;
	}
	
	
	

}
