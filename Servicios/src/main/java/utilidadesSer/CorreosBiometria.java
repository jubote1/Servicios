package utilidadesSer;

import java.util.ArrayList;

import ModeloSer.NovedadBiometria;

/**
 * Arma el HTML de los correos de novedades de biometria.
 *
 * Esta aparte del proceso para poder verla renderizada sin tener que correr el
 * envio, y porque el proceso no deberia saber de colores ni de tablas.
 *
 * Va con tablas y estilos en linea a proposito: Outlook ignora buena parte del
 * CSS moderno y un correo que se ve bien en el navegador llega descuadrado a la
 * bandeja de quien lo tiene que leer. Los atributos usan comilla simple, que en
 * HTML es valida, para no llenar el Java de escapes.
 */
public class CorreosBiometria {

	//Colores del manual de marca de Pizza Americana.
	private static final String AZUL = "#102F6F";
	private static final String ROJO = "#E42528";
	private static final String AMARILLO = "#FDC806";
	private static final String GRIS = "#F4F5F7";
	private static final String TEXTO = "#333333";
	/** Color de la etiqueta segun el tipo de novedad. */
	private static String colorNovedad(final String tipoNovedad) {
		if (tipoNovedad.equals(NovedadBiometria.INGRESO_TARDIO)) {
			return(CorreosBiometria.AMARILLO);
		}
		return(CorreosBiometria.ROJO);
	}

	/**
	 * Correo para el empleado. Va con tablas y estilos en linea a proposito: Outlook
	 * ignora buena parte del CSS moderno. Los atributos usan comilla simple, que en
	 * HTML es valida, para no llenar el Java de escapes.
	 */
	public static String armarCorreoEmpleado(final ArrayList<NovedadBiometria> delEmpleado, final String fecha) {
		final NovedadBiometria primera = delEmpleado.get(0);
		final StringBuilder m = new StringBuilder();
		m.append("<div style='font-family:Arial,Helvetica,sans-serif;font-size:14px;color:")
				.append(CorreosBiometria.TEXTO).append(";'>");
		m.append("<table cellpadding='14' cellspacing='0' border='0' width='600'>");
		m.append("<tr><td style='background-color:").append(CorreosBiometria.AZUL)
				.append(";color:#FFFFFF;'>");
		m.append("<span style='font-size:18px;font-weight:bold;'>Novedad en su registro de biometria</span>");
		m.append("<br><span style='font-size:13px;'>").append(primera.getDia()).append(" ").append(fecha);
		m.append("</span></td></tr>");
		m.append("<tr><td style='height:4px;background-color:").append(CorreosBiometria.AMARILLO)
				.append(";'></td></tr>");
		m.append("</table>");

		m.append("<table cellpadding='14' cellspacing='0' border='0' width='600'><tr><td>");
		m.append("Hola <b>").append(primera.getNombreEmpleado()).append("</b>,<br><br>");
		if (delEmpleado.size() == 1) {
			m.append("Revisando los registros del huellero encontramos una novedad con su marcacion ");
		} else {
			m.append("Revisando los registros del huellero encontramos ").append(delEmpleado.size());
			m.append(" novedades con sus marcaciones ");
		}
		m.append("del <b>").append(primera.getDia().toLowerCase()).append(" ").append(fecha);
		m.append("</b> en <b>").append(primera.getNombreTienda()).append("</b>.");
		m.append("</td></tr></table>");

		m.append("<table cellpadding='12' cellspacing='0' border='0' width='600'>");
		for (int i = 0; i < delEmpleado.size(); i++) {
			final NovedadBiometria novedad = delEmpleado.get(i);
			m.append("<tr><td style='background-color:").append(CorreosBiometria.GRIS);
			m.append(";border-left:5px solid ").append(colorNovedad(novedad.getTipoNovedad())).append(";'>");
			m.append("<span style='font-size:12px;font-weight:bold;color:")
					.append(colorNovedad(novedad.getTipoNovedad())).append(";'>");
			m.append(novedad.getTipoNovedad()).append("</span><br>");
			m.append(novedad.getExplicacion());
			m.append("</td></tr><tr><td style='height:8px;'></td></tr>");
		}
		m.append("</table>");

		m.append("<table cellpadding='14' cellspacing='0' border='0' width='600'><tr><td>");
		m.append("<b>Que debe hacer:</b> avise en su punto de venta para que registren la novedad ");
		m.append("indicando la hora real en la que entro o salio. Mientras la novedad no quede ");
		m.append("registrada, esas horas no se pueden contar bien.<br><br>");
		m.append("<span style='font-size:12px;color:#888888;'>Este correo se genera automaticamente ");
		m.append("con los registros del huellero. Si cree que es un error, informelo igual para que ");
		m.append("quede la constancia.</span>");
		m.append("</td></tr></table>");
		m.append("</div>");
		return(m.toString());
	}

