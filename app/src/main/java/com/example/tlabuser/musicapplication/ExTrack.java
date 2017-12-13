package com.example.tlabuser.musicapplication;

import android.net.Uri;

import java.util.Date;

/**
 * Extend Track class
 */


public class ExTrack {
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

    public String albumArt;
    public int    albumYear;

    public String situation;    // name name
    public int    weight;       // name weight (weight_d + weight_u)
    public int    weight_d;     // name weight (default by server)
    public int    weight_u;     // name weight (feedback by user)
    public int    fav;          // favorite +1 by good button, -1 by bad button
    public Date   lastPlayed;
    public int    playCount;
    public int    skipCount;
    public int    internal;     // 1 means the song is internal storage


    public ExTrack(){
        id         = 0;
        path       = "";
        title      = "";
        album      = "";
        albumId    = 0;
        artist     = "";
        artistId   = 0;
        duration   = 0;
        trackNo    = 0;
        bookmark   = "";
        year       = "";
        uri        = null;
        albumArt   = "";
        albumYear  = 0;
        situation  = "";
        weight     = 0;
        weight_d   = 0;
        weight_u   = 0;
        fav        = 0;
        lastPlayed = null;
        playCount  = 0;
        skipCount  = 0;
        internal   = 0;
    }

}
