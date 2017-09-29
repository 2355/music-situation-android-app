package com.example.tlabuser.musicapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by tlabuser on 2017/09/05.
 */

public class PlayerBroadcastReceiver extends BroadcastReceiver {

    public Handler handler;

    @Override
    public void onReceive(Context context, Intent intent) {

        String message = intent.getStringExtra("message");
        if(handler !=null){
            Message msg = new Message();

            Bundle data = new Bundle();
            data.putString("message", message);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    }

    /**
     * メイン画面の表示を更新
     */
    public void registerHandler(Handler handler) {
        this.handler = handler;
    }

}