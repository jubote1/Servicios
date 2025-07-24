package ModeloSer;

public class EstadisticaVentaProducto {
	
	private int idProducto;
	private String periodicidad;
	public int getIdProducto() {
		return idProducto;
	}
	public void setIdProducto(int idProducto) {
		this.idProducto = idProducto;
	}
	public String getPeriodicidad() {
		return periodicidad;
	}
	public void setPeriodicidad(String periodicidad) {
		this.periodicidad = periodicidad;
	}
	public EstadisticaVentaProducto(int idProducto, String periodicidad) {
		super();
		this.idProducto = idProducto;
		this.periodicidad = periodicidad;
	}
	
	

}
