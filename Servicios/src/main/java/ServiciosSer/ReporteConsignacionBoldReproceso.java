package ServiciosSer;

import CapaDAOSer.ParametrosDAO;

/**
 * Reproceso de la consignacion diaria de BOLD: corre como si hoy fuera
 * general.parametros.FECHAREPROCESO (yyyy-MM-dd), y por lo tanto reporta el
 * dia anterior a esa fecha.
 *
 * UPDATE general.parametros SET valortexto = '2026-09-21'
 * WHERE valorparametro = 'FECHAREPROCESO';  -- reporta el 2026-09-20
 */
public class ReporteConsignacionBoldReproceso {

	public static void main(final String[] args) {
		final String fechaReproceso = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
		ReporteConsignacionBold.generar(fechaReproceso);
	}

}
