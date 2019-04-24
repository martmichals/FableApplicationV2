package com.example.fableapplicationv2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

// TODO: Manage all the activities being created, pop off repeats
public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirestoreHelper firestoreHelper;

    private static ArrayList<Farmer> searchResults;
    private static ArrayList<DocumentSnapshot> intermediary;
    public static String TAG = "MainActivity"; //Hehe

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        //Initializing the Firestore helper object
        firestoreHelper = new FirestoreHelper();

        //Used to debug the first searching function
        searchForFarmersInRadius(0.50);
    }

    @Override
    protected void onStart(){
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
    private void goToSignInOptions(){
        Intent intent = new Intent(this, SignInOptionActivity.class);
        startActivity(intent);
    }

    //Checks user state, either stays on screen or launches log in process
    private void updateSystemState(){
        if(firebaseUser != null){
            //Stay on the current screen
        }else{
            goToSignInOptions();
        }
    }

    //Method to search for farmers in a given radius
    private void searchForFarmersInRadius(final double radius){
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

                                if(FirebaseAuth.getInstance().getCurrentUser() != null) {
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
    private void convertIntermediariesToFarmers(){
        if(intermediary != null) {
            searchResults = new ArrayList<>();
            for (DocumentSnapshot snap : intermediary) {
                Farmer farmer = new Farmer(snap);

                searchResults.add(farmer);
                Log.d(TAG, farmer.toString());
            }
            intermediary = null;
        }else{
            searchResults = null;
        }
    }
}
