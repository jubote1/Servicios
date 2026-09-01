package ModeloSer;
import java.sql.Timestamp;

public class PedidoAll {

    private int idPedidoTienda;
    private int idTienda;
    private double totalBruto;
    private double totalImpuesto;
    private double totalNeto;
    private int idCliente;
    private java.sql.Date fechaPedido;
    private int idPedidoContact;
    private Timestamp fechaInsercion;
    private String usuarioPedido;
    private int tiempoPedido;
    private int idTipoPedido;
    private int idEstado;
    private Integer idMotivoAnulacion; // Usamos Integer para permitir nulls
    private String usuarioAutAnulacion;
    private String obsAnulacion;
    private Integer idDomiciliario;
    private String obsDomiciliario;
    private String estacion;
    private String idPedidoAlt;
    private byte[] impreso;
    private String observacion;
    private String obsClienteGenerico;
    private byte[] logistica;
    private String programado;
    private String horaProgramado;
    private Integer idEmpleado;
    private String nombreEmpleado;
    private Boolean facturaGenerada;
    private String encuesta;
    private int numeroPulsador;

    // Constructor vacío
    public PedidoAll() {}

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

	public double getTotalBruto() {
		return totalBruto;
	}

	public void setTotalBruto(double totalBruto) {
		this.totalBruto = totalBruto;
	}

	public double getTotalImpuesto() {
		return totalImpuesto;
	}

	public void setTotalImpuesto(double totalImpuesto) {
		this.totalImpuesto = totalImpuesto;
	}

	public double getTotalNeto() {
		return totalNeto;
	}

	public void setTotalNeto(double totalNeto) {
		this.totalNeto = totalNeto;
	}

	public int getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(int idCliente) {
		this.idCliente = idCliente;
	}

	public java.sql.Date getFechaPedido() {
		return fechaPedido;
	}

	public void setFechaPedido(java.sql.Date fechaPedido) {
		this.fechaPedido = fechaPedido;
	}

	public int getIdPedidoContact() {
		return idPedidoContact;
	}

	public void setIdPedidoContact(int idPedidoContact) {
		this.idPedidoContact = idPedidoContact;
	}

	public Timestamp getFechaInsercion() {
		return fechaInsercion;
	}

	public void setFechaInsercion(Timestamp fechaInsercion) {
		this.fechaInsercion = fechaInsercion;
	}

	public String getUsuarioPedido() {
		return usuarioPedido;
	}

	public void setUsuarioPedido(String usuarioPedido) {
		this.usuarioPedido = usuarioPedido;
	}

	public int getTiempoPedido() {
		return tiempoPedido;
	}

	public void setTiempoPedido(int tiempoPedido) {
		this.tiempoPedido = tiempoPedido;
	}

	public int getIdTipoPedido() {
		return idTipoPedido;
	}

	public void setIdTipoPedido(int idTipoPedido) {
		this.idTipoPedido = idTipoPedido;
	}

	public int getIdEstado() {
		return idEstado;
	}

	public void setIdEstado(int idEstado) {
		this.idEstado = idEstado;
	}

	public Integer getIdMotivoAnulacion() {
		return idMotivoAnulacion;
	}

	public void setIdMotivoAnulacion(Integer idMotivoAnulacion) {
		this.idMotivoAnulacion = idMotivoAnulacion;
	}

	public String getUsuarioAutAnulacion() {
		return usuarioAutAnulacion;
	}

	public void setUsuarioAutAnulacion(String usuarioAutAnulacion) {
		this.usuarioAutAnulacion = usuarioAutAnulacion;
	}

	public String getObsAnulacion() {
		return obsAnulacion;
	}

	public void setObsAnulacion(String obsAnulacion) {
		this.obsAnulacion = obsAnulacion;
	}

	public Integer getIdDomiciliario() {
		return idDomiciliario;
	}

	public void setIdDomiciliario(Integer idDomiciliario) {
		this.idDomiciliario = idDomiciliario;
	}

	public String getObsDomiciliario() {
		return obsDomiciliario;
	}

	public void setObsDomiciliario(String obsDomiciliario) {
		this.obsDomiciliario = obsDomiciliario;
	}

	public String getEstacion() {
		return estacion;
	}

	public void setEstacion(String estacion) {
		this.estacion = estacion;
	}

	public String getIdPedidoAlt() {
		return idPedidoAlt;
	}

	public void setIdPedidoAlt(String idPedidoAlt) {
		this.idPedidoAlt = idPedidoAlt;
	}

	public byte[] getImpreso() {
		return impreso;
	}

	public void setImpreso(byte[] impreso) {
		this.impreso = impreso;
	}

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}

	public String getObsClienteGenerico() {
		return obsClienteGenerico;
	}

	public void setObsClienteGenerico(String obsClienteGenerico) {
		this.obsClienteGenerico = obsClienteGenerico;
	}

	public byte[] getLogistica() {
		return logistica;
	}

	public void setLogistica(byte[] logistica) {
		this.logistica = logistica;
	}

	public String getProgramado() {
		return programado;
	}

	public void setProgramado(String programado) {
		this.programado = programado;
	}

	public String getHoraProgramado() {
		return horaProgramado;
	}

	public void setHoraProgramado(String horaProgramado) {
		this.horaProgramado = horaProgramado;
	}

	public Integer getIdEmpleado() {
		return idEmpleado;
	}

	public void setIdEmpleado(Integer idEmpleado) {
		this.idEmpleado = idEmpleado;
	}

	public String getNombreEmpleado() {
		return nombreEmpleado;
	}

	public void setNombreEmpleado(String nombreEmpleado) {
		this.nombreEmpleado = nombreEmpleado;
	}

	public Boolean getFacturaGenerada() {
		return facturaGenerada;
	}

	public void setFacturaGenerada(Boolean facturaGenerada) {
		this.facturaGenerada = facturaGenerada;
	}

	public String getEncuesta() {
		return encuesta;
	}

	public void setEncuesta(String encuesta) {
		this.encuesta = encuesta;
	}

	public int getNumeroPulsador() {
		return numeroPulsador;
	}

	public void setNumeroPulsador(int numeroPulsador) {
		this.numeroPulsador = numeroPulsador;
	}

    
}