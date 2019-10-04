package Modelo;

public class EmpleadoEvento {
	private int id;
	private String tipoEvento;
	private String fecha;
	private String fechaHoraLog;
	private int idTienda;
	private String usoBiometria;
	private String nombreEmpleado;
	
	
	
	public String getNombreEmpleado() {
		return nombreEmpleado;
	}
	public void setNombreEmpleado(String nombreEmpleado) {
		this.nombreEmpleado = nombreEmpleado;
	}
	public String getUsoBiometria() {
		return usoBiometria;
	}
	public void setUsoBiometria(String usoBiometria) {
		this.usoBiometria = usoBiometria;
	}
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTipoEvento() {
		return tipoEvento;
	}
	public void setTipoEvento(String tipoEvento) {
		this.tipoEvento = tipoEvento;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public String getFechaHoraLog() {
		return fechaHoraLog;
	}
	public void setFechaHoraLog(String fechaHoraLog) {
		this.fechaHoraLog = fechaHoraLog;
	}
	public EmpleadoEvento(int id, String tipoEvento, String fecha, String fechaHoraLog, int idTienda, String usoBiometria) {
		super();
		this.id = id;
		this.tipoEvento = tipoEvento;
		this.fecha = fecha;
		this.fechaHoraLog = fechaHoraLog;
		this.idTienda = idTienda;
		this.usoBiometria = usoBiometria;
	}
	
	

}
