package com.owsega.citydirectory.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.owsega.citydirectory.model.City;
import com.owsega.citydirectory.viewmodel.CityAdapter.OnCityClickListener;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * ViewModel for CityListActivity. Provides data to be shown in the activity
 */
public class CityListViewModel extends ViewModel implements OnCityClickListener {

    //    public static final String CITIES_FILE = "cities.json";
    public static final String CITIES_FILE = "smallcities.json";
    private static final String TAG = "CityListViewModel";
    private static final int PAGING_SIZE = 30;

    public LiveData<PagedList<City>> cityList;
    public MutableLiveData<City> selectedCity;
    /**
     * monitors and posts when the data has been loaded and list is ready for showing
     */
    public MutableLiveData<Boolean> dataReady;
    public MutableLiveData<Boolean> emptyData;
    /**
     * monitors if the data preparation has started.
     * This helps ensure we don't start the data prep process
     * (in {@link #getAllCities(JsonReader)}) all over again
     * in a new background thread if {@link #init(JsonReader, boolean)}
     * is called again before the initial data prep is completed.
     */
    private boolean dataPrepStarted;
    private Executor executor;
    private ConcurrentNavigableMap<String, City> fullData;
    private CityDataSourceFactory dataSourceFactory;

    CityListViewModel() {
        dataPrepStarted = false;
        dataReady = new MutableLiveData<>();
        selectedCity = new MutableLiveData<>();
        emptyData = new MutableLiveData<>();
    }

    public void init(final JsonReader jsonReader, boolean useBackgroundThreads) {
        if (dataPrepStarted) {
            dataReady.postValue(dataReady.getValue());
            try {
                jsonReader.close();
            } catch (IOException ignored) {
            }
            return;
        }

        if (useBackgroundThreads) {
            this.executor = Executors.newFixedThreadPool(5);
            Executors.newSingleThreadExecutor().execute(() -> getAllCities(jsonReader));
        } else {
            getAllCities(jsonReader);
        }
    }

    private void getAllCities(JsonReader reader) {
        dataPrepStarted = true;
        System.out.println(TAG + " starting getAllcities");
        long time = System.currentTimeMillis();
        Type type = new TypeToken<List<City>>() {
        }.getType();
        List<City> cities = new Gson().fromJson(reader, type);
        time = System.currentTimeMillis() - time;
        System.out.println(TAG + " time to parse json " + time);
        ConcurrentNavigableMap<String, City> citiesMap = new ConcurrentSkipListMap<>();
        for (City city : cities) {
            citiesMap.put(city.getKey(), city);
        }
        initDataStructures(citiesMap);
        try {
            reader.close();
        } catch (IOException ignored) {
        }
    }

    private void initDataStructures(ConcurrentNavigableMap<String, City> cities) {
        fullData = cities;
        dataSourceFactory = new CityDataSourceFactory(cities);
        initList();
        setList(fullData);
        dataReady.postValue(true);
    }

    private void initList() {
        PagedList.Config pagedListConfig =
                new PagedList.Config.Builder()
                        .setEnablePlaceholders(false)
                        .setPageSize(PAGING_SIZE)
                        .build();
        LivePagedListBuilder<String, City> builder =
                new LivePagedListBuilder<>(dataSourceFactory, pagedListConfig);
        if (executor != null) builder.setBackgroundThreadExecutor(executor);
        cityList = builder.build();
    }

    private void setList(ConcurrentNavigableMap<String, City> cities) {
        dataSourceFactory.invalidateData(cities);
        emptyData.postValue(false);
    }

    public void filterCities(String text) {
        text = text.trim();
        if (text.isEmpty()) {
            setList(fullData);
        } else {
            filterWithMap(City.toKey(text));
        }
    }

    private void filterWithMap(String text) {
        String lowerBound = fullData.ceilingKey(text);

        int len = text.length() - 1;
        String higherBound = text.substring(0, len) + (char) (text.charAt(len) + 1);
        higherBound = fullData.ceilingKey(higherBound);

        if (lowerBound == null || higherBound == null) {
            System.out.println(TAG + " null bound");
            emptyData.postValue(true);
        } else {
            try {
                ConcurrentNavigableMap<String, City> newMap = fullData.subMap(
                        lowerBound,
                        lowerBound.startsWith(text),
                        higherBound,
                        false);
                if (newMap.size() < 1) emptyData.postValue(true);
                else setList(newMap);
            } catch (Exception e) {
                System.out.println(TAG + " error creating filtered map " + e.getMessage());
                e.printStackTrace();
                emptyData.postValue(true);
            }
        }
    }

    @Override
    public void onCityClicked(City city) {
        selectedCity.postValue(city);
    }
}
