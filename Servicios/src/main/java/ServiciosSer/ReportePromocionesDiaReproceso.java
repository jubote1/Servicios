package ServiciosSer;

/**
 * Reprocesa el reporte diario de promociones para el dia guardado en
 * general.parametros.FECHAREPROCESO (aaaa-mm-dd). Toda la logica vive en
 * ReportePromocionesDia: aqui no se duplica nada, solo se dispara.
 *
 * UPDATE general.parametros SET valortexto = '2026-09-27'
 * WHERE valorparametro = 'FECHAREPROCESO';
 *
 * Es el MISMO parametro de los demas reprocesos: uno solo para toda la casa.
 *
 * Reprocesar un dia lo CORRIGE, no lo duplica: datamart.promocion_dia tiene la
 * llave en (fecha, promocion, tienda, canal) y el guardado va con ON DUPLICATE
 * KEY UPDATE. Sirve para rellenar un dia en que alguna tienda no respondio.
 */
public class ReportePromocionesDiaReproceso {

	public static void main(final String[] args) {
		new ReportePromocionesDia().reprocesar();
	}
}
