package com.parrotgeek.parrotmodfloapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by ethan on 5/16/16.
 */

public class BootReceiver extends BroadcastReceiver
{
    public void onReceive(Context arg0, Intent arg1)
    {
        Intent intent = new Intent(arg0,MyService.class);
        arg0.startService(intent);
        Log.i("Autostart", "started");
    }
}