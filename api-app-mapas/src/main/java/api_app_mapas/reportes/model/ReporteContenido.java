package api_app_mapas.reportes.model;

import api_app_mapas.usuario.model.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_contenido")
public class ReporteContenido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte_contenido") private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario") private Usuario usuario;
    @Column(name = "tipo_contenido", nullable = false, length = 20) private String tipoContenido;
    @Column(name = "id_contenido", nullable = false) private Long idContenido;
    @Column(nullable = false, length = 500) private String motivo;
    @Column(nullable = false, length = 20) private String estado;
    @Column(name = "fecha_creacion", insertable = false, updatable = false) private LocalDateTime fechaCreacion;
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public void setTipoContenido(String tipoContenido) { this.tipoContenido = tipoContenido; }
    public void setIdContenido(Long idContenido) { this.idContenido = idContenido; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public void setEstado(String estado) { this.estado = estado; }
    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getTipoContenido() { return tipoContenido; }
    public Long getIdContenido() { return idContenido; }
    public String getMotivo() { return motivo; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
