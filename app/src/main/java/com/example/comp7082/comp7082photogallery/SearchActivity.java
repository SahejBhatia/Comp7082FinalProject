package com.example.comp7082.comp7082photogallery;

import android.content.Context;
import android.content.Intent;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    public String directory = Environment.getExternalStorageDirectory() + "/Android/data/com.example.comp7082.comp7082photogallery/files/Pictures/";

    TextView tagSearchEditText;
    TextView fromDateEditText;
    TextView toDateEditText;
    TextView fromTimeEditText;
    TextView toTimeEditText;

    String[] sourceFilenames;   // the list of image filenames from MainActivity
    String[] filterFilenames;   // the list of image filenames returned to MainActivity
    int currentIndex;
    String fileUserComment = null;  // the keyword tags
    Date fileCreateDate = null;
    Date fileCreateTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        tagSearchEditText = findViewById(R.id.tagSearchEditText);
        fromDateEditText = findViewById(R.id.fromDateEditText);
        toDateEditText = findViewById(R.id.toDateEditText);
        fromTimeEditText = findViewById(R.id.fromTimeEditText);
        toTimeEditText = findViewById(R.id.toTimeEditText);

        Intent intent = getIntent();
        sourceFilenames = intent.getStringArrayExtra(MainActivity.EXTRA_PHOTO_LIST);
        currentIndex = intent.getIntExtra(MainActivity.EXTRA_CURRENT_INDEX, 0);
    }

    public void searchButtonOnClick(View view) {
        // diag markers
        Log.d("searchButtonOnClick", "Button is clicked");

        hideSoftKeyboard();

        filterFilenames = searchImages();

        if (filterFilenames == null) {
            Toast.makeText(this, "Found 0 images", Toast.LENGTH_SHORT).show();
        }
        else if (filterFilenames.length == 1) {
            Toast.makeText(this, "Found " + filterFilenames.length + " image", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Found " + filterFilenames.length + " images", Toast.LENGTH_SHORT).show();
        }

        // send data back to caller
        Intent data = new Intent();
        data.putExtra(MainActivity.EXTRA_PHOTO_LIST,filterFilenames);
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

    private String[] searchImages() {

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
        Log.d("searchImages", "SEARCHING FOR: " + userSearchTags);

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
                Log.d("searchImages", "user time is null");

            }

        } catch (ParseException e) {
            Log.d("searchImages", "Date Parsing error: " + e.getMessage());
        }

        // look for terms in image file Exif data
        ArrayList<String> filteredFilesList = new ArrayList<>();

        for(String imageFileName : sourceFilenames) {
            Log.d("searchImages", "FOR FILE: " + imageFileName);

            getImageFileData(imageFileName);

            // create keywords list from image file
            List<String> imageCommentKeywordsList = new ArrayList<>();
            if (fileUserComment != null && !fileUserComment.isEmpty()) {
                imageCommentKeywordsList = Arrays.asList(fileUserComment.split(" "));
            }

            // for each user keyword, search the image keywords and save the filename of any that are found
            // if there are user keywords, then search for them
            if (userKeywordsList != null && userKeywordsList.length > 0) {
                for (String userKeyword : userKeywordsList) {
                    Log.d("searchImages", "scanning for: " + userKeyword);
                    if (imageCommentKeywordsList.contains(userKeyword) && !filteredFilesList.contains(imageFileName)) {

                        if (isUserDateProvided(userFromDate, userToDate) && isDateInRange(fileCreateDate, userFromDate, userToDate)) {
                            // matches on a keyword and in the date range
                            Log.d("searchImages", "  found for key and date: " + imageFileName);
                            filteredFilesList.add(imageFileName);
                        }
                        else if (!isUserDateProvided(userFromDate, userToDate) ) {
                            // there is no date range, and matches on keyword
                            Log.d("searchImages", "  found for key: " + imageFileName);
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
                    Log.d("searchImages", "  found for date: " + imageFileName);
                    filteredFilesList.add(imageFileName);
                }
            }
        } //file iteration

        // diag reporting
//        String foundfiles = "";
//        for (String item : filteredFilesList) {
//            foundfiles += item + "\n";
//        }
//        Log.d("searchImages", "  filtered list: " + foundfiles);

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
            ExifInterface exif = new ExifInterface(path);

            fileUserComment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT); // resolves to a String
            //String fileImageDescription = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION); // resolves to a String

            String[] fileNameTokens =  imageFileName.split("_");    // parse the date and time from the filename
            fileCreateDate = dateFormatIn.parse(fileNameTokens[1]);
            fileCreateTime = timeFormatIn.parse(fileNameTokens[2]);

            // if (fileUserComment == null) { fileUserComment = "*null*"; }
            //if (fileImageDescription == null) { fileImageDescription = "*null*"; }

            Log.d("getImageFileData", "Load UserComment: " + (fileUserComment == null ? "is null" : fileUserComment));
            //Log.d("getImageFileData", "Load ImageDescription: " + fileImageDescription);
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
}
