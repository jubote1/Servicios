package Modelo;

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
	private int pos;
	private String hostBD;
		
	
	
	public String getHostBD() {
		return hostBD;
	}
	public void setHostBD(String hostBD) {
		this.hostBD = hostBD;
	}
	public int getPos() {
		return pos;
	}
	public void setPos(int pos) {
		this.pos = pos;
	}
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
	
	
	public Tienda(int idTienda, String nombreTienda, String dsnTienda, String url, int pos) {
		super();
		this.idTienda = idTienda;
		this.nombreTienda = nombreTienda;
		this.dsnTienda = dsnTienda;
		this.url = url;
		this.pos = pos;
	}
	
	public Tienda()
	{
		
	}
	
	

}
