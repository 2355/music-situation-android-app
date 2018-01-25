package com.example.tlabuser.musicapplication;


import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import com.example.tlabuser.musicapplication.View.Album.AlbumDetailFragment;
import com.example.tlabuser.musicapplication.View.Artist.ArtistDetailFragment;
import com.example.tlabuser.musicapplication.View.Player.PlayScreen;
import com.example.tlabuser.musicapplication.View.Player.YoutubePlayScreen;
import com.example.tlabuser.musicapplication.View.Root.RootMenuFragment;
import com.example.tlabuser.musicapplication.View.Situation.SituationDetailFragment;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.DetectedActivityResponse;
import com.google.android.gms.awareness.snapshot.PlacesResponse;
import com.google.android.gms.awareness.snapshot.WeatherResponse;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.tlabuser.musicapplication.CalendarUtil.calToSituations;
import static com.example.tlabuser.musicapplication.CalendarUtil.calToStr;


public class Main extends FragmentActivity{

    private final String TAG = "Main";

    private enum FrgmType { fRoot, fSituation, fAlbum, fArtist }
    private FrgmType fTop;

    private Situation focusedSituation;
    public  void      focusSituation(Situation item) {if(item != null) focusedSituation = item;}
    public  Situation getFocusedSituation() {return focusedSituation;}

    private Album focusedAlbum;
    public  void  focusAlbum(Album item) {if(item != null) focusedAlbum = item;}
    public  Album getFocusedAlbum() {return focusedAlbum ;}

    private Artist focusedArtist;
    public  void   focusArtist(Artist item) {if(item != null) focusedArtist = item;}
    public  Artist getFocusedArtist() {return focusedArtist ;}

    private static ExTrack focusedExTrack;
    public  void   focusExTrack(ExTrack item) {if(item != null) focusedExTrack = item;}
    public  static ExTrack getFocusedExTrack() {return focusedExTrack ;}

    PlayerBroadcastReceiver receiver;
    IntentFilter            intentFilter;

    private ImageView   ivAlbumArt;
    private TextView    tvTitle;
    private TextView    tvArtist;
    private ImageButton btBack;
    private ImageButton btPlay;
    private ImageButton btSkip;

    String nowState;

