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
 * Servlet implementation class InsertarDetalleDespachoTienda
 */
@WebServlet("/InsertarDetalleDespachoTienda")
public class InsertarDetalleDespachoTienda extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertarDetalleDespachoTienda() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/json");
		Logger logger = Logger.getLogger("log_file");
		HttpSession sesion = request.getSession(true);
		double cantidad;
        int iddespacho;
        int idinsumo;
        try
        {
        	iddespacho = Integer.parseInt(request.getParameter("iddespacho"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	iddespacho = 0;
        }
        try
        {
        	idinsumo = Integer.parseInt(request.getParameter("idinsumo"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	idinsumo = 0;
        }
        try
        {
        	cantidad = Double.parseDouble(request.getParameter("cantidad"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	cantidad = 0;
        }
        String contenedor = request.getParameter("contenedor");
        InventarioCtrl inv = new InventarioCtrl();
        String respuesta = inv.InsertarDetalleInsumoDespachoTienda(iddespacho,idinsumo,cantidad,contenedor);
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
