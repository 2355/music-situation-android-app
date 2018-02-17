package com.example.tlabuser.musicapplication.View.Player;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tlabuser.musicapplication.ImageGetTask;
import com.example.tlabuser.musicapplication.JsonUtil;
import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.MediaPlayerService;
import com.example.tlabuser.musicapplication.Model.ExTrack;
import com.example.tlabuser.musicapplication.R;
import com.example.tlabuser.musicapplication.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.example.tlabuser.musicapplication.CalendarUtil.calToStr;
import static com.example.tlabuser.musicapplication.MediaPlayerService.CLICK;
import static com.example.tlabuser.musicapplication.MediaPlayerService.DURATION;
import static com.example.tlabuser.musicapplication.MediaPlayerService.STATE;
import static com.example.tlabuser.musicapplication.MediaPlayerService.State.pause;
import static com.example.tlabuser.musicapplication.MediaPlayerService.State.playing;
import static com.example.tlabuser.musicapplication.MediaPlayerService.State.stop;
import static com.example.tlabuser.musicapplication.MediaPlayerService.URI;
import static com.example.tlabuser.musicapplication.MediaPlayerService.setPlayScreenListener;

/**
 * Created by tlabuser on 2018/02/01.
 */

public class PlayScreenFragment extends Fragment {

    public static final String TAG = "PlayScreenFragment";

    public static final String FROM = "from";

    public enum From { track, panel, notification }
    private From from;

    private MediaPlayerService.State state;

    private Main mainActivity;

    private ExTrack exTrack;
    private String title, artist, album, uri, albumArt;
    private long duration;

    private TextView tvTitle, tvArtist, tvAlbum;
    private ImageView ivAlbumArt;
    private ImageButton btPlay, btBack, btSkip, btGood, btBad, btSurprise;


