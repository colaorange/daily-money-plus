package com.colaorange.dailymoney.core.context;

import android.app.Activity;
import android.preference.PreferenceFragment;

/**
 * @author dennis
 */
public class ContextsFragment extends PreferenceFragment{

    public void trackEvent(String action) {
        Activity activity = getActivity();
        if (activity instanceof ContextsActivity) {
            ((ContextsActivity) activity).trackEvent(action);
        }
    }
}