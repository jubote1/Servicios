package ModeloSer;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un caso identificado con indicios de mal manejo, abuso o fraude
 * en la acumulación y/o redención del programa de fidelización de Pizza Americana.
 * 
 * Permite auditar:
 * - Si la cuenta pertenece a un empleado de nómina/tienda (general.empleado).
 * - Si el cajero/usuario que facturó es el mismo titular (auto-acumulación).
 * - Si presenta más de 1 acumulación en un solo día calendario.
 * - Si presenta más de 3 acumulaciones en la semana.
 * - Si registra redenciones de puntos asociadas en el mismo período.
 */
public class CasoSospechosoFidelizacion {

	private String correo = "";
	private String empleadoNombre = "";
	private String empleadoUsuario = "";
	private boolean esEmpleado = false;
	private int totalAcumulaciones = 0;
	private int diasActivos = 0;
	private int maxDia = 0;
	private double totalPuntos = 0.0;
	private double totalVenta = 0.0;
	private String tiendas = "";
	private String cajeros = "";
	private String pedidos = "";
	private int autoCajeroCount = 0;
	private double puntosRedimidosSemana = 0.0;
	private String nivelRiesgo = "MEDIO";
	private List<String> motivos = new ArrayList<String>();

	public CasoSospechosoFidelizacion() {
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(final String correo) {
		this.correo = correo == null ? "" : correo.trim();
	}

	public String getEmpleadoNombre() {
		return empleadoNombre;
	}

	public void setEmpleadoNombre(final String empleadoNombre) {
		this.empleadoNombre = empleadoNombre == null ? "" : empleadoNombre.trim();
	}

	public String getEmpleadoUsuario() {
		return empleadoUsuario;
	}

	public void setEmpleadoUsuario(final String empleadoUsuario) {
		this.empleadoUsuario = empleadoUsuario == null ? "" : empleadoUsuario.trim();
	}

	public boolean isEsEmpleado() {
		return esEmpleado;
	}

	public void setEsEmpleado(final boolean esEmpleado) {
		this.esEmpleado = esEmpleado;
	}

	public int getTotalAcumulaciones() {
		return totalAcumulaciones;
	}

	public void setTotalAcumulaciones(final int totalAcumulaciones) {
		this.totalAcumulaciones = totalAcumulaciones;
	}

	public int getDiasActivos() {
		return diasActivos;
	}

	public void setDiasActivos(final int diasActivos) {
		this.diasActivos = diasActivos;
	}

	public int getMaxDia() {
		return maxDia;
	}

	public void setMaxDia(final int maxDia) {
		this.maxDia = maxDia;
	}

	public double getTotalPuntos() {
		return totalPuntos;
	}

	public void setTotalPuntos(final double totalPuntos) {
		this.totalPuntos = totalPuntos;
	}

	public double getTotalVenta() {
		return totalVenta;
	}

	public void setTotalVenta(final double totalVenta) {
		this.totalVenta = totalVenta;
	}

	public String getTiendas() {
		return tiendas;
	}

	public void setTiendas(final String tiendas) {
		this.tiendas = tiendas == null ? "" : tiendas.trim();
	}

	public String getCajeros() {
		return cajeros;
	}

	public void setCajeros(final String cajeros) {
		this.cajeros = cajeros == null ? "" : cajeros.trim();
	}

	public String getPedidos() {
		return pedidos;
	}

	public void setPedidos(final String pedidos) {
		this.pedidos = pedidos == null ? "" : pedidos.trim();
	}

	public int getAutoCajeroCount() {
		return autoCajeroCount;
	}

	public void setAutoCajeroCount(final int autoCajeroCount) {
		this.autoCajeroCount = autoCajeroCount;
	}

	public double getPuntosRedimidosSemana() {
		return puntosRedimidosSemana;
	}

	public void setPuntosRedimidosSemana(final double puntosRedimidosSemana) {
		this.puntosRedimidosSemana = puntosRedimidosSemana;
	}

	public String getNivelRiesgo() {
		return nivelRiesgo;
	}

	public void setNivelRiesgo(final String nivelRiesgo) {
		this.nivelRiesgo = nivelRiesgo;
	}

	public List<String> getMotivos() {
		return motivos;
	}

	public void setMotivos(final List<String> motivos) {
		this.motivos = motivos;
	}

	public void agregarMotivo(final String motivo) {
		if (motivo != null && !motivo.trim().isEmpty()) {
			this.motivos.add(motivo.trim());
		}
	}
}
