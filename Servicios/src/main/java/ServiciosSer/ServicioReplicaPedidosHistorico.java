package ServiciosSer;

import CapaDAOSer.ParametrosDAO;

/**
 * La replica HISTORICA al datamart: lo mismo que ServicioReplicaPedidos, pero mirando mas dias hacia atras,
 * para traer lo que haya quedado sin replicar (una tienda que estuvo apagada varios dias, tablas nuevas...).
 *
 * Antes tenia su propia copia del codigo de la replica, con sus mismos defectos: contaba "EXITOSO" lo que
 * solo habia LEIDO de la tienda y su correo ni siquiera hablaba de los despachos. Ahora es el mismo motor
 * que el proceso diario (ver ServicioReplicaPedidos y ReplicaDatamartDAO), asi que trae las mismas tablas,
 * es idempotente (lo que el datamart ya tiene no se toca) y manda el mismo correo en matriz.
 *
 * Cuantos dias: el primer argumento; si no se da, el parametro CANTIDADDIASPEDIDOS del central (20 si no existe).
 *
 *   java -jar ServicioReplicaPedidosHistorico.jar 45
 *
 * Como siempre, no replica la Bodega (idtienda 12).
 */
public class ServicioReplicaPedidosHistorico {

	private static final int DIAS_POR_DEFECTO = 20;
	private static final int DIAS_MAXIMO = 365;

	public static void main(String[] args) {
		new ServicioReplicaPedidosHistorico().generarReplicaPedidosHistoricos(diasPedidos(args));
	}

	/** Se conserva con este nombre: asi lo llamaba lo que ya lo usara. Toma los dias del parametro. */
	public void generarReplicaPedidosHistoricos() {
		generarReplicaPedidosHistoricos(diasPedidos(null));
	}

	public void generarReplicaPedidosHistoricos(int dias) {
		new ServicioReplicaPedidos().ejecutar(dias, true, "HISTORICO");
	}

	/** El argumento si viene y es valido; si no, el parametro del central; si no, 20. */
	static int diasPedidos(String[] args) {
		int dias = 0;
		if (args != null && args.length > 0) {
			try {
				dias = Integer.parseInt(args[0].trim());
			} catch (NumberFormatException e) {
				System.out.println("Argumento de dias no valido (" + args[0] + "), se usa el parametro del central.");
			}
		}
		if (dias <= 0) {
			try {
				dias = ParametrosDAO.retornarValorNumerico("CANTIDADDIASPEDIDOS");
			} catch (Exception e) {
				dias = 0;
			}
		}
		if (dias <= 0) {
			dias = DIAS_POR_DEFECTO;
		}
		return Math.min(dias, DIAS_MAXIMO);
	}
}
