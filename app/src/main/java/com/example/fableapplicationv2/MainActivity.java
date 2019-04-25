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
import android.widget.LinearLayout.LayoutParams;
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

    private static ArrayList<Farmer> searchResults;
    private static ArrayList<DocumentSnapshot> intermediary;
    public static String TAG = "MainActivity";

    private Context mContext;

    private LinearLayout mSearchResultsLinearLayout;
    private LinearLayout mSearchLinearLayout;
    private LinearLayout mFollowedLinearLayout;
    private LinearLayout mFeaturedLinearLayout;
    private TextView mRadiusTextView;
    private SearchView mSearchView;
    private TextView mFableTitle;
    private SeekBar mRadiusSeekBar;

    // Use this to get updated radius selection
    private int spinnerRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        spinnerRadius = mRadiusSeekBar.getProgress();


        mSearchView = (SearchView) findViewById(R.id.idSearchView);
        // https://android--code.blogspot.com/2015/12/android-how-to-create-cardview.html

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(mContext, query, Toast.LENGTH_SHORT).show();
                mFableTitle.setVisibility(View.GONE);
                gatherQueryResults(query);
                mSearchView.setQuery(null, false);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                spinnerRadius = progress;

                mRadiusTextView.setText(radiusLabel + progress + " mi");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        //Initializing Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        //Initializing the Firestore helper object
        firestoreHelper = new FirestoreHelper();

        //Used to debug the first searching function
        searchForFarmersInRadius(0.50);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Gets the current user, updates the app state based on user
        firebaseUser = firebaseAuth.getCurrentUser();
        updateSystemState();
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
        if (firebaseUser != null) {
            //Stay on the current screen
        } else {
            goToSignInOptions();
        }
    }

    //Method to search for farmers in a given radius
    private void searchForFarmersInRadius(final double radius) {

        String Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference ref = FirebaseFirestore.getInstance().collection(FirestoreHelper.CONSUMER_COLLECTION).document(Uid);
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
                Farmer farmer = new Farmer(snap);

                searchResults.add(farmer);
                Log.d(TAG, farmer.toString());
            }
            intermediary = null;
        } else {
            searchResults = null;
        }
    }

    // Ethan Added the Below *** Very much a work in progress
    public void gatherQueryResults(String aQuery) {
        createCards(aQuery);
    }

    public void createCards(String aQuery) {
        List<String> nameArray = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            // Initialize a new ImageView for farmer's profile picture
            ImageView profilePicture2 = new ImageView(mContext);
            profilePicture2.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.test_profile_picture));

            nameArray.add(aQuery + " " + (i + 1));
            createCard(nameArray.get(i), "Example Farm Description, We sell many greens", 20.5, profilePicture2);
        }
    }

    @SuppressLint("ResourceType")
    public void createCard(String aFarmName, String aFarmDescription, Double aDistanceAway, ImageView aProfilePicture) {
        // Initialize a new CardView
        CardView card = new CardView(mContext);

        // Initialize a new RelativeLayout
        RelativeLayout rl = new RelativeLayout(mContext);

        // Initialize new layout parameters for the CardView
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        // Set the distance between CardViews
        cardParams.bottomMargin = 10;

        // Set the layout parameters for the CardView
        card.setLayoutParams(cardParams);

        // Initialize new layout parameters for the RelativeLayout
        LinearLayout.LayoutParams rlParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        // Set the layout parameters for the RelativeLayout
        rl.setLayoutParams(rlParams);

        // Set corner radius for the CardView
        card.setRadius(10);

        // Set the content padding for the CardView
        card.setContentPadding(15, 15, 15, 15);
        card.setCardBackgroundColor(Color.parseColor("#ffffff")); // Set a background color for CardView
        card.setMaxCardElevation(15); // Set the CardView maximum elevation
        card.setCardElevation(9); // Set CardView elevation


        // Initialize a new ImageView for farmer's profile picture
        ImageView profilePicture = aProfilePicture;
        profilePicture.setId(1);
        //profilePicture.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.test_profile_picture));

        // Initialize new layout parameters for the ImageView
        RelativeLayout.LayoutParams profilePicParams = new RelativeLayout.LayoutParams(150, 150);
        profilePicParams.addRule(rl.CENTER_VERTICAL);

        profilePicture.setLayoutParams(profilePicParams);

        // Add ImageView to the relative layout
        rl.addView(profilePicture);


        LinearLayout nameAndDistanceLayout = new LinearLayout(mContext);
        nameAndDistanceLayout.setOrientation(LinearLayout.HORIZONTAL);
        nameAndDistanceLayout.setId(5);

        RelativeLayout.LayoutParams nameAndDistanceParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        nameAndDistanceParams.addRule(rl.RIGHT_OF, profilePicture.getId());

        nameAndDistanceLayout.setLayoutParams(nameAndDistanceParams);

        // Initialize a new TextView for the farm's name
        TextView farmNameTextView = new TextView(mContext);
        farmNameTextView.setId(2);
        farmNameTextView.setText(aFarmName);
        farmNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        farmNameTextView.setTextColor(Color.BLACK);

        // Initialize new layout parameters for the TextView
        //RelativeLayout.LayoutParams farmNameTextViewParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams farmNameTextViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // Position the farm name TextView to the right of the farm's profile pic ImageView
        //farmNameTextViewParams.addRule(rl.RIGHT_OF, profilePicture.getId());
        //farmNameTextViewParams.leftMargin = 50;
        //farmNameTextViewParams.gravity = Gravity.CENTER_HORIZONTAL;
        farmNameTextViewParams.gravity = Gravity.CENTER;
        //farmNameTextViewParams.gravity = Gravity.START;


        // Add farm name TextView to the RelativeLayout
        // Set the layout parameters for the farm name TextView
        //farmNameTextView.setLayoutParams(farmNameTextViewParams);
        //rl.addView(farmNameTextView);
        nameAndDistanceLayout.addView(farmNameTextView, farmNameTextViewParams);


        // Initialize a new TextView for the farm's description
        TextView distanceAwayTextView = new TextView(mContext);
        distanceAwayTextView.setId(4);
        distanceAwayTextView.setText("(" + Double.toString(aDistanceAway) + " mi)");
        distanceAwayTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        distanceAwayTextView.setTextColor(Color.GRAY);

        LinearLayout.LayoutParams distanceAwayTextViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        // Initialize new layout parameters for the frm description TextView
        //RelativeLayout.LayoutParams distanceAwayTextViewParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        // Position the farm description TextView to the right of the farm profile pic ImageView
        // and below the farm name TextView
        //distanceAwayTextViewParams.addRule(rl.RIGHT_OF, farmNameTextView.getId());
        //distanceAwayTextViewParams.addRule(rl.ABOVE, farmDescriptionTextView.getId());
        //distanceAwayTextViewParams.addRule(rl.ALIGN_PARENT_RIGHT);
        // Center horizontal
        //distanceAwayTextViewParams.leftMargin = 100;

        // Add farm description TextView to the RelativeLayout
        // Set the layout parameters for the farm description TextView
        //rl.addView(distanceAwayTextView, distanceAwayTextViewParams);
        distanceAwayTextViewParams.gravity = Gravity.CENTER_HORIZONTAL;
        //distanceAwayTextViewParams.gravity = Gravity.END;

        nameAndDistanceLayout.addView(distanceAwayTextView, distanceAwayTextViewParams);


        rl.addView(nameAndDistanceLayout);


        // Initialize a new TextView for the farm's description
        TextView farmDescriptionTextView = new TextView(mContext);
        farmDescriptionTextView.setId(3);
        farmDescriptionTextView.setText(aFarmDescription);
        farmDescriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8);
        farmDescriptionTextView.setTextColor(Color.BLACK);

        // Initialize new layout parameters for the frm description TextView
        RelativeLayout.LayoutParams farmDescriptionTextViewParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // Position the farm description TextView to the right of the farm profile pic ImageView
        // and below the farm name TextView
        farmDescriptionTextViewParams.addRule(rl.RIGHT_OF, profilePicture.getId());
        farmDescriptionTextViewParams.addRule(rl.BELOW, nameAndDistanceLayout.getId());
        farmDescriptionTextViewParams.leftMargin = 50;

        // Add farm description TextView to the RelativeLayout
        // Set the layout parameters for the farm description TextView
        rl.addView(farmDescriptionTextView, farmDescriptionTextViewParams);


        // Add the RelativeLayout to the card
        card.addView(rl);

        // Set an onclick listener for the card, increment
        // each card using a parameter with numbers generated by loop in previous method


        // Add the CardView in Search LinearLayout
        mSearchResultsLinearLayout.addView(card);
    }

    public void searchButtonOnClick(View v) {
        // Allow the search LinearLayout to be the only visible view
        mSearchLinearLayout.setVisibility(v.VISIBLE);
        mFollowedLinearLayout.setVisibility(v.GONE);
        mFeaturedLinearLayout.setVisibility(v.GONE);
    }

    public void followedButtonOnClick(View v) {
        // Allow the followed LinearLayout to be the only visible view
        mFollowedLinearLayout.setVisibility(v.VISIBLE);
        mSearchLinearLayout.setVisibility(v.GONE);
        mFeaturedLinearLayout.setVisibility(v.GONE);
    }

    public void featuredButtonOnClick(View v) {
        // Allow the featured LinearLayout to be the only visible view
        // mFeaturedLinearLayout.setVisibility(v.VISIBLE);
        // mSearchLinearLayout.setVisibility(v.GONE);
        // mFollowedLinearLayout.setVisibility(v.GONE);

        // DELETE LATER
        logOffOnClick(v);
    }

}
