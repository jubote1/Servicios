package Modelo;

public class Insumo {
	
	private int idinsumo;
	private String nombre;
	private String unidadMedida;
	private double precioUnidad;
	private String manejacanasta;
	private int cantidaxcanasta;
	private String nombreContenedor;
	private String categoria;
	private boolean control_cantidad;
	private double costoUnidad;
	
	
	
	public String getNombreContenedor() {
		return nombreContenedor;
	}
	public void setNombreContenedor(String nombreContenedor) {
		this.nombreContenedor = nombreContenedor;
	}
	public String getCategoria() {
		return categoria;
	}
	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}
	public boolean isControl_cantidad() {
		return control_cantidad;
	}
	public void setControl_cantidad(boolean control_cantidad) {
		this.control_cantidad = control_cantidad;
	}
	public double getCostoUnidad() {
		return costoUnidad;
	}
	public void setCostoUnidad(double costoUnidad) {
		this.costoUnidad = costoUnidad;
	}
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
	public Insumo(int idinsumo, String nombre, String unidadMedida, String categoria, double costoUnidad) {
		super();
		this.idinsumo = idinsumo;
		this.nombre = nombre;
		this.unidadMedida = unidadMedida;
		this.categoria = categoria;
		this.costoUnidad = costoUnidad;
	}

	
	
	
	
	
	
}
