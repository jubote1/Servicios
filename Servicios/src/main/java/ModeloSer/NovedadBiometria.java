package ModeloSer;

/**
 * Una novedad de biometria de un empleado en un dia.
 *
 * Guarda el evento EXACTO que quedo sin pareja, no el primero del dia. Antes el
 * proceso reportaba siempre el primer ingreso del empleado, asi que a alguien que
 * entro a las 6:51, salio, volvio a entrar y se le quedo sin salida el ingreso de
 * las 15:20, el correo le decia que revisara el de las 6:51.
 */
public class NovedadBiometria {

	/** Hay un ingreso que nunca tuvo su salida. */
	public static final String FALTA_SALIDA = "FALTA LA SALIDA";
	/** Hay una salida sin un ingreso abierto que le corresponda. */
	public static final String FALTA_INGRESO = "FALTA EL INGRESO";
	/** El ingreso quedo registrado a una hora sospechosamente tardia. */
	public static final String INGRESO_TARDIO = "INGRESO TARDIO";

	private int idEmpleado;
	private String nombreEmpleado;
	private String correo;
	private String fecha;
	private String dia;
	private int idTienda;
	private String nombreTienda;
	private String tipoNovedad;
	private String tipoEvento;
	private String fechaHoraEvento;

	public NovedadBiometria(final int idEmpleado, final String nombreEmpleado, final String fecha,
			final String dia, final int idTienda, final String tipoNovedad, final String tipoEvento,
			final String fechaHoraEvento) {
		this.idEmpleado = idEmpleado;
		this.nombreEmpleado = nombreEmpleado;
		this.fecha = fecha;
		this.dia = dia;
		this.idTienda = idTienda;
		this.tipoNovedad = tipoNovedad;
		this.tipoEvento = tipoEvento;
		this.fechaHoraEvento = fechaHoraEvento;
		this.correo = "";
		this.nombreTienda = "";
	}

	/**
	 * Solo la hora del evento, sin la fecha. Si la marca de tiempo no viene con el
	 * formato esperado se devuelve completa en vez de reventar: antes un substring
	 * sin guarda podia tumbar el proceso y ese dia no salia ningun correo.
	 */
	public String getHoraEvento() {
		if (this.fechaHoraEvento == null) {
			return("");
		}
		if (this.fechaHoraEvento.length() >= 19) {
			return(this.fechaHoraEvento.substring(11, 19));
		}
		return(this.fechaHoraEvento);
	}

	/** La hora del evento en numero, o -1 si no se puede leer. */
	public int getHoraNumero() {
		if (this.fechaHoraEvento == null || this.fechaHoraEvento.length() < 13) {
			return(-1);
		}
		try {
			return(Integer.parseInt(this.fechaHoraEvento.substring(11, 13)));
		} catch (final Exception e) {
			return(-1);
		}
	}

	/** Lo que se le dice al empleado, en una frase. */
	public String getExplicacion() {
		if (this.tipoNovedad.equals(NovedadBiometria.FALTA_SALIDA)) {
			return("Quedo registrado su ingreso a las " + this.getHoraEvento()
					+ ", pero no quedo registrada la salida correspondiente.");
		}
		if (this.tipoNovedad.equals(NovedadBiometria.FALTA_INGRESO)) {
			return("Quedo registrada su salida a las " + this.getHoraEvento()
					+ ", pero no habia un ingreso registrado antes de esa salida.");
		}
		if (this.tipoNovedad.equals(NovedadBiometria.INGRESO_TARDIO)) {
			return("Su ingreso quedo registrado a las " + this.getHoraEvento()
					+ ", una hora inusualmente tardia. Puede que la marcacion se haya hecho despues de haber entrado.");
		}
		return(this.tipoNovedad);
	}

	public boolean tieneCorreo() {
		return(this.correo != null && this.correo.trim().length() > 0 && this.correo.indexOf("@") > 0);
	}

	public int getIdEmpleado() { return(this.idEmpleado); }
	public String getNombreEmpleado() { return(this.nombreEmpleado); }
	public String getCorreo() { return(this.correo); }
	public void setCorreo(final String correo) { this.correo = correo; }
	public String getFecha() { return(this.fecha); }
	public String getDia() { return(this.dia); }
	public int getIdTienda() { return(this.idTienda); }
	public String getNombreTienda() { return(this.nombreTienda); }
	public void setNombreTienda(final String nombreTienda) { this.nombreTienda = nombreTienda; }
	public String getTipoNovedad() { return(this.tipoNovedad); }
	public String getTipoEvento() { return(this.tipoEvento); }
	public String getFechaHoraEvento() { return(this.fechaHoraEvento); }
}
