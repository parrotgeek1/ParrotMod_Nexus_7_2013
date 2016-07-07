package com.parrotgeek.parrotmodfloapp;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import android.app.PendingIntent;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MyService extends Service {
    public static boolean running = false;
    private static final String TAG = "ParrotMod";
    public static MainActivity mainActivity;

    private PowerManager.WakeLock wl;

    private SuShell shell;

    private String emicb;

    private class GyroRunnable implements Runnable {
        private SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        private SensorEventListener mSensorEventListener;
        long changed = System.currentTimeMillis();
        private Runnable mRunnable1 = new Runnable() {
            @Override
            public void run() {
                if(changed < (System.currentTimeMillis() - 300)) {
                    // if didn't get sensor in 300ms restart it
                    Log.d(TAG,"---- RESET SENSORS ----");
                    unregister();
                    register();
                }
            }
        };
        Handler handler = new Handler();
        @Override
        public void run() {
            mSensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    changed = System.currentTimeMillis();
                    handler.postDelayed(mRunnable1, 300); // ms
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // don't need it
                }
            };
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            filter.addAction("android.intent.action.HDMI_PLUGGED");
            registerReceiver(mReceiver, filter);
            register();

        }
        private void unregister() {
            Log.d(TAG,"unregister");
            changed = 0;
            handler.removeCallbacks(mRunnable1);
            mSensorManager.unregisterListener(mSensorEventListener);
        }

        private void register() {
            Log.d(TAG,"register");
            changed = System.currentTimeMillis();
            mSensorManager.registerListener(mSensorEventListener, accel, SensorManager.SENSOR_DELAY_NORMAL); // ~240ms
            mSensorManager.registerListener(mSensorEventListener, gyro, 10000000); // 10 sec, basically to keep it running
        }


        public BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    wl.acquire(4000); // 4 sec
                    unregister();
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    if(wl.isHeld()) wl.release();
                    register();
                } else if(intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)
                        ||intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)
                        ||intent.getAction().equals("android.intent.action.HDMI_PLUGGED")) {
                    wl.acquire(500);
                    Log.d("power",intent.getAction());
                    shell.run("cat '"+emicb+"' > /dev/elan-iap");
                }
            }
        };
    }
    private GyroRunnable mGyroRunnable;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void copyFile(String filename) {
        AssetManager assetManager = this.getAssets();

        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(filename);
            String newFileName = getApplicationContext().getApplicationInfo().dataDir + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(getApplicationContext(), "COPY ERROR: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private String execCmd(String[] cmd) {
        try {
            java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
            return s.hasNext() ? s.next().trim() : "";
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "EXEC ERROR: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            setRunning(false);
            return null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        copyFile("ParrotMod.sh");
        copyFile("emi_config.bin");
        String script = getApplicationContext().getApplicationInfo().dataDir + "/ParrotMod.sh";
        boolean su = SystemPropertiesProxy.getInstance().getBoolean("supolicy.loaded",false);
        if (!su) {
            setRunning(false);
            AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext()).create();
            alertDialog.setTitle("ParrotMod error");
            alertDialog.setMessage("You don't have root, or you denied the root request!\n\n" +
                    "NOTE: ParrotMod currently only works with SuperSU, not King(o)Root. This will not be fixed.");
            // Alert dialog button
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Dismiss",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();// use dismiss to cancel alert dialog
                        }
                    });
            alertDialog.show();
            return START_NOT_STICKY;
        }
        final String[] cmd = new String[]{"su", "-c", " sh '" + script + "' </dev/null >/dev/null 2>&1"};
        new Thread(new Runnable() {
            public void run() {
                String str;
                while (true) {
                    str = execCmd(cmd);
                    if(str == null) {
                        Log.e(TAG, "STOP LOOP due to exec error");
                        return;
                    }
                    Log.e(TAG, "run: for some reason script stopped, restarting");
                }
            }
        }).start();

        shell = new SuShell();
        emicb = getApplicationContext().getApplicationInfo().dataDir + "/emi_config.bin";

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "parrotmod_touch_calibration");

        mGyroRunnable = new GyroRunnable();
        new Thread(mGyroRunnable).start();

        setRunning(true);
        Intent notificationIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        notificationIntent.setData(Uri.parse("package:" + getPackageName()));
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("ParrotMod is running")
                .setContentText("Tap here and go to Notifications to hide this.")
                .setSmallIcon(R.drawable.notificon)
                .setContentIntent(resultPendingIntent)
                .setWhen(0)
                .setPriority(Notification.PRIORITY_MIN)
                .build();
        startForeground(0x600dc0de, notification);


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setRunning(false);
        unregisterReceiver(mGyroRunnable.mReceiver);
        sendBroadcast(new Intent("com.parrotgeek.parrotmodfloapp.action.START_SERVICE"));
        shell.end();
    }

    private void setRunning(boolean running) {
        if(mainActivity != null) mainActivity.setRunning(running);
        MyService.running = running;
    }
}
