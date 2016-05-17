package com.parrotgeek.parrotmodfloapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;
import android.os.UserManager;
import android.os.Process;

/**
 * Created by ethan on 5/16/16.
 */

public class BootReceiver extends BroadcastReceiver
{

    private boolean isAdminUser(Context context)
    {
        UserHandle uh = Process.myUserHandle();
        UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
        if(um != null) {
            return um.getSerialNumberForUser(uh) == 0;
        } else {
            return false;
        }
    }

    public void onReceive(Context arg0, Intent arg1)
    {
        UserManager um = (UserManager) arg0.getSystemService(Context.USER_SERVICE);
        if(isAdminUser(arg0)) {
            Intent intent = new Intent(arg0, MyService.class);
            arg0.startService(intent);
            Log.i("BootReceiver", "started");
        } else {
            Log.i("BootReceiver", "not first user, so not starting");
        }
    }
}