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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comp7082.comp7082photogallery.androidos.PhotoFileManager;
import com.example.comp7082.comp7082photogallery.util.Constants;
import com.example.comp7082.comp7082photogallery.util.PhotoSearch;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements GestureDetector.OnGestureListener,
        LocationListener {

    private String TAG = MainActivity.class.getSimpleName();

    private TextView imageIndexTextView;
    public ImageView imageView;
    public Bitmap bitmap;

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

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if(photoFileManager.getCurrentPhotoFile() != null){
                updateImageView(photoFileManager.getCurrentPhotoFile());
            }
        }
    }

    public void onClickSnap(View view){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //enableLocationUpdates();    // begin scanning for location upon taking a photo
        Log.d("onClickSnap", "Begin capturing a photo");
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

    public void onClickCaption(View view){
        if(photoFileManager.getPhotoList() != null) {
            Intent tagIntent = new Intent( MainActivity.this, EditContentActivity.class );
            String imageCaption = photoFileManager.getPhotoCaption(photoFileManager.getCurrentPhotoFile());

            tagIntent.putExtra(Constants.EXTRA_IMAGE_DATA, imageCaption );
            tagIntent.putExtra("Title", getString(R.string.contentCaptionTitle ));
            tagIntent.putExtra("Prompt", getString(R.string.contentPromptCaptionDescription) );
            tagIntent.putExtra("UserHint", getString(R.string.contentDetailsCaptionHintText) );
            startActivityForResult( tagIntent , Constants.REQUEST_SET_CAPTION );

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

    public void onClickTags(View view) {

        if(photoFileManager.getPhotoList() != null) {
            Intent tagIntent = new Intent( MainActivity.this, EditContentActivity.class );
            String imageKeyWords = photoFileManager.getPhotoKeyWords(photoFileManager.getCurrentPhotoFile());

            tagIntent.putExtra(Constants.EXTRA_IMAGE_DATA, imageKeyWords );
            tagIntent.putExtra("Title", getString(R.string.contentTagTitle ));
            tagIntent.putExtra("Prompt", getString(R.string.contentPromptTagDescription) );
            tagIntent.putExtra("UserHint", getString(R.string.contentDetailsTagsHintText) );
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

    public void OnClickSearch(View view){

        Intent intent = new Intent(this, SearchActivity.class);
        // ensure we send the whole list each time
        photoFileManager.setPhotoList(
                photoFileManager.getFilenames(photoFileManager.getPhotoLocation()),
                PhotoFileManager.SAME_ITEM);

//        intent.putExtra(Constants.EXTRA_PHOTO_LIST, photoFileManager.getFilenames());
//        intent.putExtra(Constants.EXTRA_CURRENT_INDEX, photoFileManager.getCurrentPhotoIndex());
        startActivityForResult(intent, Constants.REQUEST_IMAGE_SEARCH);
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

        if (requestCode == Constants.REQUEST_SET_CAPTION && resultCode == RESULT_OK){
            handlePhotoCaptionResult(data);
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
        PhotoSearch photoSearch = new PhotoSearch(photoFileManager);

        boolean KeywordSearch = data.getBooleanExtra(Constants.EXTRA_SEARCH_TYPE_KEYWORD, false);
        String userKeywords = data.getStringExtra(Constants.EXTRA_SEARCH_VALUE_KEYWORD);

        boolean DateSearch = data.getBooleanExtra(Constants.EXTRA_SEARCH_TYPE_DATE, false);
        String userFromDateStr = data.getStringExtra(Constants.EXTRA_SEARCH_VALUE_FROMDATE);
        String userToDateStr = data.getStringExtra(Constants.EXTRA_SEARCH_VALUE_TODATE);

        boolean LocationSearch = data.getBooleanExtra(Constants.EXTRA_SEARCH_TYPE_LOCATION, false);
        String addressLocation = data.getStringExtra(Constants.EXTRA_SEARCH_VALUE_LOCATION);

        boolean GpsSearch = data.getBooleanExtra(Constants.EXTRA_SEARCH_TYPE_GPSBOX, false);
        double[] swGpsCoord = data.getDoubleArrayExtra(Constants.EXTRA_SEARCH_VALUE_GPS_SW);
        double[] neGpsCoord = data.getDoubleArrayExtra(Constants.EXTRA_SEARCH_VALUE_GPS_NE);

        Date userFromDate = null;
        Date userToDate = null;
        try {
            userFromDate = new SimpleDateFormat("yyyy/MM/dd", Locale.US).parse(userFromDateStr);
            userToDate = new SimpleDateFormat("yyyy/MM/dd", Locale.US).parse(userToDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //String addressLocation =  "1500 Amphitheatre Pkwy Mountain View, CA";//"330 Main St Vancouver BC";

//        double[] swGpsCoord = {37.3680502, -122.029187};
//        double[] neGpsCoord = {37.4780502, -122.1391807};

        boolean didSearch = false;
        String[] resultSet = null;
        if (KeywordSearch) {
            resultSet = photoSearch.searchByKeyword(userKeywords);
            didSearch = true;
        }
        if (DateSearch) {
            if (!didSearch || (didSearch && resultSet != null)) {
                resultSet = photoSearch.searchByDate(userFromDate, userToDate);
            }
            didSearch = true;
        }
        if (LocationSearch) {
            if (!didSearch || (didSearch && resultSet != null)) {
                photoSearch.setGeocoder(new Geocoder(this));
                resultSet = photoSearch.searchByLocation(addressLocation);
            }
            didSearch = true;
        }
        if (GpsSearch) {
            if (!didSearch || (didSearch && resultSet != null)) {
                resultSet = photoSearch.searchByGpsBoxLocation(swGpsCoord, neGpsCoord);
            }
            didSearch = true;
        }

        if (resultSet != null) {
            photoFileManager.setPhotoList(resultSet, PhotoFileManager.FIRST_ITEM);

        }
        if (!didSearch || resultSet == null) {
//        if (filenames == null) {
            // search not found, return to full image list
            photoFileManager.setPhotoList(
                    photoFileManager.getFilenames(photoFileManager.getPhotoLocation()),
                    PhotoFileManager.FIRST_ITEM);
        }
//        String s = photoFileManager.getPhotoList()[0];

        updateImageView(photoFileManager.getCurrentPhotoFile());
    }

    private void handlePhotoTagResult(Intent data) {
        String userKeywords = data.getExtras().getString(Constants.EXTRA_IMAGE_DATA );
        Log.d(TAG, "handlePhotoTagResult: data: " + userKeywords);

        photoFileManager.setPhotoKeyWords(photoFileManager.getCurrentPhotoFile(), userKeywords);
    }

    private void handlePhotoCaptionResult(Intent data) {
        String userCaption = data.getExtras().getString(Constants.EXTRA_IMAGE_DATA );
        Log.d(TAG, "handlePhotoCaptionResult: data: " + userCaption);

        photoFileManager.setPhotoCaption(photoFileManager.getCurrentPhotoFile(), userCaption);
    }

    private void updateImageView(String imageFilename) {
        createPicture(imageFilename); // set instance bitmap
        imageView.setImageBitmap(bitmap);

        // retrieve the caption for the new image
        getPhotoCaption(imageFilename);
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

    private void getPhotoCaption(String photoFilename) {
        // retrieve the caption for the new image
        String currentFileCaption = photoFileManager.getPhotoCaption(photoFilename);
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


    /*
            LocationListener Interface Implementations
     */

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged","@@@ Location: lat[" + location.getLatitude()+ "] long[" + location.getLongitude()+ "]");

//        // experimental: get the location name from a gps location
//        Geocoder geo = new Geocoder(this);
//        String city= "Vancouver BC";
//        try {
//            List<Address> addressList = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//            for (Address addr : addressList) {
//                Log.d("onLocationChanged", "addr1: " + addr.getLocality());
//                city = addr.getLocality();
//            }
//
//            // experimental: get the gps location from a location name
//            city= "10 downing st london";
//            if (city == null) {
//                Log.d("onLocationChanged", "TESTNOW: " + (city == null ? "is null" : city));
//
//            }
//            else {
//                Log.d("onLocationChanged", "TESTNOW: " + (city == null ? "is null" : city));
//                addressList = geo.getFromLocationName(city, 4);
//                for (Address addr : addressList) {
//                    Log.d("onLocationChanged", "By locationname: " +
//                            addr.getCountryName() + "\n" +
//                            addr.getLocality() + "\n" +
//                            addr.getSubLocality() + "\n" +
//                            addr.getThoroughfare() + "\n" +
//                            addr.getSubThoroughfare() + "\n" +
//                            addr.getPostalCode() + "\n\n" +
//                            addr.getLatitude() + " " + addr.getLongitude()
//                    );
//                }
//            }
//        } catch (IOException e) {
//            Log.d("onLocationChanged", "geo IOException " + e.getMessage());
//        }
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
        // make available for Espresso testing
        return  photoFileManager;
    }

}
