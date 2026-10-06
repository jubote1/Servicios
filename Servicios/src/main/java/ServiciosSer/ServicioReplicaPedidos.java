package ServiciosSer;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ReplicaDatamartDAO;
import CapaDAOSer.ReplicaDatamartDAO.Definicion;
import CapaDAOSer.ReplicaDatamartDAO.ReplicaException;
import CapaDAOSer.ReplicaDatamartDAO.Resultado;
import CapaDAOSer.TiendaDAO;
import ConexionSer.ConexionBaseDatos;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.ReplicaCelda;
import ModeloSer.ReplicaTienda;
import ModeloSer.Tienda;
import utilidadesSer.ControladorEnvioCorreo;

/**
 * La replica diaria de las tablas de cada tienda hacia el datamart, y el correo que cuenta que paso.
 *
 * Que replica (ver ReplicaDatamartDAO): pedido, detalle_pedido, despacho_real, despacho_real_det y
 * las tres tablas del enrutamiento. Estas ultimas junto con los despachos son lo que necesita el
 * desempeno de domiciliarios del central para consultar el datamart cuando una tienda esta apagada.
 *
 * Que cambio frente a la version anterior:
 *
 * 1. El correo ya no dice "EXITOSO" porque se LEYERON filas de la tienda. Dice lo que se ESCRIBIO en el
 *    datamart, tabla por tabla y tienda por tienda, en una matriz con colores. Antes una escritura
 *    fallida (llave duplicada, columna que falta) se tragaba el error y el correo igual decia exitoso.
 * 2. Se conecta UNA vez a cada tienda. Si no contesta, se dice una sola vez y se pasa a la siguiente;
 *    antes se esperaban los 10 segundos de espera cuatro veces por tienda apagada.
 * 3. Es idempotente: si el datamart ya tiene ese dia de esa tienda no lo toca. Por eso se puede correr
 *    de nuevo sin duplicar, y por eso recupera solo los dias atrasados: una tienda que estuvo apagada
 *    el martes se pone al dia el miercoles, sin que nadie haga nada.
 *
 * Uso:   ServicioReplicaPedidos [diasAtras]
 *        diasAtras = cuantos dias hacia atras revisa (por defecto 3: ayer y los dos anteriores).
 *        Para cargar historia nueva (por ejemplo las tablas del enrutamiento) se corre una vez con 45.
 */
public class ServicioReplicaPedidos {

	private static final int DIAS_POR_DEFECTO = 3;
	private static final int DIAS_MAXIMO = 120;

	public static void main(String[] args) {
		int dias = DIAS_POR_DEFECTO;
		if (args != null && args.length > 0) {
			try {
				dias = Integer.parseInt(args[0].trim());
			} catch (NumberFormatException e) {
				System.out.println("Argumento de dias no valido (" + args[0] + "), se usa " + DIAS_POR_DEFECTO);
			}
		}
		if (dias < 1) {
			dias = 1;
		}
		if (dias > DIAS_MAXIMO) {
			dias = DIAS_MAXIMO;
		}
		new ServicioReplicaPedidos().generarReplicaPedidos(dias);
	}

	/** Se conserva con este nombre y sin argumentos: asi la llamaba la tarea programada. */
	public void generarReplicaPedidos() {
		generarReplicaPedidos(DIAS_POR_DEFECTO);
	}

	public void generarReplicaPedidos(int diasAtras) {
		System.out.println("EMPEZAMOS LA EJECUCION REPLICA PEDIDOS (dias atras: " + diasAtras + ")");
		long inicio = System.currentTimeMillis();
		SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

		//La fecha de los datos principales es la de AYER: la replica corre de madrugada.
		Calendar ayer = Calendar.getInstance();
		ayer.add(Calendar.DAY_OF_YEAR, -1);
		String fechaAyer = formato.format(ayer.getTime());

		List<Definicion> definiciones = ReplicaDatamartDAO.definiciones();
		List<ReplicaTienda> resultados = new ArrayList<ReplicaTienda>();
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();

		for (Tienda tien : tiendas) {
			ReplicaTienda rt = new ReplicaTienda(tien.getIdTienda(), tien.getNombreTienda(), tien.getHostBD());
			resultados.add(rt);
			for (Definicion d : definiciones) {
				rt.getCeldas().put(d.tabla, new ReplicaCelda(d.tabla));
			}
			replicarTienda(tien, rt, definiciones, diasAtras, formato);
		}

		enviarCorreo(resultados, definiciones, fechaAyer, diasAtras, System.currentTimeMillis() - inicio);
		System.out.println("TERMINAMOS LA EJECUCION REPLICA PEDIDOS");
	}

