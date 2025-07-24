package ModeloSer;

public class FidelizacionRedencion {
	
	private String correo;
	private String fechaRedencion;
	private double puntosRedimidos;
	public String getCorreo() {
		return correo;
	}
	public void setCorreo(String correo) {
		this.correo = correo;
	}
	public String getFechaRedencion() {
		return fechaRedencion;
	}
	public void setFechaRedencion(String fechaRedencion) {
		this.fechaRedencion = fechaRedencion;
	}
	public double getPuntosRedimidos() {
		return puntosRedimidos;
	}
	public void setPuntosRedimidos(double puntosRedimidos) {
		this.puntosRedimidos = puntosRedimidos;
	}
	public FidelizacionRedencion(String correo, String fechaRedencion, double puntosRedimidos) {
		super();
		this.correo = correo;
		this.fechaRedencion = fechaRedencion;
		this.puntosRedimidos = puntosRedimidos;
	}
	
	

}
