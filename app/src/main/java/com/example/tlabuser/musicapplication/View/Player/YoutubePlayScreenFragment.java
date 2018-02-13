package com.example.tlabuser.musicapplication.View.Player;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tlabuser.musicapplication.JsonUtil;
import com.example.tlabuser.musicapplication.Keys;
import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.Model.ExTrack;
import com.example.tlabuser.musicapplication.R;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.example.tlabuser.musicapplication.View.Player.YoutubePlayScreenFragment.State.pause;
import static com.example.tlabuser.musicapplication.View.Player.YoutubePlayScreenFragment.State.playing;
import static com.example.tlabuser.musicapplication.View.Player.YoutubePlayScreenFragment.State.stop;

/**
 * Created by tlabuser on 2018/02/03.
 */

public class YoutubePlayScreenFragment extends Fragment {

    public static final String TAG = "YoutubePSFragment";

    private static final String API_KEY = Keys.YOUTUBE_KEY;

    public static final String FROM = "from";

    public enum State { stop, playing, pause }
    private State state;

    public enum From { track, panel }
    private From from;

    private Main mainActivity;

    private ExTrack exTrack;
    private String title, artist;

    private TextView tvTitle, tvArtist;
    private ImageButton btPlay, btBack, btSkip, btGood, btBad, btSurprise;

    private YouTubePlayerSupportFragment playerFragment;
    private YouTubePlayer player;


    public static YoutubePlayScreenFragment newInstance(From from) {
        Bundle args = new Bundle();
        args.putString(FROM, from.toString());

        YoutubePlayScreenFragment fragment = new YoutubePlayScreenFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (Main) getActivity();
        state = stop;
        from = From.valueOf(getArguments().getString(FROM));
        Log.d(TAG, "from: " + from);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_youtube_play_screen, container, false);

        tvTitle  = (TextView) view.findViewById(R.id.tv_title);
        tvArtist = (TextView) view.findViewById(R.id.tv_artist);

        btPlay      = (ImageButton) view.findViewById(R.id.bt_play);
        btBack      = (ImageButton) view.findViewById(R.id.bt_back);
        btSkip      = (ImageButton) view.findViewById(R.id.bt_skip);
        btGood      = (ImageButton) view.findViewById(R.id.bt_good);
        btBad       = (ImageButton) view.findViewById(R.id.bt_bad);
        btSurprise  = (ImageButton) view.findViewById(R.id.bt_surprise);

        exTrack = mainActivity.getFocusedExTrack();
        if (exTrack == null) {
            title = "-";
            artist = "-";
        } else {
            title = exTrack.title;
            artist = exTrack.artist;

            Log.d(TAG, exTrack.title + " " + exTrack.artist + " " + exTrack.album + " " + exTrack.albumArt);


            // TODO setting lifecycle method
            player = new YouTubePlayer() {
                @Override
                public void release() {

                }

                @Override
                public void cueVideo(String s) {

                }

                @Override
                public void cueVideo(String s, int i) {

                }

                @Override
                public void loadVideo(String s) {

                }

                @Override
                public void loadVideo(String s, int i) {

                }

                @Override
                public void cuePlaylist(String s) {

                }

                @Override
                public void cuePlaylist(String s, int i, int i1) {

                }

                @Override
                public void loadPlaylist(String s) {

                }

                @Override
                public void loadPlaylist(String s, int i, int i1) {

                }

                @Override
                public void cueVideos(List<String> list) {

                }

                @Override
                public void cueVideos(List<String> list, int i, int i1) {

                }

                @Override
                public void loadVideos(List<String> list) {

                }

                @Override
                public void loadVideos(List<String> list, int i, int i1) {

                }

                @Override
                public void play() {

                }

                @Override
                public void pause() {

                }

                @Override
                public boolean isPlaying() {
                    return false;
                }

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public boolean hasPrevious() {
                    return false;
                }

                @Override
                public void next() {

                }

                @Override
                public void previous() {

                }

                @Override
                public int getCurrentTimeMillis() {
                    return 0;
                }

                @Override
                public int getDurationMillis() {
                    return 0;
                }

                @Override
                public void seekToMillis(int i) {

                }

                @Override
                public void seekRelativeMillis(int i) {

                }

                @Override
                public void setFullscreen(boolean b) {

                }

                @Override
                public void setOnFullscreenListener(OnFullscreenListener onFullscreenListener) {

                }

                @Override
                public void setFullscreenControlFlags(int i) {

                }

                @Override
                public int getFullscreenControlFlags() {
                    return 0;
                }

                @Override
                public void addFullscreenControlFlag(int i) {

                }

                @Override
                public void setPlayerStyle(PlayerStyle playerStyle) {

                }

                @Override
                public void setShowFullscreenButton(boolean b) {

                }

                @Override
                public void setManageAudioFocus(boolean b) {

                }

                @Override
                public void setPlaylistEventListener(PlaylistEventListener playlistEventListener) {

                }

                @Override
                public void setPlayerStateChangeListener(PlayerStateChangeListener playerStateChangeListener) {

                }

                @Override
                public void setPlaybackEventListener(PlaybackEventListener playbackEventListener) {

                }
            };

            // YouTubeフラグメントインスタンスを取得
            playerFragment = YouTubePlayerSupportFragment.newInstance();

            // レイアウトにYouTubeフラグメントを追加
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_youtube_player, playerFragment)
                    .commit();

