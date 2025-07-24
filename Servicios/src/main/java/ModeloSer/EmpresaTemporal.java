package ModeloSer;

public class EmpresaTemporal {
	
	int idEmpresa;
	String nombreEmpresa;
	double valorHoraNormal;
	double valorHoraDominical;
	double valorHoraSabado;
	
	
	public double getValorHoraSabado() {
		return valorHoraSabado;
	}
	public void setValorHoraSabado(double valorHoraSabado) {
		this.valorHoraSabado = valorHoraSabado;
	}
	public int getIdEmpresa() {
		return idEmpresa;
	}
	public void setIdEmpresa(int idEmpresa) {
		this.idEmpresa = idEmpresa;
	}
	public String getNombreEmpresa() {
		return nombreEmpresa;
	}
	public void setNombreEmpresa(String nombreEmpresa) {
		this.nombreEmpresa = nombreEmpresa;
	}
	public double getValorHoraNormal() {
		return valorHoraNormal;
	}
	public void setValorHoraNormal(double valorHoraNormal) {
		this.valorHoraNormal = valorHoraNormal;
	}
	public double getValorHoraDominical() {
		return valorHoraDominical;
	}
	public void setValorHoraDominical(double valorHoraDominical) {
		this.valorHoraDominical = valorHoraDominical;
	}

	
	public EmpresaTemporal(int idEmpresa, String nombreEmpresa, double valorHoraNormal, double valorHoraDominical,
			double valorHoraSabado) {
		super();
		this.idEmpresa = idEmpresa;
		this.nombreEmpresa = nombreEmpresa;
		this.valorHoraNormal = valorHoraNormal;
		this.valorHoraDominical = valorHoraDominical;
		this.valorHoraSabado = valorHoraSabado;
	}
	public String toString() {
	    return nombreEmpresa;
	}

}
