package com.example.appmovil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class PhotoViewerActivity extends AppCompatActivity {
    private ArrayList<String> urls;
    private ArrayList<String> authors;
    private ArrayList<String> avatars;
    private int index;

    static Intent intent(Context context, List<String> photos, int selected, String title) {
        return new Intent(context, PhotoViewerActivity.class)
                .putStringArrayListExtra("photos", new ArrayList<>(photos))
                .putExtra("selected", selected)
                .putExtra("title", title);
    }

    static Intent intentWithAuthors(Context context, List<ReviewRepository.Photo> photos,
                                    int selected, String title) {
        ArrayList<String> urls = new ArrayList<>();
        ArrayList<String> authors = new ArrayList<>();
        ArrayList<String> avatars = new ArrayList<>();
        for (ReviewRepository.Photo photo : photos) {
            urls.add(photo.imageUrl);
            authors.add(photo.author);
            avatars.add(photo.avatarUrl);
        }
        return intent(context, urls, selected, title)
                .putStringArrayListExtra("authors", authors)
                .putStringArrayListExtra("avatars", avatars);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        urls = getIntent().getStringArrayListExtra("photos");
        authors = getIntent().getStringArrayListExtra("authors");
        avatars = getIntent().getStringArrayListExtra("avatars");
        if (urls == null || urls.isEmpty()) { finish(); return; }
        index = Math.max(0, Math.min(getIntent().getIntExtra("selected", 0), urls.size() - 1));
        setContentView(R.layout.activity_photo_viewer);
        UiInsets.apply(this);
        ((TextView) findViewById(R.id.photoViewerTitle)).setText(
                getIntent().getStringExtra("title"));
        findViewById(R.id.btnClosePhoto).setOnClickListener(view -> finish());
        findViewById(R.id.btnPreviousPhoto).setOnClickListener(view -> {
            if (index > 0) { index--; render(); }
        });
        findViewById(R.id.btnNextPhoto).setOnClickListener(view -> {
            if (index < urls.size() - 1) { index++; render(); }
        });
        render();
    }

    private void render() {
        RemoteImageLoader.load((ImageView) findViewById(R.id.photoViewerImage), urls.get(index),
                android.R.color.transparent);
        ((TextView) findViewById(R.id.photoViewerCounter)).setText(
                (index + 1) + " / " + urls.size());
        boolean hasAuthor = authors != null && authors.size() == urls.size();
        findViewById(R.id.photoViewerAuthorRow).setVisibility(hasAuthor ? View.VISIBLE : View.GONE);
        if (hasAuthor) {
            ((TextView) findViewById(R.id.photoViewerAuthorName)).setText(authors.get(index));
            ImageView avatar = findViewById(R.id.photoViewerAuthorAvatar);
            avatar.setClipToOutline(true);
            RemoteImageLoader.load(avatar, avatars != null && avatars.size() == urls.size()
                    ? avatars.get(index) : "", R.drawable.ic_person_circle);
        }
        findViewById(R.id.btnPreviousPhoto).setEnabled(index > 0);
        findViewById(R.id.btnNextPhoto).setEnabled(index < urls.size() - 1);
    }
}
