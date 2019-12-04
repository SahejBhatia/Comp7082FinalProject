package com.example.comp7082.comp7082photogallery;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

public class GalleryPreview extends AppCompatActivity {

        ImageView GalleryPreviewImg;
        String path;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getSupportActionBar().hide();
            setContentView(R.layout.activity_gallery_preview);
            Intent intent = getIntent();
            path = intent.getStringExtra("path");
            GalleryPreviewImg = (ImageView) findViewById(R.id.GalleryPreviewImg);
            Glide.with(GalleryPreview.this)
                    .load(new File(path)) // Uri of the picture
                    .into(GalleryPreviewImg);
        }
    }

