package com.example.comp7082.comp7082photogallery;

import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * utility methods to read and write data to EXIF data tags
 *
 * Use with android version 24+
 *
 */
public class ExifUtility {

    public static final String EXIF_KEYWORDS_TAG = ExifInterface.TAG_MAKER_NOTE;
    public static final String EXIF_CAPTION_TAG = ExifInterface.TAG_USER_COMMENT;
    public static final String EXIF_DATETIME_TAG = ExifInterface.TAG_DATETIME_DIGITIZED;

    /**
     * reads a string based Exif Tag for the given file
     *
     * @param exifFile the File object to read from
     * @param exifTagName the Exif tag to read from
     * @return a string with the Exif tag data
     */
    static public String getExifTagString(File exifFile, String exifTagName) {

        String exifTagString;
        try {
            ExifInterface exif = new ExifInterface(exifFile.getCanonicalPath());

            exifTagString = exif.getAttribute(exifTagName);

            Log.d("getExifTagString", "Read " + exifTagName + ": " + (exifTagString == null ? "is null" : exifTagString));
        } catch (IOException e) {
            Log.d("getExifTagString", "IOException: " + e.getMessage());
            exifTagString = null;
        }
        return exifTagString;
    }

    /**
     * writes a string to the given Exif tag for the given file
     *
     * @param exifFile the File object to write to
     * @param exifTagName the Exif tag to write to. Should be a string based tag
     * @param exifTagValue the Exif string data to write
     */
    static public void setExifTagString(File exifFile, String exifTagName, String exifTagValue) {

        try {
            ExifInterface exif = new ExifInterface(exifFile.getCanonicalPath());

            exif.setAttribute(exifTagName, exifTagValue);
            exif.saveAttributes();

            Log.d("setExifTagString", "Write " + exifTagName + ": " + (exifTagValue == null ? "is null" : exifTagValue));
        } catch (IOException e) {
            Log.d("setExifTagString", "IOException: " + e.getMessage());
        }
    }

    /**
     * gets the latitude and longitude coordinates from the Exif tags
     *
     * @param exifFile the File object to read from
     * @param location the float array to return the latitude and longitude coordinates through
     * @return true if latitude and longitude are successfully retrieved, false otherwise
     */
    static public boolean getExifLatLong(File exifFile, float[] location) {
        boolean result = false;
        try {
            ExifInterface exif = new ExifInterface(exifFile.getCanonicalPath());

            result = exif.getLatLong(location);

            Log.d("getExifLatLong", "Read LatLong: " + result + (result ? ": lat: " + location[0] + ": long:" + location[1] : ""));
        } catch (IOException e) {
            Log.d("getExifLatLong", "IOException: " + e.getMessage());
        }
        return result;
    }
}
