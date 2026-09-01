package ModeloSer;

import java.util.Date;

public class DespachoReal {
    private int id;
    private Long despachoLogId;
    private int idDomiciliario;
    private int idTienda;
    private Date fecha;
    private Date horaSalida;
    private Date horaRegreso;
    private String observaciones;
    private Date creadoEn;

    // Constructor vacío
    public DespachoReal() {}

    // Constructor con todos los campos (excepto ID y creadoEn para inserciones)
    public DespachoReal(Long despachoLogId, int idDomiciliario, int idTienda, Date fecha, Date horaSalida, Date horaRegreso, String observaciones) {
        this.despachoLogId = despachoLogId;
        this.idDomiciliario = idDomiciliario;
        this.idTienda = idTienda;
        this.fecha = fecha;
        this.horaSalida = horaSalida;
        this.horaRegreso = horaRegreso;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Long getDespachoLogId() { return despachoLogId; }
    public void setDespachoLogId(Long despachoLogId) { this.despachoLogId = despachoLogId; }

    public int getIdDomiciliario() { return idDomiciliario; }
    public void setIdDomiciliario(int idDomiciliario) { this.idDomiciliario = idDomiciliario; }

    public int getIdTienda() { return idTienda; }
    public void setIdTienda(int idTienda) { this.idTienda = idTienda; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public Date getHoraSalida() { return horaSalida; }
    public void setHoraSalida(Date horaSalida) { this.horaSalida = horaSalida; }

    public Date getHoraRegreso() { return horaRegreso; }
    public void setHoraRegreso(Date horaRegreso) { this.horaRegreso = horaRegreso; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }
}