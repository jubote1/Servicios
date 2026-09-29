package ModeloSer;

/**
 * Una fila de la tabla consignacion de una tienda (base local, la misma que llena
 * VentPedAdmConsignacion en el POS): lo que el cajero registro como consignado al
 * banco, no lo que un procesador de pagos (Bold, Wompi) confirma. Son cosas
 * distintas -ver ConsignacionBoldDAO/ConsignacionSemanalDAO- que conviene no mezclar.
 */
public class ConsignacionSemanal {

	private int idConsignacion;
	private String fechaSistema;
	private String descripcion;
	private double valorConsignacion;
	private String horaConsignacion;
	private String fechaReal;
	private String usuario;
	private String usuarioTestigo;

	public int getIdConsignacion() {
		return idConsignacion;
	}

	public void setIdConsignacion(final int idConsignacion) {
		this.idConsignacion = idConsignacion;
	}

	public String getFechaSistema() {
		return fechaSistema;
	}

	public void setFechaSistema(final String fechaSistema) {
		this.fechaSistema = fechaSistema;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(final String descripcion) {
		this.descripcion = descripcion;
	}

	public double getValorConsignacion() {
		return valorConsignacion;
	}

	public void setValorConsignacion(final double valorConsignacion) {
		this.valorConsignacion = valorConsignacion;
	}

	public String getHoraConsignacion() {
		return horaConsignacion;
	}

	public void setHoraConsignacion(final String horaConsignacion) {
		this.horaConsignacion = horaConsignacion;
	}

	public String getFechaReal() {
		return fechaReal;
	}

	public void setFechaReal(final String fechaReal) {
		this.fechaReal = fechaReal;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(final String usuario) {
		this.usuario = usuario;
	}

	public String getUsuarioTestigo() {
		return usuarioTestigo;
	}

	public void setUsuarioTestigo(final String usuarioTestigo) {
		this.usuarioTestigo = usuarioTestigo;
	}

}
