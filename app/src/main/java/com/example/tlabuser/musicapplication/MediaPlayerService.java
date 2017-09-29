package com.example.tlabuser.musicapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;

public class MediaPlayerService extends Service {

    private MediaPlayer _player;

    String nowState;

    @Override
    public void onCreate() {
        super.onCreate();
        _player = new MediaPlayer();
        nowState = "Stop";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String click = intent.getStringExtra("click");
        Log.d("MPService", "onStartCommand:" + click);

        switch(click){
            case "Start":
                try {
                    // Stop player when playing other song
                    if(nowState.equals("Playing") || nowState.equals("Pause")) {
                        _player.stop();
                        _player.reset();
                    }
                    String mediaFileUriStr = intent.getStringExtra("mediaFileUriStr");
                    Uri mediaFileUri = Uri.parse(mediaFileUriStr);
                    _player.setDataSource(MediaPlayerService.this, mediaFileUri);
                    _player.setOnPreparedListener(new PlayerPreparedListener());
                    _player.setOnCompletionListener(new PlayerCompletionListener());
                    _player.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "Pause":
                _player.pause();
                nowState = "Pause";
                sendBroadCast(nowState);
                break;

            case "ReStart":
                _player.start();
                nowState = "Playing";
                sendBroadCast(nowState);
                break;

            case "Back":
                _player.seekTo(0);
                break;

            case "Skip":
                long duration = intent.getLongExtra("duration", 0);
                _player.seekTo((int) duration);
                break;
        }

        //return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("MPService", "サービス破棄");
        if(_player.isPlaying()) {
            _player.stop();
        }
        _player.release();
        _player = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void sendBroadCast(String message) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra("message", message);
        broadcastIntent.setAction("PLAYER_ACTION");
        getBaseContext().sendBroadcast(broadcastIntent);
    }

    /**
     * メディア再生準備が完了時のリスナクラス。
     */
    private class PlayerPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.i("MPService", "再生開始");

            mp.start();
            nowState = "Playing";
            sendBroadCast(nowState);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(MediaPlayerService.this);
            builder.setSmallIcon(android.R.drawable.ic_media_play);
            builder.setContentTitle("再生開始");
            builder.setContentText("音声ファイルの再生を開始しました");

            Intent intent = new Intent(MediaPlayerService.this, PlayScreen.class);
            intent.putExtra("from", "fromNotification");
            intent.putExtra("playing", mp.isPlaying());
            PendingIntent stopServiceIntent = PendingIntent.getActivity(MediaPlayerService.this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(stopServiceIntent);
            builder.setAutoCancel(true);

            Notification notification = builder.build();
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(1, notification);
        }
    }

    /**
     * メディア再生が終了したときのリスナクラス。
     */
    private class PlayerCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.i("Service", "再生終了");

            //Intent intent = new Intent(MediaPlayerService.this, PlayScreen.class);
            //intent.putExtra("playing", mp.isPlaying());

            nowState = "Stop";
            sendBroadCast(nowState);

            // stop service
            stopSelf();
        }
    }
}
