package com.parrotgeek.parrotmodfloapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.util.Log;
import android.os.UserManager;
import android.os.Process;


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
            arg0.stopService(intent);
            arg0.startService(intent);
            if(arg1.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
                Log.d("BootReceiver","reenable icon");
                PackageManager pm = arg0.getPackageManager();
                pm.setComponentEnabledSetting(new ComponentName(arg0, MainActivity.class),PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            }
            Log.i("BootReceiver", "started");
        } else {
            Log.i("BootReceiver", "not first user, so disable myself");
            PackageManager pm = arg0.getPackageManager();
            pm.setComponentEnabledSetting(new ComponentName(arg0, MainActivity.class),PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);

        }
    }
}