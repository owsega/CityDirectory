package com.owsega.citydirectory.provider;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.support.annotation.NonNull;
import android.util.Log;

import com.owsega.citydirectory.model.CitiesTrie;
import com.owsega.citydirectory.model.City;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

public class CityDataSourceFactory implements DataSource.Factory<String, City> {

    private MutableLiveData<CityDataSource> mutableLiveData;
    private CityDataSource cityDataSource;  // current data source (could have been filtered)
    private CitiesTrie trie;    // helps us get the number of elements the search can return
    private ConcurrentSkipListMap<String, City> fullData;
    private ConcurrentSkipListMap<String, City> data;

    public CityDataSourceFactory(List<City> items) {
        this.mutableLiveData = new MutableLiveData<>();
        this.trie = LoadDataUtils.passIntoTrie(items);
        this.fullData = new ConcurrentSkipListMap<>();
        for (City city : items) {
            this.fullData.put(city.toString().toLowerCase(), city);
        }
        data = fullData;
    }

    @Override
    public DataSource<String, City> create() {
        cityDataSource = new CityDataSource(data);
        mutableLiveData.postValue(cityDataSource);
        return cityDataSource;
    }

    public MutableLiveData<CityDataSource> getMutableLiveData() {
        return mutableLiveData;
    }

    void filterList(@NonNull String text) {
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
            data = fullData;
        }
        cityDataSource.invalidate();
        /*if (text.length() < 1) {
            cityDataSource = fullCityDataSource;  // for empty filters, use whole list
            return;
        } else {
            ConcurrentNavigableMap<String, City> filtered = cityDataSource.filterWith(text);
            Log.e("seyi", "setting new cityDataSource");
            cityDataSource = new CityDataSource(filtered);
        }
        mutableLiveData.postValue(cityDataSource);*/
    }

}
    