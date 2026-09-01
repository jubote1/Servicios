package ModeloSer;

public class ExcepcionFidelizacion {
	
	private int idExcepcion;
	private String fecha;
	private String horaInicio;
	private String horaFin;
	private double valorPunto;
	public int getIdExcepcion() {
		return idExcepcion;
	}
	public void setIdExcepcion(int idExcepcion) {
		this.idExcepcion = idExcepcion;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public String getHoraInicio() {
		return horaInicio;
	}
	public void setHoraInicio(String horaInicio) {
		this.horaInicio = horaInicio;
	}
	public String getHoraFin() {
		return horaFin;
	}
	public void setHoraFin(String horaFin) {
		this.horaFin = horaFin;
	}
	public double getValorPunto() {
		return valorPunto;
	}
	public void setValorPunto(double valorPunto) {
		this.valorPunto = valorPunto;
	}
	public ExcepcionFidelizacion(int idExcepcion, String fecha, String horaInicio, String horaFin, double valorPunto) {
		super();
		this.idExcepcion = idExcepcion;
		this.fecha = fecha;
		this.horaInicio = horaInicio;
		this.horaFin = horaFin;
		this.valorPunto = valorPunto;
	}
	
	

}
