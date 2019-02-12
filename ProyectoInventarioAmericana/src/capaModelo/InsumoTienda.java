package capaModelo;

public class InsumoTienda {

	private int idinsumo;
	private String nombreInsumo;
	private int idtienda;
	private double cantidad;
	private String fecha;
	private int controlCantidad;
	
	
	public int getControlCantidad() {
		return controlCantidad;
	}
	public void setControlCantidad(int controlCantidad) {
		this.controlCantidad = controlCantidad;
	}
	public int getIdinsumo() {
		return idinsumo;
	}
	public void setIdinsumo(int idinsumo) {
		this.idinsumo = idinsumo;
	}
	public int getIdtienda() {
		return idtienda;
	}
	public void setIdtienda(int idtienda) {
		this.idtienda = idtienda;
	}
	public double getCantidad() {
		return cantidad;
	}
	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	
	
	public String getNombreInsumo() {
		return nombreInsumo;
	}
	public void setNombreInsumo(String nombreInsumo) {
		this.nombreInsumo = nombreInsumo;
	}
	public InsumoTienda(int idinsumo, int idtienda, double cantidad, String fecha, String nombreInsumo, int controlCantidad) {
		super();
		this.idinsumo = idinsumo;
		this.idtienda = idtienda;
		this.cantidad = cantidad;
		this.fecha = fecha;
		this.nombreInsumo = nombreInsumo;
		this.controlCantidad = controlCantidad;
	}
	
	
}
