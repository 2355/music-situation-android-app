package com.example.tlabuser.musicapplication.View.Root;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tlabuser.musicapplication.ImageCache;
import com.example.tlabuser.musicapplication.ImageGetTask;
import com.example.tlabuser.musicapplication.Model.Album;
import com.example.tlabuser.musicapplication.R;

import java.util.List;

/**
 * Created by tlabuser on 2017/07/13.
 */

public class ListAlbumAdapter extends ArrayAdapter<Album> {

    LayoutInflater mInflater;
    static Context Mcontext;

    public ListAlbumAdapter(Context context, List<Album> item){
        super(context, 0, item);
        mInflater =  (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        Mcontext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        Album item = getItem(position);
        ViewHolder holder;

        if(convertView==null){
            convertView = mInflater.inflate(R.layout.item_album, null);
            holder = new ViewHolder();
            holder.albumTextView    = (TextView)convertView.findViewById(R.id.album);
            holder.artistTextView   = (TextView)convertView.findViewById(R.id.artist);
            holder.tracksTextView   = (TextView)convertView.findViewById(R.id.tracks);
            holder.artworkImageView = (ImageView)convertView.findViewById(R.id.albumart);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.albumTextView.setText(item.album);
        holder.artistTextView.setText(item.artist);
        holder.tracksTextView.setText(String.valueOf(item.tracks)+"tracks");

        String path = item.albumArt;
        holder.artworkImageView.setImageResource(R.drawable.dummy_album_art);
        if(path==null){
            path = String.valueOf( R.drawable.dummy_album_art);
            Bitmap bitmap = ImageCache.getImage(path);
            if(bitmap==null){
                bitmap = BitmapFactory.decodeResource(Mcontext.getResources(),R.drawable.dummy_album_art);
                ImageCache.setImage(path, bitmap);
            }
        }
        holder.artworkImageView.setTag(path);
        ImageGetTask task = new ImageGetTask(holder.artworkImageView);
        task.execute(path);

        return convertView;
    }

    static class ViewHolder{
        TextView  albumTextView;
        TextView  artistTextView;
        TextView  tracksTextView;
        ImageView artworkImageView;
    }
}
