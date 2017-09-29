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

    public SituationTrack(String situaion, String artist, String title, int weight){
        this.situation = situaion;
        this.artist = artist;
        this.title  = title;
        this.weight = weight;
    }

    public static List<SituationTrack> getItems(JSONArray jsonArray) {
        List situationTracks = new ArrayList();

        try{
            String situation;
            List<String>  artists = new ArrayList<String>();
            List<String>  titles  = new ArrayList<String>();
            List<Integer> weights = new ArrayList<Integer>();

            situation = jsonArray.getJSONObject(0).getJSONObject("tag").getString("value");
            for(int i=0; i<jsonArray.length(); i++){
                artists.add(jsonArray.getJSONObject(i).getJSONObject("artist").getString("value"));
                titles.add( jsonArray.getJSONObject(i).getJSONObject("title" ).getString("value"));
                weights.add(jsonArray.getJSONObject(i).getJSONObject("weight").getInt("value"));
            }

            for (int i=0; i<artists.size(); i++){
                situationTracks.add(new SituationTrack(situation, artists.get(i), titles.get(i), weights.get(i)));
            }

        } catch (JSONException e) {
                Log.d("onLoadFinished","JSONのパースに失敗しました。 JSONException=" + e);
            }

        return situationTracks;
    }

}