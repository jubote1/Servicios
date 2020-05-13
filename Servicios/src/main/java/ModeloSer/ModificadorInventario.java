package ModeloSer;

public class ModificadorInventario {
	
	private int idItem;
	private double cantidad;
	public int getIdItem() {
		return idItem;
	}
	public void setIdItem(int idItem) {
		this.idItem = idItem;
	}
	public double getCantidad() {
		return cantidad;
	}
	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}
	public ModificadorInventario(int idItem, double cantidad) {
		super();
		this.idItem = idItem;
		this.cantidad = cantidad;
	}
	
	
	

}
