package ServiciosSer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFRegionUtil;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import CapaDAOSer.ConsumoInventarioDAO;
import CapaDAOSer.ConsumoPorcionesDAO;
import CapaDAOSer.EmpleadoEventoDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ItemInventarioDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.ReporteHorariosDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.ConsumoInventario;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.EmpleadoEvento;
import ModeloSer.Insumo;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import utilidadesSer.ControladorEnvioCorreo;
import utilidadesSer.EmparejadorBiometria;
import utilidadesSer.CorreosBiometria;

public class ServicioMonitoreoUsuarios {

	/** Hora a partir de la cual un ingreso se considera sospechosamente tardio. */
	private static final int HORA_TARDIA_POR_DEFECTO = 20;


	public static void main(final String[] args) {
		final ServicioMonitoreoUsuarios servicioMonitoreoUsuarios = new ServicioMonitoreoUsuarios();
		servicioMonitoreoUsuarios.monitoreoUsuarios();
	}

	/**
	 * Revisa la biometria del dia anterior, avisa a cada empleado con novedad y manda
	 * el resumen general a la lista ERRORBIOMETRIA.
	 */
	public void monitoreoUsuarios() {
		final SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		final Calendar calendario = Calendar.getInstance();
		calendario.add(Calendar.DAY_OF_YEAR, -1);
		final String fechaRevisada = formatoFecha.format(calendario.getTime());
		System.out.println("Revisando la biometria del dia " + fechaRevisada);

		int horaTardia = ParametrosDAO.retornarValorNumericoLocal("MONITOREOBIOHORATARDIA");
		if (horaTardia <= 0) {
			horaTardia = ServicioMonitoreoUsuarios.HORA_TARDIA_POR_DEFECTO;
			System.out.println("El parametro MONITOREOBIOHORATARDIA no esta definido, se usa " + horaTardia);
		}

		final ArrayList<EmpleadoEvento> eventos = ReporteHorariosDAO
				.obtenerEntradasSalidasEmpleadosEventos(fechaRevisada, fechaRevisada);
		final ArrayList<NovedadBiometria> novedades = EmparejadorBiometria.detectarNovedades(eventos, horaTardia);
		System.out.println("Eventos revisados: " + eventos.size() + ". Novedades: " + novedades.size());
		if (novedades.size() == 0) {
			System.out.println("Sin novedades. No se envia ningun correo.");
			return;
		}

		//Nombre de la tienda, para que el correo general no muestre numeros.
		final ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
		for (int i = 0; i < novedades.size(); i++) {
			final NovedadBiometria novedad = novedades.get(i);
			novedad.setNombreTienda("No identificada");
			for (int j = 0; j < tiendas.size(); j++) {
				if (tiendas.get(j).getIdTienda() == novedad.getIdTienda()) {
					novedad.setNombreTienda(tiendas.get(j).getNombreTienda());
					break;
				}
			}
		}

		//Un solo correo por empleado con TODAS sus novedades del dia. Antes salia uno
		//por novedad y podian llegarle varios identicos. Las novedades vienen ordenadas
		//por empleado, asi que basta recorrerlas por bloques.
		final CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES",
				"CLAVECORREOREPORTE");
		int avisados = 0;
		final ArrayList<NovedadBiometria> sinCorreo = new ArrayList<NovedadBiometria>();
		int inicio = 0;
		while (inicio < novedades.size()) {
			int fin = inicio;
			while (fin + 1 < novedades.size()
					&& novedades.get(fin + 1).getIdEmpleado() == novedades.get(inicio).getIdEmpleado()) {
				fin++;
			}
			final ArrayList<NovedadBiometria> delEmpleado = new ArrayList<NovedadBiometria>();
			for (int k = inicio; k <= fin; k++) {
				delEmpleado.add(novedades.get(k));
			}
			final NovedadBiometria primera = delEmpleado.get(0);
			String correoEmpleado = "";
			try {
				correoEmpleado = EmpleadoEventoDAO.obtenerCorreoElectronico(primera.getIdEmpleado());
			} catch (final Exception e) {
				System.out.println("No se pudo leer el correo del empleado " + primera.getIdEmpleado() + ": " + e);
			}
			for (int k = 0; k < delEmpleado.size(); k++) {
				delEmpleado.get(k).setCorreo(correoEmpleado);
			}
			//OJO: si el empleado no tiene correo NO se intenta enviar. Antes se le metia la
			//direccion nula a la lista de destinatarios. Queda reportado en el general.
			if (primera.tieneCorreo()) {
				try {
					final Correo correo = new Correo();
					correo.setAsunto("Novedad en su registro de biometria del " + fechaRevisada);
					correo.setContrasena(infoCorreo.getClaveCorreo());
					correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
					correo.setMensaje(CorreosBiometria.armarCorreoEmpleado(delEmpleado, fechaRevisada));
					final ArrayList destinatarios = new ArrayList();
					destinatarios.add(primera.getCorreo().trim());
					final ControladorEnvioCorreo envio = new ControladorEnvioCorreo(correo, destinatarios);
					envio.enviarCorreoHTML();
					avisados++;
				} catch (final Exception e) {
					//Que falle un correo no puede tumbar el resto ni el resumen general.
					System.out.println("Fallo el correo al empleado " + primera.getIdEmpleado() + ": " + e);
				}
			} else {
				sinCorreo.add(primera);
			}
			inicio = fin + 1;
		}
		System.out.println("Empleados avisados: " + avisados + ". Sin correo: " + sinCorreo.size());

		try {
			final Correo general = new Correo();
			general.setAsunto("Novedades de biometria del " + fechaRevisada + " - " + novedades.size() + " novedades");
			general.setContrasena(infoCorreo.getClaveCorreo());
			general.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			general.setMensaje(CorreosBiometria.armarCorreoGeneral(novedades, sinCorreo, fechaRevisada, avisados, horaTardia));
			final ArrayList correos = GeneralDAO.obtenerCorreosParametro("ERRORBIOMETRIA");
			if (correos.size() == 0) {
				System.out.println("La lista ERRORBIOMETRIA esta vacia. No se envia el resumen.");
				return;
			}
			final ControladorEnvioCorreo envio = new ControladorEnvioCorreo(general, correos);
			envio.enviarCorreoHTML();
		} catch (final Exception e) {
			System.out.println("Fallo el envio del resumen general: " + e);
		}
	}

}