	/** Resumen general para operaciones y talento humano. */
	public static String armarCorreoGeneral(final ArrayList<NovedadBiometria> novedades,
			final ArrayList<NovedadBiometria> sinCorreo, final String fecha, final int avisados,
			final int horaTardia) {
		final StringBuilder m = new StringBuilder();
		m.append("<div style='font-family:Arial,Helvetica,sans-serif;font-size:14px;color:")
				.append(CorreosBiometria.TEXTO).append(";'>");
		m.append("<table cellpadding='14' cellspacing='0' border='0' width='820'>");
		m.append("<tr><td style='background-color:").append(CorreosBiometria.AZUL)
				.append(";color:#FFFFFF;'>");
		m.append("<span style='font-size:18px;font-weight:bold;'>Novedades de biometria</span>");
		m.append("<br><span style='font-size:13px;'>Dia revisado: ").append(fecha).append("</span>");
		m.append("</td></tr>");
		m.append("<tr><td style='height:4px;background-color:").append(CorreosBiometria.AMARILLO)
				.append(";'></td></tr>");
		m.append("</table>");

		m.append("<table cellpadding='10' cellspacing='0' border='0' width='820'><tr>");
		m.append("<td style='background-color:").append(CorreosBiometria.GRIS)
				.append(";'><b>").append(novedades.size()).append("</b> novedades &nbsp;|&nbsp; <b>")
				.append(avisados).append("</b> empleados avisados por correo");
		if (sinCorreo.size() > 0) {
			m.append(" &nbsp;|&nbsp; <b style='color:").append(CorreosBiometria.ROJO).append(";'>")
					.append(sinCorreo.size()).append("</b> sin correo registrado");
		}
		m.append("</td></tr></table>");

		m.append("<table cellpadding='8' cellspacing='0' border='0' width='820' ")
				.append("style='border-collapse:collapse;border:1px solid #DDDDDD;'>");
		m.append("<tr style='background-color:").append(CorreosBiometria.AZUL)
				.append(";color:#FFFFFF;'>");
		m.append("<td><b>Empleado</b></td><td><b>Tienda</b></td><td><b>Novedad</b></td>");
		m.append("<td><b>Evento registrado</b></td><td align='right'><b>Hora</b></td></tr>");
		for (int i = 0; i < novedades.size(); i++) {
			final NovedadBiometria novedad = novedades.get(i);
			final String fondo = (i % 2 == 0) ? "#FFFFFF" : CorreosBiometria.GRIS;
			m.append("<tr style='background-color:").append(fondo).append(";'>");
			m.append("<td style='border-bottom:1px solid #EEEEEE;'>").append(novedad.getIdEmpleado())
					.append(" - ").append(novedad.getNombreEmpleado()).append("</td>");
			m.append("<td style='border-bottom:1px solid #EEEEEE;'>").append(novedad.getNombreTienda())
					.append("</td>");
			m.append("<td style='border-bottom:1px solid #EEEEEE;color:")
					.append(colorNovedad(novedad.getTipoNovedad())).append(";font-weight:bold;'>")
					.append(novedad.getTipoNovedad()).append("</td>");
			m.append("<td style='border-bottom:1px solid #EEEEEE;'>").append(novedad.getTipoEvento())
					.append("</td>");
			m.append("<td align='right' style='border-bottom:1px solid #EEEEEE;'>")
					.append(novedad.getHoraEvento()).append("</td></tr>");
		}
		m.append("</table>");

		if (sinCorreo.size() > 0) {
			m.append("<table cellpadding='12' cellspacing='0' border='0' width='820'><tr>");
			m.append("<td style='background-color:#FDF3F3;border-left:5px solid ")
					.append(CorreosBiometria.ROJO).append(";'>");
			m.append("<b>A estas personas no se les pudo avisar</b> porque no tienen correo registrado ");
			m.append("en la ficha del empleado:<br>");
			for (int i = 0; i < sinCorreo.size(); i++) {
				m.append(sinCorreo.get(i).getIdEmpleado()).append(" - ")
						.append(sinCorreo.get(i).getNombreEmpleado()).append("<br>");
			}
			m.append("</td></tr></table>");
		}

		m.append("<p style='font-size:11px;color:#888888;'>Un ingreso se marca como tardio a partir ");
		m.append("de las ").append(horaTardia).append(" horas. Ese umbral se cambia en la tabla ");
		m.append("parametros del esquema general, en MONITOREOBIOHORATARDIA.</p>");
		m.append("</div>");
		return(m.toString());
	}
}
