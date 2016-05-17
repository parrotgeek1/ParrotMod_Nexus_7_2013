package com.parrotgeek.parrotmodfloapp;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ethan on 5/16/16.
 */
public class MyService extends Service
{
    private static final String TAG = "ParrotMod";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void copyFile(String filename) {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
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
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    private String execCmd(String[] cmd) {
        try {
            java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
            return s.hasNext() ? s.next().trim() : "";
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),"EXEC ERROR: "+e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            return "";
        }
    }

    @Override
    public void onStart(Intent intent, int startid)
    {
        Log.d(TAG, "onStart");
        copyFile("ParrotMod.sh");
        copyFile("emi_config.bin");
        String script = getApplicationContext().getApplicationInfo().dataDir + "/files/ParrotMod.sh";
        String sucheck = execCmd(new String[] {"su","-c","echo hello"});
        if(!sucheck.equals("hello")) {
            Toast.makeText(this, "ParrotMod ERROR:\n\nYOU DON'T HAVE ROOT\nOr, you denied it.", Toast.LENGTH_LONG).show();
            return;
        }
        final String[] cmd = new String[]{"su", "-c", " sh '" + script + "' </dev/null >/dev/null 2>&1"};
        new Thread(new Runnable() {
            public void run() {
                execCmd(cmd);
            }
        }).start();
    }
}
