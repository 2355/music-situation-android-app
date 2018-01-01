package com.example.tlabuser.musicapplication.View.Situation;


import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tlabuser.musicapplication.JsonLoader;
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
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SituationDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<JSONObject>{

    private Main activity;
    private SQLOpenHelper sqlOpenHelper;
    private SQLiteDatabase db;

    private Situation situation_item;
    private List<ExTrack> exTracks, internalExTracks;
    private ListExTrackSituationAdapter listExTrackAdapter, listInternalExTrackAdapter;
    private JSONArray jsonArray = new JSONArray();

    private CheckBox checkBox;
    private TextView tv_situation_name;
    private TextView tv_tracks;
    private ListView lv_track_list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (Main)getActivity();

        sqlOpenHelper = new SQLOpenHelper(activity);
        db = sqlOpenHelper.getReadableDatabase();

        // 選択されたSituationを取得
        situation_item = activity.getFocusedSituation();

        exTracks = ExTrack.getExTracksBySituation(db, situation_item.name);

        if(exTracks.isEmpty()) {
            // JSONの取得
            getLoaderManager().restartLoader(2, null, this);
            Toast.makeText(activity, "SituationTrackListを取得しています。\nこれには30秒ほどかかる場合があります。", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View partView =inflater.inflate(R.layout.part_situation, container, false);

        checkBox          = (CheckBox) partView.findViewById(R.id.checkbox);
        tv_situation_name = (TextView) partView.findViewById(R.id.situation);
        tv_tracks         = (TextView) partView.findViewById(R.id.tracks);
        lv_track_list     = (ListView) partView.findViewById(R.id.track_list);

        checkBox.setOnClickListener(CheckboxClickListener);
        tv_situation_name.setText(situation_item.name);

        listExTrackAdapter = new ListExTrackSituationAdapter(activity, exTracks);
        lv_track_list.setAdapter(listExTrackAdapter);
        lv_track_list.setOnItemClickListener(activity.ExTrackClickListener);
        lv_track_list.setOnItemLongClickListener(activity.ExTrackLongClickListener);
        tv_tracks.setText(String.valueOf(listExTrackAdapter.getTracks())+"tracks");

        // 端末内のみリスト
        internalExTracks = ExTrack.getInternalExTracksBySituation(db, situation_item.name);
        listInternalExTrackAdapter = new ListExTrackSituationAdapter(activity, internalExTracks);

        return partView;
    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        String urlStr = String.format(Urls.SELECT_TRACKS, situation_item.name);
        try {
            urlStr = URLEncoder.encode(urlStr, "UTF-8");
        }catch (UnsupportedEncodingException e){
            Log.d("SituationDetailFragment","URLエンコードに失敗しました。 UnsupportedEncodingException=" + e);
        }
        urlStr = Urls.HEAD + urlStr + Urls.TAIL;

        JsonLoader jsonLoader = new JsonLoader(getActivity(), urlStr);
        jsonLoader.forceLoad();
        return  jsonLoader;
    }

    @Override
    public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
        if (data != null) {
            try {
                jsonArray = data.getJSONObject("results").getJSONArray("bindings");
                if (jsonArray.getJSONObject(0).has("artist")) {

                    exTracks = ExTrack.getExTracksFromJson(activity, jsonArray);
                    ExTrack.insertRows(db, exTracks);

                    listExTrackAdapter = new ListExTrackSituationAdapter(activity, exTracks);
                    lv_track_list.setAdapter(listExTrackAdapter);
                    lv_track_list.setOnItemClickListener(activity.ExTrackClickListener);
                    lv_track_list.setOnItemLongClickListener(activity.ExTrackLongClickListener);
                    tv_tracks.setText(String.valueOf(listExTrackAdapter.getTracks())+"tracks");

                    // 端末内のみリスト
                    internalExTracks = ExTrack.getInternalExTracksBySituation(db, situation_item.name);
                    listInternalExTrackAdapter = new ListExTrackSituationAdapter(activity, internalExTracks);

                }else{
                    Log.d("SituationDetailFragment", "No Tracks!");
                }

            } catch (JSONException e) {
                Log.d("SituationDetailFragment","JSONのパースに失敗しました。 JSONException=" + e);
            }

        }else{
            Log.d("SituationDetailFragment", "onLoadFinished error!");
        }
    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {
        // 処理なし
    }

    public View.OnClickListener CheckboxClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            final boolean checked = ((CheckBox) v).isChecked();
            if (checked) {
                // チェックボックスがチェックされる
                if (internalExTracks.size() == 0){
                    Toast.makeText(activity, "端末内に該当楽曲がありません", Toast.LENGTH_LONG).show();
                }
                lv_track_list.setAdapter(listInternalExTrackAdapter);
                lv_track_list.setOnItemClickListener(activity.internalExTrackClickListener);
                lv_track_list.setOnItemLongClickListener(activity.internalExTrackLongClickListener);
                tv_tracks.setText(String.valueOf(listInternalExTrackAdapter.getTracks())+"tracks");
            } else {
                // チェックボックスのチェックが外される
                lv_track_list.setAdapter(listExTrackAdapter);
                lv_track_list.setOnItemClickListener(activity.ExTrackClickListener);
                lv_track_list.setOnItemLongClickListener(activity.ExTrackLongClickListener);
                tv_tracks.setText(String.valueOf(listExTrackAdapter.getTracks())+"tracks");
            }
        }

    };

}
