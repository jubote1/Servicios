package ServiciosSer;

import java.sql.Connection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import capaDAOCC.CorreoRebotadoDAO;
import capaDAOCC.IntegracionCRMDAO;
import capaDAOCC.ParametrosDAO;
import capaModeloCC.IntegracionCRM;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Le pregunta a Brevo que correos rebotaron y los deja marcados.
 *
 * POR QUE
 *
 * Una direccion muerta se intentaba en CADA campana, para siempre: nada lo
 * aprendia. Lo que se protege no es el cupo de Brevo sino la reputacion del
 * dominio, que es lo primero que miran Gmail y Outlook para decidir si algo es
 * spam. Ahi no se pierde el correo de la campana: se pierde el de las facturas.
 *
 * POR QUE BREVO Y NO EL BUZON
 *
 * Brevo ya clasifica los rebotes y distingue el duro -la direccion no existe-
 * del blando -buzon lleno-. Leer el buzon para sacar los rebotes del correo
 * directo tambien sirve, pero toca parsear mensajes de mailer-daemon cuyo
 * formato cambia con cada proveedor. Eso queda para una segunda etapa; el
 * volumen esta aqui.
 *
 * NO BORRA EL CORREO DEL CLIENTE
 *
 * Solo lo marca como "no enviar". El saldo de puntos se lleva por la cadena del
 * correo, asi que borrarlo le romperia los puntos a esa persona.
 */
public class ProcesoRebotesBrevo {

	/** Cuantos eventos pide por pagina. Brevo admite hasta 100. */
	private static final int POR_PAGINA = 100;

	/** Tope de paginas por corrida, para que una corrida no se vuelva eterna. */
	private static final int MAXIMO_PAGINAS = 50;

	/** Si nunca se ha corrido, cuantos dias hacia atras mirar la primera vez. */
	private static final int DIAS_PRIMERA_VEZ = 90;

	public static void main(final String[] args) {
		final long arranque = System.currentTimeMillis();

		final IntegracionCRM brevo = IntegracionCRMDAO.obtenerInformacionIntegracion("BREVO");
		if (brevo == null || brevo.getAccessToken() == null
				|| brevo.getAccessToken().trim().length() == 0) {
			System.out.println("No hay llave de Brevo configurada. No se hace nada.");
			return;
		}
		final String apiKey = brevo.getAccessToken().trim();

		//Desde cuando se pregunta. Se guarda el ultimo dia consultado para no
		//volver a pedir el historico completo en cada corrida.
		String desde = ParametrosDAO.retornarValorAlfanumerico("BREVOREBOTESDESDE");
		if (desde == null || desde.trim().length() < 10) {
			desde = hace(DIAS_PRIMERA_VEZ);
			System.out.println("Primera corrida: se mira desde " + desde);
		} else {
			//Se repite el ultimo dia a proposito: un evento de la noche pudo
			//quedar fuera de la corrida anterior. Anotar dos veces el mismo
			//rebote no hace dano, solo le suma al contador.
			desde = desde.trim().substring(0, 10);
		}
		final String hasta = hoy();
		System.out.println("Rebotes de Brevo entre " + desde + " y " + hasta);

		final Connection con = new conexionCC.ConexionBaseDatos().obtenerConexionBDPrincipal();
		if (con == null) {
			System.out.println("No se pudo conectar al central. No se hace nada.");
			return;
		}

		int duros = 0;
		int blandos = 0;
		try {
			duros = traer(con, apiKey, "hardBounces", "DURO", desde, hasta);
			blandos = traer(con, apiKey, "softBounces", "BLANDO", desde, hasta);

			//Solo se mueve la marca si todo salio bien: si fallo a mitad, la
			//proxima corrida vuelve a pedir el mismo rango y no se pierde nada.
			guardarUltimaFecha(con, hasta);

			System.out.println("duros: " + duros + ", blandos: " + blandos);
			final CorreoRebotadoDAO.Resumen r = CorreoRebotadoDAO.resumen();
			System.out.println("acumulado: " + r.duros + " duros, " + r.blandos + " blandos");
			System.out.println("duracion: " + ((System.currentTimeMillis() - arranque) / 1000) + " s");
		} catch (final Exception e) {
			System.out.println("Fallo el proceso de rebotes: " + e.toString());
		} finally {
			try {
				con.close();
			} catch (final Exception e) {
			}
		}

		//SALIDA EXPLICITA. Sin esto el proceso termina su trabajo pero no
		//devuelve el prompt: el cliente HTTP compartido -ClientesHttp.ok()-
		//deja vivos los hilos de su pool de conexiones y su dispatcher, y
		//mientras no sean demonios la JVM no se cierra. Se quedaba colgado
		//varios minutos despues de imprimir la duracion.
		//
		//No se le puede llamar close() al cliente: esta compartido y cerrarlo
		//lo dejaria inservible para quien lo use despues en el mismo proceso.
		//En un servicio por lote, que termina cuando termina, salir es lo
		//correcto; en el war no aplica porque la aplicacion sigue corriendo.
		System.exit(0);
	}

