package com.owsega.citydirectory.provider;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.owsega.citydirectory.R;
import com.owsega.citydirectory.model.City;
import com.owsega.citydirectory.view.CityDetailFragment;
import com.owsega.citydirectory.view.CityListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A recyclerview adapter for showing a list of cities
 */
public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {

    private Gson gson = new Gson();
    private final CityListActivity mParentActivity;
    private final List<City> mValues;
    private final boolean mTwoPane;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            City item = (City) view.getTag();
            Bundle arguments = new Bundle();
            arguments.putString(CityDetailFragment.ARG_ITEM, gson.toJson(item));
            CityDetailFragment fragment = new CityDetailFragment();
            fragment.setArguments(arguments);
            mParentActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.city_detail_container, fragment)
                    .commit();

            if (!mTwoPane) {
                Context context = view.getContext();
                CityListActivity activity = (CityListActivity) context;
                if (activity != null) {
                    activity.showDetail(true);
                }
            }
        }
    };

    public CityAdapter(CityListActivity parent, boolean twoPane) {
        mValues = new ArrayList<>();
        mParentActivity = parent;
        mTwoPane = twoPane;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.city_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mView.setText(mValues.get(position).toString());

        holder.itemView.setTag(mValues.get(position));
        holder.itemView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mView;

        ViewHolder(View view) {
            super(view);
            mView = view.findViewById(R.id.text);
        }
    }
}
    