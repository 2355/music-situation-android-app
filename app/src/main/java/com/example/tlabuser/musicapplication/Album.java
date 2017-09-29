package com.example.tlabuser.musicapplication;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * Created by tlabuser on 2017/07/13.
 */

public class Album {

    public long             id;
    public String           album;
    public String           albumArt;
    public long             albumId;
    public String           albumKey;
    public String           artist;
    public int              year;
    public int              tracks;

    public static final String[] FILLED_PROJECTION = {
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ALBUM_ART,
            MediaStore.Audio.Albums.ALBUM_KEY,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.FIRST_YEAR,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
    };

    public Album(Cursor cursor){
        id       = cursor.getLong(  cursor.getColumnIndex( MediaStore.Audio.Albums._ID            ));
        album    = cursor.getString(cursor.getColumnIndex( MediaStore.Audio.Albums.ALBUM          ));
        albumArt = cursor.getString(cursor.getColumnIndex( MediaStore.Audio.Albums.ALBUM_ART      ));
        albumId  = cursor.getLong(  cursor.getColumnIndex( MediaStore.Audio.Media._ID             ));
        albumKey = cursor.getString(cursor.getColumnIndex( MediaStore.Audio.Albums.ALBUM_KEY      ));
        artist   = cursor.getString(cursor.getColumnIndex( MediaStore.Audio.Albums.ARTIST         ));
        year     = cursor.getInt(   cursor.getColumnIndex( MediaStore.Audio.Albums.FIRST_YEAR     ));
        tracks   = cursor.getInt(   cursor.getColumnIndex( MediaStore.Audio.Albums.NUMBER_OF_SONGS));
    }


    public static List<Album> getItems(Context activity) {

        List albums = new ArrayList();
        ContentResolver resolver = activity.getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                Album.FILLED_PROJECTION,
                null,
                null,
                "album  ASC"
        );

        while( cursor.moveToNext() ){
            albums.add(new Album(cursor));
        }
        cursor.close();
        return albums;
    }


    public static List<Album> getItemsByArtist(Context activity, String artist) {

        List albums = new ArrayList();
        ContentResolver resolver = activity.getContentResolver();
        String[] SELECTION_ARG = {""};
        SELECTION_ARG[0] = artist;
        Cursor cursor = resolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                Album.FILLED_PROJECTION,
                MediaStore.Audio.Media.ARTIST + "= ?", // albumartistでフィルタリング
                SELECTION_ARG,
                "minyear DESC"
        );

        while( cursor.moveToNext() ){
            albums.add(new Album(cursor));
        }
        cursor.close();
        return albums;
    }


}
