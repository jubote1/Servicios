package ServiciosSer;

/**
 * La copia "temporal" del historico. Hacia exactamente lo mismo que ServicioReplicaPedidosHistorico (con el
 * mismo codigo duplicado y los mismos defectos), asi que ahora simplemente lo usa. Se deja la clase para no
 * romper la tarea o el jar que la nombre.
 */
public class ServicioReplicaPedidosHistoricoTemp {

	public static void main(String[] args) {
		new ServicioReplicaPedidosHistoricoTemp().generarReplicaPedidosHistoricos(
				ServicioReplicaPedidosHistorico.diasPedidos(args));
	}

	public void generarReplicaPedidosHistoricos() {
		generarReplicaPedidosHistoricos(ServicioReplicaPedidosHistorico.diasPedidos(null));
	}

	public void generarReplicaPedidosHistoricos(int dias) {
		new ServicioReplicaPedidos().ejecutar(dias, true, "HISTORICO");
	}
}
