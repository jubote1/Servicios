package Modelo;

public class MarcacionCambioPedido {
	
	private int idMarcacionCambio;
	private int idMarcacion;
	private int idPedido;
	private int numPosHeader;
	private String fechaPedido;
	private double totalNetoContact;
	private double totalNetoTienda;
	public int getIdMarcacionCambio() {
		return idMarcacionCambio;
	}
	public void setIdMarcacionCambio(int idMarcacionCambio) {
		this.idMarcacionCambio = idMarcacionCambio;
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
	public double getTotalNetoContact() {
		return totalNetoContact;
	}
	public void setTotalNetoContact(double totalNetoContact) {
		this.totalNetoContact = totalNetoContact;
	}
	public double getTotalNetoTienda() {
		return totalNetoTienda;
	}
	public void setTotalNetoTienda(double totalNetoTienda) {
		this.totalNetoTienda = totalNetoTienda;
	}
	public MarcacionCambioPedido(int idMarcacionCambio, int idMarcacion, int idPedido, int numPosHeader,
			String fechaPedido, double totalNetoContact, double totalNetoTienda) {
		super();
		this.idMarcacionCambio = idMarcacionCambio;
		this.idMarcacion = idMarcacion;
		this.idPedido = idPedido;
		this.numPosHeader = numPosHeader;
		this.fechaPedido = fechaPedido;
		this.totalNetoContact = totalNetoContact;
		this.totalNetoTienda = totalNetoTienda;
	}
	
	
	

}
