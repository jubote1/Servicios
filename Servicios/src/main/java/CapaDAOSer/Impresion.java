package CapaDAOSer;

import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

import javax.print.*;

import ModeloSer.Correo;
import utilidadesSer.ControladorEnvioCorreo;



//La clase debe de implementar la impresión implements Printable

//clase pública que se ejecuta donde debe de estar el main que 
// llama a laotra clase.
public class Impresion
{
	
   public static void main (String impresion)
   {
	   String impresoraPrincipal = "Caja";
	   
	 //Cogemos el servicio de impresión por defecto (impresora por defecto)
	   PrintService service = PrintServiceLookup.lookupDefaultPrintService();
	   System.out.println("IMPRESORA ENCONTRADA " + service);
	   //Le decimos el tipo de datos que vamos a enviar a la impresora
	   //Tipo: bytes Subtipo: autodetectado
	   DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
	   //Creamos un trabajo de impresión
	   
	   try
	   {
		   DocPrintJob pj = service.createPrintJob();
		   //Nuestro trabajo de impresión envía una cadena de texto
		   String ss=new String(impresion);
		   byte[] bytes; 
		   //Transformamos el texto a bytes que es lo que soporta la impresora
		   bytes=ss.getBytes();
		   //Creamos un documento (Como si fuese una hoja de Word para imprimir)
		   Doc doc=new SimpleDoc(bytes,flavor,null);
		   //Obligado coger la excepción PrintException
		   try {
		     //Mandamos a impremir el documento
			   
		     //pj.print(doc, null);
			 cortehoja.printer(impresion, impresoraPrincipal);
		   }
		   catch (Exception e) {
		   //catch (PrintException e) {
		    System.out.println("Error al imprimir: "+e.getMessage());
		    ArrayList correos = GeneralDAO.obtenerCorreosParametroTienda("ERRORIMPRESION");
		    String tienda = TiendaDAO.obtenerNombreTienda();
		    Date fecha = new Date();
			Correo correo = new Correo();
			correo.setAsunto("ERROR IMPRESI�N TIENDA " + tienda + " " + fecha.toString());
			correo.setContrasena("Pizzaamericana2017");
			correo.setUsuarioCorreo("alertaspizzaamericana@gmail.com");
			correo.setMensaje("En este momento existen problemas de impresi�n en la tienda " + tienda + "\n" + e.toString() + ". Impresora Encontrada " + service);
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
			contro.enviarCorreoHTML();
		   }
	   }catch(Exception e)
	   {
		   System.out.println(e.toString() + e.getMessage() + e.getStackTrace());
		   return;
	   }
	  
	      
	   
	   
//	   try {
//		cortehoja.printer("", impresoraPrincipal);
//	} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	} catch (PrinterException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
   }
   
   
}