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

    private LayoutInflater inflater;
    private Context context;

    public ListAlbumAdapter(Context context, List<Album> item){
        super(context, 0, item);
        this.inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        Album item = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_album, null);
            holder = new ViewHolder();
            holder.tvAlbum  = (TextView)convertView.findViewById(R.id.album);
            holder.tvArtist = (TextView)convertView.findViewById(R.id.artist);
            holder.tvTracks = (TextView)convertView.findViewById(R.id.tracks);
            holder.ivAlbumArt = (ImageView)convertView.findViewById(R.id.albumart);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvAlbum.setText(item.album);
        holder.tvArtist.setText(item.artist);
        holder.tvTracks.setText(String.valueOf(item.tracks)+" tracks");

        String path = item.albumArt;
        holder.ivAlbumArt.setImageResource(R.drawable.icon_album);
        if (path == null) {
            path = String.valueOf(R.drawable.icon_album);
            Bitmap bitmap = ImageCache.getImage(path);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_album);
                ImageCache.setImage(path, bitmap);
            }
        }
        holder.ivAlbumArt.setTag(path);
        ImageGetTask task = new ImageGetTask(holder.ivAlbumArt);
        task.execute(path);

        return convertView;
    }

    static class ViewHolder {
        TextView tvAlbum;
        TextView tvArtist;
        TextView tvTracks;
        ImageView ivAlbumArt;
    }
}
