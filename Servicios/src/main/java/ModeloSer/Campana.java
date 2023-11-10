package ModeloSer;

public class Campana {
	
	private int idCampana;
	private String nombreCampana;
	private String query;
	private String plantilla;
	private String mensajeTexto;
	
	
	
	public String getMensajeTexto() {
		return mensajeTexto;
	}
	public void setMensajeTexto(String mensajeTexto) {
		this.mensajeTexto = mensajeTexto;
	}
	public int getIdCampana() {
		return idCampana;
	}
	public void setIdCampana(int idCampana) {
		this.idCampana = idCampana;
	}
	public String getNombreCampana() {
		return nombreCampana;
	}
	public void setNombreCampana(String nombreCampana) {
		this.nombreCampana = nombreCampana;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getPlantilla() {
		return plantilla;
	}
	public void setPlantilla(String plantilla) {
		this.plantilla = plantilla;
	}
	public Campana(int idCampana, String nombreCampana, String query, String plantilla, String mensajeTexto) {
		super();
		this.idCampana = idCampana;
		this.nombreCampana = nombreCampana;
		this.query = query;
		this.plantilla = plantilla;
		this.mensajeTexto = mensajeTexto;
	}
	
	
}
