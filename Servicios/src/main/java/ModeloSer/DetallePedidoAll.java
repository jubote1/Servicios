package ModeloSer;
import java.sql.Timestamp;

public class DetallePedidoAll {

	private int idDetallePedido;
    private int idPedidoTienda;
    private int idTienda;
    private int idProducto;
    private double cantidad;
    private double valorUnitario;
    private double valorTotal;
    private double valorImpuesto;
    private String observacion;
    private int idDetallePedidoMaster;
    private Integer idDetalleModificador;
    private String descargoInventario;
    private Integer idMotivoAnulacion;
    private String obsAnulacion;
    private String usuarioAnulacion;
    private String usuarioAutAnulacion;
    
    
	public int getIdDetallePedido() {
		return idDetallePedido;
	}
	public void setIdDetallePedido(int idDetallePedido) {
		this.idDetallePedido = idDetallePedido;
	}
	public int getIdPedidoTienda() {
		return idPedidoTienda;
	}
	public void setIdPedidoTienda(int idPedidoTienda) {
		this.idPedidoTienda = idPedidoTienda;
	}
	public int getIdTienda() {
		return idTienda;
	}
	public void setIdTienda(int idTienda) {
		this.idTienda = idTienda;
	}
	public int getIdProducto() {
		return idProducto;
	}
	public void setIdProducto(int idProducto) {
		this.idProducto = idProducto;
	}
	public double getCantidad() {
		return cantidad;
	}
	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}
	public double getValorUnitario() {
		return valorUnitario;
	}
	public void setValorUnitario(double valorUnitario) {
		this.valorUnitario = valorUnitario;
	}
	public double getValorTotal() {
		return valorTotal;
	}
	public void setValorTotal(double valorTotal) {
		this.valorTotal = valorTotal;
	}
	public double getValorImpuesto() {
		return valorImpuesto;
	}
	public void setValorImpuesto(double valorImpuesto) {
		this.valorImpuesto = valorImpuesto;
	}
	public String getObservacion() {
		return observacion;
	}
	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
	public int getIdDetallePedidoMaster() {
		return idDetallePedidoMaster;
	}
	public void setIdDetallePedidoMaster(int idDetallePedidoMaster) {
		this.idDetallePedidoMaster = idDetallePedidoMaster;
	}
	public Integer getIdDetalleModificador() {
		return idDetalleModificador;
	}
	public void setIdDetalleModificador(Integer idDetalleModificador) {
		this.idDetalleModificador = idDetalleModificador;
	}
	public String getDescargoInventario() {
		return descargoInventario;
	}
	public void setDescargoInventario(String descargoInventario) {
		this.descargoInventario = descargoInventario;
	}
	public Integer getIdMotivoAnulacion() {
		return idMotivoAnulacion;
	}
	public void setIdMotivoAnulacion(Integer idMotivoAnulacion) {
		this.idMotivoAnulacion = idMotivoAnulacion;
	}
	public String getObsAnulacion() {
		return obsAnulacion;
	}
	public void setObsAnulacion(String obsAnulacion) {
		this.obsAnulacion = obsAnulacion;
	}
	public String getUsuarioAnulacion() {
		return usuarioAnulacion;
	}
	public void setUsuarioAnulacion(String usuarioAnulacion) {
		this.usuarioAnulacion = usuarioAnulacion;
	}
	public String getUsuarioAutAnulacion() {
		return usuarioAutAnulacion;
	}
	public void setUsuarioAutAnulacion(String usuarioAutAnulacion) {
		this.usuarioAutAnulacion = usuarioAutAnulacion;
	}
    
}