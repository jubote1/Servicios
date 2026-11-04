package ServiciosSer;

/**
 * Vuelve a sacar el reporte semanal de varianza de una semana determinada.
 *
 * Es una clase de tres lineas a proposito. La costumbre vieja en este proyecto
 * era copiar el reporte entero y cambiarle cuatro lineas -el nombre de la
 * clase, de donde sale la fecha y el main-, y eso obliga a acordarse de repetir
 * cada arreglo en las dos copias. No pasa. Y duele mas justo aqui, porque un
 * reproceso solo se corre cuando algo ya salio mal, y en ese momento nadie esta
 * comparando dos archivos.
 *
 * Toda la logica vive en ReporteVarianzaSemanal.
 *
 * QUE SEMANA SACA
 *
 * La de FECHAREPROCESO, en general.parametros. Esa fecha es el dia en que el
 * proceso DEBIO correr, no el ultimo dia de la semana que se quiere: el rango
 * se arma igual que en la corrida normal, la semana lunes a domingo que termina
 * en el domingo mas reciente que no sea posterior a esa fecha.
 * Si se pone el domingo 2026-09-13 (o el lunes 2026-09-14), saca del 2026-09-07
 * al 2026-09-13.
 *
 * Como la corrida normal es el domingo 23:50 y consulta directo a las tiendas
 * (no al datamart), este reproceso tambien va directo a las tiendas. Sirve sobre
 * todo cuando el correo salio marcado como INCOMPLETO (una tienda sin responder o
 * sin la varianza del domingo cargada): se corre mas tarde y sale completo.
 *
 * OJO: FECHAREPROCESO es uno solo y lo comparten todos los reprocesos del
 * servidor. Antes de correr esto hay que mirar que no quedo puesto para otra
 * cosa. El rango que se resolvio se imprime antes de mandar nada.
 *
 * Si se quiere una semana puntual sin tocar ese parametro, el proceso normal
 * recibe las dos fechas por argumento:
 *
 *   java -jar ReporteVarianzaSemanal.jar 2026-09-07 2026-09-13
 */
public class ReporteVarianzaSemanalReproceso {

	public static void main(final String[] args) {
		ReporteVarianzaSemanal.reprocesar();
	}
}
