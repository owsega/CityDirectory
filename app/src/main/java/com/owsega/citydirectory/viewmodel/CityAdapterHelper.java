package com.owsega.citydirectory.viewmodel;

import android.os.Handler;
import android.os.Looper;

import com.owsega.citydirectory.model.City;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.Executor;


/**
 * Helper class to interface between the {@link CityAdapter} and
 * the {@link CityListViewModel} to resolve their differences.
 * For instance, the viewModel lives in the appContext, while the adapter
 * exist per activity. Also, the adapter uses a list, while the viewModel
 * uses a Map. And, the adapter's list contains a subsection of the data
 * while the viewModel holds the full data.
 */
public class CityAdapterHelper {
    private static final int PAGE_SIZE = 30;
    private static final int MAX_LIST_SIZE = 100; // PAGE_SIZE * 3;

    private CityListViewModel viewModel;
    private CityAdapter adapter;

    public CityAdapterHelper(CityListViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * loads data around this position if necessary.
     * todo how do we handle fast scrolling?  (frequent calls, jumping, etc)
     *
     * @param position current Adapter position
     */
    void loadAround(int position) {
        int listSize = adapter.getList().size();
        if (position < listSize * 0.25)
            loadBefore(position);  // load previous page if in 1st quarter
        else if (position > listSize * 0.75)
            loadAfter(position); // load next page if in last quarter
    }

    private void loadBefore(int position) {
        // verify we are not at the top of the data
        String firstInVM = viewModel.getData().firstKey();
        String firstInAdapter = adapter.getItem(0).getKey();
        if (firstInAdapter.equalsIgnoreCase(firstInVM)) return;

        final String key = adapter.getItem(position).getKey();
        Executor executor = viewModel.getExecutor();
        if (executor != null) executor.execute(() -> loadDataBefore(PAGE_SIZE, key));
        else loadDataBefore(PAGE_SIZE, key);
    }

    private void loadAfter(int position) {
        // verify we are not at the end of the data
        String lastInVM = viewModel.getData().lastKey();
        String lastInAdapter = adapter.getItem(-1).getKey();
        if (lastInAdapter.equalsIgnoreCase(lastInVM)) return;

        final String key = adapter.getItem(position).getKey();
        Executor executor = viewModel.getExecutor();
        if (executor != null) executor.execute(() -> loadDataAfter(PAGE_SIZE, key));
        else loadDataAfter(PAGE_SIZE, key);
    }

    private void loadDataBefore(int requestedLoadSize, String key) {
        ConcurrentNavigableMap<String, City> data = viewModel.getData();
        Set<City> cityTreeSet = new TreeSet<>(adapter.getList());

        String current = key;
        for (int i = 0; i < requestedLoadSize; i++) {
            if (current == null) break;
            cityTreeSet.add(data.get(current));
            current = data.lowerKey(current);
        }
        cityTreeSet.remove(data.get(key));
        List<City> output = new ArrayList<>(cityTreeSet);
        if (output.size() > MAX_LIST_SIZE) {
            int diff = output.size() - MAX_LIST_SIZE;
            output = output.subList(0, output.size() - diff);
        }
        updateAdapter(output);
    }

    private void loadDataAfter(int requestedLoadSize, String key) {
        ConcurrentNavigableMap<String, City> data = viewModel.getData();
        Set<City> cityTreeSet = new TreeSet<>(adapter.getList());

        String current = key;
        for (int i = 0; i < requestedLoadSize; i++) {
            if (current == null) break;
            cityTreeSet.add(data.get(current));
            current = data.higherKey(current);
        }
        List<City> output = new ArrayList<>(cityTreeSet);
        if (output.size() > MAX_LIST_SIZE) {
            int min = output.size() - MAX_LIST_SIZE;
            output = output.subList(min, output.size());
        }
        updateAdapter(output);
    }

    private void updateAdapter(final List<City> output) {
        if (output.isEmpty()) return; // prevent empty lists

        new Handler(Looper.getMainLooper()).post(() -> {
            if (adapter != null) adapter.setList(output);
        });
    }

    /**
     * notifies this helper to load the current (possibly filtered)
     * data from the viewModel into the adapter.
     */
    public void reloadData(City firstCity) {
        final String key = firstCity != null ? firstCity.getKey() : viewModel.getData().firstKey();
        Executor executor = viewModel.getExecutor();
        if (executor != null) executor.execute(() -> loadInitialData(key));
        else loadInitialData(key);
    }

    private void loadInitialData(String startKey) {
        ConcurrentNavigableMap<String, City> data = viewModel.getData();
        List<City> output = new ArrayList<>();

        String current = startKey;
        for (int i = 0; i < MAX_LIST_SIZE; i++) {
            if (current == null) break;
            output.add(data.get(current));
            current = data.higherKey(current);
        }
        updateAdapter(output);
    }

    /**
     * ties this helper to the given adapter.
     * This class is initiated when the activity is created, and destroyed
     * with the activity too. A CityAdapter is only available when data is
     * loaded and ready. This sets the adapter when data is ready.
     */
    void setAdapter(CityAdapter adapter) {
        this.adapter = adapter;
    }
}
