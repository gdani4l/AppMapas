package api_app_mapas.reportes.model;

import api_app_mapas.usuario.model.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "votos_reporte")
@IdClass(VotoReporteId.class)
public class VotoReporte {
    @Id @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reporte") private ReporteSeguridad reporte;
    @Id @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario") private Usuario usuario;
    @Column(nullable = false, length = 10) private String veracidad;
    @Column(name = "fecha_creacion", insertable = false, updatable = false) private LocalDateTime fechaCreacion;
    public ReporteSeguridad getReporte() { return reporte; }
    public void setReporte(ReporteSeguridad reporte) { this.reporte = reporte; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getVeracidad() { return veracidad; }
    public void setVeracidad(String veracidad) { this.veracidad = veracidad; }
}
