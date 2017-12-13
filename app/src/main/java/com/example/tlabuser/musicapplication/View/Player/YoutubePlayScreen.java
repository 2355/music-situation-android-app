package com.example.tlabuser.musicapplication.View.Player;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tlabuser.musicapplication.JsonLoader;
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


/**
 * This is YoutubePlayScreen class.
 * called by click a SituationTrack.
 */

public class YoutubePlayScreen extends FragmentActivity implements LoaderManager.LoaderCallbacks<JSONObject>{
    // API キー
    private static final String API_KEY = Keys.YT_KEY;
    // YouTubeのビデオID
    private static String videoId;
    // 検索クエリ
    private String query;

    private YouTubePlayerSupportFragment youTubePlayerFragment;

    private static ExTrack exTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_play_screen);

        exTrack = Main.getFocusedExTrack();

        TextView title      = (TextView) findViewById(R.id.title);
        TextView artist     = (TextView) findViewById(R.id.artist);

        title.setText( exTrack.title);
        artist.setText(exTrack.artist);

        query = exTrack.artist + exTrack.title;

        // YouTubeフラグメントインスタンスを取得
        youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();

        // レイアウトにYouTubeフラグメントを追加
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.youtube_layout, youTubePlayerFragment).commit();

        // JSONの取得
        getSupportLoaderManager().restartLoader(1, null, this);

        Toast.makeText(this, "動画を取得しています。\nしばらくお待ちください。", Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        String head = "https://www.googleapis.com/youtube/v3/search?key=";
        String params = "&part=snippet&type=video&maxResults=1&q=";
        try {
            query = URLEncoder.encode(query, "UTF-8");
        }catch (UnsupportedEncodingException e){
            Log.d("onCreateLoader","URLエンコードに失敗しました。 UnsupportedEncodingException=" + e);
        }
        String urlStr = head + API_KEY + params + query;

        JsonLoader jsonLoader = new JsonLoader(this, urlStr);
        jsonLoader.forceLoad();
        return  jsonLoader;
    }

    @Override
    public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
        if (data != null) {
            try {
                videoId = data.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");

                // YouTubeフラグメントのプレーヤーを初期化する
                youTubePlayerFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {

                    // YouTubeプレーヤーの初期化成功
                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                        if (!wasRestored) {
                            player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                            player.loadVideo(videoId);
                            player.play();
                        }
                    }

                    // YouTubeプレーヤーの初期化失敗
                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
                        // YouTube error
                        String errorMessage = error.toString();
                        Toast.makeText(YoutubePlayScreen.this, errorMessage, Toast.LENGTH_LONG).show();
                        Log.d("errorMessage:", errorMessage);
                    }
                });
            } catch (JSONException e) {
                Log.d("onLoadFinished","JSONのパースに失敗しました。 JSONException=" + e);
                Toast.makeText(this, "動画がありません。", Toast.LENGTH_SHORT).show();
            }
        }else{
            Log.d("onLoadFinished", "onLoadFinished error!");
        }
    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {
        // 処理なし
    }
}
