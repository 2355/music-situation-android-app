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

public class AlbumDetailFragment extends Fragment{

    public static final String TAG = "AlbumDetailFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_album_detail, container, false);

        Main activity = (Main)getActivity();
        Album album = activity.getFocusedAlbum();

        TextView  tvAlbum    = (TextView)  view.findViewById(R.id.album);
        TextView  tvArtist   = (TextView)  view.findViewById(R.id.artist);
        TextView  tvTracks   = (TextView)  view.findViewById(R.id.tracks);
        ImageView ivAlbumArt = (ImageView) view.findViewById(R.id.albumart);

        tvAlbum.setText(album.album);
        tvArtist.setText(album.artist);
        tvTracks.setText(String.valueOf(album.tracks)+" tracks");

        String path = album.albumArt;
        ivAlbumArt.setImageResource(R.drawable.icon_album);
        if (path != null) {
            ivAlbumArt.setTag(path);
            ImageGetTask task = new ImageGetTask(ivAlbumArt);
            task.execute(path);
        }

        List<Track> tracks = Track.getItemsByAlbum(getActivity(), album.albumId);
        List<ExTrack> exTracks = new ArrayList<>();

        for (int i = 0; i < tracks.size(); i++) {
            ExTrack exTrack = new ExTrack();
            exTrack.addTrackDataToExTrack(tracks.get(i));
            exTracks.add(exTrack);
        }

        ListView ivExTracks = (ListView) view.findViewById(R.id.track_list);
        ListExTrackAlbumAdapter adapter = new ListExTrackAlbumAdapter(activity, exTracks);
        ivExTracks.setAdapter(adapter);

        ivExTracks.setOnItemClickListener(activity.internalExTrackClickListener);
        ivExTracks.setOnItemLongClickListener(activity.internalExTrackLongClickListener);

        return view;
    }
}
