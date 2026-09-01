package ModeloSer;

import java.util.Date;

public class DespachoRealDet {
    private int id;
    private int despachoRealId;
    private int idPedido;
    private Long planRutaDetId;
    private Integer ordenEntrega;
    private Integer ordenPlanificada;
    private Date horaEntrega;
    private int intento;
    private Integer idEstadoEntrega;
    private Date horaLlegadaTienda;
    private Integer incidenciaTipoDespachoId;
    private Date creadoEn;

    // Constructor vacío
    public DespachoRealDet() {}

    // Constructor completo (sin id ni creadoEn, ideales para auto-generarse)
    public DespachoRealDet(int despachoRealId, int idPedido, Long planRutaDetId, Integer ordenEntrega, 
                           Integer ordenPlanificada, Date horaEntrega, int intento, Integer idEstadoEntrega, 
                           Date horaLlegadaTienda, Integer incidenciaTipoDespachoId) {
        this.despachoRealId = despachoRealId;
        this.idPedido = idPedido;
        this.planRutaDetId = planRutaDetId;
        this.ordenEntrega = ordenEntrega;
        this.ordenPlanificada = ordenPlanificada;
        this.horaEntrega = horaEntrega;
        this.intento = intento;
        this.idEstadoEntrega = idEstadoEntrega;
        this.horaLlegadaTienda = horaLlegadaTienda;
        this.incidenciaTipoDespachoId = incidenciaTipoDespachoId;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDespachoRealId() { return despachoRealId; }
    public void setDespachoRealId(int despachoRealId) { this.despachoRealId = despachoRealId; }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public Long getPlanRutaDetId() { return planRutaDetId; }
    public void setPlanRutaDetId(Long planRutaDetId) { this.planRutaDetId = planRutaDetId; }

    public Integer getOrdenEntrega() { return ordenEntrega; }
    public void setOrdenEntrega(Integer ordenEntrega) { this.ordenEntrega = ordenEntrega; }

    public Integer getOrdenPlanificada() { return ordenPlanificada; }
    public void setOrdenPlanificada(Integer ordenPlanificada) { this.ordenPlanificada = ordenPlanificada; }

    public Date getHoraEntrega() { return horaEntrega; }
    public void setHoraEntrega(Date horaEntrega) { this.horaEntrega = horaEntrega; }

    public int getIntento() { return intento; }
    public void setIntento(int intento) { this.intento = intento; }

    public Integer getIdEstadoEntrega() { return idEstadoEntrega; }
    public void setIdEstadoEntrega(Integer idEstadoEntrega) { this.idEstadoEntrega = idEstadoEntrega; }

    public Date getHoraLlegadaTienda() { return horaLlegadaTienda; }
    public void setHoraLlegadaTienda(Date horaLlegadaTienda) { this.horaLlegadaTienda = horaLlegadaTienda; }

    public Integer getIncidenciaTipoDespachoId() { return incidenciaTipoDespachoId; }
    public void setIncidenciaTipoDespachoId(Integer incidenciaTipoDespachoId) { this.incidenciaTipoDespachoId = incidenciaTipoDespachoId; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }
}
