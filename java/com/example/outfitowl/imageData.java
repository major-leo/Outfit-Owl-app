package com.example.outfitowl;

public class imageData {
    private String imageURL, itemName, itemType, itemKey;

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

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public imageData(String imageURL, String itemName, String itemType, String itemKey) {
        this.imageURL = imageURL;
        this.itemName = itemName;
        this.itemType = itemType;
        this.itemKey = itemKey;
    }

}
