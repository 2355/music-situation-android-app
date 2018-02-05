package com.example.tlabuser.musicapplication;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tlabuser.musicapplication.Model.Album;
import com.example.tlabuser.musicapplication.Model.Artist;
import com.example.tlabuser.musicapplication.Model.ExTrack;
import com.example.tlabuser.musicapplication.Model.Situation;
import com.example.tlabuser.musicapplication.View.Album.AlbumDetailFragment;
import com.example.tlabuser.musicapplication.View.Artist.ArtistDetailFragment;
import com.example.tlabuser.musicapplication.View.Player.PlayScreenFragment;
import com.example.tlabuser.musicapplication.View.Player.YoutubePlayScreenFragment;
import com.example.tlabuser.musicapplication.View.Root.RootMenuFragment;
import com.example.tlabuser.musicapplication.View.Situation.SituationDetailFragment;

import static com.example.tlabuser.musicapplication.MediaPlayerService.CLICK;
import static com.example.tlabuser.musicapplication.MediaPlayerService.State.pause;
import static com.example.tlabuser.musicapplication.MediaPlayerService.State.playing;
import static com.example.tlabuser.musicapplication.MediaPlayerService.State.stop;
import static com.example.tlabuser.musicapplication.MediaPlayerService.URI;
import static com.example.tlabuser.musicapplication.MediaPlayerService.setMainListener;


public class Main extends FragmentActivity{

    public final String TAG = "Main";

    private final int PERMISSION_INITIAL = 1;

    private enum FrgmType { fRoot, fSituation, fAlbum, fArtist }
    private FrgmType fTop;

    private Situation focusedSituation;
    public  void      focusSituation(Situation item) {if(item != null) focusedSituation = item;}
    public  Situation getFocusedSituation() {return focusedSituation;}

    private Album focusedAlbum;
    public  void  focusAlbum(Album item) {if(item != null) focusedAlbum = item;}
    public  Album getFocusedAlbum() {return focusedAlbum ;}

    private Artist focusedArtist;
    public  void   focusArtist(Artist item) {if(item != null) focusedArtist = item;}
    public  Artist getFocusedArtist() {return focusedArtist ;}

    private ExTrack focusedExTrack;
    public  void   focusExTrack(ExTrack item) {if(item != null) focusedExTrack = item;}
    public  ExTrack getFocusedExTrack() {return focusedExTrack ;}

    private ImageView ivAlbumArt;
    private TextView tvTitle, tvArtist;
    private ImageButton btPlay;

    private MediaPlayerService.State mpState = stop;

    public static MainLifecycleListener listener;

    private enum BackFrom { playScreen, youtubePlayScreen }
    private BackFrom from = BackFrom.playScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Android 6, API 23以上でパーミッションの確認
        if(Build.VERSION.SDK_INT >= 23){
            // パーミッションの確認，許可されていなければ許可を求める
            initialCheckPermissions();
        }else{
            showFragment();
        }

        if (listener != null) {
            mpState = listener.getState();

            switch (mpState) {
                case stop:    btPlay.setImageResource(R.drawable.icon_play); break;
                case playing: btPlay.setImageResource(R.drawable.icon_pause); break;
                case pause:   btPlay.setImageResource(R.drawable.icon_play); break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    private void initialCheckPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            showFragment();

        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_INITIAL
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_INITIAL
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            showFragment();
        }else{
            Toast.makeText(this, "許可がないと何もできません\nアプリを再起動してください", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Storage permission denied.");
        }
    }

    private void showFragment(){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.root, new RootMenuFragment(),"Root");
        ft.commit();

        setContentView(R.layout.main);

        tvTitle    = (TextView)  findViewById(R.id.title);
        tvArtist   = (TextView)  findViewById(R.id.artist);
        ivAlbumArt = (ImageView) findViewById(R.id.albumart);

        btPlay = (ImageButton) findViewById(R.id.btPlay);

        btPlay.setOnClickListener(this::onPlayButtonClick);

        updatePanel();

        setMainListener(new MediaPlayerService.PlayerStateListener() {
            @Override
            public void onStop() {
                Main.this.mpState = stop;
                btPlay.setImageResource(R.drawable.icon_play);
                Log.d(TAG, "onStop");
            }

            @Override
            public void onPlaying() {
                Main.this.mpState = playing;
                btPlay.setImageResource(R.drawable.icon_pause);
                updatePanel();
                Log.d(TAG, "onPlaying");
            }

            @Override
            public void onPause() {
                Main.this.mpState = pause;
                btPlay.setImageResource(R.drawable.icon_play);
                Log.d(TAG, "onPause");
            }
        });
    }

