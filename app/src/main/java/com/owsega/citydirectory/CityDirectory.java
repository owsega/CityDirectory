package com.owsega.citydirectory;

import android.app.Application;

import com.owsega.citydirectory.viewmodel.CityListViewModel;

/**
 * The Application class
 * <p>
 * Holds the {@link CityListViewModel} and the heavy data while the application lives.
 * Ideally, we do not really need to have the data in the application context. However,
 * since the application currently only has one Activity, and we'd rather not reload the
 * data every time, it lives here for now. It could also live in non-ui fragment with
 * setRetainInstance(true) living inside an activity that needs it.
 */
public class CityDirectory extends Application {

    /**
     * only {@link CityListActivity} needs access right now.
     * No need to have it public
     */
    CityListViewModel viewModel;

    @Override
    public void onCreate() {
        super.onCreate();

        viewModel = new CityListViewModel();
    }
}
