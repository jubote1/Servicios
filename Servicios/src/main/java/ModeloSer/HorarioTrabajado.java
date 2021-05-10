package ModeloSer;

public class HorarioTrabajado {
	
	private int idHorario;
	private int idEmpleado;
	private String fecha;
	private String dia;
	private String ingreso;
	private String salida;
	private double horas;
	private int idTienda;
	public int getIdHorario() {
		return idHorario;
	}
	public void setIdHorario(int idHorario) {
		this.idHorario = idHorario;
	}
	public int getIdEmpleado() {
		return idEmpleado;
	}
	public void setIdEmpleado(int idEmpleado) {
		this.idEmpleado = idEmpleado;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public String getDia() {
		return dia;
	}
	public void setDia(String dia) {
		this.dia = dia;
	}
	public String getIngreso() {
		return ingreso;
	}
	public void setIngreso(String ingreso) {
		this.ingreso = ingreso;
	}
	public String getSalida() {
		return salida;
	}
	public void setSalida(String salida) {
		this.salida = salida;
	}
	public double getHoras() {
		return horas;
	}
	public void setHoras(double horas) {
		this.horas = horas;
	}
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public HorarioTrabajado(int idHorario, int idEmpleado, String fecha, String dia, String ingreso, String salida,
			double horas, int idTienda) {
		super();
		this.idHorario = idHorario;
		this.idEmpleado = idEmpleado;
		this.fecha = fecha;
		this.dia = dia;
		this.ingreso = ingreso;
		this.salida = salida;
		this.horas = horas;
		this.idTienda = idTienda;
	}
	
}
