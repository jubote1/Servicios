package ModeloSer;

public class EmpresaTemporal {
	
	int idEmpresa;
	String nombreEmpresa;
	double valorHoraNormal;
	double valorHoraDominical;
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
	public EmpresaTemporal(int idEmpresa, String nombreEmpresa, double valorHoraNormal, double valorHoraDominical) {
		super();
		this.idEmpresa = idEmpresa;
		this.nombreEmpresa = nombreEmpresa;
		this.valorHoraNormal = valorHoraNormal;
		this.valorHoraDominical = valorHoraDominical;
	}
	
	public String toString() {
	    return nombreEmpresa;
	}

}