    private final int PERMISSION_INITIAL = 1;

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
    }

    @Override
    protected void onStop() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

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

    // test method
    private void getWeatherSnapshot() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.getSnapshotClient(this).getWeather()
                    .addOnSuccessListener(new OnSuccessListener<WeatherResponse>() {
                        @Override
                        public void onSuccess(WeatherResponse weatherResponse) {
                            Weather weather = weatherResponse.getWeather();
                            String conditions = String.valueOf(getConditions(weather.getConditions()));
                            String temp = String.valueOf(weather.getTemperature(Weather.CELSIUS));
                            String feelTemp = String.valueOf(weather.getFeelsLikeTemperature(Weather.CELSIUS));
                            String humidity = String.valueOf(weather.getHumidity());
                            String toast = "[Weather]:"
                                    + "\nConditions: " + conditions
                                    + ", \nTemperature: " + temp
                                    + ", \nFeelLikeTemperature: " + feelTemp
                                    + ", \nHumidity: " + humidity;

                            Toast.makeText(Main.this, toast, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, toast);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Could not get weather: " + e);
                        }
                    });
        }
    }

    private void getPlacesSnapshot() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.getSnapshotClient(this).getPlaces()
                    .addOnSuccessListener(new OnSuccessListener<PlacesResponse>() {
                        @Override
                        public void onSuccess(PlacesResponse placesResponse) {
                            String toast = "[Places]: ";
                            List<PlaceLikelihood> pls = placesResponse.getPlaceLikelihoods();
                            if (pls != null) {
                                for (PlaceLikelihood pl : pls) {
                                    double likelihood = pl.getLikelihood();
                                    Place place = pl.getPlace();
                                    String name = String.valueOf(place.getName());
                                    String type = String.valueOf(getPlaceTypes(place.getPlaceTypes()));
                                    toast += "\nlikehood: " + String.valueOf(likelihood)
                                            + ", \nname: " + name
                                            + ", \ntype: " + type
                                            + "\n--";
                                }
                            }
                            Toast.makeText(Main.this, toast, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, toast);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Could not get places: " + e);
                        }
                    });
        }
    }

    private void getDetectedActivitySnapshot() {
        Awareness.getSnapshotClient(this).getDetectedActivity()
                .addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
                    @Override
                    public void onSuccess(DetectedActivityResponse dar) {
                        String toast = "[Activities]: ";
                        ActivityRecognitionResult arr = dar.getActivityRecognitionResult();
                        List<DetectedActivity> das = arr.getProbableActivities();
                        for (DetectedActivity da : das) {
                            String type = getActivityType(da.getType());
                            int confidence = da.getConfidence();
                            if (confidence < 10) {
                                break;
                            } else {
                                toast += "\ntype: " + type + ", confidence: " + confidence + "\n--";
                            }
                        }
                        Toast.makeText(Main.this, toast, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, toast);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Could not detect activity: " + e);
                    }
                });
    }

    private List<String> getConditions(int[] condition) {
        List<String> conditions = new ArrayList<>();
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
                case Weather.CONDITION_UNKNOWN: conditions.add("不明"); break;
            }

        }
        return conditions;
    }

    private List<String> getPlaceTypes(List<Integer> types) {
        List<String> placeTypes = new ArrayList<>();
        for (int type : types) {
            switch (type) {
                case Place.TYPE_STORE: placeTypes.add("お店"); break;
                case Place.TYPE_RESTAURANT: placeTypes.add("レストラン"); break;
                case Place.TYPE_CAFE: placeTypes.add("カフェ"); break;
                case Place.TYPE_SCHOOL: placeTypes.add("学校"); break;
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

    private String getActivityType(int type) {
        String activityType = "";
        switch (type) {
            case DetectedActivity.IN_VEHICLE: activityType = "車"; break;
            case DetectedActivity.ON_BICYCLE: activityType = "自転車"; break;
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.WALKING: activityType = "歩き"; break;
            case DetectedActivity.RUNNING: activityType = "走り"; break;
            case DetectedActivity.STILL: activityType = "停止"; break;
            case DetectedActivity.TILTING: activityType = "起立"; break;
            case DetectedActivity.UNKNOWN: activityType = "不明"; break;
        }
        return activityType;
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
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.root, new RootMenuFragment(),"Root");
        ft.commit();

        receiver     = new PlayerBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("PLAYER_ACTION");
        registerReceiver(receiver, intentFilter);
        receiver.registerHandler(MPStateHandler);

        setContentView(R.layout.main);

        tvTitle    = (TextView)    findViewById(R.id.title);
        tvArtist   = (TextView)    findViewById(R.id.artist);
        ivAlbumArt = (ImageView)   findViewById(R.id.albumart);
        btBack     = (ImageButton) findViewById(R.id.btBack);
        btPlay     = (ImageButton) findViewById(R.id.btPlay);
        btSkip     = (ImageButton) findViewById(R.id.btSkip);

    }

    public void setNewFragment(FrgmType CallFragment){

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fTop = CallFragment;
        switch(CallFragment)
        {
            case fRoot      : ft.replace(R.id.root, new RootMenuFragment(), "Root"); break;
            case fSituation : ft.replace(R.id.root, new SituationDetailFragment(), "Situation"); break;
            case fAlbum     : ft.replace(R.id.root, new AlbumDetailFragment(), "Album"); break;
            case fArtist    : ft.replace(R.id.root, new ArtistDetailFragment(), "Artist"); break;
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void popBackFragment(){
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();
        Log.d(TAG, "popBackStack");
    }

    public void updatePanel(){
        ExTrack exTrack = getFocusedExTrack();
        Log.d(TAG, "updatePanel:" + exTrack.title);

        tvTitle.setText(exTrack.title);
        tvArtist.setText(exTrack.artist);
        ivAlbumArt.setImageResource(R.drawable.icon_album);
    }

    public AdapterView.OnItemClickListener  SituationClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListView lv = (ListView) parent;
            Main.this.focusSituation((Situation) lv.getItemAtPosition(position));
            Main.this.setNewFragment(FrgmType.fSituation);
        }
    };

    public AdapterView.OnItemLongClickListener SituationLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ListView lv = (ListView) parent;
            Situation situation = (Situation) lv.getItemAtPosition(position);
            Toast.makeText(Main.this, "LongClick:" + situation.name, Toast.LENGTH_LONG).show();
            return true;
        }
    };

    public AdapterView.OnItemClickListener  AlbumClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        focusAlbum( (Album)lv.getItemAtPosition(position) );
        setNewFragment(FrgmType.fAlbum);
    };

    public AdapterView.OnItemLongClickListener AlbumLongClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        Album item = (Album)lv.getItemAtPosition(position);
        Toast.makeText(Main.this, "LongClick:"+item.album, Toast.LENGTH_LONG).show();
        return true;
    };

    public AdapterView.OnItemClickListener  ArtistClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        focusArtist( (Artist)lv.getItemAtPosition(position) );
        setNewFragment(FrgmType.fArtist);
    };

    public AdapterView.OnItemLongClickListener ArtistLongClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        Artist item = (Artist)lv.getItemAtPosition(position);
        Toast.makeText(Main.this, "LongClick:"+item.artist, Toast.LENGTH_LONG).show();
        return true;
    };

    public AdapterView.OnItemClickListener  ExTrackClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        focusExTrack( (ExTrack) lv.getItemAtPosition(position) );

        Intent intent = new Intent(getApplication(), YoutubePlayScreen.class);
        startActivity(intent);
    };

    public AdapterView.OnItemLongClickListener ExTrackLongClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        ExTrack item = (ExTrack) lv.getItemAtPosition(position);
        Toast.makeText(Main.this, "LongClick:"+item.title, Toast.LENGTH_LONG).show();
        return true;
    };

    public AdapterView.OnItemClickListener  internalExTrackClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        focusExTrack( (ExTrack) lv.getItemAtPosition(position) );

        Intent intent = new Intent(getApplication(), PlayScreen.class);
        intent.putExtra("from", "fromTrackList");
        intent.putExtra("state", "Stop");
        startActivity(intent);
    };

    public AdapterView.OnItemLongClickListener internalExTrackLongClickListener = (parent, view, position, id) -> {
        ListView lv = (ListView)parent;
        ExTrack item = (ExTrack) lv.getItemAtPosition(position);
        Toast.makeText(Main.this, "LongClick:"+item.title, Toast.LENGTH_LONG).show();
        return true;
    };

    public void onPlayPanelClick(View view) {
        Intent intent = new Intent(getApplication(), PlayScreen.class);
        intent.putExtra("from", "fromPlayPanel");
        intent.putExtra("state", nowState);
        startActivity(intent);

    }

    public void onBadButtonClick(View view) {
        getWeatherSnapshot();
        getPlacesSnapshot();
        getDetectedActivitySnapshot();
    }

    public void onPlayButtonClick(View view) {
        Toast.makeText(Main.this, "Click:PlayButton", Toast.LENGTH_SHORT).show();
    }

    public void onGoodButtonClick(View view) {
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        List<String> situations = calToSituations(cal);

        Toast.makeText(Main.this, calToStr(cal)  + "\n" + situations.toString(), Toast.LENGTH_SHORT).show();
    }

    // サービスから値を受け取ったら動かしたい内容を書く
    private Handler MPStateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            String message = bundle.getString("message");

            Log.d(TAG, "get massage from MPService: " + message);
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
