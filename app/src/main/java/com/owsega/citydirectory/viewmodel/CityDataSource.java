package com.owsega.citydirectory.viewmodel;

import android.arch.paging.ItemKeyedDataSource;
import android.support.annotation.NonNull;

import com.owsega.citydirectory.model.City;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class CityDataSource extends ItemKeyedDataSource<String, City> {

    private ConcurrentSkipListMap<String, City> cityMap;
    private int count;

    public CityDataSource(ConcurrentSkipListMap<String, City> data) {
        this.count = data.size();
        this.cityMap = data;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params,
                            @NonNull LoadInitialCallback<City> callback) {
        List<City> output = new ArrayList<>();

        String current = cityMap.firstKey();
        for (int i = 0; i < params.requestedLoadSize; i++) {
            if (current == null) break;
            output.add(cityMap.get(current));
            current = cityMap.higherKey(current);
        }
        callback.onResult(output, 0, count);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params,
                          @NonNull LoadCallback<City> callback) {
        List<City> output = new ArrayList<>();

        String current = params.key;
        for (int i = 0; i < params.requestedLoadSize; i++) {
            if (current == null) break;
            output.add(cityMap.get(current));
            current = cityMap.higherKey(current);
        }
        output.remove(cityMap.get(params.key));
        callback.onResult(output);
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params,
                           @NonNull LoadCallback<City> callback) {
        List<City> output = new ArrayList<>();

        String current = params.key;
        for (int i = 0; i < params.requestedLoadSize; i++) {
            if (current == null) break;
            output.add(cityMap.get(current));
            current = cityMap.lowerKey(current);
        }
        output.remove(cityMap.get(params.key));
        Collections.sort(output);
        callback.onResult(output);
    }

    @NonNull
    @Override
    public String getKey(@NonNull City item) {
        // warning: this assumes the key for any city is its toString equivalent
        return item.toString();
    }

    public ConcurrentNavigableMap<String, City> filterWith(String text) {
        String lowerBound = cityMap.floorKey(text);
        int len = text.length() - 1;
        String higherBound = text.substring(0, len) + (char) (text.charAt(len) + 1);
        return cityMap.subMap(lowerBound, higherBound);
    }
}
    