package ModeloSer;

public class ProcesoAutomatico {
	
	private int idProceso;
	private String nombreProceso;
	private String tipoProceso;
	private String tipoEjecucion;
	private String horaInicio;
	private String horaFinal;
	private String ejeLun;
	private String ejeMar;
	private String ejeMie;
	private String ejeJue;
	private String ejeVie;
	private String ejeSab;
	private String ejeDom;
	public int getIdProceso() {
		return idProceso;
	}
	public void setIdProceso(int idProceso) {
		this.idProceso = idProceso;
	}
	public String getNombreProceso() {
		return nombreProceso;
	}
	public void setNombreProceso(String nombreProceso) {
		this.nombreProceso = nombreProceso;
	}
	public String getTipoProceso() {
		return tipoProceso;
	}
	public void setTipoProceso(String tipoProceso) {
		this.tipoProceso = tipoProceso;
	}
	public String getTipoEjecucion() {
		return tipoEjecucion;
	}
	public void setTipoEjecucion(String tipoEjecucion) {
		this.tipoEjecucion = tipoEjecucion;
	}
	public String getHoraInicio() {
		return horaInicio;
	}
	public void setHoraInicio(String horaInicio) {
		this.horaInicio = horaInicio;
	}
	public String getHoraFinal() {
		return horaFinal;
	}
	public void setHoraFinal(String horaFinal) {
		this.horaFinal = horaFinal;
	}
	public String getEjeLun() {
		return ejeLun;
	}
	public void setEjeLun(String ejeLun) {
		this.ejeLun = ejeLun;
	}
	public String getEjeMar() {
		return ejeMar;
	}
	public void setEjeMar(String ejeMar) {
		this.ejeMar = ejeMar;
	}
	public String getEjeMie() {
		return ejeMie;
	}
	public void setEjeMie(String ejeMie) {
		this.ejeMie = ejeMie;
	}
	public String getEjeJue() {
		return ejeJue;
	}
	public void setEjeJue(String ejeJue) {
		this.ejeJue = ejeJue;
	}
	public String getEjeVie() {
		return ejeVie;
	}
	public void setEjeVie(String ejeVie) {
		this.ejeVie = ejeVie;
	}
	public String getEjeSab() {
		return ejeSab;
	}
	public void setEjeSab(String ejeSab) {
		this.ejeSab = ejeSab;
	}
	public String getEjeDom() {
		return ejeDom;
	}
	public void setEjeDom(String ejeDom) {
		this.ejeDom = ejeDom;
	}
	public ProcesoAutomatico(int idProceso, String nombreProceso, String tipoProceso, String tipoEjecucion,
			String horaInicio, String horaFinal, String ejeLun, String ejeMar, String ejeMie, String ejeJue,
			String ejeVie, String ejeSab, String ejeDom) {
		super();
		this.idProceso = idProceso;
		this.nombreProceso = nombreProceso;
		this.tipoProceso = tipoProceso;
		this.tipoEjecucion = tipoEjecucion;
		this.horaInicio = horaInicio;
		this.horaFinal = horaFinal;
		this.ejeLun = ejeLun;
		this.ejeMar = ejeMar;
		this.ejeMie = ejeMie;
		this.ejeJue = ejeJue;
		this.ejeVie = ejeVie;
		this.ejeSab = ejeSab;
		this.ejeDom = ejeDom;
	}
	
	
	

}
