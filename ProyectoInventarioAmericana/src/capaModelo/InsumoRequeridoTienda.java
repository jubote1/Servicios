package capaModelo;

public class InsumoRequeridoTienda {
	
	private int idinsumo;
	private int idtienda;
	private double cantidad;
	private int diasemana;
	private String unidadMedida;
	private String manejacanasta;
	private int cantidadxcanasta;
	private String nombrecontenedor;
	private double cantidadMinima;
	private String nombreInsumo;
	
	
	
	public String getNombreInsumo() {
		return nombreInsumo;
	}
	public void setNombreInsumo(String nombreInsumo) {
		this.nombreInsumo = nombreInsumo;
	}
	public double getCantidadMinima() {
		return cantidadMinima;
	}
	public void setCantidadMinima(double cantidadMinima) {
		this.cantidadMinima = cantidadMinima;
	}
	public String getNombrecontenedor() {
		return nombrecontenedor;
	}
	public void setNombrecontenedor(String nombrecontenedor) {
		this.nombrecontenedor = nombrecontenedor;
	}
	public String getManejacanasta() {
		return manejacanasta;
	}
	public void setManejacanasta(String manejacanasta) {
		this.manejacanasta = manejacanasta;
	}
	public int getCantidadxcanasta() {
		return cantidadxcanasta;
	}
	public void setCantidadxcanasta(int cantidadxcanasta) {
		this.cantidadxcanasta = cantidadxcanasta;
	}
	public String getUnidadMedida() {
		return unidadMedida;
	}
	public void setUnidadMedida(String unidadMedida) {
		this.unidadMedida = unidadMedida;
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
	public int getDiasemana() {
		return diasemana;
	}
	public void setDiasemana(int diasemana) {
		this.diasemana = diasemana;
	}
	public InsumoRequeridoTienda(int idinsumo, int idtienda, double cantidad, int diasemana, String unidadMedida,
			String manejacanasta, int cantidadxcanasta, String nombrecontenedor, double cantidadMinima) {
		super();
		this.idinsumo = idinsumo;
		this.idtienda = idtienda;
		this.cantidad = cantidad;
		this.diasemana = diasemana;
		this.unidadMedida = unidadMedida;
		this.manejacanasta = manejacanasta;
		this.cantidadxcanasta = cantidadxcanasta;
		this.nombrecontenedor = nombrecontenedor;
		this.cantidadMinima = cantidadMinima;
	}
	
	
	
	

}
