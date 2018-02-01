package com.example.tlabuser.musicapplication;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * JSON Utility class.
 * Get JSON from server
 */

public class JsonUtil {

    @Nullable
    public static JSONObject getJson(String urlStr) {

        HttpURLConnection connection = null;

        try{
            URL url = new URL(urlStr);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
        }
        catch (MalformedURLException exception){
            // NO-OP
        }
        catch (IOException exception){
            // NO-OP
        }

        try {
            BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                if (length > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }

            JSONObject json = new JSONObject(new String(outputStream.toByteArray()));
            return json;
        }
        catch (IOException exception){
            // NO-OP
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
