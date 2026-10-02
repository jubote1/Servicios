package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PromocionReporteDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import capaModeloCC.Tienda;
import utilidadesSer.ControladorEnvioCorreo;
import utilidadesSer.PlantillaCorreoPromociones;

/**
 * Reporte diario de promociones, version nueva.
 *
 * Reemplaza a ReportePromocionesDiarias, que son 806 lineas con trece bloques
 * copiados y pegados, cada uno con su promocion quemada. NO lo apaga: los dos
 * pueden correr en paralelo unos dias para comparar antes de jubilar el viejo.
 *
 * QUE HACE DISTINTO
 *
 *   Lee el catalogo        agregar una promocion es una fila, no codigo nuevo
 *   Mide en las tiendas    una sola consulta por tienda, todos los canales,
 *                          incluido el mostrador que el central no ve
 *   Cuenta cantidades      no divide plata entre un precio quemado
 *   Guarda la historia     datamart.promocion_dia, que es lo que permite
 *                          comparar contra el mismo dia de semanas anteriores
 *   Manda UNA tabla        no trece
 *
 * SI UNA TIENDA NO RESPONDE, NO SE GUARDA EL DIA
 *
 * Un dia guardado a medias se convierte manana en la referencia contra la que
 * se compara, y nadie se acordaria de que ese dia faltaban dos tiendas. Es
 * preferible no tener el dato que tener uno que miente. El correo si sale,
 * diciendo arriba cuales faltaron, y el dia se puede reprocesar despues.
 */
public class ReportePromocionesDia {

	/** Destinatarios, en general.parametros_correo. */
	private static final String PARAM_CORREOS = "REPORTEPROMOCIONESDIA";

	/** Fecha de corte para el reproceso. El mismo de siempre. */
	private static final String PARAM_FECHA_REPROCESO = "FECHAREPROCESO";

	private static final String[] DIAS = { "", "domingo", "lunes", "martes", "miercoles",
			"jueves", "viernes", "sabado" };

	public static void main(final String[] args) {
		new ReportePromocionesDia().generar(ayer());
	}

	/** Reprocesa el dia que diga FECHAREPROCESO. */
	public void reprocesar() {
		final String fecha = ParametrosDAO.retornarValorAlfanumerico(PARAM_FECHA_REPROCESO);
		if (fecha == null || fecha.trim().length() < 10) {
			System.out.println("ReportePromocionesDia: " + PARAM_FECHA_REPROCESO
					+ " no tiene una fecha utilizable: '" + fecha + "'");
			return;
		}
		this.generar(fecha.trim());
	}

