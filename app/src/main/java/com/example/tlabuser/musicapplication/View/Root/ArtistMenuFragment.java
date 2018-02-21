package com.example.tlabuser.musicapplication.View.Root;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.Model.Artist;
import com.example.tlabuser.musicapplication.R;

import java.util.List;

public class ArtistMenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Main activity = (Main)getActivity();
        List<Artist> artists = Artist.getItems(activity);

        View v = inflater.inflate(R.layout.fragment_artists_menu,container,false);
        ListView lvArtist = (ListView) v.findViewById(R.id.artist_list);
        ListArtistAdapter adapter = new ListArtistAdapter(activity, artists);
        lvArtist.setAdapter(adapter);

        lvArtist.setOnItemClickListener(activity.ArtistClickListener);
        lvArtist.setOnItemLongClickListener(activity.ArtistLongClickListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lvArtist.setNestedScrollingEnabled(true);
        }

        return v;

    }

}
