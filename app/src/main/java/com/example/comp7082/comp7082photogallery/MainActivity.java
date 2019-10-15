package com.example.comp7082.comp7082photogallery;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements GestureDetector.OnGestureListener,
        LocationListener {
    // Tag names for Intent Extra Info
    public static final String EXTRA_CURRENT_INDEX = "com.example.comp7082.comp7082photogallery.CURRENT_INDEX";
    public static final String EXTRA_KEYWORDS_TAG = "com.example.comp7082.comp7082photogallery.KEYWORDS_TAG";
    public static final String EXTRA_PHOTO_LIST = "com.example.comp7082.comp7082photogallery.PHOTO_LIST";

    private static final float MIN_FLING_DISTANCE = 200.0f;
    private static final float MAX_FLING_DISTANCE = 1000.0f;
    public static final int NAVIGATE_RIGHT = 1;
    public static final int NAVIGATE_LEFT = -1;


    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_IMAGE_SEARCH = 2;
    static final int REQUEST_SET_TAG = 3;

    public String currentPhotoPath;
    private TextView imageIndexTextView;
    public ImageView imageView;
    public Bitmap bitmap;
    public int currentIndex = 0;
    public String directory = Environment.getExternalStorageDirectory() + "/Android/data/com.example.comp7082.comp7082photogallery/files/Pictures/";
    public String[] filenames;

    private GestureDetector gestureScanner;
    private LocationManager locationManager;
    //private Location cachedLocation;

//    private Random rand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageIndexTextView = findViewById(R.id.imageIndexTextView);

        gestureScanner = new GestureDetector(getBaseContext(), this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        imageView = findViewById(R.id.imageView);

        getFilenames(directory);
        if (filenames != null && filenames.length > 0) {
            currentPhotoPath = getCurrentFilePath();
        }


//        rand = new Random();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableLocationUpdates();

    }

    @Override
    protected void onPause() {
        super.onPause();
        disableLocationUpdates();
    }

    public void onClickCaption(View view){
        toggleCaptionEditVisibility(View.VISIBLE);

        // populate caption from file
        EditText text1 = (EditText)findViewById(R.id.edit_text1);
        File myFile = new File(currentPhotoPath);
        text1.setText(ExifUtility.getExifTagString(myFile, ExifUtility.EXIF_CAPTION_TAG));

//        Button saveButton = (Button)findViewById(R.id.button_save_id);
//        saveButton.setVisibility(View.VISIBLE);
//        text1.setVisibility(View.VISIBLE);
    }

    public void saveCaption(View view){
        String comment = ((EditText) findViewById(R.id.edit_text1)).getText().toString();

        hideSoftKeyboard();
        if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
            File myFile = new File(currentPhotoPath);
            ExifUtility.setExifTagString(myFile, ExifUtility.EXIF_CAPTION_TAG, comment);

            CharSequence text = ExifUtility.getExifTagString(myFile, ExifUtility.EXIF_CAPTION_TAG);
            ((TextView) findViewById(R.id.currentImageCaptionTextView)).setText(text);
        }
        else {
            // no current photo to caption - clear text
            ((TextView) findViewById(R.id.edit_text1)).setText("");
        }
        toggleCaptionEditVisibility(View.INVISIBLE);
//        int duration = Toast.LENGTH_SHORT;
//
//        Toast toast = Toast.makeText(context, text, duration);
//        toast.show();
    }

    private void toggleCaptionEditVisibility(int viewVisibility) {
        Button saveButton = (Button)findViewById(R.id.button_save_id);
        saveButton.setVisibility(viewVisibility);
        EditText text1 = (EditText)findViewById(R.id.edit_text1);
        text1.setVisibility(viewVisibility);
        if (viewVisibility != View.VISIBLE) {
            text1.setText("");  // ensure to clear it out
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

    private String getCurrentFilePath() {
        return directory + filenames[currentIndex];
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

        toggleCaptionEditVisibility(View.INVISIBLE);
        //enableLocationUpdates();    // begin scanning for location upon taking a photo
        Log.d("onSnapClicked", "Begin capturing a photo");
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
            //disableLocationUpdates();   // end scanning for location once photo is saved

            // update gallery list
            getFilenames(directory);
            currentIndex = filenames.length - 1;
            getPhotoLocation();
            Log.d("onActivityResult", "Finished request image capture");
        }

        if (requestCode == REQUEST_IMAGE_SEARCH && resultCode == RESULT_OK) {
            filenames = data.getStringArrayExtra(MainActivity.EXTRA_PHOTO_LIST);

            if (filenames == null) {
                getFilenames(directory);
            }
            currentIndex = 0;

            currentPhotoPath = getCurrentFilePath();
            createPicture(currentPhotoPath);
            imageView.setImageBitmap(bitmap);
        }

        if (requestCode == REQUEST_SET_TAG && resultCode == RESULT_OK){

            System.out.println("back from tag activity");
            System.out.println(data.getExtras().getString( EXTRA_KEYWORDS_TAG ));

            Log.d("onActivityResult", "dataExtra: " + data.getExtras().getString( EXTRA_KEYWORDS_TAG ));
            File currentFile = new File(currentPhotoPath);
            ExifUtility.setExifTagString(currentFile, ExifUtility.EXIF_KEYWORDS_TAG, data.getExtras().getString( EXTRA_KEYWORDS_TAG ));
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

        // retrieve the caption for the new image
        getCaptionFromImageFile(currentPhotoPath);
        updateImageIndexText();
    }

    private void getCaptionFromImageFile(String photoPath) {
        // retrieve the caption for the new image
        File currentFile = new File(photoPath);
        String currentFileCaption = ExifUtility.getExifTagString(currentFile, ExifUtility.EXIF_CAPTION_TAG);
        ((TextView)findViewById(R.id.currentImageCaptionTextView)).setText((currentFileCaption == null ? "" : currentFileCaption));
    }

    private void updateImageIndexText() {

        StringBuilder sb = new StringBuilder();
        sb.append(currentIndex+1);
        if (filenames.length > 0) {
            sb.append(" of ");
            sb.append(filenames.length);
        }
        imageIndexTextView.setText(sb.toString());
    }

    // Search methods
    public void openSearchOnClick(View view){
        toggleCaptionEditVisibility(View.INVISIBLE);

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
                scrollGallery(NAVIGATE_RIGHT); // scroll right
            }
            else {
                // right swipe - so scrolling to the left
                Log.d("Fling, SWIPE RIGHT","!");
                scrollGallery(NAVIGATE_LEFT);  // scroll left
            }
        }
        return true;
    }

    // direction parameter should be an enum
    private void scrollGallery(int direction) {
        switch (direction) {
            case NAVIGATE_LEFT:     // left
                Log.d("scrollGallery :", "Scroll Left");
                --currentIndex;
                break;
            case NAVIGATE_RIGHT:    // right
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

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        // possibly show file date and time in a Toast popup
        displayPhotoTimeStamp();
        getPhotoLocation();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    private void displayPhotoTimeStamp() {
        File currentFile = new File(currentPhotoPath);
        String photoDate = ExifUtility.getExifTagString(currentFile, ExifUtility.EXIF_DATETIME_TAG);

        Toast.makeText(this, "Date: " + photoDate, Toast.LENGTH_LONG).show();
    }

    private void getPhotoLocation() {

        float location[] = {0.0f, 0.0f} ;   // lat, long

        File currentFile = new File(currentPhotoPath);
        if (ExifUtility.getExifLatLong(currentFile, location)) {
            String city = "";
            float latitude = location[0];
            float longitude = location[1];
            Log.d("getPhotoLocation", "File location: lat: " + latitude + " long: " + longitude);

            Geocoder geo = new Geocoder(this);
            try {
                List<Address> addressList = geo.getFromLocation(latitude, longitude, 1);
                for (Address addr : addressList) {
                    city = addr.getLocality();
                    Log.d("getPhotoLocation", "addr: " + addr.getLocality());
                }
            } catch (IOException e) {
                Log.d("getPhotoLocation", "geo IOException " + e.getMessage());
            }
            Toast.makeText(this,
                    "Location: lat: " + latitude + " long: " + longitude + (city.isEmpty() ? "" : "\n" + city),
                    Toast.LENGTH_LONG).show();
        }
        else {
            Log.d("getPhotoLocation", "File location: not retrieved");
        }

    }

    public void addTag(View view) {

        //check if image was taken
        //allow tags only if image is taken
        //else if show message
        //Toast.makeText( this, "photo path" + currentIndex, Toast.LENGTH_SHORT ).show();
        //System.out.println(filenames[currentIndex]);

        if(filenames != null) {

            //send current image's name to next activity

            Intent tagIntent = new Intent( MainActivity.this, addTag.class );
            tagIntent.putExtra( "FileName", filenames[currentIndex] );
            File currentFile = new File(currentPhotoPath);

            tagIntent.putExtra( EXTRA_KEYWORDS_TAG, ExifUtility.getExifTagString(currentFile, ExifUtility.EXIF_KEYWORDS_TAG) );
            startActivityForResult( tagIntent , REQUEST_SET_TAG );
        } else {
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

    /*
     * hide the soft keyboard if it is displayed
     */
    private void hideSoftKeyboard() {
        // Check if no view has focus:
        View mview = this.getCurrentFocus();
        if (mview != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mview.getWindowToken(), 0);
        }
    }

    /*
            LocationListener Interface Implementations
     */

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged","@@@ Location: lat[" + location.getLatitude()+ "] long[" + location.getLongitude()+ "]");

        // experimental: get the location name from a gps location
        Geocoder geo = new Geocoder(this);
        String city= "Vancouver BC";
        try {
            List<Address> addressList = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            for (Address addr : addressList) {
                Log.d("onLocationChanged", "addr1: " + addr.getLocality());
                city = addr.getLocality();
            }

            // experimental: get the gps location from a location name
            city= "10 downing st london";
            if (city == null) {
                Log.d("onLocationChanged", "TESTNOW: " + (city == null ? "is null" : city));

            }
            else {
                Log.d("onLocationChanged", "TESTNOW: " + (city == null ? "is null" : city));
                addressList = geo.getFromLocationName(city, 4);
                for (Address addr : addressList) {
                    Log.d("onLocationChanged", "By locationname: " +
                            addr.getCountryName() + "\n" +
                            addr.getLocality() + "\n" +
                            addr.getSubLocality() + "\n" +
                            addr.getThoroughfare() + "\n" +
                            addr.getSubThoroughfare() + "\n" +
                            addr.getPostalCode() + "\n\n" +
                            addr.getLatitude() + " " + addr.getLongitude()
                    );
                }
            }
        } catch (IOException e) {
            Log.d("onLocationChanged", "geo IOException " + e.getMessage());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void enableLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, this);
            //cachedLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.d("enableLocationUpdates","Begin accepting location updates");
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            Log.d("enableLocationUpdates","Request FINE location permission");
        }
    }

    private void disableLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            locationManager.removeUpdates(this);
            Log.d("disableLocationUpdates","End accepting location updates");
        }
    }

}
