package com.owsega.citydirectory.provider;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.support.annotation.NonNull;
import android.util.Log;

import com.owsega.citydirectory.model.City;

import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

public class CityDataSourceFactory implements DataSource.Factory<String, City> {

    private MutableLiveData<CityDataSource> mutableLiveData;
    private CityDataSource fullCityDataSource;  // backup before filters
    private CityDataSource cityDataSource;  // current data source (could have been filtered)
    private List<City> data;

    public CityDataSourceFactory(List<City> items) {
        this.data = items;
        this.mutableLiveData = new MutableLiveData<>();
    }

    @Override
    public DataSource create() {
        fullCityDataSource = new CityDataSource(data);
        cityDataSource = fullCityDataSource;
        mutableLiveData.postValue(cityDataSource);
        return cityDataSource;
    }

    public MutableLiveData<CityDataSource> getMutableLiveData() {
        return mutableLiveData;
    }

    void filterList(@NonNull String text) {
        if (text.length() < 1) {
            cityDataSource = fullCityDataSource;  // for empty filters, use whole list
            return;
        } else {
            ConcurrentNavigableMap<String, City> filtered = cityDataSource.filterWith(text);
            Log.e("seyi", "setting new cityDataSource");
            cityDataSource = new CityDataSource(filtered);
        }
        mutableLiveData.postValue(cityDataSource);
    }

}
    