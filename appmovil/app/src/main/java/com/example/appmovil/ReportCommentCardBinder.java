package com.example.appmovil;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.Collections;

final class ReportCommentCardBinder {
    private ReportCommentCardBinder() { }

    static View create(Activity activity, ViewGroup parent, JSONObject json) {
        View card=activity.getLayoutInflater().inflate(R.layout.view_report_comment,parent,false);
        String author=json.optString("autor","Usuario");
        String date=json.optString("fechaCreacion","");
        String imageUrl=nullable(json,"imagenUrl");
        String avatarUrl=nullable(json,"imagenAutor");
        ((TextView)card.findViewById(R.id.reportCommentAuthor)).setText(author);
        ((TextView)card.findViewById(R.id.reportCommentDate)).setText(
                date.length()>=16?date.substring(0,16).replace('T',' '):date);
        ((TextView)card.findViewById(R.id.reportCommentText)).setText(json.optString("comentario"));
        ImageView avatar=card.findViewById(R.id.reportCommentAvatar);avatar.setClipToOutline(true);
        RemoteImageLoader.load(avatar,avatarUrl,R.drawable.ic_person_circle);
        if(!imageUrl.isEmpty()){
            ImageView image=card.findViewById(R.id.reportCommentImage);image.setVisibility(View.VISIBLE);
            RemoteImageLoader.load(image,imageUrl,R.drawable.bg_input);
            image.setOnClickListener(v->activity.startActivity(PhotoViewerActivity.intent(
                    activity, Collections.singletonList(imageUrl),0,author)));
        }
        return card;
    }

    private static String nullable(JSONObject json,String key){
        return json.isNull(key)?"":json.optString(key,"");
    }
}
