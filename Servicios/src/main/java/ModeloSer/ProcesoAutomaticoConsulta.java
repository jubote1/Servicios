package ModeloSer;

public class ProcesoAutomaticoConsulta {

	private int idProcesoConsulta;
	private int idProceso;
	private String consulta;
	private String baseDatos;
	private String descripcion;
	public int getIdProcesoConsulta() {
		return idProcesoConsulta;
	}
	public void setIdProcesoConsulta(int idProcesoConsulta) {
		this.idProcesoConsulta = idProcesoConsulta;
	}
	public int getIdProceso() {
		return idProceso;
	}
	public void setIdProceso(int idProceso) {
		this.idProceso = idProceso;
	}
	public String getConsulta() {
		return consulta;
	}
	public void setConsulta(String consulta) {
		this.consulta = consulta;
	}
	public String getBaseDatos() {
		return baseDatos;
	}
	public void setBaseDatos(String baseDatos) {
		this.baseDatos = baseDatos;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public ProcesoAutomaticoConsulta(int idProcesoConsulta, int idProceso, String consulta, String baseDatos,
			String descripcion) {
		super();
		this.idProcesoConsulta = idProcesoConsulta;
		this.idProceso = idProceso;
		this.consulta = consulta;
		this.baseDatos = baseDatos;
		this.descripcion = descripcion;
	}
	
	
	
	
}
