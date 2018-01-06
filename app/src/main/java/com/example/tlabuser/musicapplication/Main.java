package com.example.tlabuser.musicapplication;


import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.example.tlabuser.musicapplication.View.Player.PlayScreen;
import com.example.tlabuser.musicapplication.View.Player.YoutubePlayScreen;
import com.example.tlabuser.musicapplication.View.Root.RootMenuFragment;
import com.example.tlabuser.musicapplication.View.Situation.SituationDetailFragment;

import java.util.Calendar;
import java.util.List;

import static com.example.tlabuser.musicapplication.CalendarUtil.calToSituations;
import static com.example.tlabuser.musicapplication.CalendarUtil.calToStr;


public class Main extends FragmentActivity{

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

    private static ExTrack focusedExTrack;
    public  void   focusExTrack(ExTrack item) {if(item != null) focusedExTrack = item;}
    public  static ExTrack getFocusedExTrack() {return focusedExTrack ;}

    PlayerBroadcastReceiver receiver;
    IntentFilter            intentFilter;

    ImageView   ivAlbumArt;
    TextView    tvTitle;
    TextView    tvArtist;
    ImageButton btBack;
    ImageButton btPlay;
    ImageButton btSkip;

    String nowState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Android 6, API 23以上でパーミッションの確認
        if(Build.VERSION.SDK_INT >= 23){
            // パーミッションの確認，許可されていなければ許可を求める
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions, 1000);
                return;
            }
            showFragment();
        }else{
            showFragment();
        }
    }

    @Override
    protected void onStop() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 許可が下りていれば実行
        if(requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showFragment();
        }else{
            Toast.makeText(this, "許可がないと何もできません\nアプリを再起動してください", Toast.LENGTH_LONG).show();
        }
    }

    private void showFragment(){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.root, new RootMenuFragment(),"Root");
        ft.commit();

        receiver     = new PlayerBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("PLAYER_ACTION");
        registerReceiver(receiver, intentFilter);
        receiver.registerHandler(MPStateHandler);

        setContentView(R.layout.main);

        tvTitle    = (TextView)    findViewById(R.id.title);
        tvArtist   = (TextView)    findViewById(R.id.artist);
        ivAlbumArt = (ImageView)   findViewById(R.id.albumart);
        btBack     = (ImageButton) findViewById(R.id.btBack);
        btPlay     = (ImageButton) findViewById(R.id.btPlay);
        btSkip     = (ImageButton) findViewById(R.id.btSkip);

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
        Log.d("Main", "popBackStack");
    }

    public void updatePanel(){
        ExTrack exTrack = getFocusedExTrack();
        Log.d("Main", "updatePanel:" + exTrack.title);

        tvTitle.setText(exTrack.title);
        tvArtist.setText(exTrack.artist);
        ivAlbumArt.setImageResource(R.drawable.icon_album);
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

        Intent intent = new Intent(getApplication(), YoutubePlayScreen.class);
        startActivity(intent);
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

        Intent intent = new Intent(getApplication(), PlayScreen.class);
        intent.putExtra("from", "fromTrackList");
        intent.putExtra("state", "Stop");
        startActivity(intent);
    };

    public AdapterView.OnItemLongClickListener internalExTrackLongClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        ExTrack item = (ExTrack) lv.getItemAtPosition(position);
        Toast.makeText(Main.this, "LongClick:"+item.title, Toast.LENGTH_LONG).show();
        return true;
    };

    public void onPlayPanelClick(View view) {
        Intent intent = new Intent(getApplication(), PlayScreen.class);
        intent.putExtra("from", "fromPlayPanel");
        intent.putExtra("state", nowState);
        startActivity(intent);

    }

    public void onBadButtonClick(View view) {
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        List<String> situations = calToSituations(cal);

        Toast.makeText(Main.this, situations.toString(), Toast.LENGTH_SHORT).show();
    }

    public void onPlayButtonClick(View view) {
        Toast.makeText(Main.this, "Click:PlayButton", Toast.LENGTH_SHORT).show();
    }

    public void onGoodButtonClick(View view) {
        Calendar cal = Calendar.getInstance();
        cal.getTime();

        Toast.makeText(Main.this, calToStr(cal), Toast.LENGTH_SHORT).show();
    }

    // サービスから値を受け取ったら動かしたい内容を書く
    private Handler MPStateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            String message = bundle.getString("message");

            Log.d("Main", "get massage from MPService: " + message);
            Toast.makeText(Main.this, "send message: " + message, Toast.LENGTH_SHORT).show();

            switch (message){
                case "Stop":
                    nowState = "Stop";
                    btPlay.setImageResource(R.drawable.icon_play);
                    break;

                case "Playing":
                    nowState = "Playing";
                    btPlay.setImageResource(R.drawable.icon_pause);
                    updatePanel();
                    break;

                case "Pause":
                    nowState = "Pause";
                    btPlay.setImageResource(R.drawable.icon_play);
                    break;
            }
        }
    };

}
