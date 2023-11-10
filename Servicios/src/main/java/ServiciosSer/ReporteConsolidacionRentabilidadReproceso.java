package ServiciosSer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import CapaDAOSer.EgresoHistoricoTiendaDAO;
import CapaDAOSer.GastoConfiguracionDAO;
import CapaDAOSer.GastoSemanalDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.IngresoGaseosaHistoricoDAO;
import CapaDAOSer.IngresoHistoricoTiendaDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.GastoConfiguracion;
import ModeloSer.GastoSemanal;
import ModeloSer.IngresoGaseosaHistorico;
import capaControladorPOS.OperacionesTiendaCtrl;
import capaDAOPOS.IngresoDAO;
import capaModeloCC.Tienda;
import capaModeloPOS.Egreso;
import capaModeloPOS.Ingreso;
import utilidadesSer.ControladorEnvioCorreo;

public class ReporteConsolidacionRentabilidadReproceso {
	
	public static void main( String[] args )
	{
		//TRABAJO CON LAS FECHAS///////
		//Recuperamos la fecha actual del sistema con la fecha apertura
		String fechaActual = "";
		//Variables donde manejaremos la fecha anerior con el fin realizar los cálculos de ventas
		Date datFechaAnterior;
		String fechaAnterior = "";
		//Creamos el objeto calendario
		Calendar calendarioActual = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//Formato para mostrar las cantidades
		DecimalFormat formatea = new DecimalFormat("###,###");
		//Obtenemos la fecha Actual
		try
		{
			//OJO
			fechaActual = dateFormat.format(calendarioActual.getTime());
			fechaActual = ParametrosDAO.retornarValorAlfanumerico("FECHAREPROCESO");
		}catch(Exception exc)
		{
			System.out.println(exc.toString());
		}
		try
		{
			//Al objeto calendario le fijamos la fecha actual del sitema
			calendarioActual.setTime(dateFormat.parse(fechaActual));
			
		}catch(Exception e)
		{
			System.out.println(e.toString());
		}
		//Retormanos el día de la semana actual segun la fecha del calendario
		//OJO
		//int diaActual = 1;
		int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
		
		//Domingo
		if(diaActual == 1)
		{
			calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
		}
		else if(diaActual == 2)
		{
			calendarioActual.add(Calendar.DAY_OF_YEAR, -7);
		}
		//Llevamos a un string la fecha anterior para el cálculo de la venta
		datFechaAnterior = calendarioActual.getTime();
		fechaAnterior = dateFormat.format(datFechaAnterior);
		//Asumimos que el proceso se correría el dia domingo o a más tardar el día lunes antes de que inicie las jornadas
		//de trabajo.
		String respuesta = "";
		
		//Obtenemos las tiendas
		ArrayList<Tienda> tiendas = capaDAOCC.TiendaDAO.obtenerTiendas();
		//Debemos tener las consultas que hay que correr por tienda, retornando la información y poblando la tabla
		//Variables para la labor de cada tienda
		ArrayList<GastoConfiguracion> gastosTienda = GastoConfiguracionDAO.obtenerGastorConfiguracionTienda();
		GastoConfiguracion gastoTiendaTemp;
		Tienda tiendaTemp;
		String consultaSQL;
		double porcentajeGasto = 0;
		double valorCalculo = 0;
		double valorGasto = 0;
		GastoSemanal gastoSemanalTemp;
		for(int j = 0; j < tiendas.size(); j++)
		{
			tiendaTemp = tiendas.get(j);
			if(!tiendaTemp.getHosbd().equals(""))
			{
				for(int z = 0; z < gastosTienda.size(); z++)
				{
					gastoTiendaTemp = gastosTienda.get(z);
					consultaSQL = gastoTiendaTemp.getConsultaSQL();
					if(!consultaSQL.equals(new String("NA")))
					{
						porcentajeGasto = gastoTiendaTemp.getPorcentajeGasto();
						consultaSQL = consultaSQL.replace("%fechasuperior%", "'"+fechaActual+"'");
						consultaSQL = consultaSQL.replace("%fechainferior%", "'"+fechaAnterior+"'");
						consultaSQL = consultaSQL.replace("%idtienda%", Integer.toString(tiendaTemp.getIdTienda()));
						//Posteriormente deberemos de ejecutar la consulta para tener el valor del cálculo
						valorCalculo = GastoSemanalDAO.obtenerValorCalculo(tiendaTemp.getHosbd(), consultaSQL, gastoTiendaTemp.getOrigen());
						if(porcentajeGasto != 0)
						{
							valorGasto = valorCalculo*(porcentajeGasto/100);
						}else
						{
							valorGasto = valorCalculo;
						}
						//Realizamos la inserción del Gasto semanal
						gastoSemanalTemp = new GastoSemanal(0,tiendaTemp.getIdTienda(),gastoTiendaTemp.getIdGastoConf(),fechaActual,valorCalculo,valorGasto);
						GastoSemanalDAO.insertarGastoSemanal(gastoSemanalTemp);
					}
				}
			}
		}
		
		//Volvemos a recorrer las tiendas para extraer la información de los ingresos y egresos.
		ArrayList<Egreso> egresosTienda;
		ArrayList<Ingreso> ingresosTienda;
		ArrayList<IngresoGaseosaHistorico> ingresosGaseosa;
		OperacionesTiendaCtrl operTiendaCtrl = new OperacionesTiendaCtrl(false);
		for(int j = 0; j < tiendas.size(); j++)
		{
			tiendaTemp = tiendas.get(j);
			if(!tiendaTemp.getHosbd().equals(""))
			{
				egresosTienda = operTiendaCtrl.obtenerEgresosSemanaRemoto(fechaAnterior, fechaActual,tiendaTemp.getHosbd());
				ingresosTienda = IngresoDAO.obtenerIngresosSemanaRemoto(fechaAnterior, fechaActual, tiendaTemp.getHosbd(), false);
				ingresosGaseosa = IngresoGaseosaHistoricoDAO.obtenerIngresoGaseosaHistoricoTienda(fechaAnterior, fechaActual, tiendaTemp.getHosbd());
				for(Ingreso ingTemp: ingresosTienda)
				{
					IngresoHistoricoTiendaDAO.insertarIngresoHistoricoTienda(tiendaTemp.getIdTienda(), ingTemp);
				}
				for(Egreso egrTemp: egresosTienda)
				{
					EgresoHistoricoTiendaDAO.insertarEgresoHistoricoTienda(tiendaTemp.getIdTienda(), egrTemp);
				}
				for(IngresoGaseosaHistorico ingGasTemp: ingresosGaseosa)
				{
					IngresoGaseosaHistoricoDAO.insertarIngresoGaseosaHistorico(tiendaTemp.getIdTienda(), ingGasTemp);
				}
			}
		}
		
		//Al final el envío del correo
		//Procedemos al envío del correo
		
		
//		Correo correo = new Correo();
//		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
//		correo.setAsunto("CONCILIACIÓN SEMANAL PAGOS VIRTUALES - PAGOS CON TARJETA DESDE " + fechaAnterior + " HASTA "  + fechaActual);
//		correo.setContrasena(infoCorreo.getClaveCorreo());
//		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTECONCILIACIONWOMPI");
//		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
//		correo.setMensaje("A continuación el detalle y resumen de los pedidos con forma de pago virtual entre las fechas " + fechaAnterior + " - " + fechaActual +  ": \n" + respuesta);
//		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
//		contro.enviarCorreoHTML();
	}

}
