package com.example.android_lib;

import java.io.Serializable;

public class RestrictedRegion extends Region implements Serializable {
    private Region mainRegion;

    public RestrictedRegion(String name, double latitude, double longitude, int user, Region mainRegion) {
        super(name, latitude, longitude, user);
        this.mainRegion = mainRegion;
        this.setType("RestrictedRegion");
    }
}
