package ModeloSer;

public class FidelizacionRedencion {

	private String correo = "";
	private String fechaRedencion = "";
	private double puntosRedimidos = 0.0;
	private String tienda = "";
	private String usuario = "";
	private String estado = "";
	private boolean esEmpleado = false;
	private String empleadoNombre = "";

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(final String correo) {
		this.correo = correo == null ? "" : correo;
	}

	public String getFechaRedencion() {
		return fechaRedencion;
	}

	public void setFechaRedencion(final String fechaRedencion) {
		this.fechaRedencion = fechaRedencion == null ? "" : fechaRedencion;
	}

	public double getPuntosRedimidos() {
		return puntosRedimidos;
	}

	public void setPuntosRedimidos(final double puntosRedimidos) {
		this.puntosRedimidos = puntosRedimidos;
	}

	public String getTienda() {
		return tienda;
	}

	public void setTienda(final String tienda) {
		this.tienda = tienda == null ? "" : tienda;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(final String usuario) {
		this.usuario = usuario == null ? "" : usuario;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(final String estado) {
		this.estado = estado == null ? "" : estado;
	}

	public boolean isEsEmpleado() {
		return esEmpleado;
	}

	public void setEsEmpleado(final boolean esEmpleado) {
		this.esEmpleado = esEmpleado;
	}

	public String getEmpleadoNombre() {
		return empleadoNombre;
	}

	public void setEmpleadoNombre(final String empleadoNombre) {
		this.empleadoNombre = empleadoNombre == null ? "" : empleadoNombre;
	}

	public FidelizacionRedencion(final String correo, final String fechaRedencion, final double puntosRedimidos) {
		super();
		this.correo = correo;
		this.fechaRedencion = fechaRedencion;
		this.puntosRedimidos = puntosRedimidos;
	}

	public FidelizacionRedencion(final String correo, final String fechaRedencion, final double puntosRedimidos,
			final String tienda, final String usuario, final String estado) {
		this(correo, fechaRedencion, puntosRedimidos);
		this.tienda = tienda == null ? "" : tienda;
		this.usuario = usuario == null ? "" : usuario;
		this.estado = estado == null ? "" : estado;
	}
}
