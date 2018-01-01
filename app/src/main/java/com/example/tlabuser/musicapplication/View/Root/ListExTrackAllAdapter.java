package com.example.tlabuser.musicapplication.View.Root;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tlabuser.musicapplication.Model.ExTrack;
import com.example.tlabuser.musicapplication.R;

import java.util.List;

public class ListExTrackAllAdapter extends ArrayAdapter<ExTrack> {

    private LayoutInflater mInflater;
    private int tracks;

    public ListExTrackAllAdapter(Context context, List<ExTrack> item){
        super(context, 0, item);
        mInflater =  (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        tracks = item.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        ExTrack item = getItem(position);
        ViewHolder holder;

        if(convertView==null){
            convertView = mInflater.inflate(R.layout.item_extrack_all, null);
            holder = new ViewHolder();
            holder.tvTitle    = (TextView)convertView.findViewById(R.id.title);
            holder.tvArtist   = (TextView)convertView.findViewById(R.id.artist);
            holder.tvAlbum    = (TextView)convertView.findViewById(R.id.album);
            holder.tvDuration = (TextView)convertView.findViewById(R.id.duration);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        long dm = item.duration/60000;
        long ds = (item.duration-(dm*60000))/1000;

        holder.tvTitle.setText(item.title);
        holder.tvArtist.setText(item.artist);
        holder.tvAlbum.setText(item.album);
        holder.tvDuration.setText(String.format("%d:%02d",dm,ds));

        return convertView;
    }

    static class ViewHolder{
        TextView tvTitle;
        TextView tvArtist;
        TextView tvAlbum;
        TextView tvDuration;
    }

    public int getTracks(){ return tracks; }

}
