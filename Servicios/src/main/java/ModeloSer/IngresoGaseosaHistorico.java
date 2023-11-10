package ModeloSer;

public class IngresoGaseosaHistorico {
	
	private int idIngresoInventario;
	private int idItem;
	private String nombreItem;
	private double cantidad;
	private String fechaSistema;
	public int getIdIngresoInventario() {
		return idIngresoInventario;
	}
	public void setIdIngresoInventario(int idIngresoInventario) {
		this.idIngresoInventario = idIngresoInventario;
	}
	public int getIdItem() {
		return idItem;
	}
	public void setIdItem(int idItem) {
		this.idItem = idItem;
	}
	public String getNombreItem() {
		return nombreItem;
	}
	public void setNombreItem(String nombreItem) {
		this.nombreItem = nombreItem;
	}
	public double getCantidad() {
		return cantidad;
	}
	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}
	public String getFechaSistema() {
		return fechaSistema;
	}
	public void setFechaSistema(String fechaSistema) {
		this.fechaSistema = fechaSistema;
	}
	public IngresoGaseosaHistorico(int idIngresoInventario, int idItem, String nombreItem, double cantidad,
			String fechaSistema) {
		super();
		this.idIngresoInventario = idIngresoInventario;
		this.idItem = idItem;
		this.nombreItem = nombreItem;
		this.cantidad = cantidad;
		this.fechaSistema = fechaSistema;
	}
	
}
