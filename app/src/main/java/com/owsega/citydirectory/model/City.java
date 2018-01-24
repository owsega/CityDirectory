package com.owsega.citydirectory.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * A city object
 */
public class City implements Comparable<City>, Serializable {

    public long _id;
    public String country;
    public String name;
    public Coord coord;

    @Override
    public int compareTo(@NonNull City anotherCity) {
        return this.toString().compareTo(anotherCity.toString());
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof City && this.toString().equalsIgnoreCase(o.toString());
    }

    @Override
    public String toString() {
        return name + ", " + country;
    }

    /**
     * @return a String suitable to use as key to this city in a Map
     */
    public String getKey() {
        return toKey(this.toString());
    }

    /**
     * @return a String suitable to use as key in the app. Currently this just returns
     * the lowerCase representation of the given string
     */
    public static String toKey(String string) {
        return string.toLowerCase();
    }

    public static class Coord {
        public float lon;
        public float lat;

        public Coord(float lat, float lon) {
            this.lon = lon;
            this.lat = lat;
        }
    }
}
