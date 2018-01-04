package com.example.tlabuser.musicapplication.View.Situation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tlabuser.musicapplication.Model.ExTrack;
import com.example.tlabuser.musicapplication.Model.Situation;
import com.example.tlabuser.musicapplication.R;

import java.util.List;

/**
 * Created by tlabuser on 2017/07/12.
 */

public class ListExTrackSituationAdapter extends ArrayAdapter<ExTrack> {

    private LayoutInflater mInflater;
    private String situationName;
    private int tracks;

    public ListExTrackSituationAdapter(Context context, Situation situation, List<ExTrack> exTracks){
        super(context, 0, exTracks);
        mInflater =  (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        this.situationName = situation.name;
        this.tracks = situation.tracks;
        this.tracks = exTracks.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        ExTrack exTrack = getItem(position);
        ViewHolder holder;

        if(convertView==null){
            convertView = mInflater.inflate(R.layout.item_extrack_situation, null);
            holder = new ViewHolder();
            holder.tvTitle = (TextView)convertView.findViewById(R.id.title);
            holder.tvArtist = (TextView)convertView.findViewById(R.id.artist);
            holder.tvWeight = (TextView)convertView.findViewById(R.id.weight);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvTitle.setText(exTrack.title);
        holder.tvArtist.setText(exTrack.artist);
        holder.tvWeight.setText(String.valueOf(exTrack.situationMap.get(situationName).weight));

        return convertView;
    }

    static class ViewHolder{
        TextView tvTitle;
        TextView tvArtist;
        TextView tvWeight;
    }

    public int getTracks(){ return tracks; }

}