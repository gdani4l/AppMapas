package api_app_mapas.lugares.dto;

import api_app_mapas.lugares.model.ResenaLugar;
import api_app_mapas.usuario.model.Usuario;

public record FotoLugarResponse(String imagenUrl, String nombreUsuario, String imagenPerfilUrl) {
    public static FotoLugarResponse desde(ResenaLugar resena) {
        Usuario usuario = resena.getUsuario();
        return new FotoLugarResponse(resena.getImagenUrl(),
                usuario.getPrimerNombre() + " " + usuario.getApellidoPaterno(),
                usuario.getImagenPerfilUrl());
    }
}
