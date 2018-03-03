package com.androidbootcamp.androidtemplate;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class UIEvents01Activity extends BaseActivity implements View.OnClickListener {

    Button btnChange;
    ImageView imageView;
    int RESULT_LOAD_IMG = 1254;
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uievents01);
        //enabledBack();
        loadUI();
    }

    void loadUI() {
        btnChange = (Button) findViewById(R.id.button);
        btnChange.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.imageView);
        ctx = this;
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

    /* ============= Load image with Picasso ============= */
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMG) {
                Uri selectedImageURI = data.getData();
                Picasso.with(ctx).load(selectedImageURI).noPlaceholder().centerCrop().fit()
                        .into((ImageView) findViewById(R.id.imageView));
            }

        }
    }*/

    /* ============= Load image without libraries (Action pick) ============= */
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
            } catch (Exception e) {
                Log.e("JCHOY", e.getMessage());
                Toast.makeText(ctx, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }*/


    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                if (imageUri != null) {
                    MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), imageUri);
                    Log.i("JCHOY", imageUri.getScheme());
                    if (imageUri.getScheme().equals("content")) {
                        Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String file_name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            Toast.makeText(this, file_name, Toast.LENGTH_SHORT).show();
                            String[] filePathColumn = {MediaStore.Images.Media.DATA};
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            String picturePath = cursor.getString(columnIndex);
                            Log.i("JCHOY", picturePath);
                            Log.i("JCHOY", file_name);
                            Toast.makeText(this, file_name, Toast.LENGTH_SHORT).show();
                        }
                    }
                }


                //Load image with File Provider
                File imagePath = new File(ctx.getFilesDir(), "external_files");
                File newFile = new File(imagePath, imageUri.getPath());
                Uri photoURI = FileProvider.getUriForFile(this, "com.androidbootcamp.androidtemplate.provider", newFile);
                Log.i("JCHOY", photoURI.getPath());
                Log.i("JCHOY", imageUri.toString());


                //final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                // final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                //imageView.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                Log.e("JCHOY", e.getMessage());
                Toast.makeText(ctx, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.e("JCHOY", e.getMessage());
                Toast.makeText(ctx, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e("JCHOY", e.getMessage());
                Toast.makeText(ctx, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Error al acceder a la libreria", Toast.LENGTH_LONG).show();
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
        int result = ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE);
        Toast.makeText(ctx, String.valueOf(result), Toast.LENGTH_LONG).show();
        Toast.makeText(ctx, String.valueOf(PackageManager.PERMISSION_GRANTED), Toast.LENGTH_LONG).show();
        if (result == PackageManager.PERMISSION_GRANTED) {

            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RESULT_LOAD_IMG);
            return false;
        }
    }

}
