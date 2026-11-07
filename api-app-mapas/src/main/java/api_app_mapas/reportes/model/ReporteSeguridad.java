package api_app_mapas.reportes.model;

import api_app_mapas.usuario.model.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_seguridad")
public class ReporteSeguridad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte") private Long idReporte;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario") private Usuario usuario;
    @Column(name = "tipo_evento", nullable = false, length = 40) private String tipoEvento;
    @Column(name = "nivel_peligro", nullable = false, length = 20) private String nivelPeligro;
    @Column(nullable = false, length = 2000) private String descripcion;
    @Column(nullable = false, precision = 10, scale = 7) private BigDecimal latitud;
    @Column(nullable = false, precision = 10, scale = 7) private BigDecimal longitud;
    @Column(name = "radio_metros", nullable = false) private Integer radioMetros;
    @Column(nullable = false) private boolean anonimo;
    @Column(name = "estado_validacion", nullable = false, length = 20) private String estadoValidacion;
    @Column(nullable = false) private Integer confirmaciones;
    @Column(nullable = false) private Integer refutaciones;
    @Column(name = "fecha_expiracion", nullable = false) private LocalDateTime fechaExpiracion;
    @Column(length = 100) private String distrito;
    @Column(name = "imagen_url", length = 500) private String imagenUrl;
    @Column(name = "imagen_public_id", length = 255) private String imagenPublicId;
    @Column(name = "fecha_creacion", insertable = false, updatable = false) private LocalDateTime fechaCreacion;
    @Column(name = "fecha_actualizacion", insertable = false, updatable = false) private LocalDateTime fechaActualizacion;

    public Long getIdReporte() { return idReporte; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public String getNivelPeligro() { return nivelPeligro; }
    public void setNivelPeligro(String nivelPeligro) { this.nivelPeligro = nivelPeligro; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }
    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }
    public Integer getRadioMetros() { return radioMetros; }
    public void setRadioMetros(Integer radioMetros) { this.radioMetros = radioMetros; }
    public boolean isAnonimo() { return anonimo; }
    public void setAnonimo(boolean anonimo) { this.anonimo = anonimo; }
    public String getEstadoValidacion() { return estadoValidacion; }
    public void setEstadoValidacion(String estadoValidacion) { this.estadoValidacion = estadoValidacion; }
    public Integer getConfirmaciones() { return confirmaciones; }
    public void setConfirmaciones(Integer confirmaciones) { this.confirmaciones = confirmaciones; }
    public Integer getRefutaciones() { return refutaciones; }
    public void setRefutaciones(Integer refutaciones) { this.refutaciones = refutaciones; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime value) { fechaExpiracion = value; }
    public String getDistrito() { return distrito; }
    public void setDistrito(String value) { distrito = value; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String value) { imagenUrl = value; }
    public String getImagenPublicId() { return imagenPublicId; }
    public void setImagenPublicId(String value) { imagenPublicId = value; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
}
