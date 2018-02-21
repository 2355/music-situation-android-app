package com.example.tlabuser.musicapplication;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tlabuser.musicapplication.Model.Album;
import com.example.tlabuser.musicapplication.Model.Artist;
import com.example.tlabuser.musicapplication.Model.ExTrack;
import com.example.tlabuser.musicapplication.Model.Situation;
import com.example.tlabuser.musicapplication.View.Player.PlayScreenFragment;
import com.example.tlabuser.musicapplication.View.Player.YoutubePlayScreenFragment;
import com.example.tlabuser.musicapplication.View.Root.RootFragment;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.tlabuser.musicapplication.CalendarUtil.calToSituations;
import static com.example.tlabuser.musicapplication.MediaPlayerService.CLICK;
import static com.example.tlabuser.musicapplication.MediaPlayerService.State.pause;
import static com.example.tlabuser.musicapplication.MediaPlayerService.State.playing;
import static com.example.tlabuser.musicapplication.MediaPlayerService.State.stop;
import static com.example.tlabuser.musicapplication.MediaPlayerService.URI;
import static com.example.tlabuser.musicapplication.MediaPlayerService.setMainPSListener;


public class Main extends AppCompatActivity {

    public final String TAG = "Main";

    private final int PERMISSION_INITIAL = 1;

    private static MainLifecycleListener mlListener;
    private static ChangeFragmentListener cfListener;
    private static FromListener fListener;
    private static NowSituationListener nsListener;

    public MediaPlayerService.State mpState = stop;

    private List<String> nowSituations;
    public List<String> getNowSituations() {return nowSituations;}

    private Situation focusedSituation;
    public  void      focusSituation(Situation item) {if(item != null) focusedSituation = item;}
    public  Situation getFocusedSituation() {return focusedSituation;}

    private Album focusedAlbum;
    public  void  focusAlbum(Album item) {if(item != null) focusedAlbum = item;}
    public  Album getFocusedAlbum() {return focusedAlbum ;}

    private Artist focusedArtist;
    public  void   focusArtist(Artist item) {if(item != null) focusedArtist = item;}
    public  Artist getFocusedArtist() {return focusedArtist ;}

    private ExTrack focusedExTrack;
    public  void   focusExTrack(ExTrack item) {if(item != null) focusedExTrack = item;}
    public  ExTrack getFocusedExTrack() {return focusedExTrack ;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Android 6, API 23以上でパーミッションの確認
        if(Build.VERSION.SDK_INT >= 23){
            // パーミッションの確認，許可されていなければ許可を求める
            initialCheckPermissions();
        }else{
            showFragment();
        }

        setMainPSListener(new MediaPlayerService.PlayerStateListener() {
            @Override
            public void onStop() {
                mpState = stop;
                getUserSituation();
            }

            @Override
            public void onPlaying() {
                mpState = playing;
            }

            @Override
            public void onPause() {
                mpState = pause;
            }
        });

        if (mlListener != null) {
            mpState = mlListener.getState();
        }
        // TODO focusExTrack(serviceExTrack)
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    private void initialCheckPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            showFragment();

        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_INITIAL
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_INITIAL
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            showFragment();
        }else{
            Toast.makeText(this, "許可がないと何もできません\nアプリを再起動してください", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Storage permission denied.");
        }
    }

    private void showFragment(){
        setContentView(R.layout.main);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_main, new RootFragment(),RootFragment.TAG)
                .commit();

