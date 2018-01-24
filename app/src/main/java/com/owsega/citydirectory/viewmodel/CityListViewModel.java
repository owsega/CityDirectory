package com.owsega.citydirectory.viewmodel;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.owsega.citydirectory.model.City;
import com.owsega.citydirectory.viewmodel.CityAdapter.OnCityClickListener;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * ViewModel for CityListActivity. Provides data to be shown in the activity.
 * <p>
 * The cities data is stored in a json assets file, but is accessed with
 * Gson's {@link JsonReader} here. The data is preprocessed into {@link ConcurrentNavigableMap}
 * (specifically, the {@link ConcurrentSkipListMap} because we need the data structure to be
 * <ol>
 * <li>Sorted. Since we are showing the cities alphabetically, we need a sorted data
 * structure. The sorting also helps in doing fast searches: we can easily use the
 * {@link ConcurrentNavigableMap#floorEntry(Object)} and similar functions.</li>
 * <li>Mapped. Since we have to map the actual {@link City}s to a key that
 * can be used to search. The key should be case insensitive.</li>
 * <li>Concurrent. To allow for responsive UI, heavy data processing like
 * data preprocessing and filtering should be done on background threads.</li>
 * <li>Fast. Accessing elements of the data structure should be as fast
 * as possible.</li>
 * </ol>
 * The ViewModel class is standalone, and lives in the application context. We
 */
public class CityListViewModel implements OnCityClickListener {

    //    public static final String CITIES_FILE = "cities.json";
    public static final String CITIES_FILE = "smallcities.json";
    private static final String TAG = "CityListViewModel";

    /**
     * holds listeners to updates from the ViewModel.
     * Should be only one {@link com.owsega.citydirectory.CityListActivity} at a time.
     */
    private List<UpdateListener> updateListeners;

    /**
     * monitors if the data preparation has started.
     * This helps ensure we don't start the data prep process
     * (in {@link #getAllCities(JsonReader, boolean)}) all over again
     * in a new background thread if {@link #init(JsonReader, boolean)}
     * is called again before the initial data prep is completed.
     */
    private boolean dataPrepStarted;

    /**
     * monitors if the data preparation has ended. And we can now update the UI
     */
    private boolean dataReady;

    private Executor executor;
    private ConcurrentNavigableMap<String, City> fullData;
    private ConcurrentNavigableMap<String, City> filteredData;

    public CityListViewModel() {
        updateListeners = new ArrayList<>();
        dataPrepStarted = false;
        dataReady = false;
    }

    /**
     * initialize view model. Load data from json into internal structure
     *
     * @param jsonReader           a Gson JsonReader reading input json
     * @param useBackgroundThreads if true, the heavy tasks are done in a background thread (both data initialization and filtering)
     */
    public void init(final JsonReader jsonReader, boolean useBackgroundThreads) {
        if (dataPrepStarted) {
            for (UpdateListener l : updateListeners) l.onDataReady(dataReady);
            try {
                jsonReader.close();
            } catch (IOException ignored) {
            }
            return;
        }

        if (useBackgroundThreads) {
            executor = Executors.newFixedThreadPool(5);
            executor.execute(() -> getAllCities(jsonReader, false));
        } else {
            getAllCities(jsonReader, true);
        }
    }

    private void getAllCities(JsonReader reader, boolean onMainThread) {
        dataPrepStarted = true;
        System.out.println(TAG + " starting getAllcities");
        long time = System.currentTimeMillis();
        Type type = new TypeToken<List<City>>() {
        }.getType();
        List<City> cities = new Gson().fromJson(reader, type);
        time = System.currentTimeMillis() - time;
        System.out.println(TAG + " time to parse json " + time);
        final ConcurrentNavigableMap<String, City> citiesMap = new ConcurrentSkipListMap<>();
        for (City city : cities) {
            citiesMap.put(city.getKey(), city); // todo could use multiple threads to do this
        }
        if (onMainThread) initDataStructures(citiesMap);
        else new Handler(Looper.getMainLooper()).post(() -> initDataStructures(citiesMap));
        try {
            reader.close();
        } catch (IOException ignored) {
        }
    }

    private void initDataStructures(ConcurrentNavigableMap<String, City> cities) {
        dataReady = true;
        for (UpdateListener l : updateListeners) l.onDataReady(dataReady);

        fullData = cities;
        setList(fullData);
    }

    private void setList(ConcurrentNavigableMap<String, City> cities) {
        filteredData = cities;
        for (UpdateListener l : updateListeners) l.onEmptyData(false);
    }

    public void addUpdateListener(UpdateListener updateListener) {
        updateListeners.add(updateListener);
    }

    public void removeUpdateListener(UpdateListener updateListener) {
        updateListeners.remove(updateListener);
    }

    /**
     * filter the data with the given prefix
     *
     * @param text prefix to filter the data set with
     */
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
            for (UpdateListener l : updateListeners) l.onEmptyData(true);
        } else {
            try {
                ConcurrentNavigableMap<String, City> newMap = fullData.subMap(
                        lowerBound,
                        lowerBound.startsWith(text),
                        higherBound,
                        false);
                if (newMap.size() < 1) {
                    for (UpdateListener l : updateListeners) l.onEmptyData(true);
                } else {
                    setList(newMap);
                }
            } catch (Exception e) {
                System.out.println(TAG + " error creating filtered map " + e.getMessage());
                e.printStackTrace();
                for (UpdateListener l : updateListeners) l.onEmptyData(true);
            }
        }
    }

    @Override
    public void onCityClicked(City city) {
        if (city != null) {
            for (UpdateListener l : updateListeners) l.onCitySelected(city);
        }
    }

    public ConcurrentNavigableMap<String, City> getData() {
        return filteredData;
    }

    public Executor getExecutor() {
        return executor;
    }

    /**
     * Listener for updates from the ViewModel.
     * <p>
     * This is to replace the LiveData (from android.arch) usage
     */
    public interface UpdateListener {
        void onEmptyData(boolean emptyData);

        void onCitySelected(City city);

        void onDataReady(boolean dataReady);
    }

}
