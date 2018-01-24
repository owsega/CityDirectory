package com.owsega.citydirectory;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ViewSwitcher;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.stream.JsonReader;
import com.owsega.citydirectory.model.City;
import com.owsega.citydirectory.viewmodel.CityAdapter;
import com.owsega.citydirectory.viewmodel.CityAdapterHelper;
import com.owsega.citydirectory.viewmodel.CityListViewModel;
import com.owsega.citydirectory.viewmodel.CityListViewModel.UpdateListener;

import java.io.InputStream;
import java.io.InputStreamReader;

import static com.owsega.citydirectory.viewmodel.CityListViewModel.CITIES_FILE;

/**
 * An activity representing a list of Cities. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * opens a map representing item details by showing a map.
 * On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class CityListActivity extends AppCompatActivity implements OnMapReadyCallback, UpdateListener {

    private static final String SHOWN_CITY = "currentCity";
    CityListViewModel viewModel;
    CityAdapterHelper cityAdapterHelper;

    /**
     * When the activity is not in single-pane mode, this view will not be null
     */
    private ViewSwitcher viewSwitcher;
    /**
     * toggles between the list view and a text view (for empty lists scenario)
     */
    private ViewSwitcher listViewWrapper;
    private CoordinatorLayout coordinator;
    private GoogleMap cityMap;
    /**
     * currently shown city (null, if the current view is not a Map)
     */
    private City shownCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        coordinator = findViewById(R.id.coordinator);
        listViewWrapper = findViewById(R.id.listViewWrapper);

        if (findViewById(R.id.view_switcher) != null) {
            // The view will be present only in the single-pane mode.
            // It will be absent in large-screen layouts (res/values-w900dp).
            // If this view is not present, then the activity should be in two-pane mode.
            viewSwitcher = findViewById(R.id.view_switcher);
        }

        if (savedInstanceState != null) {
            shownCity = (City) savedInstanceState.getSerializable(SHOWN_CITY);
        }

        setupMap();
        setupViewModel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewModel != null) viewModel.removeUpdateListener(this);
        cityAdapterHelper = null;
        viewModel = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (viewSwitcher.getDisplayedChild() == 1 && shownCity != null) {
            outState.putSerializable(SHOWN_CITY, shownCity);
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.city_map);
        mapFragment.getMapAsync(this);
    }

    private void setupViewModel() {
        viewModel = ((CityDirectory) getApplication()).viewModel;
        viewModel.addUpdateListener(this);
        cityAdapterHelper = new CityAdapterHelper(viewModel);

        try {
            InputStream in = getApplicationContext().getAssets().open(CITIES_FILE);
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            viewModel.init(reader, true);
        } catch (Exception e) {
            showError(getString(R.string.error_loading_cities));
            e.printStackTrace();
        }
    }

    public void showError(CharSequence message) {
        Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG);
    }

    @Override
    public void onBackPressed() {
        // if current view is the detail view then show list
        if (viewSwitcher != null && viewSwitcher.getDisplayedChild() == 1) {
            showDetail(false);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * call with true to show detail, or false to show the list
     */
    public void showDetail(boolean shouldShow) {
        viewSwitcher.setDisplayedChild(shouldShow ? 1 : 0);

        if (!shouldShow) {
            getSupportActionBar().setTitle(R.string.app_name);
            shownCity = null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        cityMap = googleMap;
        if (shownCity != null) { // load savedInstanceState's city
            onCitySelected(shownCity);
        }
    }

    /**
     * call with true to show message indicating no data on the list, or false to show the list
     */
    @Override
    public void onEmptyData(boolean isEmpty) {
        listViewWrapper.setDisplayedChild(isEmpty ? 1 : 0);
        if (!isEmpty) cityAdapterHelper.reloadData();
    }

    @Override
    public void onCitySelected(City city) {
        if (cityMap != null) {
            cityMap.animateCamera(
                    CameraUpdateFactory.newLatLng(
                            new LatLng(city.coord.lat, city.coord.lon)));

            if (viewSwitcher != null) {
                shownCity = city;
                showDetail(true);
                getSupportActionBar().setTitle(city.toString());
            }
        }
    }

    @Override
    public void onDataReady(boolean dataReady) {
        if (dataReady) {
            RecyclerView recyclerView = findViewById(R.id.city_list);
            CityAdapter cityAdapter = new CityAdapter(viewModel, cityAdapterHelper);
            recyclerView.setAdapter(cityAdapter);

            EditText editText = findViewById(R.id.search_view);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    viewModel.filterCities(s.toString());
                }
            });

            hideProgressBar();
        }
    }

    private void hideProgressBar() {
        TransitionManager.beginDelayedTransition(coordinator);
        findViewById(R.id.frameLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }
}
