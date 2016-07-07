package com.parrotgeek.parrotmodfloapp;

import android.content.ComponentName;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private boolean running = false;
    private BootReceiver rec;
    private Intent bcintent = new Intent(Intent.ACTION_BOOT_COMPLETED);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rec = new BootReceiver();
        setContentView(R.layout.activity_main);
        MyService.mainActivity = this;
        setRunning(MyService.running);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            ((TextView) findViewById(R.id.version)).setText("Version "+versionName);
        } catch (Exception e){}
    }

    @Override
    public void onResume() {
        super.onResume();
        setRunning(MyService.running);
    }

    public void setRunning(final boolean running) {
        this.running = running;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.textView2)).setText("ParrotMod is " + (running ? "" : "not ") + "running.");
            }
        });
    }

    public void start(View v) {
	rec.onReceive(getApplicationContext(), bcintent);
    }

    public void website(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.parrotgeek.com/"));
        startActivity(browserIntent);
    }

    public void xda(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/nexus-7-2013/orig-development/beta-1-parrotmod-improve-2013-nexus-7-t3375928"));
        startActivity(browserIntent);
    }
    
    public void hideicon(View v) {
        Toast.makeText(this,"App icon hidden. ParrotMod will still start on every boot.",Toast.LENGTH_LONG).show();
         if(!running) {
            start(null);
        }
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, MainActivity.class),PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        finish();
    }

    @Override
    public void onDestroy() {
        MyService.mainActivity = null;
        super.onDestroy();
    }
}
