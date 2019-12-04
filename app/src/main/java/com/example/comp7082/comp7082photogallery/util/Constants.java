package com.example.comp7082.comp7082photogallery.util;

public class Constants {
    public static final String ROOT_PACKAGE_NAME = "com.example.comp7082.comp7082photogallery";


    public static final String EXTRA_CURRENT_INDEX = ROOT_PACKAGE_NAME + ".CURRENT_INDEX";
    public static final String EXTRA_IMAGE_DATA = ROOT_PACKAGE_NAME + ".IMAGE_DATA";
    public static final String EXTRA_KEYWORDS_TAG = ROOT_PACKAGE_NAME + ".KEYWORDS_TAG";
    public static final String EXTRA_PHOTO_LIST = ROOT_PACKAGE_NAME + ".PHOTO_LIST";

    public static final String EXTRA_SEARCH_TYPE_KEYWORD = ROOT_PACKAGE_NAME + ".KEYWORD_SEARCH";
    public static final String EXTRA_SEARCH_VALUE_KEYWORD = ROOT_PACKAGE_NAME + ".KEYWORD_VALUE";
    public static final String EXTRA_SEARCH_TYPE_DATE = ROOT_PACKAGE_NAME + ".DATE_SEARCH";
    public static final String EXTRA_SEARCH_VALUE_FROMDATE = ROOT_PACKAGE_NAME + ".FROMDATE_VALUE";
    public static final String EXTRA_SEARCH_VALUE_TODATE = ROOT_PACKAGE_NAME + ".TODATE_VALUE";
    public static final String EXTRA_SEARCH_TYPE_LOCATION = ROOT_PACKAGE_NAME + ".LOCATION_SEARCH";
    public static final String EXTRA_SEARCH_VALUE_LOCATION = ROOT_PACKAGE_NAME + ".LOCATION_VALUE";
    public static final String EXTRA_SEARCH_TYPE_GPSBOX = ROOT_PACKAGE_NAME + ".GPSBOX_SEARCH";
    public static final String EXTRA_SEARCH_VALUE_GPS_SW = ROOT_PACKAGE_NAME + ".SW_GPS_VALUE";
    public static final String EXTRA_SEARCH_VALUE_GPS_NE = ROOT_PACKAGE_NAME + ".NE_GPS_VALUE";

    public static final float MIN_FLING_DISTANCE = 200.0f;
    public static final float MAX_FLING_DISTANCE = 1000.0f;

    public static final int NAVIGATE_RIGHT = 1;
    public static final int NAVIGATE_LEFT = -1;


    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_SEARCH = 2;
    public static final int REQUEST_SET_TAG = 3;
    public static final int REQUEST_SET_CAPTION = 4;

    public static final String STORAGE_LOCATION = "/Android/data/" + ROOT_PACKAGE_NAME + "/files/Pictures/";
}
