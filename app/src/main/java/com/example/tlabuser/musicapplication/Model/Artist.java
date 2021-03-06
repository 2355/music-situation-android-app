package com.example.tlabuser.musicapplication.Model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tlabuser on 2017/07/14.
 */

public class Artist {

    public long             id;
    public String           artist;
    public String           artistKey;
    public int              albums;
    public int              tracks;

    public static final String[] FILLED_PROJECTION = {
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.ARTIST_KEY,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
    };

    public Artist(Cursor cursor){
        id              =cursor.getLong(  cursor.getColumnIndex( MediaStore.Audio.Artists._ID              ));
        artist          =cursor.getString(cursor.getColumnIndex( MediaStore.Audio.Artists.ARTIST           ));
        artistKey       =cursor.getString(cursor.getColumnIndex( MediaStore.Audio.Artists.ARTIST_KEY       ));
        albums          =cursor.getInt(   cursor.getColumnIndex( MediaStore.Audio.Artists.NUMBER_OF_ALBUMS ));
        tracks          =cursor.getInt(   cursor.getColumnIndex( MediaStore.Audio.Artists.NUMBER_OF_TRACKS ));
    }

    public static List<Artist> getItems(Context activity) {

        List<Artist> artists = new ArrayList<Artist>();

        ContentResolver resolver = activity.getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                Artist.FILLED_PROJECTION,
                null,
                null,
                "artist  ASC"
        );

        while( cursor.moveToNext() ){
            artists.add(new Artist(cursor));
        }

        cursor.close();
        return artists;
    }



}