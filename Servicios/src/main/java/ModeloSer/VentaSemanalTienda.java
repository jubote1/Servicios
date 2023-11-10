package ModeloSer;

public class VentaSemanalTienda {
	
	private int idTienda;
	private String fecha;
	private double valor;
	private double meta;
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
	
	public double getMeta() {
		return meta;
	}
	public void setMeta(double meta) {
		this.meta = meta;
	}
	public VentaSemanalTienda(int idTienda, String fecha, double valor, double meta) {
		super();
		this.idTienda = idTienda;
		this.fecha = fecha;
		this.valor = valor;
		this.meta = meta;
	}
	
	

}
