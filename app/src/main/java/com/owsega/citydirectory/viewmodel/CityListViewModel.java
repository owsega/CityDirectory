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

import java.io.IOException;
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
        if (executor != null && cityList != null) {
            try {
                jsonReader.close();
            } catch (IOException ignored) {
            }
            return;
        }

        cityList = new MutableLiveData<>();
        selectedCity = new MutableLiveData<>();
        emptyData = new MutableLiveData<>();

        executor = Executors.newFixedThreadPool(5);
//        Executors.newSingleThreadExecutor().execute(() -> getAllCities(jsonReader));
        initDataStructures(getAllCities(jsonReader));
    }

    private void initDataStructures(List<City> cities) {
        fullData = new ConcurrentSkipListMap<>();
        for (City city : cities) {
            fullData.put(city.toString().toLowerCase(), city);
        }
        trie = passIntoTrie(cities);
        dataIsReady = true;
        dataReady.postValue(true);

        setList(fullData);
    }

    private List<City> getAllCities(JsonReader reader) {
        Log.d(TAG, "starting getAllcities");
        long time = System.currentTimeMillis();
        Type type = new TypeToken<List<City>>() {
        }.getType();
        List<City> cities = new Gson().fromJson(reader, type);
        time = System.currentTimeMillis() - time;
        Log.d(TAG, "time to parse json " + time);
        initDataStructures(cities);
        try {
            reader.close();
        } catch (IOException ignored) {
        }
        return cities;
    }

    private CitiesTrie passIntoTrie(List<City> cities) {
        Log.d(TAG, "starting passIntoTrie");
        CitiesTrie trie = new CitiesTrie();

        long time = System.currentTimeMillis();
        for (City city : cities) {
            trie.add(city);
        }
        time = System.currentTimeMillis() - time;
        Log.d(TAG, "Time to build Trie " + time);
        return trie;
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
/*

    ConcurrentSkipListMap<String, City> filterWith(String text) {
        ConcurrentSkipListMap<String, City> newMap;

        Log.e("seyi", "filtering with " + text);
        String lowerBound = cityMap.floorKey(text);
        Log.e("seyi", "lowerBound " + lowerBound);

        int len = text.length() - 1;
        String higherBound = text.substring(0, len) + (char) (text.charAt(len) + 1);
        Log.e("seyi", "higherBoundText " + higherBound);
        higherBound = cityMap.ceilingKey(higherBound);
        Log.e("seyi", "higherBound " + higherBound);

        if (lowerBound == null || higherBound == null) {
            // this can occur when the filter is not a suitable filter
            // todo bad design ... loadInitial will fail for new data source
            // todo return empty map
//            return new ConcurrentSkipListMap<>();
            String first = cityMap.firstKey();
            String second = cityMap.higherKey(first);
            newMap = new ConcurrentSkipListMap<>(cityMap.subMap(first, second));
        } else {
            // todo handle scenarios where lists contain aaa, ccc and b is used to filter (empty results too)
            // todo catch inconsistent bounds exception too. and handle empty
            newMap = new ConcurrentSkipListMap<>(
                    cityMap.subMap(lowerBound, lowerBound.startsWith(text), higherBound, false));
        }
        Log.e("seyi", "newMap size " + newMap.size());
        return newMap;
    }
*/

    @Override
    public void onCityClicked(City city) {
        selectedCity.postValue(city);
    }
}