        getUserSituation();
    }

    private void getUserSituation() {
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        nowSituations = calToSituations(cal);
        Log.d(TAG, "Time " + String.valueOf(nowSituations));
        if (nsListener != null) {
            nsListener.getNowSituation(nowSituations);
        }
        getWeather();
        getPlaces();
        getDetectedActivity();

        // TODO それぞれフラグを立てる、フラグがすべてオンになったら実行
        // TODO GoodButtonを押したときに実行
    }

    private void getWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.getSnapshotClient(this).getWeather()
                    .addOnSuccessListener(weatherResponse -> {
                        Weather weather = weatherResponse.getWeather();

                        nowSituations.addAll(getWeatherConditions(weather));
                        Log.d(TAG, "Weathers " + String.valueOf(nowSituations));
                        nsListener.getNowSituation(nowSituations);
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Could not get weather: " + e));
        }
    }

    private List<String> getWeatherConditions(Weather weather) {
        List<String> conditions = new ArrayList<>();

        int[] condition = weather.getConditions();
        float temp = weather.getTemperature(Weather.CELSIUS);

        for (int con : condition) {
            switch (con) {
                case Weather.CONDITION_CLEAR: conditions.add("晴"); break;
                case Weather.CONDITION_CLOUDY: conditions.add("曇"); break;
                case Weather.CONDITION_FOGGY:
                case Weather.CONDITION_HAZY: conditions.add("霧"); break;
                case Weather.CONDITION_ICY: conditions.add("寒い"); break;
                case Weather.CONDITION_RAINY: conditions.add("雨"); break;
                case Weather.CONDITION_SNOWY: conditions.add("雪"); break;
                case Weather.CONDITION_STORMY: conditions.add("嵐"); break;
                case Weather.CONDITION_WINDY: conditions.add("風"); break;
            }

        }
        if (temp < 10) conditions.add("寒い");
        if (temp > 30) conditions.add("暑い");

        return conditions;
    }

    private void getPlaces() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.getSnapshotClient(this).getPlaces()
                    .addOnSuccessListener(placesResponse -> {
                        List<PlaceLikelihood> pls = placesResponse.getPlaceLikelihoods();
                        if (pls != null) {
                            for (PlaceLikelihood pl : pls){
                                if (pl.getLikelihood() == 0.0) break;

                                List<String> types = getPlaceTypes(pl.getPlace().getPlaceTypes());
                                nowSituations.addAll(types);
                            }
                            nsListener.getNowSituation(nowSituations);
                        }
                        Log.d(TAG, "Places " + String.valueOf(nowSituations));
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Could not get places: " + e));
        }
    }

    private List<String> getPlaceTypes(List<Integer> types) {
        List<String> placeTypes = new ArrayList<>();

        for (int type : types) {
            switch (type) {
                case Place.TYPE_STORE: placeTypes.add("ショッピング"); break;
                case Place.TYPE_RESTAURANT: placeTypes.add("レストラン"); break;
                case Place.TYPE_CAFE: placeTypes.add("カフェ"); break;
                case Place.TYPE_SCHOOL:
                case Place.TYPE_UNIVERSITY: placeTypes.add("学校"); break;
                case Place.TYPE_LIBRARY: placeTypes.add("図書館"); break;
                case Place.TYPE_STADIUM:
                case Place.TYPE_GYM: placeTypes.add("スポーツ"); break;
                case Place.TYPE_PARK: placeTypes.add("公園"); break;
                case Place.TYPE_ZOO: placeTypes.add("動物園"); break;
                case Place.TYPE_AQUARIUM: placeTypes.add("水族館"); break;
                case Place.TYPE_MUSEUM: placeTypes.add("博物館"); break;
                case Place.TYPE_TRAIN_STATION: placeTypes.add("駅"); break;
            }
        }
        return placeTypes;
    }

    private void getDetectedActivity() {
        Awareness.getSnapshotClient(this).getDetectedActivity()
                .addOnSuccessListener(dar -> {
                    ActivityRecognitionResult arr = dar.getActivityRecognitionResult();
                    List<DetectedActivity> das = arr.getProbableActivities();
                    for (DetectedActivity da : das) {
                        if (da.getConfidence() < 10) break;
                        String type = getDetectedActivityType(da.getType());
                        if (type != null) nowSituations.add(type);
                    }
                    Log.d(TAG, "Activities " + String.valueOf(nowSituations));
                    nsListener.getNowSituation(nowSituations);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Could not detect activity: " + e));

    }

    private String getDetectedActivityType(int type) {
        String activityType = null;
        switch (type) {
            case DetectedActivity.IN_VEHICLE: activityType = "ドライブ"; break;
            case DetectedActivity.ON_BICYCLE: activityType = "ツーリング"; break;
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.WALKING: activityType = "散歩"; break;
            case DetectedActivity.RUNNING: activityType = "ランニング"; break;
        }
        return activityType;
    }

    public AdapterView.OnItemClickListener AlbumClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        focusAlbum( (Album)lv.getItemAtPosition(position) );
        cfListener.setFragment(RootFragment.Scene.album);
    };

    public AdapterView.OnItemLongClickListener AlbumLongClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        Album item = (Album)lv.getItemAtPosition(position);
        Toast.makeText(Main.this, "LongClick:"+item.album, Toast.LENGTH_LONG).show();
        return true;
    };

    public AdapterView.OnItemClickListener ArtistClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        focusArtist( (Artist)lv.getItemAtPosition(position) );
        cfListener.setFragment(RootFragment.Scene.artist);
    };

    public AdapterView.OnItemLongClickListener ArtistLongClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        Artist item = (Artist)lv.getItemAtPosition(position);
        Toast.makeText(Main.this, "LongClick:"+item.artist, Toast.LENGTH_LONG).show();
        return true;
    };

    public AdapterView.OnItemClickListener ExTrackClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        focusExTrack( (ExTrack) lv.getItemAtPosition(position) );

        // stop service before transition
        mpState = stop;
        Intent intent = new Intent(this, MediaPlayerService.class);
        stopService(intent);

        YoutubePlayScreenFragment fragment = YoutubePlayScreenFragment.newInstance(YoutubePlayScreenFragment.From.track);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_root, fragment, YoutubePlayScreenFragment.TAG)
                .addToBackStack(YoutubePlayScreenFragment.TAG)
                .commit();

        fListener.setFrom(RootFragment.BackFrom.youtubePlayScreen);
    };

    public AdapterView.OnItemLongClickListener ExTrackLongClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        ExTrack item = (ExTrack) lv.getItemAtPosition(position);
        Toast.makeText(Main.this, "LongClick: " + item.title, Toast.LENGTH_LONG).show();
        return true;
    };

    public AdapterView.OnItemClickListener internalExTrackClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        focusExTrack( (ExTrack) lv.getItemAtPosition(position) );

        PlayScreenFragment fragment = PlayScreenFragment.newInstance(PlayScreenFragment.From.track, mpState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_root, fragment, PlayScreenFragment.TAG)
                .addToBackStack(PlayScreenFragment.TAG)
                .commit();

        fListener.setFrom(RootFragment.BackFrom.playScreen);
    };

    public AdapterView.OnItemLongClickListener internalExTrackLongClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        ExTrack item = (ExTrack) lv.getItemAtPosition(position);
        Toast.makeText(Main.this, "LongClick: " + item.title, Toast.LENGTH_LONG).show();
        return true;
    };

    public void onPlayButtonClick(View view) {
        Toast.makeText(this, "Click:PlayButton", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "onPlayButtonClick mpState: " + mpState);

        Intent intent = new Intent(this, MediaPlayerService.class);
        switch(mpState){
            case stop:
                intent.putExtra(CLICK, MediaPlayerService.Click.start.toString());
                intent.putExtra(URI, "");
                this.startService(intent);
                break;

            case playing:
                intent.putExtra(CLICK, MediaPlayerService.Click.pause.toString());
                this.startService(intent);
                break;

            case pause:
                intent.putExtra(CLICK, MediaPlayerService.Click.restart.toString());
                this.startService(intent);
                break;
        }
    }

    // 起動時にplayerStateを取得
    public interface MainLifecycleListener {
        MediaPlayerService.State getState();
    }

    public static void setMainLifecycleListener(MainLifecycleListener l) {
        mlListener = l;
    }

    // どのFragmentに遷移したかを通知
    public interface ChangeFragmentListener {
        void setFragment(RootFragment.Scene scene);
    }

    public void setChangeFragmentListener(ChangeFragmentListener l) {
        cfListener = l;
    }

    // どちらのplayScreenから戻ってきたかを通知
    public interface FromListener {
        void setFrom(RootFragment.BackFrom from);
    }

    public void setFromListener(FromListener l) {
        fListener = l;
    }

    // SituationMenuFragmentにrecommendedSituationsを表示
    public interface NowSituationListener {
        void getNowSituation(List<String> nowSituations);
    }

    public static void setNowSituationListener(NowSituationListener l) {
        nsListener = l;
    }
}
