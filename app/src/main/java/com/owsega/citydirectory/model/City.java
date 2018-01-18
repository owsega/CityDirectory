package com.owsega.citydirectory.model;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.DiffCallback;

/**
 * A city object
 */
public class City implements Comparable<City> {

    public static DiffCallback<City> DIFF_CALLBACK = new DiffCallback<City>() {
        @Override
        public boolean areItemsTheSame(@NonNull City oldItem, @NonNull City newItem) {
            return oldItem._id == newItem._id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull City oldItem, @NonNull City newItem) {
            return oldItem.equals(newItem);
        }
    };

    public long _id;
    public String country;
    public String name;
    public Coord coord;

    @Override
    public int compareTo(@NonNull City anotherCity) {
        return this.toString().compareTo(anotherCity.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return this.toString().equalsIgnoreCase(obj.toString());
    }

    @Override
    public String toString() {
        return name + ", " + country;
    }

    public class Coord {
        public float lon;
        public float lat;
    }
}
