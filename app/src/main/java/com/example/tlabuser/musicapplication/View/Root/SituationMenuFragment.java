package com.example.tlabuser.musicapplication.View.Root;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.example.tlabuser.musicapplication.JsonUtil;
import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.Model.Situation;
import com.example.tlabuser.musicapplication.R;
import com.example.tlabuser.musicapplication.SQLOpenHelper;
import com.example.tlabuser.musicapplication.Urls;
import com.example.tlabuser.musicapplication.View.Situation.SituationDetailFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.example.tlabuser.musicapplication.Main.setNowSituationListener;

public class SituationMenuFragment extends Fragment{

    private final String TAG = "SituationMenuFragment";

    private Main mainActivity;
    private SQLiteDatabase db;

    private List<String> nowSituations;
    private List<Situation> situations;

    private RecyclerView rvSituations, rvRecommendedSituations;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (Main)getActivity();
        db = new SQLOpenHelper(mainActivity).getReadableDatabase();

        setNowSituationListener(this::setRecommendedSituations);
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

        if (situations.isEmpty()) {
            Log.d(TAG, "situations is empty");
            Toast.makeText(mainActivity, "SituationListを取得しています。\nこれには30秒ほどかかる場合があります。", Toast.LENGTH_LONG).show();

            // Get JSON from server
            Single.create((SingleOnSubscribe<JSONObject>) emitter -> emitter.onSuccess(requestJson()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onGetJson);
        } else {
            initializeRecyclerView();
        }

        nowSituations = mainActivity.getNowSituations();
        if (nowSituations != null) {
            setRecommendedSituations(nowSituations);
        }

        return v;
    }

    @Nullable
    private JSONObject requestJson() {
        String urlStr = Urls.SELECT_SITUATIONS;
        try {
            urlStr = URLEncoder.encode(urlStr, "UTF-8");
        } catch (UnsupportedEncodingException e){
            Log.d(TAG,"URLエンコードに失敗しました。 UnsupportedEncodingException=" + e);
        }
        urlStr = Urls.HEAD + urlStr + Urls.TAIL;

        return JsonUtil.getJson(urlStr);
    }

    private void onGetJson(JSONObject json) {
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

        } else {
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
    }

    private void setRecommendedSituations(List<String> nowSituations) {
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
}
