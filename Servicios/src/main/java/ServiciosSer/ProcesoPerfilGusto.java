package ServiciosSer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import ConexionSer.ConexionBaseDatos;

/**
 * Recalcula el perfil de gusto de cada persona: que especialidad prefiere.
 *
 * Es la base de las estrategias de recompra. Para poder decirle a alguien
 * "sabemos que te encanta la Hawaiana, prueba la Pinaronni" hay que saber
 * primero que le encanta la Hawaiana.
 *
 * TIENE QUE CORRER DESPUES DE ProcesoMantenerMaestroCRM
 *
 * Agrupa por cliente.idpersona, que es lo que ese proceso mantiene. Si corre
 * antes, los clientes nuevos de la noche todavia no tienen idpersona y quedan
 * sin perfil hasta la noche siguiente.
 *
 * SOLO VE LO QUE LLEGA AL CENTRAL
 *
 * El mostrador no tiene detalle de pedido en esta base, asi que el gusto se
 * calcula sobre domicilio y virtual. Quien solo compra en mostrador queda SIN
 * perfil, que es distinto de quedar con uno equivocado: las estrategias lo
 * dejan por fuera en vez de sugerirle algo con base en nada.
 *
 * UNA PIZZA MITAD Y MITAD CUENTA PARA LAS DOS
 *
 * Se cuenta por aparicion de la especialidad, no por pizza. Quien siempre pide
 * mitad Hawaiana y mitad Carniz queda en 50% de cada una, no en 100% de
 * Hawaiana. Es mas estricto y dice mejor que tan marcada es la preferencia.
 *
 * SE BORRA TODO ANTES DE RECALCULAR
 *
 * A proposito: quien dejo de comprar hace mas de un ano tiene que quedar sin
 * perfil, no con el de hace dos anos. Un gusto viejo es peor que ninguno,
 * porque la estrategia se lo cree.
 */
public class ProcesoPerfilGusto {

	/** Ventana sobre la que se mira el gusto. */
	private static final int MESES = 12;

	public static void main(final String[] args) {
		new ProcesoPerfilGusto().recalcular();
	}

