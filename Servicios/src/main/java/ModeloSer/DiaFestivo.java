package ModeloSer;

public class DiaFestivo {

	private int id;
	private String fechaFestiva;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFechaFestiva() {
		return fechaFestiva;
	}
	public void setFechaFestiva(String fechaFestiva) {
		this.fechaFestiva = fechaFestiva;
	}
	public DiaFestivo(int id, String fechaFestiva) {
		super();
		this.id = id;
		this.fechaFestiva = fechaFestiva;
	}
	
}
