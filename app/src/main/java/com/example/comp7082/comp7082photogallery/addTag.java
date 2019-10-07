package com.example.comp7082.comp7082photogallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

public class addTag extends AppCompatActivity {

    private String fileName;
    private EditText tag;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_add_tag );

        tag= findViewById( R.id.editText );

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics( dm );

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout( (int)(width *.8),(int)(height* .4) );

        intent = getIntent();
//        fileName= intent.getStringExtra( "FileName" );
//        System.out.println("from the second activity "+fileName);
        String givenTags = intent.getStringExtra( MainActivity.EXTRA_KEYWORDS_TAG );
        tag.setText(givenTags);


    }

    public void SetTag(View view) {

        if(tag.getText().toString().length() == 0 ){

            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Please enter a tag.");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }else{
        //sending back tag

            intent.putExtra(MainActivity.EXTRA_KEYWORDS_TAG, tag.getText().toString());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public void CancelTag(View view) {
        super.onBackPressed();
    }
}
