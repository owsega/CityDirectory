package com.owsega.citydirectory.provider;

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

    //    private static final String CITIES_FILE = "cities.json";
    public static final String CITIES_FILE = "smallcities.json";
    private static final String TAG = "CityListViewModel";

    public MutableLiveData<PagedList<City>> cityList;
    public MutableLiveData<City> selectedCity;
    public MutableLiveData<Boolean> emptyData;
    private Executor executor;
    private CitiesTrie trie;
    private ConcurrentSkipListMap<String, City> fullData;
    private ConcurrentSkipListMap<String, City> data;

    public CityListViewModel() {
    }

    public void init(JsonReader jsonReader) {
        if (executor != null && cityList != null) return;

        cityList = new MutableLiveData<>();
        executor = Executors.newFixedThreadPool(5);

        initDataStructures(getAllCities(jsonReader));
        setList(fullData);

        selectedCity = new MutableLiveData<>();
        emptyData = new MutableLiveData<>();
    }

    private void initDataStructures(List<City> cities) {
        fullData = new ConcurrentSkipListMap<>();
        for (City city : cities) {
            fullData.put(city.toString().toLowerCase(), city);
        }
        data = fullData;
        trie = LoadDataUtils.passIntoTrie(cities);
    }

    private List<City> getAllCities(JsonReader reader) {
        long begin = System.currentTimeMillis();
        Type type = new TypeToken<List<City>>() {
        }.getType();
        List<City> cities = new Gson().fromJson(reader, type);
        long diff = System.currentTimeMillis() - begin;
        Log.e(TAG, "time to parse json " + diff);
        return cities;
    }

    private void setList(ConcurrentSkipListMap<String, City> cities) {
        CityDataSourceFactory dataSourceFactory = new CityDataSourceFactory(cities);

        int pageSize = cities.size() >= 20 ? 20 : cities.size();
        PagedList.Config pagedListConfig =
                new PagedList.Config.Builder()
                        .setEnablePlaceholders(false)
                        .setInitialLoadSizeHint(10)
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
            data = filtered;
        } else {
            Log.e("seyi", "empty search");
            emptyData.postValue(true);
        }
        setList(data);
    }

    @Override
    public void onCityClicked(City city) {
        selectedCity.postValue(city);
    }
}
