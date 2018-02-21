package com.example.tlabuser.musicapplication.View.Artist;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.Model.Album;
import com.example.tlabuser.musicapplication.Model.Artist;
import com.example.tlabuser.musicapplication.R;
import com.example.tlabuser.musicapplication.View.Root.ListAlbumAdapter;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistDetailFragment extends Fragment{

    public static final String TAG = "ArtistDetailFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_artist_detail, container, false);

        Main activity = (Main)getActivity();
        Artist artist = activity.getFocusedArtist();

        TextView tvArtist = (TextView) view.findViewById(R.id.artist);
        TextView tvAlbums = (TextView) view.findViewById(R.id.albums);
        TextView tvTracks = (TextView) view.findViewById(R.id.tracks);

        tvArtist.setText(artist.artist);
        tvAlbums.setText(String.valueOf(artist.albums)+" albums");
        tvTracks.setText(String.valueOf(artist.tracks)+" tracks");

        List<Album> albums  = Album.getItemsByArtist(getActivity(), artist.artist);

        ListView lvAlbums = (ListView) view.findViewById(R.id.album_list);
        ListAlbumAdapter adapter = new ListAlbumAdapter(activity, albums);
        lvAlbums.setAdapter(adapter);

        lvAlbums.setOnItemClickListener(activity.AlbumClickListener);
        lvAlbums.setOnItemLongClickListener(activity.AlbumLongClickListener);

        return view;
    }
}
