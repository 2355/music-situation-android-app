package com.example.tlabuser.musicapplication.View.Root;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.Model.ExTrack;
import com.example.tlabuser.musicapplication.Model.Track;
import com.example.tlabuser.musicapplication.R;

import java.util.ArrayList;
import java.util.List;

public class ExTrackMenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Main activity = (Main)getActivity();
        List<Track> tracks = Track.getItems(activity);
        List<ExTrack> exTracks = new ArrayList<>();

        for (int i = 0; i < tracks.size(); i++){
            ExTrack exTrack = new ExTrack();
            exTrack.addTrackDataToExTrack(tracks.get(i));
            exTracks.add(exTrack);
        }

        View v = inflater.inflate(R.layout.fragment_tracks_menu,container,false);
        ListView exTrackList = (ListView) v.findViewById(R.id.track_list);
        ListExTrackAllAdapter adapter = new ListExTrackAllAdapter(activity, exTracks);
        exTrackList.setAdapter(adapter);

        exTrackList.setOnItemClickListener(activity.internalExTrackClickListener);
        exTrackList.setOnItemLongClickListener(activity.internalExTrackLongClickListener);

        return v;
    }

}
