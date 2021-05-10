package ModeloSer;

public class ImprimirAdm {
	
	
	
	private int idImpresion;
	private String imprimir;
	private String impresora;
	
	
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
	public ImprimirAdm(int idImpresion, String imprimir, String impresora) {
		super();
		this.idImpresion = idImpresion;
		this.imprimir = imprimir;
		this.impresora = impresora;
	}
	
	
	
	

}
