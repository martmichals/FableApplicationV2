package com.example.fableapplicationv2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.support.v7.widget.CardView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

// TODO: Manage all the activities being created, pop off repeats
public class MainActivity extends AppCompatActivity {
    private static MainActivityXMLHandler xmlHandler;
    private FirebaseAuth firebaseAuth;
    private static FirebaseUser firebaseUser;
    private FirestoreHelper firestoreHelper;

    private static ArrayList<FableUser> searchResults;
    private static ArrayList<DocumentSnapshot> intermediary;
    public static String TAG = "MainActivity";

    private Context mContext;

    //MainActivityXMLHandler xmlHandler;

    private LinearLayout mSearchResultsLinearLayout;
    private LinearLayout mSearchLinearLayout;
    private LinearLayout mFollowedLinearLayout;
    private LinearLayout mFeaturedLinearLayout;
    private TextView mRadiusTextView;
    private SearchView mSearchView;
    private TextView mFableTitle;
    private SeekBar mRadiusSeekBar;

    // Use this to get updated radius selection
    private int seekBarRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext(); // Get the application context
        mFableTitle = (TextView) findViewById(R.id.idFableTitle);
        mSearchResultsLinearLayout = (LinearLayout) findViewById(R.id.idSearchResultsLinearLayout);
        mSearchLinearLayout = (LinearLayout) findViewById(R.id.idSearchLayout);
        mFollowedLinearLayout = (LinearLayout) findViewById(R.id.idFollowedLayout);
        mFeaturedLinearLayout = (LinearLayout) findViewById(R.id.idFeaturedLayout);
        mRadiusSeekBar = (SeekBar) findViewById(R.id.radiusSeekBar);
        mRadiusTextView = (TextView) findViewById(R.id.idRadiusSeekBarLabelTextView);
        mSearchView = (SearchView) findViewById(R.id.idSearchView);

        xmlHandler = new MainActivityXMLHandler(mContext, mFableTitle, mSearchResultsLinearLayout, mSearchLinearLayout,
                mFollowedLinearLayout, mFeaturedLinearLayout, mRadiusSeekBar, mRadiusTextView, mSearchView);

        //Initializing Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        //Initializing the Firestore helper object
        firestoreHelper = new FirestoreHelper();

        //Gets the current user, updates the app state based on user
        firebaseUser = firebaseAuth.getCurrentUser();
        updateSystemState();

        //Used to debug the first searching function
        //searchForFarmersInRadius(0.50);
        //launchEditFarmerActivity();
    }


    /** IMPORTANT TO NOTE:
     *  Whenever launching the edit profile activity, you have to pack the intent with:
     *  "isEditProfileIntent" , true/false
     *
     *  This specifies the 'mode' to start the activity:
     *  true - edit the seller profile mode
     *  false - add a produce listing mode
     */
    private void launchEditFarmerActivity() {
        Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
        intent.putExtra("isEditProfileIntent", false);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Anything to run after the onCreate
    }

    //Called on button press
    public void logOffOnClick(View v) {
        firebaseAuth.signOut();
        goToSignInOptions();
    }

    //Creates intent and goes to sign in options
    private void goToSignInOptions() {
        Intent intent = new Intent(this, SignInOptionActivity.class);
        startActivity(intent);
    }

    //Checks user state, either stays on screen or launches log in process
    private void updateSystemState() {
        if (firebaseUser == null)
            goToSignInOptions();
    }

    //Method to search for farmers in a given radius
    private static void searchForFarmersInRadius(final double radius) {

        String Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference ref = FirebaseFirestore.getInstance().collection(FirestoreHelper.USER_COLLECTION).document(Uid);
        intermediary = null;

        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                final GeoPoint center = new GeoPoint(document.getDouble("coordinates.lat"),
                                        document.getDouble("coordinates.long"));

                                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                    final GPSQueryAssist queryAssist = new GPSQueryAssist();

                                    queryAssist.getLocationsInSquare(radius, center);
                                    queryAssist.setGPSQueryListener(new GPSQueryAssistListener() {
                                        @Override
                                        public void onSearchComplete() {
                                            intermediary = queryAssist.circularizeSquareResults(radius, center);
                                            convertIntermediariesToFarmers();
                                        }
                                    });
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });

    }

    //Converting the DocumentSnapshots dumped into the intermediary to Farmers
    private static void convertIntermediariesToFarmers() {
        if (intermediary != null) {
            searchResults = new ArrayList<>();
            for (DocumentSnapshot snap : intermediary) {
                FableUser user = new FableUser(snap);
                if(!(user.getUid().equals(firebaseUser.getUid()))) {
                    searchResults.add(user);
                    Log.d(TAG, user.toString());
                }else{
                    Log.d(TAG, "User found is the same as the current user");
                }
            }
            intermediary = null;
        } else {
            searchResults = null;
        }
    }

    public static void submitQuery(String aQuery, double aSeekBarRadius) {
        searchForFarmersInRadius(aSeekBarRadius);
        //xmlHandler.createCards(aQuery, searchResults);
    }

    public void searchButtonOnClick(View v) {
        // Allow the search LinearLayout to be the only visible view
        if (mSearchLinearLayout.getVisibility() != v.VISIBLE) {
            mSearchLinearLayout.setVisibility(v.VISIBLE);
            mFollowedLinearLayout.setVisibility(v.GONE);
            mFeaturedLinearLayout.setVisibility(v.GONE);
        }
    }

    public void followedButtonOnClick(View v) {
        // Allow the followed LinearLayout to be the only visible view
        if (mFollowedLinearLayout.getVisibility() != v.VISIBLE) {
            {
                mFollowedLinearLayout.setVisibility(v.VISIBLE);
                mSearchLinearLayout.setVisibility(v.GONE);
                mFeaturedLinearLayout.setVisibility(v.GONE);
            }
        }

        launchEditFarmerActivity();
    }

    public void featuredButtonOnClick(View v) {
        // Allow the featured LinearLayout to be the only visible view
//        if (mFeaturedLinearLayout.getVisibility() != v.VISIBLE) {
//            mFeaturedLinearLayout.setVisibility(v.VISIBLE);
//            mSearchLinearLayout.setVisibility(v.GONE);
//            mFollowedLinearLayout.setVisibility(v.GONE);
//        }
        // DELETE LATER
        logOffOnClick(v);
    }
}
