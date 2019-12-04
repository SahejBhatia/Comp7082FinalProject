package com.example.comp7082.comp7082photogallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.comp7082.comp7082photogallery.util.Constants;

public class EditContentActivity extends AppCompatActivity {

    private EditText userEditText;
    private TextView contentPromptTextView;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_edit_content);

        userEditText = findViewById( R.id.contentDetailsEditText);
        contentPromptTextView = findViewById(R.id.contentPromptTextView);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics( dm );

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout( (int)(width *.8),(int)(height* .4) );

        intent = getIntent();
        String currentData = intent.getStringExtra( Constants.EXTRA_IMAGE_DATA );
        String contentTitle = intent.getStringExtra( "Title" );
        String contentPrompt = intent.getStringExtra( "Prompt" );
        String contentHint = intent.getStringExtra( "UserHint" );

        contentPromptTextView.setText(contentPrompt);
        userEditText.setText(currentData);
        userEditText.setHint(contentHint);
        this.setTitle(contentTitle);


    }

    public void SetTag(View view) {

        if(userEditText.getText().toString().length() == 0 ){

            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Please " + this.getTitle().toString().toLowerCase());
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }else{
        //sending back userEditText

            intent.putExtra(Constants.EXTRA_IMAGE_DATA, userEditText.getText().toString());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public void CancelTag(View view) {
        super.onBackPressed();
    }
}
