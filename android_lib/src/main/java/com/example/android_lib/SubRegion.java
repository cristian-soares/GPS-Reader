package com.example.android_lib;

import java.io.Serializable;

public class SubRegion extends Region implements Serializable {
    private Region mainRegion;

    public SubRegion(String name, double latitude, double longitude, int user, Region mainRegion) {
        super(name, latitude, longitude, user);
        this.mainRegion = mainRegion;
        this.setType("SubRegion");
    }
}
