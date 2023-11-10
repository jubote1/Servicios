package ModeloSer;

public class EmpleadoEncuesta {
	
	private int idEmpleadoEncuesta;
	private int id;
	private int idEncuesta;
	private String fechaIngreso;
	public int getIdEmpleadoEncuesta() {
		return idEmpleadoEncuesta;
	}
	public void setIdEmpleadoEncuesta(int idEmpleadoEncuesta) {
		this.idEmpleadoEncuesta = idEmpleadoEncuesta;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIdEncuesta() {
		return idEncuesta;
	}
	public void setIdEncuesta(int idEncuesta) {
		this.idEncuesta = idEncuesta;
	}
	public String getFechaIngreso() {
		return fechaIngreso;
	}
	public void setFechaIngreso(String fechaIngreso) {
		this.fechaIngreso = fechaIngreso;
	}
	public EmpleadoEncuesta(int idEmpleadoEncuesta, int id, int idEncuesta, String fechaIngreso) {
		super();
		this.idEmpleadoEncuesta = idEmpleadoEncuesta;
		this.id = id;
		this.idEncuesta = idEncuesta;
		this.fechaIngreso = fechaIngreso;
	}
	
	

}
