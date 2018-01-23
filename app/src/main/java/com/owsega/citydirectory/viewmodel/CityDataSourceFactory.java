package com.owsega.citydirectory.viewmodel;

import android.arch.paging.DataSource;

import com.owsega.citydirectory.model.City;

import java.util.concurrent.ConcurrentNavigableMap;

public class CityDataSourceFactory implements DataSource.Factory<String, City> {

    private ConcurrentNavigableMap<String, City> data;
    private CityDataSource dataSource;

    CityDataSourceFactory(ConcurrentNavigableMap<String, City> cities) {
        data = cities;
    }

    void invalidateData(ConcurrentNavigableMap<String, City> newData) {
        if (!data.equals(newData)) {
            data = newData;
            if (dataSource != null) dataSource.invalidate();
        }
    }

    @Override
    public DataSource<String, City> create() {
        dataSource = new CityDataSource(data);
        return dataSource;
    }
}
    