package com.example.tlabuser.musicapplication;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by tlabuser on 2017/07/13.
 */

public  class ImageCache {
    private static HashMap<String,Bitmap> cache = new HashMap<String,Bitmap>();

    public static Bitmap getImage(String key) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        return null;
    }

    public static void setImage(String key, Bitmap image) {
        cache.put(key, image);
    }

}