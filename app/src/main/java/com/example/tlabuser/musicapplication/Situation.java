package com.example.tlabuser.musicapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.example.tlabuser.musicapplication.SQLOpenHelper.SITUATION_TABLE;

/**
 * Situation class like album
 */

public class Situation{

    public String name;
    public int    tracks;

    public Situation(){
        name   = "";
        tracks = 0;
    }
    
    /***********************************************************************************************
     * to SQL
     **********************************************************************************************/

    public static List<Situation> getItems(JSONArray jsonArray) {
        List<Situation> situations = new ArrayList<Situation>();

        try{
            for(int i=0; i<jsonArray.length(); i++){
                Situation situation = new Situation();
                situation.name   = jsonArray.getJSONObject(i).getJSONObject("tag").getString("value").replace("http://music.metadata.database.tag/", "");
                situation.tracks = 0;
                situations.add(situation);
            }


        } catch (JSONException e) {
            Log.d("onLoadFinished","JSONのパースに失敗しました。 JSONException=" + e);
        }

        return situations;
    }

    // convert Situation to ContentValues
    private static ContentValues setValues(Situation situation){
        ContentValues values = new ContentValues();

        values.put("name",   situation.name);
        values.put("tracks", situation.tracks);

        return values;
    }

    // insert Situations to SQL
    public static void insertRows(SQLiteDatabase db, List<Situation> situations){
        for (int i=0; i<situations.size(); i++){
            Situation situation = situations.get(i);

            ContentValues values = setValues(situation);
            long id = db.insert(SITUATION_TABLE, null, values);
            if (id < 0) {
                //error handling
            }
        }
    }

    /***********************************************************************************************
     * from SQL
     **********************************************************************************************/

    public static List<Situation> getSituationsFromSQL(SQLiteDatabase db){
        String orderBy = "weight DESC";

        // query(tableName, selectColumns, whereClause, whereArgs, groupBy, having, orderBy, limit);
        Cursor cursor = db.query(SITUATION_TABLE, null, null, null, null, null, orderBy, null);

        return setSituationsFromSQL(cursor);
    }

    private static List<Situation> setSituationsFromSQL(Cursor cursor){
        List<Situation> situations = new ArrayList<Situation>();

        boolean move = cursor.moveToFirst();
        while (move) {
            Situation situation = new Situation();
            situation.name   = cursor.getString(cursor.getColumnIndex("name"));
            situation.tracks = cursor.getInt(cursor.getColumnIndex("tracks"));
            situations.add(situation);
            move = cursor.moveToNext();
        }
        cursor.close();

        return situations;
    }
}