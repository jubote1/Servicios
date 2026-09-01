package ModeloSer;

public class ResultadoRuleta {
	
	private int idPedido;
	private String tienda;
	private String premio;
	private String telefono;
	private String correo;
	private String nombreCliente;
	public int getIdPedido() {
		return idPedido;
	}
	public void setIdPedido(int idPedido) {
		this.idPedido = idPedido;
	}
	public String getTienda() {
		return tienda;
	}
	public void setTienda(String tienda) {
		this.tienda = tienda;
	}
	public String getPremio() {
		return premio;
	}
	public void setPremio(String premio) {
		this.premio = premio;
	}
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	public String getCorreo() {
		return correo;
	}
	public void setCorreo(String correo) {
		this.correo = correo;
	}
	public String getNombreCliente() {
		return nombreCliente;
	}
	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}
	public ResultadoRuleta(int idPedido, String tienda, String premio, String telefono, String correo,
			String nombreCliente) {
		super();
		this.idPedido = idPedido;
		this.tienda = tienda;
		this.premio = premio;
		this.telefono = telefono;
		this.correo = correo;
		this.nombreCliente = nombreCliente;
	}
	
}
