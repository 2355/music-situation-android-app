package com.example.tlabuser.musicapplication.View.Root;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.Model.Album;
import com.example.tlabuser.musicapplication.R;

import java.util.List;

public class AlbumMenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Main activity = (Main)getActivity();
        List<Album> albums = Album.getItems(activity);

        View view = inflater.inflate(R.layout.fragment_albums_menu,container,false);
        ListView lvAlbum = (ListView) view.findViewById(R.id.album_list);
        ListAlbumAdapter adapter = new ListAlbumAdapter(activity, albums);
        lvAlbum.setAdapter(adapter);

        lvAlbum.setOnItemClickListener(activity.AlbumClickListener);
        lvAlbum.setOnItemLongClickListener(activity.AlbumLongClickListener);
        // TODO replace listView with recyclerView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lvAlbum.setNestedScrollingEnabled(true);
        }

        return view;

    }

}
