package com.example.tlabuser.musicapplication.View.Root;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tlabuser.musicapplication.ImageGetTask;
import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.MediaPlayerService;
import com.example.tlabuser.musicapplication.Model.ExTrack;
import com.example.tlabuser.musicapplication.R;
import com.example.tlabuser.musicapplication.View.Album.AlbumDetailFragment;
import com.example.tlabuser.musicapplication.View.Artist.ArtistDetailFragment;
import com.example.tlabuser.musicapplication.View.Player.PlayScreenFragment;
import com.example.tlabuser.musicapplication.View.Player.YoutubePlayScreenFragment;
import com.example.tlabuser.musicapplication.View.Situation.SituationDetailFragment;

import static com.example.tlabuser.musicapplication.MediaPlayerService.setRootPSListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class RootFragment extends Fragment {

    public static final String TAG = "RootFragment";

    private Main mainActivity;

    public enum Scene { root, situation, album, artist }
    private Scene top = Scene.root;

    public enum BackFrom { playScreen, youtubePlayScreen }
    private BackFrom from = BackFrom.playScreen;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private FragmentPagerAdapter adapter;
    private ViewPager viewPager;

    private ImageView ivAlbumArt;
    private TextView tvTitle, tvArtist;
    private ImageButton btPlay;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (Main) getActivity();

        mainActivity.setChangeFragmentListener(this::setNewFragment);
        mainActivity.setFromListener(this::setFrom);
        setRootPSListener(new PSListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_root, container, false);

        drawerLayout = (DrawerLayout) view.findViewById(R.id.drawer);
        navigationView = (NavigationView) view.findViewById(R.id.drawer_navigation);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        tabLayout = (TabLayout) view.findViewById(R.id.tab);
        viewPager = (ViewPager) view.findViewById(R.id.pager);
        ivAlbumArt = (ImageView) view.findViewById(R.id.iv_album_art);
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvArtist = (TextView) view.findViewById(R.id.tv_artist);
        btPlay = (ImageButton) view.findViewById(R.id.bt_play);

        adapter = new SectionsPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout.setupWithViewPager(viewPager);

        switch (mainActivity.mpState) {
            case stop:    btPlay.setImageResource(R.drawable.icon_play); break;
            case playing: btPlay.setImageResource(R.drawable.icon_pause); break;
            case pause:   btPlay.setImageResource(R.drawable.icon_play); break;
        }

        ivAlbumArt.setOnClickListener(this::onPlayPanelClick);
        tvTitle.setOnClickListener(this::onPlayPanelClick);
        tvArtist.setOnClickListener(this::onPlayPanelClick);
        btPlay.setOnClickListener(mainActivity::onPlayButtonClick);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                mainActivity, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        navigationView.setNavigationItemSelectedListener(select);

        mainActivity.setSupportActionBar(toolbar);


        updatePanel();

        return view;
    }

    private NavigationView.OnNavigationItemSelectedListener select = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            int count = getFragmentManager().getBackStackEntryCount();
            for (int i=0; i<count; i++){
                getFragmentManager().popBackStack();
            }

            switch (item.getItemId()) {
                case R.id.menu_situation: viewPager.setCurrentItem(0); break;
                case R.id.menu_track: viewPager.setCurrentItem(1); break;
                case R.id.menu_album: viewPager.setCurrentItem(2); break;
                case R.id.menu_artist: viewPager.setCurrentItem(3); break;

            }
            drawerLayout.closeDrawers();
            return false;
        }
    };

    private void setNewFragment(Scene scene){
        // TODO 戻ったときにtop=rootにする
        top = scene;

        // TODO playScreenへの遷移もここに入れる
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch (scene) {
            case root:
                ft.replace(R.id.fl_container, new RootFragment(), RootFragment.TAG);
                ft.addToBackStack(RootFragment.TAG);
                break;
            case situation:
                ft.replace(R.id.fl_container, new SituationDetailFragment(), SituationDetailFragment.TAG);
                ft.addToBackStack(SituationDetailFragment.TAG);
                break;
            case album:
                ft.replace(R.id.fl_container, new AlbumDetailFragment(), AlbumDetailFragment.TAG);
                ft.addToBackStack(AlbumDetailFragment.TAG);
                break;
            case artist:
                ft.replace(R.id.fl_container, new ArtistDetailFragment(), ArtistDetailFragment.TAG);
                ft.addToBackStack(ArtistDetailFragment.TAG);
                break;
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    private void setFrom(BackFrom backFrom) {
        switch (backFrom) {
            case playScreen:
                from = BackFrom.playScreen;
                break;
            case youtubePlayScreen:
                from = BackFrom.youtubePlayScreen;
                updatePanel();
                break;
        }
    }

    private void updatePanel(){
        ExTrack exTrack = mainActivity.getFocusedExTrack();
        if (exTrack != null) {
            Log.d(TAG, "updatePanel");

            exTrack.addAlbumArt(mainActivity, exTrack.albumId);
            tvTitle.setText(exTrack.title);
            tvArtist.setText(exTrack.artist);

            ivAlbumArt.setImageResource(R.drawable.icon_album);
            String path = exTrack.albumArt;
            if (path != null && path != "") {
                ivAlbumArt.setTag(path);
                ImageGetTask task = new ImageGetTask(ivAlbumArt);
                task.execute(path);
            }
            Log.d(TAG, "updatePanel: setAlbumArt");

        } else {
            Log.d(TAG, "updatePanel error: exTrack == null");
        }

        if (from == BackFrom.youtubePlayScreen) {
            btPlay.setVisibility(View.GONE);
        } else {
            btPlay.setVisibility(View.VISIBLE);
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {super(fm);}

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0: return new SituationMenuFragment();
                case 1: return new ExTrackMenuFragment();
                case 2: return new AlbumMenuFragment();
                case 3: return new ArtistMenuFragment();
            }
            return null;
        }

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

        @Override
        public int getCount() {
            return 4;
        }
    }

    private void onPlayPanelClick(View view) {
        switch (from) {
            case playScreen:
                PlayScreenFragment psFragment = PlayScreenFragment.newInstance(PlayScreenFragment.From.panel, mainActivity.mpState);
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fl_root, psFragment, PlayScreenFragment.TAG)
                        .addToBackStack(PlayScreenFragment.TAG)
                        .commit();

                from = BackFrom.playScreen;
                break;

            case youtubePlayScreen:
                YoutubePlayScreenFragment yFragment = YoutubePlayScreenFragment.newInstance(YoutubePlayScreenFragment.From.panel);
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fl_root, yFragment, YoutubePlayScreenFragment.TAG)
                        .addToBackStack(YoutubePlayScreenFragment.TAG)
                        .commit();

                from = BackFrom.youtubePlayScreen;
                break;
        }
    }

    private class PSListener implements MediaPlayerService.PlayerStateListener {
        @Override
        public void onStop() {
            btPlay.setImageResource(R.drawable.icon_play);
        }

        @Override
        public void onPlaying() {
            btPlay.setImageResource(R.drawable.icon_pause);
            updatePanel();
        }

        @Override
        public void onPause() {
            btPlay.setImageResource(R.drawable.icon_play);
        }
    }
}
