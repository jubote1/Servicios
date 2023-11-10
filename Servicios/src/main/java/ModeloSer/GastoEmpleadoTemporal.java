package ModeloSer;

public class GastoEmpleadoTemporal {
	
	private int idTienda;
	private String fecha;
	private double valor;
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public double getValor() {
		return valor;
	}
	public void setValor(double valor) {
		this.valor = valor;
	}
	public GastoEmpleadoTemporal(int idTienda, String fecha, double valor) {
		super();
		this.idTienda = idTienda;
		this.fecha = fecha;
		this.valor = valor;
	}
	
}
