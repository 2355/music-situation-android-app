package com.example.tlabuser.musicapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by tlabuser on 2017/07/12.
 */

public class ListTrackAdapter3 extends ArrayAdapter<SituationTrack> {

    LayoutInflater mInflater;
    int tracks;

    public ListTrackAdapter3(Context context, List<SituationTrack> item){
        super(context, 0, item);
        mInflater =  (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        tracks = item.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        SituationTrack item = getItem(position);
        ViewHolder holder;

        if(convertView==null){
            convertView = mInflater.inflate(R.layout.item_track3, null);
            holder = new ViewHolder();
            holder.trackTextView  = (TextView)convertView.findViewById(R.id.title);
            holder.artistTextView = (TextView)convertView.findViewById(R.id.artist);
            holder.weightTextView = (TextView)convertView.findViewById(R.id.weight);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.trackTextView.setText(item.title);
        holder.artistTextView.setText(item.artist);
        holder.weightTextView.setText(String.valueOf(item.weight));

        return convertView;
    }

    static class ViewHolder{
        TextView  trackTextView;
        TextView  artistTextView;
        TextView  weightTextView;
    }

    public int getTracks(){ return tracks; }

}