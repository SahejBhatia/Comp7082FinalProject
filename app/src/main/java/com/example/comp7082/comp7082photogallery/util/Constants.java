package com.example.comp7082.comp7082photogallery.util;

public class Constants {
    public static final String ROOT_PACKAGE_NAME = "com.example.comp7082.comp7082photogallery";


    public static final String EXTRA_CURRENT_INDEX = ROOT_PACKAGE_NAME + ".CURRENT_INDEX";
    public static final String EXTRA_KEYWORDS_TAG = ROOT_PACKAGE_NAME + ".KEYWORDS_TAG";
    public static final String EXTRA_PHOTO_LIST = ROOT_PACKAGE_NAME + ".PHOTO_LIST";

    public static final float MIN_FLING_DISTANCE = 200.0f;
    public static final float MAX_FLING_DISTANCE = 1000.0f;

    public static final int NAVIGATE_RIGHT = 1;
    public static final int NAVIGATE_LEFT = -1;


    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_SEARCH = 2;
    public static final int REQUEST_SET_TAG = 3;

    public static final String STORAGE_LOCATION = "/Android/data/" + ROOT_PACKAGE_NAME + "/files/Pictures/";
}
