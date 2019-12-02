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

import com.example.comp7082.comp7082photogallery.androidos.ExifUtility;
import com.example.comp7082.comp7082photogallery.androidos.PhotoFileManager;
import com.example.comp7082.comp7082photogallery.util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GestureDetector.OnGestureListener,
        LocationListener {

    private String TAG = MainActivity.class.getSimpleName();

    public String currentPhotoPath;
    private TextView imageIndexTextView;
    public ImageView imageView;
    public Bitmap bitmap;
    public String[] filenames;

    private GestureDetector gestureScanner;
    private LocationManager locationManager;
    private PhotoFileManager photoFileManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageIndexTextView = findViewById(R.id.imageIndexTextView);
        imageView = findViewById(R.id.imageView);

        gestureScanner = new GestureDetector(getBaseContext(), this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        photoFileManager = new PhotoFileManager();
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
        //File myFile = new File(currentPhotoPath);
        String caption = photoFileManager.getPhotoCaption(photoFileManager.getCurrentPhotoFile());
        text1.setText(caption);

//        Button saveButton = (Button)findViewById(R.id.button_save_id);
//        saveButton.setVisibility(View.VISIBLE);
//        text1.setVisibility(View.VISIBLE);
    }

    public void saveCaption(View view){
        String comment = ((EditText) findViewById(R.id.edit_text1)).getText().toString();

        hideSoftKeyboard();
        if (photoFileManager.getCurrentPhotoFile() != null && !photoFileManager.getCurrentPhotoFile().isEmpty()) {
            photoFileManager.setPhotoCaption(photoFileManager.getCurrentPhotoFile(), comment);

            String savedComment = photoFileManager.getPhotoCaption(photoFileManager.getCurrentPhotoFile());
            ((TextView) findViewById(R.id.currentImageCaptionTextView)).setText(savedComment);
        }
        else {
            // no current photo to caption - clear text
            ((TextView) findViewById(R.id.edit_text1)).setText("");
        }
        toggleCaptionEditVisibility(View.INVISIBLE);
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
            if(photoFileManager.getCurrentPhotoFile() != null){
                updateImageView(photoFileManager.getCurrentPhotoFile());
            }
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
                photoFile = photoFileManager.createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this,
                        "Photo file can't be created, please try again",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        Constants.ROOT_PACKAGE_NAME + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, Constants.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: request["+requestCode+"] result["+resultCode+"]");
        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            handleImageRequestResult();
        }

        if (requestCode == Constants.REQUEST_IMAGE_SEARCH && resultCode == RESULT_OK) {
            handleSearchRequestResult(data);
        }

        if (requestCode == Constants.REQUEST_SET_TAG && resultCode == RESULT_OK){
            handlePhotoTagResult(data);
        }
    }

    private void handleImageRequestResult() {
        //disableLocationUpdates();   // end scanning for location once photo is saved
        Log.d(TAG, "handleImageRequestResult: begin");
        updateImageView(photoFileManager.getCurrentPhotoFile());

        // update gallery list
        photoFileManager.setPhotoList(
                photoFileManager.getFilenames(photoFileManager.getPhotoLocation()),
                PhotoFileManager.LAST_ITEM);
        //getPhotoLocation();
        Log.d(TAG, "handleImageRequestResult: Finished request image capture");
    }

    private void handleSearchRequestResult(Intent data) {
        String[] filenames = data.getStringArrayExtra(Constants.EXTRA_PHOTO_LIST);

        // search not found, return to full image list
        if (filenames == null) {
            photoFileManager.setPhotoList(
                    photoFileManager.getFilenames(photoFileManager.getPhotoLocation()),
                    PhotoFileManager.FIRST_ITEM);
        }

        updateImageView(photoFileManager.getCurrentPhotoFile());
    }

    private void handlePhotoTagResult(Intent data) {
        String imageKeywords = data.getExtras().getString(Constants.EXTRA_KEYWORDS_TAG );
        Log.d(TAG, "handlePhotoTagResult: data: " + imageKeywords);

        photoFileManager.setPhotoKeyWords(photoFileManager.getCurrentPhotoFile(), imageKeywords);
    }

    private void updateImageView(String imageFilename) {
        createPicture(imageFilename); // set instance bitmap
        imageView.setImageBitmap(bitmap);

        // retrieve the caption for the new image
        getCaptionFromImageFile(imageFilename);
        updateImageIndexText();
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

    private void getCaptionFromImageFile(String photoPath) {
        // retrieve the caption for the new image
        String currentFileCaption = photoFileManager.getPhotoCaption(photoFileManager.getCurrentPhotoFile());
        ((TextView)findViewById(R.id.currentImageCaptionTextView)).setText((currentFileCaption == null ? "" : currentFileCaption));
    }

    private void updateImageIndexText() {

        StringBuilder sb = new StringBuilder();
        sb.append(photoFileManager.getCurrentPhotoIndex()+1);
        if (photoFileManager.getPhotoList().length > 0) {
            sb.append(" of ");
            sb.append(photoFileManager.getPhotoList().length);
        }
        imageIndexTextView.setText(sb.toString());
    }

    // Search methods
    public void openSearchOnClick(View view){
        toggleCaptionEditVisibility(View.INVISIBLE);

        Intent intent = new Intent(this, SearchActivity.class);
        // ensure we send the whole list each time
        photoFileManager.setPhotoList(
                photoFileManager.getFilenames(photoFileManager.getPhotoLocation()),
                PhotoFileManager.SAME_ITEM);

        intent.putExtra(Constants.EXTRA_PHOTO_LIST, photoFileManager.getFilenames());
        intent.putExtra(Constants.EXTRA_CURRENT_INDEX, photoFileManager.getCurrentPhotoIndex());
        startActivityForResult(intent, Constants.REQUEST_IMAGE_SEARCH);
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
        if ((deltaXAbs >= Constants.MIN_FLING_DISTANCE) && (deltaXAbs <= Constants.MAX_FLING_DISTANCE)) {
            if (deltaX > 0) {
                // left swipe - so scrolling to the right
                Log.d("Fling, SWIPE LEFT","!");
                scrollGallery(Constants.NAVIGATE_RIGHT); // scroll right
            }
            else {
                // right swipe - so scrolling to the left
                Log.d("Fling, SWIPE RIGHT","!");
                scrollGallery(Constants.NAVIGATE_LEFT);  // scroll left
            }
        }
        return true;
    }

    // direction parameter should be an enum
    private void scrollGallery(int direction) {
        int currentIndex = photoFileManager.getCurrentPhotoIndex();

        switch (direction) {
            case Constants.NAVIGATE_LEFT:     // left
                Log.d("scrollGallery :", "Scroll Left");
                --currentIndex;
                break;
            case Constants.NAVIGATE_RIGHT:    // right
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

        // will handle upper bound
        photoFileManager.setCurrentPhotoIndex(currentIndex);
        photoFileManager.setCurrentPhotoFile(photoFileManager.getCurrentPhotoIndex());

        updateImageView(photoFileManager.getCurrentPhotoFile());
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
        String photoDate = photoFileManager.getPhotoTimeStamp(photoFileManager.getCurrentPhotoFile());
        Toast.makeText(this, "Date: " + photoDate, Toast.LENGTH_LONG).show();
    }

    private void getPhotoLocation() {

//        float location[] = {0.0f, 0.0f} ;   // lat, long
//
//        File currentFile = new File(currentPhotoPath);
//        if (ExifUtility.getExifLatLong(currentFile, location)) {
//            String city = "";
//            float latitude = location[0];
//            float longitude = location[1];
//            Log.d("getPhotoLocation", "File location: lat: " + latitude + " long: " + longitude);
//
//            Geocoder geo = new Geocoder(this);
//            try {
//                List<Address> addressList = geo.getFromLocation(latitude, longitude, 1);
//                for (Address addr : addressList) {
//                    city = addr.getLocality();
//                    Log.d("getPhotoLocation", "addr: " + addr.getLocality());
//                }
//            } catch (IOException e) {
//                Log.d("getPhotoLocation", "geo IOException " + e.getMessage());
//            }
//            Toast.makeText(this,
//                    "Location: lat: " + latitude + " long: " + longitude + (city.isEmpty() ? "" : "\n" + city),
//                    Toast.LENGTH_LONG).show();
//        }
//        else {
//            Log.d("getPhotoLocation", "File location: not retrieved");
//        }

    }

    public void addTag(View view) {

        if(photoFileManager.getPhotoList() != null) {
            Intent tagIntent = new Intent( MainActivity.this, addTag.class );
            String imageKeyWords = photoFileManager.getPhotoKeyWords(photoFileManager.getCurrentPhotoFile());

            tagIntent.putExtra(Constants.EXTRA_KEYWORDS_TAG, imageKeyWords );
            startActivityForResult( tagIntent , Constants.REQUEST_SET_TAG );
        } else {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Please take a pic first.");
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

    public PhotoFileManager getPhotoFileManagerForTest() {
        return  photoFileManager;
    }

}
