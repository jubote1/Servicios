package capaModelo;

public class RazonSocial {

	private int idRazon;
	private String nombreRazon;
	private String identificacion;
	public int getIdRazon() {
		return idRazon;
	}
	public void setIdRazon(int idRazon) {
		this.idRazon = idRazon;
	}
	public String getNombreRazon() {
		return nombreRazon;
	}
	public void setNombreRazon(String nombreRazon) {
		this.nombreRazon = nombreRazon;
	}
	public String getIdentificacion() {
		return identificacion;
	}
	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
	}
	public RazonSocial(int idRazon, String nombreRazon, String identificacion) {
		super();
		this.idRazon = idRazon;
		this.nombreRazon = nombreRazon;
		this.identificacion = identificacion;
	}
	
	
	
}
