package ModeloSer;

public class EstadisticaProducto {
	private int idTienda;
	private String fecha;
	private String descripcion;
	private double cantidad;
	private double total;
	private String tamano;
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
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public double getCantidad() {
		return cantidad;
	}
	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public String getTamano() {
		return tamano;
	}
	public void setTamano(String tamano) {
		this.tamano = tamano;
	}
	public EstadisticaProducto(int idTienda, String fecha, String descripcion, double cantidad, double total,
			String tamano) {
		super();
		this.idTienda = idTienda;
		this.fecha = fecha;
		this.descripcion = descripcion;
		this.cantidad = cantidad;
		this.total = total;
		this.tamano = tamano;
	}
	public EstadisticaProducto() {
		super();
	}
	
	
}
