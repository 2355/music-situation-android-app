package com.example.tlabuser.musicapplication.Model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.tlabuser.musicapplication.CalendarUtil.calToStr;
import static com.example.tlabuser.musicapplication.CalendarUtil.strToCal;
import static com.example.tlabuser.musicapplication.SQLOpenHelper.EXTRACK_SITUATION_TABLE;
import static com.example.tlabuser.musicapplication.SQLOpenHelper.EXTRACK_TABLE;

/**
 * Extend Track class
 */

public class ExTrack {
    public static final String TAG = "ExTrack";

    public long     id;          // コンテントプロバイダに登録されたID
    public String   path;        // 実データのPATH
    public String   title;       // トラックタイトル
    public String   album;       // アルバムタイトル
    public long     albumId;     // アルバムのID
    public String   artist;      // アーティスト名
    public long     artistId;    // アーティストのID
    public long     duration;    // 再生時間(ミリ秒)
    public int      trackNo;     // アルバムのトラックナンバ
    public String   bookmark;    // 最後に聴いた場所(ms)
    public String   year;        // 発売年
    public Uri      uri;         // URI

    public String   albumArt;
    public int      albumYear;

    public int      fav;         // favorite +1 by good button, -1 by bad button
    public Calendar lastPlayed;
    public int      playCount;
    public int      skipCount;
    public int      internal;    // 1 means the song is internal storage

    public int      musicId;     // unique ID
    public Map<String, ExTrackSituation> situationMap = new HashMap<>();

    public ExTrack() {

    }

    /***********************************************************************************************
     * to SQL
     **********************************************************************************************/
    // called at SituationDetailFragment.onLoadFinished()
    // get ExTracks from server
    public static List<ExTrack> getExTracksFromJson(SQLiteDatabase db, Context context, JSONArray jsonArray){
        List<ExTrack> exTracks = new ArrayList<>();

        try{
            for (int i=0; i<jsonArray.length(); i++) {
                String situation = jsonArray.getJSONObject(i).getJSONObject("tag").getString("value").replace("http://music.metadata.database.tag/", "");
                String artist    = jsonArray.getJSONObject(i).getJSONObject("artist").getString("value");
                String title     = jsonArray.getJSONObject(i).getJSONObject("title" ).getString("value");
                int    weight_d  = jsonArray.getJSONObject(i).getJSONObject("weight").getInt("value");

                List<ExTrack> exTracksFromSQL = getExTracksByArtistTitle(db, artist, title);

                if (exTracksFromSQL.isEmpty()) {
                    ExTrack exTrack = new ExTrack();
                    exTrack.artist = artist;
                    exTrack.title = title;
                    exTrack = addTrackDataByArtistTitle(context, exTrack);

                    ExTrackSituation es = new ExTrackSituation();
                    es.situation = situation;
                    es.weight    = 0;
                    es.weight_d  = weight_d;
                    es.weight_u  = 0;
                    exTrack.situationMap.put(situation, es);

                    insertExTrack(db, exTrack);

                    exTracks.add(exTrack);

                } else {
                    for (int j=0; j<exTracksFromSQL.size(); j++) {
                        ExTrack exTrack = exTracksFromSQL.get(j);

                        if (exTrack.situationMap.containsKey(situation)){
                            exTrack.situationMap.get(situation).weight_d = weight_d;

                            updateExTrackSituation(db, exTrack.musicId, exTrack.situationMap.get(situation));

                        } else {
                            ExTrackSituation es = new ExTrackSituation();
                            es.situation = situation;
                            es.weight    = 0;
                            es.weight_d  = weight_d;
                            es.weight_u  = 0;
                            exTrack.situationMap.put(situation, es);

                            insertExTrackSituation(db, exTrack.musicId, exTrack.situationMap.get(situation));
                        }

                        exTracks.add(exTrack);
                    }
                }

            }

        } catch (JSONException e) {
            Log.d("ExTrack","JSONのパースに失敗しました。 JSONException=" + e);
        }

        return exTracks;
    }

    // add Track data to ExTrack by artist and title if you have the song
    private static ExTrack addTrackDataByArtistTitle(Context context, ExTrack exTrack) {
        ContentResolver resolver = context.getContentResolver();
        String[] SELECTION_ARG = {exTrack.artist, exTrack.title};
        Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Track.COLUMNS,
                "artist = ? AND title = ?",
                SELECTION_ARG,
                null
        );

