package com.example.tlabuser.musicapplication.View.Root;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tlabuser.musicapplication.Model.Situation;
import com.example.tlabuser.musicapplication.R;

import java.util.List;

/**
 * Created by tlabuser on 2017/12/15.
 */

public class SituationsRecyclerAdapter extends RecyclerView.Adapter<SituationsRecyclerAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private Context context;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    private List<Situation> situations;

    public SituationsRecyclerAdapter(Context context, List<Situation> situations) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.situations = situations;
    }

    @Override
    public SituationsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(inflater.inflate(R.layout.item_situation, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Situation situation = situations.get(position);

        viewHolder.tvSituation.setText(situation.name);
        viewHolder.tvTracks.setText(String.valueOf(situation.tracks)+" tracks");
        if (situation.tracks != 0) {
            viewHolder.tvTracks.setVisibility(View.VISIBLE);
        }

        viewHolder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(situation);
        });

        viewHolder.itemView.setOnLongClickListener(v -> {
            // TODO situationの再取得などができるダイアログを出す
            if (longClickListener != null) longClickListener.onItemLongClick(situation);
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return situations.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSituation;
        TextView tvTracks;

        public ViewHolder(View itemView) {
            super(itemView);
            tvSituation = (TextView) itemView.findViewById(R.id.tv_situation);
            tvTracks    = (TextView) itemView.findViewById(R.id.tv_tracks);
        }
    }

    public void setOnItemClickedListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Situation situation);
    }

    public void setOnItemLongClickedListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Situation situation);
    }
}