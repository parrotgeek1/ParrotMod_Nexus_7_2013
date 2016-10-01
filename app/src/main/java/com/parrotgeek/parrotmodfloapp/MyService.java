package com.parrotgeek.parrotmodfloapp;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
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
    public static boolean actuallyStop;
    public static MyService self;
    private SharedPreferences sharedPreferences;

    private class GyroRunnable implements Runnable {
        private SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        private Sensor accel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        private Sensor gyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        private SensorEventListener mSensorEventListener;
        long changed = System.currentTimeMillis();
        private Runnable mRunnable1 = new Runnable() {
            @Override
            public void run() {
                if(changed < (System.currentTimeMillis() - 500)) {
                    // if didn't get sensor in 500ms restart it
                    Log.d(TAG,"---- RESET SENSORS ----");
                    unregister();
                    register();
                }
            }
        };
        private Runnable calibrun = new Runnable() {
            @Override
            public void run() {
                calib();
            }
        };
        private Handler handler = new Handler();
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
            changed = 0;
            handler.removeCallbacks(mRunnable1);
            mSensorManager.unregisterListener(mSensorEventListener);
        }

        private void register() {
            changed = System.currentTimeMillis();
            mSensorManager.registerListener(mSensorEventListener, accel, SensorManager.SENSOR_DELAY_NORMAL); // ~240ms
            mSensorManager.registerListener(mSensorEventListener, gyro, 2500000); // 2.5 sec, basically to keep it running
        }

        public BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    wl.acquire(5000); // 5 sec
                    unregister();
                    handler.postDelayed(calibrun,2000); // calib in 2sec
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    handler.removeCallbacks(calibrun); // dont calibrate
                    register();
                    shell.run("cat '"+emicb+"' > /dev/elan-iap");
                    if(wl.isHeld()) wl.release();
                } else if(intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)
                        ||intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)
                        ||intent.getAction().equals("android.intent.action.HDMI_PLUGGED")) {
                    wl.acquire(2000);
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
            Crasher.crash();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyService.actuallyStop = false;
        String model = Build.DEVICE;
        if(!(model.equals("flo")||model.equals("deb"))) {
            Log.d(TAG,"unsupported device: "+model);
            MyService.actuallyStop = true;
            return START_NOT_STICKY;
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        try {
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            String lastCopied = sharedPreferences.getString("lastcopiedver","");
            if(!lastCopied.equals(version)) {
                copyFile("ParrotMod.sh");
                copyFile("emi_config.bin");
                sharedPreferences.edit().putString("lastcopiedver",version).commit();
            }
        } catch (Exception e) {
            Crasher.crash();
        }
        String script = getApplicationContext().getApplicationInfo().dataDir + "/ParrotMod.sh";

        shell = new SuShell();
        emicb = getApplicationContext().getApplicationInfo().dataDir + "/emi_config.bin";

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "parrotmod_touch_calibration");

        mGyroRunnable = new GyroRunnable();
        new Thread(mGyroRunnable).start();

        shell.run(". '" + script + "'");

        setRunning(true);
        Intent notificationIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        notificationIntent.setData(Uri.parse("package:" + getPackageName()));
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("ParrotMod is enabled")
                .setContentText("Tap here and go to Notifications to hide this.")
                .setSmallIcon(R.drawable.notificon)
                .setContentIntent(resultPendingIntent)
                .setWhen(0)
                .setPriority(Notification.PRIORITY_MIN)
                .build();
        startForeground(0x600dc0de, notification);

        MyService.self = this;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setRunning(false);
        unregisterReceiver(mGyroRunnable.mReceiver);
        shell.end();
        self = null;
        if(!MyService.actuallyStop) sendBroadcast(new Intent("com.parrotgeek.parrotmodfloapp.action.START_SERVICE"));
    }

    private void setRunning(boolean running) {
        if(mainActivity != null) mainActivity.setRunning(running);
        MyService.running = running;
    }

    private void waitsec(int sec) {
        try {
            Thread.sleep(1000*sec);
        } catch (InterruptedException e) {
            Log.wtf(TAG,e.getMessage());
        }
    }

    private void calib() {
        shell.run("pwr=$(cat /sys/devices/i2c-3/3-0010/power/control); echo on > /sys/devices/i2c-3/3-0010/power/control; echo ff > /proc/ektf_dbg");
        waitsec(1);
        shell.run("echo $pwr > /sys/devices/i2c-3/3-0010/power/control");
    }
    public void suerror() {
        if(MyService.actuallyStop) return;
        Log.e(TAG, "suerror");
        setRunning(false);
        startActivity(new Intent(this,MainActivity.class).putExtra("rooterror",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        MyService.actuallyStop = true;
    }
}
