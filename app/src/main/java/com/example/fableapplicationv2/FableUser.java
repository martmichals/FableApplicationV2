package com.example.fableapplicationv2;

import com.google.firebase.firestore.DocumentSnapshot;

public class FableUser {
    private String Uid;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private double latitude;
    private double longitude;

    public FableUser(){
        Uid = "";
        firstName = "";
        lastName = "";
        email = "";
        phoneNumber = "";
        streetAddress = "";
        city = "";
        state = "";
        zipCode = "";
        latitude = 0;
        longitude = 0;
    }

    public FableUser(String Uid, String firstName, String lastName, String email, String phoneNumber, String streetAddress,
                     String city, String state, String zipCode, double latitude, double longitude){
        this.Uid = Uid;
        this.firstName = firstName;
        this.lastName = lastName;

        this.email = email;
        this.phoneNumber = phoneNumber;

        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public FableUser(DocumentSnapshot snapshot){
        this.Uid = snapshot.getId();
        this.firstName = snapshot.getString(FirestoreHelper.NAME_KEY + "." + FirestoreHelper.FIRST_KEY);
        this.lastName = snapshot.getString(FirestoreHelper.NAME_KEY + "." + FirestoreHelper.LAST_KEY);

        this.email = snapshot.getString(FirestoreHelper.EMAIL_KEY);
        this.phoneNumber = snapshot.getString(FirestoreHelper.PHONE_NUMBER_KEY);

        this.streetAddress = snapshot.getString(FirestoreHelper.ADDRESS_KEY + "." + FirestoreHelper.STREET_ADDRESS_KEY);
        this.city = snapshot.getString(FirestoreHelper.ADDRESS_KEY + "." + FirestoreHelper.CITY_KEY);
        this.state = snapshot.getString(FirestoreHelper.ADDRESS_KEY + "." + FirestoreHelper.STATE_KEY);
        this.zipCode = snapshot.getString(FirestoreHelper.ADDRESS_KEY + "." + FirestoreHelper.ZIP_CODE_KEY);

        this.latitude = snapshot.getDouble(FirestoreHelper.GPS_COORDINATE_KEY + "." + FirestoreHelper.GPS_LATITUDE_KEY);
        this.longitude = snapshot.getDouble(FirestoreHelper.GPS_COORDINATE_KEY + "." + FirestoreHelper.GPS_LONGITUDE_KEY);
    }

    @Override
    public String toString(){
        String str = "Farmer Object" + "\n";
        str+= "Uid: " + Uid + "\n";
        str+= "User: " + email + "\n";
        str+= "Phone Number: " + phoneNumber + "\n";
        str+= "First Name: " + firstName + "\n";
        str+= "Last Name: " + lastName + "\n";
        str+= "Street: " + streetAddress + "\n";
        str+= "City: " + city + "\n";
        str+= "State: " + state + "\n";
        str+= "Zip Code: " + zipCode + "\n";
        str+= "Lat: " + latitude + "\n";
        str+= "Long: " + longitude + "\n";
        return str;
    }

    public String getUid(){return Uid;}

    public String getEmail(){
        return email;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getStreetAddress(){
        return streetAddress;
    }

    public String getCity(){
        return city;
    }

    public String getState(){
        return state;
    }

    public String getZipCode(){
        return zipCode;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }
}
