package ServiciosSer;

import capaControladorCC.BoldEntregaTiendaCtrl;

/**
 * Reintenta entregar a su tienda los eventos de Bold que no llegaron: porque la
 * tienda estaba apagada o sin red cuando el webhook los recibio, o porque su
 * sede todavia no estaba registrada en bold_sede_tienda (en el central).
 *
 * El webhook ya intenta la entrega en el momento; esto solo recoge lo que
 * quedo pendiente. Es seguro correrlo seguido: cada evento se guarda una sola
 * vez en la tienda. Va en el crontab cada pocos minutos (por ejemplo cada 5).
 *
 * Jar propio, Main-Class = ServiciosSer.ServicioReintentoEventosBold.
 */
public class ServicioReintentoEventosBold {

	public static void main(final String[] args) {
		BoldEntregaTiendaCtrl.reintentarPendientes();
	}

}
