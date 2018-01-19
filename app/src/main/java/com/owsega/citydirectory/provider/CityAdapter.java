package com.owsega.citydirectory.provider;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.owsega.citydirectory.R;
import com.owsega.citydirectory.model.City;
import com.owsega.citydirectory.view.CityListActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A recyclerview adapter for showing a list of cities
 */
public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {

    //    private static final String CITIES_FILE = "cities.json";
    private static final String CITIES_FILE = "smallcities.json";
    private final OnCityClickListener cityClickListener;
    private List<City> cities;
    private Gson gson = new Gson();
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            cityClickListener.onCityClicked(gson.toJson(view.getTag()));
        }
    };

    public CityAdapter(CityListActivity context, OnCityClickListener listener) {
        cityClickListener = listener;

        try {
            InputStream citiesStream = context.getAssets().open(CITIES_FILE);
            if (citiesStream != null) {
                // new LoadDataUtils(this, citiesStream).execute();
                cities = LoadDataUtils.getCities(citiesStream);
            }
        } catch (IOException e) {
            context.showError(context.getString(R.string.error_loading_cities));
            cities = new ArrayList<>();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.city_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.textView.setText(cities.get(position).toString());

        holder.itemView.setTag(cities.get(position));
        holder.itemView.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    public interface OnCityClickListener {
        void onCityClicked(String city);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.text);
        }
    }
}
    