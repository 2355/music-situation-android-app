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
import com.example.tlabuser.musicapplication.Model.Track;
import com.example.tlabuser.musicapplication.View.Album.AlbumMenu;
import com.example.tlabuser.musicapplication.View.Artist.ArtistMenu;
import com.example.tlabuser.musicapplication.View.Player.PlayScreen;
import com.example.tlabuser.musicapplication.View.Player.YoutubePlayScreen;
import com.example.tlabuser.musicapplication.View.Root.RootMenu;
import com.example.tlabuser.musicapplication.View.Situation.SituationMenu;


public class Main extends FragmentActivity{

    private enum FrgmType { fRoot, fSituation, fAlbum, fArtist}
    private FrgmType fTop;

    private Situation focusedSituaion;
    public  void      focusSituation(Situation item)   {if(item != null) focusedSituaion = item;}
    public  Situation getFocusedSituaion()             {return focusedSituaion ;}

    private Album focusedAlbum;
    public  void      focusAlbum(Album item)           {if(item != null) focusedAlbum = item;}
    public  Album     getFocusedAlbum()                {return focusedAlbum ;}

    private Artist focusedArtist;
    public  void      focusArtist(Artist item)         {if(item != null) focusedArtist = item;}
    public  Artist    getFocusedArtist()               {return focusedArtist ;}

    private static Track focusedTrack;
    public  void   focusTrack(Track item)                        {if(item != null) focusedTrack = item;}
    public  static Track              getFocusedTrack()          {return focusedTrack ;}

    private static ExTrack focusedExTrack;
    public  void   focusExTrack(ExTrack item)      {if(item != null) focusedExTrack = item;}
    public  static ExTrack     getFocusedExTrack() {return focusedExTrack ;}

    PlayerBroadcastReceiver receiver;
    IntentFilter            intentFilter;

    ImageView   ivAlbum_art;
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
        ft.replace(R.id.root, new RootMenu(),"Root");
        ft.commit();

        receiver     = new PlayerBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("PLAYER_ACTION");
        registerReceiver(receiver, intentFilter);
        receiver.registerHandler(MPStateHandler);

        setContentView(R.layout.main);

