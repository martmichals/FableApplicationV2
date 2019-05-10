package com.example.fableapplicationv2;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Seller extends FableUser {
    public static String TAG = "SellerClass";

    private ArrayList<Listing> listings;
    private String slogan;
    private String description;

    public Seller(){
        super();
    }

    public Seller(DocumentSnapshot snapshot){
        super(snapshot);

        this.slogan = snapshot.getString("slogan");
        this.description = snapshot.getString("description");
    }

    public String toString(){
        String str = super.toString();

        str+= "Slogan: " + slogan + "\n";
        str+= "Description: " + description + "\n";

        for(Listing listing: listings)
            str+=listing.toString();
        return str;
    }

    public void fillListings(final GeneralListener listener){
        FirestoreHelper helper = new FirestoreHelper();
        CollectionReference collectionReference = helper.getDatabase().collection(FirestoreHelper.SELLER_COLLECTION).document(super.getUid())
                                                                      .collection(FirestoreHelper.LISTING_COLLECTION);
        listings = new ArrayList<>();

        collectionReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                listings.add(new Listing(document));
                            }
                            listener.onSuccess();
                        } else {
                            Log.d(TAG, "Error getting all documents in the sub-collection", task.getException());
                            listener.onFail();
                        }
                    }
                });
    }

    public Listing listingsContains(String searchQuery){
        searchQuery = searchQuery.toLowerCase();

        for(Listing listing: listings){
            String listingName = listing.getProduceName().toLowerCase();

            for(int i = 1; i <= listingName.length(); i++){
                String str = listingName.substring(0, i);
                Log.d(TAG, "The substring used to check: " + str);
                if(str.equals(searchQuery))
                    return listing;
            }
        }
        return null;
    }

    public double getDistanceToUser(FableUser fableUser){
        return GPSQueryAssist.getMilesBtwGPSCoordinates(this.getLatitude(), this.getLongitude(),
                                         fableUser.getLatitude(), fableUser.getLongitude());

    }
}
