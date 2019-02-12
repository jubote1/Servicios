package capaModelo;

public class ClienteFiel {
	
	private int idCliente;
	private String nombreCliente;
	private int numeroPedidos;
	private String fechaMaxima;
	private String fechaMinima;
	private String telefono;
	private String nombreTienda;
	private int ofertas;
	private int ofertasVigentes;
	
	
	
	public int getOfertas() {
		return ofertas;
	}
	public void setOfertas(int ofertas) {
		this.ofertas = ofertas;
	}
	public int getOfertasVigentes() {
		return ofertasVigentes;
	}
	public void setOfertasVigentes(int ofertasVigentes) {
		this.ofertasVigentes = ofertasVigentes;
	}
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	public String getNombreTienda() {
		return nombreTienda;
	}
	public void setNombreTienda(String nombreTienda) {
		this.nombreTienda = nombreTienda;
	}
	public int getIdCliente() {
		return idCliente;
	}
	public void setIdCliente(int idCliente) {
		this.idCliente = idCliente;
	}
	public String getNombreCliente() {
		return nombreCliente;
	}
	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}
	public int getNumeroPedidos() {
		return numeroPedidos;
	}
	public void setNumeroPedidos(int numeroPedidos) {
		this.numeroPedidos = numeroPedidos;
	}
	public String getFechaMaxima() {
		return fechaMaxima;
	}
	public void setFechaMaxima(String fechaMaxima) {
		this.fechaMaxima = fechaMaxima;
	}
	public String getFechaMinima() {
		return fechaMinima;
	}
	public void setFechaMinima(String fechaMinima) {
		this.fechaMinima = fechaMinima;
	}
	public ClienteFiel(int idCliente, String nombreCliente, int numeroPedidos, String fechaMaxima, String fechaMinima,
			String telefono, String nombreTienda, int ofertas, int ofertasVigentes) {
		super();
		this.idCliente = idCliente;
		this.nombreCliente = nombreCliente;
		this.numeroPedidos = numeroPedidos;
		this.fechaMaxima = fechaMaxima;
		this.fechaMinima = fechaMinima;
		this.telefono = telefono;
		this.nombreTienda = nombreTienda;
		this.ofertas = ofertas;
		this.ofertasVigentes = ofertasVigentes;
	}

	
	
	
	
}
