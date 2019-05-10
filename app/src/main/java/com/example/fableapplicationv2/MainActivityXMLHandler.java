package com.example.fableapplicationv2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.sip.SipSession;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.fableapplicationv2.MainActivity.submitQuery;

public class MainActivityXMLHandler extends AppCompatActivity {
    private Context mContext;
    private LinearLayout mSearchResultsLinearLayout;
    private LinearLayout mSearchLinearLayout;
    private LinearLayout mFollowedLinearLayout;
    private LinearLayout mFeaturedLinearLayout;
    private TextView mRadiusTextView;
    private SearchView mSearchView;
    private TextView mFableTitle;
    private SeekBar mRadiusSeekBar;
    final String radiusLabel = "Radius: ";
    private double seekBarRadius; // Use this to store updated radius selection
    private List<CardView> currentCardsList;

    public MainActivityXMLHandler(Context aContext, TextView aFableTitle, LinearLayout aSearchResultsLinearLayout,
                                  LinearLayout aSearchLinearLayout, LinearLayout aFollowedLinearLayout,
                                  LinearLayout aFeaturedLinearLayout, SeekBar aRadiusSeekBar,
                                  TextView aRadiusTextView, SearchView aSearchView) {
        // Get the application context
        mContext = aContext;
        mFableTitle = aFableTitle;

        currentCardsList = new ArrayList<CardView>();

        // Get the widget references from XML layout
        mSearchResultsLinearLayout = aSearchResultsLinearLayout;
        mSearchLinearLayout = aSearchLinearLayout;
        mFollowedLinearLayout = aFollowedLinearLayout;
        mFeaturedLinearLayout = aFeaturedLinearLayout;
        mSearchView = aSearchView;
        mRadiusSeekBar = aRadiusSeekBar;
        mRadiusTextView = aRadiusTextView;
        mRadiusTextView.setText(radiusLabel + mRadiusSeekBar.getProgress() + " mi");
        seekBarRadius = mRadiusSeekBar.getProgress();

        setListeners();
    }


    public void setListeners() {

        // https://android--code.blogspot.com/2015/12/android-how-to-create-cardview.html

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(mContext, query, Toast.LENGTH_SHORT).show();
                mFableTitle.setVisibility(View.GONE);

                mSearchResultsLinearLayout.removeAllViews();
                currentCardsList = new ArrayList<CardView>();
                //submitQuery(query, seekBarRadius);
                //gatherQueryResults(query);
                createCards(query);
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
                seekBarRadius = progress;

                mRadiusTextView.setText(radiusLabel + progress + " mi");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //public void createCards(String aQuery, ArrayList<FableUser> aSearchResults) {
    public void createCards(String aQuery) {
        List<String> nameArray = new ArrayList<>();

        //for (int i = 0; i < aSearchResults.size(); i++) {
        for (int i = 0; i < 2; i++) {
            // Initialize a new ImageView for farmer's profile picture
            ImageView profilePicture = new ImageView(mContext);
            profilePicture.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.test_profile_picture));

            //createCard(aSearchResults.get(i).getFirstName(), "Example Farm Description, We sell many greens", 20.5, profilePicture);
            createCard(aQuery, "Example Farm Description, We sell many greens", seekBarRadius, profilePicture);
        }

        for (int j = 0; j < currentCardsList.size(); j++) {
            mSearchResultsLinearLayout.addView(currentCardsList.get(j));
        }
    }

//    public void gatherQueryResults(String aQuery) {
//        createCards(aQuery);
//    }

    // https://android--code.blogspot.com/2015/12/android-how-to-create-cardview.html

    @SuppressLint("ResourceType")
    public void createCard(final String aFarmName, String aFarmDescription, final Double aDistanceAway, ImageView aProfilePicture) {
        // Initialize a new CardView
        CardView card = new CardView(mContext);

        card.setClickable(true);


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
        farmNameTextViewParams.gravity = Gravity.CENTER_VERTICAL;
        //farmNameTextViewParams.gravity = Gravity.START;


        // Add farm name TextView to the RelativeLayout
        // Set the layout parameters for the farm name TextView
        //farmNameTextView.setLayoutParams(farmNameTextViewParams);
        //rl.addView(farmNameTextView);


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
        distanceAwayTextViewParams.gravity = Gravity.CENTER_VERTICAL;
        distanceAwayTextViewParams.leftMargin = 20;
        //distanceAwayTextViewParams.gravity = Gravity.END;
        //distanceAwayTextViewParams.gravity = Gravity.END;
        nameAndDistanceLayout.addView(farmNameTextView, farmNameTextViewParams);
        nameAndDistanceLayout.addView(distanceAwayTextView, distanceAwayTextViewParams);
        nameAndDistanceParams.leftMargin = 50;

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

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRadiusTextView.setText(aFarmName);
            }
        });

        // Set an onclick listener for the card, increment
        // each card using a parameter with numbers generated by loop in previous method


        currentCardsList.add(card);
        // Add the CardView in Search LinearLayout
        //mSearchResultsLinearLayout.addView(card);
    }

}
