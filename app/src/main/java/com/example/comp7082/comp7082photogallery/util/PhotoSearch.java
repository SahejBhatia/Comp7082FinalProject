package com.example.comp7082.comp7082photogallery.util;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.example.comp7082.comp7082photogallery.androidos.PhotoFileManager;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhotoSearch {

    private PhotoFileManager searchFileManager;
    private Geocoder geocoder;

    public PhotoSearch(PhotoFileManager sourceFileManager) {
        searchFileManager = sourceFileManager;
    }


    public String[] searchByKeyword(String keywordsList) {
        if (searchFileManager == null || searchFileManager.getPhotoList().length < 1 ||
                keywordsList == null || keywordsList.isEmpty())
        {
            return null;    // nothing to do
        }

        ArrayList<String> searchResultsList = new ArrayList<>();
        String[] userKeywordsList = keywordsList.toLowerCase().split(" ");

        for (String photoFile : searchFileManager.getPhotoList() ) {

            // create a list of keywords from image file
            List<String> photoFileKeywordsList = new ArrayList<>();
            String photoFileKeywords = searchFileManager.getPhotoKeyWords(searchFileManager.getPhotoLocation() + photoFile);
            if (photoFileKeywords != null && !photoFileKeywords.isEmpty()) {
                photoFileKeywordsList = Arrays.asList(photoFileKeywords.toLowerCase().split(" "));
            }

            // check for a keyword match
            for (String userKeyword : userKeywordsList ) {
                if (photoFileKeywordsList.contains(userKeyword) && !searchResultsList.contains(photoFile)) {
                    searchResultsList.add(photoFile);
                }
            }
        }

        // return the results
        if (searchResultsList.isEmpty()) {
            return null;
        }
        searchFileManager.setPhotoList(searchResultsList.toArray(new String[0]));
        return searchFileManager.getPhotoList();
    } //searchByKeyword

    public String[] searchByDate(Date userFromDate, Date userToDate) {
        if (searchFileManager == null || searchFileManager.getPhotoList().length < 1 ||
                userFromDate == null || userToDate == null ||
                userToDate.before(userFromDate) )
        {
            return null;    // nothing to do
        }

        ArrayList<String> searchResultsList = new ArrayList<>();

        for (String photoFile : searchFileManager.getPhotoList() ) {

            // get date from image file
            String[] fileNameTokens =  photoFile.split("_");    // parse the date and time from the filename
            Date fileCreateDate;
            try {
                fileCreateDate = new SimpleDateFormat("yyyyMMdd", Locale.US).parse(fileNameTokens[1]);

                // check for a date match
                if (fileCreateDate.compareTo(userFromDate) >= 0 && fileCreateDate.compareTo(userToDate) <= 0
                        && !searchResultsList.contains(photoFile))
                {
                    searchResultsList.add(photoFile);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        // return the results
        if (searchResultsList.isEmpty()) {
            return null;
        }
        searchFileManager.setPhotoList(searchResultsList.toArray(new String[0]));
        return searchFileManager.getPhotoList();
    } //searchByDate

    public String[] searchByLocation(String userLocationAddress) {
        if (searchFileManager == null || searchFileManager.getPhotoList().length < 1 ||
                userLocationAddress == null || userLocationAddress.isEmpty())
        {
            return null;    // nothing to do
        }

        double[] locationAddressGps = {0.0, 0.0};
        try {
            List<Address>addressList = geocoder.getFromLocationName(userLocationAddress, 1);

            if (addressList != null && addressList.size() > 0) {
                Log.d("locationSearchImages", "search by address: results found");

                locationAddressGps[BoundingBox.LATITUDE_INDEX] = addressList.get(0).getLatitude();
                locationAddressGps[BoundingBox.LONGITUDE_INDEX] = addressList.get(0).getLongitude();
            }
            else {
                Log.d("locationSearchImages", "empty address results");
                return null;
            }
        } catch (IOException e) {
            Log.d("locationSearchImages", "address geocoder IOException: " + e.getMessage());
            return null;
        }

        return doGpsBoundingBoxSearch(new BoundingBox(locationAddressGps));
    } //searchByLocation

    public String[] searchByGpsBoxLocation(double[] swGpsCoord, double[] neGpsCoord) {
        if (searchFileManager == null || searchFileManager.getPhotoList().length < 1)
        {
            return null;    // nothing to do
        }

        return doGpsBoundingBoxSearch(new BoundingBox(swGpsCoord, neGpsCoord));
    }

    private String[] doGpsBoundingBoxSearch(BoundingBox boundingBox) {
        ArrayList<String> searchResultsList = new ArrayList<>();

        for (String photoFile : searchFileManager.getPhotoList() ) {
            float[] photoGpsLocation = searchFileManager.getPhotoGpsLocation(
                    searchFileManager.getPhotoLocation() + photoFile);

            if (boundingBox.contains(photoGpsLocation)  && !searchResultsList.contains(photoFile)) {
                searchResultsList.add(photoFile);
            }
        }

        // return the results
        if (searchResultsList.isEmpty()) {
            return null;
        }
        searchFileManager.setPhotoList(searchResultsList.toArray(new String[0]));
        return searchFileManager.getPhotoList();
    }

    // needed to give the searchByLocation the means to lookup an address
    public void setGeocoder(Geocoder geocoder) {
        this.geocoder = geocoder;
    }

    class BoundingBox {
        public static final int LATITUDE_INDEX = 0;
        public static final int LONGITUDE_INDEX = 1;

        private double[] boundsSWCoord = {0.0, 0.0};   // lat, long
        private double[] boundsNECoord = {0.0, 0.0};   // lat, long
        private double boundingBoxSize = 0.05500;       // an arbitrary value, approximating 4 km @ 49 lat

        BoundingBox(double[] swCoordinate, double[] neCoordinate) {
            boundsSWCoord = swCoordinate;
            boundsNECoord = neCoordinate;
        }

        BoundingBox(double[] locationGps) {
            boundsSWCoord[LATITUDE_INDEX] = locationGps[LATITUDE_INDEX] - boundingBoxSize;
            boundsSWCoord[LONGITUDE_INDEX] = locationGps[LONGITUDE_INDEX] + boundingBoxSize;
            boundsNECoord[LATITUDE_INDEX] = locationGps[LATITUDE_INDEX] + boundingBoxSize;
            boundsNECoord[LONGITUDE_INDEX] = locationGps[LONGITUDE_INDEX] - boundingBoxSize;

            if (boundsSWCoord[LATITUDE_INDEX] < -90) {  boundsSWCoord[LATITUDE_INDEX] = -90; }
            if (boundsSWCoord[LATITUDE_INDEX] >  90) {  boundsSWCoord[LATITUDE_INDEX] =  90; }
            if (boundsNECoord[LATITUDE_INDEX] < -90) {  boundsNECoord[LATITUDE_INDEX] = -90; }
            if (boundsNECoord[LATITUDE_INDEX] >  90) {  boundsNECoord[LATITUDE_INDEX] =  90; }
            if (boundsSWCoord[LONGITUDE_INDEX] < -180) {  boundsSWCoord[LONGITUDE_INDEX] = -180; }
            if (boundsSWCoord[LONGITUDE_INDEX] >  180) {  boundsSWCoord[LONGITUDE_INDEX] =  180; }
            if (boundsNECoord[LONGITUDE_INDEX] < -180) {  boundsNECoord[LONGITUDE_INDEX] = -180; }
            if (boundsNECoord[LONGITUDE_INDEX] >  180) {  boundsNECoord[LONGITUDE_INDEX] =  180; }
        }

        boolean contains(float[] targetCoordinate) {
            // convert float/doubles to strings for comparison to
            String[] fileLocation = {Float.toString(targetCoordinate[LATITUDE_INDEX]),
                    Float.toString(targetCoordinate[LONGITUDE_INDEX])};
            String[] boundsSWCoordStr = {Double.toString(boundsSWCoord[LATITUDE_INDEX]),
                    Double.toString(boundsSWCoord[LONGITUDE_INDEX])};
            String[] boundsNECoordStr = {Double.toString(boundsNECoord[LATITUDE_INDEX]),
                    Double.toString(boundsNECoord[LONGITUDE_INDEX])};

            return  (fileLocation[LATITUDE_INDEX].compareTo(boundsSWCoordStr[LATITUDE_INDEX]) >= 0 &&
                    fileLocation[LATITUDE_INDEX].compareTo(boundsNECoordStr[LATITUDE_INDEX]) <= 0 &&
                    fileLocation[LONGITUDE_INDEX].compareTo(boundsSWCoordStr[LONGITUDE_INDEX]) >= 0 &&
                    fileLocation[LONGITUDE_INDEX].compareTo(boundsNECoordStr[LONGITUDE_INDEX]) <= 0);
        }
    } //class BoundingBox

}
