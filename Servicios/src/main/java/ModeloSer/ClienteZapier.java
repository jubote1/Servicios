package ModeloSer;

public class ClienteZapier {
	
	private String telefono;
	private String nombre;
	private String codigo;
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	public ClienteZapier(String telefono, String nombre, String codigo) {
		super();
		this.telefono = telefono;
		this.nombre = nombre;
		this.codigo = codigo;
	}
	
	

}
