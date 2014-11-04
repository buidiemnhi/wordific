package com.evilgeniustechnologies.Wordrific.utilties;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by benjamin on 5/23/14.
 */
public class L {
    private static final String TAG = "com.evilgeniustechnologies.Wordrific";
    private static boolean debug = false;

    public static void t(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void e(String message1, String message2) {
        if (debug) {
            Log.e(TAG, message1 + " > " + message2);
        }
    }

    public static void e(String message) {
        if (debug) {
            Log.e(TAG, message);
        }
    }

    public static void e(String message1, String message2, Exception exception) {
        if (debug) {
            Log.e(TAG, message1 + " > " + message2, exception);
        }
    }

    public static void e(String message, int value) {
        L.e(message, String.valueOf(value));
    }

    public static void e(String message, double value) {
        L.e(message, String.valueOf(value));
    }

    public static void e(String message, boolean value) {
        L.e(message, String.valueOf(value));
    }

    public static void e(String message, float value) {
        L.e(message, String.valueOf(value));
    }

    public static void enableDebugging() {
        debug = true;
    }
}
