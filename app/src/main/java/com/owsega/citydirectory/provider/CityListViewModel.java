package com.owsega.citydirectory.provider;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.owsega.citydirectory.model.City;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * ViewModel for CityListActivity. Provides data to be shown in the activity
 */
public class CityListViewModel extends ViewModel {

    //    private static final String CITIES_FILE = "cities.json";
    public static final String CITIES_FILE = "smallcities.json";

    public LiveData<PagedList<City>> cityList;
    private LiveData<CityDataSource> cityDataSource;
    private Executor executor;

    public CityListViewModel() {
    }

    public void init(Context context) {
        if (cityDataSource != null && executor != null && cityList != null) return;

        List<City> cities;
        try {
            cities = getCities(context);
        } catch (Exception e) {
            // todo use LiveData to send error to UI
            // context.showError(context.getString(R.string.error_loading_cities));
            e.printStackTrace();
            return;
        }
        CityDataSourceFactory cityDataSourceFactory = new CityDataSourceFactory(cities);
//        cityDataSource = cityDataSourceFactory.getMutableLiveData();

        PagedList.Config pagedListConfig =
                new PagedList.Config.Builder()
                        .setEnablePlaceholders(false)
                        .setInitialLoadSizeHint(10)
                        .setPageSize(20)
                        .build();

        executor = Executors.newFixedThreadPool(5);
        cityList = new LivePagedListBuilder(cityDataSourceFactory, pagedListConfig)
                .setBackgroundThreadExecutor(executor)
                .build();
    }

    private List<City> getCities(Context context) throws IOException {
        InputStream in = context.getApplicationContext().getAssets().open(CITIES_FILE);
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        long begin = System.currentTimeMillis();
        Type type = new TypeToken<List<City>>() {
        }.getType();
        List<City> cities = new Gson().fromJson(reader, type);
        long diff = System.currentTimeMillis() - begin;
        Log.e("seyi", "time to parse json " + diff);
        reader.close();
        return cities;
    }

    public void filterCities(String filterText) {
        CityListViewModel viewModel;
    }
}
