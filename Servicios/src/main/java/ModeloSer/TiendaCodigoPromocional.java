package ModeloSer;

public class TiendaCodigoPromocional {
	
	private int idTienda;
	private String fechaInicial;
	private String fechaFinal;
	private int lunClientes;
	private int marClientes;
	private int mieClientes;
	private int jueClientes;
	private int vieClientes;
	private int sabClientes;
	private int domClientes;
	private int idOferta;
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public String getFechaInicial() {
		return fechaInicial;
	}
	public void setFechaInicial(String fechaInicial) {
		this.fechaInicial = fechaInicial;
	}
	public String getFechaFinal() {
		return fechaFinal;
	}
	public void setFechaFinal(String fechaFinal) {
		this.fechaFinal = fechaFinal;
	}
	public int getLunClientes() {
		return lunClientes;
	}
	public void setLunClientes(int lunClientes) {
		this.lunClientes = lunClientes;
	}
	public int getMarClientes() {
		return marClientes;
	}
	public void setMarClientes(int marClientes) {
		this.marClientes = marClientes;
	}
	public int getMieClientes() {
		return mieClientes;
	}
	public void setMieClientes(int mieClientes) {
		this.mieClientes = mieClientes;
	}
	public int getJueClientes() {
		return jueClientes;
	}
	public void setJueClientes(int jueClientes) {
		this.jueClientes = jueClientes;
	}
	public int getVieClientes() {
		return vieClientes;
	}
	public void setVieClientes(int vieClientes) {
		this.vieClientes = vieClientes;
	}
	public int getSabClientes() {
		return sabClientes;
	}
	public void setSabClientes(int sabClientes) {
		this.sabClientes = sabClientes;
	}
	public int getDomClientes() {
		return domClientes;
	}
	public void setDomClientes(int domClientes) {
		this.domClientes = domClientes;
	}
	public int getIdOferta() {
		return idOferta;
	}
	public void setIdOferta(int idOferta) {
		this.idOferta = idOferta;
	}
	public TiendaCodigoPromocional(int idTienda, String fechaInicial, String fechaFinal, int lunClientes,
			int marClientes, int mieClientes, int jueClientes, int vieClientes, int sabClientes, int domClientes,
			int idOferta) {
		super();
		this.idTienda = idTienda;
		this.fechaInicial = fechaInicial;
		this.fechaFinal = fechaFinal;
		this.lunClientes = lunClientes;
		this.marClientes = marClientes;
		this.mieClientes = mieClientes;
		this.jueClientes = jueClientes;
		this.vieClientes = vieClientes;
		this.sabClientes = sabClientes;
		this.domClientes = domClientes;
		this.idOferta = idOferta;
	}
	
	

}
