package com.taleckij_anton.taleckijapp.launcher;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.taleckij_anton.taleckijapp.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Lenovo on 31.01.2018.
 */

public class LauncherRecyclerFragment extends Fragment {
    public final static String LINEAR = "LINEAR";
    public final static String GRID = "GRID";
    public final static String LAYOUT_TYPE = "LAYOUT_TYPE";

    private final int RGB_CANALS_COUNT = 255;
    private final ArrayList<RecyclerItem> mItems = generateFakeData(1000);
    private final View.OnClickListener mFabOnClick = new View.OnClickListener() {
        final Random random = new Random();

        @Override
        public void onClick(View view) {
            mItems.add(0, new RecyclerItem(
                    "Some new String",
                    Color.argb(RGB_CANALS_COUNT,
                            random.nextInt(RGB_CANALS_COUNT),
                            random.nextInt(RGB_CANALS_COUNT),
                            random.nextInt(RGB_CANALS_COUNT)
                    ))
            );
            layoutManager.scrollToPosition(0);
            launcherRecyclerAdapter.notifyItemInserted(0);
        }
    };

    RecyclerView.LayoutManager layoutManager;
    LauncherRecyclerAdapter launcherRecyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.i("onCreateView", "+++++++++++++++++++++++++++++++++++++++++++"+getGridSpanCount());
        View view = inflater.inflate(
                R.layout.fragment_recycler_launcher, container, false);

        final RecyclerView recyclerView = view.findViewById(R.id.launcher_recycler_view);
        //final RecyclerView.LayoutManager layoutManager;
        //final LauncherRecyclerAdapter launcherRecyclerAdapter;
        if(getArguments().getString(LAYOUT_TYPE, GRID).equals(GRID)) {
            final int spanCount = getGridSpanCount();
            layoutManager = new GridLayoutManager(getActivity(), spanCount);
            launcherRecyclerAdapter = new LauncherRecyclerAdapter(R.layout.grid_item_launcher, mItems);
        } else {
            layoutManager = new LinearLayoutManager(getActivity());
            launcherRecyclerAdapter = new LauncherRecyclerAdapter(R.layout.linear_item_launcher, mItems);
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(launcherRecyclerAdapter);

        //FloatingActionButton fab = getActivity().findViewById(R.id.fab_launcher);
        FloatingActionButton fab = view.findViewById(R.id.fab_launcher);
        fab.setOnClickListener(mFabOnClick);

        return view;
    }

    private ArrayList<RecyclerItem> generateFakeData(int itemCount){
        final ArrayList<RecyclerItem> items = new ArrayList<>();
        Random random = new Random();
        int rgbCanalsCount = 255;
        for (int i = 0; i < itemCount; i++) {
            RecyclerItem item = new RecyclerItem(
                    "Some string" + i,
                        Color.argb(rgbCanalsCount,
                                random.nextInt(rgbCanalsCount),
                                random.nextInt(rgbCanalsCount),
                                random.nextInt(rgbCanalsCount)
                        )
            );
            items.add(item);
        }
        return items;
    }

    public class RecyclerItem{
        public final String text;
        public final @ColorInt int color;

        RecyclerItem(String text, @ColorInt int color){
            this.text = text;
            this.color = color;
        }
    }

    private int getGridSpanCount(){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String compactPrefKey = getResources().getString(R.string.compact_layout_preference_key);
        final boolean isCompact = sharedPreferences.getBoolean(compactPrefKey, false);
        return isCompact ?
                getResources().getInteger(R.integer.compact_views_count) :
                getResources().getInteger(R.integer.views_count);
    }
}

class SquareView extends View{
    public SquareView(final Context context) {
        super(context);
    }

    public SquareView(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
