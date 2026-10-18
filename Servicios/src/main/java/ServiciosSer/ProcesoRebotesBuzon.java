package ServiciosSer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;

import capaDAOCC.CorreoRebotadoDAO;
import capaDAOCC.ParametrosDAO;

/**
 * Lee los rebotes que llegan al buzon del correo directo y los deja marcados.
 *
 * POR QUE EXISTE, Y POR QUE NO BASTABA CON BREVO
 *
 * ProcesoRebotesBrevo aprende de los rebotes de las campanas, pero hay correos
 * que NO salen por Brevo: el link de pago sale por el Gmail de
 * CUENTACORREOWOMPI, directo por SMTP. Para el servidor SMTP ese envio es
 * exitoso -el correo se entrega al servidor de salida- y el rebote llega
 * minutos despues como un mensaje de vuelta al buzon. Nadie lo leia, asi que
 * nada se enteraba: una direccion muerta se podia reintentar para siempre. El
 * 2026-10-17 el link del pedido 2395879 salio unas once veces en dos minutos a
 * una direccion que no existe.
 *
 * QUE RESUELVE EL FORMATO ESTANDAR
 *
 * La duda al dejar esto para despues era tener que parsear mensajes de
 * mailer-daemon, cuyo texto cambia con cada proveedor y cada idioma. No hace
 * falta: un rebote de verdad viene como multipart/report con una parte
 * message/delivery-status (RFC 3464), y ahi los datos van en campos fijos
 * -Final-Recipient, Action, Status, Diagnostic-Code- que son iguales en Gmail,
 * Outlook y cualquier otro. Se lee ese bloque y no el texto para humanos.
 *
 * Y la clasificacion sale del mismo estandar, sin adivinar: el Status 5.x es
 * permanente (DURO, la direccion no existe) y el 4.x es temporal (BLANDO, buzon
 * lleno o servidor caido). Eso es exactamente la distincion que crm.correo_rebotado
 * necesita, y la que decide si se vuelve a intentar o no.
 *
 * NO TOCA EL BUZON
 *
 * La carpeta se abre en READ_ONLY a proposito. Ese buzon lo lee una persona, y
 * un proceso por lote no tiene por que marcarle correos como leidos ni moverlos:
 * si manana alguien busca el rebote del pedido 2395879, tiene que estar donde
 * estaba. Para no reprocesar se usa una marca de fecha propia, no el estado del
 * mensaje.
 *
 * ANTES DE LA PRIMERA CORRIDA
 *
 * La cuenta necesita IMAP habilitado, y si tiene verificacion en dos pasos, la
 * clave de CLAVECORREOWOMPI tiene que ser una clave de aplicacion. Si no, la
 * conexion falla con AuthenticationFailedException y el proceso lo dice.
 */
public class ProcesoRebotesBuzon {

	/** Si nunca se ha corrido, cuantos dias hacia atras mirar la primera vez. */
	private static final int DIAS_PRIMERA_VEZ = 30;

	/** Tope de mensajes por corrida, para que una corrida no se vuelva eterna. */
	private static final int MAXIMO_MENSAJES = 3000;

	/** Servidor IMAP por defecto, si no esta el parametro. */
	private static final String IMAP_POR_DEFECTO = "imap.gmail.com";

