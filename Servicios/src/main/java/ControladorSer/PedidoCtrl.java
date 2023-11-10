package ControladorSer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import CapaDAOSer.TiendaDAO;
import ModeloSer.Pedido;
import ModeloSer.Tienda;

public class PedidoCtrl {
	
	public PedidoCtrl()
	{

	}
	
	public boolean notificarPedidoTienda(String idLink, String tipoPago, int idTienda)
	{
		HttpClient client = HttpClientBuilder.create().build();
		boolean respuestaProceso = false;
		Tienda tienda = TiendaDAO.obtenerTienda(idTienda);
		String rutaURLTienda = tienda.getUrl() + "NotificarPagoVirtual?idlink=" + idLink + "&tipopago=" + tipoPago;
		HttpGet servicioGet = new HttpGet(rutaURLTienda);
        try {
        	StringBuffer retornoTienda = new StringBuffer();
            HttpResponse responseFinPedTienda = client.execute(servicioGet);
            BufferedReader rdTienda = new BufferedReader(new InputStreamReader(
            		responseFinPedTienda.getEntity().getContent()));
            String lineTienda = "";
            while ((lineTienda = rdTienda.readLine()) != null) {
            	retornoTienda.append(lineTienda);
            }
            //En este punto ya tendríamos la respuesta de la inserción de la tienda y tendríamos que finalizar
            if(retornoTienda.toString().trim().equals(new String("OK")))
            {
            	respuestaProceso = true;
            }
        }catch(Exception e)
        {
        	
        }
		return(respuestaProceso);
	}
	
	public boolean reenviarPedidoJava(Pedido pedidoReenviar, String urlContactCenter, String tiendaKuno)
	{
		//Realizamos la invocación mediante el uso de HTTPCLIENT
		HttpClient client = HttpClientBuilder.create().build();
		//Comenzamos con la finalización del pedido en el contact center
		//Este valor es cero si el cliente ya existia y 1 si es creado
		//Traemos este valor con base en el memcode
		boolean respuestaProceso = true;
		int insertado = 0;
		if(pedidoReenviar.getMemcode() > 0)
		{
			insertado = 0;
		}else
		{
			insertado = 1;
		}
		String rutaURL = urlContactCenter + "FinalizarPedido?idpedido=" + pedidoReenviar.getIdpedido() + "&idformapago=" + pedidoReenviar.getIdformapago() + "&valortotal=" + pedidoReenviar.getTotal_neto() + "&valorformapago=" + pedidoReenviar.getValorFormaPago() + "&idcliente=" + pedidoReenviar.getIdcliente() + "&insertado=" + insertado + "&tiempopedido=" + pedidoReenviar.getTiempopedido() +"&validadir=" + "S" + "&descuento=" + pedidoReenviar.getDescuento() + "&motivodescuento=" + pedidoReenviar.getMotivoDescuento()+"&programado=" + pedidoReenviar.getHoraProgramado() + "&tiendakuno=" + tiendaKuno;
		HttpGet request = new HttpGet(rutaURL);
		try
		{
			StringBuffer retorno = new StringBuffer();
			StringBuffer retornoTienda = new StringBuffer();
			//Se realiza la ejecución del servicio de finalizar pedido
			HttpResponse responseFinPed = client.execute(request);
			BufferedReader rd = new BufferedReader
				    (new InputStreamReader(
				    		responseFinPed.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				    retorno.append(line);
				}
			System.out.println(retorno);
			//Traemos el valor del JSON con toda la info del pedido
			String datosJSONArray = retorno.toString();
			//Los datos vienen en un arreglo, debemos de tomar el primer valor como lo hacemos en la parte gráfica
			JSONParser parser = new JSONParser();
			Object objParser = parser.parse(datosJSONArray);
			JSONObject jsonObject=(JSONObject) ((JSONArray)objParser).get(0);
			String datosJSON = jsonObject.toJSONString();
			//En el anterior punto sacamos el primer objeto del arreglo y lo llevamos a un string para procesarlo en la inserción de la tienda
						
			//En retorno tendremos el resultado de la finalización del pedido y continuaremos con el envío del pedido a la tienda
			Tienda tienda = TiendaDAO.obtenerTienda(pedidoReenviar.getIdtienda());
			//Recordar que este es un llamado POS, del JSON recibido en el anterior
			String rutaURLTienda = tienda.getUrl() + "FinalizarPedidoPixel";
			HttpPost post = new HttpPost(rutaURLTienda);
	        try {
	            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	            nameValuePairs.add(new BasicNameValuePair("datos",
	            		datosJSON));
	            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	            HttpResponse responseFinPedTienda = client.execute(post);
	            BufferedReader rdTienda = new BufferedReader(new InputStreamReader(
	            		responseFinPedTienda.getEntity().getContent()));
	            String lineTienda = "";
	            while ((lineTienda = rdTienda.readLine()) != null) {
	            	retornoTienda.append(lineTienda);
	            }
	            //En este punto ya tendríamos la respuesta de la inserción de la tienda y tendríamos que finalizar
	            System.out.println(retornoTienda.toString());
	            //Realizamos el tratamiento de la respuesta final
	          	JSONParser parserFinal = new JSONParser();
				Object objParserFinal = parser.parse(retornoTienda.toString());
				JSONObject jsonObjectFinal=(JSONObject) ((JSONArray)objParserFinal).get(0);
				int memberCode = ((Long)jsonObjectFinal.get("membercode")).intValue();
				int numeroFactura = ((Long)jsonObjectFinal.get("numerofactura")).intValue();
				int idPedido = ((Long)jsonObjectFinal.get("idpedido")).intValue();
				boolean creaCliente = ((boolean)jsonObjectFinal.get("creacliente"));
				String strCreaCliente = "";
				if(creaCliente)
				{
					strCreaCliente = "true";
				}else
				{
					strCreaCliente = "false";
				}
	            int idCliente = ((Long)jsonObjectFinal.get("idcliente")).intValue();
	            //Obtenidos todos los parámetros realizamos el llamado al servicio
	            String rutaURLFinal = urlContactCenter + "ActualizarNumeroPedidoPixel?idpedido=" + idPedido + "&numpedidopixel=" + numeroFactura +  "&creacliente=" + strCreaCliente +  "&membercode=" + memberCode + "&idcliente=" + idCliente;
	    		HttpGet requestFinal = new HttpGet(rutaURLFinal);
	    		try
	    		{
	    			StringBuffer retornoFinal = new StringBuffer();
	    			//Se realiza la ejecución del servicio de finalizar pedido
	    			HttpResponse responseFinal = client.execute(requestFinal);
	    			BufferedReader rdFinal = new BufferedReader
	    				    (new InputStreamReader(
	    				    		responseFinPed.getEntity().getContent()));
	    			line = "";
	    			while ((line = rd.readLine()) != null) {
	    				retornoFinal.append(line);
	    				}
	    			
	    			System.out.println(retornoFinal.toString());
	    			
	    		}catch(Exception e3)
	    		{
	    			 e3.printStackTrace();
	    			 respuestaProceso = false;
	    		}
	            

	        } catch (Exception e2) {
	            e2.printStackTrace();
	            respuestaProceso = false;
	        }
			
			
		}catch(Exception e1)
		{
			e1.printStackTrace();
			respuestaProceso = false;
		}
		
		return(respuestaProceso);
	}

}
