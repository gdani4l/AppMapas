package api_app_mapas.reportes.model;

import java.io.Serializable;
import java.util.Objects;

public class VotoReporteId implements Serializable {
    private Long reporte;
    private Integer usuario;
    public VotoReporteId() {}
    public VotoReporteId(Long reporte, Integer usuario) { this.reporte = reporte; this.usuario = usuario; }
    @Override public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof VotoReporteId other)) return false;
        return Objects.equals(reporte, other.reporte) && Objects.equals(usuario, other.usuario);
    }
    @Override public int hashCode() { return Objects.hash(reporte, usuario); }
}
