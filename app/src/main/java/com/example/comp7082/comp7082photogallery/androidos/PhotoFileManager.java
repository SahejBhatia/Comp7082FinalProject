package com.example.comp7082.comp7082photogallery.androidos;

import android.os.Environment;
import android.util.Log;

import com.example.comp7082.comp7082photogallery.util.Constants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PhotoFileManager {
    private String TAG = PhotoFileManager.class.getSimpleName();

    public static final String storageLocation = Environment.getExternalStorageDirectory() + Constants.STORAGE_LOCATION;
    public static final int FIRST_ITEM = -1;
    public static final int LAST_ITEM = -2;
    public static final int SAME_ITEM = -3;


    private String currentPhotoLocation; // the path to the photo folder
    private String[] photoList;
    private String currentPhotoFile;
    private int currentPhotoIndex;

    public PhotoFileManager() {
        this(storageLocation);
    }
    public PhotoFileManager(String photoLocation) {
        currentPhotoIndex = 0;
        if (photoLocation != null && !photoLocation.isEmpty()) {
            currentPhotoLocation = photoLocation;
            photoList = getFilenames(photoLocation);
            setCurrentPhotoFile(currentPhotoIndex);
        }
    }

    public String getPhotoLocation() {
        return currentPhotoLocation;
    }

    public String[] getPhotoList() {
        return photoList;
    }

    public int getCurrentPhotoIndex() {
        return currentPhotoIndex;
    }

    public String getCurrentPhotoFile() {
        return currentPhotoFile;
    }

    public void setCurrentPhotoFile(String newCurrentPhotoFile) {
        currentPhotoFile = newCurrentPhotoFile;
    }

    public void setCurrentPhotoFile(int currentPhotoIndex) {
        if (photoList != null && photoList.length > 0) {
            currentPhotoFile = getPhotoLocation() + photoList[currentPhotoIndex];
        }
    }

    public void setCurrentPhotoIndex(int newCurrentPosition) {
        // making the assumption that photoList != null
        switch (newCurrentPosition) {
            case PhotoFileManager.FIRST_ITEM:
                currentPhotoIndex = 0;
                break;
            case PhotoFileManager.LAST_ITEM:
                currentPhotoIndex = photoList.length -1;
                break;
            case PhotoFileManager.SAME_ITEM:
                // keep current index position
                break;
            default:
                if (newCurrentPosition < photoList.length) {
                    currentPhotoIndex = newCurrentPosition;
                }
                else {
                    currentPhotoIndex = photoList.length -1;
                    Log.d(TAG, "set_PhotoList: requested index[" + newCurrentPosition  + "] defaulted to last list item: " + currentPhotoIndex);
                }
                if (currentPhotoIndex < 0) {
                    currentPhotoIndex = 0;
                    Log.d(TAG, "set_PhotoList: requested index[" + newCurrentPosition  + "] defaulted to: " + currentPhotoIndex);
                }
                break;
        }
    }

    public void setPhotoList(String[] newPhotoList) {
        set_PhotoList(newPhotoList, 0);
    }

    public void setPhotoList(String[] newPhotoList, int newCurrentPosition) {
        set_PhotoList(newPhotoList, newCurrentPosition);
    }

    private void set_PhotoList(String[] newPhotoList, int newCurrentPosition) {
        if (newPhotoList != null && newPhotoList.length > 0) {
            photoList = newPhotoList;
            setCurrentPhotoIndex(newCurrentPosition);
            setCurrentPhotoFile(getCurrentPhotoIndex());
        }
    }

    public String[] getFilenames() {
        return getFilenames(currentPhotoLocation);
    }

    // get the file names from the provided storage location
    public String[] getFilenames(String directory) {
        File path = new File(directory);
        String[] filenames;
        if (path.exists()) {
            filenames = path.list();
            if (filenames != null) {
                Log.d(TAG, "getFilenames: filenames length: " + filenames.length);
            }
            else {
                Log.d(TAG, "getFilenames: filenames length: null" );
            }
        }
        else {
            filenames = new String[0];
        }
        return filenames;
    }

    public String getPhotoKeyWords(String workingPhotoFile) {
        File currentFile = new File(workingPhotoFile);
        return ExifUtility.getExifTagString(currentFile, ExifUtility.EXIF_KEYWORDS_TAG);
    }

    public void setPhotoKeyWords(String workingPhotoFile, String imageKeywords) {
        File currentFile = new File(workingPhotoFile);
        ExifUtility.setExifTagString(currentFile, ExifUtility.EXIF_KEYWORDS_TAG, imageKeywords);
    }

    public String getPhotoCaption(String workingPhotoFile) {
        File currentFile = new File(workingPhotoFile);
        return ExifUtility.getExifTagString(currentFile, ExifUtility.EXIF_CAPTION_TAG);
    }

    public void setPhotoCaption(String workingPhotoFile, String imageCaptionText) {
        File currentFile = new File(workingPhotoFile);
        ExifUtility.setExifTagString(currentFile, ExifUtility.EXIF_CAPTION_TAG, imageCaptionText);
    }

    public String getPhotoTimeStamp(String workingPhotoFile) {
        File currentFile = new File(workingPhotoFile);
        return ExifUtility.getExifTagString(currentFile, ExifUtility.EXIF_DATETIME_TAG);
    }

    public float[] getPhotoGpsLocation(String workingPhotoFile) {
        File currentFile = new File(workingPhotoFile);
        float[] fileLocation = {0.0f, 0.0f};
        boolean found = ExifUtility.getExifLatLong(currentFile, fileLocation);
        return fileLocation;
    }

    public String getCurrentFilePath() {
        if (photoList != null && photoList.length > 0) {
            return currentPhotoLocation + photoList[currentPhotoIndex];
        }
        return null;
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(getPhotoLocation());
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        setCurrentPhotoFile(image.getAbsolutePath());
        return image;
    }

}
