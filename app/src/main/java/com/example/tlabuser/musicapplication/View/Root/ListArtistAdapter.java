package com.example.tlabuser.musicapplication.View.Root;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tlabuser.musicapplication.Model.Artist;
import com.example.tlabuser.musicapplication.R;

import java.util.List;

/**
 * set values to Views in item_artist
 */

public class ListArtistAdapter extends ArrayAdapter<Artist> {

    LayoutInflater mInflater;

    public ListArtistAdapter(Context context, List<Artist> item){
        super(context, 0, item);
        mInflater =  (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        Artist item = getItem(position);
        ViewHolder holder;

        if(convertView==null){
            convertView = mInflater.inflate(R.layout.item_artist, null);
            holder = new ViewHolder();
            holder.artistTextView   = (TextView)convertView.findViewById(R.id.artist);
            holder.albumsTextView   = (TextView)convertView.findViewById(R.id.albums);
            holder.tracksTextView   = (TextView)convertView.findViewById(R.id.tracks);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.artistTextView.setText(item.artist);
        holder.albumsTextView.setText(String.format("%d Albums", item.albums));
        holder.tracksTextView.setText(String.format("%d tracks", item.tracks));

        return convertView;
    }

    static class ViewHolder{
        TextView  artistTextView;
        TextView  albumsTextView;
        TextView  tracksTextView;
    }

}