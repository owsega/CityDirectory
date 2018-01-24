package com.owsega.citydirectory.viewmodel;

import android.os.Handler;
import android.os.Looper;

import com.owsega.citydirectory.model.City;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.Executor;


/**
 * Helper class to interface between the {@link CityAdapter} and the {@link CityListViewModel}
 * to resolve their differences. For instance, the viewModel lives in the appContext, the adapter
 * exist per activity. The adapter uses a list, the viewModel uses a Map.
 */
public class CityAdapterHelper {
    private static final int PAGE_SIZE = 100;

    private CityListViewModel viewModel;
    private CityAdapter adapter;
    private int count;
    private int mostRecentIndex;

    public CityAdapterHelper(CityListViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void loadAround(int position) {
        mostRecentIndex = position;
    }

    private void loadAfter(int requestedLoadSize, String key) {
        ConcurrentNavigableMap<String, City> data = viewModel.getData();
        List<City> output = new ArrayList<>();

        String current = key;
        for (int i = 0; i < requestedLoadSize; i++) {
            if (current == null) break;
            output.add(data.get(current));
            current = data.higherKey(current);
        }
        output.remove(data.get(key));
        updateAdapter(output);
    }

    private void updateAdapter(final List<City> output) {
        System.out.println("updateAdapter with data of size " + output.size());
        new Handler(Looper.getMainLooper()).post(() -> {
            if (adapter != null) adapter.setList(output);
        });
    }

    private void loadBefore(int requestedLoadSize, String key) {
        ConcurrentNavigableMap<String, City> data = viewModel.getData();
        List<City> output = new ArrayList<>();

        String current = key;
        for (int i = 0; i < requestedLoadSize; i++) {
            if (current == null) break;
            output.add(data.get(current));
            current = data.lowerKey(current);
        }
        output.remove(data.get(key));
        Collections.sort(output);
        updateAdapter(output);
    }

    public void reloadData() {
        count = viewModel.getData().size();
        if (count > 0) {
            int pageSize = PAGE_SIZE;
            Executor executor = viewModel.getExecutor();
            System.out.println("Load initial beginning " + pageSize + " in background=" + (executor == null));
            if (executor != null) executor.execute(() -> loadInitialData(pageSize));
            else loadInitialData(pageSize);
        }
    }

    private void loadInitialData(int requestedLoadSize) {
        //todo disregarding param requestedLoadSize
        requestedLoadSize = count > 0 ? count : requestedLoadSize;
        ConcurrentNavigableMap<String, City> data = viewModel.getData();
        List<City> output = new ArrayList<>();

        String current = data.firstKey();
        for (int i = 0; i < requestedLoadSize; i++) {
            if (current == null) break;
            output.add(data.get(current));
            current = data.higherKey(current);
        }
        updateAdapter(output);
    }

    void setAdapter(CityAdapter adapter) {
        this.adapter = adapter;
    }
}
