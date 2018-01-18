package com.owsega.citydirectory.model;

import android.support.annotation.NonNull;

/**
 * A city object
 */
public class City implements Comparable<City> {
    public long _id;
    public String country;
    public String name;
    public Coord coord;

    @Override
    public int compareTo(@NonNull City anotherCity) {
        return this.toString().compareTo(anotherCity.toString());
    }

    @Override
    public String toString() {
//        return String.format(Locale.ENGLISH, "%s, %s (%f,%f)", name, country, coord.lon, coord.lat);
        return name + ", " + country;
    }

    public class Coord {
        public float lon;
        public float lat;
    }
}
