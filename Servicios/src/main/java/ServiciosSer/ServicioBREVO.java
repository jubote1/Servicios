package ServiciosSer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
import org.json.JSONArray;
import org.json.JSONObject;

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
import ModeloSer.PedidoPlanFidelizacion;
import ModeloSer.Tienda;
import ModeloSer.Usuario;
import capaDAOCC.ClienteFidelizacionDAO;
import capaDAOCC.FidelizacionTransaccionDAO;
import capaDAOCC.IntegracionCRMDAO;
import capaModeloCC.FidelizacionTransaccion;
import capaModeloCC.IntegracionCRM;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import utilidadesSer.ControladorEnvioCorreo;

public class ServicioBREVO {
	
	
	
	
public static void main(String[] args)
{
	ServicioBREVO reporteReplicaUsuarios = new ServicioBREVO();
	reporteReplicaUsuarios.envioInsuperables();
	
}

public void envioInsuperables()
{
	String[] correos = {"leylondo16@hotmail.com",
			"santi.sanzro@gmail.com",
			"josequinterogil@gmail.com",
			"sescudero54@gmail.com",
			"valentina.romeroangel@gmail.com",
			"florelly.gallego@gmail.com",
			"ardeco@outlook.es",
			"santiagoalv51@gmail.com",
			"juanbarrada2019@gmail.com",
			"fernandatejada.15@gmail.com",
			"RDJULIANA16@GMAIL.COM",
			"elmylo0113@hotmail.com",
			"patojito413@gmail.com",
			"camy1098@hotmail.com",
			"vicmaryenixe@gmail.com",
			"luisa.guzmanga@amigo.edu.co",
			"Juanlopezm25@hotmail.com",
			"luisgmovs@gmail.com",
			"piipe0198@hotmail.com",
			"ktyps1893@gmail.com",
			"jander626@gmail.com",
			"ivoneemontoya1993@gmail.com",
			"daniela.isaza590@pascualbravo.edu.co",
			"carvajaldaniela32@gmail.com",
			"lauri0821@gmail.com",
			"mayezca80@hotmail.com",
			"acuario0921@hotmail.com",
			"luisa_f_325@hotmail.com",
			"marcela.henao0811@gmail.com",
			"giordano376@hotmail.com",
			"planetaalejandralondono@gmail.com",
			"joseaguirreluna@hotmail.com",
			"osneideroleasanchez32@gmail.com",
			"Katherinecuartas4@gmail.com",
			"bivicardona87@hotmail.com",
			"eliza2476@gmail.com",
			"leydyguzman2113@gmail.com",
			"adrianarciniegas@gmail.com",
			"oflicuo@gmail.com",
			"vaheba100@gmail.com",
			"cajaram3@hotmail.com",
			"yulicas8@gmail.com",
			"violetaayama@gmail.com",
			"galvissantiago681@gmail.com",
			"Isabel.sarabia@hotmail.com",
			"csvilladelsocorro@gmail.com",
			"juanjohincapie@gmail.com",
			"alejobetanrepo@gmail.com",
			"aquintero87@hotmail.com",
			"carlitosbon7@hotmail.com",
			"jjunior083@gmail.com",
			"luisamendoza83@hotmail.com",
			"wilsoncardona9512@gmail.com",
			"carlos.alvarez.velez@gmail.com",
			"mj-maggy@hotmail.com",
			"paulina_gr@hotmail.com",
			"sansonjk2010@hotmail.com",
			"sukha.paraelalma@gmail.com",
			"luzmamesa@hotmail.com",
			"luisfgonzal@gmail.com",
			"juanfelipeholguintamayo@gmail.com",
			"ccrisguerra@hotmail.com",
			"mariuran98@hotmail.com",
			"leandrolp456@gmail.com",
			"julian200520@gmail.com",
			"natymarquezr@gmail.com",
			"patricia.amarilis@hotmail.com",
			"Andrex.nava@gmail.com",
			"gupego2005@gmail.com",
			"maira0411@hotmail.com",
			"jonico1804@gmail.com",
			"valentina-c.a@hotmail.com",
			"pocoton5@hotmail.com",
			"kamilaarboleda61@gmail.com",
			"samij95@hotmail.com",
			"juan_diego_01@hotmail.com",
			"a.correa9410@gmail.com",
			"carolina.henao3005@gmail.com",
			"isabellatreszulu3005@hotmail.com",
			"cataquintero26@gmail.com",
			"valent.escobar@gmail.com",
			"natadiaz81@yahoo.com",
			"marianarodriguezusuga@gmail.com",
			"isasamu0507@gmail.com",
			"danilo1905@outlook.com",
			"andreagiraldov13@gmail.com",
			"jhonatannt03@gmail.com",
			"juanhius@hotmail.com",
			"david.aristizabalgiraldo1034@gmail.com",
			"anamariamenesesacosta@gmail.com",
			"yeider1684@gmail.com",
			"isabelcristinag24@hotmail.com",
			"mtcamila0112@gmail.com",
			"integration@rappi.com",
			"annietoti88@hotmail.com",
			"smpalacio@alemautos.com.co",
			"dcmarinr@gmail.com",
			"samuelaguirre25comfe@gmail.com",
			"mary.luz.gallego@hotmail.com",
			"leidyjohanamazo@gmail.com",
			"geraldinegarciamedellin@gmail.com",
			"carolina0229@gmail.com",
			"litha-0305@hotmail.com",
			"simonpestana0803@hotmail.com",
			"yuliposada@gmail.com",
			"leidy14_489@hotmail.com",
			"dmarceg@gmail.com",
			"juagomez99@gmail.com",
			"bety-1604@hotmail.com",
			"alejandra.abeja.145@gmail.com",
			"mariacamilaa200303@gmail.com",
			"gomezcmariai10@gmail.com",
			"M.taborda1897@pascualbravo.edu.co",
			"martha.piedrahita@vocesporeltrabajo.org",
			"valentinamesa46@gmail.com",
			"amcicorreai@gmail.com",
			"leachim80@gmail.com",
			"marchbusi3@gmail.com",
			"lizge20@hotmail.com",
			"malvarado0992@gmail.com",
			"crisli8923@hotmail.com",
			"karen.cano1919@gmail.com",
			"zapatalopezj@gmail.com",
			"maryud627@gmail.com",
			"beatrizmunera19@gmail.com",
			"dimonsalvemo@unal.edu.co",
			"vanearanpe@gmail.com",
			"katherinefernandez729@gmail.com",
			"ospinan50@gmail.com",
			"enithcare04@gmail.com",
			"luiscaxi1019@gmail.com",
			"susanam.losada@gmail.com",
			"pepe47@outlook.cl",
			"danielrr0691@gmail.com",
			"julianandresosorioparra999@gmail.com",
			"luisgallegocano@gmail.com",
			"ana.grisales1987@gmail.com",
			"mosma80@hotmail.com",
			"daro1995@hotmail.com",
			"wendy.valencia0595@gmail.com",
			"majome17@hotmail.com",
			"Mayelyalvarez08@hotmail.com",
			"ddelgado220193@gmail.com",
			"esezeta04@gmail.com",
			"gutierrezjuanita94@gmail.com",
			"wolftyrant616@gmail.com",
			"tuidea86@gmail.com",
			"Juandiego777@gmail.com",
			"luisferbedoya@gmail.com",
			"alexmofra@gmail.com",
			"macamilamunoza@outlook.com",
			"luisapabs@gmail.com",
			"gonsalesm2@gmail.com",
			"dianis316@yahoo.com",
			"jfmaes02@gmail.com",
			"jvrgasmon@gmail.com",
			"sebas-a-2@hotmail.com",
			"civaesmo@gmail.com",
			"pauli091594@gmail.com",
			"davidmg0814@hotmail.com",
			"camilarangob@gmail.com",
			"ana.castaneda.echavarria@gmail.com",
			"jplcomunicador@gmail.com",
			"luisdavidmezahenao@gmail.com",
			"miguelars460@gmail.com",
			"meomfggv@gmail.com",
			"ricardoac44@hotmail.com",
			"martardiazl@yahoo.es",
			"pipehr77@gmail.com",
			"vivianhoyos1@hotmail.com",
			"carom2306@gmail.com",
			"animaba0602@gmail.com",
			"sarisnaranjo18@gmail.com",
			"catalinayepes_31@hotmail.com",
			"kellyramirez9311@gmail.com",
			"Rosfer710@gmail.com",
			"mpinedavele@uniminuto.edu.co",
			"giselle.7montano@gmail.com",
			"luiisanegrete9@gmail.com",
			"anamariaanguloescudero@gmail.com",
			"tatianaospina706@gmail.com",
			"mariarocelymonsalve@hotmail.com",
			"marber1989@hotmail.com",
			"psanaespinal@hotmail.com",
			"llanogi@hotmail.com",
			"ramiro.ojeda88@hotmail.com",
			"carlitospati123@gmail.com",
			"carlosandres.rojasvasquez@gmail.com",
			"paoandrearosasw@gmail.com",
			"jm024@gmail.com",
			"kathe.perza@gmail.com",
			"davenava@gmail.com",
			"juanescorredu@outlook.es",
			"tatianaavergara@hotmail.com",
			"dmpiedrahita@hotmail.com",
			"dianamaz24@hotmail.com",
			"julianarodriguez2002@hotmail.com",
			"saragomez92@outlook.com",
			"sabarrera@unal.edu.co",
			"anitaac_220@hotmail.com",
			"anakarina_140@hotmail.com",
			"diana.p224@gmail.com",
			"juanfernandoagudelo16@hotmail.com",
			"saristy29@gmail.com",
			"daniela.garcia.0324@gmail.com",
			"kevinsko98@gmail.com",
			"jotsypinilla@gmail.com",
			"vivicalderong@gmail.com",
			"tatianav2005@gmail.com",
			"pcano1019@gmail.com",
			"ladygarcia0882@gmail.com",
			"danaya.natural@gmail.com",
			"hennerys.serrano@gmail.com",
			"cardonajose2001@gmail.com",
			"jricardo.benitez@gmail.com",
			"jeor9610@gmail.com",
			"mauriciobarrera01@gmail.com",
			"alexrengifo990@gmail.com",
			"maribelcaicedo@hotmail.com",
			"nancylorenas@gmail.com",
			"roner.ortega@gmail.com",
			"andreacastropalacios333@gmail.com",
			"felipemr889@mail.com",
			"esterilizacion.vida@gmail.com",
			"melissagrojas30@gmail.com",
			"Marinbustamantejohana@gmail.com",
			"yiama16@gmail.com",
			"anamrl.213@gmail.com",
			"Juancamilogarcia41@gmail.com",
			"juanita11riveraosorio@gmail.com",
			"anamariamorenoherrera@gmail.com",
			"jramirce@gmail.com",
			"aimirandae@gmail.com",
			"apipe1703@gmail.com",
			"gustavopabon1981@gmail.com",
			"laura_1016@hotmail.com",
			"badjdlf@hotmail.com",
			"stiivenburitica@gmail.com",
			"cris198407@gmail.com",
			"ziomi31@hotmail.com",
			"yorswell2006@gmail.com",
			"sarazuluaicasalazar@gmail.com",
			"tonyalzate0456@gmail.com",
			"kevinocag@gmail.com",
			"saraga1298@gmail.com",
			"David.luis0498@gmail.com",
			"mateousuga11@gmail.com",
			"sindysanchezgallego1991@gmail.com",
			"yakelinflorez95@gmail.com",
			"dpcastanot@hotmail.com",
			"stiven2010200@gmail.com",
			"matias.agudelop@gmail.com",
			"dianamcallet@gmail.com"};
	//Realizamos recuperación de datos de integración de brevo
	IntegracionCRM brevo = IntegracionCRMDAO.obtenerInformacionIntegracion("BREVO");
	//Se realiza logica para envio de correo
	OkHttpClient client = new OkHttpClient();
    // Configuración global
    String apiKey = brevo.getAccessToken();
    String senderEmail = "mercadeo@pizzaamericana.com.co";
    String senderName = "Pizza Americana";
    String subjectDefault = "COMBOS DESDE $39.900 🍕";
    ArrayList<JSONObject> destinatarios = new ArrayList();
    int templateId = 5; // ID de la plantilla en Brevo
	for(String correoTemp : correos)
	{
		destinatarios.add(new JSONObject().put("email", correoTemp));
	}
				
		//Realiza el envío de correo con Brevo por tienda
		// Construcción del JSON principal
		JSONObject paramsDefault = new JSONObject();
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("subject", subjectDefault);
        jsonRequest.put("sender", new JSONObject().put("email", senderEmail).put("name", senderName));
        jsonRequest.put("templateId", templateId);
        //jsonRequest.put("params", paramsDefault);
        // Construcción de `messageVersions` con parámetros personalizados
        JSONArray messageVersions = new JSONArray();
        for (JSONObject destinatario : destinatarios) {
            // Parámetros personalizados por destinatario
            JSONObject params = new JSONObject();
            params.put("nombre", "Insuperable");
            JSONObject messageVersion = new JSONObject();
            messageVersion.put("to", new JSONArray().put(
                new JSONObject().put("email", destinatario.getString("email"))
            ));
            //messageVersion.put("params", params);
           
            messageVersions.put(messageVersion);
        }
        jsonRequest.put("messageVersions", messageVersions);
       
        System.out.println(jsonRequest.toString(2)); // Formateado para mejor lectura
        // Envío de la solicitud HTTP
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonRequest.toString());
        Request request = new Request.Builder()
                .url("https://api.brevo.com/v3/smtp/email")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("api-key", apiKey)
                .build();
        // Ejecución de la solicitud
        try (Response response = client.newCall(request).execute()) {
            System.out.println("Response Code: " + response.code());
            System.out.println("Response Body: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }

	
}


}




