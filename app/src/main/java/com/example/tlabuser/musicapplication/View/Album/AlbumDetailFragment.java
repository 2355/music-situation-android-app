package com.example.tlabuser.musicapplication.View.Album;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tlabuser.musicapplication.ImageGetTask;
import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.Model.Album;
import com.example.tlabuser.musicapplication.Model.Track;
import com.example.tlabuser.musicapplication.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumDetailFragment extends Fragment{

    private static Album album_item;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View partView =inflater.inflate(R.layout.part_album, container, false);

        Main activity = (Main)getActivity();
        album_item = activity.getFocusedAlbum();

        TextView album_title  = (TextView) partView.findViewById(R.id.album);
        TextView album_artist = (TextView) partView.findViewById(R.id.artist);
        TextView album_tracks = (TextView) partView.findViewById(R.id.tracks);
        ImageView album_art   = (ImageView) partView.findViewById(R.id.albumart);

        album_title.setText(album_item.album);
        album_artist.setText(album_item.artist);
        album_tracks.setText(String.valueOf(album_item.tracks)+"tracks");

        String path = album_item.albumArt;
        album_art.setImageResource(R.drawable.dummy_album_art);
        if(path!=null){
            album_art.setTag(path);
            ImageGetTask task = new ImageGetTask(album_art);
            task.execute(path);
        }

        /*
        partView.findViewById(R.id.album_info).setOnClickListener(new View.OnClickListener()
        {@Override public void onClick(View v) {}});
        partView.findViewById(R.id.tracktitle).setOnClickListener(new View.OnClickListener()
        {@Override public void onClick(View v) {}});
        */

        List tracks  = Track.getItemsByAlbum(getActivity(), album_item.albumId);

        ListView trackList = (ListView) partView.findViewById(R.id.track_list);
        ListTrackAdapter2 adapter = new ListTrackAdapter2(activity, tracks);
        trackList.setAdapter(adapter);

        trackList.setOnItemClickListener(activity.TrackClickListener);
        trackList.setOnItemLongClickListener(activity.TrackLongClickListener);

        return partView;
    }
}
