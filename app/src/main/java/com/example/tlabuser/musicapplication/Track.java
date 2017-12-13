package com.example.tlabuser.musicapplication;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * class of Track
 */


public class Track{
    public long   id;          // コンテントプロバイダに登録されたID
    public String path;        // 実データのPATH
    public String title;       // トラックタイトル
    public String album;       // アルバムタイトル
    public long   albumId;     // アルバムのID
    public String artist;      // アーティスト名
    public long   artistId;    // アーティストのID
    public long   duration;    // 再生時間(ミリ秒)
    public int    trackNo;     // アルバムのトラックナンバ
    public String bookmark;    // 最後に聴いた場所(ms)
    public String year;        // 発売年
    public Uri    uri;         // URI

    public static final String[] COLUMNS = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.BOOKMARK,
            MediaStore.Audio.Media.YEAR,
    };

    public void setTrack(Cursor cursor){
        id       = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
        path     = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        title    = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        album    = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        albumId  = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        artist   = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        artistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
        duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
        trackNo  = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK));
        bookmark = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.BOOKMARK));
        year     = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));
        uri      = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }

    public static List<Track> getItems(Context activity) {

        List tracks = new ArrayList();
        ContentResolver resolver = activity.getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Track.COLUMNS,
                null,
                null,
                "title ASC"
        );
        while( cursor.moveToNext() ){
            if( cursor.getLong(cursor.getColumnIndex( MediaStore.Audio.Media.DURATION)) < 3000 ){continue;}
            Track track = new Track();
            track.setTrack(cursor);
            tracks.add(track);
        }
        cursor.close();
        return tracks;
    }

    public static List<Track> getItemsByAlbum(Context activity, long albumID) {

        List tracks = new ArrayList();
        ContentResolver resolver = activity.getContentResolver();
        String[] SELECTION_ARG = {""};
        SELECTION_ARG[0] = String.valueOf(albumID);
        Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Track.COLUMNS,
                MediaStore.Audio.Media.ALBUM_ID + "= ?",
                SELECTION_ARG,
                null
        );
        while( cursor.moveToNext() ){
            if( cursor.getLong(cursor.getColumnIndex( MediaStore.Audio.Media.DURATION)) < 3000 ){continue;}
            Track track = new Track();
            track.setTrack(cursor);
            tracks.add(track);
        }
        cursor.close();
        return tracks;
    }

}
