package com.example.comp7082.comp7082photogallery;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    public static final int LATITUDE_INDEX = 0;
    public static final int LONGITUDE_INDEX = 1;

    public String directory = Environment.getExternalStorageDirectory() + "/Android/data/com.example.comp7082.comp7082photogallery/files/Pictures/";

    TextView tagSearchEditText;
    TextView fromDateEditText;
    TextView toDateEditText;
    TextView fromTimeEditText;
    TextView toTimeEditText;

    TextView addressEditText;
    TextView gpsSWLatitudeEditText;
    TextView gpsSWLongitudeEditText;
    TextView gpsNELatitudeEditText;
    TextView gpsNELongitudeEditText;

    String[] sourceFilenames;   // the list of image filenames from MainActivity
    int currentIndex;
    String fileKeywordTags = null;  // the keyword tags
    Date fileCreateDate = null;
    Date fileCreateTime = null;
    float[] fileLocation = {0.0f, 0.0f};    // exif lat/long
    boolean fileLocationFound = false;      // does exif lat/long exist

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Keyword / Date fields
        tagSearchEditText = findViewById(R.id.tagSearchEditText);
        fromDateEditText = findViewById(R.id.fromDateEditText);
        toDateEditText = findViewById(R.id.toDateEditText);
        fromTimeEditText = findViewById(R.id.fromTimeEditText);
        toTimeEditText = findViewById(R.id.toTimeEditText);

        // Location fields
        addressEditText = findViewById(R.id.addressLocationEditText);
        gpsSWLatitudeEditText = findViewById(R.id.gpsSWLatitudeTextEdit);
        gpsSWLongitudeEditText = findViewById(R.id.gpsSWLongitudeEditText);
        gpsNELatitudeEditText = findViewById(R.id.gpsNELatitudeTextEdit);
        gpsNELongitudeEditText = findViewById(R.id.gpsNELongitudeTextEdit);

        Intent intent = getIntent();
        sourceFilenames = intent.getStringArrayExtra(MainActivity.EXTRA_PHOTO_LIST);
        currentIndex = intent.getIntExtra(MainActivity.EXTRA_CURRENT_INDEX, 0);
    }

    /**
     * perform a keyword and/or datetime search when the keywordSearch button is pressed
     *
     * @param view not used
     */
    public void keywordSearchButtonOnClick(View view) {
        // diag markers
        Log.d("keywordSearchBtn", "Button is clicked");

        hideSoftKeyboard();
        returnSearchResults(keywordSearchImages());
    }

    private void returnSearchResults(String[] resultSet) {
        if (resultSet == null) {
            Toast.makeText(this, "Found 0 images", Toast.LENGTH_SHORT).show();
        }
        else if (resultSet.length == 1) {
            Toast.makeText(this, "Found " + resultSet.length + " image", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Found " + resultSet.length + " images", Toast.LENGTH_SHORT).show();
        }

        // send data back to caller
        Intent data = new Intent();
        data.putExtra(MainActivity.EXTRA_PHOTO_LIST,resultSet);
        setResult(RESULT_OK,data);
        finish();
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

    private String[] keywordSearchImages() {

        if (tagSearchEditText.getText().toString().isEmpty() &&
            fromDateEditText.getText().toString().isEmpty() &&
            toDateEditText.getText().toString().isEmpty() &&
            fromTimeEditText.getText().toString().isEmpty() &&
            toTimeEditText.getText().toString().isEmpty())
        {
            return null;
        }

        // collect the user search terms
        String userSearchTags = tagSearchEditText.getText().toString();
        String[] userKeywordsList = null;
        Log.d("keywordSearchImages", "SEARCHING FOR: " + userSearchTags);

        if (!userSearchTags.isEmpty()) {
            userKeywordsList = userSearchTags.toLowerCase().split(" ");
        }

        Date userFromDate = null;
        Date userToDate = null;
        Date userFromTime = null;
        Date userToTime = null;
        try {
            if (!fromDateEditText.getText().toString().isEmpty()) {
                    userFromDate =  new SimpleDateFormat("yyyy/MM/dd", Locale.US).parse(fromDateEditText.getText().toString());
            }
            if (!toDateEditText.getText().toString().isEmpty()) {
                userToDate =  new SimpleDateFormat("yyyy/MM/dd", Locale.US).parse(toDateEditText.getText().toString());
            }
            if (!fromTimeEditText.getText().toString().isEmpty()) {
                userFromTime =  new SimpleDateFormat("HH:mm:ss", Locale.US).parse(fromTimeEditText.getText().toString());
            }
            if (!toTimeEditText.getText().toString().isEmpty()) {
                userToTime =  new SimpleDateFormat("HH:mm:ss", Locale.US).parse(toTimeEditText.getText().toString());
            }

            if (userFromTime == null || userToTime == null) {
                // temp code for compiler
                Log.d("keywordSearchImages", "user time is null");

            }

        } catch (ParseException e) {
            Log.d("keywordSearchImages", "Date Parsing error: " + e.getMessage());
        }

        // look for terms in image file Exif data
        ArrayList<String> filteredFilesList = new ArrayList<>();

        for(String imageFileName : sourceFilenames) {
            Log.d("keywordSearchImages", "FOR FILE: " + imageFileName);

            getImageFileData(imageFileName);

            // create keywords list from image file
            List<String> imageCommentKeywordsList = new ArrayList<>();
            if (fileKeywordTags != null && !fileKeywordTags.isEmpty()) {
                imageCommentKeywordsList = Arrays.asList(fileKeywordTags.split(" "));
            }

            // for each user keyword, search the image keywords and save the filename of any that are found
            // if there are user keywords, then search for them
            if (userKeywordsList != null && userKeywordsList.length > 0) {
                for (String userKeyword : userKeywordsList) {
                    Log.d("keywordSearchImages", "scanning for: " + userKeyword);
                    if (imageCommentKeywordsList.contains(userKeyword) && !filteredFilesList.contains(imageFileName)) {

                        if (isUserDateProvided(userFromDate, userToDate) && isDateInRange(fileCreateDate, userFromDate, userToDate)) {
                            // matches on a keyword and in the date range
                            Log.d("keywordSearchImages", "  found for key and date: " + imageFileName);
                            filteredFilesList.add(imageFileName);
                        }
                        else if (!isUserDateProvided(userFromDate, userToDate) ) {
                            // there is no date range, and matches on keyword
                            Log.d("keywordSearchImages", "  found for key: " + imageFileName);
                            filteredFilesList.add(imageFileName);
                        }

                    }
                }

            }
            else if ( !filteredFilesList.contains(imageFileName)){  // if current file is not in the list
                // there are no keywords, try for a date only search

                if (isUserDateProvided(userFromDate, userToDate) && isDateInRange(fileCreateDate, userFromDate, userToDate)) {
//                if ( minDate != null &&
//                        !fileCreateDate.before(minDate) && !fileCreateDate.after(minDate) ) {
                    // matches on a keyword and in the date range
                    Log.d("keywordSearchImages", "  found for date: " + imageFileName);
                    filteredFilesList.add(imageFileName);
                }
            }
        } //file iteration

        // diag reporting
//        String foundfiles = "";
//        for (String item : filteredFilesList) {
//            foundfiles += item + "\n";
//        }
//        Log.d("keywordSearchImages", "  filtered list: " + foundfiles);

        // return the results
        if (filteredFilesList.isEmpty()) {
            return null;
        }
        return filteredFilesList.toArray(new String[0]);
    }

    private void getImageFileData(String imageFileName) {
        SimpleDateFormat dateFormatIn = new SimpleDateFormat("yyyyMMdd", Locale.US);
        SimpleDateFormat dateFormatOut = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat timeFormatIn = new SimpleDateFormat("HHmmss", Locale.US);
        SimpleDateFormat timeFormatOut = new SimpleDateFormat("HH:mm:ss", Locale.US);

        try {
            String path = directory + imageFileName;
            File localFile = new File(path);
            ExifInterface exif = new ExifInterface(path);

            fileKeywordTags = ExifUtility.getExifTagString(localFile, ExifUtility.EXIF_KEYWORDS_TAG);
            File currentFile = new File(directory + imageFileName);
            fileLocationFound = ExifUtility.getExifLatLong(currentFile, fileLocation);
            //String fileImageDescription = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION); // resolves to a String

            String[] fileNameTokens =  imageFileName.split("_");    // parse the date and time from the filename
            fileCreateDate = dateFormatIn.parse(fileNameTokens[1]);
            fileCreateTime = timeFormatIn.parse(fileNameTokens[2]);

            // if (fileKeywordTags == null) { fileKeywordTags = "*null*"; }
            //if (fileImageDescription == null) { fileImageDescription = "*null*"; }

            Log.d("getImageFileData", "Load UserComment: " + (fileKeywordTags == null ? "is null" : fileKeywordTags));
            Log.d("getImageFileData", "Load LatLong[" + fileLocationFound + "]: " + fileLocation[LATITUDE_INDEX] + " " + fileLocation[LONGITUDE_INDEX]);
            Log.d("getImageFileData", "Parsed Date: " + dateFormatOut.format(fileCreateDate) + " Time: " + timeFormatOut.format(fileCreateTime));
            Log.d("getImageFileData", "Load Date?: " + exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED));
        } catch (IOException | ParseException e) {
            Log.d("getImageFileData", "Could not open file: " + imageFileName);
            //e.printStackTrace();
        }
    }

    private boolean isUserDateProvided(Date fromDate, Date toDate) {
        return fromDate != null || toDate != null;
    }

    private boolean isDateInRange(Date targetDate, Date fromDate, Date toDate) {
        Date minDate = null;
        Date maxDate = null;

        // check for date range
        if (fromDate != null) {
            minDate = fromDate;
        }
        else if (toDate != null) {
            minDate = toDate;
        }

        if (toDate != null) {
            maxDate = toDate;
        }
        else if (fromDate != null) {
            maxDate = fromDate;
        }

        return minDate != null && !targetDate.before(minDate) && !targetDate.after(maxDate);
    }


    /*
            Location Search methods
     */

    /**
     * use onKeyUp to toggle location fields accessibility based on which
     * fields the user has entered first:
     *      * enter in address field, gps fields are disabled
     *      * enter in gps fields, address field is disabled
     *
     * @param keyCode not used
     * @param event not used
     * @return boolean, based on super result
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

//        Log.d("onKeyUp", "event: " + event.toString());
//        Log.d("onKeyUp", "state: addr["+addressEditText.isEnabled()+"] gps["+gpsCoordFieldsIsEnabled()+"]" );

        if (addressEditText.isEnabled()) {
            if (!addressEditText.getText().toString().isEmpty()) {
                setGpsFieldsEnabled(false);
                Log.d("onKeyUp", "typing in address field");
            } else {
                setGpsFieldsEnabled(true);
                Log.d("onKeyUp", "enable gps fields");
            }
        }
        if (gpsCoordFieldsIsEnabled()) {
            Log.d("onKeyUp", "gps(T): empty["+gpsCoordFieldsIsEmpty()+"]");
            if (!gpsCoordFieldsIsEmpty()) {
                addressEditText.setEnabled(false);
                Log.d("onKeyUp", "typing in gps field");
            } else {
                addressEditText.setEnabled(true);
                Log.d("onKeyUp", "enable address field");
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void setGpsFieldsEnabled(boolean isEnabled) {
        gpsNELatitudeEditText.setEnabled(isEnabled);
        gpsNELongitudeEditText.setEnabled(isEnabled);
        gpsSWLatitudeEditText.setEnabled(isEnabled);
        gpsSWLongitudeEditText.setEnabled(isEnabled);
    }

    private boolean gpsCoordFieldsIsEnabled() {
        return     gpsNELatitudeEditText.isEnabled();
    }

    private boolean gpsCoordFieldsIsEmpty() {
        return gpsNELatitudeEditText.getText().toString().isEmpty() &&
                gpsNELongitudeEditText.getText().toString().isEmpty() &&
                gpsSWLatitudeEditText.getText().toString().isEmpty() &&
                gpsSWLongitudeEditText.getText().toString().isEmpty();
    }

    /**
     * perform a location search when the locationSearch button is pressed
     *      * search by text address, or
     *      * search by gps bounding area
     *
     * @param view not used
     */
    public void locationSearchButtonOnClick(View view) {
        Log.d("locationSearchBtn", "Button is clicked");

        hideSoftKeyboard();
        if (isValidGpsCoords()) {
            returnSearchResults(locationSearchImages());
        }
        else {
            Toast.makeText(this, "Please correct an invalid GPS coordinate", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isValidGpsCoords() {
        boolean result = true;

        float maxLatitude = 90.0f;
        float maxLongitude = 180f;

//        Log.d("isValidGpsCoords","SWLat: " + gpsSWLatitudeEditText.getText().toString() +
//                " SWLng: " + gpsSWLongitudeEditText.getText().toString() +
//                " NELat: " + gpsNELatitudeEditText.getText().toString() +
//                " NELng: " + gpsNELongitudeEditText.getText().toString() );

        if (!gpsSWLatitudeEditText.getText().toString().isEmpty() &&
                Math.abs(Float.valueOf(gpsSWLatitudeEditText.getText().toString())) > maxLatitude)
        {
            result = false;
        }
        if (!gpsNELatitudeEditText.getText().toString().isEmpty() &&
                Math.abs(Float.valueOf(gpsNELatitudeEditText.getText().toString())) > maxLatitude)
        {
            result = false;
        }
        if (!gpsSWLongitudeEditText.getText().toString().isEmpty() &&
                Math.abs(Float.valueOf(gpsSWLongitudeEditText.getText().toString())) > maxLongitude)
        {
            result = false;
        }
        if (!gpsNELongitudeEditText.getText().toString().isEmpty() &&
                Math.abs(Float.valueOf(gpsNELongitudeEditText.getText().toString())) > maxLongitude)
        {
            result = false;
        }

        return result;
    }

    private String[] locationSearchImages() {
        Log.d("locationSearchImages", "search on location");
        if (addressEditText.getText().toString().isEmpty() &&
                gpsSWLatitudeEditText.getText().toString().isEmpty() &&
                gpsSWLongitudeEditText.getText().toString().isEmpty() &&
                gpsNELatitudeEditText.getText().toString().isEmpty() &&
                gpsNELongitudeEditText.getText().toString().isEmpty())
        {
            return null;
        }

        Geocoder geo = new Geocoder(this);
        double boundsSWCoord[] = {0.0, 0.0} ;   // lat, long
        double boundsNECoord[] = {0.0, 0.0} ;   // lat, long
        double boundingBoxSize = 0.05500;       // an arbitrary value, approximating 4 km @ 49 lat

        // collect the user search terms
        if (!addressEditText.getText().toString().isEmpty()) {
            Log.d("locationSearchImages", "search by address: "+ addressEditText.getText().toString());
            // address search
            /*
                    steps
                    give address to geocoder to get the latlong: gets an addresslist w/ latlong
                    if addresslist is empty, then return null

                    set up a bounding box based on addresslist.latlong

             */
            try {
                List<Address>addressList = geo.getFromLocationName(addressEditText.getText().toString(), 1);

                if (addressList != null && addressList.size() > 0) {
                    Log.d("locationSearchImages", "search by address: results found");
                    boundsSWCoord[LATITUDE_INDEX] = addressList.get(0).getLatitude() - boundingBoxSize;
                    boundsSWCoord[LONGITUDE_INDEX] = addressList.get(0).getLongitude() + boundingBoxSize;
                    boundsNECoord[LATITUDE_INDEX] = addressList.get(0).getLatitude() + boundingBoxSize;
                    boundsNECoord[LONGITUDE_INDEX] = addressList.get(0).getLongitude() - boundingBoxSize;
                }
                else {
                    Log.d("locationSearchImages", "empty address results");
                    return null;
                }
            } catch (IOException e) {
                Log.d("locationSearchImages", "address geocoder IOException: " + e.getMessage());
                return null;
            }
        }
        else {
            // gps bounding box search
            /*
                    steps
                    test user bounding box
                        if sw is null and ne is null then return null
                        if sw != null and ne is null then set ne = sw
                        if sw == null and ne != null then set sw = ne

             */
            if (    gpsSWLatitudeEditText.getText().toString().isEmpty() &&
                    gpsSWLongitudeEditText.getText().toString().isEmpty() &&
                    gpsNELatitudeEditText.getText().toString().isEmpty() &&
                    gpsNELongitudeEditText.getText().toString().isEmpty() )
            {
                Log.d("locationSearchImages", "gps box search: empty bounds");
                return null;
            }
            if (    !gpsSWLatitudeEditText.getText().toString().isEmpty() &&
                    !gpsSWLongitudeEditText.getText().toString().isEmpty() &&
                    gpsNELatitudeEditText.getText().toString().isEmpty() &&
                    gpsNELongitudeEditText.getText().toString().isEmpty() )
            {
                Log.d("locationSearchImages", "gps box search: set gpsNE to gpsSW");
                boundsSWCoord[LATITUDE_INDEX] = Double.valueOf(gpsSWLatitudeEditText.getText().toString());
                boundsSWCoord[LONGITUDE_INDEX] = Double.valueOf(gpsSWLongitudeEditText.getText().toString());
                boundsNECoord[LATITUDE_INDEX] = boundsSWCoord[LATITUDE_INDEX];
                boundsNECoord[LONGITUDE_INDEX] =boundsSWCoord[LONGITUDE_INDEX];

            }
            if (    gpsSWLatitudeEditText.getText().toString().isEmpty() &&
                    gpsSWLongitudeEditText.getText().toString().isEmpty() &&
                    !gpsNELatitudeEditText.getText().toString().isEmpty() &&
                    !gpsNELongitudeEditText.getText().toString().isEmpty() )
            {
                Log.d("locationSearchImages", "gps box search: set gpsSW to gpsNE");
                boundsNECoord[LATITUDE_INDEX] = Double.valueOf(gpsNELatitudeEditText.getText().toString());
                boundsNECoord[LONGITUDE_INDEX] =  Double.valueOf(gpsNELongitudeEditText.getText().toString());
                boundsSWCoord[LATITUDE_INDEX] = boundsNECoord[LATITUDE_INDEX];
                boundsSWCoord[LONGITUDE_INDEX] =boundsNECoord[LONGITUDE_INDEX];
            }
        }

        /*
            perform location search

                    for each image
                        get image latlong from exif
                        if not null then is image latlong within bounds
                            if true then save image, else go to next image
                    return the filtered image list
         */
        ArrayList<String> filteredFilesList = new ArrayList<>();

        for(String imageFileName : sourceFilenames) {
            Log.d("locationSearchImages", "FOR FILE: " + imageFileName);

            fileLocationFound = false;
            getImageFileData(imageFileName);    // sets fileLocationFound

            Log.d("locationSearchImages", "file[" + fileLocation[LATITUDE_INDEX] + " " + fileLocation[LONGITUDE_INDEX]
                    +"] bounds: SW["+ boundsSWCoord[LATITUDE_INDEX] +
                    " " + boundsSWCoord[LONGITUDE_INDEX]  +
                    "] NE[" +boundsNECoord[LATITUDE_INDEX]+
                    " " +boundsNECoord[LONGITUDE_INDEX]+"]");
            if (fileLocationFound) {

                // convert to float/doubles to strings for comparison to
                String[] fileLocation = {Float.toString(this.fileLocation[LATITUDE_INDEX]), Float.toString(this.fileLocation[LONGITUDE_INDEX])};
                String[] bndsSWCoord = {Double.toString(boundsSWCoord[LATITUDE_INDEX]), Double.toString(boundsSWCoord[LONGITUDE_INDEX])};
                String[] bndsNECoord = {Double.toString(boundsNECoord[LATITUDE_INDEX]), Double.toString(boundsNECoord[LONGITUDE_INDEX])};

//                Log.d("locationSearchImages","logic SWlat: " + (fileLocation[LATITUDE_INDEX].compareTo(bndsSWCoord[LATITUDE_INDEX]) >= 0));
//                Log.d("locationSearchImages","logic NElat: " + (fileLocation[LATITUDE_INDEX].compareTo(bndsNECoord[LATITUDE_INDEX]) <= 0));
//                Log.d("locationSearchImages","logic SWlng: " + (fileLocation[LONGITUDE_INDEX].compareTo(bndsSWCoord[LONGITUDE_INDEX]) >= 0));
//                Log.d("locationSearchImages","logic NElng: " + (fileLocation[LONGITUDE_INDEX].compareTo(bndsNECoord[LONGITUDE_INDEX]) <= 0));
                if (fileLocation[LATITUDE_INDEX].compareTo(bndsSWCoord[LATITUDE_INDEX]) >= 0 &&
                        fileLocation[LATITUDE_INDEX].compareTo(bndsNECoord[LATITUDE_INDEX]) <= 0 &&
                        fileLocation[LONGITUDE_INDEX].compareTo(bndsSWCoord[LONGITUDE_INDEX]) >= 0 &&
                        fileLocation[LONGITUDE_INDEX].compareTo(bndsNECoord[LONGITUDE_INDEX]) <= 0)
                {
                    Log.d("locationSearchImages", "  found for location: " + imageFileName);
                    filteredFilesList.add(imageFileName);
                }
            }
            else {
                Log.d("locationSearchImages", "  exif not found: " + imageFileName);
            }

        }

        // return the results
        if (filteredFilesList.isEmpty()) {
            return null;
        }
        return filteredFilesList.toArray(new String[0]);
    }
}
