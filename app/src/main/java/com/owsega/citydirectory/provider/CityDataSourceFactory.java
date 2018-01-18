package com.owsega.citydirectory.provider;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;

import com.owsega.citydirectory.model.City;

import java.util.List;

public class CityDataSourceFactory implements DataSource.Factory {

    MutableLiveData<CityDataSource> mutableLiveData;
    CityDataSource cityDataSource;
    List<City> data;

    public CityDataSourceFactory(List<City> items) {
        this.data = items;
        this.mutableLiveData = new MutableLiveData<>();
    }

    @Override
    public DataSource create() {
        cityDataSource = new CityDataSource(data);
        mutableLiveData.postValue(cityDataSource);
        return cityDataSource;
    }

    public MutableLiveData<CityDataSource> getMutableLiveData() {
        return mutableLiveData;
    }

}
    