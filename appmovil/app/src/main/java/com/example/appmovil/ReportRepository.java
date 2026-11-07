package com.example.appmovil;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

final class ReportRepository {
    interface Callback<T>{ void success(T value); void error(String message); }
    static void comments(long id,int page, Callback<JSONObject> callback){
        ApiClient.getAuthorized(ApiConfig.REPORTS_BASE_URL+"/"+id+"/comentarios/pagina?pagina="+page+"&limite=5","",new ApiClient.ApiCallback(){
            public void onSuccess(String response){try{callback.success(new JSONObject(response));}catch(Exception e){callback.error("No se pudieron leer los comentarios.");}}
            public void onError(String error){callback.error(error);}
        });
    }
    static void comment(android.content.Context context,long id,String text,Callback<JSONObject> callback){
        try{ApiClient.postAuthorized(ApiConfig.REPORTS_BASE_URL+"/"+id+"/comentarios",new SessionManager(context).getToken(),new JSONObject().put("texto",text),bridge(callback));}catch(Exception e){callback.error("Comentario no válido.");}
    }
    static void commentWithImage(android.content.Context context,long id,String text,byte[] image,String mime,ApiClient.ApiCallback callback){
        ApiClient.uploadReportComment(ApiConfig.REPORTS_BASE_URL+"/"+id+"/comentarios/con-imagen",
                new SessionManager(context).getToken(),text,image,mime,callback);
    }
    static void vote(android.content.Context context,long id,String truth,Callback<JSONObject> callback){
        try{ApiClient.putAuthorized(ApiConfig.REPORTS_BASE_URL+"/"+id+"/voto",new SessionManager(context).getToken(),new JSONObject().put("veracidad",truth),bridge(callback));}catch(Exception e){callback.error("No se pudo registrar el voto.");}
    }
    static void flag(android.content.Context context,long id,String reason,Callback<JSONObject> callback){
        try{ApiClient.postAuthorized(ApiConfig.REPORTS_BASE_URL+"/"+id+"/reportar",new SessionManager(context).getToken(),new JSONObject().put("texto",reason),bridge(callback));}catch(Exception e){callback.error("No se pudo enviar el reporte.");}
    }
    static void deleteOwn(android.content.Context context,long id,ApiClient.ApiCallback callback){
        ApiClient.deleteAuthorized(ApiConfig.REPORTS_BASE_URL+"/"+id,new SessionManager(context).getToken(),callback);
    }
    static void deleteAsAdmin(android.content.Context context,long id,ApiClient.ApiCallback callback){
        ApiClient.deleteAuthorized(ApiConfig.ADMIN_BASE_URL+"/reportes/"+id,new SessionManager(context).getToken(),callback);
    }
    private static ApiClient.ApiCallback bridge(Callback<JSONObject> callback){return new ApiClient.ApiCallback(){public void onSuccess(String response){try{callback.success(response.isEmpty()?new JSONObject():new JSONObject(response));}catch(Exception e){callback.error("Respuesta no válida.");}}public void onError(String error){callback.error(error);}};}
}
