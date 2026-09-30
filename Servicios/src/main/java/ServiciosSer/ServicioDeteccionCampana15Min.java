package ServiciosSer;

import java.util.ArrayList;

import CapaDAOSer.Campana15MinDAO;
import ModeloSer.Campana15MinAplicado;

/**
 * Deteccion silenciosa de incumplimientos de la campana "15 minutos o
 * gratis". Se ejecuta como tarea programada cada pocos minutos (5 es
 * razonable dado que la ventana tipica son 15).
 *
 * No avisa por correo ni interrumpe a nadie: por cada pedido de punto de
 * venta con la campana aplicada que ya supero los minutos prometidos sin
 * evaluar, calcula el monto a devolver (el valor base de la pizza, menos el
 * porcentaje de retencion configurado si el pago fue por medio virtual) y lo
 * deja en la cola de revision del central para que Servicio al Cliente
 * decida. Nunca mueve dinero.
 */
public class ServicioDeteccionCampana15Min {

	public static void main(final String[] args) {
		final ArrayList<Campana15MinAplicado> vencidos = Campana15MinDAO.obtenerVencidosSinEvaluar();
		System.out.println("ServicioDeteccionCampana15Min: " + vencidos.size() + " pedidos vencidos sin evaluar");
		for (int i = 0; i < vencidos.size(); i++) {
			final Campana15MinAplicado aplicado = vencidos.get(i);
			try {
				if (!Campana15MinDAO.existeIncumplimiento(aplicado.getIdPedidoTienda(), aplicado.getIdTienda())) {
					final double porcentajeRetencion = aplicado.esMedioVirtual()
							? Campana15MinDAO.obtenerPorcentajeRetencion(aplicado.getIdCampana())
							: 0;
					final double retencionAplicada = aplicado.getValorBasePizza() * (porcentajeRetencion / 100.0);
					final double valorADevolver = aplicado.getValorBasePizza() - retencionAplicada;
					Campana15MinDAO.insertarIncumplimiento(aplicado.getIdPedidoTienda(), aplicado.getIdTienda(),
							aplicado.getFechaHoraInicio(), aplicado.getValorBasePizza(), retencionAplicada,
							valorADevolver);
				}
				//No hay forma de confirmar si se entrego a tiempo (evento fisico, no
				//queda en el sistema): se marca 'N' siempre que se vence, y el caso
				//queda en manos de la revision humana decidir si de verdad incumplio.
				Campana15MinDAO.marcarEvaluado(aplicado.getIdPedidoTienda(), aplicado.getIdTienda(), false);
			} catch (final Exception e) {
				System.out.println("ServicioDeteccionCampana15Min pedido " + aplicado.getIdPedidoTienda() + ": "
						+ e.toString());
			}
		}
	}
}
