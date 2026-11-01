package api_app_mapas.lugares.model;

import api_app_mapas.usuario.model.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resenas_lugar", uniqueConstraints = @UniqueConstraint(
        name = "uq_resena_lugar_usuario", columnNames = {"id_lugar", "id_usuario"}))
public class ResenaLugar {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resena")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lugar", nullable = false)
    private Lugar lugar;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "estrellas", nullable = false)
    private int estrellas;

    @Column(name = "comentario", length = 2000)
    private String comentario;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "imagen_public_id", length = 255)
    private String imagenPublicId;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
    private LocalDateTime fechaActualizacion;

    public Long getId() { return id; }
    public Lugar getLugar() { return lugar; }
    public void setLugar(Lugar value) { lugar = value; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario value) { usuario = value; }
    public int getEstrellas() { return estrellas; }
    public void setEstrellas(int value) { estrellas = value; }
    public String getComentario() { return comentario; }
    public void setComentario(String value) { comentario = value; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String value) { imagenUrl = value; }
    public String getImagenPublicId() { return imagenPublicId; }
    public void setImagenPublicId(String value) { imagenPublicId = value; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
}
