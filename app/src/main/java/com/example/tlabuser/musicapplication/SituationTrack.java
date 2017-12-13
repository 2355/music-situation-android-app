package com.example.tlabuser.musicapplication;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * class of SituationTrack like Track
 */

public class SituationTrack {

    public String situation;
    public String artist;
    public String title;
    public int    weight;

    public SituationTrack(String situation, String artist, String title, int weight){
        this.situation = situation;
        this.artist = artist;
        this.title  = title;
        this.weight = weight;
    }

    public static List<SituationTrack> getItems(JSONArray jsonArray) {
        List situationTracks = new ArrayList();

        try{
            String situation = jsonArray.getJSONObject(0).getJSONObject("tag").getString("value");
            String artist;
            String title;
            Integer weight;

            for(int i=0; i<jsonArray.length(); i++){
                artist = jsonArray.getJSONObject(i).getJSONObject("artist").getString("value");
                title  = jsonArray.getJSONObject(i).getJSONObject("title" ).getString("value");
                weight = jsonArray.getJSONObject(i).getJSONObject("weight").getInt("value");
                situationTracks.add(new SituationTrack(situation, artist, title, weight));
            }

        } catch (JSONException e) {
                Log.d("onLoadFinished","JSONのパースに失敗しました。 JSONException=" + e);
            }

        return situationTracks;
    }

}