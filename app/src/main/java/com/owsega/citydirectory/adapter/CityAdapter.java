package com.owsega.citydirectory.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.owsega.citydirectory.R;
import com.owsega.citydirectory.model.DummyContent;
import com.owsega.citydirectory.model.DummyContent.DummyItem;
import com.owsega.citydirectory.view.CityDetailFragment;
import com.owsega.citydirectory.view.CityListActivity;

import java.util.List;

/**
 * A recyclerview adapter for showing a list of cities
 */
public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {

    private final CityListActivity mParentActivity;
    private final List<DummyItem> mValues;
    private final boolean mTwoPane;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DummyContent.DummyItem item = (DummyContent.DummyItem) view.getTag();
            Log.e("seyi", "inside onClick RecyView");
            Log.e("seyi", "inside onClick RecyView " + mTwoPane);
            Bundle arguments = new Bundle();
            arguments.putString(CityDetailFragment.ARG_ITEM_ID, item.id);
            CityDetailFragment fragment = new CityDetailFragment();
            fragment.setArguments(arguments);
            mParentActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.city_detail_container, fragment)
                    .commit();

            if (!mTwoPane) {
                Context context = view.getContext();
                CityListActivity activity = (CityListActivity) context;
                Log.e("seyi", "accitivyt " + (activity == null));
                if (activity != null) {
                    activity.showDetail(true);
                }
            }
        }
    };

    public CityAdapter(CityListActivity parent, List<DummyContent.DummyItem> items, boolean twoPane) {
        mValues = items;
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
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).content);

        holder.itemView.setTag(mValues.get(position));
        holder.itemView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mIdView;
        final TextView mContentView;

        ViewHolder(View view) {
            super(view);
            mIdView = view.findViewById(R.id.id_text);
            mContentView = view.findViewById(R.id.content);
        }
    }
}
    