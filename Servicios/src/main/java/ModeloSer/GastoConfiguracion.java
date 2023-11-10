package ModeloSer;

public class GastoConfiguracion {
	
	private int idGastoConf;
	private String nombreGasto;
	private String consultaSQL;
	private double porcentajeGasto;
	private String origen;
	public int getIdGastoConf() {
		return idGastoConf;
	}
	public void setIdGastoConf(int idGastoConf) {
		this.idGastoConf = idGastoConf;
	}
	public String getNombreGasto() {
		return nombreGasto;
	}
	public void setNombreGasto(String nombreGasto) {
		this.nombreGasto = nombreGasto;
	}
	public String getConsultaSQL() {
		return consultaSQL;
	}
	public void setConsultaSQL(String consultaSQL) {
		this.consultaSQL = consultaSQL;
	}
	public double getPorcentajeGasto() {
		return porcentajeGasto;
	}
	public void setPorcentajeGasto(double porcentajeGasto) {
		this.porcentajeGasto = porcentajeGasto;
	}
	public String getOrigen() {
		return origen;
	}
	public void setOrigen(String origen) {
		this.origen = origen;
	}
	public GastoConfiguracion(int idGastoConf, String nombreGasto, String consultaSQL, double porcentajeGasto,
			String origen) {
		super();
		this.idGastoConf = idGastoConf;
		this.nombreGasto = nombreGasto;
		this.consultaSQL = consultaSQL;
		this.porcentajeGasto = porcentajeGasto;
		this.origen = origen;
	}
	public GastoConfiguracion() {
		super();
	}
	
	
	
	
}
