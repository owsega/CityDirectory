package com.owsega.citydirectory.viewmodel;

import android.support.v7.util.DiffUtil;
import android.support.v7.util.DiffUtil.DiffResult;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.owsega.citydirectory.R;
import com.owsega.citydirectory.model.City;
import com.owsega.citydirectory.model.CityDiffCallback;

import java.util.List;

/**
 * A {@link RecyclerView.Adapter} for a list of City objects
 */
public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {

    private final OnCityClickListener cityClickListener;
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            cityClickListener.onCityClicked((City) view.getTag());
        }
    };

    private List<City> currentList;
    private CityAdapterHelper helper;

    public CityAdapter(CityListViewModel listener, CityAdapterHelper helper) {
        cityClickListener = listener;
        this.helper = helper;
        this.helper.setAdapter(this);
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
            holder.textView.setText(city.toString());
            holder.itemView.setTag(city);
            holder.itemView.setOnClickListener(onClickListener);
        }
        helper.loadAround(position);
    }

    /**
     * @param position position to return item for, between 0 and listSize - 1. Use -1 to return the last item in the list.
     * @return the City object tied to the given position in the current list (page)
     */
    public City getItem(int position) {
        if (currentList == null) {
            throw new IndexOutOfBoundsException("Null list, getItem() call is invalid");
        } else if (position == -1) {  // helper for getting the last item
            return currentList.get(currentList.size() - 1);
        } else {
            return currentList.get(position);
        }
    }

    @Override
    public int getItemCount() {
        return currentList == null ? 0 : currentList.size();
    }

    public List<City> getList() {
        return currentList;
    }

    /**
     * Set the new list to be displayed.
     * <p>
     * If a list is already being displayed, a diff will be computed on a background thread, which
     * will dispatch Adapter.notifyItem events on the main thread.
     *
     * @param newList The new list to be displayed.
     */
    public void setList(List<City> newList) {
        // if list difference is > 100, just use the old style, to save time
        // the drawback is that it resets the current position
        if (currentList == null || Math.abs(newList.size() - currentList.size()) > 100) {
            this.currentList = newList;
            notifyDataSetChanged();
        } else {
            DiffResult diffResult = DiffUtil.calculateDiff(new CityDiffCallback(currentList, newList));
            this.currentList = newList;
            diffResult.dispatchUpdatesTo(this);
        }
    }

    /**
     * Listener for clicks on {@link City}s in the UI
     */
    public interface OnCityClickListener {
        /**
         * @param city the City object clicked
         */
        void onCityClicked(City city);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.text);
        }
    }

}
    