package com.example.tlabuser.musicapplication.View.Root;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.Model.Situation;
import com.example.tlabuser.musicapplication.R;
import com.example.tlabuser.musicapplication.SQLOpenHelper;
import com.example.tlabuser.musicapplication.Urls;
import com.example.tlabuser.musicapplication.View.Situation.SituationDetailFragment;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.example.tlabuser.musicapplication.CalendarUtil.calToSituations;

public class SituationMenuFragment extends Fragment{

    private final String TAG = "SituationMenuFragment";

    private Main mainActivity;
    private SQLiteDatabase db;

    private List<String> nowSituations;
    private List<Situation> situations;

    private RecyclerView rvRecommendedSituations, rvSituations;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (Main)getActivity();

        db = new SQLOpenHelper(mainActivity).getReadableDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_situations_menu,container,false);

        rvSituations = (RecyclerView) v.findViewById(R.id.rv_situations);
        rvSituations.setLayoutManager(new GridLayoutManager(mainActivity, 2));
        rvSituations.setNestedScrollingEnabled(false);

        rvRecommendedSituations = (RecyclerView) v.findViewById(R.id.rv_recommended_situations);
        rvRecommendedSituations.setLayoutManager(new GridLayoutManager(mainActivity, 2));
        rvRecommendedSituations.setNestedScrollingEnabled(false);


        situations = Situation.getAllSituations(db);

        if(situations.isEmpty()) {
            Log.d(TAG, "situations is empty");
            Toast.makeText(mainActivity, "SituationListを取得しています。\nこれには30秒ほどかかる場合があります。", Toast.LENGTH_LONG).show();

            // Get JSON from server
            Single.create((SingleOnSubscribe<JSONObject>) emitter -> emitter.onSuccess(SituationMenuFragment.this.networkRequest()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(SituationMenuFragment.this::render);
        } else {
            initializeRecyclerView();
        }

        return v;
    }

    @Nullable
    private JSONObject networkRequest() {
        String urlStr = Urls.SELECT_SITUATIONS;
        try {
            urlStr = URLEncoder.encode(urlStr, "UTF-8");
        }catch (UnsupportedEncodingException e){
            Log.d(TAG,"URLエンコードに失敗しました。 UnsupportedEncodingException=" + e);
        }
        urlStr = Urls.HEAD + urlStr + Urls.TAIL;


        HttpURLConnection connection = null;

        try{
            URL url = new URL(urlStr);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
        }
        catch (MalformedURLException exception){
            // 処理なし
        }
        catch (IOException exception){
            // 処理なし
        }

        try {
            BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                if (length > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }

            JSONObject json = new JSONObject(new String(outputStream.toByteArray()));
            return json;
        }
        catch (IOException exception){
            // 処理なし
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void render(JSONObject json) {
        if (json != null) {
            try {
                JSONArray jsonArray = json.getJSONObject("results").getJSONArray("bindings");
                if (jsonArray.getJSONObject(0).has("tag") && situations.isEmpty()) {
                    situations = Situation.getSituationsFromJson(db, jsonArray);
                    initializeRecyclerView();
                }

            } catch (JSONException e) {
                Log.d(TAG,"JSONのパースに失敗しました。 JSONException=" + e);
            }

        }else{
            Log.d(TAG, "JSONObject is null");
        }
    }

    private void initializeRecyclerView() {
        SituationsRecyclerAdapter adapter;

        adapter = new SituationsRecyclerAdapter(mainActivity, situations);
        adapter.setItemClickedListener(situation -> {
            mainActivity.focusSituation(situation);
            FragmentManager fm = mainActivity.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.root, new SituationDetailFragment()).addToBackStack(null).commit();
        });
        rvSituations.setAdapter(adapter);


        // Get nowSituations and Set recommendedSituations
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        nowSituations = calToSituations(cal);
        Log.d(TAG, "Time " + String.valueOf(nowSituations));

        getWeather();
        getPlaces();
        getDetectedActivity();
    }

    private void setRecommendedSituations() {
        List<Situation> recommendedSituations = Situation.getRecommendedSituations(db, nowSituations);

        SituationsRecyclerAdapter adapter;

        adapter = new SituationsRecyclerAdapter(mainActivity, recommendedSituations);
        adapter.setItemClickedListener(situation -> {
            mainActivity.focusSituation(situation);
            FragmentManager fm = mainActivity.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.root, new SituationDetailFragment()).addToBackStack(null).commit();
        });
        rvRecommendedSituations.setAdapter(adapter);
    }

    private void getWeather() {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.getSnapshotClient(mainActivity).getWeather()
                    .addOnSuccessListener(weatherResponse -> {
                        Weather weather = weatherResponse.getWeather();

                        nowSituations.addAll(getWeatherConditions(weather));
                        Log.d(TAG, "Weathers " + String.valueOf(nowSituations));
                        setRecommendedSituations();
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
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.getSnapshotClient(mainActivity).getPlaces()
                    .addOnSuccessListener(placesResponse -> {
                        List<PlaceLikelihood> pls = placesResponse.getPlaceLikelihoods();
                        if (pls != null) {
                            for (PlaceLikelihood pl : pls){
                                if (pl.getLikelihood() == 0.0) break;

                                List<String> types = getPlaceTypes(pl.getPlace().getPlaceTypes());
                                nowSituations.addAll(types);
                            }
                            setRecommendedSituations();
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
        Awareness.getSnapshotClient(mainActivity).getDetectedActivity()
                .addOnSuccessListener(dar -> {
                    ActivityRecognitionResult arr = dar.getActivityRecognitionResult();
                    List<DetectedActivity> das = arr.getProbableActivities();
                    for (DetectedActivity da : das) {
                        if (da.getConfidence() < 10) break;
                        String type = getDetectedActivityType(da.getType());
                        if (type != null) nowSituations.add(type);
                    }
                    Log.d(TAG, "Activities " + String.valueOf(nowSituations));
                    setRecommendedSituations();
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
}
