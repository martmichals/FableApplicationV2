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
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirestoreHelper firestoreHelper;

    private static ArrayList<FableUser> searchResults;
    private static ArrayList<DocumentSnapshot> intermediary;
    private static boolean isUserAFarmer;
    public static String TAG = "MainActivity";

    private Context mContext;

    MainActivityXMLHandler xmlHandler;

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

        xmlHandler = new MainActivityXMLHandler();

        executeAdditionalSetup();

        //Initializing Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        //Initializing the Firestore helper object
        firestoreHelper = new FirestoreHelper();

        //Gets the current user, updates the app state based on user
        firebaseUser = firebaseAuth.getCurrentUser();
        updateSystemState();

        // TODO : Create a method to check if the user is a farmer or a consumer

        //Used to debug the first searching function
        searchForFarmersInRadius(0.50);
        //launchEditFarmerActivity();
    }

    private void launchEditFarmerActivity() {
        Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
        startActivity(intent);
    }

    private void executeAdditionalSetup() {
        // Get the application context
        mContext = getApplicationContext();

        mFableTitle = (TextView) findViewById(R.id.idFableTitle);

        // Get the widget references from XML layout
        mSearchResultsLinearLayout = (LinearLayout) findViewById(R.id.idSearchResultsLinearLayout);
        mSearchLinearLayout = (LinearLayout) findViewById(R.id.idSearchLayout);
        mFollowedLinearLayout = (LinearLayout) findViewById(R.id.idFollowedLayout);
        mFeaturedLinearLayout = (LinearLayout) findViewById(R.id.idFeaturedLayout);

        final String radiusLabel = "Radius: ";
        mRadiusSeekBar = (SeekBar) findViewById(R.id.radiusSeekBar);
        mRadiusTextView = (TextView) findViewById(R.id.idRadiusSeekBarLabelTextView);
        mRadiusTextView.setText(radiusLabel + mRadiusSeekBar.getProgress() + " mi");
        seekBarRadius = mRadiusSeekBar.getProgress();


        mSearchView = (SearchView) findViewById(R.id.idSearchView);
        // https://android--code.blogspot.com/2015/12/android-how-to-create-cardview.html

        xmlHandler.handleXML(mContext, mFableTitle, mSearchResultsLinearLayout, mSearchLinearLayout,
                mFollowedLinearLayout, mFeaturedLinearLayout, mRadiusSeekBar, mRadiusTextView,
                seekBarRadius, mSearchView);
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
    private void searchForFarmersInRadius(final double radius) {

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
    private void convertIntermediariesToFarmers() {
        if (intermediary != null) {
            searchResults = new ArrayList<>();
            for (DocumentSnapshot snap : intermediary) {
                FableUser user = new FableUser(snap);

                searchResults.add(user);
                Log.d(TAG, user.toString());
            }
            intermediary = null;
        } else {
            searchResults = null;
        }
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

    //TODO : Create a method to order Farmer search results based on their distance from the user
    //TODO : Create a method to order Farmer search results based their review ratings
}
