package com.parrotgeek.parrotmodfloapp;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private boolean running = false;
    private BootReceiver rec;
    private Intent bcintent = new Intent(Intent.ACTION_BOOT_COMPLETED);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String model = Build.DEVICE;
        if(!(model.equals("flo")||model.equals("deb"))) {
            AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext()).create();
            alertDialog.setTitle("ParrotMod does not support this device!");
            alertDialog.setMessage("Only the Nexus 7 2013 (both Wi-Fi and LTE) is supported.");
            // Alert dialog button
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Dismiss",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();// use dismiss to cancel alert dialog
                            finish();
                        }
                    });
            alertDialog.show();
            this.running = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Button btn = (Button) findViewById(R.id.button);
                    if(btn != null) {
                        btn.setText("Unsupported device!");
                        btn.setEnabled(false);
                    } else {
                        Crasher.crash();
                    }
                }
            });
        }

        if(getIntent().getBooleanExtra("rooterror",false))  {
            rootpopup();
        }
        rec = new BootReceiver();
        setContentView(R.layout.activity_main);
        MyService.mainActivity = this;
        setRunning(MyService.running);
        try {
            String versionName = "Version " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            TextView tv = (TextView) findViewById(R.id.version);
            if(tv != null) tv.setText(versionName);
        } catch (Exception e){
            finish();
        }
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
                Button btn = (Button) findViewById(R.id.button);
                if(btn != null) {
                    btn.setText(running ? "ParrotMod is enabled" : "Enable ParrotMod");
                    btn.setEnabled(!running);
                } else {
                    Crasher.crash();
                }
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

    public void rootpopup() {
        AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext()).create();
        alertDialog.setTitle("ParrotMod needs root access!");
        alertDialog.setMessage("You don't have root access, it is disabled, or you denied the root request.");
        // Alert dialog button
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Dismiss",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();// use dismiss to cancel alert dialog
                        finish();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onDestroy() {
        MyService.mainActivity = null;
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getBooleanExtra("rooterror",false))  {
            rootpopup();
        }
    }
}
