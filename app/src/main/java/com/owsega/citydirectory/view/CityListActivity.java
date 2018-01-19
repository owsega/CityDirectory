package com.owsega.citydirectory.view;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ViewSwitcher;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.stream.JsonReader;
import com.owsega.citydirectory.R;
import com.owsega.citydirectory.model.City;
import com.owsega.citydirectory.provider.CityAdapter.OnCityClickListener;
import com.owsega.citydirectory.provider.CityListViewModel;
import com.owsega.citydirectory.provider.CityPagedAdapter;

import java.io.InputStream;
import java.io.InputStreamReader;

import static com.owsega.citydirectory.provider.CityListViewModel.CITIES_FILE;

/**
 * An activity representing a list of Cities. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * opens a map representing item details by showing a map.
 * On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class CityListActivity extends AppCompatActivity
        implements OnCityClickListener, OnMapReadyCallback {

    CityListViewModel viewModel;
    /**
     * When the activity is not in single-pane mode, this view will not be null
     */
    private ViewSwitcher viewSwitcher;
    private CoordinatorLayout coordinator;
    private GoogleMap cityMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        coordinator = findViewById(R.id.coordinator);

        if (findViewById(R.id.view_switcher) != null) {
            // The view will be present only in the single-pane mode.
            // It will be absent in large-screen layouts (res/values-w900dp).
            // If this view is not present, then the activity should be in two-pane mode.
            viewSwitcher = findViewById(R.id.view_switcher);
        }

        setupViewModel();

        View recyclerView = findViewById(R.id.city_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        EditText editText = findViewById(R.id.search_view);
        assert editText != null;
        setupSearchView(editText);

        setupMap();
    }

    private void setupViewModel() {
        viewModel = ViewModelProviders.of(this).get(CityListViewModel.class);
        try {
            InputStream in = getApplicationContext().getAssets().open(CITIES_FILE);
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            viewModel.init(reader);
            reader.close();
        } catch (Exception e) {
            showError(getString(R.string.error_loading_cities));
            e.printStackTrace();
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        final CityPagedAdapter cityAdapter = new CityPagedAdapter(viewModel);
        viewModel.cityList.observe(this, pagedList -> {
            if (pagedList == null)
                Log.e("seyi", "CityListActivity just observed a null list ");
            else
                Log.e("seyi", "CityListActivity just observed a list " + pagedList.size());
            cityAdapter.setList(pagedList);
        });
        viewModel.selectedCity.observe(this, city -> {
            if (city != null) updateUiWithNewCity(city);
        });
        viewModel.emptyData.observe(this, isEmpty -> {
            Log.e("seyi", "emptyData bool observed " + isEmpty);
        });
        // recyclerView.setAdapter(new CityAdapter(this, this));
        recyclerView.setAdapter(cityAdapter);
    }

    private void setupSearchView(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.filterCities(s.toString().trim());
            }
        });
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.city_map);
        mapFragment.getMapAsync(this);
    }

    public void showError(CharSequence message) {
        Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG);
    }

    private void updateUiWithNewCity(@NonNull City city) {
        if (cityMap != null) {
            cityMap.animateCamera(
                    CameraUpdateFactory.newLatLng(
                            new LatLng(city.coord.lat, city.coord.lon)));

            if (viewSwitcher != null) {
                showDetail(true);
                getSupportActionBar().setTitle(city.toString());
            }
        }
    }

    /**
     * call with true to show detail, or false to show the list
     */
    public void showDetail(boolean shouldShow) {
        viewSwitcher.setDisplayedChild(shouldShow ? 1 : 0);

        if (!shouldShow) getSupportActionBar().setTitle(R.string.app_name);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        cityMap = googleMap;
    }

    @Override
    public void onCityClicked(String city) {
    }
}
