package capaModelo;

public class InsumoDespachadoTienda {
	
	private int idinsumo;
	private String nombreInsumo;
	private double cantidadTienda;
	private double cantidadSurtir;
	private String contenedor;
	private String unidadMedida;
	public int getIdinsumo() {
		return idinsumo;
	}
	public void setIdinsumo(int idinsumo) {
		this.idinsumo = idinsumo;
	}
	public String getNombreInsumo() {
		return nombreInsumo;
	}
	public void setNombreInsumo(String nombreInsumo) {
		this.nombreInsumo = nombreInsumo;
	}
	
	public double getCantidadTienda() {
		return cantidadTienda;
	}
	public void setCantidadTienda(double cantidadTienda) {
		this.cantidadTienda = cantidadTienda;
	}
	public double getCantidadSurtir() {
		return cantidadSurtir;
	}
	public void setCantidadSurtir(double cantidadSurtir) {
		this.cantidadSurtir = cantidadSurtir;
	}
	
	public String getContenedor() {
		return contenedor;
	}
	public void setNombreContenedor(String contenedor) {
		this.contenedor = contenedor;
	}
	public String getUnidadMedida() {
		return unidadMedida;
	}
	public void setUnidadMedida(String unidadMedida) {
		this.unidadMedida = unidadMedida;
	}
	public InsumoDespachadoTienda(int idinsumo, String nombreInsumo, double cantidadTienda, double cantidadSurtir,
			String contenedor, String unidadMedida) {
		super();
		this.idinsumo = idinsumo;
		this.nombreInsumo = nombreInsumo;
		this.cantidadTienda = cantidadTienda;
		this.cantidadSurtir = cantidadSurtir;
		this.contenedor = contenedor;
		this.unidadMedida = unidadMedida;
	}

	
	
	
	
	
	

}
