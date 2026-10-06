package ModeloSer;

/**
 * El resultado de replicar UNA tabla de UNA tienda para el dia de ayer.
 *
 * Una celda es lo que se pinta en la matriz del correo: verde si replico,
 * rojo si fallo, y gris o amarillo cuando no hay nada que alarmar o conviene mirar.
 */
public class ReplicaCelda {

	/** Se leyo de la tienda y se escribio en el datamart. */
	public static final String OK = "OK";
	/** La tienda no tenia filas para ese dia. Puede ser normal o no segun la tabla. */
	public static final String CERO = "CERO";
	/** El datamart ya tenia ese dia de esa tienda: no se toco. */
	public static final String YA_ESTABA = "YA";
	/** Fallo: no se pudo leer la tienda o no se pudo escribir. Es lo unico rojo. */
	public static final String ERROR = "ERROR";
	/** No aplica o falta una migracion: se dice, pero no es una falla de la tienda. */
	public static final String NO_APLICA = "NA";

	private String tabla = "";
	private String estado = NO_APLICA;
	private int filas = 0;
	private String detalle = "";
	/** Cuantos dias ATRASADOS se recuperaron en esta corrida (tienda que estuvo apagada). */
	private int diasRecuperados = 0;
	/** Ultima fecha de datos con replica buena, para las celdas en rojo. Vacio si no se sabe. */
	private String ultimoOk = "";

	public ReplicaCelda(String tabla) {
		this.tabla = tabla;
	}

	public boolean esError() {
		return ERROR.equals(estado);
	}

	public String getTabla() {
		return tabla;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public int getFilas() {
		return filas;
	}

	public void setFilas(int filas) {
		this.filas = filas;
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle == null ? "" : detalle;
	}

	public int getDiasRecuperados() {
		return diasRecuperados;
	}

	public void setDiasRecuperados(int diasRecuperados) {
		this.diasRecuperados = diasRecuperados;
	}

	public String getUltimoOk() {
		return ultimoOk;
	}

	public void setUltimoOk(String ultimoOk) {
		this.ultimoOk = ultimoOk == null ? "" : ultimoOk;
	}
}
