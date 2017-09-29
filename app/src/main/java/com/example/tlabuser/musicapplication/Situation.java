package com.example.tlabuser.musicapplication;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Situation class like album
 */

public class Situation{

    public String situation;
    public int    tracks;

    public Situation(String situation, int tracks){
        this.situation = situation;
        this.tracks    = tracks;
    }

    public static List<Situation> getItems(JSONArray jsonArray) {
        List situations = new ArrayList();

        try{
            List<String> tags = new ArrayList<String>();
            for(int i=0; i<jsonArray.length(); i++){
                String tag = jsonArray.getJSONObject(i).getJSONObject("tag").getString("value").replace("http://music.metadata.database.tag/", "");
                tags.add(tag);
            }

            for (int i=0; i<tags.size(); i++){
                // tracks = 0
                situations.add(new Situation(tags.get(i), 0));
            }

        } catch (JSONException e) {
                Log.d("onLoadFinished","JSONのパースに失敗しました。 JSONException=" + e);
            }

        return situations;
    }



}