        boolean move = cursor.moveToFirst();
        while(move){
            if (cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) > 3000){
                Track track = new Track();
                track.setTrack(cursor);

                exTrack.addTrackDataToExTrack(track);
                cursor.close();
                return exTrack;

            } else { move = cursor.moveToNext(); }

        }
        cursor.close();
        return exTrack;
    }

    // add Track data to ExTrack
    public void addTrackDataToExTrack(Track track){
        this.id       = track.id;
        this.path     = track.path;
        this.title    = track.title;
        this.album    = track.album;
        this.albumId  = track.albumId;
        this.artist   = track.artist;
        this.artistId = track.artistId;
        this.duration = track.duration;
        this.trackNo  = track.trackNo;
        if(track.bookmark != null){
            this.bookmark = track.bookmark;
        }
        this.year     = track.year;
        this.uri      = track.uri;
        this.internal = 1;
    }

    public static void insertExTrack(SQLiteDatabase db, ExTrack exTrack) {
        ContentValues values = new ContentValues();
        values.put("id",          exTrack.id);
        values.put("path",        exTrack.path);
        values.put("title",       exTrack.title);
        values.put("album",       exTrack.album);
        values.put("album_id",    exTrack.albumId);
        values.put("artist",      exTrack.artist);
        values.put("artist_id",   exTrack.artistId);
        values.put("duration",    exTrack.duration);
        values.put("track_no",    exTrack.trackNo);
        values.put("bookmark",    exTrack.bookmark);
        values.put("year",        exTrack.year);
        if (exTrack.uri != null) {
            values.put("uri",     exTrack.uri.toString());
        }
        values.put("album_art",   exTrack.albumArt);
        values.put("album_year",  exTrack.albumYear);
        values.put("fav",         exTrack.fav);
        if (exTrack.lastPlayed != null) {
            values.put("last_played", calToStr(exTrack.lastPlayed));
        }
        values.put("play_count",  exTrack.playCount);
        values.put("skip_count",  exTrack.skipCount);
        values.put("internal",    exTrack.internal);

        long id = db.insert(EXTRACK_TABLE, null, values);
        if (id < 0) {
            //error handling
            Log.d(TAG, "insert ExTrack error");
        }

        exTrack.musicId = getMusicIdFromSQL(db, exTrack);
        for (Map.Entry<String, ExTrackSituation> esEntry : exTrack.situationMap.entrySet()) {
            insertExTrackSituation(db, exTrack.musicId, esEntry.getValue());
        }
    }

    public static void insertExTrackSituation(SQLiteDatabase db, int musicId, ExTrackSituation es){
        es.weight = es.weight_d + es.weight_u;

        ContentValues values = new ContentValues();
        values.put("music_id",  musicId);
        values.put("situation", es.situation);
        values.put("weight",    es.weight);
        values.put("weight_d",  es.weight_d);
        values.put("weight_u",  es.weight_u);

        long id = db.insert(EXTRACK_SITUATION_TABLE, null, values);
        if (id < 0) {
            //error handling
            Log.d(TAG, "insert ExTrackSituation error");
        }
    }

    public static void updateExTrackSituation(SQLiteDatabase db, int musicId, ExTrackSituation es) {
        String   where = "music_id = ? AND situation = ?";
        String[] params = new String[]{String.valueOf(musicId), es.situation};

        es.weight = es.weight_d + es.weight_u;

        ContentValues values = new ContentValues();
        values.put("music_id",  musicId);
        values.put("situation", es.situation);
        values.put("weight",    es.weight);
        values.put("weight_d",  es.weight_d);
        values.put("weight_u",  es.weight_u);

        db.update(EXTRACK_SITUATION_TABLE, values, where, params);
    }

    // not use
    public void deleteRow(SQLiteDatabase db, String where, String[] params){
        db.delete(EXTRACK_TABLE, where, params);
    }


    /***********************************************************************************************
     * from SQL
     **********************************************************************************************/

    public static int getMusicIdFromSQL(SQLiteDatabase db, ExTrack exTrack){
        String where = "id = ? AND artist = ? AND title = ?";
        String[] params = new String[]{String.valueOf(exTrack.id), exTrack.artist, exTrack.title};

        Cursor cursor = db.query(EXTRACK_TABLE, null, where, params, null, null, null, null);

        List<ExTrack> exTracks = setExTracksFromSQL(db, cursor);
        if (exTracks.size() == 1) {
            return exTracks.get(0).musicId;

        } else {
            // error handling
            Log.d(TAG, "getExTrackByMusicId: " + String.valueOf(exTracks.size()));
            return -1;
        }
    }

    public static List<ExTrack> getExTracksByArtistTitle(SQLiteDatabase db, String artist, String title){
        String where = "artist = ? AND title = ?";
        String[] params = new String[]{artist, title};

        Cursor cursor = db.query(EXTRACK_TABLE, null, where, params, null, null, null, null);

        return setExTracksFromSQL(db, cursor);
    }

    // step1: Get music_id from EXTRACK_SITUATION_TABLE where situation = situation
    // step2: Get ExTrack from EXTRACK_TABLE where music_id = musicId
    // step3: Set ExTrackSituation from EXTRACK_SITUATION_TABLE where music_id = musicId
    public static List<ExTrack> getExTracksBySituation(SQLiteDatabase db, String situation){
        List<ExTrack> exTracks = new ArrayList<>();

        String where = "situation = ?";
        String[] params = new String[]{situation};
        String orderBy = "weight DESC";

        Cursor cursor = db.query(EXTRACK_SITUATION_TABLE, null, where, params, null, null, orderBy, null);

        boolean move = cursor.moveToFirst();
        while (move) {
            int musicId = cursor.getInt(cursor.getColumnIndex("music_id"));
            ExTrack exTrack = getExTrackByMusicId(db, musicId);

            if (exTrack != null) {
                exTracks.add(exTrack);
            }
            move = cursor.moveToNext();
        }
        cursor.close();

        return exTracks;
    }

    public static ExTrack getExTrackByMusicId(SQLiteDatabase db, int musicId){
        String where = "music_id = ?";
        String[] params = new String[]{String.valueOf(musicId)};

        Cursor cursor = db.query(EXTRACK_TABLE, null, where, params, null, null, null, null);

        List<ExTrack> exTracks = setExTracksFromSQL(db, cursor);
        if (exTracks.size() == 1) {
            return exTracks.get(0);

        } else {
            // error handling
            Log.d(TAG, "getExTrackByMusicId: " + String.valueOf(exTracks.size()));
            return null;
        }
    }

    public static List<ExTrack> getInternalExTracks(List<ExTrack> exTracks) {
        List<ExTrack> internalExTracks = new ArrayList<>();
        for (ExTrack exTrack : exTracks) {
            if (exTrack.internal == 1) {
                internalExTracks.add(exTrack);
            }
        }

        return internalExTracks;
    }

    // set from sql
    private static List<ExTrack> setExTracksFromSQL(SQLiteDatabase db, Cursor cursor){
        List<ExTrack> exTracks = new ArrayList<ExTrack>();

        boolean move = cursor.moveToFirst();
        while (move) {
            ExTrack exTrack = new ExTrack();
            exTrack.id         = cursor.getLong(cursor.getColumnIndex("id"));
            exTrack.path       = cursor.getString(cursor.getColumnIndex("path"));
            exTrack.title      = cursor.getString(cursor.getColumnIndex("title"));
            exTrack.album      = cursor.getString(cursor.getColumnIndex("album"));
            exTrack.albumId    = cursor.getLong(cursor.getColumnIndex("album_id"));
            exTrack.artist     = cursor.getString(cursor.getColumnIndex("artist"));
            exTrack.artistId   = cursor.getLong(cursor.getColumnIndex("artist_id"));
            exTrack.duration   = cursor.getLong(cursor.getColumnIndex("duration"));
            exTrack.trackNo    = cursor.getInt(cursor.getColumnIndex("track_no"));
            exTrack.bookmark   = cursor.getString(cursor.getColumnIndex("bookmark"));
            exTrack.year       = cursor.getString(cursor.getColumnIndex("year"));
            exTrack.uri        = Uri.parse(cursor.getString(cursor.getColumnIndex("uri")));
            exTrack.albumArt   = cursor.getString(cursor.getColumnIndex("album_art"));
            exTrack.albumYear  = cursor.getInt(cursor.getColumnIndex("album_year"));
            exTrack.fav        = cursor.getInt(cursor.getColumnIndex("fav"));
            exTrack.lastPlayed = strToCal(cursor.getString(cursor.getColumnIndex("last_played")));
            exTrack.playCount  = cursor.getInt(cursor.getColumnIndex("play_count"));
            exTrack.skipCount  = cursor.getInt(cursor.getColumnIndex("skip_count"));
            exTrack.internal   = cursor.getInt(cursor.getColumnIndex("internal"));
            exTrack.musicId    = cursor.getInt(cursor.getColumnIndex("music_id"));

            setExTrackSituationFromSQL(db, exTrack);

            exTracks.add(exTrack);
            move = cursor.moveToNext();
        }
        cursor.close();

        return exTracks;
    }

    // add ExTrackSituations to ExTracks
    public static void setExTrackSituationFromSQL(SQLiteDatabase db, ExTrack exTrack) {
        String where = "music_id = ?";
        String[] params = new String[]{String.valueOf(exTrack.musicId)};

        Cursor cursor = db.query(EXTRACK_SITUATION_TABLE, null, where, params, null, null, null, null);

        boolean move = cursor.moveToFirst();
        while (move) {
            ExTrackSituation es = new ExTrackSituation();
            es.situation = cursor.getString(cursor.getColumnIndex("situation"));
            es.weight    = cursor.getInt(cursor.getColumnIndex("weight"));
            es.weight_d  = cursor.getInt(cursor.getColumnIndex("weight_d"));
            es.weight_u  = cursor.getInt(cursor.getColumnIndex("weight_u"));

            exTrack.situationMap.put(es.situation, es);
            move = cursor.moveToNext();
        }
        cursor.close();
    }

}
