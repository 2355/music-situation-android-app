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
    private static final String TAG = "SituationDetailFragment";

    private Main mainActivity;
    private SQLOpenHelper sqlOpenHelper;
    private SQLiteDatabase db;

    private Situation situation;
    private List<ExTrack> exTracks, internalExTracks;
    private ListExTrackSituationAdapter listExTrackAdapter, listInternalExTrackAdapter;
    private JSONArray jsonArray = new JSONArray();

    private CheckBox checkBox;
    private TextView tvSituationName;
    private TextView tvTracks;
    private ListView lvTrackList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (Main)getActivity();

        sqlOpenHelper = new SQLOpenHelper(mainActivity);
        db = sqlOpenHelper.getReadableDatabase();

        // 選択されたSituationを取得
        situation = mainActivity.getFocusedSituation();

        exTracks = ExTrack.getExTracksBySituation(db, situation.name);
        listExTrackAdapter = new ListExTrackSituationAdapter(mainActivity, situation, exTracks);

        if(exTracks.isEmpty()) {
            // JSONの取得
            getLoaderManager().restartLoader(2, null, this);
            Toast.makeText(mainActivity, "SituationTrackListを取得しています。\nこれには30秒ほどかかる場合があります。", Toast.LENGTH_LONG).show();
        } else {
            internalExTracks = ExTrack.getInternalExTracks(exTracks);
            listInternalExTrackAdapter = new ListExTrackSituationAdapter(mainActivity, situation, internalExTracks);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View partView =inflater.inflate(R.layout.part_situation, container, false);

        checkBox        = (CheckBox) partView.findViewById(R.id.checkbox);
        tvSituationName = (TextView) partView.findViewById(R.id.situation);
        tvTracks        = (TextView) partView.findViewById(R.id.tracks);
        lvTrackList     = (ListView) partView.findViewById(R.id.track_list);

        checkBox.setOnClickListener(CheckboxClickListener);
        tvSituationName.setText(situation.name);

        lvTrackList.setAdapter(listExTrackAdapter);
        lvTrackList.setOnItemClickListener(mainActivity.ExTrackClickListener);
        lvTrackList.setOnItemLongClickListener(mainActivity.ExTrackLongClickListener);
        tvTracks.setText(String.valueOf(listExTrackAdapter.getTracks())+" tracks");

        return partView;
    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        String urlStr = String.format(Urls.SELECT_TRACKS, situation.name);
        try {
            urlStr = URLEncoder.encode(urlStr, "UTF-8");
        }catch (UnsupportedEncodingException e){
            Log.d(TAG,"URLエンコードに失敗しました。 UnsupportedEncodingException=" + e);
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

                    exTracks = ExTrack.getExTracksFromJson(db, mainActivity, jsonArray);
                    situation.setTracks(db, exTracks.size());

                    listExTrackAdapter = new ListExTrackSituationAdapter(mainActivity, situation, exTracks);
                    lvTrackList.setAdapter(listExTrackAdapter);
                    lvTrackList.setOnItemClickListener(mainActivity.ExTrackClickListener);
                    lvTrackList.setOnItemLongClickListener(mainActivity.ExTrackLongClickListener);
                    tvTracks.setText(String.valueOf(situation.tracks)+" tracks");

                    // 端末内のみリスト
                    internalExTracks = ExTrack.getInternalExTracks(exTracks);
                    listInternalExTrackAdapter = new ListExTrackSituationAdapter(mainActivity, situation, internalExTracks);

                }else{
                    Log.d(TAG, "No Tracks!");
                }

            } catch (JSONException e) {
                Log.d(TAG,"JSONのパースに失敗しました。 JSONException=" + e);
            }

        }else{
            Log.d(TAG, "onLoadFinished error!");
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
                    Toast.makeText(mainActivity, "端末内に該当楽曲がありません", Toast.LENGTH_LONG).show();
                }
                lvTrackList.setAdapter(listInternalExTrackAdapter);
                lvTrackList.setOnItemClickListener(mainActivity.internalExTrackClickListener);
                lvTrackList.setOnItemLongClickListener(mainActivity.internalExTrackLongClickListener);
                tvTracks.setText(String.valueOf(listInternalExTrackAdapter.getTracks())+" tracks");
            } else {
                // チェックボックスのチェックが外される
                lvTrackList.setAdapter(listExTrackAdapter);
                lvTrackList.setOnItemClickListener(mainActivity.ExTrackClickListener);
                lvTrackList.setOnItemLongClickListener(mainActivity.ExTrackLongClickListener);
                tvTracks.setText(String.valueOf(listExTrackAdapter.getTracks())+" tracks");
            }
        }

    };

}
