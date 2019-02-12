package capaModelo;

/**
 * Clase que implementa la entidad Tienda.
 * 
 * @author JuanDavid
 *
 */
public class Tienda {
	
	private int idTienda;
	private String nombreTienda;
	private String dsnTienda;
	private String url;
	
		
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public String getNombreTienda() {
		return nombreTienda;
	}
	public void setNombreTienda(String nombreTienda) {
		this.nombreTienda = nombreTienda;
	}
	public String getDsnTienda() {
		return dsnTienda;
	}
	public void setDsnTienda(String dsnTienda) {
		this.dsnTienda = dsnTienda;
	}
	
	public Tienda(int idTienda, String nombreTienda, String dsnTienda) {
		super();
		this.idTienda = idTienda;
		this.nombreTienda = nombreTienda;
		this.dsnTienda = dsnTienda;
	}
	
	public Tienda(int idTienda, String nombreTienda, String dsnTienda, String url) {
		super();
		this.idTienda = idTienda;
		this.nombreTienda = nombreTienda;
		this.dsnTienda = dsnTienda;
		this.url = url;
	}
	
	public Tienda()
	{
		
	}
	
	

}
