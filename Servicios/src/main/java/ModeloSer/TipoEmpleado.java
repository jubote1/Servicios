package ModeloSer;

public class TipoEmpleado {
	
	int idTipoEmpleado;
	String descriTipoEmpleado;
	boolean cajero;
	boolean domiciliario;
	boolean administrador;
	boolean hornero;
	boolean cocinero;
	public int getIdTipoEmpleado() {
		return idTipoEmpleado;
	}
	public void setIdTipoEmpleado(int idTipoEmpleado) {
		this.idTipoEmpleado = idTipoEmpleado;
	}
	public String getDescriTipoEmpleado() {
		return descriTipoEmpleado;
	}
	public void setDescriTipoEmpleado(String descriTipoEmpleado) {
		this.descriTipoEmpleado = descriTipoEmpleado;
	}
	public boolean isCajero() {
		return cajero;
	}
	public void setCajero(boolean cajero) {
		this.cajero = cajero;
	}
	public boolean isDomiciliario() {
		return domiciliario;
	}
	public void setDomiciliario(boolean domiciliario) {
		this.domiciliario = domiciliario;
	}
	public boolean isAdministrador() {
		return administrador;
	}
	public void setAdministrador(boolean administrador) {
		this.administrador = administrador;
	}
	public boolean isHornero() {
		return hornero;
	}
	public void setHornero(boolean hornero) {
		this.hornero = hornero;
	}
	public boolean isCocinero() {
		return cocinero;
	}
	public void setCocinero(boolean cocinero) {
		this.cocinero = cocinero;
	}
	public TipoEmpleado(int idTipoEmpleado, String descriTipoEmpleado, boolean cajero, boolean domiciliario,
			boolean administrador, boolean hornero, boolean cocinero) {
		super();
		this.idTipoEmpleado = idTipoEmpleado;
		this.descriTipoEmpleado = descriTipoEmpleado;
		this.cajero = cajero;
		this.domiciliario = domiciliario;
		this.administrador = administrador;
		this.hornero = hornero;
		this.cocinero = cocinero;
	}
	
	public String toString()
	{
		return(descriTipoEmpleado);
	}

}
