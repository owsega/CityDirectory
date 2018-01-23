package com.owsega.citydirectory.model;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link android.support.v7.util.DiffUtil.Callback} for adapters using {@link City objects}
 */
public class CityDiffCallback extends DiffUtil.Callback {

    private final List<City> oldCities;
    private final List<City> newCities;

    public CityDiffCallback(List<City> oldCityList, List<City> newCityList) {
        this.oldCities = oldCityList != null ? oldCityList : new ArrayList<>();
        this.newCities = newCityList != null ? newCityList : new ArrayList<>();
    }

    @Override
    public int getOldListSize() {
        return oldCities.size();
    }

    @Override
    public int getNewListSize() {
        return newCities.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldCities.get(oldItemPosition)._id == newCities.get(newItemPosition)._id;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldCities.get(oldItemPosition).equals(newCities.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}