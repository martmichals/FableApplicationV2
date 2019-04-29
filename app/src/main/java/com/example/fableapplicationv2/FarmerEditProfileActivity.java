package com.example.fableapplicationv2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class FarmerEditProfileActivity extends AppCompatActivity {
    private static final String TAG = "FarmerEditProfile";
    private static final int PICK_IMAGE_REQUEST = 1;

    private Uri mImageUri;
    private ImageView profilePictureImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_edit_profile);

        profilePictureImageView = findViewById(R.id.idFarmerProfileImage);
    }

    public void onImageSearch(View v){
        Intent intent  = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // TODO : Work here
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Running the onActivityResult method");

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.getData() != null){
            mImageUri = data.getData();

            profilePictureImageView.setImageURI(mImageUri);
            Log.d(TAG, "Loaded image from user into the ImageView");
        }
    }
}
