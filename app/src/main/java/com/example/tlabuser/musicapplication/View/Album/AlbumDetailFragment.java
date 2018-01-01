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
import com.example.tlabuser.musicapplication.Model.ExTrack;
import com.example.tlabuser.musicapplication.Model.Track;
import com.example.tlabuser.musicapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumDetailFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View partView =inflater.inflate(R.layout.part_album, container, false);

        Main activity = (Main)getActivity();
        Album item = activity.getFocusedAlbum();

        TextView  tvAlbum    = (TextView)  partView.findViewById(R.id.album);
        TextView  tvArtist   = (TextView)  partView.findViewById(R.id.artist);
        TextView  tvTracks   = (TextView)  partView.findViewById(R.id.tracks);
        ImageView ivAlbumArt = (ImageView) partView.findViewById(R.id.albumart);

        tvAlbum.setText(item.album);
        tvArtist.setText(item.artist);
        tvTracks.setText(String.valueOf(item.tracks)+"tracks");

        String path = item.albumArt;
        ivAlbumArt.setImageResource(R.drawable.dummy_album_art);
        if(path!=null){
            ivAlbumArt.setTag(path);
            ImageGetTask task = new ImageGetTask(ivAlbumArt);
            task.execute(path);
        }

        List<Track> tracks = Track.getItemsByAlbum(getActivity(), item.albumId);
        List<ExTrack> exTracks = new ArrayList<>();

        for (int i = 0; i < tracks.size(); i++){
            ExTrack exTrack = new ExTrack();
            exTrack = ExTrack.trackToExTrack(exTrack, tracks.get(i));
            exTrack.internal = 1;
            exTracks.add(exTrack);
        }

        ListView exTrackList = (ListView) partView.findViewById(R.id.track_list);
        ListExTrackAlbumAdapter adapter = new ListExTrackAlbumAdapter(activity, exTracks);
        exTrackList.setAdapter(adapter);

        exTrackList.setOnItemClickListener(activity.internalExTrackClickListener);
        exTrackList.setOnItemLongClickListener(activity.internalExTrackLongClickListener);

        return partView;
    }
}
