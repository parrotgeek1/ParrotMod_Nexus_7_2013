package com.parrotgeek.parrotmodfloapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void start(View v) {
        Intent serviceIntent = new Intent(this, MyService.class);
        startService(serviceIntent);
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
        pm.setComponentEnabledSetting(new ComponentName(this, MainActivity.class),PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        if(!MyService.running) {
            start(null);
        }
        Toast.makeText(this,"App icon hidden. ParrotMod will still be enabled at every boot.",Toast.LENGTH_LONG).show();
        finish();
    }
}
