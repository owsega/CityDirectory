package com.owsega.citydirectory.viewmodel;

import android.support.annotation.Nullable;

import com.owsega.citydirectory.model.City;
import com.owsega.citydirectory.viewmodel.CityListViewModel.UpdateListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Mock class for mocking updates from {@link CityListViewModel} during test
 */
public class MockCityListViewModelUpdateListener implements UpdateListener {

    CityListViewModel viewModel;
    boolean emptyData;
    boolean dataReady;
    City selectedCity;
    List<City> cityList;
    private int dataReadyChangeCount;

    MockCityListViewModelUpdateListener(CityListViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void onEmptyData(boolean emptyData) {
        this.emptyData = emptyData;
        if (!emptyData) {
            this.cityList = new ArrayList<>();
            for (Entry<String, City> city : viewModel.getData().entrySet()) {
                this.cityList.add(city.getValue());
            }
        } else this.cityList = null;
    }

    @Override
    public void onCitySelected(@Nullable City city) {
        this.selectedCity = city;
    }

    @Override
    public void onDataReady(boolean dataReady) {
        this.dataReady = dataReady;
        dataReadyChangeCount++;
        if (dataReady) {
            this.cityList = new ArrayList<>();
            for (Entry<String, City> city : viewModel.getData().entrySet()) {
                this.cityList.add(city.getValue());
            }
        } else this.cityList = null;
    }

    public int getDataReadyChangeCount() {
        return dataReadyChangeCount;
    }

    public void listenForDataReadyChanges() {
        dataReadyChangeCount = 0;
    }
}
