package ServiciosSer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

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
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import CapaDAOSer.FacturaElectronicaGeneradaDAO;
import CapaDAOSer.GeneralDAO;
import CapaDAOSer.ItemInventarioDAO;
import CapaDAOSer.ParametrosDAO;
import CapaDAOSer.PedidoDAO;
import CapaDAOSer.TiendaDAO;
import CapaDAOSer.UsuarioDAO;
import ModeloSer.Correo;
import ModeloSer.CorreoElectronico;
import ModeloSer.EmpleadoBiometria;
import ModeloSer.Insumo;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import capaControladorCC.PedidoCtrl;
import capaControladorPOS.PedidoCtrl.ValidationResult;
import capaDAOCC.IntegracionCRMDAO;
import capaModeloCC.IntegracionCRM;
import capaModeloPOS.PedidoFactElectronica;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioValidacionFacturasElectronicas {
	
	
	
/**
 * Este programa se encargar� de correr como un servicio todos los d�as a las 12:50 am, con el fin de revisar
 * si los sistemas se encuentran cerrados y enviar un mensaje al correo con la revisi�n.
 * @param args
 * @throws IOException 
 */
public static void main(String[] args) throws IOException
{
	IntegracionCRM intMatias = IntegracionCRMDAO.obtenerInformacionIntegracion("MATIAS");
	ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendasLocal();
	for(Tienda tien : tiendas)
	{
		if(!tien.getHostBD().equals(new String("")))
		{
			//ParametrosDAO.EditarParametroTiendaRemota(tien.getHostBD(),"MATIASAPITOKEN", intMatias.getAccessToken());
			String fechaAnterior = "2024-06-01";
			String fechaPosterior = "2024-06-10";
			ArrayList<PedidoFactElectronica> pedidosVerificar= PedidoDAO.obtenerPedidosValidarFactElectronica(fechaAnterior, fechaPosterior, tien.getHostBD());
			PedidoFactElectronica pedidoTemp;
			ValidationResult resValidacion;
			for(int i = 0; i < pedidosVerificar.size(); i++)
			{
				pedidoTemp = pedidosVerificar.get(i);
				resValidacion = ValidarDocumentogenerado( pedidoTemp.getPrefijo(), pedidoTemp.getNumerodocumento(), intMatias.getAccessToken());
				if(resValidacion !=  null) 
				{
					 if (resValidacion.isFound()) 
					 {				 
				           JSONObject data =  resValidacion.getJson();
				           int idPedidoJSON = Integer.parseInt((String)data.get("notas"));
				           //Si coinciden ambos pedidos es porque si fue generada la factura
				           if(idPedidoJSON == pedidoTemp.getIdPedido())
				           {
				        	   //Debemos de marcar el pedido como genero factura electronica
				        	   PedidoDAO.ActualizarPedidoFacturado(pedidoTemp.getIdPedido(), true, tien.getHostBD());
				        	   //Actualizamos mensaje en LOG que indique que fue reproceso.
				        	   FacturaElectronicaGeneradaDAO.actualizarLogFacturaElectronicaReproceso(pedidoTemp.getIdLog(), tien.getHostBD());
				           }
					 }
				}
			}
		}
	}
}

public static ValidationResult ValidarDocumentogenerado(String prefijo, String num_doc, String token) {
    ValidationResult result;
    JSONObject rs =  null;
    try {
        String num = prefijo + num_doc;
        Request request = new Request.Builder()
                .url("https://api-v2.matias-api.com/api/ubl2.1/documents?query=" + num)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .get()
                .build();
        
        OkHttpClient client = new OkHttpClient();
        okhttp3.Response response = client.newCall(request).execute();
     
        int statusCode = response.code();
        String responseBody = response.body().string();
      //  System.out.print(responseBody);

        if (statusCode == 200) {
        	
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> map = gson.fromJson(responseBody, type);

            // Acceder a los datos del Map
            Map<String, Object> dataRecords = (Map<String, Object>) map.get("dataRecords");

            // Obtener la lista de datos
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataRecords.get("data");

            // Iterar sobre la lista y verificar si se encontró el documento
            boolean found = false;
            
            for (Map<String, Object> data : dataList) {
                String documentNumber = (String) data.get("document_number");
   
                if (num.equals(documentNumber)) {
                    found = true;
                    Map<String, Object> jsonData = (Map<String, Object>) data.get("jsonData");
                    String notes=  (String) jsonData.get("notes");
                    Number is_valid = (Number) data.get("is_valid"); // Puede ser Double o Integer
                    int is_validint = is_valid.intValue(); 
                    boolean estado = (is_validint == 1);
                    String mensaje_estado ="";
                    if(estado) {
                    	mensaje_estado = "Validado ante la Dian.";
                    }else {
                    	mensaje_estado = "Rechazado por la Dian.";
                    }
                    rs =new  JSONObject();
                    rs.put("documento", documentNumber);
                    rs.put("notas", notes);
                    rs.put("estado", mensaje_estado);
                    if(estado)   
                   	{
                   		break;
                   	} 
                }
                
            }
            result = new ValidationResult(found, null,rs);
        } else {
            result = new ValidationResult(false, new RuntimeException( "Error de estado " + statusCode),rs);
        }

    } catch (Exception e) {
        System.out.println("Error:" + e.getMessage());
        result = new ValidationResult(false, e,rs);
    }
    return result;
}


}






