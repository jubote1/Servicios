package ModeloSer;

/**
 * Clase que implementa la entidad Usuario.
 * @author JuanDavid
 *
 */
public class Usuario {
	
	private int idUsuario;
	private String nombreUsuario;
	private String contrasena;
	private String nombreLargo;
	private int idTipoEmpleado;
	private String tipoInicio;
	private String administrador;
	private int estadoDomiciliario;
	private int ingreso;
	private String ultimoIngreso;
	private int esEmpleado;
	private int caducado;
	private String claveRapida;
	private int activo;
	private int autorizoRedes;
	private String email;
	private int rotacion;
	private double salario;
	
	
	
	public int getActivo() {
		return activo;
	}
	public void setActivo(int activo) {
		this.activo = activo;
	}
	public int getAutorizoRedes() {
		return autorizoRedes;
	}
	public void setAutorizoRedes(int autorizoRedes) {
		this.autorizoRedes = autorizoRedes;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public int getRotacion() {
		return rotacion;
	}
	public void setRotacion(int rotacion) {
		this.rotacion = rotacion;
	}
	public double getSalario() {
		return salario;
	}
	public void setSalario(double salario) {
		this.salario = salario;
	}
	public String getClaveRapida() {
		return claveRapida;
	}
	public void setClaveRapida(String claveRapida) {
		this.claveRapida = claveRapida;
	}
	public String getUltimoIngreso() {
		return ultimoIngreso;
	}
	public void setUltimoIngreso(String ultimoIngreso) {
		this.ultimoIngreso = ultimoIngreso;
	}
	public int getEsEmpleado() {
		return esEmpleado;
	}
	public void setEsEmpleado(int esEmpleado) {
		this.esEmpleado = esEmpleado;
	}
	public int getCaducado() {
		return caducado;
	}
	public void setCaducado(int caducado) {
		this.caducado = caducado;
	}
	public int getIngreso() {
		return ingreso;
	}
	public void setIngreso(int ingreso) {
		this.ingreso = ingreso;
	}
	public int getIdTipoEmpleado() {
		return idTipoEmpleado;
	}
	public void setIdTipoEmpleado(int idTipoEmpleado) {
		this.idTipoEmpleado = idTipoEmpleado;
	}
	public int getEstadoDomiciliario() {
		return estadoDomiciliario;
	}
	public void setEstadoDomiciliario(int estadoDomiciliario) {
		this.estadoDomiciliario = estadoDomiciliario;
	}
	public String getNombreUsuario() {
		return nombreUsuario;
	}
	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}
	public String getContrasena() {
		return contrasena;
	}
	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}
	public String getNombreLargo() {
		return nombreLargo;
	}
	public void setNombreLargo(String nombreLargo) {
		this.nombreLargo = nombreLargo;
	}
	
	
	
	public int getIdUsuario() {
		return idUsuario;
	}
	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}
	public int getidTipoEmpleado() {
		return idTipoEmpleado;
	}
	public void idTipoEmpleado(int idTipoEmpleado) {
		this.idTipoEmpleado = idTipoEmpleado;
	}
	public String getTipoInicio() {
		return tipoInicio;
	}
	public void setTipoInicio(String tipoInicio) {
		this.tipoInicio = tipoInicio;
	}

	
	
	public String getAdministrador() {
		return administrador;
	}
	public void setAdministrador(String administrador) {
		this.administrador = administrador;
	}
	public Usuario(int idUsuario, String nombreUsuario, String contrasena, String nombreLargo, int idTipoEmpleado,
			String tipoInicio, String administrador, int activo, int autorizoRedes, String email, int rotacion, double salario) {
		super();
		this.idUsuario = idUsuario;
		this.nombreUsuario = nombreUsuario;
		this.contrasena = contrasena;
		this.nombreLargo = nombreLargo;
		this.idTipoEmpleado = idTipoEmpleado;
		this.tipoInicio = tipoInicio;
		this.administrador = administrador;
		this.activo = activo;
		this.autorizoRedes = autorizoRedes;
		this.email = email;
		this.rotacion = rotacion;
		this.salario = salario;
	}
	
	public Usuario(int idUsuario, String nombreUsuario, String contrasena, String nombreLargo, int idTipoEmpleado,
			String tipoInicio, String administrador) {
		super();
		this.idUsuario = idUsuario;
		this.nombreUsuario = nombreUsuario;
		this.contrasena = contrasena;
		this.nombreLargo = nombreLargo;
		this.idTipoEmpleado = idTipoEmpleado;
		this.tipoInicio = tipoInicio;
		this.administrador = administrador;
	}
	public Usuario(String nombreUsuario) {
		super();
		this.nombreUsuario = nombreUsuario;
	}
	
	
	
	

}
