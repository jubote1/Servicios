package Modelo;

/**
 * Clase que implementa la entidad pedido.
 * @author JuanDavid
 *
 */
public class Pedido {
	
	private int idpedido;
	private int idtienda;
	private String nombretienda;
	private double totalbruto;
	private double impuesto;
	private double total_neto;
	private int idestadopedido;
	private String estadopedido;
	private String fechapedido;
	private int idcliente;
	private String nombrecliente;
	private int enviadoPixel;
	private int numposheader;
	private Tienda tienda;
	private String stringpixel;
	private String fechainsercion;
	private String usuariopedido;
	private String direccion;
	private String telefono;
	private String formapago;
	private int idformapago;
	private double tiempopedido;
	private String obsMarcacion;
	
	
	
	
	public String getObsMarcacion() {
		return obsMarcacion;
	}
	public void setObsMarcacion(String obsMarcacion) {
		this.obsMarcacion = obsMarcacion;
	}
	public double getTiempopedido() {
		return tiempopedido;
	}
	public void setTiempopedido(double tiempopedido) {
		this.tiempopedido = tiempopedido;
	}
	public int getIdformapago() {
		return idformapago;
	}
	public void setIdformapago(int idformapago) {
		this.idformapago = idformapago;
	}
	public String getFormapago() {
		return formapago;
	}
	public void setFormapago(String formapago) {
		this.formapago = formapago;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	public String getFechainsercion() {
		return fechainsercion;
	}
	public void setFechainsercion(String fechainsercion) {
		this.fechainsercion = fechainsercion;
	}
	public String getUsuariopedido() {
		return usuariopedido;
	}
	public void setUsuariopedido(String usuariopedido) {
		this.usuariopedido = usuariopedido;
	}
	public String getStringpixel() {
		return stringpixel;
	}
	public void setStringpixel(String stringpixel) {
		this.stringpixel = stringpixel;
	}
	public Tienda getTienda() {
		return tienda;
	}
	public void setTienda(Tienda tienda) {
		this.tienda = tienda;
	}
	public int getEnviadoPixel() {
		return enviadoPixel;
	}
	public void setEnviadoPixel(int enviadoPixel) {
		this.enviadoPixel = enviadoPixel;
	}
	public int getNumposheader() {
		return numposheader;
	}
	public void setNumposheader(int numposheader) {
		this.numposheader = numposheader;
	}
	public int getIdpedido() {
		return idpedido;
	}
	public void setIdpedido(int idpedido) {
		this.idpedido = idpedido;
	}
	public int getIdtienda() {
		return idtienda;
	}
	public void setIdtienda(int idtienda) {
		this.idtienda = idtienda;
	}
	public String getNombretienda() {
		return nombretienda;
	}
	public void setNombretienda(String nombretienda) {
		this.nombretienda = nombretienda;
	}
	public double getTotalbruto() {
		return totalbruto;
	}
	public void setTotalbruto(double totalbruto) {
		this.totalbruto = totalbruto;
	}
	public double getImpuesto() {
		return impuesto;
	}
	public void setImpuesto(double impuesto) {
		this.impuesto = impuesto;
	}
	public double getTotal_neto() {
		return total_neto;
	}
	public void setTotal_neto(double total_neto) {
		this.total_neto = total_neto;
	}
	public int getIdestadopedido() {
		return idestadopedido;
	}
	public void setIdestadopedido(int idestadopedido) {
		this.idestadopedido = idestadopedido;
	}
	public String getEstadopedido() {
		return estadopedido;
	}
	public void setEstadopedido(String estadopedido) {
		this.estadopedido = estadopedido;
	}
	public String getFechapedido() {
		return fechapedido;
	}
	public void setFechapedido(String fechapedido) {
		this.fechapedido = fechapedido;
	}
	 
	
	
	public int getIdcliente() {
		return idcliente;
	}
	public void setIdcliente(int idcliente) {
		this.idcliente = idcliente;
	}
	public String getNombrecliente() {
		return nombrecliente;
	}
	public void setNombrecliente(String nombrecliente) {
		this.nombrecliente = nombrecliente;
	}
	public Pedido(int idpedido, String nombretienda, double totalbruto, double impuesto, double total_neto,
			String estadopedido, String fechapedido, String nombrecliente, int idcliente, int enviadopixel, int numposheader) {
		super();
		this.idpedido = idpedido;
		this.nombretienda = nombretienda;
		this.totalbruto = totalbruto;
		this.impuesto = impuesto;
		this.total_neto = total_neto;
		this.estadopedido = estadopedido;
		this.fechapedido = fechapedido;
		this.nombrecliente = nombrecliente;
		this.idcliente = idcliente;
		this.enviadoPixel = enviadopixel;
		this.numposheader = numposheader;
	}
	
	public Pedido(int idpedido, String nombretienda, double totalbruto, double impuesto, double total_neto,
			String estadopedido, String fechapedido, String nombrecliente, int idcliente, int enviadopixel, int numposheader, Tienda tienda, String stringpixel, String fechainsercion, String usuariopedido, String direccion, String telefono, String formapago, int idformapago, double tiempopedido) {
		super();
		this.idpedido = idpedido;
		this.nombretienda = nombretienda;
		this.totalbruto = totalbruto;
		this.impuesto = impuesto;
		this.total_neto = total_neto;
		this.estadopedido = estadopedido;
		this.fechapedido = fechapedido;
		this.nombrecliente = nombrecliente;
		this.idcliente = idcliente;
		this.enviadoPixel = enviadopixel;
		this.numposheader = numposheader;
		this.tienda = tienda;
		this.stringpixel = stringpixel;
		this.fechainsercion = fechainsercion;
		this.usuariopedido = usuariopedido;
		this.direccion = direccion;
		this.telefono = telefono;
		this.formapago = formapago;
		this.idformapago = idformapago;
		this.tiempopedido = tiempopedido;
	}
	
	public Pedido()
	{
		
	}
	

}