	public static void main(final String[] args) {
		final long arranque = System.currentTimeMillis();

		final String cuenta = ParametrosDAO.retornarValorAlfanumerico("CUENTACORREOWOMPI");
		final String clave = ParametrosDAO.retornarValorAlfanumerico("CLAVECORREOWOMPI");
		if (cuenta == null || cuenta.trim().length() == 0
				|| clave == null || clave.trim().length() == 0) {
			System.out.println("No hay cuenta o clave de correo configurada"
					+ " (CUENTACORREOWOMPI / CLAVECORREOWOMPI). No se hace nada.");
			return;
		}

		String servidor = ParametrosDAO.retornarValorAlfanumerico("IMAPSERVIDORREBOTES");
		if (servidor == null || servidor.trim().length() == 0) {
			servidor = IMAP_POR_DEFECTO;
		}
		servidor = servidor.trim();

		//Desde cuando se mira. Se repite el ultimo dia a proposito, igual que en
		//ProcesoRebotesBrevo: un rebote de la noche pudo quedar fuera de la
		//corrida anterior. Anotar dos veces el mismo rebote no hace dano, solo le
		//suma al contador de veces.
		String desdeTexto = ParametrosDAO.retornarValorAlfanumerico("REBOTESBUZONDESDE");
		final java.util.Date desde;
		if (desdeTexto == null || desdeTexto.trim().length() < 10) {
			desde = hace(DIAS_PRIMERA_VEZ);
			System.out.println("Primera corrida: se mira desde " + aTexto(desde));
		} else {
			desde = deTexto(desdeTexto.trim().substring(0, 10), hace(DIAS_PRIMERA_VEZ));
		}
		System.out.println("Rebotes en el buzon de " + cuenta + " desde " + aTexto(desde));

		final Connection con = new conexionCC.ConexionBaseDatos().obtenerConexionBDPrincipal();
		if (con == null) {
			System.out.println("No se pudo conectar al central. No se hace nada.");
			return;
		}

		Store almacen = null;
		Folder bandeja = null;
		try {
			final Properties props = new Properties();
			props.setProperty("mail.store.protocol", "imaps");
			props.setProperty("mail.imaps.host", servidor);
			props.setProperty("mail.imaps.port", "993");
			props.setProperty("mail.imaps.ssl.enable", "true");
			//Sin estos dos, una red lenta deja el proceso colgado sin decir nada.
			props.setProperty("mail.imaps.connectiontimeout", "20000");
			props.setProperty("mail.imaps.timeout", "60000");

			final Session sesion = Session.getInstance(props, null);
			almacen = sesion.getStore("imaps");
			almacen.connect(servidor, cuenta.trim(), clave.trim());

			bandeja = almacen.getFolder("INBOX");
			//READ_ONLY: el buzon es de una persona, no se le tocan los mensajes.
			bandeja.open(Folder.READ_ONLY);

			//La busqueda la resuelve el servidor (IMAP SEARCH SINCE), asi que no
			//se descargan los miles de mensajes viejos para descartarlos aqui.
			final Message[] mensajes = bandeja.search(
					new ReceivedDateTerm(ComparisonTerm.GE, desde));
			System.out.println("mensajes en el rango: " + mensajes.length);

			int revisados = 0;
			int rebotes = 0;
			int duros = 0;
			int blandos = 0;
			int noAnotados = 0;
			for (int i = 0; i < mensajes.length && revisados < MAXIMO_MENSAJES; i++) {
				revisados++;
				final String bloque = buscarDeliveryStatus(mensajes[i]);
				if (bloque == null) {
					//No es un rebote: es correo normal que llego al buzon.
					continue;
				}
				final java.util.List<Rebote> delMensaje = parsear(bloque);
				boolean alguno = false;
				for (final Rebote r : delMensaje) {
					if (!CorreoRebotadoDAO.anotar(con, r.correo, r.tipo, r.motivo, "BUZON")) {
						//CUENTA LOS QUE NO SE PUDIERON ANOTAR, y los imprime abajo.
						//anotar() reporta su error por log4j, y los jars de
						//Servicios corren sin appenders configurados: ese error no
						//queda en ninguna parte. Sin este contador, una corrida con
						//la base caida se veria igual que una corrida sin rebotes
						//-"rebotes: 0"- y en un proceso desatendido eso se puede
						//quedar asi semanas.
						noAnotados++;
						continue;
					}
					alguno = true;
					if ("DURO".equals(r.tipo)) {
						duros++;
					} else {
						blandos++;
					}
				}
				if (alguno) {
					rebotes++;
				}
			}

			//La marca se mueve solo si la corrida llego hasta aqui: si fallo a
			//mitad, la proxima vuelve a mirar el mismo rango y no se pierde nada.
			guardarUltimaFecha(con, aTexto(new java.util.Date()));

			System.out.println("revisados: " + revisados + ", rebotes: " + rebotes
					+ " (duros: " + duros + ", blandos: " + blandos + ")");
			if (noAnotados > 0) {
				System.out.println("ATENCION: " + noAnotados + " rebotes NO se pudieron anotar."
						+ " Revisar la conexion al central.");
			}
			final CorreoRebotadoDAO.Resumen r = CorreoRebotadoDAO.resumen();
			System.out.println("acumulado: " + r.duros + " duros, " + r.blandos + " blandos");
			System.out.println("duracion: " + ((System.currentTimeMillis() - arranque) / 1000) + " s");

		} catch (final javax.mail.AuthenticationFailedException e) {
			System.out.println("No se pudo entrar al buzon de " + cuenta + ": " + e.getMessage());
			System.out.println("Revisar que IMAP este habilitado y que, si la cuenta tiene"
					+ " verificacion en dos pasos, CLAVECORREOWOMPI sea una clave de aplicacion.");
		} catch (final Exception e) {
			System.out.println("Fallo el proceso de rebotes del buzon: " + e.toString());
		} finally {
			try {
				if (bandeja != null && bandeja.isOpen()) {
					//false: no se expurga nada, no se cambio nada.
					bandeja.close(false);
				}
			} catch (final Exception e) {
			}
			try {
				if (almacen != null) {
					almacen.close();
				}
			} catch (final Exception e) {
			}
			try {
				con.close();
			} catch (final Exception e) {
			}
		}

		//Salida explicita, por lo mismo que ProcesoRebotesBrevo: en un proceso por
		//lote que ya termino, lo correcto es salir y no quedarse esperando a que
		//algun hilo de alguna libreria decida morirse.
		System.exit(0);
	}