	public void recalcular() {
		final long arranque = System.currentTimeMillis();
		System.out.println("ProcesoPerfilGusto: recalculando el gusto de los ultimos " + MESES + " meses");

		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try {
			cn = con.obtenerConexionBDContactLocal();
			if (cn == null) {
				System.out.println("ProcesoPerfilGusto: sin conexion al central, no se hace nada");
				return;
			}
			final Statement stm = cn.createStatement();

			//1. Las familias nuevas. Una especialidad que se creo esta semana
			//   todavia no tiene familia, y sin fila en especialidad_familia sus
			//   pizzas se caerian del JOIN y no contarian para nadie.
			final int familiasNuevas = stm.executeUpdate(
					"INSERT INTO crm.especialidad_familia (idespecialidad, familia)"
					+ " SELECT e.idespecialidad, UPPER(e.nombre) FROM pizzaamericana.especialidad e"
					+ " WHERE NOT EXISTS (SELECT 1 FROM crm.especialidad_familia ya"
					+ "                    WHERE ya.idespecialidad = e.idespecialidad)");
			if (familiasNuevas > 0) {
				System.out.println("ProcesoPerfilGusto: " + familiasNuevas
						+ " especialidad(es) nueva(s) entraron al catalogo de familias");
			}

			//2. El conteo por persona y familia, en una temporal.
			stm.executeUpdate("DROP TEMPORARY TABLE IF EXISTS crm.gusto_tmp");
			stm.executeUpdate(
					"CREATE TEMPORARY TABLE crm.gusto_tmp ("
					+ " idpersona BIGINT NOT NULL, familia VARCHAR(40) NOT NULL, veces INT NOT NULL,"
					+ " PRIMARY KEY (idpersona, familia)) ENGINE=InnoDB");

			final String conteo =
					"INSERT INTO crm.gusto_tmp (idpersona, familia, veces)"
					+ " SELECT t.idpersona, t.familia, SUM(t.veces) FROM ("
					+ sumaDe("d.idespecialidad1")
					+ " UNION ALL "
					+ sumaDe("d.idespecialidad2")
					+ " ) t GROUP BY t.idpersona, t.familia";
			final int filas = stm.executeUpdate(conteo);
			System.out.println("ProcesoPerfilGusto: " + filas + " combinaciones persona/familia");

			//3. Se limpia y se vuelve a escribir.
			stm.executeUpdate(
					"UPDATE crm.persona_resumen SET familia_favorita = NULL,"
					+ " fidelidad_favorita = NULL, pizzas_12m = 0, gusto_actualizado_en = NOW()");

			//El desempate va por nombre para que dos corridas seguidas den lo
			//mismo: sin eso, dos familias empatadas se alternarian cada noche y
			//una persona cambiaria de favorita sin haber cambiado de gustos.
			final int conPerfil = stm.executeUpdate(
					"UPDATE crm.persona_resumen r JOIN ("
					+ "   SELECT g.idpersona,"
					+ "          SUBSTRING_INDEX(GROUP_CONCAT(g.familia ORDER BY g.veces DESC, g.familia"
					+ "                          SEPARATOR '||'), '||', 1) AS familia,"
					+ "          MAX(g.veces) AS veces_favorita, SUM(g.veces) AS total"
					+ "     FROM crm.gusto_tmp g GROUP BY g.idpersona"
					+ " ) x ON x.idpersona = r.idpersona"
					+ " SET r.familia_favorita = x.familia,"
					+ "     r.fidelidad_favorita = ROUND(x.veces_favorita * 100 / x.total),"
					+ "     r.pizzas_12m = x.total, r.gusto_actualizado_en = NOW()");

			stm.executeUpdate("DROP TEMPORARY TABLE IF EXISTS crm.gusto_tmp");

			//4. El resumen, para que el log diga algo util y no solo "listo".
			final ResultSet rs = stm.executeQuery(
					"SELECT COUNT(*) AS con_perfil,"
					+ " SUM(fidelidad_favorita >= 60 AND pizzas_12m >= 3) AS con_favorita_clara"
					+ " FROM crm.persona_resumen WHERE familia_favorita IS NOT NULL");
			if (rs.next()) {
				System.out.println("ProcesoPerfilGusto: " + rs.getInt("con_perfil")
						+ " personas con perfil, " + rs.getInt("con_favorita_clara")
						+ " con una favorita clara");
			}
			rs.close();
			stm.close();

			System.out.println("ProcesoPerfilGusto: listo, " + conPerfil + " personas actualizadas en "
					+ ((System.currentTimeMillis() - arranque) / 1000) + " segundos");
		} catch (final Exception e) {
			//No se deja a medias en silencio: si fallo, el perfil quedo borrado
			//o viejo, y quien arme un envio manana tiene que saberlo.
			System.out.println("ProcesoPerfilGusto FALLO: " + e.toString());
		} finally {
			try {
				if (cn != null) {
					cn.close();
				}
			} catch (final Exception e1) {
				System.out.println("ProcesoPerfilGusto: cerrando conexion " + e1.toString());
			}
		}
	}

	/** El conteo de una de las dos columnas de especialidad. */
	private String sumaDe(final String columna) {
		return ("SELECT c.idpersona, f.familia, COUNT(*) AS veces"
				+ " FROM pizzaamericana.pedido p"
				+ " JOIN pizzaamericana.detalle_pedido d ON d.idpedido = p.idpedido"
				+ " JOIN pizzaamericana.cliente c ON c.idcliente = p.idcliente"
				+ " JOIN crm.especialidad_familia f ON f.idespecialidad = " + columna
				+ " WHERE p.fechapedido >= DATE_SUB(CURDATE(), INTERVAL " + MESES + " MONTH)"
				//numposheader > 0 deja por fuera los pedidos que nunca llegaron a
				//la tienda: un pedido que no se despacho no dice nada del gusto.
				+ "   AND p.numposheader > 0 AND c.idpersona > 0 AND " + columna + " > 0"
				+ " GROUP BY c.idpersona, f.familia");
	}
}
