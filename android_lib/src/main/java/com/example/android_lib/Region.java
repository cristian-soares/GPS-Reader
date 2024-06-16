package com.example.android_lib;

import android.location.Location;

import java.io.Serializable;

public class Region implements Serializable {
    protected String name;
    protected double latitude;
    protected double longitude;
    protected int user;
    protected long timestamp;
    private boolean loadedFromFirebase;
    private String type;

    public Region(String name, double latitude, double longitude, int user) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.user = user;
        this.timestamp = System.nanoTime(); // Define o timestamp com System.nanoTime()
        this.type = "Region"; // Define o tipo padrão
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getUser() {
        return user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isLoadedFromFirebase() {
        return loadedFromFirebase;
    }

    public void setLoadedFromFirebase(boolean loadedFromFirebase) {
        this.loadedFromFirebase = loadedFromFirebase;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float calculateDistance(double lat2, double lon2) {
        float[] result = new float[1];
        Location.distanceBetween(latitude, longitude, lat2, lon2, result);
        return result[0];
    }
}
