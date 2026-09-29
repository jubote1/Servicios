package utilidadesSer;

import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import ModeloSer.Correo;
import CapaDAOSer.ParametrosDAO;
import ModeloSer.CorreoElectronico;

public class ControladorEnvioCorreo {

private Correo  c;
private ArrayList correos;


public ControladorEnvioCorreo(Correo co, ArrayList correosenv)
{
	this.c= co;
	this.correos = correosenv;
}

public boolean enviarCorreo()
{
	try
	{
		Properties p = new Properties();
		p.put("mail.smtp.host", "smtp.gmail.com");
		p.put("mail.smtp.ssl.protocols", "TLSv1.2");
		p.setProperty("mail.smtp.starttls.enable", "true");
		p.setProperty("mail.smtp.port", "587");
		p.setProperty("mail.smtp.user", c.getUsuarioCorreo());
		p.setProperty("mail.smtp.auth", "true");
		
		Session s = Session.getDefaultInstance(p, null);
		BodyPart texto = new MimeBodyPart();
		texto.setText(c.getMensaje());
		MimeMultipart m = new MimeMultipart();
		m.addBodyPart(texto);
		//Agregamos los archivos Anexos
		String[] archAnexos = c.getRutasArchivos();
		for(int i = 0; i < archAnexos.length; i++)
		{
			BodyPart adjunto = new MimeBodyPart();
			String cadenaCompleta = archAnexos[i];
			if(!(cadenaCompleta == null))
			{
				//La convencion es "ruta%&nombre_que_ve_el_destinatario". Antes se
				//pedian los dos pedazos sin preguntar si el segundo existia, y un
				//proceso que mandara solo la ruta reventaba con NoSuchElementException
				//ANTES de armar el mensaje: no salia el correo, y en el log quedaba
				//el nombre pelado de la excepcion, sin decir de que archivo ni de
				//que reporte. Paso con el cierre semanal de consignaciones.
				//
				//Ahora, si no viene el nombre, se toma el del propio archivo. El
				//correo sale y el adjunto se llama como corresponde. Cuando si
				//viene, no cambia nada.
				StringTokenizer tokens = new StringTokenizer(cadenaCompleta,"%&");
				String ruta = tokens.hasMoreTokens() ? tokens.nextToken() : cadenaCompleta;
				String nombreArchivo = tokens.hasMoreTokens()
						? tokens.nextToken()
						: new java.io.File(ruta).getName();
				adjunto.setDataHandler(new DataHandler(new FileDataSource(ruta)));
				adjunto.setFileName(nombreArchivo);
				m.addBodyPart(adjunto);
			}
		}
		
		//
		MimeMessage mensaje = new MimeMessage(s);
		mensaje.setFrom(new InternetAddress(c.getUsuarioCorreo()));
		//Ponemos un control para cuando no hay destinatarios del correo y evitarse una demora en el envío
		if(correos.size() == 0)
		{
			return(false);
		}
		for(int i = 0; i< correos.size(); i++)
		{
			mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress((String)correos.get(i)));
		}
		mensaje.setSubject(c.getAsunto());
		mensaje.setContent(m);
		Transport t = s.getTransport("smtp");
		t.connect(c.getUsuarioCorreo(),c.getContrasena());
		t.sendMessage(mensaje, mensaje.getAllRecipients());
		t.close();
		return(true);
		
	}
	catch(Exception e)
	{
		System.out.println(e.toString() + e.getStackTrace() + e.getMessage());
		return(false);
	}
	
}

public boolean enviarCorreoHTML()
{
	try
	{
		Properties p = new Properties();
		p.put("mail.debug", "true");
		p.put("mail.smtp.host", "smtp.gmail.com");
		p.put("mail.smtp.ssl.protocols", "TLSv1.2");
		p.setProperty("mail.smtp.starttls.enable", "true");
		p.setProperty("mail.smtp.port", "587");
		p.setProperty("mail.smtp.user", c.getUsuarioCorreo());
		p.setProperty("mail.smtp.auth", "true");
		
		Session s = Session.getDefaultInstance(p, null);
		BodyPart texto = new MimeBodyPart();
		texto.setContent(c.getMensaje(), "text/html; charset=utf-8");
		//texto.setText(c.getMensaje());
		MimeMultipart m = new MimeMultipart();
		
		m.addBodyPart(texto);
		MimeMessage mensaje = new MimeMessage(s);
		mensaje.setFrom(new InternetAddress(c.getUsuarioCorreo()));
		//Ponemos un control para cuando no hay destinatarios del correo y evitarse una demora en el envío
		if(correos.size() == 0)
		{
			return(false);
		}
		for(int i = 0; i< correos.size(); i++)
		{
			mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress((String)correos.get(i)));
		}
		mensaje.setSubject(c.getAsunto());
		mensaje.setContent(m, "text/html");
		Transport t = s.getTransport("smtp");
		t.connect(c.getUsuarioCorreo(),c.getContrasena());
		t.sendMessage(mensaje, mensaje.getAllRecipients());
		t.close();
		return(true);
		
	}
	catch(Exception e)
	{
		System.out.println(e.toString());
		return(false);
	}
	
}