        tvTitle     = (TextView)    findViewById(R.id.title);
        tvArtist    = (TextView)    findViewById(R.id.artist);
        ivAlbum_art = (ImageView)   findViewById(R.id.albumart);
        btBack      = (ImageButton) findViewById(R.id.btBack);
        btPlay      = (ImageButton) findViewById(R.id.btPlay);
        btSkip      = (ImageButton) findViewById(R.id.btSkip);

    }

    public void setNewFragment(FrgmType CallFragment){

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fTop = CallFragment;
        switch(CallFragment)
        {
            case fRoot      : ft.replace(R.id.root, new RootMenu(),      "Root");      break;
            case fSituation : ft.replace(R.id.root, new SituationMenu(), "Situation"); break;
            case fAlbum     : ft.replace(R.id.root, new AlbumMenu(),     "Album");     break;
            case fArtist    : ft.replace(R.id.root, new ArtistMenu(),    "Artist");    break;
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
        Track track_item = getFocusedTrack();
        Log.d("Main", "updatePanel:" + track_item.title);

        tvTitle.setText( track_item.title);
        tvArtist.setText(track_item.artist);
        ivAlbum_art.setImageResource(R.drawable.icon_album);
    }

    public AdapterView.OnItemClickListener  SituationClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            ListView lv = (ListView)parent;
            focusSituation( (Situation) lv.getItemAtPosition(position) );
            setNewFragment(FrgmType.fSituation);
        }
    };

    public  AdapterView.OnItemLongClickListener SituationLongClickListener = new AdapterView.OnItemLongClickListener(){

        @Override
        public boolean onItemLongClick(AdapterView parent, View view, int position, long id){
            ListView lv = (ListView)parent;
            Situation situation = (Situation) lv.getItemAtPosition(position);
            Toast.makeText(Main.this, "LongClick:"+situation.name, Toast.LENGTH_LONG).show();
            return true;
        }
    };

    public AdapterView.OnItemClickListener  AlbumClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            ListView lv = (ListView)parent;
            focusAlbum( (Album)lv.getItemAtPosition(position) );
            setNewFragment(FrgmType.fAlbum);
        }
    };

    public  AdapterView.OnItemLongClickListener AlbumLongClickListener = new AdapterView.OnItemLongClickListener(){

        @Override
        public boolean onItemLongClick(AdapterView parent, View view, int position, long id){
            ListView lv = (ListView)parent;
            Album item = (Album)lv.getItemAtPosition(position);
            Toast.makeText(Main.this, "LongClick:"+item.album, Toast.LENGTH_LONG).show();
            return true;
        }
    };

    public AdapterView.OnItemClickListener  ArtistClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            ListView lv = (ListView)parent;
            focusArtist( (Artist)lv.getItemAtPosition(position) );
            setNewFragment(FrgmType.fArtist);
        }
    };

    public  AdapterView.OnItemLongClickListener ArtistLongClickListener = new AdapterView.OnItemLongClickListener(){

        @Override
        public boolean onItemLongClick(AdapterView parent, View view, int position, long id){
            ListView lv = (ListView)parent;
            Artist item = (Artist)lv.getItemAtPosition(position);
            Toast.makeText(Main.this, "LongClick:"+item.artist, Toast.LENGTH_LONG).show();
            return true;
        }
    };

    public AdapterView.OnItemClickListener  TrackClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            ListView lv = (ListView)parent;
            focusTrack( (Track) lv.getItemAtPosition(position) );

            Intent intent = new Intent(getApplication(), PlayScreen.class);
            intent.putExtra("from", "fromTrackList");
            intent.putExtra("state", "Stop");
            startActivity(intent);
        }
    };

    public  AdapterView.OnItemLongClickListener TrackLongClickListener = new AdapterView.OnItemLongClickListener(){

        @Override
        public boolean onItemLongClick(AdapterView parent, View view, int position, long id){
            ListView lv = (ListView)parent;
            Track item = (Track) lv.getItemAtPosition(position);
            Toast.makeText(Main.this, "LongClick:"+item.title, Toast.LENGTH_LONG).show();
            return true;
        }
    };

    public AdapterView.OnItemClickListener  ExTrackClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            ListView lv = (ListView)parent;
            focusExTrack( (ExTrack) lv.getItemAtPosition(position) );

            Intent intent = new Intent(getApplication(), YoutubePlayScreen.class);
            startActivity(intent);
        }
    };

    public  AdapterView.OnItemLongClickListener ExTrackLongClickListener = new AdapterView.OnItemLongClickListener(){

        @Override
        public boolean onItemLongClick(AdapterView parent, View view, int position, long id){
            ListView lv = (ListView)parent;
            ExTrack item = (ExTrack) lv.getItemAtPosition(position);
            Toast.makeText(Main.this, "LongClick:"+item.title, Toast.LENGTH_LONG).show();
            return true;
        }
    };

    public void onPlayPanelClick(View view) {
        Intent intent = new Intent(getApplication(), PlayScreen.class);
        intent.putExtra("from", "fromPlayPanel");
        intent.putExtra("state", nowState);
        startActivity(intent);

    }

    public void onBadButtonClick(View view) {
        Toast.makeText(Main.this, "Click:BadButton", Toast.LENGTH_SHORT).show();
    }

    public void onPlayButtonClick(View view) {
        Toast.makeText(Main.this, "Click:PlayButton", Toast.LENGTH_SHORT).show();
    }

    public void onGoodButtonClick(View view) {
        Toast.makeText(Main.this, "Click:GoodButton", Toast.LENGTH_SHORT).show();
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
