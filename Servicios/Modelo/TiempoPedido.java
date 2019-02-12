package Modelo;

public class TiempoPedido {
	
	private int idtienda;
	private String Tienda;
	private int minutosPedido;
	public int getIdtienda() {
		return idtienda;
	}
	public void setIdtienda(int idtienda) {
		this.idtienda = idtienda;
	}
	public String getTienda() {
		return Tienda;
	}
	public void setTienda(String tienda) {
		Tienda = tienda;
	}
	public int getMinutosPedido() {
		return minutosPedido;
	}
	public void setMinutosPedido(int minutosPedido) {
		this.minutosPedido = minutosPedido;
	}
	public TiempoPedido(int idtienda, String tienda, int minutosPedido) {
		super();
		this.idtienda = idtienda;
		Tienda = tienda;
		this.minutosPedido = minutosPedido;
	}
	
	
	

}
