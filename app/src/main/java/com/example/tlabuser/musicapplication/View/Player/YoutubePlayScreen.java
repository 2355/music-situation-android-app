package com.example.tlabuser.musicapplication.View.Player;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
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

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * This is YoutubePlayScreen class.
 * called by click a ExTrack.
 */

public class YoutubePlayScreen extends FragmentActivity{

    private static final String TAG = "YoutubePlayScreen";

    private static final String API_KEY = Keys.YOUTUBE_KEY;

    private YouTubePlayerSupportFragment player;

    private static ExTrack exTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_play_screen);

        exTrack = Main.getFocusedExTrack();

        TextView title  = (TextView) findViewById(R.id.title);
        TextView artist = (TextView) findViewById(R.id.artist);

        title.setText(exTrack.title);
        artist.setText(exTrack.artist);

        // YouTubeフラグメントインスタンスを取得
        player = YouTubePlayerSupportFragment.newInstance();

        // レイアウトにYouTubeフラグメントを追加
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.youtube_layout, player).commit();

        // Get JSON from server
        Single.create((SingleOnSubscribe<JSONObject>) emitter -> emitter.onSuccess(requestJson()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGetJson);

        Toast.makeText(this, "動画を取得しています。\nしばらくお待ちください。", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    private JSONObject requestJson() {
        String head = "https://www.googleapis.com/youtube/v3/search?key=";
        String params = "&part=snippet&type=video&maxResults=1&q=";
        String query = exTrack.artist + " " + exTrack.title;
        try {
            query = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e){
            Log.d("onCreateLoader","URLエンコードに失敗しました。 UnsupportedEncodingException=" + e);
        }
        String urlStr = head + API_KEY + params + query;

        return JsonUtil.getJson(urlStr);
    }

    private void onGetJson(JSONObject json) {
        if (json != null) {
            try {
                String videoId = json.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");

                player.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {

                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                        if (!wasRestored) {
                            player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                            player.loadVideo(videoId);
                            player.play();
                        }
                    }

                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
                        // error
                        String errorMessage = error.toString();
                        Toast.makeText(YoutubePlayScreen.this, "読み込みエラーが発生しました。", Toast.LENGTH_LONG).show();
                        Log.d(TAG, errorMessage);
                    }
                });
            } catch (JSONException e) {
                Log.d(TAG,"JSONのパースに失敗しました。 JSONException=" + e);
                Toast.makeText(this, "動画がありません。", Toast.LENGTH_SHORT).show();
            }

        } else {
            Log.d(TAG, "JSONObject is null");
            Toast.makeText(this, "読み込みエラーが発生しました。", Toast.LENGTH_SHORT).show();
        }
    }

    public void onBadButtonClick(View view) {
        Toast.makeText(this, "BadButtonClick", Toast.LENGTH_SHORT).show();
    }

    public void onGoodButtonClick(View view) {
        Toast.makeText(this, "GoodButtonClick", Toast.LENGTH_SHORT).show();
    }
}
