package ModeloSer;

public class EmpleadoEncuestaDetalle {
	
	private int idEmpEncuestaDetalle;
	private int idEmpleadoEncuesta;
	private int idEncuestaDetalle;
	private String respuestaSi;
	private String respuestaNo;
	private String observacion;
	public EmpleadoEncuestaDetalle(int idEmpEncuestaDetalle, int idEmpleadoEncuesta, int idEncuestaDetalle,
			String respuestaSi, String respuestaNo, String observacion) {
		super();
		this.idEmpEncuestaDetalle = idEmpEncuestaDetalle;
		this.idEmpleadoEncuesta = idEmpleadoEncuesta;
		this.idEncuestaDetalle = idEncuestaDetalle;
		this.respuestaSi = respuestaSi;
		this.respuestaNo = respuestaNo;
		this.observacion = observacion;
	}
	public int getIdEmpEncuestaDetalle() {
		return idEmpEncuestaDetalle;
	}
	public void setIdEmpEncuestaDetalle(int idEmpEncuestaDetalle) {
		this.idEmpEncuestaDetalle = idEmpEncuestaDetalle;
	}
	public int getIdEmpleadoEncuesta() {
		return idEmpleadoEncuesta;
	}
	public void setIdEmpleadoEncuesta(int idEmpleadoEncuesta) {
		this.idEmpleadoEncuesta = idEmpleadoEncuesta;
	}
	public int getIdEncuestaDetalle() {
		return idEncuestaDetalle;
	}
	public void setIdEncuestaDetalle(int idEncuestaDetalle) {
		this.idEncuestaDetalle = idEncuestaDetalle;
	}
	public String getRespuestaSi() {
		return respuestaSi;
	}
	public void setRespuestaSi(String respuestaSi) {
		this.respuestaSi = respuestaSi;
	}
	public String getRespuestaNo() {
		return respuestaNo;
	}
	public void setRespuestaNo(String respuestaNo) {
		this.respuestaNo = respuestaNo;
	}
	public String getObservacion() {
		return observacion;
	}
	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
	
	

}
