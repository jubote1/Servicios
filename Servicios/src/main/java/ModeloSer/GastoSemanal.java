package ModeloSer;

public class GastoSemanal {
	
	private int idGastoSemanal;
	private int idTienda;
	private int idGastoConf;
	private String fecha;
	private double valorCalculo;
	private double valorGasto;
	public int getIdGastoSemanal() {
		return idGastoSemanal;
	}
	public void setIdGastoSemanal(int idGastoSemanal) {
		this.idGastoSemanal = idGastoSemanal;
	}
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public int getIdGastoConf() {
		return idGastoConf;
	}
	public void setIdGastoConf(int idGastoConf) {
		this.idGastoConf = idGastoConf;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public double getValorCalculo() {
		return valorCalculo;
	}
	public void setValorCalculo(double valorCalculo) {
		this.valorCalculo = valorCalculo;
	}
	public double getValorGasto() {
		return valorGasto;
	}
	public void setValorGasto(double valorGasto) {
		this.valorGasto = valorGasto;
	}
	public GastoSemanal(int idGastoSemanal, int idTienda, int idGastoConf, String fecha, double valorCalculo,
			double valorGasto) {
		super();
		this.idGastoSemanal = idGastoSemanal;
		this.idTienda = idTienda;
		this.idGastoConf = idGastoConf;
		this.fecha = fecha;
		this.valorCalculo = valorCalculo;
		this.valorGasto = valorGasto;
	}
	
	

}
