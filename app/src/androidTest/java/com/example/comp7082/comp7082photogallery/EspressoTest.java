package com.example.comp7082.comp7082photogallery;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.v4.graphics.drawable.DrawableCompat;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import com.example.comp7082.comp7082photogallery.androidos.ExifUtility;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EspressoTest {
    @Rule public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);
    @Rule public GrantPermissionRule mRuntimeWritePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    @Rule public GrantPermissionRule mRuntimeReadPermissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);
    @Rule public GrantPermissionRule mRuntimeLocationPermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    @Test
    public void A_CreateImage(){
        String directoryString = activityRule.getActivity().getPhotoFileManagerForTest().getPhotoLocation();
        OutputStream fOutputStream;

        try {
            Drawable drawable = activityRule.getActivity().getDrawable(R.drawable.ic_camera);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
            }

            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            File directory = new File(directoryString);
            if(!directory.exists()){
                directory.mkdirs();
            }

            File file = createImageFile();
            if (file.exists()) {
                file.delete();
            }

            fOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);

            fOutputStream.flush();
            fOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void B_ensureCaptionWorks() {
        onView(withId(R.id.button_caption_id)).perform(click());
        onView(withId(R.id.contentDetailsEditText)).perform(typeText("camera icon test"), closeSoftKeyboard());
        onView(withId(R.id.contentSetButton)).perform(click());
        onView(withId(R.id.currentImageCaptionTextView)).check(matches(withText("camera icon test")));
    }

    @Test
    public void C_ensureTagsWorks() {
        //String currentPhotoPath = activityRule.getActivity().currentPhotoPath;
        String currentPhotoPath = activityRule.getActivity().getPhotoFileManagerForTest().getCurrentFilePath();
        onView(withId(R.id.button_tag_id)).perform(click());
        onView(withId(R.id.contentDetailsEditText)).perform(typeText("camera icon"), closeSoftKeyboard());
        onView(withId(R.id.contentSetButton)).perform(click());

        File localFile = new File(currentPhotoPath);
        String fileKeywordTags = ExifUtility.getExifTagString(localFile, ExifUtility.EXIF_KEYWORDS_TAG);
        fileKeywordTags.matches("camera icon");
    }

    @Test
    public void D_CreateSecondImage(){
        String directoryString = activityRule.getActivity().getPhotoFileManagerForTest().getPhotoLocation();
        OutputStream fOutputStream;

        try {
            Drawable drawable = activityRule.getActivity().getDrawable(R.drawable.ic_launcher_background);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
            }

            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            File directory = new File(directoryString);
            if(!directory.exists()){
                directory.mkdirs();
            }

            File file = createImageFile();
            if (file.exists()) {
                file.delete();
            }

            fOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);

            fOutputStream.flush();
            fOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void E_ensureTagSearchWorks(){
        onView(withId(R.id.openSearchButton)).perform(click());
        onView(withId(R.id.tagSearchEditText)).perform(typeText("camera"), closeSoftKeyboard());
        onView(withId(R.id.searchButton)).perform(click());
        BitmapDrawable drawable = (BitmapDrawable) activityRule.getActivity().imageView.getDrawable();
        Bitmap imageViewBitmap = drawable.getBitmap();
        imageViewBitmap.sameAs(activityRule.getActivity().bitmap);
    }

    @Test
    public void F_ensureTagSearchCountWorks(){
        onView(withId(R.id.openSearchButton)).perform(click());
        onView(withId(R.id.tagSearchEditText)).perform(typeText("camera"), closeSoftKeyboard());
        onView(withId(R.id.searchButton)).perform(click());
//        int filenamesCount = activityRule.getActivity().filenames.length;
        int filenamesCount = activityRule.getActivity().getPhotoFileManagerForTest().getPhotoList().length;

        assertEquals(filenamesCount, 1);
    }

    @Test
    public void G_ensureDateSearchCountWorks(){
        onView(withId(R.id.openSearchButton)).perform(click());
        onView(withId(R.id.fromDateEditText)).perform(typeText("2019/10/01"), closeSoftKeyboard());
        onView(withId(R.id.toDateEditText)).perform(typeText("2019/10/31"), closeSoftKeyboard());
        onView(withId(R.id.searchButton)).perform(click());
//        int filenamesCount = activityRule.getActivity().filenames.length;
        int filenamesCount = activityRule.getActivity().getPhotoFileManagerForTest().getPhotoList().length;
        assertEquals(filenamesCount, 2);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activityRule.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }
}
