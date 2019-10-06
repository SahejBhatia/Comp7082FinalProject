package com.example.comp7082.comp7082photogallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity
    implements GestureDetector.OnGestureListener
{
    // Tag names for Intent Extra Info
    public static final String EXTRA_PHOTO_LIST = "com.example.comp7082.comp7082photogallery.PHOTO_LIST";
    public static final String EXTRA_CURRENT_INDEX = "com.example.comp7082.comp7082photogallery.CURRENT_INDEX";

    private static final float MIN_FLING_DISTANCE = 200.0f;
    private static final float MAX_FLING_DISTANCE = 1000.0f;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_IMAGE_SEARCH = 2;
    static final int REQUEST_SET_TAG = 3;

    public String currentPhotoPath;
    public ImageView imageView;
    public Bitmap bitmap;
    public int currentIndex = 0;
    public String directory = Environment.getExternalStorageDirectory() + "/Android/data/com.example.comp7082.comp7082photogallery/files/Pictures/";
    public String[] filenames;

    private GestureDetector gestureScanner;
    private Random rand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gestureScanner = new GestureDetector(getBaseContext(), this);
        imageView = findViewById(R.id.imageView);

        getFilenames(directory);
        if(filenames != null && filenames.length > 0) {
            currentPhotoPath = directory + filenames[currentIndex];
        }

        rand = new Random();
    }

    public void onClickeCapition(View view){
        Button saveButton = (Button)findViewById(R.id.button_save_id);
        saveButton.setVisibility(view.VISIBLE);
        EditText text1 = (EditText)findViewById(R.id.edit_text1);
        text1.setVisibility(view.VISIBLE);
    }

    public void saveCaption(View view){
        File path = new File(directory);
        if (path.exists()) {
            filenames = path.list();
        }
        /*File myFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getPackageName());*/

        String comment = ((EditText) findViewById(R.id.edit_text1)).getText().toString();
        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        //File exifVar =  new File(directory.getPath(), "IMG_" + timeStamp + ".jpg");
        File myFile = new File(currentPhotoPath);
        Uri uri;
        InputStream in;
        try{
            //in = getContentResolver().openInputStream(uri);
            ExifInterface exif = new ExifInterface(myFile.getCanonicalPath());
            //exif.readExif(exifVar.getAbsolutePath());
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, comment);
            exif.saveAttributes();


            // Context context = getApplicationContext();
            CharSequence text = exif.getAttribute(ExifInterface.TAG_USER_COMMENT);

            ((TextView) findViewById(R.id.text_view_id23)).setText(text);
//            int duration = Toast.LENGTH_SHORT;
//
//            Toast toast = Toast.makeText(context, text, duration);
//            toast.show();
        }catch(Exception e){

        }

    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if(currentPhotoPath != null){
                createPicture(currentPhotoPath);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private void getFilenames(String directory){
        File path = new File(directory);
        if (path.exists()) {
            filenames = path.list();
            Log.d("getFileNames", "filenames length = " + filenames.length);
        }
    }

    public void onSnapClicked(View view){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this,
                        "Photo file can't be created, please try again",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.comp7082.comp7082photogallery.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            createPicture(currentPhotoPath);
            imageView.setImageBitmap(bitmap);

            // update gallery list
            getFilenames(directory);
            currentIndex = filenames.length - 1;

            // exif data test
            // search development use - needs to be removed once tag functionality is in place
            try {
                String mString = getCommentTags();
                ExifInterface exif;
                exif = new ExifInterface(currentPhotoPath);
                exif.setAttribute("UserComment", mString); // or "ImageDescription"
                exif.setAttribute("ImageDescription", filenames[currentIndex]); // or "ImageDescription"
                exif.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // end test

        }
        if (requestCode == REQUEST_IMAGE_SEARCH && resultCode == RESULT_OK) {
            filenames = data.getStringArrayExtra(MainActivity.EXTRA_PHOTO_LIST);

            if (filenames == null) {
                getFilenames(directory);
            }
            currentIndex = 0;

            currentPhotoPath = directory + filenames[currentIndex];
            createPicture(currentPhotoPath);
            imageView.setImageBitmap(bitmap);

        }


        if (requestCode ==REQUEST_SET_TAG && resultCode == RESULT_OK){

            System.out.println("back from tagg ctivity");
            System.out.println(data.getExtras().getString( "tag" ));

            try{

                ExifInterface exif_tag;
                exif_tag = new ExifInterface(currentPhotoPath);
                exif_tag.setAttribute("TAG_MAKER_NOTE", data.getExtras().getString( "tag" )); // or "ImageDescription"
                exif_tag.saveAttributes();

            }catch(IOException io){
                io.printStackTrace();
            }

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void createPicture(String filepath) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        bitmap = BitmapFactory.decodeFile(filepath, bmOptions);
    }

    // Search methods
    public void openSearchOnClick(View view){
        Intent intent = new Intent(this, SearchActivity.class);
        getFilenames(directory);    // ensure we send the whole list each time
        intent.putExtra(EXTRA_PHOTO_LIST, filenames);
        intent.putExtra(EXTRA_CURRENT_INDEX, currentIndex);
        startActivityForResult(intent, REQUEST_IMAGE_SEARCH);

    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        return gestureScanner.onTouchEvent(me);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float v, float v1) {

        // Get swipe delta value in x axis.
        float deltaX = e1.getX() - e2.getX();

        // Get swipe delta value in y axis.
        float deltaY = e1.getY() - e2.getY();

        // Get absolute value.
        float deltaXAbs = Math.abs(deltaX);
        float deltaYAbs = Math.abs(deltaY);

        Log.d("Fling, deltaX = ", Float.toString(deltaX));
        Log.d("Fling, deltaY = ", Float.toString(deltaY));
        Log.d("Fling, deltaXAbs = ", Float.toString(deltaXAbs));
        Log.d("Fling, deltaYAbs = ", Float.toString(deltaYAbs));
        if ((deltaXAbs >= MIN_FLING_DISTANCE) && (deltaXAbs <= MAX_FLING_DISTANCE)) {
            if (deltaX > 0) {
                // left swipe - so scrolling to the right
                Log.d("Fling, SWIPE LEFT","!");
                scrollGallery(1); // scroll right
            }
            else {
                // right swipe - so scrolling to the left
                Log.d("Fling, SWIPE RIGHT","!");
                scrollGallery(-1);  // scroll left
            }
        }
        return true;
    }

    // direction parameter should be an enum
    private void scrollGallery(int direction) {
        switch (direction) {
            case -1:    // left
                Log.d("scrollGallery :", "Scroll Left");
                --currentIndex;
                break;
            case 1:     // right
                Log.d("scrollGallery :", "Scroll Right");
                ++currentIndex;
                break;
            default:
                break;
        }

        // stay in bounds
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        if (filenames.length > 0 && currentIndex >= filenames.length) {
            currentIndex = filenames.length - 1;
        }

        // update the gallery image
        currentPhotoPath = directory + filenames[currentIndex];
        Log.d("scrollGallery :", "currentIndex = " + currentIndex + " filenames.length = " + filenames.length);
        Log.d("scrollGallery :", "currentPhotoPath = " + currentPhotoPath);
        createPicture(currentPhotoPath);
        imageView.setImageBitmap(bitmap);
    }

    // development method only
    // search development use - needs to be removed once tag functionality is in place
    private String getCommentTags() {
        String[] words = { "stove", "sink", "dog", "books", "kitchen", "dishwasher", "table", "chairs", "tv"};
        String tags = "";
        int stop = rand.nextInt(3) + 1;

        for (int i = 0; i < stop ; i ++) {
            tags += words[rand.nextInt(words.length)];
            if (i < stop -1) {
                tags += " ";
            }
        }
        return tags;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    public void addTag(View view) {

        //check if image was taken
        //allow tags only if image is taken
        //else if show message
        //Toast.makeText( this, "photo path" + currentIndex, Toast.LENGTH_SHORT ).show();


        //System.out.println(filenames[currentIndex]);

        if(filenames != null){

            //send current image's name to next activity

            Intent tagIntent = new Intent( MainActivity.this, addTag.class );
            tagIntent.putExtra( "FileName", filenames[currentIndex] );
            startActivityForResult( tagIntent , REQUEST_SET_TAG );
        }else{


            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Please take a pic first.");
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
        }



    }
}