	/**
	 * Trae de Brevo los eventos de un tipo y los anota.
	 *
	 * Va por paginas porque la API devuelve un maximo por llamada. Se para
	 * cuando una pagina viene vacia o cuando se llega al tope de paginas: sin
	 * ese tope, un parametro mal puesto haria girar esto toda la noche.
	 */
	private static int traer(final Connection con, final String apiKey, final String evento,
			final String tipo, final String desde, final String hasta) {
		final OkHttpClient cliente = utilidadesCC.ClientesHttp.ok();
		int anotados = 0;
		for (int pagina = 0; pagina < MAXIMO_PAGINAS; pagina++) {
			final String url = "https://api.brevo.com/v3/smtp/statistics/events"
					+ "?limit=" + POR_PAGINA
					+ "&offset=" + (pagina * POR_PAGINA)
					+ "&startDate=" + desde
					+ "&endDate=" + hasta
					+ "&event=" + evento;
			final Request peticion = new Request.Builder().url(url)
					.addHeader("accept", "application/json")
					.addHeader("api-key", apiKey)
					.build();

			int enEstaPagina = 0;
			try (Response respuesta = cliente.newCall(peticion).execute()) {
				if (!respuesta.isSuccessful()) {
					System.out.println("  Brevo respondio " + respuesta.code() + " en " + evento
							+ ", pagina " + pagina + ". Se corta.");
					break;
				}
				final String cuerpo = respuesta.body() == null ? "" : respuesta.body().string();
				final JsonElement raiz = JsonParser.parseString(cuerpo);
				if (raiz == null || !raiz.isJsonObject()) {
					break;
				}
				final JsonObject objeto = raiz.getAsJsonObject();
				if (!objeto.has("events") || !objeto.get("events").isJsonArray()) {
					break;
				}
				final JsonArray eventos = objeto.getAsJsonArray("events");
				for (int i = 0; i < eventos.size(); i++) {
					final JsonObject e = eventos.get(i).getAsJsonObject();
					final String correo = texto(e, "email");
					if (correo.length() == 0) {
						continue;
					}
					final String motivo = texto(e, "reason");
					if (CorreoRebotadoDAO.anotar(con, correo, tipo,
							motivo.length() > 0 ? motivo : evento, "BREVO")) {
						anotados++;
					}
					enEstaPagina++;
				}
			} catch (final Exception ex) {
				System.out.println("  fallo pidiendo " + evento + ", pagina " + pagina + ": "
						+ ex.toString());
				break;
			}
			if (enEstaPagina < POR_PAGINA) {
				break;
			}
		}
		System.out.println("  " + evento + ": " + anotados + " anotados");
		return (anotados);
	}

	/**
	 * Deja dicho hasta donde se pregunto.
	 *
	 * Va por la conexion que ya esta abierta y no por ParametrosDAO porque ese
	 * EditarParametro pide un Parametro completo y pisaria el valor numerico y
	 * la descripcion que ya tiene la fila.
	 */
	private static void guardarUltimaFecha(final Connection con, final String hasta) {
		try (java.sql.PreparedStatement ps = con.prepareStatement(
				"UPDATE general.parametros SET valortexto = ? WHERE valorparametro = ?")) {
			ps.setString(1, hasta);
			ps.setString(2, "BREVOREBOTESDESDE");
			ps.executeUpdate();
		} catch (final Exception e) {
			System.out.println("No se pudo guardar BREVOREBOTESDESDE: " + e.toString());
		}
	}

	private static String texto(final JsonObject o, final String campo) {
		if (o == null || !o.has(campo) || o.get(campo).isJsonNull()) {
			return ("");
		}
		return (o.get(campo).getAsString());
	}

	private static String hoy() {
		return (new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
	}

	private static String hace(final int dias) {
		final java.util.Calendar c = java.util.Calendar.getInstance();
		c.add(java.util.Calendar.DAY_OF_MONTH, -dias);
		return (new java.text.SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));
	}
}
