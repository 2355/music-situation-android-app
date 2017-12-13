package com.example.tlabuser.musicapplication.View.Player;


import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.MediaPlayerService;
import com.example.tlabuser.musicapplication.Model.Track;
import com.example.tlabuser.musicapplication.PlayerBroadcastReceiver;
import com.example.tlabuser.musicapplication.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayScreen extends FragmentActivity{

    private static Track track_item;

    PlayerBroadcastReceiver receiver;
    IntentFilter            intentFilter;

    ImageButton btBack;
    ImageButton btPlay;
    ImageButton btSkip;

    String nowState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_screen);

        receiver     = new PlayerBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("PLAYER_ACTION");
        registerReceiver(receiver, intentFilter);
        receiver.registerHandler(MPStateHandler);

        btBack = (ImageButton) findViewById(R.id.btBack);
        btPlay = (ImageButton) findViewById(R.id.btPlay);
        btSkip = (ImageButton) findViewById(R.id.btSkip);

        TextView  tvTitle     = (TextView) findViewById(R.id.title);
        TextView  tvArtist    = (TextView) findViewById(R.id.artist);
        TextView  tvAlbum     = (TextView) findViewById(R.id.album);
        ImageView ivAlbum_art = (ImageView)findViewById(R.id.albumart);

        track_item = Main.getFocusedTrack();
        tvTitle.setText( track_item.title);
        tvArtist.setText(track_item.artist);
        tvAlbum.setText( track_item.album);
        ivAlbum_art.setImageResource(R.drawable.icon_album);

        Intent fromIntent = getIntent();
        String from = fromIntent.getStringExtra("from");
        String state = fromIntent.getStringExtra("state");
        Log.d("PlayScreen", "onCreate from:" + from);
        Log.d("PlayScreen", "onCreate state:" + state);
        switch (from){
            case "fromTrackList":
                Intent intent = new Intent(PlayScreen.this, MediaPlayerService.class);
                nowState = "Playing";
                intent.putExtra("click", "Start");
                intent.putExtra("mediaFileUriStr", track_item.uri.toString());
                startService(intent);
                btPlay.setImageResource(R.drawable.icon_pause);
                break;

            case "fromPlayPanel":
                switch (state){
                    case "Stop":
                        nowState = "Stop";
                        btPlay.setImageResource(R.drawable.icon_play);
                        break;

                    case "Playing":
                        nowState = "Playing";
                        btPlay.setImageResource(R.drawable.icon_pause);
                        break;

                    case "Pause":
                        nowState = "Pause";
                        btPlay.setImageResource(R.drawable.icon_play);
                        break;
                }

                break;

            case "fromNotification":
                nowState = "Stop";

                break;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void onBadButtonClick(View view) {
        Toast.makeText(PlayScreen.this, "BadButtonClick", Toast.LENGTH_SHORT).show();
    }

    public void onBackButtonClick(View view) {
        Intent intent = new Intent(PlayScreen.this, MediaPlayerService.class);
        intent.putExtra("click", "Back");
        startService(intent);
    }

    public void onPlayButtonClick(View view) {
        Intent intent = new Intent(PlayScreen.this, MediaPlayerService.class);
        Log.d("PlayScreen", "onPlayButtonClick nowState:" + nowState);
        switch(nowState){
            case "Stop":
                intent.putExtra("click", "Start");
                intent.putExtra("mediaFileUriStr", track_item.uri.toString());
                startService(intent);
                break;

            case "Playing":
                intent.putExtra("click", "Pause");
                startService(intent);
                break;

            case "Pause":
                intent.putExtra("click", "ReStart");
                startService(intent);
                break;
        }
    }

    public void onSkipButtonClick(View view) {
        Intent intent = new Intent(PlayScreen.this, MediaPlayerService.class);
        intent.putExtra("click", "Skip");
        intent.putExtra("duration", track_item.duration);
        startService(intent);
    }

    public void onGoodButtonClick(View view) {
        Toast.makeText(PlayScreen.this, "GoodButtonClick", Toast.LENGTH_SHORT).show();
    }

    // サービスから値を受け取ったら動かしたい内容を書く
    private Handler MPStateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            String message = bundle.getString("message");

            Log.d("PlayScreen", "get massage from MPService: " + message);
            Toast.makeText(PlayScreen.this, "send message: " + message, Toast.LENGTH_SHORT).show();

            switch (message){
                case "Stop":
                    nowState = "Stop";
                    btPlay.setImageResource(R.drawable.icon_play);
                    break;

                case "Playing":
                    nowState = "Playing";
                    btPlay.setImageResource(R.drawable.icon_pause);
                    break;

                case "Pause":
                    nowState = "Pause";
                    btPlay.setImageResource(R.drawable.icon_play);
                    break;
            }
        }
    };
}
