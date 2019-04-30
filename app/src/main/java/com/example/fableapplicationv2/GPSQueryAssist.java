package com.example.fableapplicationv2;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

//Class to help with the GPS search functionality
public class GPSQueryAssist {
    public static final String TAG = "GPSQueryAssist";
    public static final double ONE_MILE_LAT = 0.0144927536231884;
    public static final double ONE_MILE_LONG = 0.0181818181818182;
    public static int EARTH_RADIUS_KM = 6371;

    private static ArrayList<DocumentSnapshot> latitudeSnapshots;
    private static ArrayList<DocumentSnapshot> longitudeSnapshots;

    //Booleans for the radial search
    private static ArrayList<DocumentSnapshot> squareSearchResults;

    //Listeners
    private static GPSQueryAssistListener listener;

    public GPSQueryAssist(){
        listener = null;
    }

    /** Method to get all the sellers in a box, centered
     * @param radius : radius of the circle to search in
     * @param center : center of the box to search in
     * @return
     */
    public static void getLocationsInSquare(double radius, GeoPoint center){
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        double squareSideLengthMiles = radius * 2;
        CollectionReference reference = database.collection(FirestoreHelper.USER_COLLECTION);
        double centerLat = center.getLatitude();
        double centerLong = center.getLongitude();
        double offsetLat = (squareSideLengthMiles / 2) * ONE_MILE_LAT;
        double offsetLong = (squareSideLengthMiles / 2) * ONE_MILE_LONG;

        double[] bottomLeftPoint = {centerLat - offsetLat, centerLong - offsetLong};
        double[] topRightPoint = {centerLat + offsetLat, centerLong + offsetLong};

        Query queryLat = reference.whereGreaterThan("coordinates.lat", bottomLeftPoint[0])
                .whereLessThan("coordinates.lat", topRightPoint[0]);
        Query queryLong = reference.whereGreaterThan("coordinates.long", bottomLeftPoint[1])
                .whereLessThan("coordinates.long", topRightPoint[1]);

        latitudeSnapshots = null;
        longitudeSnapshots = null;
        squareSearchResults = null;

        queryLat.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        latitudeSnapshots = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for(QueryDocumentSnapshot document: task.getResult()){
                                latitudeSnapshots.add(document);
                            }
                            Log.d(TAG, "Query for latitude success. "
                                    + task.getResult().size() + " documents found.");
                        } else {
                            Log.d(TAG, "Query failure. Check logs.");
                        }

                        if(longitudeSnapshots != null) {
                            squareSearchResults = combineLatLongResults(latitudeSnapshots, longitudeSnapshots);
                            listener.onSearchComplete();
                        }
                    }
                });

        queryLong.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        longitudeSnapshots = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for(QueryDocumentSnapshot document: task.getResult()){
                                longitudeSnapshots.add(document);
                            }
                            Log.d(TAG, "Query for longitude success. "
                            + task.getResult().size() + " documents found.");
                        } else {
                            Log.d(TAG, "Query failure. Check logs.");
                        }

                        if(latitudeSnapshots != null) {
                            squareSearchResults = combineLatLongResults(latitudeSnapshots, longitudeSnapshots);
                            listener.onSearchComplete();
                        }
                    }
                });
    }

    public static ArrayList<DocumentSnapshot> circularizeSquareResults(double radius, GeoPoint center) {
        ArrayList<DocumentSnapshot> documentsInCircle = new ArrayList<>();
        double latitudeCenter = center.getLatitude();
        double longitudeCenter = center.getLongitude();

        String lat_path = FirestoreHelper.GPS_COORDINATE_KEY + "." + FirestoreHelper.GPS_LATITUDE_KEY;
        String long_path = FirestoreHelper.GPS_COORDINATE_KEY + "." + FirestoreHelper.GPS_LONGITUDE_KEY;

        if (squareSearchResults != null){
            for (DocumentSnapshot snap : squareSearchResults) {
                double latDoc = snap.getDouble(lat_path);
                double longDoc = snap.getDouble(long_path);
                if (getMilesBtwGPSCoordinates(latitudeCenter, longitudeCenter, latDoc, longDoc) <= radius) {
                    documentsInCircle.add(snap);
                    Log.d(TAG, "Added this user: " + snap.getString("name.first") + " " + snap.getString("name.last"));
                }
            }
        }

        return documentsInCircle;
    }

    private static ArrayList<DocumentSnapshot> combineLatLongResults(ArrayList<DocumentSnapshot> latList, ArrayList<DocumentSnapshot> longList){
        ArrayList<DocumentSnapshot> combinedArrayList = new ArrayList<>();
        Log.d(TAG, "Attempting document list combination.");

        if(latList.size() < 1 || longList.size() < 1)
            return null;

        for(int i = 0; i < latList.size(); i++){
            for(int j = 0; j < longList.size(); j++){
                if(latList.get(i).equals(longList.get(j))){
                    combinedArrayList.add(latList.get(i));
                    longList.remove(j);
                    j = longList.size();
                }
            }
        }

        Log.d(TAG, "Result combination successful.");
        return combinedArrayList;
    }

    public static double getMilesBtwGPSCoordinates(double lat1, double long1, double lat2, double long2){
        double dLatitude = Math.toRadians(lat2 - lat1);
        double dLongitude = Math.toRadians(long2 - long1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLatitude / 2) * Math.sin(dLatitude / 2) +
                Math.sin(dLongitude / 2) * Math.sin(dLongitude / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distanceKilometers = EARTH_RADIUS_KM * c;

        Log.d(TAG, "Distance in between the two passed points: " + (distanceKilometers / 1.609));
        return distanceKilometers / 1.609;
    }

    public void setGPSQueryListener(GPSQueryAssistListener listener){
        this.listener = listener;
    }

    public ArrayList<DocumentSnapshot> getSquareSearchResults(){
        return squareSearchResults;
    }
}
