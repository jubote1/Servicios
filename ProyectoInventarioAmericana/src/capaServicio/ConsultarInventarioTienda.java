package capaServicio;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import capaControlador.InventarioCtrl;

/**
 * Servlet implementation class CalcularInventarioTienda
 */
@WebServlet("/ConsultarInventarioTienda")
public class ConsultarInventarioTienda extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ConsultarInventarioTienda() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * Este es el método del servlet que se encarga de recuperar los valores de idtienda y fecha a surtir
	 * con estos instanciará un objeto de la capa Controlador y la respuesta en un string con formato JSON será retornada.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/json");
		Logger logger = Logger.getLogger("log_file");
		HttpSession sesion = request.getSession(true);
		int idtienda;
        try
        {
        	idtienda = Integer.parseInt(request.getParameter("idtienda"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	idtienda = 0;
        }
        InventarioCtrl inv = new InventarioCtrl();
        String respuesta = inv.ConsultarInventarioTienda(idtienda);
        PrintWriter out = response.getWriter();
		out.write(respuesta);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
