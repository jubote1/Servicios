package Modelo;

public class InsumoDespachoTienda {
	
	
	private int idDespacho;
	private int idTienda;
	private String fechaDespacho;
	private String fechaReal;
	private String estado;
	private String observacion;
	
	
	public String getObservacion() {
		return observacion;
	}
	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
	public int getIdDespacho() {
		return idDespacho;
	}
	public void setIdDespacho(int idDespacho) {
		this.idDespacho = idDespacho;
	}
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public String getFechaDespacho() {
		return fechaDespacho;
	}
	public void setFechaDespacho(String fechaDespacho) {
		this.fechaDespacho = fechaDespacho;
	}
	public String getFechaReal() {
		return fechaReal;
	}
	public void setFechaReal(String fechaReal) {
		this.fechaReal = fechaReal;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public InsumoDespachoTienda(int idDespacho, int idTienda, String fechaDespacho, String fechaReal, String estado, String observacion) {
		super();
		this.idDespacho = idDespacho;
		this.idTienda = idTienda;
		this.fechaDespacho = fechaDespacho;
		this.fechaReal = fechaReal;
		this.estado = estado;
		this.observacion = observacion;
	}
	
	
	

}
