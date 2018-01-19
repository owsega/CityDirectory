package com.owsega.citydirectory.viewmodel;

import android.arch.paging.DataSource;

import com.owsega.citydirectory.model.City;

import java.util.concurrent.ConcurrentNavigableMap;

public class CityDataSourceFactory implements DataSource.Factory<String, City> {

    private ConcurrentNavigableMap<String, City> data;

    CityDataSourceFactory(ConcurrentNavigableMap<String, City> cities) {
        data = cities;
    }

    @Override
    public DataSource<String, City> create() {
        return new CityDataSource(data);
    }
}
    