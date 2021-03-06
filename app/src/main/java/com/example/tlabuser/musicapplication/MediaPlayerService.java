package com.example.tlabuser.musicapplication;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import static com.example.tlabuser.musicapplication.Main.setMainLifecycleListener;
import static com.example.tlabuser.musicapplication.MediaPlayerService.State.pause;
import static com.example.tlabuser.musicapplication.MediaPlayerService.State.playing;
import static com.example.tlabuser.musicapplication.MediaPlayerService.State.stop;

public class MediaPlayerService extends Service {

    public static final String TAG = "MediaPlayerService";

    public static final String STATE = "state";
    public static final String CLICK = "click";
    public static final String URI = "uri";
    public static final String DURATION = "duration";

    public enum State { stop, playing, pause }
    private State state;

    public enum Click { start, pause, restart, back, skip }
    private Click click;

    private MediaPlayer player;

    public static PlayerStateListener mainPSListener, rootPSListener, playScreenPSListener;


    @Override
    public void onCreate() {
        super.onCreate();

        player = new MediaPlayer();
        state = stop;

        setMainLifecycleListener(() -> state);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        click = Click.valueOf(intent.getStringExtra(CLICK));
        Log.d(TAG, "onStartCommand:" + click.toString());

        switch(click){
            case start:
                // Stop player when playing other song
                if(state == playing || state == pause) {
                    player.stop();
                    player.reset();
                }

                String uriStr = intent.getStringExtra(URI);
                if (uriStr.equals("")) {
                    Log.d(TAG, "URI = null");
                    break;
                }

                Uri uri = Uri.parse(uriStr);
                try {
                    player.setDataSource(MediaPlayerService.this, uri);
                    player.setOnPreparedListener(new PlayerPreparedListener());
                    player.setOnCompletionListener(new PlayerCompletionListener());
                    player.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case pause:
                player.pause();
                state = pause;
                listener.onPause();
                break;

            case restart:
                player.start();
                state = playing;
                listener.onPlaying();
                break;

            case back:
                player.seekTo(0);
                break;

            case skip:
                long duration = intent.getLongExtra(DURATION, 0);
                player.seekTo((int) duration);
                break;
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "サービス破棄");
        if(player.isPlaying()) {
            player.stop();
        }
        player.release();
        player = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * メディア再生準備が完了時のリスナクラス。
     */
    private class PlayerPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.i(TAG, "再生開始");

            mp.start();
            state = playing;
            listener.onPlaying();

            /* // notification
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
            */
        }
    }

    /**
     * メディア再生が終了したときのリスナクラス。
     */
    private class PlayerCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.i(TAG, "再生終了");

            state = stop;
            listener.onStop();

            // stop service
            stopSelf();
        }
    }

    public interface PlayerStateListener {
        void onStop();
        void onPlaying();
        void onPause();
    }

    private PlayerStateListener listener = new PlayerStateListener() {
        @Override
        public void onStop() {
            mainPSListener.onStop();
            rootPSListener.onStop();
            playScreenPSListener.onStop();
        }

        @Override
        public void onPlaying() {
            mainPSListener.onPlaying();
            rootPSListener.onPlaying();
            playScreenPSListener.onPlaying();
        }

        @Override
        public void onPause() {
            mainPSListener.onPause();
            rootPSListener.onPause();
            playScreenPSListener.onPause();
        }
    };

    public State getState() {
        return state;
    }

    public static void setMainPSListener(PlayerStateListener l) {
        mainPSListener = l;
    }

    public static void setRootPSListener(PlayerStateListener l) {
        rootPSListener = l;
    }

    public static void setPlayScreenPSListener(PlayerStateListener l) {
        playScreenPSListener = l;
    }
}
