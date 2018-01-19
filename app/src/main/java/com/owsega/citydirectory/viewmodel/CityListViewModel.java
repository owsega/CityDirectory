package com.owsega.citydirectory.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.owsega.citydirectory.model.CitiesTrie;
import com.owsega.citydirectory.model.City;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * ViewModel for CityListActivity. Provides data to be shown in the activity
 */
public class CityListViewModel extends ViewModel implements CityPagedAdapter.OnCityClickListener {

    //    public static final String CITIES_FILE = "cities.json";
    public static final String CITIES_FILE = "smallcities.json";
    private static final String TAG = "CityListViewModel";

    public MutableLiveData<PagedList<City>> cityList;
    public MutableLiveData<City> selectedCity;
    public MutableLiveData<Boolean> dataReady;
    public MutableLiveData<Boolean> emptyData;
    private boolean dataIsReady;
    private Executor executor;
    private CitiesTrie trie;
    private ConcurrentSkipListMap<String, City> fullData;

    public CityListViewModel() {
        dataIsReady = false;
        dataReady = new MutableLiveData<>();
    }

    public void init(final JsonReader jsonReader) {
        if (dataIsReady) dataReady.postValue(true);
        if (executor != null && cityList != null) return;

        cityList = new MutableLiveData<>();
        executor = Executors.newFixedThreadPool(5);

        executor.execute(() -> initDataStructures(getAllCities(jsonReader)));

        setList(fullData);

        selectedCity = new MutableLiveData<>();
        emptyData = new MutableLiveData<>();
    }

    private void initDataStructures(List<City> cities) {
        fullData = new ConcurrentSkipListMap<>();
        for (City city : cities) {
            fullData.put(city.toString().toLowerCase(), city);
        }
        trie = passIntoTrie(cities);
        dataIsReady = true;
        dataReady.postValue(true);
    }

    private List<City> getAllCities(JsonReader reader) {
        long begin = System.currentTimeMillis();
        Type type = new TypeToken<List<City>>() {
        }.getType();
        List<City> cities = new Gson().fromJson(reader, type);
        long diff = System.currentTimeMillis() - begin;
        Log.d(TAG, "time to parse json " + diff);
        return cities;
    }

    private void setList(ConcurrentSkipListMap<String, City> cities) {
        CityDataSourceFactory dataSourceFactory = new CityDataSourceFactory(cities);

        int pageSize = cities.size() >= 30 ? 30 : cities.size();
        PagedList.Config pagedListConfig =
                new PagedList.Config.Builder()
                        .setEnablePlaceholders(false)
                        .setPageSize(pageSize)
                        .build();

        final LiveData<PagedList<City>> liveData = new LivePagedListBuilder<>(dataSourceFactory, pagedListConfig)
                .setBackgroundThreadExecutor(executor)
                .build();
        liveData.observeForever(new Observer<PagedList<City>>() {
            @Override
            public void onChanged(@Nullable PagedList<City> cities1) {
                cityList.postValue(cities1);
                liveData.removeObserver(this);
            }
        });
    }

    private CitiesTrie passIntoTrie(List<City> cities) {
        CitiesTrie trie = new CitiesTrie();

        long time = System.currentTimeMillis();
        for (City city : cities) {
            trie.add(city);
        }
        time = System.currentTimeMillis() - time;
        Log.e(TAG, "Time to build Trie");
        return trie;
    }

    public void filterCities(String text) {
        if (text.isEmpty()) {
            setList(fullData);
        }

        int count = trie.find(text);
        String firstKey = fullData.ceilingKey(text);
        if (count > 0 && firstKey != null && firstKey.startsWith(text)) {
            ConcurrentSkipListMap<String, City> filtered = new ConcurrentSkipListMap<>();
            String currentKey = firstKey;
            for (int i = 0; i < count; i++) {
                filtered.put(currentKey, fullData.get(currentKey));
                currentKey = fullData.higherKey(currentKey);
            }
            setList(filtered);
        } else {
            Log.d("seyi", "empty search");
            emptyData.postValue(true);
        }
    }

    @Override
    public void onCityClicked(City city) {
        selectedCity.postValue(city);
    }
}
