package com.example.tlabuser.musicapplication.View.Root;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tlabuser.musicapplication.ImageGetTask;
import com.example.tlabuser.musicapplication.Model.ExTrack;
import com.example.tlabuser.musicapplication.R;

import java.util.List;

public class ListExTrackAllAdapter extends ArrayAdapter<ExTrack> {

    private LayoutInflater inflater;
    private Context context;
    private int tracks;

    public ListExTrackAllAdapter(Context context, List<ExTrack> item){
        super(context, 0, item);
        this.inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.context = context;

        tracks = item.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        ExTrack item = getItem(position);
        ViewHolder holder;

        if(convertView==null){
            convertView = inflater.inflate(R.layout.item_extrack_all, null);
            holder = new ViewHolder();
            holder.tvTitle    = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tvArtist   = (TextView) convertView.findViewById(R.id.tv_artist);
            holder.tvAlbum    = (TextView) convertView.findViewById(R.id.tv_album);
            holder.tvDuration = (TextView) convertView.findViewById(R.id.tv_duration);
            holder.ivAlbumArt = (ImageView) convertView.findViewById(R.id.iv_album_art);
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

        // TODO albumArt表示
        holder.ivAlbumArt.setImageResource(R.drawable.icon_track);
        String path = item.albumArt;
        /*
        if (path == null || path == "") {
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
        */
        if (path != null && path != "") {
            holder.ivAlbumArt.setTag(path);
            ImageGetTask task = new ImageGetTask(holder.ivAlbumArt);
            task.execute(path);
        }

        return convertView;
    }

    static class ViewHolder{
        TextView tvTitle;
        TextView tvArtist;
        TextView tvAlbum;
        TextView tvDuration;
        ImageView ivAlbumArt;
    }

    public int getTracks(){ return tracks; }

}
