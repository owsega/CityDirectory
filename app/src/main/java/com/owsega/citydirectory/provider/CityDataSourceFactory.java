package com.owsega.citydirectory.provider;

import android.arch.paging.DataSource;

import com.owsega.citydirectory.model.City;

import java.util.concurrent.ConcurrentSkipListMap;

public class CityDataSourceFactory implements DataSource.Factory<String, City> {

    private ConcurrentSkipListMap<String, City> data;

    CityDataSourceFactory(ConcurrentSkipListMap<String, City> cities) {
        data = cities;
    }

    @Override
    public DataSource<String, City> create() {
        return new CityDataSource(data);
    }
}
    