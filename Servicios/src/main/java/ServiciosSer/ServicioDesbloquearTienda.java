package ServiciosSer;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import capaControladorCC.TiendaBloqueadaCtrl;
import capaDAOCC.LogBloqueoTiendaDAO;
import capaDAOCC.TiendaBloqueadaDAO;
import capaModeloCC.LogBloqueoTienda;
import capaModeloCC.TiendaBloqueada;
import capaControladorPOS.PedidoCtrl;
import capaDAOFirebase.CrudFirebase;

public class ServicioDesbloquearTienda {
	
	public static void main(String[] args)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormatHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		PedidoCtrl pedCtrl = new PedidoCtrl(false);
		//Obtenemos las tiendas que están bloqueadas
		TiendaBloqueadaCtrl tienBloCtrl = new TiendaBloqueadaCtrl();
		ArrayList<TiendaBloqueada> tiendasBlo = TiendaBloqueadaDAO.retornarTiendasBloqueadas();
		//Recorremos el ArrayList con las tiendas bloqueadas
		for(TiendaBloqueada tiendaBloTemp: tiendasBlo)
		{
			//Debemos de recorrer el último registro de bloqueo de la tienda en cuestión
			LogBloqueoTienda logBloqueo = LogBloqueoTiendaDAO.obtenerUltimoBloqueoTienda(tiendaBloTemp.getIdtienda());
			//Realizamos la conversión del campo tiempo para saber si debemos o no realizar el desbloqueo
			int tiempoDesbloqueo = 0;
			try {
				tiempoDesbloqueo = Integer.parseInt(logBloqueo.getDebloqueoEn());
			}catch(Exception e)
			{
				tiempoDesbloqueo = 0;
			}
			//Si tiempoDesbloqueo es mayor a cero deberemos de mirar los tiempos para realizar desbloqueo
			if(tiempoDesbloqueo > 0)
			{
				Date fechaActual = new Date();
				Date fechaHoraBloqueo;
				try
				{
					fechaHoraBloqueo = dateFormatHora.parse(logBloqueo.getFechaAccion());
				}catch(Exception e)
				{
					fechaHoraBloqueo = new Date();
				}
				//Necesitamos calcular la diferencia en minutos de 2 fechas para tomar la decisión del desbloqueo
				long dif = fechaActual.getTime() - fechaHoraBloqueo.getTime();
				long difMinutos = TimeUnit.MILLISECONDS.toMinutes(dif);
				if(difMinutos > tiempoDesbloqueo)
				{
					System.out.println("debo desbloquear");
					tienBloCtrl.eliminarTiendaBloqueada(tiendaBloTemp.getIdtienda());
					//Posteriormenente deberemos de realizar la ejecución del servicio en la tienda para el desbloqueo
					//
					
					String rutaURL = logBloqueo.getUrlTienda() + "DesbloquearTienda";
					URL url=null;
					//Realizamos la invocación mediante el uso de HTTPCLIENT
					HttpClient client = HttpClientBuilder.create().build();
					HttpGet request = new HttpGet(rutaURL);
					try
					{
						StringBuffer retorno = new StringBuffer();
						HttpResponse response = client.execute(request);
						BufferedReader rd = new BufferedReader
							    (new InputStreamReader(
							    response.getEntity().getContent()));
						String line = "";
						while ((line = rd.readLine()) != null) {
							    retorno.append(line);
							}
						System.out.println(retorno);
					
						
					}catch(Exception e)
					{
						e.printStackTrace();
					}
				
				}
		
			}
		}
	}
}
