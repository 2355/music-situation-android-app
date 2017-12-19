package com.example.tlabuser.musicapplication.View.Artist;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tlabuser.musicapplication.View.Root.ListAlbumAdapter;
import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.Model.Album;
import com.example.tlabuser.musicapplication.Model.Artist;
import com.example.tlabuser.musicapplication.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistDetailFragment extends Fragment{

    private static Artist artist_item;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View partView =inflater.inflate(R.layout.part_artist, container, false);

        Main activity = (Main)getActivity();
        artist_item = activity.getFocusedArtist();

        TextView artist_name   = (TextView) partView.findViewById(R.id.artist);
        TextView artist_albums = (TextView) partView.findViewById(R.id.albums);
        TextView album_tracks  = (TextView) partView.findViewById(R.id.tracks);

        artist_name.setText(artist_item.artist);
        artist_albums.setText(String.valueOf(artist_item.albums)+"albums");
        album_tracks.setText( String.valueOf(artist_item.tracks)+"tracks");

        /*
        partView.findViewById(R.id.artist_info).setOnClickListener(new View.OnClickListener()
        {@Override public void onClick(View v) {}});
        partView.findViewById(R.id.albumtitle).setOnClickListener(new View.OnClickListener()
        {@Override public void onClick(View v) {}});
        */

        List albums  = Album.getItemsByArtist(getActivity(), artist_item.artist);

        ListView albumList = (ListView) partView.findViewById(R.id.album_list);
        ListAlbumAdapter adapter = new ListAlbumAdapter(activity, albums);
        albumList.setAdapter(adapter);

        albumList.setOnItemClickListener(activity.AlbumClickListener);
        albumList.setOnItemLongClickListener(activity.AlbumLongClickListener);


        return partView;
    }
}
