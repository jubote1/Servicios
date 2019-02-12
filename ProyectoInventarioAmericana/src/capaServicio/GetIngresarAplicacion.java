package capaServicio;

import java.io.IOException;
import java.io.IOException.*;
import java.io.PrintWriter;
import capaModelo.Usuario;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import capaControlador.AutenticacionCtrl;
import org.apache.log4j.Logger;
/**
 * Servlet implementation class IngresarAplicacion
 * Servicio utilizado para el logueo de la aplicación, recibiendo usuario y password, validando el resultado de la operacion
 * y retornando un resultado para que el sistema interprete y continue la carga de la interface de pedidos.
 */
@WebServlet("/GetIngresarAplicacion")
public class GetIngresarAplicacion extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
  

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * Servicio para el logueo a la aplicación recibiendo los parámetros de usuario y login, e invocando al método autenticarUsuario 
	 * de la capa Autenticacion controlador, si el proceso es exitoso, se creará un objeto tipo usuario y se dejará dentro de la sesión.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.addHeader("Access-Control-Allow-Origin", "*");
		Logger logger = Logger.getLogger("log_file");
		HttpSession sesion = request.getSession(true);
		String user = request.getParameter("txtUsuario");
        String pass = request.getParameter("txtPassword");
        AutenticacionCtrl aut = new AutenticacionCtrl();
        logger.info("Solicitud Logueo del usuario " + user);
        boolean resultado = aut.autenticarUsuario(user, pass);
        PrintWriter out = response.getWriter();
        if (resultado){
        		Usuario usuario = new Usuario(user);
        		sesion.setAttribute("usuario", usuario);
        		logger.info("El logueo del usuario " + user + " ha sido satisfactorio");
        		out.write("OK");
        		//response.sendRedirect("http://localhost:8080/ProyectoPizzaAmericana/Pedidos.html");
        }
        else{
        	logger.error("El logueo del usuario " + user + "no ha sido satisfactorio");
        	out.write("Error en el Logueo, favor verifique usuario y contraseña");
        	
        }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
		
	}
}
