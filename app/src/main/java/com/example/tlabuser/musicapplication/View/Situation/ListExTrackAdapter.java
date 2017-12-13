package com.example.tlabuser.musicapplication.View.Situation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tlabuser.musicapplication.Model.ExTrack;
import com.example.tlabuser.musicapplication.R;

import java.util.List;

/**
 * Created by tlabuser on 2017/07/12.
 */

public class ListExTrackAdapter extends ArrayAdapter<ExTrack> {

    LayoutInflater mInflater;
    int tracks;

    public ListExTrackAdapter(Context context, List<ExTrack> exTracks){
        super(context, 0, exTracks);
        mInflater =  (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        tracks = exTracks.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        ExTrack exTrack = getItem(position);
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

        holder.trackTextView.setText(exTrack.title);
        holder.artistTextView.setText(exTrack.artist);
        holder.weightTextView.setText(String.valueOf(exTrack.weight));

        return convertView;
    }

    static class ViewHolder{
        TextView  trackTextView;
        TextView  artistTextView;
        TextView  weightTextView;
    }

    public int getTracks(){ return tracks; }

}