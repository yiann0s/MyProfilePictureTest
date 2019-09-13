package com.example.myprofilepicturetest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button takePictureButton;
    private ImageView topIcon;
    PermissionListener permissionlistener;
    Uri imageUri;
    static final int REQUEST_TAKE_PHOTO = 1;

    final String TAG = "TEST.MainActivity";

    public static final String MyPREFERENCES = "MyPrefs" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        topIcon = findViewById(R.id.topicon);
        String imagePath = loadDataFromSharedPreferences();
        if ( imagePath != null ) {
            Glide.
                    with(this).
                    load(decodeBase64(imagePath)).
                    placeholder(R.drawable.user).
                    into(topIcon);
        }

        permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                dispatchTakePictureIntent();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }


        };

        takePictureButton = findViewById(R.id.takePictureButton);
        takePictureButton.setOnClickListener(this);

    }

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d(TAG,"DispatchTakePictureIntent error " + ex.getLocalizedMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "MyPicture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "Photo taken on " + System.currentTimeMillis());
                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            } else {
                Log.d(TAG,"DispatchTakePictureIntent photoFile is null ");
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if ((requestCode == REQUEST_TAKE_PHOTO) && (resultCode == RESULT_OK)) {
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                saveDataToSharedPreferences(encodeToBase64(bitmap, Bitmap.CompressFormat.JPEG, 60));
                Glide.with(this).load(bitmap).into(topIcon);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // method for base64 to bitmap
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private void saveDataToSharedPreferences(String myBitmap){
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("imagePreference", myBitmap);
        editor.apply();
    }

    private String loadDataFromSharedPreferences(){
        SharedPreferences prefs = this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString("imagePreference", null);
    }

    @Override
    public void onClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TedPermission.with(MainActivity.this)
                    .setPermissionListener(permissionlistener)
                    .setGotoSettingButton(false)
                    .setPermissions(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                    .check();
        } else {
            TedPermission.with(MainActivity.this)
                    .setPermissionListener(permissionlistener)
                    .setGotoSettingButton(false)
                    .setPermissions(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .check();
        }
    }

}
