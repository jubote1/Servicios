package ModeloSer;

/**
 * Pedido de punto de venta con la campana "15 minutos o gratis" aplicada,
 * leido de la tabla campana_15min_aplicado en el central (pizzaamericana).
 */
public class Campana15MinAplicado {

	private int idPedidoTienda;

	private int idTienda;

	private int idCampana;

	private String fechaHoraInicio;

	private double valorBasePizza;

	private String idFormaPagoVirtual;

	public Campana15MinAplicado(int idPedidoTienda, int idTienda, int idCampana, String fechaHoraInicio,
			double valorBasePizza, String idFormaPagoVirtual) {
		this.idPedidoTienda = idPedidoTienda;
		this.idTienda = idTienda;
		this.idCampana = idCampana;
		this.fechaHoraInicio = fechaHoraInicio;
		this.valorBasePizza = valorBasePizza;
		this.idFormaPagoVirtual = idFormaPagoVirtual;
	}

	public int getIdPedidoTienda() {
		return idPedidoTienda;
	}

	public int getIdTienda() {
		return idTienda;
	}

	public int getIdCampana() {
		return idCampana;
	}

	public String getFechaHoraInicio() {
		return fechaHoraInicio;
	}

	public double getValorBasePizza() {
		return valorBasePizza;
	}

	public boolean esMedioVirtual() {
		return "S".equals(idFormaPagoVirtual);
	}
}
