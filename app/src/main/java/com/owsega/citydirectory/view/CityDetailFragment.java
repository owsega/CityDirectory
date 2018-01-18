package com.owsega.citydirectory.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.owsega.citydirectory.R;
import com.owsega.citydirectory.model.City;

/**
 * A fragment representing a single City detail screen.
 */
public class CityDetailFragment extends Fragment {
    /**
     * The fragment argument representing the city that this fragment shows.
     */
    public static final String ARG_ITEM = "item";

    /**
     * The content this fragment is presenting.
     */
    private City city;

    public CityDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_ITEM)) {
            // Load the content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            city = new Gson().fromJson(getArguments().getString(ARG_ITEM), City.class);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.city_detail, container, false);

        if (city != null) {
            ((TextView) rootView.findViewById(R.id.city_detail)).setText(city.toString());
        }

        return rootView;
    }
}
