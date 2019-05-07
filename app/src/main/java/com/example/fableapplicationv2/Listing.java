package com.example.fableapplicationv2;

import com.google.firebase.firestore.DocumentSnapshot;

public class Listing {
    private String produceName;
    private String description;
    private double price;

    public Listing(){
        produceName = "";
        description = "";
        price = 0;
    }

    public Listing(DocumentSnapshot document){
        this.produceName = document.getId();
        this.description = document.getString("description");
        this.price = document.getDouble("price");
    }

    public String toString(){
        String str = "A listing object" + "\n";
        str+= "Name: " + produceName + "\n";
        str+= "Description: " + description + "\n";
        str+= "Price: " + price + "\n";

        return str;
    }

    public String getProduceName(){
        return produceName;
    }

    public String getDescription(){
        return description;
    }

    public double getPrice(){
        return price;
    }
}
