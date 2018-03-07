package com.androidbootcamp.androidtemplate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;



/*
    @see https://developer.android.com/reference/android/media/ExifInterface.html
    http://codetheory.in/android-pick-select-image-from-gallery-with-intents/
    https://android-developers.googleblog.com/2016/12/introducing-the-exifinterface-support-library.html
*/
public class UIEvents01Activity extends AppCompatActivity implements View.OnClickListener {

    Button btnChange;
    ImageView imageView;
    int RESULT_LOAD_IMG = 1254;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uievents01);
        loadUI();
    }

    void loadUI() {
        btnChange = findViewById(R.id.button);
        btnChange.setOnClickListener(this);
        imageView = findViewById(R.id.imageView);
    }

    @Override
    public void onClick(View v) {
        if (v == btnChange)
            changePhoto();
    }

    private void changePhoto() {
        if (!checkPermission())
            return;
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            Uri uri = data.getData();
            String[] projection = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();

            Log.d("CONSOLE", DatabaseUtils.dumpCursorToString(cursor));

            int columnIndex = cursor.getColumnIndex(projection[0]);
            String picturePath = cursor.getString(columnIndex); // returns null
            cursor.close();

            Log.d("CONSOLE", " picturePath "+picturePath);

            ExifInterface exifInterface= exifInterfaceByUri(uri);
            int rotation= rotationByExifInterface(exifInterface);

            Log.d("CONSOLE", " exifInterface "+exifInterface);
            Log.d("CONSOLE", " rotation "+rotation);

            Bitmap bitmap= rotateBitmap(bitmapByUri(uri),rotation);
            if(bitmap!=null){
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == RESULT_LOAD_IMG) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Accepted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMG);
            } else {
                Log.e("value", "Permission Denied, You cannot use local drive .");
            }
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        Toast.makeText(this, String.valueOf(result), Toast.LENGTH_LONG).show();
        Toast.makeText(this, String.valueOf(PackageManager.PERMISSION_GRANTED), Toast.LENGTH_LONG).show();
        if (result == PackageManager.PERMISSION_GRANTED) {

            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RESULT_LOAD_IMG);
            return false;
        }
    }


    private int rotationByExifInterface(ExifInterface exifInterface){
        int rotation = 0;
        if(exifInterface==null) return rotation;

        int orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
        }
        return rotation;
    }

    private ExifInterface exifInterfaceByUri(Uri uri){
        // the URI you've received from the other app
        InputStream in=null;
        ExifInterface exifInterface=null;
        try {
            in = getContentResolver().openInputStream(uri);
            exifInterface= new ExifInterface(in);
            // Now you can extract any Exif tag you want
            // Assuming the image is a JPEG or supported raw format
        } catch (IOException e) {
            // Handle any errors
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {}
            }
        }

        return exifInterface;
    }

    private Bitmap bitmapByUri(Uri uri){
        InputStream imageStream=null;
        Bitmap  bitmap=null;
        try {
            imageStream= getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(imageStream!=null){
            bitmap= BitmapFactory.decodeStream(imageStream);
        }
        return bitmap;

    }
    //https://gist.github.com/codeswimmer/858833
    //https://teamtreehouse.com/community/how-to-rotate-images-to-the-correct-orientation-portrait-by-editing-the-exif-data-once-photo-has-been-taken
    public Bitmap rotateBitmap(Bitmap original, float degrees) {
        int width = original.getWidth();
        int height = original.getHeight();

        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);

        // create a new bitmap from the original using the matrix to transform the result
        Bitmap rotatedBitmap = Bitmap.createBitmap(original , 0, 0, width ,height, matrix, true);

        return rotatedBitmap;
    }
}
