package ModeloSer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lo que paso con una tienda en la replica: si contesto y, tabla por tabla, que se replico.
 */
public class ReplicaTienda {

	private int idTienda;
	private String nombre = "";
	private String hostBD = "";
	private boolean conecto = false;
	private String errorConexion = "";
	private Map<String, ReplicaCelda> celdas = new LinkedHashMap<String, ReplicaCelda>();

	public ReplicaTienda(int idTienda, String nombre, String hostBD) {
		this.idTienda = idTienda;
		this.nombre = nombre == null ? "" : nombre;
		this.hostBD = hostBD == null ? "" : hostBD;
	}

	/** Si alguna tabla fallo o la tienda no contesto. */
	public boolean tieneProblemas() {
		if (!conecto) {
			return true;
		}
		for (ReplicaCelda c : celdas.values()) {
			if (c.esError()) {
				return true;
			}
		}
		return false;
	}

	public int cuantasConError() {
		int n = 0;
		for (ReplicaCelda c : celdas.values()) {
			if (c.esError()) {
				n++;
			}
		}
		return n;
	}

	public int getIdTienda() {
		return idTienda;
	}

	public String getNombre() {
		return nombre;
	}

	public String getHostBD() {
		return hostBD;
	}

	public boolean isConecto() {
		return conecto;
	}

	public void setConecto(boolean conecto) {
		this.conecto = conecto;
	}

	public String getErrorConexion() {
		return errorConexion;
	}

	public void setErrorConexion(String errorConexion) {
		this.errorConexion = errorConexion == null ? "" : errorConexion;
	}

	public Map<String, ReplicaCelda> getCeldas() {
		return celdas;
	}
}
