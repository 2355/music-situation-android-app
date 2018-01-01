package com.example.tlabuser.musicapplication.View.Album;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tlabuser.musicapplication.Model.ExTrack;
import com.example.tlabuser.musicapplication.R;

import java.util.List;

public class ListExTrackAlbumAdapter extends ArrayAdapter<ExTrack> {

    private LayoutInflater mInflater;

    public ListExTrackAlbumAdapter(Context context, List<ExTrack> item){
        super(context, 0, item);
        mInflater =  (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        ExTrack item = getItem(position);
        ViewHolder holder;

        if(convertView==null){
            convertView = mInflater.inflate(R.layout.item_extrack_album, null);
            holder = new ViewHolder();
            holder.tvTrackNo  = (TextView)convertView.findViewById(R.id.track_no);
            holder.tvTitle    = (TextView)convertView.findViewById(R.id.title);
            holder.tvArtist   = (TextView)convertView.findViewById(R.id.artist);
            holder.tvDuration = (TextView)convertView.findViewById(R.id.duration);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        long dm = item.duration/60000;
        long ds = (item.duration-(dm*60000))/1000;

        holder.tvTrackNo.setText(String.valueOf(item.trackNo));
        holder.tvTitle.setText(item.title);
        holder.tvArtist.setText(item.artist);
        holder.tvDuration.setText(String.format("%d:%02d",dm,ds));

        return convertView;
    }

    static class ViewHolder{
        TextView tvTrackNo;
        TextView tvTitle;
        TextView tvArtist;
        TextView tvDuration;
    }

}
