package ModeloSer;

public class ProcesoAutomaticoParametro {
	
	private int idProcesoParametro;
	private int idProcesoConsulta;
	private int idParametro;
	private String tipoParametro;
	private double restarValor;
	public int getIdProcesoParametro() {
		return idProcesoParametro;
	}
	public void setIdProcesoParametro(int idProcesoParametro) {
		this.idProcesoParametro = idProcesoParametro;
	}
	public int getIdProcesoConsulta() {
		return idProcesoConsulta;
	}
	public void setIdProcesoConsulta(int idProcesoConsulta) {
		this.idProcesoConsulta = idProcesoConsulta;
	}
	public int getIdParametro() {
		return idParametro;
	}
	public void setIdParametro(int idParametro) {
		this.idParametro = idParametro;
	}
	public String getTipoParametro() {
		return tipoParametro;
	}
	public void setTipoParametro(String tipoParametro) {
		this.tipoParametro = tipoParametro;
	}
	public double getRestarValor() {
		return restarValor;
	}
	public void setRestarValor(double restarValor) {
		this.restarValor = restarValor;
	}
	
	public ProcesoAutomaticoParametro(int idProcesoParametro, int idProcesoConsulta, int idParametro,
			String tipoParametro, double restarValor) {
		super();
		this.idProcesoParametro = idProcesoParametro;
		this.idProcesoConsulta = idProcesoConsulta;
		this.idParametro = idParametro;
		this.tipoParametro = tipoParametro;
		this.restarValor = restarValor;
	}
	
	

}