    public static PlayScreenFragment newInstance(From from, MediaPlayerService.State state) {
        Bundle args = new Bundle();
        args.putString(FROM, from.toString());
        args.putString(STATE, state.toString());

        PlayScreenFragment fragment = new PlayScreenFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (Main) getActivity();

        from = From.valueOf(getArguments().getString(FROM));
        state = MediaPlayerService.State.valueOf(getArguments().getString(STATE));
        Log.d(TAG, "from: " + from);
        Log.d(TAG, "state: " + state);

        setPlayScreenListener(new MediaPlayerService.PlayerStateListener() {
            @Override
            public void onStop() {
                Log.d(TAG, "onStop");
                PlayScreenFragment.this.state = stop;
                btPlay.setImageResource(R.drawable.icon_play);
                // TODO updateExTrack(extrack, situations);
            }

            @Override
            public void onPlaying() {
                Log.d(TAG, "onPlaying");
                PlayScreenFragment.this.state = playing;
                btPlay.setImageResource(R.drawable.icon_pause);
            }

            @Override
            public void onPause() {
                Log.d(TAG, "onPause");
                PlayScreenFragment.this.state = pause;
                btPlay.setImageResource(R.drawable.icon_play);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_play_screen, container, false);

        tvTitle    = (TextView) view.findViewById(R.id.tv_title);
        tvArtist   = (TextView) view.findViewById(R.id.tv_artist);
        tvAlbum    = (TextView) view.findViewById(R.id.tv_album);
        ivAlbumArt = (ImageView) view.findViewById(R.id.iv_album_art);

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
            album = "-";
            uri = "";
            duration = 0;
            albumArt = "";

        } else {
            exTrack.addAlbumArt(mainActivity, exTrack.albumId);
            title = exTrack.title;
            artist = exTrack.artist;
            album = exTrack.album;
            uri = exTrack.uri.toString();
            duration = exTrack.duration;
            albumArt = exTrack.albumArt;
            Log.d(TAG, exTrack.title + " " + exTrack.artist + " " + exTrack.album + " " + exTrack.albumArt);
        }

        tvTitle.setText(title);
        tvArtist.setText(artist);
        tvAlbum.setText(album);
        ivAlbumArt.setImageResource(R.drawable.icon_album);
        if (albumArt != null && albumArt != "") {
            ivAlbumArt.setTag(albumArt);
            ImageGetTask task = new ImageGetTask(ivAlbumArt);
            task.execute(albumArt);
        }

        switch (from){
            case track:
                Intent intent = new Intent(mainActivity, MediaPlayerService.class);
                state = playing;
                intent.putExtra(CLICK, MediaPlayerService.Click.start.toString());
                intent.putExtra(URI, uri);
                mainActivity.startService(intent);
                btPlay.setImageResource(R.drawable.icon_pause);
                break;

            case panel:
                switch (state){
                    case stop:
                        btPlay.setImageResource(R.drawable.icon_play);
                        break;

                    case playing:
                        btPlay.setImageResource(R.drawable.icon_pause);
                        break;

                    case pause:
                        btPlay.setImageResource(R.drawable.icon_play);
                        break;
                }
                break;

            case notification:
                state = stop;
                break;
        }

        btPlay.setOnClickListener(this::onPlayButtonClick);
        btBack.setOnClickListener(this::onBackButtonClick);
        btSkip.setOnClickListener(this::onSkipButtonClick);
        btGood.setOnClickListener(this::onGoodButtonClick);
        btBad.setOnClickListener(this::onBadButtonClick);
        btSurprise.setOnClickListener(this::onSurpriseButtonClick);

        return view;
    }

    private void onPlayButtonClick(View view) {
        Toast.makeText(mainActivity, "PlayButtonClick", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onPlayButtonClick state:" + state);

        Intent intent = new Intent(mainActivity, MediaPlayerService.class);
        switch(state){
            case stop:
                intent.putExtra(CLICK, MediaPlayerService.Click.start.toString());
                intent.putExtra(URI, uri);
                mainActivity.startService(intent);
                break;

            case playing:
                intent.putExtra(CLICK, MediaPlayerService.Click.pause.toString());
                mainActivity.startService(intent);
                break;

            case pause:
                intent.putExtra(CLICK, MediaPlayerService.Click.restart.toString());
                mainActivity.startService(intent);
                break;
        }
    }

    private void onBackButtonClick(View view) {
        Toast.makeText(mainActivity, "BackButtonClick", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(mainActivity, MediaPlayerService.class);
        intent.putExtra(CLICK, MediaPlayerService.Click.back.toString());
        mainActivity.startService(intent);
    }

    private void onSkipButtonClick(View view) {
        Toast.makeText(mainActivity, "SkipButtonClick", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(mainActivity, MediaPlayerService.class);
        intent.putExtra(CLICK, MediaPlayerService.Click.skip.toString());
        intent.putExtra(DURATION, duration);
        mainActivity.startService(intent);
    }

    private void onGoodButtonClick(View view) {
        // Get JSON from server
        Single.create((SingleOnSubscribe<JSONObject>) emitter -> emitter.onSuccess(requestJson("suit")))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGetJson);
    }

    private void onBadButtonClick(View view) {
    }

    private void onSurpriseButtonClick(View view) {
        // Get JSON from server
        Single.create((SingleOnSubscribe<JSONObject>) emitter -> emitter.onSuccess(requestJson("unexp")))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGetJson);
    }

    @Nullable
    private JSONObject requestJson(String evaluation) {
        String uid = "test";
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        String time = calToStr(cal);
        String artist = exTrack.artist;
        String title = exTrack.title;
        String selected_situation = mainActivity.getFocusedSituation().name;
        String now_situations = mainActivity.getNowSituations().toString();

        try {
            uid = URLEncoder.encode(uid, "UTF-8");
            time = URLEncoder.encode(time, "UTF-8");
            artist = URLEncoder.encode(artist, "UTF-8");
            title = URLEncoder.encode(title, "UTF-8");
            selected_situation = URLEncoder.encode(selected_situation, "UTF-8");
            now_situations = URLEncoder.encode(now_situations, "UTF-8");
            evaluation = URLEncoder.encode(evaluation, "UTF-8");
        } catch (UnsupportedEncodingException e){
            Log.d(TAG,"URLエンコードに失敗しました。 UnsupportedEncodingException=" + e);
        }
        String urlStr = String.format(Urls.Evaluation.SET, uid, time, artist, title,
                selected_situation, now_situations, evaluation);
        urlStr = Urls.Evaluation.HEAD + urlStr;

        return JsonUtil.getJson(urlStr);
    }

    private void onGetJson(JSONObject json) {
        if (json != null) {
            try {
                if (json.getString("status") == "OK") {
                    Log.d(TAG, "send evaluation succeeded");
                    Toast.makeText(mainActivity, "send evaluation succeeded", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                Log.d(TAG,"JSONのパースに失敗しました。 JSONException=" + e);
            }

        } else {
            Log.d(TAG, "JSONArray is null !");
        }
    }

}
