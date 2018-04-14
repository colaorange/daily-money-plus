package com.colaorange.dailymoney.util;

import android.util.Log;

import com.colaorange.commons.util.Strings;

/**
 * @author dennis
 */
public class Logger {

    static String LOG_TAG = "daily-money";

    static {
        Log.i(LOG_TAG, "To enable "+LOG_TAG+" debug log, run ");
        Log.i(LOG_TAG, "adb shell setprop log.tag."+LOG_TAG+" DEBUG");
        Log.i(LOG_TAG, "adb logcat -s "+LOG_TAG);
    }

    static public void d(String msg) {
        if(Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, msg);
        }
    }

    static public void d(String msg, Object... args) {
        if(Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            msg = Strings.format(msg, args);
            Log.d(LOG_TAG, msg);
        }
    }

    static public void d(String msg, Throwable t) {
        if(Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, msg, t);
        }
    }

    static public void w(String msg) {
        if(Log.isLoggable(LOG_TAG, Log.WARN)) {
            Log.w(LOG_TAG, msg);
        }
    }
    static public void w(String msg, Object... args) {
        if(Log.isLoggable(LOG_TAG, Log.WARN)) {
            msg = Strings.format(msg, args);
            Log.w(LOG_TAG, msg);
        }
    }

    static public void w(String msg, Throwable t) {
        if(Log.isLoggable(LOG_TAG, Log.WARN)) {
            Log.w(LOG_TAG, msg, t);
        }
    }

    static public void e(String msg) {
        if(Log.isLoggable(LOG_TAG, Log.ERROR)) {
            Log.e(LOG_TAG, msg);
        }
    }
    static public void e(String msg, Object... args) {
        if(Log.isLoggable(LOG_TAG, Log.ERROR)) {
            msg = Strings.format(msg, args);
            Log.e(LOG_TAG, msg);
        }
    }

    static public void e(String msg, Throwable t) {
        if(Log.isLoggable(LOG_TAG, Log.ERROR)) {
            Log.e(LOG_TAG, msg, t);
        }
    }

    static public void i(String msg) {
        if(Log.isLoggable(LOG_TAG, Log.INFO)) {
            Log.i(LOG_TAG, msg);
        }
    }
    static public void i(String msg, Object... args) {
        if(Log.isLoggable(LOG_TAG, Log.INFO)) {
            msg = Strings.format(msg, args);
            Log.i(LOG_TAG, msg);
        }
    }

    static public void i(String msg, Throwable t) {
        if(Log.isLoggable(LOG_TAG, Log.INFO)) {
            Log.i(LOG_TAG, msg, t);
        }
    }
}
