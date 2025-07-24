package ModeloSer;

public class ImprimirAdm {
	
	
	
	private int idImpresion;
	private String imprimir;
	private String impresora;
	private String qr;
	
	
	
	public String getQr() {
		return qr;
	}
	public void setQr(String qr) {
		this.qr = qr;
	}
	public String getImpresora() {
		return impresora;
	}
	public void setImpresora(String impresora) {
		this.impresora = impresora;
	}
	public int getIdImpresion() {
		return idImpresion;
	}
	public void setIdImpresion(int idImpresion) {
		this.idImpresion = idImpresion;
	}
	public String getImprimir() {
		return imprimir;
	}
	public void setImprimir(String imprimir) {
		this.imprimir = imprimir;
	}
	public ImprimirAdm(int idImpresion, String imprimir, String impresora, String qr) {
		super();
		this.idImpresion = idImpresion;
		this.imprimir = imprimir;
		this.impresora = impresora;
		this.qr = qr;
	}

	
	
	
	

}
