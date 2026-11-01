package api_app_mapas.lugares.dto;

import api_app_mapas.lugares.model.CategoriaLugar;

public record CategoriaLugarResponse(String codigo, String nombre) {
    public static CategoriaLugarResponse desde(CategoriaLugar categoria) {
        return new CategoriaLugarResponse(categoria.getCodigo(), categoria.getNombre());
    }
}
