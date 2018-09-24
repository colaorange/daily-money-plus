package com.colaorange.dailymoney.core.util;

import android.support.annotation.Nullable;
import android.util.Log;

import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.Preference;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * @author dennis
 */
public class Logger {

    private static String LOG_TAG = "daily-money";

    static {
        Log.i(LOG_TAG, "To enable " + LOG_TAG + " debug log, run ");
        //adb shell setprop log.tag.daily-money DEBUG
        Log.i(LOG_TAG, "adb shell setprop log.tag." + LOG_TAG + " DEBUG");
        Log.i(LOG_TAG, "adb logcat -s " + LOG_TAG);
    }

    private static List<String> logList = new LinkedList<String>();


    private static void appendToLogList(char level, String msg, Object... args) {
        Preference pref = Contexts.instance().getPreference();
        if (pref.isLogOn()) {
            appendToLogList0(level, Strings.format(msg, args), null, pref.getLogMaxLine());
        } else if (logList.size() > 0) {
            logList.clear();
        }
    }

    private static void appendToLogList(char level, String msg, Throwable t) {
        Preference pref = Contexts.instance().getPreference();
        if (pref.isLogOn()) {
            appendToLogList0(level, msg, t, pref.getLogMaxLine());
        } else if (logList.size() > 0) {
            logList.clear();
        }
    }

    private static void appendToLogList0(char level, String msg, @Nullable Throwable e, int max) {
        logList.add(level + " " + msg);
        while (logList.size() > max) {
            logList.remove(0);
        }
        if (e != null) {
            StringWriter w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            BufferedReader r = new BufferedReader(new StringReader(w.getBuffer().toString()));
            String s;
            try {
                while ((s = r.readLine()) != null) {
                    logList.add("  " + s);
                    while (logList.size() > max) {
                        logList.remove(0);
                    }
                }
            } catch (Exception x) {
            }
        }
    }

    static public String getLogs() {
        StringBuilder sb = new StringBuilder();
        for (String s : logList) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }
    static public List<String> getLogList() {
        return logList;
    }

    static public void d(String msg) {
        appendToLogList('D', msg);
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, msg);
        }
    }

    static public void d(String msg, Object... args) {
        appendToLogList('D', msg, args);
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            msg = Strings.format(msg, args);
            Log.d(LOG_TAG, msg);
        }
    }

    static public void d(String msg, Throwable t) {
        appendToLogList('D', msg, t);
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, msg, t);
        }
    }

    static public void w(String msg) {
        appendToLogList('W', msg);
        if (Log.isLoggable(LOG_TAG, Log.WARN)) {
            Log.w(LOG_TAG, msg);
        }
    }

    static public void w(String msg, Object... args) {
        appendToLogList('W', msg, args);
        if (Log.isLoggable(LOG_TAG, Log.WARN)) {
            msg = Strings.format(msg, args);
            Log.w(LOG_TAG, msg);
        }
    }

    static public void w(String msg, Throwable t) {
        appendToLogList('W', msg, t);
        if (Log.isLoggable(LOG_TAG, Log.WARN)) {
            Log.w(LOG_TAG, msg, t);
        }
    }

    static public void e(String msg) {
        appendToLogList('E', msg);
        if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
            Log.e(LOG_TAG, msg);
        }
    }

    static public void e(String msg, Object... args) {
        appendToLogList('E', msg, args);
        if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
            msg = Strings.format(msg, args);
            Log.e(LOG_TAG, msg);
        }
    }

    static public void e(String msg, Throwable t) {
        appendToLogList('E', msg, t);
        if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
            Log.e(LOG_TAG, msg, t);
        }
    }

    static public void i(String msg) {
        appendToLogList('I', msg);
        if (Log.isLoggable(LOG_TAG, Log.INFO)) {
            Log.i(LOG_TAG, msg);
        }
    }

    static public void i(String msg, Object... args) {
        appendToLogList('I', msg, args);
        if (Log.isLoggable(LOG_TAG, Log.INFO)) {
            msg = Strings.format(msg, args);
            Log.i(LOG_TAG, msg);
        }
    }

    static public void i(String msg, Throwable t) {
        appendToLogList('I', msg, t);
        if (Log.isLoggable(LOG_TAG, Log.INFO)) {
            Log.i(LOG_TAG, msg, t);
        }
    }
}
