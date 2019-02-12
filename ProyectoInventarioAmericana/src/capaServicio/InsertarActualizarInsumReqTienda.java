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
import capaModelo.InsumoRequeridoTienda;

/**
 * Servlet implementation class InsertarDetalleDespachoTienda
 */
@WebServlet("/InsertarActualizarInsumReqTienda")
public class InsertarActualizarInsumReqTienda extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertarActualizarInsumReqTienda() {
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
		double cantidadMinima;
        int idtienda;
        int idinsumo;
        int diaSemana;
        try
        {
        	idtienda = Integer.parseInt(request.getParameter("idtienda"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	idtienda = 0;
        }
        try
        {
        	diaSemana = Integer.parseInt(request.getParameter("diasemana"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	diaSemana = 0;
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
        try
        {
        	cantidadMinima = Double.parseDouble(request.getParameter("cantidadminima"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	cantidadMinima = 0;
        }
        InventarioCtrl inv = new InventarioCtrl();
        InsumoRequeridoTienda insReqTienda = new InsumoRequeridoTienda(idinsumo, idtienda, cantidad, diaSemana, "",
    			"", 0, "", cantidadMinima);
        String respuesta = inv.insertarActualizarInsumReqTienda(insReqTienda);
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
