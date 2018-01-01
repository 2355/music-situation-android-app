package com.example.tlabuser.musicapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLOpenHelper extends SQLiteOpenHelper {
    private static final int    DB_VERSION = 1;
    private static final String DB_NAME = "situation_track";
    public static final String EXTRACK_TABLE = "extrack";
    public static final String EXTRACK_SITUATION_TABLE = "extrack_situation";
    public static final String SITUATION_TABLE = "situation";

    public SQLOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    private void createTables(SQLiteDatabase db){
        String createExTrackTable =
                "CREATE TABLE " + EXTRACK_TABLE + "(" +
                        "id "          + "integer " + "NOT NULL DEFAULT 0, " +
                        "path "        + "text "    + "NOT NULL DEFAULT '', " +
                        "title "       + "text "    + "NOT NULL DEFAULT '', " +
                        "album "       + "text "    + "NOT NULL DEFAULT '', " +
                        "album_id "    + "integer " + "NOT NULL DEFAULT 0, " +
                        "artist "      + "text "    + "NOT NULL DEFAULT '', " +
                        "artist_id "   + "integer " + "NOT NULL DEFAULT 0, " +
                        "duration "    + "integer " + "NOT NULL DEFAULT 0, " +
                        "track_no "    + "integer " + "NOT NULL DEFAULT 0, " +
                        "bookmark "    + "text "    + "NOT NULL DEFAULT '', " +
                        "year "        + "text "    + "NOT NULL DEFAULT '', " +
                        "uri "         + "text "    + "NOT NULL DEFAULT '', " +
                        "album_art "   + "text "    + "NOT NULL DEFAULT '', " +
                        "album_year "  + "integer " + "NOT NULL DEFAULT 0, " +
                        "situation "   + "text "    + "NOT NULL DEFAULT '', " +
                        "weight "      + "integer " + "NOT NULL DEFAULT 0, " +
                        "weight_d "    + "integer " + "NOT NULL DEFAULT 0, " +
                        "weight_u "    + "integer " + "NOT NULL DEFAULT 0, " +
                        "fav "         + "integer " + "NOT NULL DEFAULT 0, " +
                        "last_played " + "text "    + "NOT NULL DEFAULT '', " +
                        "play_count "  + "integer " + "NOT NULL DEFAULT 0, " +
                        "skip_count "  + "integer " + "NOT NULL DEFAULT 0, " +
                        "internal "    + "integer " + "NOT NULL DEFAULT 0" +
                        ");";

        String createSituationTable =
                "CREATE TABLE " + SITUATION_TABLE + "(" +
                        "name "   + "text "    + "NOT NULL DEFAULT '', " +
                        "tracks " + "integer " + "NOT NULL DEFAULT 0" +
                        ");";

        db.execSQL(createExTrackTable);
        db.execSQL(createSituationTable);
    }

}