	/**
	 * El dia de ayer.
	 *
	 * El proceso corre de madrugada, asi que el dia que hay que reportar es el
	 * anterior. El reporte viejo usa la fecha de hoy y por eso hay que correrlo
	 * antes de medianoche.
	 */
	private static String ayer() {
		final Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, -1);
		return (new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));
	}

	public void generar(final String fecha) {
		System.out.println("ReportePromocionesDia: reportando " + fecha);

		final ArrayList<PromocionReporteDAO.Promocion> catalogo = PromocionReporteDAO.obtenerCatalogo();
		if (catalogo.isEmpty()) {
			System.out.println("ReportePromocionesDia: el catalogo esta vacio. "
					+ "Revisar pizzaamericana.promocion_reporte.");
			return;
		}
		System.out.println("ReportePromocionesDia: " + catalogo.size() + " promociones en el catalogo");

		final ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendas();
		final ArrayList<PromocionReporteDAO.Venta> todas = new ArrayList<PromocionReporteDAO.Venta>();
		final ArrayList<String> sinResponder = new ArrayList<String>();

		for (int i = 0; i < tiendas.size(); i++) {
			final Tienda tienda = tiendas.get(i);
			if (tienda.getHosbd() == null || tienda.getHosbd().trim().length() == 0) {
				continue;
			}
			final ArrayList<PromocionReporteDAO.Venta> delDia = PromocionReporteDAO.obtenerVentaDelDia(
					tienda.getHosbd(), tienda.getIdTienda(), fecha, catalogo);
			if (delDia == null) {
				sinResponder.add(tienda.getNombreTienda());
				continue;
			}
			todas.addAll(delDia);
		}

		//---- La historia solo si el dia esta completo ----------------------
		if (sinResponder.isEmpty()) {
			final ArrayList<PromocionReporteDAO.Venta> agrupadas = agruparPorPromoTiendaCanal(todas);
			final int guardadas = PromocionReporteDAO.guardarDia(fecha, agrupadas);
			System.out.println("ReportePromocionesDia: " + guardadas + " filas guardadas en promocion_dia");
		} else {
			System.out.println("ReportePromocionesDia: NO se guarda el dia, no respondieron "
					+ sinResponder.size() + " tienda(s): " + sinResponder);
		}

		//---- El resumen que va al correo ----------------------------------
		final ArrayList<PromocionReporteDAO.Resumen> resumenes = resumir(catalogo, todas);
		PromocionReporteDAO.completarPromedios(fecha, resumenes);

		double totalUnidades = 0;
		for (int i = 0; i < resumenes.size(); i++) {
			totalUnidades += resumenes.get(i).unidades;
		}

		final String porTienda = detallePorTienda(catalogo, tiendas, todas);
		final String nombreDia = nombreDelDia(fecha);
		final String cuerpo = PlantillaCorreoPromociones.cuerpo(fecha, nombreDia, resumenes,
				porTienda, sinResponder);

		enviar(PlantillaCorreoPromociones.asunto(fecha, totalUnidades), cuerpo);
		System.out.println("ReportePromocionesDia: listo. " + totalUnidades + " promociones vendidas.");
	}

	/**
	 * Suma las filas que comparten promocion, tienda y canal.
	 *
	 * Vienen separadas por producto -el Combo Insuperable son tres- y la tabla
	 * de historia tiene la llave en (fecha, promo, tienda, canal). Sin esto, la
	 * segunda fila pisaria a la primera en vez de sumarse.
	 */
	private ArrayList<PromocionReporteDAO.Venta> agruparPorPromoTiendaCanal(
			final ArrayList<PromocionReporteDAO.Venta> ventas) {
		final LinkedHashMap<String, PromocionReporteDAO.Venta> mapa =
				new LinkedHashMap<String, PromocionReporteDAO.Venta>();
		for (int i = 0; i < ventas.size(); i++) {
			final PromocionReporteDAO.Venta v = ventas.get(i);
			final String llave = v.idPromo + "|" + v.idTienda + "|" + v.canal;
			final PromocionReporteDAO.Venta ya = mapa.get(llave);
			if (ya == null) {
				final PromocionReporteDAO.Venta nueva = new PromocionReporteDAO.Venta();
				nueva.idPromo = v.idPromo;
				nueva.idTienda = v.idTienda;
				nueva.canal = v.canal;
				nueva.unidades = v.unidades;
				nueva.valor = v.valor;
				mapa.put(llave, nueva);
			} else {
				ya.unidades += v.unidades;
				ya.valor += v.valor;
			}
		}
		return (new ArrayList<PromocionReporteDAO.Venta>(mapa.values()));
	}

	/** Una fila por promocion del catalogo, incluso las que no vendieron. */
	private ArrayList<PromocionReporteDAO.Resumen> resumir(
			final ArrayList<PromocionReporteDAO.Promocion> catalogo,
			final ArrayList<PromocionReporteDAO.Venta> ventas) {
		final ArrayList<PromocionReporteDAO.Resumen> resumenes =
				new ArrayList<PromocionReporteDAO.Resumen>();
		for (int i = 0; i < catalogo.size(); i++) {
			final PromocionReporteDAO.Promocion promo = catalogo.get(i);
			final PromocionReporteDAO.Resumen r = new PromocionReporteDAO.Resumen();
			r.idPromo = promo.idPromo;
			r.nombre = promo.nombre;
			r.plataforma = promo.plataforma;
			for (int j = 0; j < ventas.size(); j++) {
				if (ventas.get(j).idPromo == promo.idPromo) {
					r.unidades += ventas.get(j).unidades;
					r.valor += ventas.get(j).valor;
				}
			}
			resumenes.add(r);
		}
		return (resumenes);
	}

	/**
	 * El detalle por tienda, solo de las promociones que se movieron.
	 *
	 * Una tabla de once tiendas por trece promociones son 143 celdas casi todas
	 * en cero. Se muestran solo las promociones con venta, y dentro de cada una
	 * solo las tiendas que vendieron: lo que esta en cero no necesita una fila
	 * para decirlo.
	 */
	private String detallePorTienda(final ArrayList<PromocionReporteDAO.Promocion> catalogo,
			final ArrayList<Tienda> tiendas, final ArrayList<PromocionReporteDAO.Venta> ventas) {

		final DecimalFormat formato = new DecimalFormat("###,###.#");
		final StringBuilder h = new StringBuilder();
		boolean hayAlgo = false;

		for (int i = 0; i < catalogo.size(); i++) {
			final PromocionReporteDAO.Promocion promo = catalogo.get(i);

			final LinkedHashMap<Integer, Double> porTienda = new LinkedHashMap<Integer, Double>();
			for (int j = 0; j < ventas.size(); j++) {
				final PromocionReporteDAO.Venta v = ventas.get(j);
				if (v.idPromo != promo.idPromo || v.unidades <= 0) {
					continue;
				}
				final Double ya = porTienda.get(Integer.valueOf(v.idTienda));
				porTienda.put(Integer.valueOf(v.idTienda),
						Double.valueOf((ya == null ? 0 : ya.doubleValue()) + v.unidades));
			}
			if (porTienda.isEmpty()) {
				continue;
			}
			if (!hayAlgo) {
				hayAlgo = true;
				h.append("<p style=\"font-size:13px;font-weight:bold;color:#102F6F;margin:22px 0 6px;\">")
				 .append("Qui&eacute;n las vendi&oacute;</p>");
				h.append("<table style=\"border-collapse:collapse;font-size:12.5px;\">");
			}
			h.append("<tr><td style=\"padding:5px 10px 5px 0;vertical-align:top;white-space:nowrap;\">")
			 .append("<strong>").append(escapar(promo.nombre)).append("</strong></td><td style=\"padding:5px 0;\">");
			boolean primera = true;
			for (int t = 0; t < tiendas.size(); t++) {
				final Tienda tienda = tiendas.get(t);
				final Double cantidad = porTienda.get(Integer.valueOf(tienda.getIdTienda()));
				if (cantidad == null) {
					continue;
				}
				if (!primera) {
					h.append("&nbsp;&nbsp;&middot;&nbsp;&nbsp;");
				}
				primera = false;
				h.append(escapar(tienda.getNombreTienda())).append(" <strong>")
				 .append(formato.format(cantidad.doubleValue())).append("</strong>");
			}
			h.append("</td></tr>");
		}
		if (hayAlgo) {
			h.append("</table>");
		}
		return (h.toString());
	}

	private String nombreDelDia(final String fecha) {
		try {
			final Date d = new SimpleDateFormat("yyyy-MM-dd").parse(fecha);
			final Calendar c = Calendar.getInstance();
			c.setTime(d);
			return (DIAS[c.get(Calendar.DAY_OF_WEEK)]);
		} catch (final Exception e) {
			return ("");
		}
	}

	private void enviar(final String asunto, final String cuerpo) {
		try {
			final ArrayList correos = GeneralDAO.obtenerCorreosParametro(PARAM_CORREOS);
			if (correos == null || correos.isEmpty()) {
				System.out.println("ReportePromocionesDia: no hay destinatarios en parametros_correo para "
						+ PARAM_CORREOS + ". No se envia correo.");
				return;
			}
			final CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo(
					"CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			final Correo correo = new Correo();
			correo.setAsunto(asunto);
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setMensaje(cuerpo);
			final boolean enviado = new ControladorEnvioCorreo(correo, correos).enviarCorreoHTML();
			System.out.println("ReportePromocionesDia: " + (enviado
					? "correo enviado a " + correos.size() + " destinatario(s)."
					: "NO se pudo enviar el correo."));
		} catch (final Exception e) {
			//Un problema con el correo no puede perder lo que ya se guardo.
			System.out.println("ReportePromocionesDia.enviar: " + e.toString());
		}
	}

	private String escapar(final String texto) {
		if (texto == null) {
			return ("");
		}
		return (texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
	}
}
