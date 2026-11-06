package CapaDAOSer;

import java.sql.Connection;
import java.sql.PreparedStatement;

import ConexionSer.ConexionBaseDatos;
import capaModeloCC.RazonSocial;

/**
 * Deja guardada la cuenta de la "CONSIGNACION APROXIMADA" del reporte semanal
 * de Rappi.
 *
 * POR QUE EXISTE
 *
 * Esa linea solo vivia dentro del correo. Cuando habia que responder "lo que
 * Rappi consigno, coincidio con lo que el proceso dijo?" tocaba ir a buscar el
 * correo de esa semana, y en gasto_semanal solo queda el concepto de comision,
 * no el total. La conciliacion de los miercoles no tenia contra que comparar.
 *
 * Ahora cada corrida deja aqui su cuenta completa, renglon por renglon, y la
 * comparacion contra el "valor a transferir" del portal de Rappi se hace con un
 * SELECT.
 *
 * NUNCA TUMBA EL REPORTE. Si no se puede escribir la bitacora se dice por el
 * log y se sigue: quedarse sin el registro es malo, no mandar el correo de la
 * consignacion es peor.
 *
 * Si un reproceso vuelve a correr el mismo periodo, REEMPLAZA la fila en vez de
 * duplicarla: la llave unica es (proceso, idrazon, fecha_fin).
 */
public class ConsignacionEstimadaDAO {

	/**
	 * @param proceso SEMANALRAPPI o SEMANALPAGORAPPI, que son dos cortes
	 *                distintos del mismo calculo y no se deben mezclar
	 */
	public static void guardar(final String proceso, final RazonSocial razon,
			final String fechaInicio, final String fechaFin,
			final double recaudoBruto, final double comisionRappi, final double costoPagosLinea,
			final double tarifaServicio, final double descuentosRappi,
			final int cargoPedidos, final double cargoCosto,
			final int cargoCancelados, final double cargoCostoCancel,
			final double cargoEfectivo, final double cargoComisionEfectivo,
			final double consignacionEstimada)
	{
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try
		{
			cn = con.obtenerConexionBDGeneral();
			final PreparedStatement ps = cn.prepareStatement(
					"insert into consignacion_semanal_estimada"
					+ " (proceso, idrazon, nombre_razon, fecha_inicio, fecha_fin,"
					+ "  recaudo_bruto, comision_rappi, costo_pagos_linea, tarifa_servicio, descuentos_rappi,"
					+ "  cargo_pedidos, cargo_costo, cargo_cancelados, cargo_costo_cancel,"
					+ "  cargo_efectivo, cargo_comision_efect, consignacion_estimada)"
					+ " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
					+ " on duplicate key update"
					+ "  nombre_razon = values(nombre_razon),"
					+ "  fecha_inicio = values(fecha_inicio),"
					+ "  recaudo_bruto = values(recaudo_bruto),"
					+ "  comision_rappi = values(comision_rappi),"
					+ "  costo_pagos_linea = values(costo_pagos_linea),"
					+ "  tarifa_servicio = values(tarifa_servicio),"
					+ "  descuentos_rappi = values(descuentos_rappi),"
					+ "  cargo_pedidos = values(cargo_pedidos),"
					+ "  cargo_costo = values(cargo_costo),"
					+ "  cargo_cancelados = values(cargo_cancelados),"
					+ "  cargo_costo_cancel = values(cargo_costo_cancel),"
					+ "  cargo_efectivo = values(cargo_efectivo),"
					+ "  cargo_comision_efect = values(cargo_comision_efect),"
					+ "  consignacion_estimada = values(consignacion_estimada),"
					+ "  fecha_calculo = current_timestamp");
			ps.setString(1, proceso);
			ps.setInt(2, razon == null ? 0 : razon.getIdRazon());
			ps.setString(3, razon == null ? null : razon.getNombreRazon());
			ps.setString(4, fechaInicio);
			ps.setString(5, fechaFin);
			ps.setDouble(6, recaudoBruto);
			ps.setDouble(7, comisionRappi);
			ps.setDouble(8, costoPagosLinea);
			ps.setDouble(9, tarifaServicio);
			ps.setDouble(10, descuentosRappi);
			ps.setInt(11, cargoPedidos);
			ps.setDouble(12, cargoCosto);
			ps.setInt(13, cargoCancelados);
			ps.setDouble(14, cargoCostoCancel);
			ps.setDouble(15, cargoEfectivo);
			ps.setDouble(16, cargoComisionEfectivo);
			ps.setDouble(17, consignacionEstimada);
			ps.executeUpdate();
			ps.close();
			System.out.println("ConsignacionEstimadaDAO: guardada " + proceso + " razon "
					+ (razon == null ? "?" : razon.getNombreRazon()) + " " + fechaInicio + " a " + fechaFin
					+ " = " + Math.round(consignacionEstimada));
		}catch(final Exception e)
		{
			System.out.println("ConsignacionEstimadaDAO.guardar: no se pudo guardar la consignacion de "
					+ proceso + " " + fechaInicio + " a " + fechaFin + ", el reporte sigue: " + e);
		}finally
		{
			try
			{
				if(cn != null)
				{
					cn.close();
				}
			}catch(final Exception e)
			{
				System.out.println("ConsignacionEstimadaDAO: no cerro la conexion, " + e);
			}
		}
	}
}
