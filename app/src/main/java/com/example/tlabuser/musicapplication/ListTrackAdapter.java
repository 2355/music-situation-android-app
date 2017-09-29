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

public class ListTrackAdapter extends ArrayAdapter<Track> {

    LayoutInflater mInflater;
    int tracks;

    public ListTrackAdapter(Context context, List<Track> item){
        super(context, 0, item);
        mInflater =  (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        tracks = item.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        Track item = getItem(position);
        ViewHolder holder;

        if(convertView==null){
            convertView = mInflater.inflate(R.layout.item_track, null);
            holder = new ViewHolder();
            holder.trackTextView    = (TextView)convertView.findViewById(R.id.title);
            holder.artistTextView   = (TextView)convertView.findViewById(R.id.artist);
            holder.albumTextView   = (TextView)convertView.findViewById(R.id.album);
            holder.durationTextView = (TextView)convertView.findViewById(R.id.duration);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        long dm = item.duration/60000;
        long ds = (item.duration-(dm*60000))/1000;

        holder.trackTextView.setText(item.title);
        holder.artistTextView.setText(item.artist);
        holder.albumTextView.setText(item.album);
        holder.durationTextView.setText(String.format("%d:%02d",dm,ds));

        return convertView;
    }

    static class ViewHolder{
        TextView  trackTextView;
        TextView  artistTextView;
        TextView  albumTextView;
        TextView  durationTextView;
    }

    public int getTracks(){ return tracks; }

}