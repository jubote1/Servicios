package ModeloSer;

public class CierreDatafonoPOS {
	private int idTienda;
	private String fecha;
	private double valorCierre;
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
	public double getValorCierre() {
		return valorCierre;
	}
	public void setValorCierre(double valorCierre) {
		this.valorCierre = valorCierre;
	}
	public CierreDatafonoPOS(int idTienda, String fecha, double valorCierre) {
		super();
		this.idTienda = idTienda;
		this.fecha = fecha;
		this.valorCierre = valorCierre;
	}
	
}
