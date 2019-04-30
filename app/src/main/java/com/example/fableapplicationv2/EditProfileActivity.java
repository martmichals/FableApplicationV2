package com.example.fableapplicationv2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    private Uri mImageUri;
    private ImageView profilePictureImageView;

    public void onImageSearch(View v){
        Intent intent  = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profilePictureImageView = findViewById(R.id.idFarmerProfileImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Running the onActivityResult method");

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.getData() != null){
            mImageUri = data.getData();
            Bitmap bitmap;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
            } catch (IOException e) {
                Log.e(TAG, "Exception encountered when trying to load image bitmap");
                bitmap = null;
            }

            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();

            Bitmap resizedImage;
            if(imageWidth < imageHeight){
                resizedImage = Bitmap.createBitmap(bitmap, 0, 0, imageWidth, imageWidth);
            }else{
                resizedImage = Bitmap.createBitmap(bitmap, 0, 0, imageHeight, imageHeight);
            }
            profilePictureImageView.setImageBitmap(resizedImage);
        }
    }
}