	/**
	 * Devuelve el texto de la parte message/delivery-status, o null si el mensaje
	 * no es un rebote.
	 *
	 * Esa parte es la que hace que esto funcione con cualquier proveedor: el
	 * texto para humanos cambia de idioma y de redaccion, pero este bloque tiene
	 * campos fijos definidos en el RFC 3464.
	 *
	 * Busca en profundidad porque algunos proveedores anidan el reporte dentro de
	 * otro multipart en vez de dejarlo en el primer nivel.
	 */
	private static String buscarDeliveryStatus(final Part parte) {
		try {
			if (parte == null) {
				return (null);
			}
			if (parte.isMimeType("message/delivery-status")) {
				return (textoDe(parte));
			}
			if (parte.isMimeType("multipart/*")) {
				final Object contenido = parte.getContent();
				if (contenido instanceof Multipart) {
					final Multipart multi = (Multipart) contenido;
					for (int i = 0; i < multi.getCount(); i++) {
						final String encontrado = buscarDeliveryStatus(multi.getBodyPart(i));
						if (encontrado != null) {
							return (encontrado);
						}
					}
				}
			}
		} catch (final Exception e) {
			//Un mensaje ilegible no puede tumbar la corrida: hay miles mas.
			System.out.println("  no se pudo leer un mensaje: " + e.toString());
		}
		return (null);
	}

	/**
	 * Saca el texto de una parte, venga como cadena o como flujo.
	 *
	 * Para message/delivery-status JavaMail no siempre entrega un String: sin el
	 * paquete de DSN instalado devuelve el flujo crudo. Se contemplan los dos
	 * casos para no depender de que ese paquete este.
	 */
	private static String textoDe(final Part parte) throws Exception {
		final Object contenido = parte.getContent();
		if (contenido instanceof String) {
			return ((String) contenido);
		}
		if (contenido instanceof InputStream) {
			final StringBuilder sb = new StringBuilder();
			final BufferedReader lector = new BufferedReader(
					new InputStreamReader((InputStream) contenido, "ISO-8859-1"));
			String linea = lector.readLine();
			while (linea != null) {
				sb.append(linea).append("\n");
				linea = lector.readLine();
			}
			lector.close();
			return (sb.toString());
		}
		return (contenido == null ? "" : contenido.toString());
	}

	/** Un destinatario rebotado, ya clasificado y listo para anotar. */
	static class Rebote {
		final String correo;
		final String tipo;
		final String motivo;

		Rebote(final String correo, final String tipo, final String motivo) {
			this.correo = correo;
			this.tipo = tipo;
			this.motivo = motivo;
		}
	}

	/**
	 * Lee el bloque de delivery-status y devuelve los rebotes que trae.
	 *
	 * Un mismo rebote puede traer varios destinatarios, cada uno con su propio
	 * Final-Recipient: por eso se va acumulando y se cierra uno cada vez que
	 * empieza el siguiente, y el ultimo al terminar.
	 *
	 * SEPARADO DE LA ESCRITURA A PROPOSITO. El parseo es la parte que se puede
	 * equivocar -formatos de varios proveedores, campos que faltan- y asi se
	 * puede probar con rebotes de verdad sin buzon y sin base de datos. Es
	 * package-private por eso.
	 */
	static java.util.List<Rebote> parsear(final String bloque) {
		final java.util.List<Rebote> rebotes = new java.util.ArrayList<Rebote>();
		if (bloque == null) {
			return (rebotes);
		}
		String correo = "";
		String estado = "";
		String accion = "";
		String diagnostico = "";

		final String[] lineas = bloque.split("\n");
		for (int i = 0; i < lineas.length; i++) {
			final String linea = lineas[i].trim();
			final String minuscula = linea.toLowerCase();

			if (minuscula.startsWith("final-recipient:")
					|| minuscula.startsWith("original-recipient:")) {
				//Empieza otro destinatario: se cierra el anterior.
				agregar(rebotes, correo, estado, accion, diagnostico);
				estado = "";
				accion = "";
				diagnostico = "";
				//Viene como "rfc822; alguien@dominio.com"
				correo = despuesDe(linea, ";");
			} else if (minuscula.startsWith("action:")) {
				accion = despuesDe(linea, ":").toLowerCase();
			} else if (minuscula.startsWith("status:")) {
				estado = despuesDe(linea, ":");
			} else if (minuscula.startsWith("diagnostic-code:")) {
				diagnostico = despuesDe(linea, ":");
			}
		}
		agregar(rebotes, correo, estado, accion, diagnostico);
		return (rebotes);
	}

