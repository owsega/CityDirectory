package com.owsega.citydirectory.viewmodel;

import android.support.annotation.Nullable;
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

import static android.support.v7.util.DiffUtil.calculateDiff;

/**
 * Adapter a list of for City objects
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

    public CityAdapter(OnCityClickListener listener) {
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
            holder.textView.setText(city.toString());
            holder.itemView.setTag(city);
            holder.itemView.setOnClickListener(onClickListener);
        }
    }

    @Nullable
    City getItem(int position) {
        if (currentList == null) {
            throw new IndexOutOfBoundsException(
                    "Item count is zero, getItem() call is invalid");
        } else {
            //todo loadAround(index);
            return currentList.get(position);
        }
    }

    @Override
    public int getItemCount() {
        return currentList == null ? 0 : currentList.size();
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
        DiffResult diffResult = calculateDiff(new CityDiffCallback(this.currentList, newList));
        this.currentList = newList;
        diffResult.dispatchUpdatesTo(this);
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
    