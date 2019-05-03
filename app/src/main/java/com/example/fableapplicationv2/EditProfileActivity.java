package com.example.fableapplicationv2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private boolean editProfileState;

    private Uri mImageUri;
    private Bitmap imageBitmap;
    private FirestoreHelper helper;
    private ImageView profilePictureImageView;
    private EditText sloganEditText;
    private EditText descriptionEditText;
    private TextView sloganErrorTextView;
    private ProgressBar uploadingProgressBar;
    private Button doneEditingButton;
    private TextView produceListingTitleTextView;
    private TextView titleTextView;

    public void onImageSearch(View v){
        Intent intent  = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    public void onDoneEditingButtonPress(View v){
        Log.d(TAG,"Running with a " + !editProfileState + " value passed");
        changeActivityState(!editProfileState);

//        boolean validSlogan = DataVerification.checkSellerSlogan(sloganEditText.getText().toString());
//        if(validSlogan){
//            sloganErrorTextView.setVisibility(View.GONE);
//            uploadingProgressBar.setVisibility(View.VISIBLE);
//            helper.writeSellerProfile(imageBitmap, sloganEditText.getText().toString(),
//                                      descriptionEditText.getText().toString(), new FirestoreHelperListener() {
//                        @Override
//                        public void onSuccessfulRequestComplete() {
//                            uploadingProgressBar.setVisibility(View.GONE);
//                            Toast.makeText(EditProfileActivity.this, "Updated profile successfully",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                        @Override
//                        public void onFailedRequest() {
//                            uploadingProgressBar.setVisibility(View.GONE);
//                            Toast.makeText(EditProfileActivity.this, "Failed to update profile",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        }else{
//            sloganErrorTextView.setVisibility(View.VISIBLE);
//        }
    }

    private void changeActivityState(boolean state){
        if(state){
            editProfileState = true;
            setEditProfileVisibility(View.VISIBLE);
            setListingCreatorVisibility(View.GONE);
        }else{
            editProfileState = false;
            setEditProfileVisibility(View.GONE);
            setListingCreatorVisibility(View.VISIBLE);
        }
    }

    /** Method to set the visibility of all the edit profile elements
     * @param code : View code, i.e. View.GONE, View.VISIBLE
     */
    private void setEditProfileVisibility(int code){
        profilePictureImageView.setVisibility(code);
        sloganEditText.setVisibility(code);
        descriptionEditText.setVisibility(code);
        uploadingProgressBar.setVisibility(View.GONE);
        produceListingTitleTextView.setVisibility(code);

        if(code == View.VISIBLE)
            titleTextView.setText(getString(R.string.editProfileTitle));
    }

    /** Method to set the visibility of all create produce listing elements
     * @param code : View code, i.e. View.GONE, View.VISIBLE
     */
    private void setListingCreatorVisibility(int code){
        if(code == View.VISIBLE)
            titleTextView.setText(getString(R.string.createProduceListingTitle));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profilePictureImageView = findViewById(R.id.idFarmerProfileImage);
        sloganEditText = findViewById(R.id.idSloganEditText);
        sloganErrorTextView = findViewById(R.id.idSloganError);
        descriptionEditText = findViewById(R.id.idShortDescriptionEditText);
        uploadingProgressBar = findViewById(R.id.idUploadingProgressBar);
        doneEditingButton = findViewById(R.id.idDoneEditingButton);
        produceListingTitleTextView = findViewById(R.id.idProduceListingTitle);
        titleTextView = findViewById(R.id.idProfileTitle);

        helper = new FirestoreHelper();

        // Get rid of this line, set to intent asking
        setEditProfileVisibility(View.VISIBLE);
        editProfileState = true;
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
            imageBitmap = resizedImage;
            profilePictureImageView.setImageBitmap(resizedImage);
        }
    }
}
