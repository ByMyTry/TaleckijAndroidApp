package com.taleckij_anton.taleckijapp.launcher.recycler_training;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.taleckij_anton.taleckijapp.R;

import java.util.ArrayList;

/**
 * Created by Lenovo on 03.02.2018.
 */

public class LauncherRecyclerAdapter extends RecyclerView.Adapter<LauncherItemHolder>{
    private final int mItemLayoutId;
    private final ArrayList<LauncherRecyclerFragment.RecyclerItem> mItems;

    LauncherRecyclerAdapter(int itemLayoutId,ArrayList<LauncherRecyclerFragment.RecyclerItem> items){
        super();
        mItemLayoutId = itemLayoutId;
        mItems = items;
    }

    @Override
    public LauncherItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(
                mItemLayoutId, parent, false);
        final LauncherItemHolder launcherItemHolder = new LauncherItemHolder(view);

        launcherItemHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                    /*Toast.makeText(getActivity(), launcherItemHolder.getText(), Toast.LENGTH_SHORT)
                            .show();*/
                createSnackbarWithAction(snackbarOnClickListener, view)
                        .show();
                return true;
            }

            Snackbar createSnackbarWithAction(View.OnClickListener snackbarOnClickListener, View view){
                //final View coordinator = view.findViewById(R.id.coordinator_parent_recycler);
                final String snackbareMessage = view.getResources().getString(R.string.snackbar_delete_message);
                final String snackbareActionText = view.getResources().getString(R.string.snackbar_action_text);
                final Integer snackbareDuration = view.getResources().getInteger(R.integer.snackbar_duration_millis);

                Snackbar snackbar = Snackbar.make(view, snackbareMessage, snackbareDuration)
                        .setAction(snackbareActionText, snackbarOnClickListener);

                return snackbar;
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
        LauncherRecyclerFragment.RecyclerItem item = mItems.get(position);
        launcherItemHolder.bind(item.text, item.color);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
