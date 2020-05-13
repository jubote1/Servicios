package ModeloSer;

public class OfertaCliente {
	
	private int idOfertaCliente;
	private int idOferta;
	private String nombreOferta;
	private int idCliente;
	private String utilizada;
	private String ingresoOferta;
	private String usoOferta;
	private String observacion;
	private int PQRS;
	
	
	
	public int getPQRS() {
		return PQRS;
	}
	public void setPQRS(int pQRS) {
		PQRS = pQRS;
	}
	public String getObservacion() {
		return observacion;
	}
	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
	public String getNombreOferta() {
		return nombreOferta;
	}
	public void setNombreOferta(String nombreOferta) {
		this.nombreOferta = nombreOferta;
	}
	public String getIngresoOferta() {
		return ingresoOferta;
	}
	public void setIngresoOferta(String ingresoOferta) {
		this.ingresoOferta = ingresoOferta;
	}
	public String getUsoOferta() {
		return usoOferta;
	}
	public void setUsoOferta(String usoOferta) {
		this.usoOferta = usoOferta;
	}
	public int getIdOfertaCliente() {
		return idOfertaCliente;
	}
	public void setIdOfertaCliente(int idOfertaCliente) {
		this.idOfertaCliente = idOfertaCliente;
	}
	public int getIdOferta() {
		return idOferta;
	}
	public void setIdOferta(int idOferta) {
		this.idOferta = idOferta;
	}
	public int getIdCliente() {
		return idCliente;
	}
	public void setIdCliente(int idCliente) {
		this.idCliente = idCliente;
	}
	public String getUtilizada() {
		return utilizada;
	}
	public void setUtilizada(String utilizada) {
		this.utilizada = utilizada;
	}

	public OfertaCliente(int idOfertaCliente, int idOferta, int idCliente, String utilizada, int pqrs, String ingresoOferta,
			String usoOferta, String observacion) {
		super();
		this.idOfertaCliente = idOfertaCliente;
		this.idOferta = idOferta;
		this.idCliente = idCliente;
		this.utilizada = utilizada;
		this.ingresoOferta = ingresoOferta;
		this.usoOferta = usoOferta;
		this.observacion = observacion;
		this.PQRS = pqrs;
	}
	
	
	

}
