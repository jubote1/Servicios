package ModeloSer;

public class CierreInventarioSemanal {
	
	private int idInsumo;
	private String fecha;
	private int idTienda;
	private double inventarioInicial;
	private double enviadoTienda;
	private double retiro;
	private double inventarioFinal;
	private double consumo;
	private double costoUnitario;
	private double costoTotal;
	private double costoSinConsumir;
	public int getIdInsumo() {
		return idInsumo;
	}
	public void setIdInsumo(int idInsumo) {
		this.idInsumo = idInsumo;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public double getInventarioInicial() {
		return inventarioInicial;
	}
	public void setInventarioInicial(double inventarioInicial) {
		this.inventarioInicial = inventarioInicial;
	}
	public double getEnviadoTienda() {
		return enviadoTienda;
	}
	public void setEnviadoTienda(double enviadoTienda) {
		this.enviadoTienda = enviadoTienda;
	}
	public double getRetiro() {
		return retiro;
	}
	public void setRetiro(double retiro) {
		this.retiro = retiro;
	}
	public double getInventarioFinal() {
		return inventarioFinal;
	}
	public void setInventarioFinal(double inventarioFinal) {
		this.inventarioFinal = inventarioFinal;
	}
	public double getConsumo() {
		return consumo;
	}
	public void setConsumo(double consumo) {
		this.consumo = consumo;
	}
	public double getCostoUnitario() {
		return costoUnitario;
	}
	public void setCostoUnitario(double costoUnitario) {
		this.costoUnitario = costoUnitario;
	}
	public double getCostoTotal() {
		return costoTotal;
	}
	public void setCostoTotal(double costoTotal) {
		this.costoTotal = costoTotal;
	}
	public double getCostoSinConsumir() {
		return costoSinConsumir;
	}
	public void setCostoSinConsumir(double costoSinConsumir) {
		this.costoSinConsumir = costoSinConsumir;
	}
	public CierreInventarioSemanal(int idInsumo, String fecha, int idTienda, double inventarioInicial,
			double enviadoTienda, double retiro, double inventarioFinal, double consumo, double costoUnitario,
			double costoTotal, double costoSinConsumir) {
		super();
		this.idInsumo = idInsumo;
		this.fecha = fecha;
		this.idTienda = idTienda;
		this.inventarioInicial = inventarioInicial;
		this.enviadoTienda = enviadoTienda;
		this.retiro = retiro;
		this.inventarioFinal = inventarioFinal;
		this.consumo = consumo;
		this.costoUnitario = costoUnitario;
		this.costoTotal = costoTotal;
		this.costoSinConsumir = costoSinConsumir;
	}
	public CierreInventarioSemanal() {
		super();
	}
	
	

}