public boolean enviarCorreoHTMLAnexo()
{
	try
	{
		Properties p = new Properties();
		p.put("mail.smtp.host", "smtp.gmail.com");
		p.put("mail.smtp.ssl.protocols", "TLSv1.2");
		p.setProperty("mail.smtp.starttls.enable", "true");
		p.setProperty("mail.smtp.port", "587");
		p.setProperty("mail.smtp.user", c.getUsuarioCorreo());
		p.setProperty("mail.smtp.auth", "true");
		
		Session s = Session.getDefaultInstance(p, null);
		BodyPart texto = new MimeBodyPart();
		texto.setContent(c.getMensaje(), "text/html; charset=utf-8");
		//texto.setText(c.getMensaje());
		MimeMultipart m = new MimeMultipart();
		
		m.addBodyPart(texto);
		//Revisan si hay Anexos para enviar tambien
		//Agregamos los archivos Anexos
		String[] archAnexos = c.getRutasArchivos();
		for(int i = 0; i < archAnexos.length; i++)
		{
			BodyPart adjunto = new MimeBodyPart();
			String cadenaCompleta = archAnexos[i];
			if(!(cadenaCompleta == null))
			{
				//La convencion es "ruta%&nombre_que_ve_el_destinatario". Antes se
				//pedian los dos pedazos sin preguntar si el segundo existia, y un
				//proceso que mandara solo la ruta reventaba con NoSuchElementException
				//ANTES de armar el mensaje: no salia el correo, y en el log quedaba
				//el nombre pelado de la excepcion, sin decir de que archivo ni de
				//que reporte. Paso con el cierre semanal de consignaciones.
				//
				//Ahora, si no viene el nombre, se toma el del propio archivo. El
				//correo sale y el adjunto se llama como corresponde. Cuando si
				//viene, no cambia nada.
				StringTokenizer tokens = new StringTokenizer(cadenaCompleta,"%&");
				String ruta = tokens.hasMoreTokens() ? tokens.nextToken() : cadenaCompleta;
				String nombreArchivo = tokens.hasMoreTokens()
						? tokens.nextToken()
						: new java.io.File(ruta).getName();
				adjunto.setDataHandler(new DataHandler(new FileDataSource(ruta)));
				adjunto.setFileName(nombreArchivo);
				m.addBodyPart(adjunto);
			}
		}
		MimeMessage mensaje = new MimeMessage(s);
		mensaje.setFrom(new InternetAddress(c.getUsuarioCorreo()));
		//Ponemos un control para cuando no hay destinatarios del correo y evitarse una demora en el envío
		if(correos.size() == 0)
		{
			return(false);
		}
		for(int i = 0; i< correos.size(); i++)
		{
			mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress((String)correos.get(i)));
		}
		mensaje.setSubject(c.getAsunto());
		mensaje.setContent(m, "text/html");
		Transport t = s.getTransport("smtp");
		t.connect(c.getUsuarioCorreo(),c.getContrasena());
		t.sendMessage(mensaje, mensaje.getAllRecipients());
		t.close();
		return(true);
		
	}
	catch(Exception e)
	{
		System.out.println(e.toString());
		return(false);
	}
	
}

public static CorreoElectronico recuperarCorreo(String variableCuenta, String variableClave)
{
	String cuentaCorreo = ParametrosDAO.retornarValorAlfanumerico(variableCuenta);
	String claveCorreo = ParametrosDAO.retornarValorAlfanumerico(variableClave);
	CorreoElectronico respuesta = new CorreoElectronico(cuentaCorreo, claveCorreo);
	return(respuesta);
}
	
}
