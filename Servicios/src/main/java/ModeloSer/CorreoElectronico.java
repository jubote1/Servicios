package ModeloSer;

public class CorreoElectronico {
	
	private String cuentaCorreo;
	private String claveCorreo;
	
	public String getCuentaCorreo() {
		return cuentaCorreo;
	}
	public void setCuentaCorreo(String cuentaCorreo) {
		this.cuentaCorreo = cuentaCorreo;
	}
	public String getClaveCorreo() {
		return claveCorreo;
	}
	public void setClaveCorreo(String claveCorreo) {
		this.claveCorreo = claveCorreo;
	}
	public CorreoElectronico(String cuentaCorreo, String claveCorreo) {
		super();
		this.cuentaCorreo = cuentaCorreo;
		this.claveCorreo = claveCorreo;
	}
	
}
