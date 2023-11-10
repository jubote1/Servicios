package ModeloSer;

public class TicketPromedioMes {
	
	private int idTienda;
	private int mes;
	private int ano;
	private double valor;
	private int cantidad;
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public int getMes() {
		return mes;
	}
	public void setMes(int mes) {
		this.mes = mes;
	}
	public int getAno() {
		return ano;
	}
	public void setAno(int ano) {
		this.ano = ano;
	}
	public double getValor() {
		return valor;
	}
	public void setValor(double valor) {
		this.valor = valor;
	}
	public int getCantidad() {
		return cantidad;
	}
	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}
	public TicketPromedioMes(int idTienda, int mes, int ano, double valor, int cantidad) {
		super();
		this.idTienda = idTienda;
		this.mes = mes;
		this.ano = ano;
		this.valor = valor;
		this.cantidad = cantidad;
	}
	
	

}
