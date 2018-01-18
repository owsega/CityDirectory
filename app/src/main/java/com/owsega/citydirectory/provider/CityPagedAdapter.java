package com.owsega.citydirectory.provider;

import android.arch.paging.PagedListAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.owsega.citydirectory.R;
import com.owsega.citydirectory.model.City;
import com.owsega.citydirectory.provider.CityPagedAdapter.ViewHolder;

/**
 * Adapter for cities list.
 */
public class CityPagedAdapter extends PagedListAdapter<City, ViewHolder> {

    private final OnCityClickListener cityClickListener;
    private Gson gson = new Gson();
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            cityClickListener.onCityClicked(gson.toJson(view.getTag()));
        }
    };

    public CityPagedAdapter(OnCityClickListener listener) {
        super(City.DIFF_CALLBACK);
        cityClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.city_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        City city = getItem(position);
        if (city != null) {
            holder.bind(city);
        }
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

        void bind(City city) {
            textView.setText(city.toString());
            itemView.setTag(city);
            itemView.setOnClickListener(onClickListener);
        }
    }
}
    