package com.owsega.citydirectory.view;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ViewSwitcher;

import com.owsega.citydirectory.R;
import com.owsega.citydirectory.provider.CityAdapter.OnCityClickListener;
import com.owsega.citydirectory.provider.CityListViewModel;
import com.owsega.citydirectory.provider.CityPagedAdapter;

/**
 * An activity representing a list of Cities. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * opens a {@link CityDetailFragment} representing item details.
 * On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class CityListActivity extends AppCompatActivity implements OnCityClickListener, CityPagedAdapter.OnCityClickListener {

    CityListViewModel viewModel;
    /**
     * When the activity is not in single-pane mode, this view will not be null
     */
    private ViewSwitcher viewSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.view_switcher) != null) {
            // The view will be present only in the single-pane mode.
            // It will be absent in large-screen layouts (res/values-w900dp).
            // If this view is not present, then the activity should be in two-pane mode.
            viewSwitcher = findViewById(R.id.view_switcher);
        }

        View recyclerView = findViewById(R.id.city_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        EditText editText = findViewById(R.id.search_view);
        assert editText != null;
        setupSearchView(editText);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        // recyclerView.setAdapter(new CityAdapter(this, this));
        final CityPagedAdapter cityAdapter = new CityPagedAdapter(this);
        viewModel = ViewModelProviders.of(this).get(CityListViewModel.class);
        viewModel.init(this);
        viewModel.cityList.observe(this, cityAdapter::setList);
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
                filterList(s.toString());
            }
        });
    }

    private void filterList(String text) {
        viewModel.filterCities(text);
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
    }

    @Override
    public void onCityClicked(String city) {
        Bundle arguments = new Bundle();
        arguments.putString(CityDetailFragment.ARG_CITY, city);
        CityDetailFragment fragment = new CityDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.city_detail_container, fragment)
                .commit();

        if (viewSwitcher != null) {
            showDetail(true);
        }
    }

    public void showError(CharSequence message) {
        Snackbar.make(viewSwitcher, message, Snackbar.LENGTH_LONG);
    }
}
