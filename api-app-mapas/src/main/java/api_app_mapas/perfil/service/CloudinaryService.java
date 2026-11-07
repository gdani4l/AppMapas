package api_app_mapas.perfil.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret
    ) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public UploadResult subirImagenPerfil(byte[] contenido, Integer idUsuario) {
        Cloudinary cloudinary = cliente();

        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(
                    contenido,
                    ObjectUtils.asMap(
                            "folder", "app_mapas/perfiles",
                            "public_id", "usuario_" + idUsuario,
                            "overwrite", true,
                            "invalidate", true,
                            "resource_type", "image",
                            "transformation", "c_fill,g_face,w_512,h_512,q_auto,f_jpg"
                    )
            );
            return new UploadResult(
                    String.valueOf(resultado.get("secure_url")),
                    String.valueOf(resultado.get("public_id"))
            );
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo subir la imagen a Cloudinary",
                    e
            );
        }
    }

    public UploadResult subirImagenResena(byte[] contenido) {
        return subirImagenContenido(contenido, "resenas", "No se pudo subir la imagen de la reseña");
    }

    public UploadResult subirImagenReporte(byte[] contenido) {
        return subirImagenContenido(contenido, "reportes", "No se pudo subir la imagen del reporte");
    }

    public UploadResult subirImagenComentarioReporte(byte[] contenido) {
        return subirImagenContenido(contenido, "comentarios_reportes",
                "No se pudo subir la imagen del comentario");
    }

    private UploadResult subirImagenContenido(byte[] contenido, String carpeta, String mensaje) {
        try {
            Map<?, ?> resultado = cliente().uploader().upload(contenido, ObjectUtils.asMap(
                    "folder", "app_mapas/" + carpeta,
                    "public_id", UUID.randomUUID().toString(),
                    "resource_type", "image",
                    "transformation", "c_limit,w_1400,h_1400,q_auto"
            ));
            return new UploadResult(String.valueOf(resultado.get("secure_url")),
                    String.valueOf(resultado.get("public_id")));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, mensaje, e);
        }
    }

    public void eliminarImagen(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            cliente().uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo eliminar la imagen anterior", e);
        }
    }

    private Cloudinary cliente() {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Cloudinary todavía no está configurado en el backend");
        }
        return new Cloudinary(ObjectUtils.asMap("cloud_name", cloudName,
                "api_key", apiKey, "api_secret", apiSecret, "secure", true));
    }

    public record UploadResult(String url, String publicId) {
    }
}
