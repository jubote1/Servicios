package ServiciosSer;

import capaControladorCC.DidiEntregaTiendaCtrl;

/**
 * Reintenta subirle a su tienda los cambios de estado de DiDi que no llegaron:
 * porque la tienda estaba apagada o sin red cuando el webhook recibio el evento.
 *
 * DiDi avisa que el domiciliario recogio (delivery_status 140) o que entrego
 * (160), y el central traduce eso en una llamada HTTP al servicio de la tienda.
 * El webhook ya intenta la entrega en el momento; esto solo recoge lo que quedo
 * pendiente en didi_estado_pendiente.
 *
 * Antes no existia: la llamada iba en un try cuyo catch hacia solo un
 * System.out.println, asi que un pedido que no subia de estado no se
 * reintentaba y no quedaba en ninguna parte. Del 28 al 30 de octubre de 2026 le
 * pasaba a 1 o 2 pedidos por tienda por dia.
 *
 * Es seguro correrlo seguido: la llave es (orden, estado) y lo ya entregado no
 * se vuelve a mandar. Va en el crontab cada pocos minutos, como el de Bold.
 *
 * Jar propio, Main-Class = ServiciosSer.ServicioReintentoEstadosDidi.
 */
public class ServicioReintentoEstadosDidi {

	public static void main(final String[] args) {
		DidiEntregaTiendaCtrl.reintentarPendientes();
	}

}
