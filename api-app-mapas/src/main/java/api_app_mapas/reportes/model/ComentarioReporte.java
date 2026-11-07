package api_app_mapas.reportes.model;

import api_app_mapas.usuario.model.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios_reporte")
public class ComentarioReporte {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comentario") private Long idComentario;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reporte", nullable = false) private ReporteSeguridad reporte;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false) private Usuario usuario;
    @Column(nullable = false, length = 1000) private String comentario;
    @Column(name = "imagen_url", length = 500) private String imagenUrl;
    @Column(name = "imagen_public_id", length = 255) private String imagenPublicId;
    @Column(name = "fecha_creacion", insertable = false, updatable = false) private LocalDateTime fechaCreacion;
    public Long getIdComentario() { return idComentario; }
    public ReporteSeguridad getReporte() { return reporte; }
    public void setReporte(ReporteSeguridad reporte) { this.reporte = reporte; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String value) { imagenUrl = value; }
    public String getImagenPublicId() { return imagenPublicId; }
    public void setImagenPublicId(String value) { imagenPublicId = value; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
