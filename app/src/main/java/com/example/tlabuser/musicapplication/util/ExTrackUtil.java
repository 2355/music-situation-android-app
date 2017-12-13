package com.example.tlabuser.musicapplication.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.tlabuser.musicapplication.ExTrack;
import com.example.tlabuser.musicapplication.Track;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.tlabuser.musicapplication.SQLOpenHelper.EXTRACK_TABLE;

/**
 * ExTrack Utility class
 */

public class ExTrackUtil {

    /***********************************************************************************************
     * to SQL
     **********************************************************************************************/
    // called at SituationMenu.onLoadFinished()
    // get ExTracks from server
    public static List<ExTrack> parseJsonArray(Context context, JSONArray jsonArray){
        List<ExTrack> exTracks = new ArrayList<ExTrack>();

        try{
            for(int i=0; i<jsonArray.length(); i++){
                ExTrack exTrack = new ExTrack();
                exTrack.situation = jsonArray.getJSONObject(i).getJSONObject("tag").getString("value").replace("http://music.metadata.database.tag/", "");
                exTrack.artist    = jsonArray.getJSONObject(i).getJSONObject("artist").getString("value");
                exTrack.title     = jsonArray.getJSONObject(i).getJSONObject("title" ).getString("value");
                exTrack.weight_d  = jsonArray.getJSONObject(i).getJSONObject("weight").getInt("value");

                exTrack = addTrackDataByArtistTitle(context, exTrack);
                exTracks.add(exTrack);
            }

        } catch (JSONException e) {
            Log.d("onLoadFinished","JSONのパースに失敗しました。 JSONException=" + e);
        }

        return exTracks;
    }

    // add Track data to ExTrack by artist, title
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
            if(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) > 3000){
                Track track = new Track();
                track.setTrack(cursor);
                exTrack = trackToExTrack(exTrack, track);
                return exTrack;

            }else{ move = cursor.moveToNext(); }

        }
        cursor.close();
        return exTrack;
    }

    // add Track data to ExTrack
    private static ExTrack trackToExTrack(ExTrack exTrack, Track track){
        exTrack.id       = track.id;
        exTrack.path     = track.path;
        exTrack.title    = track.title;
        exTrack.album    = track.album;
        exTrack.albumId  = track.albumId;
        exTrack.artist   = track.artist;
        exTrack.artistId = track.artistId;
        exTrack.duration = track.duration;
        exTrack.trackNo  = track.trackNo;
        exTrack.bookmark = track.bookmark;
        exTrack.year     = track.year;
        exTrack.uri      = track.uri;
        exTrack.internal = 1;

        return exTrack;
    }


    // convert ExTrack to ContentValues
    private static ContentValues setValues(ExTrack exTrack){
        exTrack.weight = exTrack.weight_d + exTrack.weight_u;

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
        if (exTrack.uri != null){
            values.put("uri",         exTrack.uri.toString());
        }
        values.put("album_art",   exTrack.albumArt);
        values.put("album_year",  exTrack.albumYear);
        values.put("name",   exTrack.situation);
        values.put("weight",      exTrack.weight);
        values.put("weight_d",    exTrack.weight_d);
        values.put("weight_u",    exTrack.weight_u);
        values.put("fav",         exTrack.fav);
        if (exTrack.lastPlayed != null){
            values.put("last_played", exTrack.lastPlayed.toString());
        }
        values.put("play_count",  exTrack.playCount);
        values.put("skip_count",  exTrack.skipCount);
        values.put("internal",    exTrack.internal);

        return values;
    }

    // insert ExTracks to SQL
    public static void insertRows(SQLiteDatabase db, List<ExTrack> exTracks){
        for (int i=0; i<exTracks.size(); i++){
            ExTrack exTrack = exTracks.get(i);

            ContentValues values = setValues(exTrack);
            long id = db.insert(EXTRACK_TABLE, null, values);
            if (id < 0) {
                //error handling
            }
        }
    }

    public static void updateRow(SQLiteDatabase db, ExTrack exTrack, String where, String[] params){
        ContentValues values = setValues(exTrack);
        db.update(EXTRACK_TABLE, values, where, params);
    }

    public void deleteRow(SQLiteDatabase db, String where, String[] params){
        db.delete(EXTRACK_TABLE, where, params);
    }


    /***********************************************************************************************
     * from SQL
     **********************************************************************************************/
    // call at SituationMenu.onCreate()
    public static List<ExTrack> getExTracksBySituation(SQLiteDatabase db, String situation){
        String   where = "name = ?";
        String[] params = new String[]{situation};
        String   orderBy = "weight DESC";

        Cursor cursor = selectRows(db, where, params, orderBy);

        return setExTracksFromSQL(cursor);
    }

    public static List<ExTrack> getInternalExTracksBySituation(SQLiteDatabase db, String situation){
        String   where = "name = ? AND internal = 1";
        String[] params = new String[]{situation};
        String   orderBy = "weight DESC";

        Cursor cursor = selectRows(db, where, params, orderBy);

        return setExTracksFromSQL(cursor);
    }

    public static List<ExTrack> getExTracksByArtistTitle(SQLiteDatabase db, String artist, String title){
        String   where = "artist = ? AND title = ?";
        String[] params = new String[]{artist, title};

        Cursor cursor = selectRows(db, where, params);

        return setExTracksFromSQL(cursor);
    }

    public static List<ExTrack> getExTracksById(SQLiteDatabase db, String id){
        String   where = "id = ?";
        String[] params = new String[]{id};

        Cursor cursor = selectRows(db, where, params);

        return setExTracksFromSQL(cursor);
    }


    // set from sql
    private static List<ExTrack> setExTracksFromSQL(Cursor cursor){
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
            exTrack.situation  = cursor.getString(cursor.getColumnIndex("name"));
            exTrack.weight     = cursor.getInt(cursor.getColumnIndex("weight"));
            exTrack.weight_d   = cursor.getInt(cursor.getColumnIndex("weight_d"));
            exTrack.weight_u   = cursor.getInt(cursor.getColumnIndex("weight_u"));
            exTrack.fav        = cursor.getInt(cursor.getColumnIndex("fav"));
            exTrack.lastPlayed = strToDate(cursor.getString(cursor.getColumnIndex("last_played")));
            exTrack.playCount  = cursor.getInt(cursor.getColumnIndex("play_count"));
            exTrack.skipCount  = cursor.getInt(cursor.getColumnIndex("skip_count"));
            exTrack.internal   = cursor.getInt(cursor.getColumnIndex("internal"));

            exTracks.add(exTrack);
            move = cursor.moveToNext();
        }
        cursor.close();

        return exTracks;
    }

    private static Date strToDate(String dateString) {
        Date date;
        try{
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            date = format.parse(dateString);
            return date;
        }catch (ParseException e){
            return null;
        }
    }

    private static Cursor selectRows(SQLiteDatabase db, String where, String[] params) {
        return selectRows(db, where, params, null);
    }

    private static Cursor selectRows(SQLiteDatabase db, String where, String[] params, String orderBy){
        // query(tableName, selectColumns, whereClause, whereArgs, groupBy, having, orderBy, limit);
        return db.query(EXTRACK_TABLE, null, where, params, null, null, orderBy, null);
    }

}
