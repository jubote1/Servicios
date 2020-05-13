package ModeloSer;

public class EmpleadoTemporalDia {
	
	private int id;
	private String identificacion;
	private String nombre;
	private String telefono;
	private String empresa;
	private String fechaSistema;
	private String horaIngreso;
	private String horaSalida;
	private int idEmpresa;
	
	
	
	public int getIdEmpresa() {
		return idEmpresa;
	}
	public void setIdEmpresa(int idEmpresa) {
		this.idEmpresa = idEmpresa;
	}
	public String getHoraIngreso() {
		return horaIngreso;
	}
	public void setHoraIngreso(String horaIngreso) {
		this.horaIngreso = horaIngreso;
	}
	public String getHoraSalida() {
		return horaSalida;
	}
	public void setHoraSalida(String horaSalida) {
		this.horaSalida = horaSalida;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIdentificacion() {
		return identificacion;
	}
	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	public String getEmpresa() {
		return empresa;
	}
	public void setEmpresa(String empresa) {
		this.empresa = empresa;
	}
	public String getFechaSistema() {
		return fechaSistema;
	}
	public void setFechaSistema(String fechaSistema) {
		this.fechaSistema = fechaSistema;
	}
	public EmpleadoTemporalDia(int id, String identificacion, String nombre, String telefono, String empresa,
			String fechaSistema, int idEmpresa) {
		super();
		this.id = id;
		this.identificacion = identificacion;
		this.nombre = nombre;
		this.telefono = telefono;
		this.empresa = empresa;
		this.fechaSistema = fechaSistema;
		this.idEmpresa = idEmpresa;
	}
	
	

}
