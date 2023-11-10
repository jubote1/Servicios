package ModeloSer;

public class HorarioResumen {
	
	private int idHorarioResumen;
	private int idEmpleado;
	private double totalHoras;
	private double horasExtrasOrd;
	private double horasExtrasDomi;
	private double horasFestiva;
	private double horasRecargoNocturno;
	private String fechaInicial;
	private String fechaFinal;
	public int getIdHorarioResumen() {
		return idHorarioResumen;
	}
	public void setIdHorarioResumen(int idHorarioResumen) {
		this.idHorarioResumen = idHorarioResumen;
	}
	public int getIdEmpleado() {
		return idEmpleado;
	}
	public void setIdEmpleado(int idEmpleado) {
		this.idEmpleado = idEmpleado;
	}
	public double getTotalHoras() {
		return totalHoras;
	}
	public void setTotalHoras(double totalHoras) {
		this.totalHoras = totalHoras;
	}
	public double getHorasExtrasOrd() {
		return horasExtrasOrd;
	}
	public void setHorasExtrasOrd(double horasExtrasOrd) {
		this.horasExtrasOrd = horasExtrasOrd;
	}
	public double getHorasExtrasDomi() {
		return horasExtrasDomi;
	}
	public void setHorasExtrasDomi(double horasExtrasDomi) {
		this.horasExtrasDomi = horasExtrasDomi;
	}
	public double getHorasFestiva() {
		return horasFestiva;
	}
	public void setHorasFestiva(double horasFestiva) {
		this.horasFestiva = horasFestiva;
	}
	public double getHorasRecargoNocturno() {
		return horasRecargoNocturno;
	}
	public void setHorasRecargoNocturno(double horasRecargoNocturno) {
		this.horasRecargoNocturno = horasRecargoNocturno;
	}
	public String getFechaInicial() {
		return fechaInicial;
	}
	public void setFechaInicial(String fechaInicial) {
		this.fechaInicial = fechaInicial;
	}
	public String getFechaFinal() {
		return fechaFinal;
	}
	public void setFechaFinal(String fechaFinal) {
		this.fechaFinal = fechaFinal;
	}
	public HorarioResumen(int idHorarioResumen, int idEmpleado, double totalHoras, double horasExtrasOrd,
			double horasExtrasDomi, double horasFestiva, double horasRecargoNocturno, String fechaInicial,
			String fechaFinal) {
		super();
		this.idHorarioResumen = idHorarioResumen;
		this.idEmpleado = idEmpleado;
		this.totalHoras = totalHoras;
		this.horasExtrasOrd = horasExtrasOrd;
		this.horasExtrasDomi = horasExtrasDomi;
		this.horasFestiva = horasFestiva;
		this.horasRecargoNocturno = horasRecargoNocturno;
		this.fechaInicial = fechaInicial;
		this.fechaFinal = fechaFinal;
	}
	
}
