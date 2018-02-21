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
    public static final String USER_TABLE = "user_table";

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
                        "music_id "    + "integer " + "NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                        "id "          + "integer " + "DEFAULT 0, " +
                        "path "        + "text "    + "DEFAULT '', " +
                        "title "       + "text "    + "DEFAULT '', " +
                        "album "       + "text "    + "DEFAULT '', " +
                        "album_id "    + "integer " + "DEFAULT 0, " +
                        "artist "      + "text "    + "DEFAULT '', " +
                        "artist_id "   + "integer " + "DEFAULT 0, " +
                        "duration "    + "integer " + "DEFAULT 0, " +
                        "track_no "    + "integer " + "DEFAULT 0, " +
                        "bookmark "    + "text "    + "DEFAULT '', " +
                        "year "        + "text "    + "DEFAULT '', " +
                        "uri "         + "text "    + "DEFAULT '', " +
                        "album_art "   + "text "    + "DEFAULT '', " +
                        "album_year "  + "integer " + "DEFAULT 0, " +
                        "fav "         + "integer " + "DEFAULT 0, " +
                        "last_played " + "text "    + "DEFAULT '', " +
                        "play_count "  + "integer " + "DEFAULT 0, " +
                        "skip_count "  + "integer " + "DEFAULT 0, " +
                        "internal "    + "integer " + "DEFAULT 0" +
                        ");";

        String createExTrackSituationTable =
                "CREATE TABLE " + EXTRACK_SITUATION_TABLE + "(" +
                        "music_id "    + "integer " + "NOT NULL DEFAULT 0, " +
                        "situation "   + "text "    + "DEFAULT '', " +
                        "weight "      + "integer " + "DEFAULT 0, " +
                        "weight_d "    + "integer " + "DEFAULT 0, " +
                        "weight_u "    + "integer " + "DEFAULT 0" +
                        ");";

        String createSituationTable =
                "CREATE TABLE " + SITUATION_TABLE + "(" +
                        "name "        + "text "    + "UNIQUE DEFAULT '', " +
                        "tracks "      + "integer " + "DEFAULT 0" +
                        ");";

        String createUserTable =
                "CREATE TABLE " + USER_TABLE + "(" +
                        "name "        + "text "    + "UNIQUE DEFAULT '', " +
                        "created_at "  + "text " + "DEFAULT ''" +
                        ");";

        db.execSQL(createExTrackTable);
        db.execSQL(createExTrackSituationTable);
        db.execSQL(createSituationTable);
    }

}