	// ------------------------------------------------------------------
	// Una tienda
	// ------------------------------------------------------------------

	private void replicarTienda(Tienda tien, ReplicaTienda rt, List<Definicion> definiciones, int diasAtras,
			SimpleDateFormat formato) {
		if (tien.getHostBD() == null || tien.getHostBD().trim().length() == 0) {
			rt.setConecto(false);
			rt.setErrorConexion("La tienda no tiene host configurado (tienda.hosbd).");
			marcarTodo(rt, ReplicaCelda.NO_APLICA, "Sin host configurado");
			return;
		}

		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection datamart = null;
		Connection tienda = null;
		try {
			tienda = con.obtenerConexionBDTiendaRemota(tien.getHostBD());
			if (tienda == null) {
				rt.setConecto(false);
				rt.setErrorConexion("No contesto el computador de la tienda (" + tien.getHostBD() + ").");
				marcarTodo(rt, ReplicaCelda.ERROR, "Sin conexion con la tienda");
				pegarUltimoOk(rt);
				registrar(rt, formato.format(ayerFecha()));
				return;
			}
			rt.setConecto(true);
			datamart = con.obtenerConexionBDDatamartLocal();
			if (datamart == null) {
				marcarTodo(rt, ReplicaCelda.ERROR, "Sin conexion con el datamart");
				return;
			}

			//Del dia mas viejo al mas reciente: asi un dia atrasado se recupera antes que el de ayer.
			for (int atras = diasAtras; atras >= 1; atras--) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_YEAR, -atras);
				String fecha = formato.format(cal.getTime());
				boolean esAyer = (atras == 1);
				for (Definicion d : definiciones) {
					replicarTabla(tien, rt, tienda, datamart, d, fecha, esAyer);
				}
			}
		} finally {
			cerrar(tienda);
			cerrar(datamart);
		}
		for (ReplicaCelda c : rt.getCeldas().values()) {
			if (c.esError()) {
				c.setUltimoOk(ReplicaDatamartDAO.ultimaFechaBuena(rt.getIdTienda(), c.getTabla()));
			}
		}
		registrar(rt, formato.format(ayerFecha()));
	}

	private void replicarTabla(Tienda tien, ReplicaTienda rt, Connection tienda, Connection datamart, Definicion d,
			String fecha, boolean esAyer) {
		ReplicaCelda celda = rt.getCeldas().get(d.tabla);
		try {
			if (ReplicaDatamartDAO.yaEstaEnDatamart(datamart, d, tien.getIdTienda(), fecha)) {
				if (esAyer && !celda.esError() && celda.getDiasRecuperados() == 0) {
					//Si en esta corrida se recupero un dia atrasado, eso es lo que se cuenta: el verde se queda.
					celda.setEstado(ReplicaCelda.YA_ESTABA);
					celda.setDetalle("El datamart ya tenia este dia.");
				}
				return;
			}
			Resultado r = ReplicaDatamartDAO.replicar(tienda, d, tien.getIdTienda(), fecha);
			if (r.filasEscritas > 0) {
				if (esAyer) {
					//Si un dia ATRASADO dejo un hueco (rojo), no se tapa con el verde de ayer.
					if (!celda.esError()) {
						celda.setEstado(ReplicaCelda.OK);
					}
					celda.setFilas(celda.getFilas() + r.filasEscritas);
					celda.setDetalle(r.columnasSinDestino.isEmpty() ? ""
							: "El datamart no tiene las columnas: " + r.columnasSinDestino);
				} else {
					//Un dia ATRASADO que se recupero.
					celda.setDiasRecuperados(celda.getDiasRecuperados() + 1);
					if (celda.getEstado().equals(ReplicaCelda.NO_APLICA) || celda.getEstado().equals(ReplicaCelda.CERO)) {
						celda.setEstado(ReplicaCelda.OK);
					}
					celda.setFilas(celda.getFilas() + r.filasEscritas);
				}
			} else if (esAyer && !celda.esError()) {
				//Cero filas: normal para unas tablas, para revisar en otras.
				boolean normal = d.ceroEsNormal || esBodega(tien);
				celda.setEstado(normal ? ReplicaCelda.NO_APLICA : ReplicaCelda.CERO);
				celda.setDetalle(normal ? "Sin registros ese dia." : "La tienda no tenia filas ese dia.");
			}
		} catch (ReplicaException e) {
			if (e.esNoAplica()) {
				if (esAyer && !celda.esError()) {
					celda.setEstado(ReplicaCelda.NO_APLICA);
					celda.setDetalle(e.getMessage());
				}
			} else {
				//Un error en CUALQUIER dia es un hueco en los datos: se pinta en rojo.
				celda.setEstado(ReplicaCelda.ERROR);
				celda.setDetalle((esAyer ? "" : "Dia " + fecha + ": ") + e.getMessage());
				System.out.println("ERROR replica " + tien.getNombreTienda() + " " + d.tabla + " " + fecha + ": "
						+ e.getMessage());
			}
		}
	}

	private void marcarTodo(ReplicaTienda rt, String estado, String detalle) {
		for (ReplicaCelda c : rt.getCeldas().values()) {
			c.setEstado(estado);
			c.setDetalle(detalle);
		}
	}

	private void pegarUltimoOk(ReplicaTienda rt) {
		for (ReplicaCelda c : rt.getCeldas().values()) {
			c.setUltimoOk(ReplicaDatamartDAO.ultimaFechaBuena(rt.getIdTienda(), c.getTabla()));
		}
	}

	private void registrar(ReplicaTienda rt, String fechaAyer) {
		for (ReplicaCelda c : rt.getCeldas().values()) {
			ReplicaDatamartDAO.registrar(rt.getIdTienda(), c.getTabla(), fechaAyer, c.getEstado(), c.getFilas(),
					c.getDetalle());
		}
	}

	private Date ayerFecha() {
		Calendar ayer = Calendar.getInstance();
		ayer.add(Calendar.DAY_OF_YEAR, -1);
		return ayer.getTime();
	}

	private boolean esBodega(Tienda tien) {
		return tien.getNombreTienda() != null && tien.getNombreTienda().trim().equalsIgnoreCase("BODEGA");
	}

	private void cerrar(Connection c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			//Nada que hacer.
		}
	}

	// ------------------------------------------------------------------
	// El correo
	// ------------------------------------------------------------------

	private void enviarCorreo(List<ReplicaTienda> resultados, List<Definicion> definiciones, String fechaAyer,
			int diasAtras, long milisegundos) {
		int conProblemas = 0;
		for (ReplicaTienda rt : resultados) {
			if (rt.tieneProblemas()) {
				conProblemas++;
			}
		}
		String estado = conProblemas == 0 ? "OK" : "REVISAR " + conProblemas + " TIENDA(S)";

		Correo correo = new Correo();
		//El asunto va solo en ASCII: los acentos del asunto se danan en algunos clientes de correo.
		correo.setAsunto("REPLICA DATAMART " + estado + " - datos del " + fechaAyer);
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES",
				"CLAVECORREOREPORTE");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPLICAUSUARIOS");
		correo.setMensaje(ReplicaCorreoHtml.construir(resultados, definiciones, fechaAyer, diasAtras, milisegundos));
		new ControladorEnvioCorreo(correo, correos).enviarCorreoHTML();
	}
}
