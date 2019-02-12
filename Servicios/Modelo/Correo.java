package Modelo;

public class Correo {
	String usuarioCorreo;
	String contrasena;
	String destino;
	String destino2;
	String destino3;
	String destino4;
	String destino5;
	String asunto;
	String Mensaje;
	String[] rutasArchivos;
	
	
	
	public String[] getRutasArchivos() {
		return rutasArchivos;
	}
	public void setRutasArchivos(String[] rutasArchivos) {
		this.rutasArchivos = rutasArchivos;
	}
	public String getUsuarioCorreo() {
		return usuarioCorreo;
	}
	public void setUsuarioCorreo(String usuarioCorreo) {
		this.usuarioCorreo = usuarioCorreo;
	}
	public String getContrasena() {
		return contrasena;
	}
	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}
	public String getDestino() {
		return destino;
	}
	public void setDestino(String destino) {
		this.destino = destino;
	}
	
	public String getDestino2() {
		return destino2;
	}
	public void setDestino2(String destino2) {
		this.destino2 = destino2;
	}
	public String getDestino3() {
		return destino3;
	}
	public void setDestino3(String destino3) {
		this.destino3 = destino3;
	}
	public String getAsunto() {
		return asunto;
	}
	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}
	public String getMensaje() {
		return Mensaje;
	}
	public void setMensaje(String mensaje) {
		Mensaje = mensaje;
	}
	
	public String getDestino4() {
		return destino4;
	}
	public void setDestino4(String destino4) {
		this.destino4 = destino4;
	}
	public String getDestino5() {
		return destino5;
	}
	public void setDestino5(String destino5) {
		this.destino5 = destino5;
	}
	public Correo(String usuarioCorreo, String contrasena, String destino, String destino2, String destino3, String destino4, String destino5, String asunto, String mensaje) {
		super();
		this.usuarioCorreo = usuarioCorreo;
		this.contrasena = contrasena;
		this.destino = destino;
		this.destino2 = destino2;
		this.destino3 = destino3;
		this.destino4 = destino4;
		this.destino5 = destino5;
		this.asunto = asunto;
		Mensaje = mensaje;
	}
	
	public Correo()
	{
		
	}
	
	
	
}
