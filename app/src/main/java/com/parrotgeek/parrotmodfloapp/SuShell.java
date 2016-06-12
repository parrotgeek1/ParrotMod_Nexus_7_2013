package com.parrotgeek.parrotmodfloapp;

import android.util.Log;

import java.io.DataOutputStream;

public class SuShell {
    java.lang.Process proc;
    DataOutputStream dos;

    public SuShell() {
        try {
            proc = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(proc.getOutputStream());
        } catch (Exception e) {
            Log.e("sushell",e.getLocalizedMessage());
        }
    }

    public void run(String cmd) {
        try {
            dos.writeBytes(cmd+"\n");
            dos.flush();
        } catch (Exception e) {
            Log.e("sushell",e.getLocalizedMessage());
        }
    }

    public void end() {
        try {
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            proc.waitFor();
        } catch (Exception e) {
            Log.e("sushell",e.getLocalizedMessage());
        }
    }
}
