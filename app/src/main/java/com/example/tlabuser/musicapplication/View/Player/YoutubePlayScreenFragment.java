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

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by tlabuser on 2018/02/03.
 */

public class YoutubePlayScreenFragment extends Fragment {

    public static final String TAG = "YoutubePSFragment";

    private static final String API_KEY = Keys.YOUTUBE_KEY;

    private Main mainActivity;

    private ExTrack exTrack;
    private String title, artist;

    private TextView tvTitle, tvArtist;
    private ImageButton btGood, btBad;
    private YouTubePlayerSupportFragment player;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (Main) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_youtube_play_screen, container, false);

        tvTitle  = (TextView) view.findViewById(R.id.tv_title);
        tvArtist = (TextView) view.findViewById(R.id.tv_artist);

        btGood = (ImageButton) view.findViewById(R.id.bt_good);
        btBad  = (ImageButton) view.findViewById(R.id.bt_bad);

        exTrack = Main.getFocusedExTrack();
        if (exTrack == null) {
            title = "-";
            artist = "-";
        } else {
            title = exTrack.title;
            artist = exTrack.artist;
        }

        tvTitle.setText(title);
        tvArtist.setText(artist);

        btGood.setOnClickListener(this::onGoodButtonClick);
        btBad.setOnClickListener(this::onBadButtonClick);

        // YouTubeフラグメントインスタンスを取得
        player = YouTubePlayerSupportFragment.newInstance();

        // レイアウトにYouTubeフラグメントを追加
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_youtube_player, player)
                .commit();

        // Get JSON from server
        Single.create((SingleOnSubscribe<JSONObject>) emitter -> emitter.onSuccess(requestJson()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGetJson);

        Toast.makeText(mainActivity, "動画を取得しています。\nしばらくお待ちください。", Toast.LENGTH_SHORT).show();

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();

        //TODO ここでplaypanelをアップデートする（リスナー）作る
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
            Log.d(TAG, "JSONObject is null");
            Toast.makeText(mainActivity, "読み込みエラーが発生しました。", Toast.LENGTH_SHORT).show();
        }
    }

    public void onBadButtonClick(View view) {
        Toast.makeText(mainActivity, "BadButtonClick", Toast.LENGTH_SHORT).show();
    }

    public void onGoodButtonClick(View view) {
        Toast.makeText(mainActivity, "GoodButtonClick", Toast.LENGTH_SHORT).show();
    }
}
