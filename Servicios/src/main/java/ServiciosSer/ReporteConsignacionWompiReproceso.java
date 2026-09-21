package ServiciosSer;

import CapaDAOSer.ParametrosDAO;

/**
 * Reproceso de la consignacion diaria de Wompi: corre como si hoy fuera
 * general.parametros.FECHAREPROCESO (yyyy-MM-dd). La logica es la misma de
 * ReporteConsignacionWompi.
 */
public class ReporteConsignacionWompiReproceso {

	public static void main(final String[] args) {
		final String fechaReproceso = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
		ReporteConsignacionWompi.generar(fechaReproceso);
	}

}