    public void setNewFragment(FrgmType CallFragment){

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fTop = CallFragment;
        switch(CallFragment)
        {
            case fRoot      : ft.replace(R.id.root, new RootMenuFragment(), "Root"); break;
            case fSituation : ft.replace(R.id.root, new SituationDetailFragment(), "Situation"); break;
            case fAlbum     : ft.replace(R.id.root, new AlbumDetailFragment(), "Album"); break;
            case fArtist    : ft.replace(R.id.root, new ArtistDetailFragment(), "Artist"); break;
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void popBackFragment(){
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();
        Log.d(TAG, "popBackStack");
    }

    private void updatePanel(){
        ExTrack exTrack = getFocusedExTrack();
        if (exTrack != null) {
            Log.d(TAG, "updatePanel");

            exTrack.addAlbumArt(this, exTrack.albumId);
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

    public AdapterView.OnItemClickListener  SituationClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListView lv = (ListView) parent;
            Main.this.focusSituation((Situation) lv.getItemAtPosition(position));
            Main.this.setNewFragment(FrgmType.fSituation);
        }
    };

    public AdapterView.OnItemLongClickListener SituationLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ListView lv = (ListView) parent;
            Situation situation = (Situation) lv.getItemAtPosition(position);
            Toast.makeText(Main.this, "LongClick:" + situation.name, Toast.LENGTH_LONG).show();
            return true;
        }
    };

    public AdapterView.OnItemClickListener  AlbumClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        focusAlbum( (Album)lv.getItemAtPosition(position) );
        setNewFragment(FrgmType.fAlbum);
    };

    public AdapterView.OnItemLongClickListener AlbumLongClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        Album item = (Album)lv.getItemAtPosition(position);
        Toast.makeText(Main.this, "LongClick:"+item.album, Toast.LENGTH_LONG).show();
        return true;
    };

    public AdapterView.OnItemClickListener  ArtistClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        focusArtist( (Artist)lv.getItemAtPosition(position) );
        setNewFragment(FrgmType.fArtist);
    };

    public AdapterView.OnItemLongClickListener ArtistLongClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        Artist item = (Artist)lv.getItemAtPosition(position);
        Toast.makeText(Main.this, "LongClick:"+item.artist, Toast.LENGTH_LONG).show();
        return true;
    };

    public AdapterView.OnItemClickListener  ExTrackClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        focusExTrack( (ExTrack) lv.getItemAtPosition(position) );

        // stop service before transition
        mpState = stop;
        Intent intent = new Intent(this, MediaPlayerService.class);
        stopService(intent);

        YoutubePlayScreenFragment fragment = YoutubePlayScreenFragment.newInstance(YoutubePlayScreenFragment.From.track);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_container, fragment, YoutubePlayScreenFragment.TAG)
                .addToBackStack(YoutubePlayScreenFragment.TAG)
                .commit();

        from = BackFrom.youtubePlayScreen;
        updatePanel();
        // TODO hide playButton
    };

    public AdapterView.OnItemLongClickListener ExTrackLongClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        ExTrack item = (ExTrack) lv.getItemAtPosition(position);
        Toast.makeText(Main.this, "LongClick:"+item.title, Toast.LENGTH_LONG).show();
        return true;
    };

    public AdapterView.OnItemClickListener  internalExTrackClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        focusExTrack( (ExTrack) lv.getItemAtPosition(position) );

        PlayScreenFragment fragment = PlayScreenFragment.newInstance(PlayScreenFragment.From.track, mpState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_container, fragment, PlayScreenFragment.TAG)
                .addToBackStack(PlayScreenFragment.TAG)
                .commit();

        from = BackFrom.playScreen;
    };

    public AdapterView.OnItemLongClickListener internalExTrackLongClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        ExTrack item = (ExTrack) lv.getItemAtPosition(position);
        Toast.makeText(Main.this, "LongClick:"+item.title, Toast.LENGTH_LONG).show();
        return true;
    };

    public void onPlayPanelClick(View view) {
        switch (from) {
            case playScreen:
                PlayScreenFragment psFragment = PlayScreenFragment.newInstance(PlayScreenFragment.From.panel, mpState);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fl_container, psFragment, PlayScreenFragment.TAG)
                        .addToBackStack(PlayScreenFragment.TAG)
                        .commit();

                from = BackFrom.playScreen;
                break;

            case youtubePlayScreen:
                YoutubePlayScreenFragment yFragment = YoutubePlayScreenFragment.newInstance(YoutubePlayScreenFragment.From.panel);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fl_container, yFragment, YoutubePlayScreenFragment.TAG)
                        .addToBackStack(YoutubePlayScreenFragment.TAG)
                        .commit();

                from = BackFrom.youtubePlayScreen;
                break;
        }

        updatePanel();
    }

    public void onPlayButtonClick(View view) {
        Toast.makeText(Main.this, "Click:PlayButton", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "onPlayButtonClick mpState: " + mpState);

        Intent intent = new Intent(this, MediaPlayerService.class);
        switch(mpState){
            case stop:
                intent.putExtra(CLICK, MediaPlayerService.Click.start.toString());
                intent.putExtra(URI, "");
                this.startService(intent);
                break;

            case playing:
                intent.putExtra(CLICK, MediaPlayerService.Click.pause.toString());
                this.startService(intent);
                break;

            case pause:
                intent.putExtra(CLICK, MediaPlayerService.Click.restart.toString());
                this.startService(intent);
                break;
        }
    }

    public interface MainLifecycleListener {
        MediaPlayerService.State getState();
    }

    public static void setMainLifecycleListener(MainLifecycleListener l) {
        listener = l;
    }
}
