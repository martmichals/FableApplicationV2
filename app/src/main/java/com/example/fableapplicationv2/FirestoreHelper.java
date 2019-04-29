package com.example.fableapplicationv2;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirestoreHelper {
    public final static String TAG = "Firestore Helper";
    public final static String USER_COLLECTION = "users";

    public final static String NAME_KEY = "name";
    public final static String ADDRESS_KEY = "address";
    public static final String GPS_COORDINATE_KEY = "coordinates";

    //Tags for common user keys
    public static final String FIRST_KEY = "first";
    public static final String LAST_KEY = "last";
    public static final String STREET_ADDRESS_KEY = "street";
    public static final String CITY_KEY = "city";
    public static final String STATE_KEY = "state";
    public static final String ZIP_CODE_KEY = "zip";
    public static final String PHONE_NUMBER_KEY = "phone";
    public static final String EMAIL_KEY = "email";
    public static final String GPS_LATITUDE_KEY = "lat";
    public static final String GPS_LONGITUDE_KEY = "long";

    //Firebase objects
    private FirebaseFirestore database;
    private FirebaseUser user;

    private FirestoreHelperListener listener;

    public FirestoreHelper(){
        database = FirebaseFirestore.getInstance();

        FirebaseAuth myAuth = FirebaseAuth.getInstance();
        user = myAuth.getCurrentUser();
        listener = null;
    }

    /** Method in order to add a new consumer to the database
     * @param firstName : first name of the consumer
     * @param lastName : last name of the consumer
     * @param streetAddress : street address of the consumer
     * @param city : city of the consumer
     * @param zipCode : zip code of the consumer
     * @param phoneNumber : phone number of the consumer
     * @param state : state of residence for the consumer
     * @param email : email of the consumer
     * @param context : context from which the method is called from
     */
    public void addNewUser(String firstName, String lastName, String streetAddress, String city,
                               String zipCode, String phoneNumber, String state, String email,
                               Context context) {
        Map<String, Object> consumerDataDoc = new HashMap<>();
        String Uid = user.getUid();

        Map<String, Object> nestedName = new HashMap<>();
        nestedName.put(FIRST_KEY, firstName);
        nestedName.put(LAST_KEY, lastName);
        consumerDataDoc.put(NAME_KEY, nestedName);

        Map<String, Object> nestedAddress = new HashMap<>();
        nestedAddress.put(STREET_ADDRESS_KEY, streetAddress);
        nestedAddress.put(CITY_KEY, city);
        nestedAddress.put(STATE_KEY, state);
        nestedAddress.put(ZIP_CODE_KEY, zipCode);
        consumerDataDoc.put(ADDRESS_KEY, nestedAddress);

        consumerDataDoc.put(PHONE_NUMBER_KEY, phoneNumber);
        consumerDataDoc.put(EMAIL_KEY, email);

        // Attempts to convert address to GPS coordinates
        double[] coordinates = getGPSCoordinatesFromAddress(context, (streetAddress + ", " + city
                                                            + ", " + state + ", " + zipCode));
        Map<String, Object> nestedCoordinate = new HashMap<>();
        nestedCoordinate.put(GPS_LATITUDE_KEY, coordinates[0]);
        nestedCoordinate.put(GPS_LONGITUDE_KEY, coordinates[1]);
        consumerDataDoc.put(GPS_COORDINATE_KEY, nestedCoordinate);

        database.collection(USER_COLLECTION).document(Uid)
                .set(consumerDataDoc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "New consumer successfully added to the database");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing new consumer to the database", e);
                    }
                });
    }

    // TODO : Add method to search for farm based on farm name
    // TODO : Add method to add additional farmer data
    // Produce sold
    // Farm Image
    // Farm Bio
    // Short Description
    // Farm Name



    /** Method to get the GPS coordinates from a given address in string form
     * @param context : context from which the method is called
     * @param completeAddress : the complete address the user enter, street address, zip, etc.
     * @return : double array of GPS coordinates or null array
     */
    public double[] getGPSCoordinatesFromAddress(Context context, String completeAddress){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        Address gatheredAddress = null;
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocationName(completeAddress, 1);
        } catch (IOException e) {
            Log.e(TAG, "IO Exception exncountered when converting address from location name");
        }
        gatheredAddress = addresses.get(0);
        double latitude = gatheredAddress.getLatitude();
        double longitude = gatheredAddress.getLongitude();
        double[] gpsCoordinates = {latitude, longitude};

        if(gpsCoordinates == null){
            gpsCoordinates = new double[2];
            gpsCoordinates[0] = 0;
            gpsCoordinates[1] = 0;
        }

        Log.d(TAG, "Coordinates: " + gpsCoordinates[0] + ", " + gpsCoordinates[1]);
        return gpsCoordinates;
    }

    /** Method in order to change the email of a user
     * @param email : new email of the user
     */
    public void updateUserEmail(final String email){
        database.collection(FirestoreHelper.USER_COLLECTION).document(user.getUid())
                .update(EMAIL_KEY, email);
    }

    /** Method to update the name of the user
     * @param firstName : new first name of the user
     * @param lastName : new last name of the user
     */
    public void updateUserName(String firstName, String lastName){
        //No listener for task completion
        database.collection(FirestoreHelper.USER_COLLECTION).document(user.getUid())
                .update(
                        NAME_KEY + "." + FIRST_KEY, firstName,
                        NAME_KEY + "." + LAST_KEY, lastName
                );
    }

    /** Method to update the address of the user
     * @param streetAddress : new street address of the user
     * @param city : new city of the user
     * @param zipCode : new zip code of the user
     * @param state : new state of the user
     */
    public void updateUserAddress(String streetAddress, String city, String zipCode, String state){
        //No listener for task completion
        database.collection(FirestoreHelper.USER_COLLECTION).document(user.getUid())
                .update(
                        ADDRESS_KEY + "." + STREET_ADDRESS_KEY, streetAddress,
                        ADDRESS_KEY + "." + CITY_KEY, city,
                        ADDRESS_KEY + "." + ZIP_CODE_KEY, zipCode,
                        ADDRESS_KEY + "." + STATE_KEY, state
                );
    }

    /** Method to update the phone number of the user
     * @param phoneNumber : new phone number for the user
     */
    public void updateUserPhoneNumber(String phoneNumber){
        //No listener for task completion
        database.collection(FirestoreHelper.USER_COLLECTION).document(user.getUid())
                .update(PHONE_NUMBER_KEY, phoneNumber);
    }

    public void setRequestListener(FirestoreHelperListener listener){this.listener = listener;}
}
