package ModeloSer;

public class InsumoDespachoTiendaDetalle {
	
	private int idDespachoDetalle;
	private int idDespacho;
	private int idInsumo;
	private double cantidad;
	public int getIdDespachoDetalle() {
		return idDespachoDetalle;
	}
	public void setIdDespachoDetalle(int idDespachoDetalle) {
		this.idDespachoDetalle = idDespachoDetalle;
	}
	public int getIdDespacho() {
		return idDespacho;
	}
	public void setIdDespacho(int idDespacho) {
		this.idDespacho = idDespacho;
	}
	public int getIdInsumo() {
		return idInsumo;
	}
	public void setIdInsumo(int idInsumo) {
		this.idInsumo = idInsumo;
	}
	public double getCantidad() {
		return cantidad;
	}
	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}
	public InsumoDespachoTiendaDetalle(int idDespachoDetalle, int idDespacho, int idInsumo, double cantidad) {
		super();
		this.idDespachoDetalle = idDespachoDetalle;
		this.idDespacho = idDespacho;
		this.idInsumo = idInsumo;
		this.cantidad = cantidad;
	}
	
	

}
