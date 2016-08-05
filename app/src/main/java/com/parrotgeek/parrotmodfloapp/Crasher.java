package com.parrotgeek.parrotmodfloapp;

import android.util.Log;

public class Crasher {
    public static void crash() {
        Log.e("CRASHER","INTENTIONAL CRASH FROM UNDEFINED STATE");
        throw new IllegalStateException();
    }
}
