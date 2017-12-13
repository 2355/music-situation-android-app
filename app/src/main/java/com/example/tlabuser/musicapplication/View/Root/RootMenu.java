package com.example.tlabuser.musicapplication.View.Root;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tlabuser.musicapplication.JsonLoader;
import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.Model.Album;
import com.example.tlabuser.musicapplication.Model.Artist;
import com.example.tlabuser.musicapplication.Model.Situation;
import com.example.tlabuser.musicapplication.Model.Track;
import com.example.tlabuser.musicapplication.R;
import com.example.tlabuser.musicapplication.SQLOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class RootMenu extends Fragment{

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager            mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.root_menu, container, false);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //PagerTabカスタム
        PagerTabStrip strip = (PagerTabStrip) rootView.findViewById(R.id.pager_title_strip);
        strip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        strip.setTextSpacing(50);
        strip.setNonPrimaryAlpha(0.3f);

        return rootView;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {super(fm);}

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch(position){
                case 0: fragment = new SituationSectionFragment(); break;
                case 1: fragment = new TrackSectionFragment();     break;
                case 2: fragment = new AlbumSectionFragment();     break;
                case 3: fragment = new ArtistSectionFragment();    break;
            }
            return fragment;
        }

        @Override
        public int getCount() { return 4; }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return "Situation";
                case 1: return "Track";
                case 2: return "Album";
                case 3: return "Artist";
            }
            return null;
        }

    }

    public static class SituationSectionFragment extends Fragment implements LoaderManager.LoaderCallbacks<JSONObject>{

        JSONArray jsonArray = new JSONArray();
        ListView  situationList;

        private Main activity;

        private SQLOpenHelper sqlOpenHelper;
        private SQLiteDatabase db;

        private List<Situation> situations;
        private ListSituationAdapter listSituationAdapter;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);

            activity = (Main)getActivity();

            sqlOpenHelper = new SQLOpenHelper(activity);
            db = sqlOpenHelper.getReadableDatabase();

            situations = Situation.getSituationsFromSQL(db);

            if(situations.isEmpty()){
                // JSONの取得
                Log.d("RootMenu", "situations.isEmpty()");
                getLoaderManager().restartLoader(1, null, this);
                Toast.makeText(activity, "SituationListを取得しています。\nしばらくお待ちください。", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            View v = inflater.inflate(R.layout.menu_situations,container,false);
            situationList = (ListView) v.findViewById(R.id.situation_list);


            listSituationAdapter = new ListSituationAdapter(activity, situations);
            situationList.setAdapter(listSituationAdapter);
            situationList.setOnItemClickListener(activity.SituationClickListener);
            situationList.setOnItemLongClickListener(activity.SituationLongClickListener);

            return v;

        }

        @Override
        public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
            String head = "https://musicmetadata.herokuapp.com/ds/query?query=";
            String tail = "&output=json&stylesheet=/xml-to-html.xsl";
            String urlStr =
                            "prefix situation: <http://music.metadata.database.situation/> \n" +
                            "\n" +
                            "SELECT distinct ?tag\n" +
                            "WHERE {\n" +
                            "  ?b situation:tag ?tag;\n" +
                            "      situation:weight ?weight.\n" +
                            "}\n" +
                            "order by (?tag)";
            try {
                urlStr = URLEncoder.encode(urlStr, "UTF-8");
            }catch (UnsupportedEncodingException e){
                Log.d("RootMenu","URLエンコードに失敗しました。 UnsupportedEncodingException=" + e);
            }
            urlStr = head + urlStr + tail;

            JsonLoader jsonLoader = new JsonLoader(getActivity(), urlStr);
            jsonLoader.forceLoad();
            return  jsonLoader;
        }

        @Override
        public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
            Main activity = (Main)getActivity();
            if (data != null) {

                try {
                    jsonArray = data.getJSONObject("results").getJSONArray("bindings");
                    if (jsonArray.getJSONObject(0).has("tag")) {
                        situations = Situation.getItems(jsonArray);
                        Situation.insertRows(db, situations);

                        ListSituationAdapter adapter = new ListSituationAdapter(activity, situations);
                        situationList.setAdapter(adapter);
                    }

                } catch (JSONException e) {
                    Log.d("RootMenu","JSONのパースに失敗しました。 JSONException=" + e);
                }
            }else{
                Log.d("RootMenu", "onLoadFinished error!");
            }
        }

        @Override
        public void onLoaderReset(Loader<JSONObject> loader) {
            // 処理なし
        }
    }

    public static class TrackSectionFragment extends Fragment {

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

    public static class AlbumSectionFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            Main activity = (Main)getActivity();
            List albums = Album.getItems(activity);

            View v = inflater.inflate(R.layout.menu_albums,container,false);
            ListView albumList = (ListView) v.findViewById(R.id.album_list);
            ListAlbumAdapter adapter = new ListAlbumAdapter(activity, albums);
            albumList.setAdapter(adapter);

            albumList.setOnItemClickListener(activity.AlbumClickListener);
            albumList.setOnItemLongClickListener(activity.AlbumLongClickListener);

            return v;

        }

    }

    public static class ArtistSectionFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            Main activity = (Main)getActivity();
            List artists = Artist.getItems(activity);

            View v = inflater.inflate(R.layout.menu_artists,container,false);
            ListView artistList = (ListView) v.findViewById(R.id.artist_list);
            ListArtistAdapter adapter = new ListArtistAdapter(activity, artists);
            artistList.setAdapter(adapter);

            artistList.setOnItemClickListener(activity.ArtistClickListener);
            artistList.setOnItemLongClickListener(activity.ArtistLongClickListener);

            return v;

        }

    }


}
