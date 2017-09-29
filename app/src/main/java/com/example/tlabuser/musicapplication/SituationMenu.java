package com.example.tlabuser.musicapplication;


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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SituationMenu extends Fragment implements LoaderManager.LoaderCallbacks<JSONObject>{

    private static Situation situation_item;

    Main activity;

    TextView tv_tracks;
    ListView lv_track_list;

    JSONArray            jsonArray = new JSONArray();
    List<SituationTrack> situationTrackList;
    ListTrackAdapter3    situationTrackListAdapter;
    List<Track>          trackList;
    ListTrackAdapter     trackListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View partView =inflater.inflate(R.layout.part_situation, container, false);

        activity = (Main)getActivity();
        // 選択されたSituationを取得
        situation_item = activity.getFocusedSituaion();

        // JSONの取得
        getLoaderManager().restartLoader(2, null, this);
        Toast.makeText(activity, "SituationTrackListを取得しています。\nしばらくお待ちください。", Toast.LENGTH_LONG).show();

        CheckBox checkBox          = (CheckBox) partView.findViewById(R.id.checkbox);
        TextView tv_situation_name = (TextView) partView.findViewById(R.id.situation);
                 tv_tracks         = (TextView) partView.findViewById(R.id.tracks);
                 lv_track_list     = (ListView) partView.findViewById(R.id.track_list);

        checkBox.setOnClickListener(CheckboxClickListener);
        tv_situation_name.setText(situation_item.situation);
        tv_tracks.setText(String.valueOf(situation_item.tracks)+"tracks");

        return partView;
    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        String head = "https://musicmetadata.herokuapp.com/ds/query?query=";
        String tail = "&output=json&stylesheet=/xml-to-html.xsl";
        String urlStr = String.format(
                        "prefix dc: <http://purl.org/dc/elements/1.1/> \n" +
                        "prefix foaf: <http://xmlns.com/foaf/0.1/> \n" +
                        "prefix situation: <http://music.metadata.database.situation/> \n" +
                        "prefix tag: <http://music.metadata.database.tag/>\n" +
                        "\n" +
                        "SELECT ?artist ?title ?tag ?weight\n" +
                        "WHERE {\n" +
                        "  ?s foaf:maker ?artist;\n" +
                        "     dc:title ?title;\n" +
                        "     situation:blank ?b.\n" +
                        "  \n" +
                        "  ?b situation:tag ?tag;\n" +
                        "      situation:weight ?weight.\n" +
                        "\n" +
                        "FILTER( ?tag = tag:%s ) \n" +
                        "}\n" +
                        "order by desc(?weight)",
                    situation_item.situation);

        try {
            urlStr = URLEncoder.encode(urlStr, "UTF-8");
        }catch (UnsupportedEncodingException e){
            Log.d("onCreateLoader","URLエンコードに失敗しました。 UnsupportedEncodingException=" + e);
        }
        urlStr = head + urlStr + tail;

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
                    situationTrackList        = SituationTrack.getItems(jsonArray);
                    situationTrackListAdapter = new ListTrackAdapter3(activity, situationTrackList);

                    lv_track_list.setAdapter(situationTrackListAdapter);
                    tv_tracks.setText(String.valueOf(situationTrackListAdapter.getTracks())+"tracks");
                    lv_track_list.setOnItemClickListener(activity.SituationTrackClickListener);
                    lv_track_list.setOnItemLongClickListener(activity.SituationTrackLongClickListener);

                    // 端末内のみリスト
                    trackList        = Track.getItemsBySituationTrack(activity, situationTrackList);
                    trackListAdapter = new ListTrackAdapter(activity, trackList);

                }else{
                    Log.d("getTracks", "No Tracks!");
                }

            } catch (JSONException e) {
                Log.d("onLoadFinished","JSONのパースに失敗しました。 JSONException=" + e);
            }

        }else{
            Log.d("onLoadFinished", "onLoadFinished error!");
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
                if (trackList.size() == 0){
                    Toast.makeText(activity, "端末内に該当楽曲がありません", Toast.LENGTH_LONG).show();
                }
                lv_track_list.setAdapter(trackListAdapter);
                tv_tracks.setText(String.valueOf(trackListAdapter.getTracks())+"tracks");
                lv_track_list.setOnItemClickListener(activity.TrackClickListener);
                lv_track_list.setOnItemLongClickListener(activity.TrackLongClickListener);
            } else {
                // チェックボックスのチェックが外される
                lv_track_list.setAdapter(situationTrackListAdapter);
                tv_tracks.setText(String.valueOf(situationTrackListAdapter.getTracks())+"tracks");
                lv_track_list.setOnItemClickListener(activity.SituationTrackClickListener);
                lv_track_list.setOnItemLongClickListener(activity.SituationTrackLongClickListener);
            }
        }

    };

}