	/**
	 * Clasifica un destinatario y lo agrega, si es que hay que anotarlo.
	 *
	 * La clasificacion sale del Status del RFC 3463: 5.x es permanente y 4.x es
	 * temporal. Si no vino Status se cae en el Action, y "failed" sin mas es
	 * DURO, que es lo que significa en el estandar.
	 *
	 * Un Action "delayed" NO se anota: el correo todavia puede llegar, y marcar
	 * por una demora sacaria a un cliente bueno por un rato malo del servidor.
	 */
	private static void agregar(final java.util.List<Rebote> rebotes, final String correoCrudo,
			final String estado, final String accion, final String diagnostico) {

		final String correo = limpiarCorreo(correoCrudo);
		if (correo.length() == 0 || correo.indexOf('@') < 0) {
			return;
		}
		if (accion.startsWith("delayed") || accion.startsWith("relayed")
				|| accion.startsWith("delivered") || accion.startsWith("expanded")) {
			return;
		}

		final String tipo;
		if (estado.startsWith("5")) {
			tipo = "DURO";
		} else if (estado.startsWith("4")) {
			tipo = "BLANDO";
		} else if (accion.startsWith("failed")) {
			tipo = "DURO";
		} else {
			//Ni estado ni accion reconocibles: no se inventa nada.
			return;
		}

		//El motivo es lo que despues se lee en pantalla para saber que paso, asi
		//que se arma con lo que haya y nunca queda en blanco.
		final StringBuilder motivo = new StringBuilder();
		if (estado.length() > 0) {
			motivo.append(estado);
		}
		if (diagnostico.length() > 0) {
			motivo.append(motivo.length() > 0 ? " " : "").append(diagnostico);
		}
		if (motivo.length() == 0) {
			motivo.append("action ").append(accion);
		}
		rebotes.add(new Rebote(correo, tipo, motivo.toString()));
	}

	/** Lo que viene despues del primer separador, sin espacios. */
	private static String despuesDe(final String linea, final String separador) {
		final int donde = linea.indexOf(separador);
		if (donde < 0 || donde + 1 >= linea.length()) {
			return ("");
		}
		return (linea.substring(donde + 1).trim());
	}

	/**
	 * Deja la direccion sola.
	 *
	 * Viene de formas distintas segun el proveedor: con picos
	 * -&lt;alguien@dominio.com&gt;-, con el nombre delante, o pelada.
	 */
	private static String limpiarCorreo(final String crudo) {
		if (crudo == null) {
			return ("");
		}
		String correo = crudo.trim();
		final int abre = correo.indexOf('<');
		final int cierra = correo.indexOf('>');
		if (abre >= 0 && cierra > abre) {
			correo = correo.substring(abre + 1, cierra);
		}
		//Algunos ponen comillas o una coma al final.
		correo = correo.replace("\"", "").replace("'", "").trim();
		if (correo.endsWith(",") || correo.endsWith(";") || correo.endsWith(".")) {
			correo = correo.substring(0, correo.length() - 1).trim();
		}
		return (correo.toLowerCase());
	}

	/**
	 * Deja dicho hasta donde se leyo.
	 *
	 * Va por la conexion abierta y no por ParametrosDAO porque su EditarParametro
	 * pide un Parametro completo y pisaria el valor numerico y la descripcion que
	 * ya tiene la fila. Mismo motivo que en ProcesoRebotesBrevo.
	 */
	private static void guardarUltimaFecha(final Connection con, final String hasta) {
		try (java.sql.PreparedStatement ps = con.prepareStatement(
				"UPDATE general.parametros SET valortexto = ? WHERE valorparametro = ?")) {
			ps.setString(1, hasta);
			ps.setString(2, "REBOTESBUZONDESDE");
			ps.executeUpdate();
		} catch (final Exception e) {
			System.out.println("No se pudo guardar REBOTESBUZONDESDE: " + e.toString());
		}
	}

	private static String aTexto(final java.util.Date fecha) {
		return (new java.text.SimpleDateFormat("yyyy-MM-dd").format(fecha));
	}

	private static java.util.Date deTexto(final String texto, final java.util.Date siFalla) {
		try {
			return (new java.text.SimpleDateFormat("yyyy-MM-dd").parse(texto));
		} catch (final Exception e) {
			return (siFalla);
		}
	}

	private static java.util.Date hace(final int dias) {
		final java.util.Calendar c = java.util.Calendar.getInstance();
		c.add(java.util.Calendar.DAY_OF_MONTH, -dias);
		return (c.getTime());
	}
}
