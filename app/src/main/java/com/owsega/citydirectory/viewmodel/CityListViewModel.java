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
import com.owsega.citydirectory.viewmodel.CityPagedAdapter.OnCityClickListener;

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

    public static final String CITIES_FILE = "cities.json";
    //    public static final String CITIES_FILE = "smallcities.json";
    private static final String TAG = "CityListViewModel";

    public MutableLiveData<PagedList<City>> cityList;
    public MutableLiveData<City> selectedCity;
    public MutableLiveData<Boolean> dataReady;
    public MutableLiveData<Boolean> emptyData;
    private boolean dataIsReady;
    private Executor executor;
    private ConcurrentNavigableMap<String, City> fullData;
    private CityDataSourceFactory dataSourceFactory;

    public CityListViewModel() {
        dataIsReady = false;
        dataReady = new MutableLiveData<>();
        selectedCity = new MutableLiveData<>();
        emptyData = new MutableLiveData<>();
    }

    public void init(final JsonReader jsonReader) {
        if (dataIsReady) dataReady.postValue(true);
        if (executor != null && cityList != null) {
            try {
                jsonReader.close();
            } catch (IOException ignored) {
            }
            return;
        }

        cityList = new MutableLiveData<>();

        executor = Executors.newFixedThreadPool(5);
        getAllCities(jsonReader);
    }

    private void getAllCities(JsonReader reader) {
        Executors.newSingleThreadExecutor().execute(() -> {
//        AsyncTask.execute(() -> {
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
        });
    }

    private void initDataStructures(ConcurrentNavigableMap<String, City> cities) {
        fullData = cities;
        dataIsReady = true;
        dataReady.postValue(true);
        dataSourceFactory = new CityDataSourceFactory(cities);
        initList();
        setList(fullData);
    }

    private void initList() {
        PagedList.Config pagedListConfig =
                new PagedList.Config.Builder()
                        .setEnablePlaceholders(false)
                        .setPageSize(30)
                        .build();
        LiveData<PagedList<City>> liveData =
                new LivePagedListBuilder<>(dataSourceFactory, pagedListConfig)
                        .setBackgroundThreadExecutor(executor)
                        .build();
        liveData.observeForever(newCities -> cityList.postValue(newCities));
    }

    private void setList(ConcurrentNavigableMap<String, City> cities) {
        dataSourceFactory.invalidateData(cities);
    }

    public void filterCities(String text) {
        if (text.isEmpty()) {
            setList(fullData);
        } else {
            filterWithMap(text);
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
