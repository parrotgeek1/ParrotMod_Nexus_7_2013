package com.parrotgeek.parrotmodfloapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SuShell {
    public class StreamGobbler extends Thread {
        InputStream is;
        String type;

        public StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    Log.d(type, line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                Crasher.crash();
            }
        }
    }
    private java.lang.Process proc;
    private DataOutputStream dos;

    public SuShell() {
        try {
            proc = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(proc.getOutputStream());
            // any error message?
            StreamGobbler errorGobbler = new
                    StreamGobbler(proc.getErrorStream(), "sushell ERROR");

            // any output?
            StreamGobbler outputGobbler = new
                    StreamGobbler(proc.getInputStream(), "sushell OUTPUT");

            // kick them off
            errorGobbler.start();
            outputGobbler.start();
        } catch (Exception e) {
            Log.e("sushell",e.getLocalizedMessage());
            Crasher.crash();
        }
    }

    public synchronized void run(String cmd) {
        try {
            dos.writeBytes(cmd+"\n");
            dos.flush();
        } catch (Exception e) {
            Log.e("sushell",e.getLocalizedMessage());
            Crasher.crash();
        }
    }

    public synchronized void end() {
        try {
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            proc.waitFor();
        } catch (Exception e) {
            Log.e("sushell",e.getLocalizedMessage());
            Crasher.crash();
        }
    }
}
