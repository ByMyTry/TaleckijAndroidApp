package com.taleckij_anton.taleckijapp;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Lenovo on 31.01.2018.
 */

public class LauncherGridFragment extends Fragment {
    public final static String LINEAR = "LINEAR";
    public final static String GRID = "GRID";
    public final static String LAYOUT_TYPE = "LAYOUT_TYPE";


    private final ArrayList<String> mItems = generateFakeData(1000);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_recycler_launcher, container, false);

        final RecyclerView recyclerView = view.findViewById(R.id.launcher_recycler_view);
        final RecyclerView.LayoutManager layoutManager;
        final LauncherRecyclerAdapter launcherRecyclerAdapter;
        if(getArguments().getString(LAYOUT_TYPE).equals(GRID)) {
            final int spanCount = getGridSpanCount();
            layoutManager = new GridLayoutManager(getActivity(), spanCount);
            launcherRecyclerAdapter = new LauncherRecyclerAdapter(R.layout.grid_item_launcher) ;
        } else {
            layoutManager = new LinearLayoutManager(getActivity());
            launcherRecyclerAdapter = new LauncherRecyclerAdapter(R.layout.linear_item_launcher);
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(launcherRecyclerAdapter);

        FloatingActionButton fab = getActivity().findViewById(R.id.fab_launcher);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItems.add(0, "Some new String");
                layoutManager.scrollToPosition(0);
                launcherRecyclerAdapter.notifyItemInserted(0);
            }
        });

        return view;
    }

    private ArrayList<String> generateFakeData(int itemCount){
        final ArrayList<String> items = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            items.add("Some string" + i);
        }
        return items;
    }

    private int getGridSpanCount(){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String compactPrefKey = getResources().getString(R.string.compact_layout_preference_key);
        final boolean isCompact = sharedPreferences.getBoolean(compactPrefKey, false);
        return isCompact ?
                getResources().getInteger(R.integer.compact_views_count) :
                getResources().getInteger(R.integer.views_count);
    }

    private Snackbar createSnackbarWithAction(View.OnClickListener snackbarOnClickListener){
        final View coordinator = getActivity().findViewById(R.id.coordinator_parent_recycler);
        final String snackbareMessage = getResources().getString(R.string.snackbar_delete_message);
        final String snackbareActionText = getResources().getString(R.string.snackbar_action_text);
        final Integer snackbareDuration = getResources().getInteger(R.integer.snackbar_duration_millis);

        Snackbar snackbar = Snackbar.make(coordinator, snackbareMessage, snackbareDuration)
                .setAction(snackbareActionText, snackbarOnClickListener);

        return snackbar;
    }

    class LauncherItemHolder extends RecyclerView.ViewHolder {
        private final TextView mItemTextView;

        LauncherItemHolder(View itemView) {
            super(itemView);
            mItemTextView = itemView.findViewById(R.id.launcher_item_text);
        }

        void bind(String itemText){
            mItemTextView.setText(itemText);
        }

        public String getText(){
            return mItemTextView.getText().toString();
        }
    }

    class LauncherRecyclerAdapter extends RecyclerView.Adapter<LauncherItemHolder>{
        private final int mItemLayoutId;

        LauncherRecyclerAdapter(int itemLayoutId){
            super();
            mItemLayoutId = itemLayoutId;
        }

        @Override
        public LauncherItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(
                    mItemLayoutId, parent, false);
            final LauncherItemHolder launcherItemHolder = new LauncherItemHolder(view);

            launcherItemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(), launcherItemHolder.getText(), Toast.LENGTH_SHORT)
                            .show();
                    createSnackbarWithAction(snackbarOnClickListener)
                            .show();
                }

                View.OnClickListener snackbarOnClickListener = new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        mItems.remove(launcherItemHolder.getAdapterPosition());
                        notifyItemRemoved(launcherItemHolder.getAdapterPosition());
                    }
                };
            });

            return launcherItemHolder;
        }

        @Override
        public void onBindViewHolder(final LauncherItemHolder launcherItemHolder, int position) {
            launcherItemHolder.bind(mItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }
}
