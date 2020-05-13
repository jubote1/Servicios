package ModeloSer;

import java.io.ByteArrayInputStream;

public class EmpleadoBiometria {
	
	private int id;
	private byte datoshuella[];
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public byte[] getDatoshuella() {
		return datoshuella;
	}
	public void setDatoshuella(byte[] datoshuella) {
		this.datoshuella = datoshuella;
	}
	public EmpleadoBiometria(int id, byte[] datoshuella) {
		super();
		this.id = id;
		this.datoshuella = datoshuella;
	}
	
	
	

}
