# comp7082

EXIF NOTES

List of Exif Tags: https://sno.phy.queensu.ca/~phil/exiftool/TagNames/EXIF.html

Writing to a Tag
* https://stackoverflow.com/questions/8851425/exif-data-in-jpeg-file
* https://stackoverflow.com/questions/15506862/android-create-custom-exif-attributes-for-an-image-file
* After file is created; after camera has finished (onActivityResult)

    // exif data test
    String mString = "writing a test message";
    ExifInterface exif = null;
    try {
        exif = new ExifInterface(currentPhotoPath);
        exif.setAttribute("UserComment", mString); // or "ImageDescription"
        exif.saveAttributes();
    } catch (IOException e) {
        e.printStackTrace();
    }
    // end test

Reading from a Tag
* placed this in DisplayPhoto as a test

    //exif test
    try {
        ExifInterface exif = new ExifInterface(path);
        String msg = exif.getAttribute(ExifInterface.TAG_USER_COMMENT); // resolves to a String
        if (msg == null) { msg = "*null*"; }
        Log.d("displayPhoto", "UserComment: " + msg);
    } catch (IOException e) {
        e.printStackTrace();
    }
    // end test
