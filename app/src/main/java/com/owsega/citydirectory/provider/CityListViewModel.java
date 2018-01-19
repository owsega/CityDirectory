package com.owsega.citydirectory.provider;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.owsega.citydirectory.model.City;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * ViewModel for CityListActivity. Provides data to be shown in the activity
 */
public class CityListViewModel extends ViewModel implements CityPagedAdapter.OnCityClickListener {

    //    private static final String CITIES_FILE = "cities.json";
    public static final String CITIES_FILE = "smallcities.json";
    private static final String TAG = "CityListViewModel";

    public MutableLiveData<PagedList<City>> cityList;
    public MutableLiveData<City> selectedCity;
    private CityDataSourceFactory dataSourceFactory;
    private Executor executor;
    private LiveData<CityDataSource> cityDataSource;

    public CityListViewModel() {
    }

    public void init(JsonReader jsonReader) {
        if (executor != null && cityList != null) return;

        cityList = new MutableLiveData<>();
        executor = Executors.newFixedThreadPool(5);

        setList(getCities(jsonReader));

        selectedCity = new MutableLiveData<>();
    }

    private void setList(List<City> cities) {
        dataSourceFactory = new CityDataSourceFactory(cities);
        cityDataSource = dataSourceFactory.getMutableLiveData();

        PagedList.Config pagedListConfig =
                new PagedList.Config.Builder()
                        .setEnablePlaceholders(false)
                        .setInitialLoadSizeHint(10)
                        .setPageSize(20)
                        .build();

        cityList.postValue(new LivePagedListBuilder<>(dataSourceFactory, pagedListConfig)
                .setBackgroundThreadExecutor(executor)
                .build()
                .getValue());
    }

    private List<City> getCities(JsonReader reader) {
        long begin = System.currentTimeMillis();
        Type type = new TypeToken<List<City>>() {
        }.getType();
        List<City> cities = new Gson().fromJson(reader, type);
        long diff = System.currentTimeMillis() - begin;
        Log.e(TAG, "time to parse json " + diff);
        return cities;
    }

    public void filterCities(String filterText) {
        dataSourceFactory.filterList(filterText);
    }

    @Override
    public void onCityClicked(City city) {
        selectedCity.postValue(city);
    }
}
