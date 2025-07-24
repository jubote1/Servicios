package ModeloSer;

public class VarianzaResumen {
	
	private int idItem;
	private String fecha;
	private double inicio;
	private double retiro;
	private double ingreso;
	private double consumo;
	private double teoricoReal;
	private double teoricoIngresado;
	private double varianza;
	public int getIdItem() {
		return idItem;
	}
	public void setIdItem(int idItem) {
		this.idItem = idItem;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public double getInicio() {
		return inicio;
	}
	public void setInicio(double inicio) {
		this.inicio = inicio;
	}
	public double getRetiro() {
		return retiro;
	}
	public void setRetiro(double retiro) {
		this.retiro = retiro;
	}
	public double getIngreso() {
		return ingreso;
	}
	public void setIngreso(double ingreso) {
		this.ingreso = ingreso;
	}
	public double getConsumo() {
		return consumo;
	}
	public void setConsumo(double consumo) {
		this.consumo = consumo;
	}
	public double getTeoricoReal() {
		return teoricoReal;
	}
	public void setTeoricoReal(double teoricoReal) {
		this.teoricoReal = teoricoReal;
	}
	public double getTeoricoIngresado() {
		return teoricoIngresado;
	}
	public void setTeoricoIngresado(double teoricoIngresado) {
		this.teoricoIngresado = teoricoIngresado;
	}
	public double getVarianza() {
		return varianza;
	}
	public void setVarianza(double varianza) {
		this.varianza = varianza;
	}
	public VarianzaResumen(int idItem, String fecha, double inicio, double retiro, double ingreso, double consumo,
			double teoricoReal, double teoricoIngresado, double varianza) {
		super();
		this.idItem = idItem;
		this.fecha = fecha;
		this.inicio = inicio;
		this.retiro = retiro;
		this.ingreso = ingreso;
		this.consumo = consumo;
		this.teoricoReal = teoricoReal;
		this.teoricoIngresado = teoricoIngresado;
		this.varianza = varianza;
	}
	
	

}
