package capaModelo;

public class Insumo {
	
	private int idinsumo;
	private String nombre;
	private String unidadMedida;
	private double precioUnidad;
	private String manejacanasta;
	private int cantidaxcanasta;
	
	
	public String getManejacanasta() {
		return manejacanasta;
	}
	public void setManejacanasta(String manejacanasta) {
		this.manejacanasta = manejacanasta;
	}
	public int getCantidaxcanasta() {
		return cantidaxcanasta;
	}
	public void setCantidaxcanasta(int cantidaxcanasta) {
		this.cantidaxcanasta = cantidaxcanasta;
	}
	public int getIdinsumo() {
		return idinsumo;
	}
	public void setIdinsumo(int idinsumo) {
		this.idinsumo = idinsumo;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getUnidadMedida() {
		return unidadMedida;
	}
	public void setUnidadMedida(String unidadMedida) {
		this.unidadMedida = unidadMedida;
	}
	public double getPrecioUnidad() {
		return precioUnidad;
	}
	public void setPrecioUnidad(double precioUnidad) {
		this.precioUnidad = precioUnidad;
	}
	public Insumo(int idinsumo, String nombre, String unidadMedida, double precioUnidad, String manejacanasta,
			int cantidaxcanasta) {
		super();
		this.idinsumo = idinsumo;
		this.nombre = nombre;
		this.unidadMedida = unidadMedida;
		this.precioUnidad = precioUnidad;
		this.manejacanasta = manejacanasta;
		this.cantidaxcanasta = cantidaxcanasta;
	}
	
	
	
	
	
}
