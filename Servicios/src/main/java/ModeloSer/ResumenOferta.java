package ModeloSer;

/**
 * Una fila del resumen semanal: una oferta, de un origen, con cuanto se
 * envio y cuanto se redimio esa semana.
 *
 * El origen no viene de una columna propia -oferta_cliente no la tiene-,
 * se deduce de como quedo el registro: idenvio da la campana de CRM
 * (Envio de Publicidad), y usuario_ingreso delata a los procesos que
 * emiten solos (RULETA, bono-nocturno). Ver ReporteOfertasSemanales.
 */
public class ResumenOferta {

	private String nombreOferta;
	private String origen;
	private int enviadas;
	private double valorEnviado;
	private int redimidas;
	private double valorRedimido;

	public String getClave() {
		return (this.nombreOferta + "|" + this.origen);
	}

	public String getNombreOferta() {
		return nombreOferta;
	}
	public void setNombreOferta(String nombreOferta) {
		this.nombreOferta = nombreOferta;
	}
	public String getOrigen() {
		return origen;
	}
	public void setOrigen(String origen) {
		this.origen = origen;
	}
	public int getEnviadas() {
		return enviadas;
	}
	public void setEnviadas(int enviadas) {
		this.enviadas = enviadas;
	}
	public double getValorEnviado() {
		return valorEnviado;
	}
	public void setValorEnviado(double valorEnviado) {
		this.valorEnviado = valorEnviado;
	}
	public int getRedimidas() {
		return redimidas;
	}
	public void setRedimidas(int redimidas) {
		this.redimidas = redimidas;
	}
	public double getValorRedimido() {
		return valorRedimido;
	}
	public void setValorRedimido(double valorRedimido) {
		this.valorRedimido = valorRedimido;
	}
}
