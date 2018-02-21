package com.example.tlabuser.musicapplication.View.Situation;


import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tlabuser.musicapplication.JsonUtil;
import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.Model.ExTrack;
import com.example.tlabuser.musicapplication.Model.Situation;
import com.example.tlabuser.musicapplication.R;
import com.example.tlabuser.musicapplication.SQLOpenHelper;
import com.example.tlabuser.musicapplication.Urls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class SituationDetailFragment extends Fragment {

    public static final String TAG = "SituationDetailFragment";

    private final int INTERVAL = 50;

    private Main mainActivity;
    private SQLiteDatabase db;

    private boolean isLoading = false;
    private boolean loadCompleted = false;
    private int offset;
    private Situation situation;
    private List<ExTrack> exTracks, internalExTracks, padding, internalPadding;
    private ListExTrackSituationAdapter exTrackAdapter, internalExTrackAdapter;

    private CheckBox checkBox;
    private TextView tvSituationName, tvTracks;
    private ListView lvExTracks,lvInternalExTracks;
    private View footer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (Main)getActivity();

        db = new SQLOpenHelper(mainActivity).getReadableDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v =inflater.inflate(R.layout.fragment_situation_detail, container, false);

        checkBox = (CheckBox) v.findViewById(R.id.checkbox);
        tvSituationName = (TextView) v.findViewById(R.id.situation);
        tvTracks = (TextView) v.findViewById(R.id.tracks);
        lvExTracks = (ListView) v.findViewById(R.id.lv_extracks);
        lvInternalExTracks = (ListView) v.findViewById(R.id.lv_internal_extracks);

        footer = inflater.inflate(R.layout.listview_footer, null);

        checkBox.setOnClickListener(CheckboxClickListener);

        situation = mainActivity.getFocusedSituation();
        tvSituationName.setText(situation.name);

        padding = new ArrayList<>(); 
        internalPadding = new ArrayList<>();
        exTracks = ExTrack.getExTracksBySituation(db, situation.name);
        internalExTracks = ExTrack.getInternalExTracks(exTracks);
        offset = exTracks.size();

        if (exTracks.isEmpty()) {
            Log.d(TAG, "exTracks is empty");
            Toast.makeText(mainActivity, "SituationTrackListを取得しています。\nこれには30秒ほどかかる場合があります。", Toast.LENGTH_LONG).show();

            // Get JSON from server
            Single.create((SingleOnSubscribe<JSONObject>) emitter -> emitter.onSuccess(requestJson()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onGetJson);
        } else {
            initializeListView();
            padding.addAll(exTracks);
        }

        return v;
    }

    @Nullable
    private JSONObject requestJson() {
        String urlStr = String.format(Urls.Fuseki.SELECT_TRACKS, situation.name, INTERVAL, offset);
        try {
            urlStr = URLEncoder.encode(urlStr, "UTF-8");
        } catch (UnsupportedEncodingException e){
            Log.d(TAG,"URLエンコードに失敗しました。 UnsupportedEncodingException=" + e);
        }
        urlStr = Urls.Fuseki.HEAD + urlStr + Urls.Fuseki.TAIL;

        return JsonUtil.getJson(urlStr);
    }

    private void onGetJson(JSONObject json) {
        if (json != null) {
            try {
                JSONArray jsonArray = json.getJSONObject("results").getJSONArray("bindings");
                if (jsonArray.length() != 0) {
                    Log.d(TAG, jsonArray.toString());

                    padding = ExTrack.getExTracksFromJson(db, mainActivity, jsonArray);
                    exTracks.addAll(padding);

                    internalPadding = ExTrack.getInternalExTracks(padding);
                    internalExTracks.addAll(internalPadding);

                    situation.setTracks(db, exTracks.size());

                    initializeListView();
                    offset += INTERVAL;

                    if (jsonArray.length() < INTERVAL) {
                        Log.d(TAG,"Finish loading JSONArray !");
                        loadCompleted = true;
                        lvExTracks.removeFooterView(footer);
                    }
                }

            } catch (JSONException e) {
                Log.d(TAG,"JSONのパースに失敗しました。 JSONException=" + e);
            }

        } else {
            Log.d(TAG, "JSONObject is null !");
        }
    }

    private void initializeListView() {
        exTrackAdapter = new ListExTrackSituationAdapter(mainActivity, situation, exTracks);
        lvExTracks.setAdapter(exTrackAdapter);
        lvExTracks.setOnItemClickListener(mainActivity.ExTrackClickListener);
        lvExTracks.setOnItemLongClickListener(mainActivity.ExTrackLongClickListener);
        lvExTracks.addFooterView(footer);
        lvExTracks.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!isLoading && totalItemCount == firstVisibleItem + visibleItemCount) {
                    additionalReading();
                }
            }
        });

        internalExTrackAdapter = new ListExTrackSituationAdapter(mainActivity, situation, internalExTracks);
        lvInternalExTracks.setAdapter(internalExTrackAdapter);
        lvInternalExTracks.setOnItemClickListener(mainActivity.internalExTrackClickListener);
        lvInternalExTracks.setOnItemLongClickListener(mainActivity.internalExTrackLongClickListener);

        tvTracks.setText(String.valueOf(exTrackAdapter.getCount())+" tracks");
    }

    private void additionalReading() {
        if (!loadCompleted) {
            isLoading = true;

            Single.create((SingleOnSubscribe<JSONObject>) emitter -> emitter.onSuccess(requestJson()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onGetAdditionalJson);
        }
    }

    private void onGetAdditionalJson(JSONObject json) {
        if (json != null) {
            try {
                JSONArray jsonArray = json.getJSONObject("results").getJSONArray("bindings");
                if (jsonArray.length() != 0) {
                    Log.d(TAG, jsonArray.toString());

                    padding = ExTrack.getExTracksFromJson(db, mainActivity, jsonArray);
                    exTracks.addAll(padding);
                    exTrackAdapter.notifyDataSetChanged();

                    internalPadding = ExTrack.getInternalExTracks(padding);
                    internalExTracks.addAll(internalPadding);
                    internalExTrackAdapter.notifyDataSetChanged();

                    situation.setTracks(db, exTrackAdapter.getCount());
                    tvTracks.setText(String.valueOf(exTrackAdapter.getCount())+" tracks");

                    offset += INTERVAL;

                    if (jsonArray.length() < INTERVAL) {
                        Log.d(TAG,"Finish loading JSONArray !");
                        loadCompleted = true;
                        lvExTracks.removeFooterView(footer);
                    }

                } else {
                    Log.d(TAG,"JSONArray is empty !");
                    loadCompleted = true;
                    lvExTracks.removeFooterView(footer);
                }

            } catch (JSONException e) {
                Log.d(TAG,"JSONのパースに失敗しました。 JSONException=" + e);
            }

        } else {
            Log.d(TAG, "JSONObject is null !");
        }

        isLoading = false;
    }

    public View.OnClickListener CheckboxClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final boolean checked = ((CheckBox) v).isChecked();
            if (checked) {
                // チェックボックスがチェックされる
                if (internalExTracks.size() == 0){
                    Toast.makeText(mainActivity, "端末内に該当楽曲がありません", Toast.LENGTH_LONG).show();
                }
                lvExTracks.setVisibility(View.INVISIBLE);
                lvInternalExTracks.setVisibility(View.VISIBLE);
                tvTracks.setText(String.valueOf(internalExTrackAdapter.getCount())+" tracks");
            } else {
                // チェックボックスのチェックが外される
                lvInternalExTracks.setVisibility(View.INVISIBLE);
                lvExTracks.setVisibility(View.VISIBLE);
                tvTracks.setText(String.valueOf(exTrackAdapter.getCount())+" tracks");
            }
        }

    };

}
