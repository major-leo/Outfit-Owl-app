package com.example.outfitowl;

public class imageData {
    private String imageURL, itemName;

    public imageData(){

    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }


    public imageData(String imageURL, String itemName) {
        this.imageURL = imageURL;
        this.itemName = itemName;
    }
}
