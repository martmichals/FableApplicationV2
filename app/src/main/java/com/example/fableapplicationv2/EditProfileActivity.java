package com.example.fableapplicationv2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private boolean editProfileState;

    //Edit profile elements
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

    //Add listing elements
    private EditText produceNameEditText;
    private EditText produceDescriptionEditText;
    private EditText producePriceEditText;
    private TextView producePriceLabelTextView;
    private Button addListingButton;
    private Button cancelButton;

    public void onImageSearch(View v){
        Intent intent  = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    public void onDoneEditingButtonPress(View v){
        final boolean validSlogan = DataVerification.checkSellerSlogan(sloganEditText.getText().toString());
        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection(FirestoreHelper.USER_COLLECTION)
                .document(FirebaseAuth.getInstance().getUid());
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            FableUser user = new FableUser(document);

                            if(validSlogan){
                                sloganErrorTextView.setVisibility(View.GONE);
                                uploadingProgressBar.setVisibility(View.VISIBLE);
                                helper.writeSellerProfile(imageBitmap, sloganEditText.getText().toString(),
                                        descriptionEditText.getText().toString(), user,
                                        new FirestoreHelperListener() {
                                            @Override
                                            public void onSuccessfulRequestComplete() {
                                                uploadingProgressBar.setVisibility(View.GONE);
                                                Toast.makeText(EditProfileActivity.this, "Updated profile successfully",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                            @Override
                                            public void onFailedRequest() {
                                                uploadingProgressBar.setVisibility(View.GONE);
                                                Toast.makeText(EditProfileActivity.this, "Failed to update profile",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }else{
                                sloganErrorTextView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d(TAG, "Getting the user failed", task.getException());
                        }
                    }
                });

    }

    public void onDoneAddingListingPress(View v){

    }

    public void onCancelButtonPress(View v){

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
        doneEditingButton.setVisibility(code);

        if(code == View.VISIBLE)
            titleTextView.setText(getString(R.string.editProfileTitle));
    }

    /** Method to set the visibility of all create produce listing elements
     * @param code : View code, i.e. View.GONE, View.VISIBLE
     */
    private void setListingCreatorVisibility(int code){
        produceNameEditText.setVisibility(code);
        produceDescriptionEditText.setVisibility(code);
        producePriceEditText.setVisibility(code);
        producePriceLabelTextView.setVisibility(code);
        addListingButton.setVisibility(code);
        cancelButton.setVisibility(code);

        if(code == View.VISIBLE)
            titleTextView.setText(getString(R.string.createProduceListingTitle));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Edit profile elements
        profilePictureImageView = findViewById(R.id.idFarmerProfileImage);
        sloganEditText = findViewById(R.id.idSloganEditText);
        sloganErrorTextView = findViewById(R.id.idSloganError);
        descriptionEditText = findViewById(R.id.idShortDescriptionEditText);
        uploadingProgressBar = findViewById(R.id.idUploadingProgressBar);
        doneEditingButton = findViewById(R.id.idDoneEditingButton);
        produceListingTitleTextView = findViewById(R.id.idProduceListingTitle);
        titleTextView = findViewById(R.id.idProfileTitle);

        //Add produce listing elements
        produceNameEditText = findViewById(R.id.idProduceNameEditText);
        produceDescriptionEditText = findViewById(R.id.idProduceDescriptionEditText);
        producePriceEditText = findViewById(R.id.idPriceEditText);
        producePriceLabelTextView = findViewById(R.id.idProducePriceLabel);
        addListingButton = findViewById(R.id.idDoneAddingListing);
        cancelButton = findViewById(R.id.idCancelButton);

        helper = new FirestoreHelper();

        Bundle extras = getIntent().getExtras();
        editProfileState = extras.getBoolean("isEditProfileIntent", true);
        changeActivityState(editProfileState);
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