            // Get JSON from server
            Single.create((SingleOnSubscribe<JSONObject>) emitter -> emitter.onSuccess(requestJson()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onGetJson);

            Toast.makeText(mainActivity, "動画を取得しています。\nしばらくお待ちください。", Toast.LENGTH_SHORT).show();
        }

        tvTitle.setText(title);
        tvArtist.setText(artist);

        btPlay.setOnClickListener(this::onPlayButtonClick);
        btBack.setOnClickListener(this::onBackButtonClick);
        btSkip.setOnClickListener(this::onSkipButtonClick);
        btGood.setOnClickListener(this::onGoodButtonClick);
        btBad.setOnClickListener(this::onBadButtonClick);
        btSurprise.setOnClickListener(this::onSurpriseButtonClick);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();

        //TODO releaseじゃなくて置いておきたい
        player.release();
    }

    @Nullable
    private JSONObject requestJson() {
        String head = "https://www.googleapis.com/youtube/v3/search?key=";
        String params = "&part=snippet&type=video&maxResults=1&q=";
        String query = exTrack.artist + " " + exTrack.title;
        try {
            query = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e){
            Log.d(TAG,"URLエンコードに失敗しました。 UnsupportedEncodingException=" + e);
        }
        String urlStr = head + API_KEY + params + query;

        return JsonUtil.getJson(urlStr);
    }

    private void onGetJson(JSONObject json) {
        if (json != null) {
            try {
                String videoId = json.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");

                playerFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                        if (!wasRestored) {
                            initializePlayer(player, videoId);
                        }
                    }

                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
                        // error handling
                        String errorMessage = error.toString();
                        Toast.makeText(mainActivity, "読み込みエラーが発生しました。", Toast.LENGTH_LONG).show();
                        Log.d(TAG, errorMessage);
                    }
                });

            } catch (JSONException e) {
                Log.d(TAG,"JSONのパースに失敗しました。 JSONException=" + e);
                Toast.makeText(mainActivity, "動画がありません。", Toast.LENGTH_SHORT).show();
            }

        } else {
            Log.d(TAG, "JSONObject is null !");
            Toast.makeText(mainActivity, "読み込みエラーが発生しました。", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializePlayer(YouTubePlayer player, String videoId) {
        this.player = player;
        this.player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
        this.player.setPlaybackEventListener(new PlaybackListener());
        // load and play
        this.player.loadVideo(videoId);
        if (from == From.panel) {
            // TODO panelからのときは止めておきたい
            //this.player.pause();
        }
    }

    private class PlaybackListener implements YouTubePlayer.PlaybackEventListener {
        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onPaused() {
            state = pause;
            btPlay.setImageResource(R.drawable.icon_play);
        }

        @Override
        public void onPlaying() {
            state = playing;
            btPlay.setImageResource(R.drawable.icon_pause);
        }

        @Override
        public void onSeekTo(int i) {

        }

        @Override
        public void onStopped() {
            state = stop;
            btPlay.setImageResource(R.drawable.icon_play);
        }
    }

    private void onPlayButtonClick(View view) {
        Toast.makeText(mainActivity, "PlayButtonClick", Toast.LENGTH_SHORT).show();
        switch (state) {
            case stop:    player.play(); break;
            case playing: player.pause(); break;
            case pause:   player.play(); break;
        }
    }

    private void onBackButtonClick(View view) {
        Toast.makeText(mainActivity, "BackButtonClick", Toast.LENGTH_SHORT).show();
        player.seekToMillis(0);
    }

    private void onSkipButtonClick(View view) {
        Toast.makeText(mainActivity, "SkipButtonClick", Toast.LENGTH_SHORT).show();
        int duration = player.getDurationMillis();
        player.seekToMillis(duration);
    }

    public void onBadButtonClick(View view) {
        Toast.makeText(mainActivity, "BadButtonClick", Toast.LENGTH_SHORT).show();
    }

    public void onGoodButtonClick(View view) {
        Toast.makeText(mainActivity, "GoodButtonClick", Toast.LENGTH_SHORT).show();
    }

    private void onSurpriseButtonClick(View view) {
        Toast.makeText(mainActivity, "SurpriseButtonClick", Toast.LENGTH_SHORT).show();
    }

}
