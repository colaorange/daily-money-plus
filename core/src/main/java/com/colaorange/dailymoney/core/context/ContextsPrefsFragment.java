package com.colaorange.dailymoney.core.context;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.bg.TimeTickReceiver;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.HashSet;
import java.util.Set;

/**
 * @author dennis
 */
public class ContextsPrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    boolean dirty = false;
    boolean markRestart = false;

    Set<String> recreateKeys = new HashSet<>();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        final I18N i18n = Contexts.instance().getI18n();
        recreateKeys.add(i18n.string(R.string.pref_theme));
        recreateKeys.add(i18n.string(R.string.pref_text_size));
    }

    public void trackEvent(String action) {
        Activity activity = getActivity();
        if (activity instanceof ContextsActivity) {
            ((ContextsActivity) activity).trackEvent(action);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dirty) {
            Contexts.instance().reloadPreference();
        }
        dirty = false;
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);

        //clear backup error mark, since preference of backup might change.
        Intent intent = new Intent();
        intent.setAction(TimeTickReceiver.ACTION_CLEAR_BACKUP_ERROR);
        getActivity().sendBroadcast(intent);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        dirty = true;
        trackEvent(Contexts.TE.PREFENCE + key);
        if(recreateKeys.contains(key)){
            ((ContextsActivity)getActivity()).markWholeRecreate();
            getActivity().recreate();
        }
    }
}