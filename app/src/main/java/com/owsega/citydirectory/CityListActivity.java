package com.owsega.citydirectory;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.owsega.citydirectory.dummy.DummyContent;

import java.util.List;

/**
 * An activity representing a list of Cities. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * opens a {@link CityDetailFragment} representing item details.
 * On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class CityListActivity extends AppCompatActivity {

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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (findViewById(R.id.view_switcher) != null) {
            // The view will be present only in the single-pane mode.
            // It will be absent in large-screen layouts (res/values-w900dp).
            // If this view is not present, then the activity should be in two-pane mode.
            viewSwitcher = findViewById(R.id.view_switcher);
        }

        View recyclerView = findViewById(R.id.city_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        boolean mTwoPane = viewSwitcher == null;
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, DummyContent.ITEMS, mTwoPane));
    }

    @Override
    public void onBackPressed() {
        if (viewSwitcher != null) {
            // if current view is the detail view then show list
            View detail = viewSwitcher.getChildAt(1);
            if (viewSwitcher.getCurrentView() == detail) {
                showDetail(false);
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    /**
     * call with true to show detail, or false to show the list
     */
    public void showDetail(boolean shouldShow) {
        Log.e("seyi", "inside showDetail");
        viewSwitcher.setDisplayedChild(shouldShow ? 1 : 0);
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final CityListActivity mParentActivity;
        private final List<DummyContent.DummyItem> mValues;
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

        SimpleItemRecyclerViewAdapter(CityListActivity parent,
                                      List<DummyContent.DummyItem> items,
                                      boolean twoPane) {

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
}
