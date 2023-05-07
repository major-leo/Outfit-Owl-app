package com.example.outfitowl;

import java.util.ArrayList;
import java.util.HashMap;

public class outfitData {
    private String id;
    private String name;
    private HashMap<String, ArrayList<String>> items;
    private long timestamp;

    public outfitData() {
    }

    public outfitData(String id, String name, HashMap<String, ArrayList<String>> items, long timestamp) {
        this.id = id;
        this.name = name;
        this.items = items;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, ArrayList<String>> getItems() {
        return items;
    }

    public void setItems(HashMap<String, ArrayList<String>> items) {
        this.items = items;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
