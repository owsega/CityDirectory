package com.owsega.citydirectory.viewmodel;

import android.support.annotation.Nullable;

import com.owsega.citydirectory.model.City;
import com.owsega.citydirectory.viewmodel.CityListViewModel.UpdateListener;

import java.util.List;

/**
 * Mock class for mocking updates from {@link CityListViewModel} during test
 */
public class MockCityListViewModelUpdateListener implements UpdateListener {

    boolean emptyData;
    boolean dataReady;
    City selectedCity;
    List<City> cityList;
    private int dataReadyChangeCount;

    @Override
    public void onEmptyData(boolean emptyData) {
        this.emptyData = emptyData;
    }

    @Override
    public void onCitySelected(@Nullable City city) {
        this.selectedCity = city;
    }

    @Override
    public void onDataReady(boolean dataReady) {
        this.dataReady = dataReady;
        dataReadyChangeCount++;
    }

    @Override
    public void onCityListUpdated(List<City> cities) {
        this.cityList = cities;
    }

    public int getDataReadyChangeCount() {
        return dataReadyChangeCount;
    }

    public void listenForDataReadyChanges() {
        dataReadyChangeCount = 0;
    }
}
