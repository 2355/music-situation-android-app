package com.example.tlabuser.musicapplication.View.Root;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.Model.Track;
import com.example.tlabuser.musicapplication.R;

import java.util.List;

public class TrackMenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Main activity = (Main)getActivity();
        List tracks = Track.getItems(activity);

        View v = inflater.inflate(R.layout.menu_tracks,container,false);
        ListView trackList = (ListView) v.findViewById(R.id.track_list);
        ListTrackAdapter adapter = new ListTrackAdapter(activity, tracks);
        trackList.setAdapter(adapter);

        trackList.setOnItemClickListener(activity.TrackClickListener);
        trackList.setOnItemLongClickListener(activity.TrackLongClickListener);

        return v;

    }

}
