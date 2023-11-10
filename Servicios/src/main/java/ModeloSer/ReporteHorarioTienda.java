package ModeloSer;

public class ReporteHorarioTienda {
	
	private int idTienda;
	private String email;
	
	
	
	public int getIdTienda() {
		return idTienda;
	}



	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}



	public String getEmail() {
		return email;
	}



	public void setEmail(String email) {
		this.email = email;
	}



	public ReporteHorarioTienda(int idTienda, String email) {
		super();
		this.idTienda = idTienda;
		this.email = email;
	}
	
	

}
