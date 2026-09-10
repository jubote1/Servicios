package ModeloSer;

import java.util.ArrayList;

/**
 * Una fila del resumen de operacion: todo lo que se muestra de una tienda.
 *
 * Es un contenedor sin logica de base de datos a proposito, para poder ordenar y
 * pintar sin volver a consultar nada.
 */
public class FilaResumenOperacion {

	private int idTienda = 0;
	private String nombreTienda = "";
	/** Minutos que la tienda esta ofreciendo, de tiempo_pedido_tienda. */
	private int tiempoTienda = 0;
	private int pedidosCocina = 0;
	/** Minutos del pedido mas viejo que sigue en cocina, o -1 si no hay. */
	private int minutosCocina = -1;
	private int pedidosPorSalir = 0;
	/** Minutos del pedido mas viejo pendiente por salir, o -1 si no hay. */
	private int minutosPorSalir = -1;
	private int pedidosEnRuta = 0;
	private int ultimaHoraDomicilio = 0;
	private int ultimaHoraOtros = 0;
	private double ventaDia = 0.0;
	private int domiciliariosInternos = 0;
	private int domiciliariosExternos = 0;
	private int pedidosDescuadrados = 0;
	/** false si la tienda no contesto: no es lo mismo que no tener pedidos. */
	private boolean seLeyo = false;
	/** Cada elemento: pedido, factura de la tienda, valor, hora programada. */
	private ArrayList<String[]> programados = new ArrayList<String[]>();

	public FilaResumenOperacion(final int idTienda, final String nombreTienda) {
		this.idTienda = idTienda;
		this.nombreTienda = nombreTienda;
	}

	/**
	 * El trabajo que tiene la tienda en el momento. Es el criterio con el que se
	 * ordena el resumen: primero la que mas pedidos tiene encima.
	 */
	public int getCarga() {
		return (this.pedidosCocina + this.pedidosPorSalir);
	}

	/**
	 * El peor de los dos tiempos. Sirve para desempatar el orden y para decidir
	 * de que color se pinta la fila.
	 */
	public int getPeorTiempo() {
		return ((this.minutosCocina > this.minutosPorSalir) ? this.minutosCocina : this.minutosPorSalir);
	}

	/** Pendientes de verdad: los que estan por salir mas los que ya van en la calle. */
	public int getPendientes() {
		return (this.pedidosPorSalir + this.pedidosEnRuta);
	}

	public int getIdTienda() {
		return (this.idTienda);
	}

	public String getNombreTienda() {
		return (this.nombreTienda);
	}

	public int getTiempoTienda() {
		return (this.tiempoTienda);
	}

	public void setTiempoTienda(final int tiempoTienda) {
		this.tiempoTienda = tiempoTienda;
	}

	public int getPedidosCocina() {
		return (this.pedidosCocina);
	}

	public void setPedidosCocina(final int pedidosCocina) {
		this.pedidosCocina = pedidosCocina;
	}

	public int getMinutosCocina() {
		return (this.minutosCocina);
	}

	public void setMinutosCocina(final int minutosCocina) {
		this.minutosCocina = minutosCocina;
	}

	public int getPedidosPorSalir() {
		return (this.pedidosPorSalir);
	}

	public void setPedidosPorSalir(final int pedidosPorSalir) {
		this.pedidosPorSalir = pedidosPorSalir;
	}

	public int getMinutosPorSalir() {
		return (this.minutosPorSalir);
	}

	public void setMinutosPorSalir(final int minutosPorSalir) {
		this.minutosPorSalir = minutosPorSalir;
	}

	public int getPedidosEnRuta() {
		return (this.pedidosEnRuta);
	}

	public void setPedidosEnRuta(final int pedidosEnRuta) {
		this.pedidosEnRuta = pedidosEnRuta;
	}

	public int getUltimaHoraDomicilio() {
		return (this.ultimaHoraDomicilio);
	}

	public void setUltimaHoraDomicilio(final int ultimaHoraDomicilio) {
		this.ultimaHoraDomicilio = ultimaHoraDomicilio;
	}

	public int getUltimaHoraOtros() {
		return (this.ultimaHoraOtros);
	}

	public void setUltimaHoraOtros(final int ultimaHoraOtros) {
		this.ultimaHoraOtros = ultimaHoraOtros;
	}

	public double getVentaDia() {
		return (this.ventaDia);
	}

	public void setVentaDia(final double ventaDia) {
		this.ventaDia = ventaDia;
	}

	public int getDomiciliariosInternos() {
		return (this.domiciliariosInternos);
	}

	public void setDomiciliariosInternos(final int domiciliariosInternos) {
		this.domiciliariosInternos = domiciliariosInternos;
	}

	public int getDomiciliariosExternos() {
		return (this.domiciliariosExternos);
	}

	public void setDomiciliariosExternos(final int domiciliariosExternos) {
		this.domiciliariosExternos = domiciliariosExternos;
	}

	public int getPedidosDescuadrados() {
		return (this.pedidosDescuadrados);
	}

	public void setPedidosDescuadrados(final int pedidosDescuadrados) {
		this.pedidosDescuadrados = pedidosDescuadrados;
	}

	public boolean getSeLeyo() {
		return (this.seLeyo);
	}

	public void setSeLeyo(final boolean seLeyo) {
		this.seLeyo = seLeyo;
	}

	public ArrayList<String[]> getProgramados() {
		return (this.programados);
	}

	public void setProgramados(final ArrayList<String[]> programados) {
		this.programados = programados;
	}
}
